package cn.toside.music.liquid.player

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import cn.toside.music.liquid.LxLiquidApp
import cn.toside.music.liquid.MainActivity
import cn.toside.music.liquid.R
import cn.toside.music.liquid.data.model.PlayMode
import cn.toside.music.liquid.engine.AppLogger
import cn.toside.music.liquid.engine.HttpClientProvider
import cn.toside.music.liquid.ui.lockscreen.LockScreenActivity
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File

/**
 * 纯原生 Media3 后台音频播放服务：
 * - 自定义 QueueForwardingPlayer：通知栏/系统锁屏/蓝牙耳机/线控的上一首与下一首直通 LX 跨源解析队列
 * - CoilMediaBitmapLoader：带防盗链 Referer 的高清封面直出通知栏与锁屏媒体卡片
 * - DefaultMediaNotificationProvider：自定义 24dp 棱镜声浪单色状态栏小图标 + 独立播放通知渠道
 * - 自定义通知栏交互按钮：❤️ 收藏/取消收藏 + 🔁 播放模式切换，状态与 App/锁屏实时双向同步
 * - 息屏监听：播放中锁屏且用户开启「液态玻璃锁屏页」时自动拉起沉浸式锁屏播放界面
 */
class LxPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var mediaCache: SimpleCache? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var screenOffReceiverRegistered = false
    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != Intent.ACTION_SCREEN_OFF) return
            val app = runCatching { LxLiquidApp.instance }.getOrNull() ?: return
            val enabled = app.settingsStore.settings.value.lockScreenEnabled
            val playerState = app.playerController.state.value
            val exoPlaying = mediaSession?.player?.isPlaying == true
            if (enabled && (exoPlaying || playerState.isPlaying) && playerState.currentTrack != null) {
                runCatching {
                    val lockIntent = Intent(this@LxPlaybackService, LockScreenActivity::class.java).apply {
                        addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                                Intent.FLAG_ACTIVITY_NO_ANIMATION,
                        )
                    }
                    startActivity(lockIntent)
                }.onFailure {
                    AppLogger.warn("LockScreen", "息屏唤起液态玻璃锁屏页失败: ${it.message}")
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        // 1. 配置自定义通知渠道与棱镜声浪 24dp 状态栏小图标
        val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
            .setChannelId(CHANNEL_ID)
            .setChannelName(R.string.notification_channel_playback)
            .build()
            .apply {
                setSmallIcon(R.drawable.ic_notification)
            }
        setMediaNotificationProvider(notificationProvider)

        // 2. 构建带磁盘缓存和平台防盗链的 ExoPlayer
        val cacheDir = File(cacheDir, "media_cache")
        val maxCacheMb = runCatching {
            LxLiquidApp.instance.settingsStore.settings.value.audioCacheMaxMb
        }.getOrDefault(512).coerceIn(64, 4096)
        val evictor = LeastRecentlyUsedCacheEvictor(maxCacheMb.toLong() * 1024L * 1024L)
        val dbProvider = StandaloneDatabaseProvider(this)
        val cache = SimpleCache(cacheDir, evictor, dbProvider)
        mediaCache = cache

        val httpFactory = OkHttpDataSource.Factory(HttpClientProvider.mediaOkHttpClient)
        val upstreamFactory = DefaultDataSource.Factory(this, httpFactory)
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val exoPlayer = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()

        // 3. 包装 QueueForwardingPlayer：打通通知栏/锁屏/蓝牙上一首下一首
        val forwardingPlayer = QueueForwardingPlayer(exoPlayer)

        // 4. 点击通知栏卡片直接拉起全屏液态流体播放页
        val openPlayerIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_OPEN_PLAYER, true)
        }
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            1001,
            openPlayerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val initialFav = PlaybackCommandBridge.delegate?.isFavorite() ?: false
        val initialMode = PlaybackCommandBridge.delegate?.playMode() ?: PlayMode.ORDER

        val bitmapLoader = CoilMediaBitmapLoader(
            context = this,
            imageLoader = LxLiquidApp.instance.coilImageLoader,
        )

        val session = MediaSession.Builder(this, forwardingPlayer)
            .setSessionActivity(sessionActivityPendingIntent)
            .setBitmapLoader(bitmapLoader)
            .setCallback(LxMediaSessionCallback())
            .setCustomLayout(buildCustomButtons(initialFav, initialMode))
            .build()
        mediaSession = session

        observePlaybackUiForNotificationButtons(session)
        registerScreenOffReceiver()
    }

    @OptIn(UnstableApi::class)
    private fun observePlaybackUiForNotificationButtons(session: MediaSession) {
        serviceScope.launch {
            val app = runCatching { LxLiquidApp.instance }.getOrNull() ?: return@launch
            app.playerController.state
                .map { Triple(it.isFavorite, it.playMode, it.queue.isNotEmpty()) }
                .distinctUntilChanged()
                .collectLatest { (isFav, playMode, _) ->
                    session.setCustomLayout(buildCustomButtons(isFav, playMode))
                }
        }
    }

    private fun registerScreenOffReceiver() {
        if (screenOffReceiverRegistered) return
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(screenOffReceiver, filter, RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(screenOffReceiver, filter)
            }
            screenOffReceiverRegistered = true
        }
    }

    @OptIn(UnstableApi::class)
    private fun buildCustomButtons(
        isFavorite: Boolean,
        playMode: PlayMode,
    ): List<CommandButton> {
        val favoriteButton = CommandButton.Builder(
            if (isFavorite) CommandButton.ICON_HEART_FILLED else CommandButton.ICON_HEART_UNFILLED,
        )
            .setDisplayName(if (isFavorite) "取消收藏" else "收藏歌曲")
            .setSessionCommand(COMMAND_TOGGLE_FAVORITE)
            .setSlots(CommandButton.SLOT_BACK_SECONDARY, CommandButton.SLOT_OVERFLOW)
            .build()

        val modeIcon = when (playMode) {
            PlayMode.ORDER -> CommandButton.ICON_REPEAT_ALL
            PlayMode.SHUFFLE -> CommandButton.ICON_SHUFFLE_ON
            PlayMode.SINGLE -> CommandButton.ICON_REPEAT_ONE
            PlayMode.SINGLE_STOP -> CommandButton.ICON_REPEAT_OFF
        }
        val playModeButton = CommandButton.Builder(modeIcon)
            .setDisplayName("模式：${playMode.label}")
            .setSessionCommand(COMMAND_CYCLE_PLAY_MODE)
            .setSlots(CommandButton.SLOT_FORWARD_SECONDARY, CommandButton.SLOT_OVERFLOW)
            .build()

        return listOf(favoriteButton, playModeButton)
    }

    @OptIn(UnstableApi::class)
    private inner class LxMediaSessionCallback : MediaSession.Callback {

        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
        ): MediaSession.ConnectionResult {
            val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS
                .buildUpon()
                .add(COMMAND_TOGGLE_FAVORITE)
                .add(COMMAND_CYCLE_PLAY_MODE)
                .build()

            val playerCommands = MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS
                .buildUpon()
                .add(Player.COMMAND_SEEK_TO_NEXT)
                .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                .build()

            val fav = PlaybackCommandBridge.delegate?.isFavorite() ?: false
            val mode = PlaybackCommandBridge.delegate?.playMode() ?: PlayMode.ORDER

            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCommands)
                .setAvailablePlayerCommands(playerCommands)
                .setCustomLayout(buildCustomButtons(fav, mode))
                .build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle,
        ): ListenableFuture<SessionResult> {
            val delegate = PlaybackCommandBridge.delegate
            when (customCommand.customAction) {
                ACTION_TOGGLE_FAVORITE -> {
                    delegate?.toggleFavoriteFromSystem()
                }
                ACTION_CYCLE_PLAY_MODE -> {
                    delegate?.cyclePlayModeFromSystem()
                }
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }

    /**
     * 队列转发播放器：
     * 由于 LX Music 每一首曲目的真实音频流需按需通过自定义 JS 音源解析，ExoPlayer 内部仅持有当前曲目；
     * 通过 ForwardingPlayer 显式声明上一首/下一首能力并转发给 PlayerController，让通知栏、锁屏与蓝牙切歌完全可用。
     */
    @OptIn(UnstableApi::class)
    private class QueueForwardingPlayer(player: Player) : ForwardingPlayer(player) {

        private fun hasQueue(): Boolean = PlaybackCommandBridge.delegate?.hasQueue() == true

        override fun getAvailableCommands(): Player.Commands {
            val base = super.getAvailableCommands()
            return if (hasQueue()) {
                base.buildUpon()
                    .add(COMMAND_SEEK_TO_NEXT)
                    .add(COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                    .add(COMMAND_SEEK_TO_PREVIOUS)
                    .add(COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                    .build()
            } else {
                base
            }
        }

        override fun isCommandAvailable(command: Int): Boolean {
            if (hasQueue() && (
                    command == COMMAND_SEEK_TO_NEXT ||
                        command == COMMAND_SEEK_TO_NEXT_MEDIA_ITEM ||
                        command == COMMAND_SEEK_TO_PREVIOUS ||
                        command == COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM
                    )
            ) {
                return true
            }
            return super.isCommandAvailable(command)
        }

        override fun hasNextMediaItem(): Boolean = hasQueue() || super.hasNextMediaItem()

        override fun hasPreviousMediaItem(): Boolean = hasQueue() || super.hasPreviousMediaItem()

        override fun seekToNext() {
            val delegate = PlaybackCommandBridge.delegate
            if (delegate != null && delegate.hasQueue()) {
                delegate.skipNextFromSystem()
            } else {
                super.seekToNext()
            }
        }

        override fun seekToNextMediaItem() = seekToNext()

        override fun seekToPrevious() {
            val delegate = PlaybackCommandBridge.delegate
            if (delegate != null && delegate.hasQueue()) {
                delegate.skipPreviousFromSystem()
            } else {
                super.seekToPrevious()
            }
        }

        override fun seekToPreviousMediaItem() = seekToPrevious()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    @OptIn(UnstableApi::class)
    override fun onDestroy() {
        if (screenOffReceiverRegistered) {
            runCatching { unregisterReceiver(screenOffReceiver) }
            screenOffReceiverRegistered = false
        }
        serviceScope.cancel()
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        runCatching { mediaCache?.release() }
        mediaCache = null
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "lx_music_playback_v1"
        const val ACTION_TOGGLE_FAVORITE = "cn.toside.music.liquid.ACTION_TOGGLE_FAVORITE"
        const val ACTION_CYCLE_PLAY_MODE = "cn.toside.music.liquid.ACTION_CYCLE_PLAY_MODE"

        val COMMAND_TOGGLE_FAVORITE = SessionCommand(ACTION_TOGGLE_FAVORITE, Bundle.EMPTY)
        val COMMAND_CYCLE_PLAY_MODE = SessionCommand(ACTION_CYCLE_PLAY_MODE, Bundle.EMPTY)
    }
}
