package cn.toside.music.liquid.ui.lockscreen

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.core.view.WindowCompat
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.toside.music.liquid.LxLiquidApp
import cn.toside.music.liquid.MainActivity
import cn.toside.music.liquid.data.model.PlayMode
import cn.toside.music.liquid.data.model.ThemeMode
import cn.toside.music.liquid.player.PlayerController
import cn.toside.music.liquid.player.PlayerUiState
import cn.toside.music.liquid.ui.components.CoverArt
import cn.toside.music.liquid.ui.components.FluidMeshBackground
import cn.toside.music.liquid.ui.components.LiquidGlassSurface
import cn.toside.music.liquid.ui.components.LiquidPlaybackScrubber
import cn.toside.music.liquid.ui.components.bouncyClick
import cn.toside.music.liquid.ui.components.formatMillis
import cn.toside.music.liquid.ui.theme.LocalLiquidColors
import cn.toside.music.liquid.ui.theme.LxLiquidTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 沉浸式 iOS 26 Apple Music 液态玻璃锁屏播放页：
 * - 实时 GPU 流体渐变封面背景 + 顶部通透锁屏时钟与日期
 * - 居中悬浮大圆角封面（播放/暂停呼吸弹性缩放）支持单击丝滑切换「封面 / 实时同步歌词」双视图
 * - 底部液态玻璃毛玻璃播控底座（实时模糊后方流体背景）+ 弹性进度条 + 收藏/模式/切歌
 * - 全屏上滑弹性解锁手势，支持系统指纹/面容解锁后自动无痕退出，并支持设置页一键预览
 */
class LockScreenActivity : ComponentActivity() {

    private var isPreviewMode: Boolean = false
    private var unlockReceiverRegistered: Boolean = false

    private val userPresentReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_USER_PRESENT && !isPreviewMode) {
                finishWithoutTransition()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isPreviewMode = intent?.getBooleanExtra(EXTRA_PREVIEW_MODE, false) == true

        configureLockScreenWindow()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
        )

        if (!isPreviewMode) {
            registerUnlockReceiver()
        }

        val app = LxLiquidApp.instance

        setContent {
            val settings by app.settingsStore.settings.collectAsState()
            val playerState by app.playerController.state.collectAsState()
            val hazeState = remember { HazeState() }

            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }

            // If playback was cleared completely while in real lockscreen mode, close cleanly.
            LaunchedEffect(playerState.currentTrack, isPreviewMode) {
                if (!isPreviewMode && playerState.currentTrack == null) {
                    finishWithoutTransition()
                }
            }

            BackHandler {
                dismissLockScreen(openMainPlayer = false)
            }

            LxLiquidTheme(
                themeMode = ThemeMode.DARK,
                materialStyle = settings.materialStyle,
                hazeState = hazeState,
            ) {
                LockScreenRoot(
                    player = app.playerController,
                    playerState = playerState,
                    hazeState = hazeState,
                    isPreviewMode = isPreviewMode,
                    onUnlock = { dismissLockScreen(openMainPlayer = false) },
                    onOpenFullPlayer = { dismissLockScreen(openMainPlayer = true) },
                )
            }
        }
    }

    private fun configureLockScreenWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(false)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        }
    }

    private fun registerUnlockReceiver() {
        if (unlockReceiverRegistered) return
        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(userPresentReceiver, filter, RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(userPresentReceiver, filter)
            }
            unlockReceiverRegistered = true
        }
    }

    private fun dismissLockScreen(openMainPlayer: Boolean) {
        if (isPreviewMode) {
            if (openMainPlayer) launchMainPlayer()
            finish()
            return
        }

        val km = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && km != null && km.isKeyguardLocked) {
            km.requestDismissKeyguard(
                this,
                object : KeyguardManager.KeyguardDismissCallback() {
                    override fun onDismissSucceeded() {
                        if (openMainPlayer) launchMainPlayer()
                        finishWithoutTransition()
                    }

                    override fun onDismissCancelled() {
                        // User cancelled PIN/biometric prompt; stay on lockscreen player.
                    }

                    override fun onDismissError() {
                        finishWithoutTransition()
                    }
                },
            )
        } else {
            if (openMainPlayer) launchMainPlayer()
            finishWithoutTransition()
        }
    }

    private fun launchMainPlayer() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(MainActivity.EXTRA_OPEN_PLAYER, true)
        }
        runCatching { startActivity(intent) }
    }

    private fun finishWithoutTransition() {
        finish()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, android.R.anim.fade_out)
        }
    }

    override fun onDestroy() {
        if (unlockReceiverRegistered) {
            runCatching { unregisterReceiver(userPresentReceiver) }
            unlockReceiverRegistered = false
        }
        super.onDestroy()
    }

    companion object {
        const val EXTRA_PREVIEW_MODE = "cn.toside.music.liquid.LOCKSCREEN_PREVIEW"

        fun createPreviewIntent(context: Context): Intent {
            return Intent(context, LockScreenActivity::class.java).apply {
                putExtra(EXTRA_PREVIEW_MODE, true)
            }
        }
    }
}

