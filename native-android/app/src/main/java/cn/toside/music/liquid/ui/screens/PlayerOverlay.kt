package cn.toside.music.liquid.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.toside.music.liquid.data.model.MusicSources
import cn.toside.music.liquid.data.model.PlayMode
import cn.toside.music.liquid.engine.LyricParser
import cn.toside.music.liquid.player.PlayerUiState
import cn.toside.music.liquid.ui.MainViewModel
import cn.toside.music.liquid.ui.adaptive.LocalLiquidWindowClass
import cn.toside.music.liquid.ui.components.CoverArt
import cn.toside.music.liquid.ui.components.FluidMeshBackground
import cn.toside.music.liquid.ui.components.LiquidMotion
import cn.toside.music.liquid.ui.components.LiquidPlaybackScrubber
import cn.toside.music.liquid.ui.components.PlayingEqualizer
import cn.toside.music.liquid.ui.components.bouncyClick
import cn.toside.music.liquid.ui.components.formatMillis
import cn.toside.music.liquid.ui.theme.LocalHazeState
import cn.toside.music.liquid.ui.theme.LocalLiquidColors
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

/** Presentation selected when a MiniPlayer opens Now Playing. */
enum class PlayerOverlayView { Cover, Lyrics, Queue }

@Composable
fun PlayerOverlay(
    vm: MainViewModel,
    initialView: PlayerOverlayView = PlayerOverlayView.Cover,
    onDismiss: () -> Unit,
) {
    val player by vm.player.state.collectAsState()
    val settings by vm.settings.collectAsState()
    val window = LocalLiquidWindowClass.current
    val dismiss by rememberUpdatedState(onDismiss)
    var requestedView by rememberSaveable(initialView) { mutableStateOf(initialView) }
    var dragY by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    val settledY by animateFloatAsState(
        targetValue = dragY,
        animationSpec = LiquidMotion.SpringGentle,
        label = "playerDismissOffset",
    )
    val density = LocalDensity.current
    val displayedY = if (dragging) dragY else settledY
    val dragProgress = (displayedY / with(density) { 280.dp.toPx() }).coerceIn(0f, 1f)
    val track = player.currentTrack
    val coverUrl = player.resolvedCoverUrl ?: track?.picUrl
    val hazeState = remember { HazeState() }

    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationY = displayedY
                scaleX = 1f - dragProgress * 0.045f
                scaleY = scaleX
                transformOrigin = TransformOrigin(0.5f, 0f)
            }
            .clip(RoundedCornerShape(topStart = 28.dp * dragProgress, topEnd = 28.dp * dragProgress)),
    ) {
        FluidMeshBackground(
            colors = player.meshColors,
            modifier = Modifier.fillMaxSize().haze(state = hazeState),
        )
        // A single scrim keeps light cover palettes legible behind the white controls.
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))
        CompositionLocalProvider(LocalHazeState provides hazeState) {
            BoxWithConstraints(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                val short = maxHeight < 440.dp
                val wide = window.prefersTwoPanePlayer && maxWidth >= 584.dp
                val alwaysSplit = wide && (short || window.isCompactHeight)
                val view = if (alwaysSplit && requestedView == PlayerOverlayView.Cover) {
                    PlayerOverlayView.Lyrics
                } else {
                    requestedView
                }
                val dismissDistance = with(density) { (maxHeight * 0.18f).coerceIn(48.dp, 104.dp).toPx() }
                val utilities: @Composable () -> Unit = {
                    PlayerUtilities(
                        player = player,
                        view = view,
                        onSelect = { target ->
                            requestedView = when {
                                target != view -> target
                                alwaysSplit -> if (view == PlayerOverlayView.Queue) PlayerOverlayView.Lyrics else view
                                else -> PlayerOverlayView.Cover
                            }
                        },
                        onComments = { track?.let(vm::openComments) },
                        onTimer = vm::openSleepTimer,
                        showLabels = !wide && !short,
                    )
                }
                val details: @Composable (PlayerOverlayView, Boolean) -> Unit = { panel, inSplit ->
                    when (panel) {
                        PlayerOverlayView.Cover -> Unit
                        PlayerOverlayView.Lyrics -> key(track?.source, track?.id) {
                            PlayerLyricsStage(
                                player = player,
                                coverUrl = coverUrl,
                                wide = inSplit,
                                showTranslation = settings.showLyricTranslation,
                                showRoma = settings.showLyricRoma,
                                onToggleTranslation = {
                                    vm.updateSettings { it.copy(showLyricTranslation = !it.showLyricTranslation) }
                                },
                                onToggleRoma = {
                                    vm.updateSettings { it.copy(showLyricRoma = !it.showLyricRoma) }
                                },
                                onSeek = vm.player::seekTo,
                            )
                        }
                        PlayerOverlayView.Queue -> PlayerQueueStage(vm, player)
                    }
                }
                Column(Modifier.fillMaxSize()) {
                    PlayerHeader(
                        player = player,
                        wide = wide,
                        onDismiss = { dismiss() },
                        onActions = { track?.let(vm::openTrackActions) },
                        utilities = utilities,
                        modifier = Modifier.pointerInput(dismissDistance) {
                            detectVerticalDragGestures(
                                onDragStart = { dragging = true },
                                onVerticalDrag = { _, delta -> dragY = (dragY + delta).coerceAtLeast(0f) },
                                onDragCancel = { dragY = 0f; dragging = false },
                                onDragEnd = {
                                    val shouldDismiss = dragY >= dismissDistance
                                    dragY = 0f
                                    dragging = false
                                    if (shouldDismiss) dismiss()
                                },
                            )
                        },
                    )
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        if (wide) {
                            WidePlayerStage(
                                vm = vm,
                                player = player,
                                coverUrl = coverUrl,
                                view = view,
                                details = details,
                            )
                        } else {
                            AnimatedContent(
                                targetState = view,
                                modifier = Modifier.fillMaxHeight().widthIn(max = 520.dp).fillMaxWidth(),
                                transitionSpec = { LiquidMotion.tabSwitchTransition() },
                                label = "phonePlayerView",
                            ) { page ->
                                if (page == PlayerOverlayView.Cover) {
                                    PlayerCoverStage(vm, player, coverUrl)
                                } else {
                                    Column(Modifier.fillMaxSize()) {
                                        Box(Modifier.weight(1f).fillMaxWidth()) { details(page, false) }
                                        CompactPlayerControls(vm, player, short = short)
                                    }
                                }
                            }
                        }
                    }
                    if (!wide) {
                        Box(
                            Modifier.fillMaxWidth().padding(bottom = if (short) 0.dp else 8.dp),
                            contentAlignment = Alignment.Center,
                        ) { utilities() }
                    }
                }
            }
        }
    }
}

