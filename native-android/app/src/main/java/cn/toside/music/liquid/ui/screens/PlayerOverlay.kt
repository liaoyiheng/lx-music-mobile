package cn.toside.music.liquid.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.toside.music.liquid.data.model.MusicSources
import cn.toside.music.liquid.data.model.PlayMode
import cn.toside.music.liquid.player.PlayerUiState
import cn.toside.music.liquid.ui.MainViewModel
import cn.toside.music.liquid.ui.components.CoverArt
import cn.toside.music.liquid.ui.components.FluidMeshBackground
import cn.toside.music.liquid.ui.components.LiquidPlaybackScrubber
import cn.toside.music.liquid.ui.components.PlayingEqualizer
import cn.toside.music.liquid.ui.components.bouncyClick
import cn.toside.music.liquid.ui.components.formatMillis
import cn.toside.music.liquid.ui.theme.LocalLiquidColors
import kotlin.math.roundToInt

private const val PLAYER_VIEW_COVER = 0
private const val PLAYER_VIEW_LYRICS = 1
private const val PLAYER_VIEW_QUEUE = 2

@Composable
fun PlayerOverlay(
    vm: MainViewModel,
    onDismiss: () -> Unit,
) {
    val playerState by vm.player.state.collectAsState()
    val settings by vm.settings.collectAsState()
    var activeView by remember { mutableIntStateOf(PLAYER_VIEW_COVER) }

    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val animatedOffsetY by animateFloatAsState(
        targetValue = dragOffsetY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "playerDragY",
    )

    val track = playerState.currentTrack
    val coverUrl = playerState.resolvedCoverUrl ?: track?.picUrl
    val dragProgress = (animatedOffsetY / 720f).coerceIn(0f, 1f)
    val sheetScale = 1f - dragProgress * 0.055f
    val topCornerRadius = (dragProgress * 36f).coerceIn(0f, 36f).dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationY = animatedOffsetY.coerceAtLeast(0f)
                scaleX = sheetScale
                scaleY = sheetScale
                transformOrigin = TransformOrigin(0.5f, 0f)
            }
            .clip(RoundedCornerShape(topStart = topCornerRadius, topEnd = topCornerRadius)),
    ) {
        // 1. Hardware-Accelerated 4-Pole Fluid Mesh Background (Zero Blackout!)
        FluidMeshBackground(
            colors = playerState.meshColors,
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            // 2. Draggable Top Grabber & Header Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                if (dragOffsetY > 180f) {
                                    onDismiss()
                                }
                                dragOffsetY = 0f
                            },
                            onDragCancel = { dragOffsetY = 0f },
                            onVerticalDrag = { _, dragAmount ->
                                val next = (dragOffsetY + dragAmount).coerceAtLeast(0f)
                                dragOffsetY = next
                            },
                        )
                    }
                    .padding(top = 8.dp, bottom = 6.dp, start = 18.dp, end = 18.dp),
            ) {
                // Center Grabber Pill
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .width(40.dp)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.42f))
                        .bouncyClick { onDismiss() },
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.14f))
                            .bouncyClick { onDismiss() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "收起播放页",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    }

                    // Active Source & Quality Glass Pill
                    if (track != null) {
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.14f))
                                .border(0.6.dp, Color.White.copy(alpha = 0.24f), CircleShape)
                                .padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = MusicSources.displayName(
                                    playerState.activeSourceBadge.ifBlank { track.source }
                                ),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White.copy(alpha = 0.92f),
                            )
                            val qBadge = playerState.activeQualityBadge.ifBlank { track.highestQualityBadge() }
                            if (qBadge.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .size(3.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.5f)),
                                )
                                Text(
                                    text = qBadge,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = Color(0xFFFFD166),
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.14f))
                            .bouncyClick { track?.let { vm.openTrackActions(it) } },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreHoriz,
                            contentDescription = "更多操作",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            // 3. Main Content Crossfade (Cover / Lyrics / Queue)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                AnimatedContent(
                    targetState = activeView,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "playerViewSwitch",
                ) { view ->
                    when (view) {
                        PLAYER_VIEW_COVER -> PlayerCoverStage(
                            vm = vm,
                            playerState = playerState,
                            coverUrl = coverUrl,
                        )
                        PLAYER_VIEW_LYRICS -> PlayerLyricsStage(
                            vm = vm,
                            playerState = playerState,
                            coverUrl = coverUrl,
                            showTranslation = settings.showLyricTranslation,
                            showRoma = settings.showLyricRoma,
                            onToggleTranslation = {
                                vm.updateSettings { it.copy(showLyricTranslation = !it.showLyricTranslation) }
                            },
                            onToggleRoma = {
                                vm.updateSettings { it.copy(showLyricRoma = !it.showLyricRoma) }
                            },
                        )
                        PLAYER_VIEW_QUEUE -> PlayerQueueStage(
                            vm = vm,
                            playerState = playerState,
                        )
                    }
                }
            }

            // 4. Compact Playback Bar when in Lyrics or Queue view
            if (activeView != PLAYER_VIEW_COVER) {
                CompactPlayerControls(
                    playerState = playerState,
                    onSeek = { vm.player.seekTo(it) },
                    onPrev = { vm.player.skipPrevious() },
                    onTogglePlay = { vm.player.togglePlayPause() },
                    onNext = { vm.player.skipNext() },
                )
            }

            // 5. Bottom Glass Utility Bar (Lyrics / Comments / Timer / Queue)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PlayerUtilityButton(
                    icon = Icons.Rounded.FormatQuote,
                    label = "歌词",
                    selected = activeView == PLAYER_VIEW_LYRICS,
                    onClick = {
                        activeView = if (activeView == PLAYER_VIEW_LYRICS) PLAYER_VIEW_COVER else PLAYER_VIEW_LYRICS
                    },
                )
                PlayerUtilityButton(
                    icon = Icons.Rounded.ChatBubbleOutline,
                    label = "评论",
                    selected = false,
                    onClick = { track?.let { vm.openComments(it) } },
                )
                val timerText = if (playerState.sleepTimerRemainingSec > 0) {
                    "${(playerState.sleepTimerRemainingSec + 59) / 60}m"
                } else {
                    "定时"
                }
                PlayerUtilityButton(
                    icon = Icons.Rounded.Timer,
                    label = timerText,
                    selected = playerState.sleepTimerRemainingSec > 0,
                    onClick = { vm.openSleepTimer() },
                )
                PlayerUtilityButton(
                    icon = Icons.Rounded.QueueMusic,
                    label = "队列",
                    selected = activeView == PLAYER_VIEW_QUEUE,
                    onClick = {
                        activeView = if (activeView == PLAYER_VIEW_QUEUE) PLAYER_VIEW_COVER else PLAYER_VIEW_QUEUE
                    },
                )
            }
        }
    }
}