@Composable
private fun LockScreenRoot(
    player: PlayerController,
    playerState: PlayerUiState,
    hazeState: HazeState,
    isPreviewMode: Boolean,
    onUnlock: () -> Unit,
    onOpenFullPlayer: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    var showLyricsStage by remember { mutableStateOf(false) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    val animatedDragY by animateFloatAsState(
        targetValue = dragOffsetY,
        animationSpec = spring(
            dampingRatio = 0.76f,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "lockscreen_drag_y",
    )
    val dismissFraction = (-animatedDragY / 420f).coerceIn(0f, 1f)

    // Live wall clock updating cleanly every second
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }
    val timeText = remember(nowMillis / 1000L) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(nowMillis))
    }
    val dateText = remember(nowMillis / 60_000L) {
        SimpleDateFormat("M月d日  EEEE", Locale.CHINESE).format(Date(nowMillis))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (dragOffsetY < -140f) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onUnlock()
                        }
                        dragOffsetY = 0f
                    },
                    onDragCancel = { dragOffsetY = 0f },
                    onVerticalDrag = { _, dragAmount ->
                        // Allow gentle upward drag to unlock
                        val next = (dragOffsetY + dragAmount).coerceIn(-360f, 32f)
                        dragOffsetY = next
                    },
                )
            },
    ) {
        // Layer 1: Fluid Mesh Background feeds HazeState so bottom control deck refracts it in real time
        FluidMeshBackground(
            colors = playerState.meshColors,
            modifier = Modifier
                .fillMaxSize()
                .haze(state = hazeState),
        )

        // Subtle atmospheric vertical scrim for ultra-crisp white typography contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Black.copy(alpha = 0.24f),
                        0.38f to Color.Transparent,
                        0.76f to Color.Black.copy(alpha = 0.28f),
                        1.0f to Color.Black.copy(alpha = 0.46f),
                    ),
                ),
        )

        // Layer 2: Interactive LockScreen Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = animatedDragY * 0.65f
                    alpha = 1f - dismissFraction * 0.42f
                    val s = 1f - dismissFraction * 0.04f
                    scaleX = s
                    scaleY = s
                }
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(10.dp))

            // Top Preview Banner (only when invoked from Settings preview)
            if (isPreviewMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.16f))
                            .border(0.6.dp, Color.White.copy(alpha = 0.28f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                    ) {
                        Text(
                            text = "锁屏播放页预览",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White.copy(alpha = 0.92f),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.16f))
                            .bouncyClick { onUnlock() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "关闭预览",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            // Minimalist iOS 26 Lock Screen Clock & Date
            Text(
                text = dateText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.4.sp,
                ),
                color = Color.White.copy(alpha = 0.78f),
            )
            Text(
                text = timeText,
                fontSize = 62.sp,
                lineHeight = 66.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-2.2).sp,
                color = Color.White.copy(alpha = 0.94f),
            )

            Spacer(Modifier.height(10.dp))

            // Center Interactive Stage: Tap to flip Artwork <-> Live Lyrics
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedContent(
                    targetState = showLyricsStage,
                    transitionSpec = {
                        (fadeIn(tween(260)) + scaleIn(initialScale = 0.94f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))) togetherWith
                            (fadeOut(tween(180)) + scaleOut(targetScale = 0.95f))
                    },
                    label = "lockscreen_stage_switch",
                    modifier = Modifier.fillMaxSize(),
                ) { lyricsMode ->
                    if (!lyricsMode) {
                        LockScreenArtworkStage(
                            playerState = playerState,
                            onToggleLyrics = { showLyricsStage = true },
                        )
                    } else {
                        LockScreenLyricsStage(
                            playerState = playerState,
                            onSeekTo = { player.seekTo(it) },
                            onToggleArtwork = { showLyricsStage = false },
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Bottom Liquid Glass Control Deck (real-time GPU blur over fluid background)
            LockScreenControlDeck(
                player = player,
                playerState = playerState,
                showLyricsStage = showLyricsStage,
                onToggleLyricsStage = { showLyricsStage = !showLyricsStage },
                onOpenFullPlayer = onOpenFullPlayer,
            )

            Spacer(Modifier.height(12.dp))

            // Subtle breathing swipe-up unlock affordance
            SwipeUpUnlockHint(
                isPreviewMode = isPreviewMode,
                onClick = onUnlock,
            )

            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun LockScreenArtworkStage(
    playerState: PlayerUiState,
    onToggleLyrics: () -> Unit,
) {
    val track = playerState.currentTrack
    val coverUrl = playerState.resolvedCoverUrl ?: track?.picUrl
    val artScale by animateFloatAsState(
        targetValue = if (playerState.isPlaying) 1f else 0.87f,
        animationSpec = spring(
            dampingRatio = 0.62f,
            stiffness = Spring.StiffnessLow,
        ),
        label = "lock_art_scale",
    )
    val artShadow by animateDpAsState(
        targetValue = if (playerState.isPlaying) 28.dp else 10.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "lock_art_shadow",
    )

    val activeLine = playerState.lyrics.getOrNull(playerState.activeLyricIndex)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.76f)
                .aspectRatio(1f)
                .graphicsLayer {
                    scaleX = artScale
                    scaleY = artScale
                }
                .shadow(
                    elevation = artShadow,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = Color.Black.copy(alpha = 0.65f),
                    spotColor = Color.Black.copy(alpha = 0.72f),
                )
                .clip(RoundedCornerShape(28.dp))
                .border(0.9.dp, Color.White.copy(alpha = 0.24f), RoundedCornerShape(28.dp))
                .bouncyClick(pressedScale = 0.96f, onClick = onToggleLyrics),
        ) {
            CoverArt(
                url = coverUrl,
                contentDescription = track?.name,
                cornerRadius = 28.dp,
                placeholderSeed = track?.id ?: "lock",
                modifier = Modifier.fillMaxSize(),
            )
        }

        Spacer(Modifier.height(16.dp))

        // Live 2-line floating lyric preview below artwork
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .bouncyClick(pressedScale = 0.98f, onClick = onToggleLyrics),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                targetState = activeLine,
                transitionSpec = {
                    fadeIn(tween(240)) togetherWith fadeOut(tween(180))
                },
                label = "lock_mini_lyric",
            ) { line ->
                if (line != null && line.text.isNotBlank()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = line.text,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                            ),
                            color = Color.White.copy(alpha = 0.94f),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (!line.translation.isNullOrBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = line.translation,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.64f),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                } else {
                    Text(
                        text = if (track != null) "轻点封面切换沉浸歌词" else "当前暂无播放曲目",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.52f),
                    )
                }
            }
        }
    }
}