@Composable
private fun WidePlayerStage(
    vm: MainViewModel,
    player: PlayerUiState,
    coverUrl: String?,
    view: PlayerOverlayView,
    details: @Composable (PlayerOverlayView, Boolean) -> Unit,
) {
    BoxWithConstraints(
        Modifier.fillMaxHeight().widthIn(max = 1260.dp).fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        val gap = if (maxWidth >= 780.dp) 40.dp else 16.dp
        val coverWidth = ((maxWidth - gap) * 0.44f).coerceIn(244.dp, 460.dp)
        val split = view != PlayerOverlayView.Cover
        val coverOffset by animateDpAsState(
            targetValue = if (split) 0.dp else (maxWidth - coverWidth) / 2,
            animationSpec = LiquidMotion.springGentle(),
            label = "widePlayerCoverOffset",
        )
        PlayerCoverStage(
            vm = vm,
            player = player,
            coverUrl = coverUrl,
            modifier = Modifier.offset { IntOffset(coverOffset.roundToPx(), 0) }.width(coverWidth),
        )
        var detailView by remember { mutableStateOf(view) }
        if (split) detailView = view
        AnimatedVisibility(
            visible = split,
            modifier = Modifier.align(Alignment.CenterEnd).width(maxWidth - coverWidth - gap).fillMaxHeight(),
            enter = fadeIn(LiquidMotion.SpringGentle),
            exit = fadeOut(LiquidMotion.SpringSnappy),
        ) {
            AnimatedContent(
                targetState = detailView,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = { LiquidMotion.tabSwitchTransition() },
                label = "widePlayerDetails",
            ) { panel -> details(panel, true) }
        }
    }
}