@Composable
private fun PlayerCoverStage(
    vm: MainViewModel,
    playerState: PlayerUiState,
    coverUrl: String?,
) {
    val track = playerState.currentTrack
    val colors = LocalLiquidColors.current

    // Classic Apple Music artwork spring scale on play/pause
    val artScale by animateFloatAsState(
        targetValue = if (playerState.isPlaying) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = 0.68f,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "artScale",
    )
    val artElevation by animateDpAsState(
        targetValue = if (playerState.isPlaying) 28.dp else 10.dp,
        label = "artElev",
    )

    var isSeeking by remember { mutableStateOf(false) }
    var seekFraction by remember { mutableFloatStateOf(0f) }

    val durationMs = playerState.durationMs.coerceAtLeast(1L)
    val currentFraction = if (isSeeking) {
        seekFraction
    } else {
        (playerState.positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(0.6f))

        // Floating Cover Art with dynamic scale & colored ambient shadow
        Box(
            modifier = Modifier
                .fillMaxWidth(0.84f)
                .aspectRatio(1f)
                .graphicsLayer {
                    scaleX = artScale
                    scaleY = artScale
                }
                .shadow(
                    elevation = artElevation,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = Color.Black.copy(alpha = 0.65f),
                    spotColor = Color.Black.copy(alpha = 0.72f),
                )
                .clip(RoundedCornerShape(24.dp))
                .border(0.8.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(24.dp)),
        ) {
            CoverArt(
                url = coverUrl,
                contentDescription = track?.name,
                cornerRadius = 24.dp,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Spacer(Modifier.weight(0.75f))

        // Track Title + Artist + Favorite Heart
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = track?.name ?: "未在播放",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.4).sp,
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(3.dp))
                val subtitle = buildString {
                    append(track?.singer?.ifBlank { "未知艺人" } ?: "")
                    if (!track?.albumName.isNullOrBlank()) {
                        append(" — ")
                        append(track?.albumName)
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.74f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.14f))
                    .bouncyClick { vm.player.toggleCurrentFavorite() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (playerState.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = "喜爱",
                    tint = if (playerState.isFavorite) colors.accent else Color.White,
                    modifier = Modifier.size(21.dp),
                )
            }
        }

        if (!playerState.statusMessage.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = playerState.statusMessage,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFFFD166),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(14.dp))

        // Apple Music iOS 26 Elastic Liquid Scrubber (thin-to-thick pill, zero Material dot)
        LiquidPlaybackScrubber(
            progress = currentFraction,
            onScrubbingChange = { scrubbing, frac ->
                isSeeking = scrubbing
                seekFraction = frac
            },
            onScrubFinished = { frac ->
                val targetMs = (frac * durationMs).toLong()
                vm.player.seekTo(targetMs)
                isSeeking = false
            },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(2.dp))
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

        Spacer(Modifier.height(16.dp))

        // Main Transport Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .bouncyClick {
                        vm.player.cyclePlayMode()
                        vm.showToast("播放模式：${vm.player.state.value.playMode.label}")
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = playModeIcon(playerState.playMode),
                    contentDescription = playerState.playMode.label,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(23.dp),
                )
            }

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .bouncyClick { vm.player.skipPrevious() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipPrevious,
                    contentDescription = "上一首",
                    tint = Color.White,
                    modifier = Modifier.size(38.dp),
                )
            }

            // Glass Halo Primary Play/Pause Button
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.20f))
                    .border(1.dp, Color.White.copy(alpha = 0.36f), CircleShape)
                    .bouncyClick(pressedScale = 0.90f) { vm.player.togglePlayPause() },
                contentAlignment = Alignment.Center,
            ) {
                if (playerState.isBuffering || playerState.isResolvingUrl) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.6.dp,
                        modifier = Modifier.size(30.dp),
                    )
                } else {
                    Icon(
                        imageVector = if (playerState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (playerState.isPlaying) "暂停" else "播放",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .bouncyClick { vm.player.skipNext() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = "下一首",
                    tint = Color.White,
                    modifier = Modifier.size(38.dp),
                )
            }

            // Placeholder balanced icon: Alternate Sources quick switch
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .bouncyClick { track?.let { vm.openAlternateSources(it) } },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "换源",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White.copy(alpha = 0.84f),
                )
            }
        }

        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun PlayerLyricsStage(
    vm: MainViewModel,
    playerState: PlayerUiState,
    coverUrl: String?,
    showTranslation: Boolean,
    showRoma: Boolean,
    onToggleTranslation: () -> Unit,
    onToggleRoma: () -> Unit,
) {
    val track = playerState.currentTrack
    val listState = rememberLazyListState()
    val lyrics = playerState.lyrics
    val activeIdx = playerState.activeLyricIndex

    LaunchedEffect(activeIdx, lyrics.size) {
        if (activeIdx in lyrics.indices && !listState.isScrollInProgress) {
            val target = (activeIdx - 2).coerceAtLeast(0)
            runCatching {
                listState.animateScrollToItem(index = target, scrollOffset = 0)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp),
    ) {
        // Compact Track Header + Translation / Romaji toggles
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CoverArt(
                url = coverUrl,
                contentDescription = track?.name,
                cornerRadius = 10.dp,
                modifier = Modifier.size(48.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = track?.name ?: "",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = track?.singer ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(8.dp))
            LyricToggleChip(label = "译", active = showTranslation, onClick = onToggleTranslation)
            Spacer(Modifier.width(6.dp))
            LyricToggleChip(label = "音", active = showRoma, onClick = onToggleRoma)
        }

        // Seamless Alpha-Faded Lyrics Stream (Zero dark bars, Zero relayout)
        if (lyrics.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "暂无同步歌词",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White.copy(alpha = 0.60f),
                )
            }
        } else {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(top = 44.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            brush = Brush.verticalGradient(
                                0.0f to Color.Transparent,
                                0.09f to Color.Black,
                                0.88f to Color.Black,
                                1.0f to Color.Transparent,
                            ),
                            blendMode = BlendMode.DstIn,
                        )
                    },
            ) {
                itemsIndexed(lyrics, key = { idx, line -> "${line.timeMs}_$idx" }) { idx, line ->
                    val isActive = idx == activeIdx
                    val isPast = activeIdx >= 0 && idx < activeIdx
                    val scale by animateFloatAsState(
                        targetValue = if (isActive) 1.05f else 0.94f,
                        animationSpec = spring(
                            dampingRatio = 0.74f,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                        label = "lrcScale",
                    )
                    val alpha by animateFloatAsState(
                        targetValue = when {
                            isActive -> 1f
                            isPast -> 0.42f
                            else -> 0.54f
                        },
                        label = "lrcAlpha",
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                this.alpha = alpha
                                transformOrigin = TransformOrigin(0f, 0.5f)
                            }
                            .clip(RoundedCornerShape(12.dp))
                            .bouncyClick(pressedScale = 0.97f) {
                                vm.player.seekTo(line.timeMs)
                            }
                            .padding(vertical = 4.dp, horizontal = 2.dp),
                    ) {
                        Text(
                            text = line.text,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                lineHeight = 32.sp,
                                letterSpacing = (-0.3).sp,
                            ),
                            color = Color.White,
                        )
                        if (showTranslation && !line.translation.isNullOrBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = line.translation,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 22.sp,
                                ),
                                color = Color.White.copy(alpha = 0.84f),
                            )
                        }
                        if (showRoma && !line.romaji.isNullOrBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = line.romaji,
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                                color = Color.White.copy(alpha = 0.68f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LyricToggleChip(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (active) Color.White else Color.White.copy(alpha = 0.16f))
            .bouncyClick(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = if (active) Color.Black else Color.White,
        )
    }
}

