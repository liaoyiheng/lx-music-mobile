package cn.toside.music.liquid.player

import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.palette.graphics.Palette
import cn.toside.music.liquid.data.MusicRepository
import cn.toside.music.liquid.data.model.AudioQuality
import cn.toside.music.liquid.data.model.LyricLine
import cn.toside.music.liquid.data.model.MusicInfo
import cn.toside.music.liquid.data.model.MusicSources
import cn.toside.music.liquid.data.model.PlayMode
import cn.toside.music.liquid.engine.AppLogger
import cn.toside.music.liquid.engine.DesktopLyricManager
import cn.toside.music.liquid.engine.LyricParser
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.random.Random

data class PlayerUiState(
    val currentTrack: MusicInfo? = null,
    val resolvedCoverUrl: String? = null,
    val queue: List<MusicInfo> = emptyList(),
    val queueSourceName: String = "播放队列",
    val queueIndex: Int = -1,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val isResolvingUrl: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playMode: PlayMode = PlayMode.ORDER,
    val activeQualityBadge: String = "",
    val activeSourceBadge: String = "",
    val lyrics: List<LyricLine> = emptyList(),
    val activeLyricIndex: Int = -1,
    val isFavorite: Boolean = false,
    val statusMessage: String? = null,
    val sleepTimerRemainingSec: Int = 0,
    val meshColors: List<Color> = DefaultMeshPalette,
)

val DefaultMeshPalette = listOf(
    Color(0xFF4A2545),
    Color(0xFF223354),
    Color(0xFF7B2846),
    Color(0xFF1D2436),
)