@Composable
private fun PlayerHeader(
    player: PlayerUiState,
    wide: Boolean,
    onDismiss: () -> Unit,
    onActions: () -> Unit,
    utilities: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        Box(Modifier.fillMaxWidth().height(14.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.width(36.dp).height(4.dp).background(Color.White.copy(alpha = 0.48f), CircleShape))
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlayerIconButton(Icons.Rounded.KeyboardArrowDown, "收起播放页", glass = true, onClick = onDismiss)
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { SourceBadge(player) }
            if (wide) utilities()
            PlayerIconButton(
                Icons.Rounded.MoreHoriz, "更多操作", glass = true,
                enabled = player.currentTrack != null, onClick = onActions,
            )
        }
    }
}

@Composable
private fun SourceBadge(player: PlayerUiState) {
    val track = player.currentTrack ?: return
    val source = MusicSources.displayName(player.activeSourceBadge.ifBlank { track.source })
    val quality = player.activeQualityBadge.ifBlank { track.highestQualityBadge() }
    Text(
        text = if (quality.isBlank()) source else "$source · $quality",
        color = Color.White.copy(alpha = 0.92f),
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.13f))
            .border(0.5.dp, Color.White.copy(alpha = 0.23f), CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

@Composable
private fun PlayerCoverStage(
    vm: MainViewModel,
    player: PlayerUiState,
    coverUrl: String?,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier.fillMaxHeight().padding(horizontal = 12.dp)) {
        val short = maxHeight < 330.dp
        val stackArtwork = !short || maxWidth < 205.dp
        val track = player.currentTrack
        val artworkSize = if (stackArtwork) {
            val freeHeight = (maxHeight - if (short) 176.dp else 218.dp).coerceAtLeast(76.dp)
            minOf(maxWidth * 0.88f, freeHeight, 398.dp)
        } else {
            minOf(maxWidth * 0.36f, (maxHeight - 118.dp).coerceIn(70.dp, 174.dp))
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(vertical = if (short) 4.dp else 10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (stackArtwork) {
                PlayerArtwork(player, coverUrl, artworkSize)
                Spacer(Modifier.height(if (short) 8.dp else 18.dp))
                PlayerTrackInfo(vm, player, compact = short)
            } else {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    PlayerArtwork(player, coverUrl, artworkSize)
                    Spacer(Modifier.width(12.dp))
                    PlayerTrackInfo(vm, player, compact = true, modifier = Modifier.weight(1f))
                }
            }
            player.statusMessage?.takeIf { it.isNotBlank() }?.let { status ->
                Text(
                    text = status,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFFFD166),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                )
            }
            Spacer(Modifier.height(if (short) 1.dp else 6.dp))
            PlayerScrubber(player, onSeek = vm.player::seekTo, showTimes = true)
            Spacer(Modifier.height(if (short) 2.dp else 10.dp))
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PlayerIconButton(
                    playModeIcon(player.playMode), "播放模式：${player.playMode.label}",
                    modifier = Modifier.weight(1f), onClick = { cyclePlayerMode(vm) },
                )
                PlayerIconButton(
                    Icons.Rounded.SkipPrevious, "上一首", iconSize = if (short) 30.dp else 36.dp,
                    modifier = Modifier.weight(1f), enabled = player.queue.isNotEmpty(),
                    onClick = vm.player::skipPrevious,
                )
                PlayerPlayButton(
                    player = player,
                    size = if (short) 50.dp else 64.dp,
                    onClick = vm.player::togglePlayPause,
                )
                PlayerIconButton(
                    Icons.Rounded.SkipNext, "下一首", iconSize = if (short) 30.dp else 36.dp,
                    modifier = Modifier.weight(1f), enabled = player.queue.isNotEmpty(),
                    onClick = { vm.player.skipNext() },
                )
                PlayerAction(
                    modifier = Modifier.weight(1f),
                    enabled = track != null,
                    onClick = { track?.let(vm::openAlternateSources) },
                ) {
                    Text("换源", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.84f))
                }
            }
        }
    }
}

@Composable
private fun PlayerArtwork(player: PlayerUiState, coverUrl: String?, size: Dp) {
    val scale by animateFloatAsState(
        targetValue = if (player.isPlaying) 1f else 0.82f,
        animationSpec = LiquidMotion.SpringBouncy,
        label = "playerArtworkScale",
    )
    val elevation by animateDpAsState(
        targetValue = if (player.isPlaying) 24.dp else 8.dp,
        animationSpec = LiquidMotion.springBouncy(),
        label = "playerArtworkElevation",
    )
    val corners = minOf(24.dp, size * 0.13f)
    val shape = RoundedCornerShape(corners)
    CoverArt(
        url = coverUrl,
        contentDescription = player.currentTrack?.name?.let { "${it}的封面" },
        cornerRadius = corners,
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation, shape, clip = false)
            .border(0.6.dp, Color.White.copy(alpha = 0.21f), shape),
    )
}

