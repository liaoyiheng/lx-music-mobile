package cn.toside.music.liquid.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.toside.music.liquid.player.PlayerUiState
import cn.toside.music.liquid.ui.theme.LocalLiquidColors

enum class MainTab(val label: String, val icon: ImageVector) {
    LIBRARY("资料库", Icons.Rounded.LibraryMusic),
    DISCOVER("发现", Icons.Rounded.AutoAwesome),
    CHARTS("排行", Icons.Rounded.Equalizer),
    SETTINGS("设置", Icons.Rounded.Tune),
    SEARCH("搜索", Icons.Rounded.Search),
}

val DockPillTabs = listOf(MainTab.LIBRARY, MainTab.DISCOVER, MainTab.CHARTS, MainTab.SETTINGS)

@Composable
fun LiquidFloatingDock(
    activeTab: MainTab,
    onSelectTab: (MainTab) -> Unit,
    playerState: PlayerUiState,
    onMiniPlayerClick: () -> Unit,
    onTogglePlay: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = LocalLiquidColors.current
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    0.0f to Color.Transparent,
                    0.48f to colors.background.copy(alpha = 0.28f),
                    1.0f to colors.background.copy(alpha = 0.62f),
                )
            )
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Floating MiniPlayer Glass Capsule (supports left/right elastic swipe to skip track)
        AnimatedVisibility(
            visible = playerState.currentTrack != null,
            enter = fadeIn(tween(200)) + slideInVertically(
                animationSpec = spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow),
            ) { it / 2 },
            exit = fadeOut(tween(150)) + slideOutVertically(tween(150)) { it / 2 },
        ) {
            val track = playerState.currentTrack
            if (track != null) {
                val progress = if (playerState.durationMs > 0L) {
                    (playerState.positionMs.toFloat() / playerState.durationMs.toFloat()).coerceIn(0f, 1f)
                } else 0f

                var swipeOffsetX by remember { mutableFloatStateOf(0f) }
                val animatedSwipeX by animateFloatAsState(
                    targetValue = swipeOffsetX,
                    animationSpec = spring(
                        dampingRatio = 0.66f,
                        stiffness = Spring.StiffnessMedium,
                    ),
                    label = "mini_player_swipe_x",
                )

                val activeLyric = if (playerState.isPlaying) {
                    playerState.lyrics.getOrNull(playerState.activeLyricIndex)?.text?.takeIf { it.isNotBlank() }
                } else null
                val fallbackSubtitle = buildString {
                    append(track.singer.ifBlank { "未知歌手" })
                    if (playerState.activeQualityBadge.isNotBlank()) {
                        append(" · ")
                        append(playerState.activeQualityBadge)
                    }
                }
                val displaySubtitle = activeLyric ?: fallbackSubtitle

                LiquidGlassSurface(
                    shape = RoundedCornerShape(22.dp),
                    blurRadius = 30.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .graphicsLayer { translationX = animatedSwipeX }
                        .pointerInput(track.id) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    when {
                                        swipeOffsetX <= -64f -> {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onSkipNext()
                                        }
                                        swipeOffsetX >= 64f -> {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onSkipPrevious()
                                        }
                                    }
                                    swipeOffsetX = 0f
                                },
                                onDragCancel = { swipeOffsetX = 0f },
                                onHorizontalDrag = { _, dragAmount ->
                                    swipeOffsetX = (swipeOffsetX + dragAmount * 0.55f).coerceIn(-96f, 96f)
                                },
                            )
                        }
                        .bouncyClick(pressedScale = 0.975f, onClick = onMiniPlayerClick),
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CoverArt(
                                url = playerState.resolvedCoverUrl ?: track.picUrl,
                                cornerRadius = 12.dp,
                                placeholderSeed = track.id,
                                modifier = Modifier.size(42.dp),
                            )

                            Spacer(Modifier.width(11.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.name,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                AnimatedContent(
                                    targetState = displaySubtitle,
                                    transitionSpec = {
                                        fadeIn(tween(220)) togetherWith fadeOut(tween(160))
                                    },
                                    label = "mini_subtitle_lyric",
                                ) { line ->
                                    Text(
                                        text = line,
                                        fontSize = 11.5.sp,
                                        color = if (activeLyric != null) colors.accent else colors.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .bouncyClick(pressedScale = 0.86f, onClick = onTogglePlay),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (playerState.isResolvingUrl || playerState.isBuffering) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = colors.accent,
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (playerState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                        contentDescription = "播放/暂停",
                                        tint = colors.textPrimary,
                                        modifier = Modifier.size(26.dp),
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .bouncyClick(pressedScale = 0.86f, onClick = onSkipNext),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.SkipNext,
                                    contentDescription = "下一首",
                                    tint = colors.textPrimary,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        }

                        // Delicate inner glass progress hairline along bottom edge
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(2.dp)
                                .clip(CircleShape)
                                .background(colors.divider),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progress)
                                    .clip(CircleShape)
                                    .background(colors.accent),
                            )
                        }
                    }
                }
            }
        }

        // Signature iOS 26 Liquid Glass TabBar + Detached Circular Search Orb
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left 4-tab glass capsule with gliding spring pill indicator
            LiquidGlassSurface(
                shape = CircleShape,
                blurRadius = 30.dp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                val pillIndex = DockPillTabs.indexOf(activeTab)
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                ) {
                    val slotWidth = maxWidth / DockPillTabs.size
                    val indicatorX by animateDpAsState(
                        targetValue = slotWidth * pillIndex.coerceAtLeast(0),
                        animationSpec = spring(
                            dampingRatio = 0.68f,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                        label = "dock_capsule_x",
                    )
                    val indicatorAlpha by animateFloatAsState(
                        targetValue = if (pillIndex >= 0) 1f else 0f,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "dock_capsule_alpha",
                    )

                    if (indicatorAlpha > 0.01f) {
                        Box(
                            modifier = Modifier
                                .offset(x = indicatorX)
                                .width(slotWidth)
                                .fillMaxHeight()
                                .graphicsLayer { alpha = indicatorAlpha }
                                .clip(CircleShape)
                                .background(
                                    if (colors.isDark) Color(0x28FFFFFF) else Color(0xF2FFFFFF)
                                )
                                .border(
                                    width = 0.7.dp,
                                    color = if (colors.isDark) Color(0x38FFFFFF) else Color(0x20000000),
                                    shape = CircleShape,
                                ),
                        )
                    }

                    Row(modifier = Modifier.fillMaxSize()) {
                        DockPillTabs.forEach { tab ->
                            val selected = activeTab == tab
                            val scale by animateFloatAsState(
                                targetValue = if (selected) 1.05f else 0.96f,
                                animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium),
                                label = "tab_icon_scale",
                            )
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                    ) { onSelectTab(tab) },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label,
                                    tint = if (selected) colors.accent else colors.textSecondary,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .graphicsLayer {
                                            scaleX = scale
                                            scaleY = scale
                                        },
                                )
                                Spacer(Modifier.height(1.dp))
                                Text(
                                    text = tab.label,
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) colors.accent else colors.textSecondary,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }

            // Detached Circular Liquid Glass Search Orb
            val searchSelected = activeTab == MainTab.SEARCH
            LiquidGlassSurface(
                shape = CircleShape,
                blurRadius = 30.dp,
                modifier = Modifier
                    .size(60.dp)
                    .bouncyClick(pressedScale = 0.88f) { onSelectTab(MainTab.SEARCH) },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (searchSelected) colors.accentSoft else Color.Transparent),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "搜索",
                        tint = if (searchSelected) colors.accent else colors.textPrimary,
                        modifier = Modifier.size(25.dp),
                    )
                }
            }
        }
    }
}