@Composable
private fun LockScreenLyricsStage(
    playerState: PlayerUiState,
    onSeekTo: (Long) -> Unit,
    onToggleArtwork: () -> Unit,
) {
    val listState = rememberLazyListState()
    val lyrics = playerState.lyrics
    val activeIdx = playerState.activeLyricIndex

    LaunchedEffect(activeIdx) {
        if (activeIdx >= 0 && activeIdx < lyrics.size) {
            runCatching {
                listState.animateScrollToItem(index = activeIdx, scrollOffset = -180)
            }
        }
    }

    if (lyrics.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .bouncyClick(pressedScale = 0.98f, onClick = onToggleArtwork),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "暂无同步歌词\n轻点返回专辑封面",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.62f),
                textAlign = TextAlign.Center,
            )
        }
        return
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(vertical = 84.dp, horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.verticalGradient(
                        0.0f to Color.Transparent,
                        0.15f to Color.Black,
                        0.85f to Color.Black,
                        1.0f to Color.Transparent,
                    ),
                    blendMode = BlendMode.DstIn,
                )
            },
    ) {
        itemsIndexed(lyrics) { idx, line ->
            val isActive = idx == activeIdx
            val scale by animateFloatAsState(
                targetValue = if (isActive) 1.05f else 0.96f,
                animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMedium),
                label = "lock_lrc_scale",
            )
            val textAlpha by animateFloatAsState(
                targetValue = if (isActive) 1f else 0.42f,
                animationSpec = tween(260),
                label = "lock_lrc_alpha",
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        alpha = textAlpha
                    }
                    .clip(RoundedCornerShape(14.dp))
                    .bouncyClick(pressedScale = 0.97f) {
                        onSeekTo(line.timeMs)
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = line.text,
                    fontSize = if (isActive) 21.sp else 18.sp,
                    lineHeight = if (isActive) 28.sp else 24.sp,
                    fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
                if (!line.translation.isNullOrBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = line.translation,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.78f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun LockScreenControlDeck(
    player: PlayerController,
    playerState: PlayerUiState,
    showLyricsStage: Boolean,
    onToggleLyricsStage: () -> Unit,
    onOpenFullPlayer: () -> Unit,
) {
    val colors = LocalLiquidColors.current
    val track = playerState.currentTrack
    var isSeeking by remember { mutableStateOf(false) }
    var seekFraction by remember { mutableFloatStateOf(0f) }

    val durationMs = playerState.durationMs.coerceAtLeast(1L)
    val currentFraction = if (isSeeking) {
        seekFraction
    } else {
        (playerState.positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    }

    LiquidGlassSurface(
        shape = RoundedCornerShape(30.dp),
        dense = true,
        darkOverride = true,
        blurRadius = 32.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            // Track Meta + Favorite Heart
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = track?.name ?: "洛雪音乐",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp,
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        if (playerState.activeQualityBadge.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(alpha = 0.16f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                            ) {
                                Text(
                                    text = playerState.activeQualityBadge,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.92f),
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = track?.singer?.ifBlank { "未知艺人" } ?: "选择一首喜欢的歌开始播放",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.72f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.14f))
                        .bouncyClick(pressedScale = 0.86f) { player.toggleCurrentFavorite() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (playerState.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "收藏",
                        tint = if (playerState.isFavorite) colors.accent else Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Shared Apple Music Elastic Scrubber
            LiquidPlaybackScrubber(
                progress = currentFraction,
                onScrubbingChange = { scrubbing, frac ->
                    isSeeking = scrubbing
                    seekFraction = frac
                },
                onScrubFinished = { frac ->
                    val targetMs = (frac * durationMs).toLong()
                    player.seekTo(targetMs)
                    isSeeking = false
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val displayPos = if (isSeeking) (seekFraction * durationMs).toLong() else playerState.positionMs
                Text(
                    text = formatMillis(displayPos),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = Color.White.copy(alpha = 0.68f),
                )
                Text(
                    text = formatMillis(playerState.durationMs),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = Color.White.copy(alpha = 0.68f),
                )
            }

            Spacer(Modifier.height(12.dp))

            // Main Transport Row: Mode · Prev · Play/Pause · Next · Lyrics/App
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val modeIcon = when (playerState.playMode) {
                    PlayMode.ORDER -> Icons.Rounded.Repeat
                    PlayMode.SHUFFLE -> Icons.Rounded.Shuffle
                    PlayMode.SINGLE -> Icons.Rounded.RepeatOne
                    PlayMode.SINGLE_STOP -> Icons.Rounded.StopCircle
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .bouncyClick(pressedScale = 0.85f) { player.cyclePlayMode() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = modeIcon,
                        contentDescription = playerState.playMode.label,
                        tint = Color.White.copy(alpha = 0.80f),
                        modifier = Modifier.size(22.dp),
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .bouncyClick(pressedScale = 0.84f) { player.skipPrevious() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "上一首",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp),
                    )
                }

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.94f))
                        .bouncyClick(pressedScale = 0.88f) { player.togglePlayPause() },
                    contentAlignment = Alignment.Center,
                ) {
                    if (playerState.isResolvingUrl || playerState.isBuffering) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.4.dp,
                            color = Color(0xFF141416),
                        )
                    } else {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = "播放/暂停",
                            tint = Color(0xFF141416),
                            modifier = Modifier.size(34.dp),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .bouncyClick(pressedScale = 0.84f) { player.skipNext() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "下一首",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp),
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (showLyricsStage) Color.White.copy(alpha = 0.22f) else Color.Transparent)
                            .bouncyClick(pressedScale = 0.86f, onClick = onToggleLyricsStage),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FormatQuote,
                            contentDescription = "切换歌词",
                            tint = Color.White.copy(alpha = if (showLyricsStage) 1f else 0.78f),
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // Quick open full player subtle action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.10f))
                        .bouncyClick(pressedScale = 0.94f, onClick = onOpenFullPlayer)
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.OpenInNew,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.74f),
                        modifier = Modifier.size(13.dp),
                    )
                    Text(
                        text = "进入完整播放页",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.78f),
                    )
                }
            }
        }
    }
}

@Composable
private fun SwipeUpUnlockHint(
    isPreviewMode: Boolean,
    onClick: () -> Unit,
) {
    val infinite = rememberInfiniteTransition(label = "unlock_hint_pulse")
    val bounceY by infinite.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "unlock_chevron_y",
    )

    Column(
        modifier = Modifier
            .bouncyClick(pressedScale = 0.94f, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Rounded.KeyboardArrowUp,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.72f),
            modifier = Modifier
                .size(20.dp)
                .graphicsLayer { translationY = bounceY },
        )
        Text(
            text = if (isPreviewMode) "向上轻滑关闭预览" else "向上轻滑解锁",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.6.sp,
            ),
            color = Color.White.copy(alpha = 0.64f),
        )
    }
}