@Composable
private fun PlayerTrackInfo(
    vm: MainViewModel,
    player: PlayerUiState,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    val track = player.currentTrack
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                text = track?.name ?: "未在播放",
                style = (if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall)
                    .copy(fontWeight = FontWeight.ExtraBold),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (track != null) {
                Text(
                    text = listOf(track.singer.ifBlank { "未知艺人" }, track.albumName)
                        .filter(String::isNotBlank).joinToString(" · "),
                    style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.78f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (track != null) {
            PlayerIconButton(
                icon = if (player.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                label = if (player.isFavorite) "取消喜爱" else "添加到喜爱",
                modifier = Modifier.semantics { selected = player.isFavorite },
                glass = true,
                tint = if (player.isFavorite) LocalLiquidColors.current.accent else Color.White,
                onClick = { vm.player.toggleCurrentFavorite() },
            )
        }
    }
}

@Composable
private fun PlayerScrubber(player: PlayerUiState, onSeek: (Long) -> Unit, showTimes: Boolean) {
    val duration = player.durationMs.coerceAtLeast(0L)
    val fraction = if (duration > 0) (player.positionMs.toFloat() / duration).coerceIn(0f, 1f) else 0f
    val seek: (Float) -> Unit = { value ->
        if (duration > 0) onSeek((value.coerceIn(0f, 1f) * duration).toLong())
    }
    key(player.currentTrack?.source, player.currentTrack?.id) {
        LiquidPlaybackScrubber(
            progress = fraction,
            onScrubbingChange = { _, _ -> },
            onScrubFinished = seek,
            durationMs = duration.takeIf { showTimes },
            positionMs = player.positionMs,
            showRemainingTime = false,
            modifier = Modifier.semantics {
                contentDescription = "播放进度"
                progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f)
                stateDescription = "${formatMillis(player.positionMs)} / ${formatMillis(duration)}"
                if (duration > 0) {
                    setProgress { value ->
                        seek(value)
                        true
                    }
                }
            },
        )
    }
}

@Composable
private fun CompactPlayerControls(vm: MainViewModel, player: PlayerUiState, short: Boolean) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        PlayerScrubber(player, onSeek = vm.player::seekTo, showTimes = !short)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlayerIconButton(
                Icons.Rounded.SkipPrevious, "上一首", iconSize = 30.dp,
                enabled = player.queue.isNotEmpty(), onClick = vm.player::skipPrevious,
            )
            PlayerPlayButton(player, size = 48.dp, onClick = vm.player::togglePlayPause)
            PlayerIconButton(
                Icons.Rounded.SkipNext, "下一首", iconSize = 30.dp,
                enabled = player.queue.isNotEmpty(), onClick = { vm.player.skipNext() },
            )
        }
    }
}

@Composable
private fun PlayerPlayButton(player: PlayerUiState, size: Dp, onClick: () -> Unit) {
    val busy = player.isBuffering || player.isResolvingUrl
    val actionLabel = if (player.isPlaying) "暂停" else "播放"
    PlayerAction(
        modifier = Modifier.size(size).semantics {
            contentDescription = actionLabel
            if (busy) stateDescription = "正在加载"
        },
        glass = true,
        enabled = player.currentTrack != null || player.queue.isNotEmpty(),
        prominent = true,
        onClick = onClick,
    ) {
        if (busy) {
            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(size * 0.42f))
        } else {
            Icon(
                if (player.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(size * 0.59f),
            )
        }
    }
}