@Composable
private fun PlayerQueueStage(
    vm: MainViewModel,
    playerState: PlayerUiState,
) {
    val queue = playerState.queue
    val colors = LocalLiquidColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "待播清单",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White,
                )
                Text(
                    text = "${playerState.queueSourceName} · 共 ${queue.size} 首",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.70f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f))
                        .bouncyClick {
                            vm.player.cyclePlayMode()
                            vm.showToast("播放模式：${vm.player.state.value.playMode.label}")
                        }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = playModeIcon(playerState.playMode),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = playerState.playMode.label,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f))
                        .bouncyClick { vm.player.clearQueue() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteSweep,
                        contentDescription = "清空队列",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }

        if (queue.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text("播放队列为空", color = Color.White.copy(alpha = 0.65f))
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                itemsIndexed(queue, key = { idx, t -> "q_${t.id}_$idx" }) { idx, item ->
                    val isCurrent = idx == playerState.queueIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isCurrent) Color.White.copy(alpha = 0.16f) else Color.Transparent)
                            .bouncyClick(pressedScale = 0.98f) { vm.player.playTrackAtIndex(idx) }
                            .padding(horizontal = 10.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(modifier = Modifier.size(44.dp)) {
                            CoverArt(
                                url = item.picUrl,
                                contentDescription = item.name,
                                cornerRadius = 9.dp,
                                modifier = Modifier.fillMaxSize(),
                            )
                            if (isCurrent) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(Color.Black.copy(alpha = 0.42f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    PlayingEqualizer(
                                        isPlaying = playerState.isPlaying,
                                        color = Color.White,
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                ),
                                color = if (isCurrent) colors.accent else Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = item.singer.ifBlank { "未知艺人" },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.68f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .bouncyClick { vm.player.removeFromQueue(idx) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "移除",
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactPlayerControls(
    playerState: PlayerUiState,
    onSeek: (Long) -> Unit,
    onPrev: () -> Unit,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
) {
    val durationMs = playerState.durationMs.coerceAtLeast(1L)
    val fraction = (playerState.positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
    ) {
        Slider(
            value = fraction,
            onValueChange = { onSeek((it * durationMs).toLong()) },
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White.copy(alpha = 0.9f),
                inactiveTrackColor = Color.White.copy(alpha = 0.22f),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .bouncyClick(onClick = onPrev),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipPrevious,
                    contentDescription = "上一首",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp),
                )
            }
            Spacer(Modifier.width(24.dp))
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.20f))
                    .bouncyClick(onClick = onTogglePlay),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (playerState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = "播放暂停",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp),
                )
            }
            Spacer(Modifier.width(24.dp))
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .bouncyClick(onClick = onNext),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = "下一首",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp),
                )
            }
        }
    }
}

@Composable
private fun PlayerUtilityButton(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) Color.White.copy(alpha = 0.22f) else Color.Transparent)
            .bouncyClick(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) Color.White else Color.White.copy(alpha = 0.72f),
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 10.sp,
            ),
            color = if (selected) Color.White else Color.White.copy(alpha = 0.68f),
        )
    }
}

private fun playModeIcon(mode: PlayMode): ImageVector = when (mode) {
    PlayMode.ORDER -> Icons.Rounded.Repeat
    PlayMode.SHUFFLE -> Icons.Rounded.Shuffle
    PlayMode.SINGLE -> Icons.Rounded.RepeatOne
    PlayMode.SINGLE_STOP -> Icons.Rounded.StopCircle
}