class PlayerController(
    private val context: Context,
    private val repository: MusicRepository,
    private val imageLoader: ImageLoader,
    private val desktopLyricManager: DesktopLyricManager,
) : PlaybackSessionDelegate {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _state = MutableStateFlow(PlayerUiState())
    val state: StateFlow<PlayerUiState> = _state.asStateFlow()
    override val uiState: StateFlow<PlayerUiState> get() = state

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private var currentAudioUrl: String? = null
    private var currentAudioTrackId: String? = null
    private var lastNotificationSubtitle: String? = null

    private var playJob: Job? = null
    private var favoriteJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var consecutiveErrorCount = 0

    init {
        PlaybackCommandBridge.delegate = this
        connectService()
        observeSettings()
        startProgressTicker()
    }

    override fun hasQueue(): Boolean = _state.value.queue.isNotEmpty()
    override fun isFavorite(): Boolean = _state.value.isFavorite
    override fun playMode(): PlayMode = _state.value.playMode
    override fun skipNextFromSystem() = skipNext(autoTriggered = false)
    override fun skipPreviousFromSystem() = skipPrevious()
    override fun toggleFavoriteFromSystem() = toggleCurrentFavorite()
    override fun cyclePlayModeFromSystem() = cyclePlayMode()

    private fun connectService() {
        val token = SessionToken(context, ComponentName(context, LxPlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        controllerFuture = future
        scope.launch {
            runCatching {
                val controller = future.await()
                mediaController = controller
                attachPlayerListener(controller)
            }.onFailure {
                AppLogger.error("Player", "连接 Media3 播放服务失败", it)
            }
        }
    }

    private suspend fun ensureController(): MediaController? {
        mediaController?.let { return it }
        val future = controllerFuture ?: return null
        return runCatching {
            val c = future.await()
            if (mediaController == null) {
                mediaController = c
                attachPlayerListener(c)
            }
            c
        }.getOrNull()
    }

    private fun attachPlayerListener(controller: MediaController) {
        controller.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.update { it.copy(isPlaying = isPlaying) }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        _state.update { it.copy(isBuffering = true) }
                    }
                    Player.STATE_READY -> {
                        consecutiveErrorCount = 0
                        val dur = controller.duration.takeIf { it > 0L } ?: _state.value.durationMs
                        _state.update { it.copy(isBuffering = false, durationMs = dur) }
                    }
                    Player.STATE_ENDED -> {
                        _state.update { it.copy(isBuffering = false, isPlaying = false) }
                        handleTrackEnded()
                    }
                    Player.STATE_IDLE -> {
                        _state.update { it.copy(isBuffering = false) }
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                AppLogger.error("Player", "ExoPlayer 播放出错: ${error.errorCodeName}", error)
                _state.update {
                    it.copy(
                        isBuffering = false,
                        isPlaying = false,
                        statusMessage = "音频流播放失败 (${error.errorCodeName})，尝试切换下一首",
                    )
                }
                consecutiveErrorCount++
                if (consecutiveErrorCount <= 2 && _state.value.queue.size > 1) {
                    scope.launch {
                        delay(1000L)
                        skipNext(autoTriggered = true)
                    }
                }
            }
        })
    }

    private fun observeSettings() {
        scope.launch {
            repository.settingsStore.settings.collectLatest { s ->
                _state.update { it.copy(playMode = s.playMode) }
                desktopLyricManager.applyConfig(s.desktopLyric)
                if (!s.notificationShowLyric && lastNotificationSubtitle != null) {
                    lastNotificationSubtitle = null
                    syncActiveMediaMetadata(lyricLineText = null)
                }
            }
        }
    }

    private fun startProgressTicker() {
        scope.launch {
            while (isActive) {
                val c = mediaController
                val st = _state.value
                if (c != null && st.currentTrack != null) {
                    val pos = c.currentPosition.coerceAtLeast(0L)
                    val dur = c.duration.takeIf { it > 0L } ?: st.durationMs
                    val newIdx = LyricParser.findActiveIndex(st.lyrics, pos)
                    if (newIdx != st.activeLyricIndex) {
                        val line = st.lyrics.getOrNull(newIdx)
                        desktopLyricManager.updateLyricLine(line)
                        if (repository.settingsStore.settings.value.notificationShowLyric) {
                            val text = line?.text?.takeIf { it.isNotBlank() }
                            if (text != lastNotificationSubtitle) {
                                lastNotificationSubtitle = text
                                syncActiveMediaMetadata(lyricLineText = text)
                            }
                        }
                    }
                    _state.update {
                        it.copy(
                            positionMs = pos,
                            durationMs = dur,
                            activeLyricIndex = newIdx,
                        )
                    }
                }
                delay(120L)
            }
        }
    }

    fun playList(
        tracks: List<MusicInfo>,
        startIndex: Int = 0,
        sourceName: String = "播放队列",
        shuffle: Boolean = false,
    ) {
        if (tracks.isEmpty()) return
        val safeIndex = if (shuffle) Random.nextInt(tracks.size) else startIndex.coerceIn(0, tracks.lastIndex)
        if (shuffle) {
            setPlayMode(PlayMode.SHUFFLE)
        }
        _state.update {
            it.copy(
                queue = tracks,
                queueSourceName = sourceName,
                queueIndex = safeIndex,
                statusMessage = null,
            )
        }
        playTrackAtIndex(safeIndex)
    }

    fun playSingleTrack(track: MusicInfo, sourceName: String = "试听队列") {
        val currentQueue = _state.value.queue.toMutableList()
        val existingIdx = currentQueue.indexOfFirst { it.id == track.id }
        if (existingIdx >= 0) {
            playTrackAtIndex(existingIdx)
        } else {
            val insertIdx = (_state.value.queueIndex + 1).coerceIn(0, currentQueue.size)
            currentQueue.add(insertIdx, track)
            _state.update {
                it.copy(
                    queue = currentQueue,
                    queueSourceName = sourceName,
                    queueIndex = insertIdx,
                )
            }
            playTrackAtIndex(insertIdx)
        }
    }

    fun enqueueNext(track: MusicInfo) {
        val currentQueue = _state.value.queue.toMutableList()
        if (currentQueue.isEmpty()) {
            playSingleTrack(track)
            return
        }
        val existing = currentQueue.indexOfFirst { it.id == track.id }
        if (existing >= 0 && existing != _state.value.queueIndex) {
            currentQueue.removeAt(existing)
        }
        val curIdx = currentQueue.indexOfFirst { it.id == _state.value.currentTrack?.id }.coerceAtLeast(0)
        val insertPos = (curIdx + 1).coerceAtMost(currentQueue.size)
        if (currentQueue.getOrNull(curIdx)?.id != track.id) {
            currentQueue.add(insertPos, track)
        }
        _state.update { it.copy(queue = currentQueue, queueIndex = curIdx) }
    }

    fun removeFromQueue(index: Int) {
        val q = _state.value.queue.toMutableList()
        if (index !in q.indices) return
        val curIdx = _state.value.queueIndex
        q.removeAt(index)
        if (q.isEmpty()) {
            mediaController?.stop()
            _state.update {
                it.copy(
                    queue = emptyList(),
                    queueIndex = -1,
                    currentTrack = null,
                    isPlaying = false,
                    lyrics = emptyList(),
                )
            }
            return
        }
        val nextIdx = when {
            index < curIdx -> curIdx - 1
            index == curIdx -> curIdx.coerceAtMost(q.lastIndex)
            else -> curIdx
        }
        _state.update { it.copy(queue = q, queueIndex = nextIdx) }
        if (index == curIdx) {
            playTrackAtIndex(nextIdx)
        }
    }

    fun clearQueue() {
        mediaController?.stop()
        desktopLyricManager.updateLyricLine(null)
        _state.update {
            it.copy(
                queue = emptyList(),
                queueIndex = -1,
                currentTrack = null,
                isPlaying = false,
                isBuffering = false,
                positionMs = 0L,
                durationMs = 0L,
                lyrics = emptyList(),
                activeLyricIndex = -1,
            )
        }
    }

    fun playTrackAtIndex(index: Int) {
        val queue = _state.value.queue
        if (index !in queue.indices) return
        val track = queue[index]

        playJob?.cancel()
        favoriteJob?.cancel()

        favoriteJob = scope.launch {
            repository.observeIsFavorite(track.id).collectLatest { fav ->
                _state.update { it.copy(isFavorite = fav) }
            }
        }

        _state.update {
            it.copy(
                currentTrack = track,
                resolvedCoverUrl = track.picUrl,
                queueIndex = index,
                isResolvingUrl = true,
                isBuffering = true,
                positionMs = 0L,
                durationMs = parseIntervalToMs(track.interval),
                lyrics = emptyList(),
                activeLyricIndex = -1,
                activeSourceBadge = MusicSources.displayName(track.source),
                activeQualityBadge = track.highestQualityBadge(),
                statusMessage = null,
            )
        }
        desktopLyricManager.updateLyricLine(null)

        playJob = scope.launch {
            // 1. Load cover & palette & lyrics concurrently without blocking audio start
            launch { loadCoverAndPalette(track) }
            launch { loadLyrics(track) }

            // 2. Resolve playable audio URL
            val resolved = resolveAudioUrl(track)
            if (resolved == null) {
                val hasApi = repository.userApi.status.value.initialized
                val msg = if (!hasApi) {
                    "未启用自定义音源：请前往「设置 → 自定义音源」导入 .js 音源脚本"
                } else {
                    "当前音源无法解析《${track.name}》，可尝试在更多菜单中换源"
                }
                _state.update {
                    it.copy(
                        isResolvingUrl = false,
                        isBuffering = false,
                        isPlaying = false,
                        statusMessage = msg,
                    )
                }
                return@launch
            }

            val (audioUrl, qualityLabel, actualSource) = resolved
            val controller = ensureController()
            if (controller == null) {
                _state.update {
                    it.copy(
                        isResolvingUrl = false,
                        isBuffering = false,
                        statusMessage = "播放器服务未就绪，请重试",
                    )
                }
                return@launch
            }

            val coverUrl = _state.value.resolvedCoverUrl ?: track.picUrl
            currentAudioUrl = audioUrl
            currentAudioTrackId = track.id
            lastNotificationSubtitle = null
            val mediaItem = buildMediaItem(
                track = track,
                audioUrl = audioUrl,
                coverUrl = coverUrl,
                lyricLineText = null,
            )

            _state.update {
                it.copy(
                    isResolvingUrl = false,
                    activeQualityBadge = qualityLabel,
                    activeSourceBadge = MusicSources.displayName(actualSource),
                    statusMessage = null,
                )
            }

            controller.setMediaItem(mediaItem)
            controller.prepare()
            controller.play()
        }
    }

    private fun buildMediaItem(
        track: MusicInfo,
        audioUrl: String,
        coverUrl: String?,
        lyricLineText: String?,
    ): MediaItem {
        val coverUri = coverUrl?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }
        val artistLine = if (!lyricLineText.isNullOrBlank()) {
            lyricLineText
        } else {
            track.singer.ifBlank { "未知歌手" }
        }
        val metadata = MediaMetadata.Builder()
            .setTitle(track.name)
            .setDisplayTitle(track.name)
            .setArtist(artistLine)
            .setSubtitle(lyricLineText ?: track.albumName.takeIf { it.isNotBlank() })
            .setAlbumTitle(track.albumName.takeIf { it.isNotBlank() })
            .setArtworkUri(coverUri)
            .build()

        return MediaItem.Builder()
            .setMediaId(track.id)
            .setUri(audioUrl)
            .setMediaMetadata(metadata)
            .build()
    }

    /**
     * 无缝热更新当前曲目的系统通知栏 / 锁屏媒体元数据（封面异步解析到达、或开启通知栏歌词时）：
     * Media3 1.5.0 replaceMediaItem 在 URI 不变时直接原位更新 MediaMetadata，完全不打断音频解码播放。
     */
    private fun syncActiveMediaMetadata(lyricLineText: String?) {
        val controller = mediaController ?: return
        val track = _state.value.currentTrack ?: return
        val audioUrl = currentAudioUrl ?: return
        if (currentAudioTrackId != track.id || controller.mediaItemCount == 0) return
        val coverUrl = _state.value.resolvedCoverUrl ?: track.picUrl
        val updated = buildMediaItem(
            track = track,
            audioUrl = audioUrl,
            coverUrl = coverUrl,
            lyricLineText = lyricLineText,
        )
        runCatching {
            controller.replaceMediaItem(0, updated)
        }
    }

    private data class ResolvedAudio(
        val url: String,
        val qualityBadge: String,
        val source: String,
    )

    private suspend fun resolveAudioUrl(track: MusicInfo): ResolvedAudio? = withContext(Dispatchers.IO) {
        // Local file shortcut
        val localPath = track.meta.filePath
        if (!localPath.isNullOrBlank()) {
            return@withContext ResolvedAudio(localPath, "本地", MusicSources.LOCAL)
        }

        val settings = repository.settingsStore.settings.value
        val preferred = settings.preferredQuality

        // Try direct track via active custom User API script
        val directResult = tryResolveWithUserApi(track, preferred)
        if (directResult != null) return@withContext directResult

        // Cross-source automatic fallback (just like desktop/mobile LX Music)
        if (settings.autoFallbackSource && repository.userApi.status.value.initialized) {
            AppLogger.info("Player", "主源解析失败，触发跨源换源匹配: ${track.name} - ${track.singer}")
            val candidates = runCatching { repository.sdk.findMusic(track) }.getOrDefault(emptyList())
            for (candidate in candidates.take(4)) {
                val fallback = tryResolveWithUserApi(candidate, preferred)
                if (fallback != null) {
                    AppLogger.info("Player", "跨源匹配成功: ${candidate.source} -> ${fallback.qualityBadge}")
                    return@withContext fallback
                }
            }
        }
        null
    }

    private suspend fun tryResolveWithUserApi(track: MusicInfo, preferred: AudioQuality): ResolvedAudio? {
        if (!repository.userApi.status.value.initialized) return null
        val supported = repository.userApi.status.value.supportedSources[track.source]
        val qualityOrder = AudioQuality.fallbackOrder(preferred).filter { q ->
            supported == null || supported.isEmpty() || q in supported
        }.ifEmpty { listOf("320k", "128k") }

        for (q in qualityOrder) {
            val url = runCatching { repository.userApi.getMusicUrl(track, q) }.getOrNull()
            if (!url.isNullOrBlank() && (url.startsWith("http://") || url.startsWith("https://"))) {
                val badge = AudioQuality.fromKey(q).badge
                return ResolvedAudio(url, badge, track.source)
            }
        }
        return null
    }

    private suspend fun loadCoverAndPalette(track: MusicInfo) {
        val url = repository.resolvePicUrl(track)
        if (!url.isNullOrBlank() && _state.value.currentTrack?.id == track.id) {
            _state.update { it.copy(resolvedCoverUrl = url) }
            syncActiveMediaMetadata(lyricLineText = lastNotificationSubtitle)
            extractPaletteColors(url, track.id)
        }
    }

    private suspend fun extractPaletteColors(url: String, trackId: String) = withContext(Dispatchers.IO) {
        val req = ImageRequest.Builder(context)
            .data(url)
            .size(160)
            .allowHardware(false)
            .build()
        val result = runCatching { imageLoader.execute(req) }.getOrNull()
        val bmp = ((result as? SuccessResult)?.drawable as? BitmapDrawable)?.bitmap ?: return@withContext
        val palette = Palette.from(bmp).maximumColorCount(12).generate()

        val rawColors = listOfNotNull(
            palette.vibrantSwatch?.rgb,
            palette.darkVibrantSwatch?.rgb,
            palette.lightVibrantSwatch?.rgb,
            palette.mutedSwatch?.rgb,
            palette.dominantSwatch?.rgb,
            palette.darkMutedSwatch?.rgb,
        ).distinct()

        val lifted = rawColors.map { rgb -> liftForFluidMesh(rgb) }
        val final4 = when {
            lifted.size >= 4 -> lifted.take(4)
            lifted.isNotEmpty() -> List(4) { i -> lifted[i % lifted.size] }
            else -> DefaultMeshPalette
        }
        if (_state.value.currentTrack?.id == trackId) {
            _state.update { it.copy(meshColors = final4) }
        }
    }

    /**
     * 智能提亮并增强色度，确保播放页流体渐变背景饱满通透，彻底杜绝死黑发灰。
     */
    private fun liftForFluidMesh(rgb: Int): Color {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(rgb, hsv)
        // Boost saturation moderately and keep luminance in rich atmospheric band [0.26..0.68]
        hsv[1] = (hsv[1] * 1.28f).coerceIn(0.28f, 0.88f)
        hsv[2] = hsv[2].coerceIn(0.28f, 0.66f)
        return Color(android.graphics.Color.HSVToColor(hsv))
    }

    private suspend fun loadLyrics(track: MusicInfo) {
        val lrcData = runCatching { repository.getCachedLyric(track) }.getOrNull() ?: return
        val parsed = withContext(Dispatchers.Default) { LyricParser.parse(lrcData) }
        if (_state.value.currentTrack?.id == track.id) {
            _state.update { it.copy(lyrics = parsed, activeLyricIndex = -1) }
        }
    }

    fun togglePlayPause() {
        val c = mediaController
        if (c == null) {
            val idx = _state.value.queueIndex
            if (idx >= 0) playTrackAtIndex(idx)
            return
        }
        if (c.isPlaying) {
            c.pause()
        } else {
            if (c.playbackState == Player.STATE_IDLE && _state.value.queueIndex >= 0) {
                playTrackAtIndex(_state.value.queueIndex)
            } else {
                c.play()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        val clamped = positionMs.coerceAtLeast(0L)
        mediaController?.seekTo(clamped)
        _state.update { it.copy(positionMs = clamped) }
    }

    fun skipNext(autoTriggered: Boolean = false) {
        val st = _state.value
        val q = st.queue
        if (q.isEmpty()) return

        if (autoTriggered && st.playMode == PlayMode.SINGLE) {
            seekTo(0L)
            mediaController?.play()
            return
        }
        if (autoTriggered && st.playMode == PlayMode.SINGLE_STOP) {
            mediaController?.pause()
            return
        }

        scope.launch {
            val nextIdx = pickNextIndex(q, st.queueIndex, st.playMode)
            playTrackAtIndex(nextIdx)
        }
    }

    fun skipPrevious() {
        val st = _state.value
        val q = st.queue
        if (q.isEmpty()) return
        if (st.positionMs > 4000L) {
            seekTo(0L)
            return
        }
        val prevIdx = if (st.queueIndex <= 0) q.lastIndex else st.queueIndex - 1
        playTrackAtIndex(prevIdx)
    }

    private suspend fun pickNextIndex(queue: List<MusicInfo>, current: Int, mode: PlayMode): Int {
        if (queue.size <= 1) return 0
        var attempts = 0
        var candidate = current
        while (attempts < minOf(queue.size, 8)) {
            candidate = when (mode) {
                PlayMode.SHUFFLE -> {
                    var r = Random.nextInt(queue.size)
                    if (r == current) r = (r + 1) % queue.size
                    r
                }
                else -> (candidate + 1) % queue.size
            }
            if (!repository.isDisliked(queue[candidate])) return candidate
            attempts++
        }
        return (current + 1) % queue.size
    }

    private fun handleTrackEnded() {
        skipNext(autoTriggered = true)
    }

    fun cyclePlayMode() {
        val next = _state.value.playMode.next()
        setPlayMode(next)
    }

    fun setPlayMode(mode: PlayMode) {
        _state.update { it.copy(playMode = mode) }
        scope.launch {
            repository.settingsStore.update { it.copy(playMode = mode) }
        }
    }

    fun toggleCurrentFavorite() {
        val track = _state.value.currentTrack ?: return
        scope.launch {
            val nowFav = repository.toggleFavorite(track)
            _state.update { it.copy(isFavorite = nowFav) }
        }
    }

    fun setSleepTimerMinutes(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            _state.update { it.copy(sleepTimerRemainingSec = 0) }
            return
        }
        var remaining = minutes * 60
        _state.update { it.copy(sleepTimerRemainingSec = remaining) }
        sleepTimerJob = scope.launch {
            while (remaining > 0) {
                delay(1000L)
                remaining--
                _state.update { it.copy(sleepTimerRemainingSec = remaining) }
            }
            mediaController?.pause()
            _state.update { it.copy(statusMessage = "定时关闭已触发，已暂停播放") }
        }
    }

    fun clearStatusMessage() {
        _state.update { it.copy(statusMessage = null) }
    }

    fun replaceCurrentTrackWithAlternate(alternate: MusicInfo) {
        val q = _state.value.queue.toMutableList()
        val idx = _state.value.queueIndex
        if (idx in q.indices) {
            q[idx] = alternate
            _state.update { it.copy(queue = q) }
            playTrackAtIndex(idx)
        } else {
            playSingleTrack(alternate)
        }
    }

    private fun parseIntervalToMs(interval: String?): Long {
        if (interval.isNullOrBlank()) return 0L
        val parts = interval.split(":").mapNotNull { it.trim().toLongOrNull() }
        return when (parts.size) {
            2 -> (parts[0] * 60L + parts[1]) * 1000L
            3 -> (parts[0] * 3600L + parts[1] * 60L + parts[2]) * 1000L
            else -> 0L
        }
    }
}