@Composable
private fun PlayerUtilities(
    player: PlayerUiState,
    view: PlayerOverlayView,
    onSelect: (PlayerOverlayView) -> Unit,
    onComments: () -> Unit,
    onTimer: () -> Unit,
    showLabels: Boolean,
) {
    val minutes = (player.sleepTimerRemainingSec + 59) / 60
    Row(
        modifier = Modifier.widthIn(max = 360.dp).then(if (showLabels) Modifier.fillMaxWidth() else Modifier),
        horizontalArrangement = if (showLabels) Arrangement.SpaceEvenly else Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlayerUtilityButton(Icons.Rounded.FormatQuote, "歌词", showLabels, selected = view == PlayerOverlayView.Lyrics) {
            onSelect(PlayerOverlayView.Lyrics)
        }
        PlayerUtilityButton(Icons.Rounded.ChatBubbleOutline, "评论", showLabels, enabled = player.currentTrack != null, onClick = onComments)
        PlayerUtilityButton(
            Icons.Rounded.Timer, "定时", showLabels, selected = minutes > 0,
            status = if (minutes > 0) "剩余 $minutes 分钟" else null,
            visibleLabel = if (minutes > 0) "${minutes}m" else "定时",
            onClick = onTimer,
        )
        PlayerUtilityButton(Icons.Rounded.QueueMusic, "队列", showLabels, selected = view == PlayerOverlayView.Queue) {
            onSelect(PlayerOverlayView.Queue)
        }
    }
}

@Composable
private fun PlayerUtilityButton(
    icon: ImageVector,
    label: String,
    showLabel: Boolean,
    selected: Boolean = false,
    enabled: Boolean = true,
    status: String? = null,
    visibleLabel: String = label,
    onClick: () -> Unit,
) {
    PlayerAction(
        modifier = Modifier.width(if (showLabel) 62.dp else 48.dp).semantics {
            if (!showLabel || visibleLabel != label) contentDescription = label
            this.selected = selected
            status?.let { stateDescription = it }
        },
        chosen = selected,
        enabled = enabled,
        onClick = onClick,
    ) {
        if (showLabel) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 4.dp)) {
                Icon(icon, null, tint = playerButtonTint(enabled, selected), modifier = Modifier.size(21.dp))
                Text(visibleLabel, style = MaterialTheme.typography.labelSmall, color = playerButtonTint(enabled, selected))
            }
        } else {
            Icon(icon, null, tint = playerButtonTint(enabled, selected), modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun PlayerIconButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier.size(48.dp),
    iconSize: Dp = 22.dp,
    glass: Boolean = false,
    enabled: Boolean = true,
    tint: Color = playerButtonTint(enabled, false),
    onClick: () -> Unit,
) {
    PlayerAction(modifier, glass = glass, enabled = enabled, onClick = onClick) {
        Icon(icon, label, tint = tint, modifier = Modifier.size(iconSize))
    }
}

@Composable
private fun PlayerAction(
    modifier: Modifier,
    glass: Boolean = false,
    enabled: Boolean = true,
    prominent: Boolean = false,
    chosen: Boolean = false,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier.heightIn(min = 48.dp).semantics { role = Role.Button }.bouncyClick(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (glass || chosen) {
            Box(
                Modifier.matchParentSize().padding(if (prominent) 1.dp else 4.dp)
                    .background(Color.White.copy(alpha = if (prominent || chosen) 0.21f else 0.12f), CircleShape)
                    .border(0.6.dp, Color.White.copy(alpha = if (prominent || chosen) 0.34f else 0.19f), CircleShape),
            )
        }
        content()
    }
}

private fun playerButtonTint(enabled: Boolean, selected: Boolean): Color = Color.White.copy(
    alpha = when {
        !enabled -> 0.38f
        selected -> 1f
        else -> 0.84f
    },
)

private fun cyclePlayerMode(vm: MainViewModel) {
    vm.player.cyclePlayMode()
    vm.showToast("播放模式：${vm.player.state.value.playMode.label}")
}

private fun playModeIcon(mode: PlayMode): ImageVector = when (mode) {
    PlayMode.ORDER -> Icons.Rounded.Repeat
    PlayMode.SHUFFLE -> Icons.Rounded.Shuffle
    PlayMode.SINGLE -> Icons.Rounded.RepeatOne
    PlayMode.SINGLE_STOP -> Icons.Rounded.StopCircle
}

@Composable
private fun PlayerLyricsStage(
    player: PlayerUiState,
    coverUrl: String?,
    wide: Boolean,
    showTranslation: Boolean,
    showRoma: Boolean,
    onToggleTranslation: () -> Unit,
    onToggleRoma: () -> Unit,
    onSeek: (Long) -> Unit,
) {
    val track = player.currentTrack
    val lyrics = player.lyrics
    val activeIndex = LyricParser.findActiveIndex(lyrics, player.positionMs)
    val (hasTranslation, hasRoma) = remember(lyrics) {
        lyrics.any { !it.translation.isNullOrBlank() } to lyrics.any { !it.romaji.isNullOrBlank() }
    }
    val listState = rememberLazyListState()
    var browsing by remember { mutableStateOf(false) }
    val manualScroll = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput && available.y != 0f) browsing = true
                return Offset.Zero
            }
        }
    }
    LaunchedEffect(listState) {
        snapshotFlow { browsing to listState.isScrollInProgress }.collectLatest { (manual, moving) ->
            if (manual && !moving) {
                delay(4_000)
                browsing = false
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = if (wide) 6.dp else 18.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (wide) {
                Text(
                    "歌词", color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    modifier = Modifier.weight(1f).semantics { heading() },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                CoverArt(coverUrl, contentDescription = null, cornerRadius = 9.dp, modifier = Modifier.size(38.dp))
                Spacer(Modifier.width(9.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        track?.name ?: "歌词", color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.semantics { heading() },
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                    if (!track?.singer.isNullOrBlank()) {
                        Text(
                            track?.singer.orEmpty(), color = Color.White.copy(alpha = 0.75f),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1, overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            LyricToggleChip("译", "歌词翻译", showTranslation, hasTranslation, onToggleTranslation)
            LyricToggleChip("音", "歌词音译", showRoma, hasRoma, onToggleRoma)
        }
        if (lyrics.isEmpty()) {
            PlayerPanelEmpty("暂无同步歌词", Modifier.weight(1f))
        } else {
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                val anchor = maxHeight * 0.27f
                val topPadding = 8.dp
                val anchorOffset = with(LocalDensity.current) { (anchor - topPadding).coerceAtLeast(0.dp).roundToPx() }
                val focusIndex = activeIndex.coerceAtLeast(0)
                var hasPositioned by remember(listState) { mutableStateOf(false) }

                LaunchedEffect(lyrics, focusIndex, browsing, anchorOffset, showTranslation, showRoma) {
                    if (!browsing) {
                        val focusedItem = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == focusIndex }
                        when {
                            !hasPositioned -> listState.scrollToItem(focusIndex, -anchorOffset)
                            focusedItem != null -> listState.animateScrollBy(
                                value = (focusedItem.offset - anchorOffset).toFloat(),
                                animationSpec = LiquidMotion.SpringFluid,
                            )
                            else -> listState.animateScrollToItem(focusIndex, -anchorOffset)
                        }
                        hasPositioned = true
                    }
                }

                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(start = 7.dp, top = topPadding, end = 13.dp, bottom = maxHeight - anchor),
                    verticalArrangement = Arrangement.spacedBy(if (maxHeight < 220.dp) 10.dp else 17.dp),
                    modifier = Modifier.fillMaxSize().nestedScroll(manualScroll).lyricEdgeFade(),
                ) {
                    itemsIndexed(lyrics, key = { index, line -> "${line.timeMs}_$index" }) { index, line ->
                        val active = index == activeIndex
                        val distance = kotlin.math.abs(index - focusIndex)
                        val alpha by animateFloatAsState(
                            targetValue = when {
                                active || browsing || !player.isPlaying -> 1f
                                distance <= 1 -> 0.78f
                                distance <= 3 -> 0.60f
                                else -> 0.47f
                            },
                            animationSpec = LiquidMotion.SpringFluid,
                            label = "lyricLineAlpha",
                        )
                        val scale by animateFloatAsState(
                            targetValue = if (active && !browsing) 1.035f else 1f,
                            animationSpec = LiquidMotion.SpringFluid,
                            label = "lyricLineFocus",
                        )

                        Column(
                            Modifier.fillMaxWidth()
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    this.alpha = alpha
                                    transformOrigin = TransformOrigin(0f, 0.5f)
                                }
                                .semantics {
                                    selected = active
                                    role = Role.Button
                                    onClick(label = "跳到 ${formatMillis(line.timeMs)}", action = null)
                                }
                                .bouncyClick(pressedScale = 0.98f) {
                                    onSeek(line.timeMs)
                                    browsing = false
                                }
                                .padding(vertical = 4.dp),
                        ) {
                            Text(
                                line.text,
                                style = (if (wide) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineSmall)
                                    .copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.35).sp),
                                color = Color.White,
                            )
                            if (showRoma && !line.romaji.isNullOrBlank()) {
                                Text(
                                    line.romaji,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.76f),
                                    modifier = Modifier.padding(top = 3.dp),
                                )
                            }
                            if (showTranslation && !line.translation.isNullOrBlank()) {
                                Text(
                                    line.translation,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                    color = Color.White.copy(alpha = 0.87f),
                                    modifier = Modifier.padding(top = 3.dp),
                                )
                            }
                        }
                    }
                }
                if (browsing && activeIndex >= 0) {
                    Box(Modifier.align(Alignment.BottomEnd).padding(end = 4.dp, bottom = 4.dp)) {
                        Text(
                            "回到当前歌词", color = Color.White,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.heightIn(min = 48.dp).clip(CircleShape)
                                .background(Color(0xEF262333)).border(0.6.dp, Color.White.copy(alpha = 0.24f), CircleShape)
                                .bouncyClick { browsing = false }.padding(horizontal = 14.dp, vertical = 15.dp),
                        )
                    }
                }
            }
        }
    }
}

private fun Modifier.lyricEdgeFade(): Modifier = graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        drawRect(
            brush = Brush.verticalGradient(
                0f to Color.Transparent,
                0.08f to Color.Black,
                0.84f to Color.Black,
                1f to Color.Transparent,
            ),
            blendMode = BlendMode.DstIn,
        )
    }

@Composable
private fun LyricToggleChip(label: String, description: String, active: Boolean, enabled: Boolean, onClick: () -> Unit) {
    PlayerAction(
        modifier = Modifier.size(48.dp).semantics {
            contentDescription = description
            role = Role.Switch
            toggleableState = if (active) ToggleableState.On else ToggleableState.Off
            if (!enabled) stateDescription = "此曲暂无${description.removePrefix("歌词")}"
        },
        glass = true,
        chosen = active && enabled,
        enabled = enabled,
        onClick = onClick,
    ) {
        Text(
            label, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = playerButtonTint(enabled, active),
        )
    }
}

@Composable
private fun PlayerQueueStage(vm: MainViewModel, player: PlayerUiState) {
    val queue = player.queue
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = if (queue.isEmpty()) 0 else player.queueIndex.coerceIn(0, queue.lastIndex),
    )
    Column(Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f).padding(start = 8.dp)) {
                Text(
                    "待播清单", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White, modifier = Modifier.semantics { heading() },
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${player.queueSourceName} · 共 ${queue.size} 首", style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.75f), maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
            PlayerIconButton(
                playModeIcon(player.playMode), "播放模式：${player.playMode.label}",
                glass = true, onClick = { cyclePlayerMode(vm) },
            )
            PlayerIconButton(
                Icons.Rounded.DeleteSweep, "清空队列", glass = true, enabled = queue.isNotEmpty(),
                onClick = { vm.player.clearQueue() },
            )
        }
        if (queue.isEmpty()) {
            PlayerPanelEmpty("播放队列为空", Modifier.weight(1f))
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(top = 5.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                itemsIndexed(queue, key = { index, item -> "${item.source}_${item.id}_$index" }) { index, item ->
                    val current = index == player.queueIndex
                    val rowShape = RoundedCornerShape(14.dp)
                    Row(
                        Modifier.fillMaxWidth().clip(rowShape)
                            .background(if (current) Color.White.copy(alpha = 0.13f) else Color.Transparent),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            Modifier.weight(1f).semantics {
                                selected = current
                                onClick(label = "播放 ${item.name}", action = null)
                            }.bouncyClick(pressedScale = 0.98f) { vm.player.playTrackAtIndex(index) }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(Modifier.size(42.dp)) {
                                CoverArt(item.picUrl, contentDescription = null, cornerRadius = 8.dp, modifier = Modifier.fillMaxSize())
                                if (current) {
                                    Box(
                                        Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black.copy(alpha = 0.45f)),
                                        contentAlignment = Alignment.Center,
                                    ) { PlayingEqualizer(isPlaying = player.isPlaying, color = Color.White) }
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    item.name, style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = if (current) FontWeight.Bold else FontWeight.Medium,
                                    ), color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    item.singer.ifBlank { "未知艺人" }, style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.73f), maxLines = 1, overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        PlayerIconButton(Icons.Rounded.Close, "从队列移除 ${item.name}", iconSize = 18.dp) {
                            vm.player.removeFromQueue(index)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerPanelEmpty(text: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.75f))
    }
}
