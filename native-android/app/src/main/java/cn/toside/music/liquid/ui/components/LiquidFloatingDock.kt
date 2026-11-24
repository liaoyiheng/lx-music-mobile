package cn.toside.music.liquid.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.toside.music.liquid.player.PlayerUiState
import cn.toside.music.liquid.ui.theme.LocalLiquidColors
import kotlinx.coroutines.delay
import kotlin.math.abs

enum class MainTab(val label: String, val icon: ImageVector) {
    LIBRARY("资料库", Icons.Rounded.LibraryMusic),
    DISCOVER("发现", Icons.Rounded.AutoAwesome),
    CHARTS("排行", Icons.Rounded.Equalizer),
    SETTINGS("设置", Icons.Rounded.Tune),
    SEARCH("搜索", Icons.Rounded.Search),
}

val primaryTabs = listOf(MainTab.LIBRARY, MainTab.DISCOVER, MainTab.CHARTS, MainTab.SETTINGS)
val DockPillTabs = primaryTabs

/**
 * iOS 26 悬浮双层液态玻璃底座：
 * - 上层：悬浮胶囊 MiniPlayer（支持详情页沉浸色调融入、左右跟手阻尼滑切歌、点击或上滑打开全屏播放页）
 * - 下层：左侧 4 项悬浮玻璃胶囊（资料库/发现/排行/设置，滑块按 SpringSnappy 弹性滑行并带轻度横向拉伸形变）+ 右侧独立圆形「搜索」玻璃钮
 * - 搜索变形模式（[isSearchActive]）：左侧 4-Tab 胶囊收缩淡出，搜索圆钮横向弹性延展成全宽玻璃搜索输入框，右侧浮现圆形「×」玻璃钮
 * - 完全向后兼容原有调用方参数
 */
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
    immersiveTint: Color? = null,
    isSearchActive: Boolean = false,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onSearchSubmit: () -> Unit = {},
    onOpenSearch: () -> Unit = { onSelectTab(MainTab.SEARCH) },
    onCloseSearch: () -> Unit = {
        if (activeTab == MainTab.SEARCH) onSelectTab(MainTab.DISCOVER)
    },
    onSwipePrevious: (() -> Unit)? = null,
    onSwipeNext: (() -> Unit)? = null,
    searchPlaceholder: String = "艺人、歌曲、歌词及更多内容",
    autoFocusSearch: Boolean = false,
    showDockBar: Boolean = true,
    isCollapsed: Boolean = false,
    onExpand: () -> Unit = {},
) {
    val colors = LocalLiquidColors.current
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isSearchActive, autoFocusSearch) {
        if (isSearchActive && autoFocusSearch) {
            delay(120L)
            runCatching { focusRequester.requestFocus() }
        }
    }

    val animatedImmersiveTint by animateColorAsState(
        targetValue = immersiveTint ?: colors.background,
        animationSpec = LiquidMotion.springFluid(),
        label = "dock_immersive_scrim",
    )
    val isImmersive = immersiveTint != null
    val dockDarkOverride = if (isImmersive) true else null
    val dockGlassTintOverride = immersiveTint?.let { tint ->
        lerp(Color(0xD414161F), tint.copy(alpha = 1f), 0.34f).copy(alpha = 0.80f)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    0.0f to Color.Transparent,
                    0.42f to animatedImmersiveTint.copy(
                        alpha = if (isImmersive) 0.38f else if (colors.isDark) 0.42f else 0.56f,
                    ),
                    1.0f to animatedImmersiveTint.copy(
                        alpha = if (isImmersive) 0.82f else if (colors.isDark) 0.78f else 0.92f,
                    ),
                ),
            )
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (isCollapsed && showDockBar && !isSearchActive) {
            Row(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DockShortcut(activeTab, "展开导航 · ${activeTab.label}", onExpand)
                Box(Modifier.weight(1f)) {
                    LiquidMiniPlayerCapsule(
                        playerState = playerState,
                        onMiniPlayerClick = onMiniPlayerClick,
                        onTogglePlay = onTogglePlay,
                        onSkipNext = onSkipNext,
                        onSwipeNext = onSwipeNext ?: onSkipNext,
                        onSwipePrevious = onSwipePrevious ?: onSkipPrevious,
                        immersiveTint = immersiveTint,
                        inline = true,
                    )
                }
                DockShortcut(MainTab.SEARCH, MainTab.SEARCH.label, onOpenSearch)
            }
            return@Column
        }

        // Floating MiniPlayer Glass Capsule
        AnimatedVisibility(
            visible = playerState.currentTrack != null,
            enter = fadeIn(tween(200)) + slideInVertically(
                animationSpec = LiquidMotion.springGentle(),
            ) { it / 2 },
            exit = fadeOut(tween(150)) + slideOutVertically(tween(150)) { it / 2 },
        ) {
            LiquidMiniPlayerCapsule(
                playerState = playerState,
                onMiniPlayerClick = onMiniPlayerClick,
                onTogglePlay = onTogglePlay,
                onSkipNext = onSkipNext,
                onSwipeNext = onSwipeNext ?: onSkipNext,
                onSwipePrevious = onSwipePrevious ?: onSkipPrevious,
                immersiveTint = immersiveTint,
            )
        }

        if (showDockBar) {
            val morphProgress by animateFloatAsState(
                targetValue = if (isSearchActive) 1f else 0f,
                animationSpec = LiquidMotion.springGentle(),
                label = "dock_search_morph",
            )
            val p = morphProgress.coerceIn(0f, 1f)

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
            ) {
                val orbSize = 58.dp
                val gap = 10.dp
                val dockCapsuleWidth = (maxWidth - orbSize - gap).coerceAtLeast(180.dp)

                // 1. Left 4-Tab Pill Capsule (shrinks & fades out smoothly during search morph)
                if (p < 0.99f) {
                    val capsuleAlpha = (1f - p * 1.45f).coerceIn(0f, 1f)
                    val capsuleScaleX = (1f - p * 0.22f).coerceIn(0.75f, 1f)
                    val capsuleScaleY = (1f - p * 0.10f).coerceIn(0.88f, 1f)

                    LiquidGlassSurface(
                        shape = CircleShape,
                        blurRadius = 30.dp,
                        darkOverride = dockDarkOverride,
                        tintOverride = dockGlassTintOverride,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .width(dockCapsuleWidth)
                            .fillMaxHeight()
                            .graphicsLayer {
                                alpha = capsuleAlpha
                                scaleX = capsuleScaleX
                                scaleY = capsuleScaleY
                                transformOrigin = TransformOrigin(0f, 0.5f)
                            },
                    ) {
                        val pillIndex = primaryTabs.indexOf(activeTab)
                        val targetIndex = pillIndex.coerceAtLeast(0).toFloat()
                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                        ) {
                            val slotWidth = maxWidth / primaryTabs.size
                            val animatedIndex by animateFloatAsState(
                                targetValue = targetIndex,
                                animationSpec = LiquidMotion.springSnappy(),
                                label = "dock_capsule_idx",
                            )
                            val indicatorAlpha by animateFloatAsState(
                                targetValue = if (pillIndex >= 0) 1f else 0f,
                                animationSpec = LiquidMotion.springSnappy(),
                                label = "dock_capsule_alpha",
                            )
                            val travelDelta = abs(targetIndex - animatedIndex).coerceIn(0f, 1.4f)
                            val stretchX = 1f + travelDelta * 0.15f
                            val stretchY = 1f - travelDelta * 0.04f

                            if (indicatorAlpha > 0.01f) {
                                Box(
                                    modifier = Modifier
                                        .offset(x = slotWidth * animatedIndex)
                                        .width(slotWidth)
                                        .fillMaxHeight()
                                        .graphicsLayer {
                                            alpha = indicatorAlpha
                                            scaleX = stretchX
                                            scaleY = stretchY
                                        }
                                        .clip(CircleShape)
                                        .background(
                                            if (isImmersive) Color(0x2AFFFFFF) else colors.glassPillActive,
                                        )
                                        .border(
                                            width = 0.6.dp,
                                            color = if (isImmersive || colors.isDark) Color(0x34FFFFFF) else Color(0x28FFFFFF),
                                            shape = CircleShape,
                                        ),
                                )
                            }

                            Row(modifier = Modifier.fillMaxSize()) {
                                primaryTabs.forEach { tab ->
                                    val selected = activeTab == tab
                                    val iconScale by animateFloatAsState(
                                        targetValue = if (selected) 1.08f else 0.96f,
                                        animationSpec = LiquidMotion.springBouncy(),
                                        label = "tab_icon_scale_${tab.name}",
                                    )
                                    val iconLift by animateDpAsState(
                                        targetValue = if (selected) (-1).dp else 0.dp,
                                        animationSpec = LiquidMotion.springBouncy(),
                                        label = "tab_icon_lift_${tab.name}",
                                    )
                                    val idleTabColor = when {
                                        isImmersive -> colors.immersiveTextSecondary
                                        colors.isDark -> colors.textSecondary
                                        else -> Color(0xFF3A3A3C)
                                    }

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null,
                                                enabled = !isSearchActive,
                                            ) {
                                                if (!selected) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                }
                                                onSelectTab(tab)
                                            },
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                    ) {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.label,
                                            tint = if (selected) colors.accent else idleTabColor,
                                            modifier = Modifier
                                                .size(21.dp)
                                                .offset(y = iconLift)
                                                .graphicsLayer {
                                                    scaleX = iconScale
                                                    scaleY = iconScale
                                                },
                                        )
                                        Spacer(Modifier.height(1.5.dp))
                                        Text(
                                            text = tab.label,
                                            fontSize = 10.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                            color = if (selected) colors.accent else idleTabColor,
                                            maxLines = 1,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Morphing Search Orb -> Full-Width Glass Search Bar
                val expandedSearchWidth = (maxWidth - orbSize - gap).coerceAtLeast(180.dp)
                val currentSearchWidth = orbSize + (expandedSearchWidth - orbSize) * p
                val currentSearchOffsetX = (maxWidth - orbSize) * (1f - p)
                val searchOrbActive = activeTab == MainTab.SEARCH && !isSearchActive
                val orbIconAlpha = (1f - p * 2.4f).coerceIn(0f, 1f)
                val fieldAlpha = ((p - 0.16f) / 0.54f).coerceIn(0f, 1f)

                val searchOrbTextColor = when {
                    isImmersive -> colors.immersiveTextPrimary
                    else -> colors.textPrimary
                }
                val searchOrbHintColor = when {
                    isImmersive -> colors.immersiveTextSecondary
                    else -> colors.textTertiary
                }

                LiquidGlassSurface(
                    shape = CircleShape,
                    dense = isSearchActive,
                    blurRadius = 30.dp,
                    darkOverride = dockDarkOverride,
                    tintOverride = dockGlassTintOverride,
                    modifier = Modifier
                        .offset(x = currentSearchOffsetX)
                        .width(currentSearchWidth)
                        .fillMaxHeight()
                        .let { m ->
                            if (!isSearchActive) {
                                m.liquidPress(
                                    pressedScale = 0.89f,
                                    highlightAlpha = 0.18f,
                                    shape = CircleShape,
                                    haptic = true,
                                    onClick = onOpenSearch,
                                )
                            } else {
                                m
                            }
                        },
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (searchOrbActive) colors.accentSoft else Color.Transparent),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (orbIconAlpha > 0.01f) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "搜索",
                                tint = if (searchOrbActive) colors.accent else searchOrbTextColor,
                                modifier = Modifier
                                    .size(24.dp)
                                    .graphicsLayer { alpha = orbIconAlpha },
                            )
                        }

                        if (fieldAlpha > 0.01f) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { alpha = fieldAlpha }
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = null,
                                    tint = searchOrbHintColor,
                                    modifier = Modifier.size(19.dp),
                                )

                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterStart,
                                ) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = searchPlaceholder,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontSize = 14.5.sp,
                                            color = searchOrbHintColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                    BasicTextField(
                                        value = searchQuery,
                                        onValueChange = onSearchQueryChange,
                                        singleLine = true,
                                        enabled = isSearchActive,
                                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = searchOrbTextColor,
                                        ),
                                        cursorBrush = SolidColor(colors.accent),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                        keyboardActions = KeyboardActions(
                                            onSearch = {
                                                focusManager.clearFocus()
                                                onSearchSubmit()
                                            },
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(focusRequester),
                                    )
                                }

                                AnimatedVisibility(
                                    visible = searchQuery.isNotEmpty(),
                                    enter = scaleIn(
                                        initialScale = 0.7f,
                                        animationSpec = LiquidMotion.springSnappy(),
                                    ) + fadeIn(tween(120)),
                                    exit = scaleOut(
                                        targetScale = 0.7f,
                                        animationSpec = tween(100),
                                    ) + fadeOut(tween(100)),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isImmersive || colors.isDark) Color(0x33FFFFFF) else Color(0x1C000000),
                                            )
                                            .liquidPress(
                                                pressedScale = 0.85f,
                                                highlightAlpha = 0.16f,
                                                shape = CircleShape,
                                                haptic = true,
                                            ) { onSearchQueryChange("") },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "清空搜索",
                                            tint = searchOrbTextColor,
                                            modifier = Modifier.size(13.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Detached Circular Close Button (emerges on right when search morph opens)
                val closeProgress = ((p - 0.18f) / 0.82f).coerceIn(0f, 1f)
                if (closeProgress > 0.01f) {
                    val closeScale = 0.68f + 0.32f * closeProgress
                    LiquidGlassSurface(
                        shape = CircleShape,
                        dense = true,
                        blurRadius = 30.dp,
                        darkOverride = dockDarkOverride,
                        tintOverride = dockGlassTintOverride,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(orbSize)
                            .graphicsLayer {
                                alpha = closeProgress
                                scaleX = closeScale
                                scaleY = closeScale
                            }
                            .liquidPress(
                                enabled = isSearchActive,
                                pressedScale = 0.88f,
                                highlightAlpha = 0.18f,
                                shape = CircleShape,
                                haptic = true,
                                onClick = {
                                    focusManager.clearFocus()
                                    onCloseSearch()
                                },
                            ),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "关闭搜索",
                                tint = searchOrbTextColor,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

private enum class MiniPlayerDragAxis {
    NONE,
    HORIZONTAL,
    VERTICAL_UP,
}

@Composable
private fun DockShortcut(tab: MainTab, label: String, onClick: () -> Unit) {
    val colors = LocalLiquidColors.current
    LiquidGlassSurface(
        modifier = Modifier.size(52.dp).clip(CircleShape).clickable(onClick = onClick),
        shape = CircleShape,
    ) {
        Icon(tab.icon, label, Modifier.align(Alignment.Center).size(22.dp), tint = colors.accent)
    }
}

@Composable
internal fun LiquidMiniPlayerCapsule(
    playerState: PlayerUiState,
    onMiniPlayerClick: () -> Unit,
    onTogglePlay: () -> Unit,
    onSkipNext: () -> Unit,
    onSwipeNext: () -> Unit,
    onSwipePrevious: () -> Unit,
    immersiveTint: Color?,
    modifier: Modifier = Modifier,
    inline: Boolean = false,
    wide: Boolean = false,
    height: Dp = 56.dp,
) {
    val track = playerState.currentTrack ?: return
    val colors = LocalLiquidColors.current
    val haptic = LocalHapticFeedback.current

    val currentOnMiniPlayerClick by rememberUpdatedState(onMiniPlayerClick)
    val currentOnSwipeNext by rememberUpdatedState(onSwipeNext)
    val currentOnSwipePrevious by rememberUpdatedState(onSwipePrevious)

    val progress = if (playerState.durationMs > 0L) {
        (playerState.positionMs.toFloat() / playerState.durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    var swipeOffsetX by remember { mutableFloatStateOf(0f) }
    var swipeOffsetY by remember { mutableFloatStateOf(0f) }

    val animatedSwipeX by animateFloatAsState(
        targetValue = swipeOffsetX,
        animationSpec = LiquidMotion.springSnappy(),
        label = "mini_player_swipe_x",
    )
    val animatedSwipeY by animateFloatAsState(
        targetValue = swipeOffsetY,
        animationSpec = LiquidMotion.springSnappy(),
        label = "mini_player_swipe_y",
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

    val isImmersive = immersiveTint != null
    val immersiveGlassColor = immersiveTint?.let { tint ->
        lerp(Color(0xD6141620), tint.copy(alpha = 1f), 0.36f).copy(alpha = 0.84f)
    }
    val animatedImmersiveGlass by animateColorAsState(
        targetValue = immersiveGlassColor ?: colors.glassTint,
        animationSpec = LiquidMotion.springFluid(),
        label = "mini_immersive_tint",
    )

    val primaryText = if (isImmersive) colors.immersiveTextPrimary else colors.textPrimary
    val secondaryText = if (isImmersive) colors.immersiveTextSecondary else colors.textSecondary
    val subtitleHighlight = if (isImmersive) Color(0xFFFF6E87) else colors.accent
    val dividerLine = if (isImmersive) colors.immersiveDivider else colors.divider

    LiquidGlassSurface(
        shape = CircleShape,
        blurRadius = 30.dp,
        darkOverride = if (isImmersive) true else null,
        tintOverride = if (isImmersive) animatedImmersiveGlass else null,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .graphicsLayer {
                translationX = animatedSwipeX
                translationY = animatedSwipeY
            }
            .liquidPress(
                pressedScale = 0.975f,
                highlightAlpha = 0.12f,
                shape = CircleShape,
                haptic = true,
                onClick = onMiniPlayerClick,
            )
            .pointerInput(track.id) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var axis = MiniPlayerDragAxis.NONE
                    var totalDx = 0f
                    var totalDy = 0f
                    var hapticCrossed = false
                    val slop = viewConfiguration.touchSlop

                    try {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) {
                                when (axis) {
                                    MiniPlayerDragAxis.HORIZONTAL -> {
                                        change.consume()
                                        when {
                                            swipeOffsetX <= -58f -> {
                                                if (!hapticCrossed) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                }
                                                currentOnSwipeNext()
                                            }
                                            swipeOffsetX >= 58f -> {
                                                if (!hapticCrossed) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                }
                                                currentOnSwipePrevious()
                                            }
                                        }
                                    }
                                    MiniPlayerDragAxis.VERTICAL_UP -> {
                                        change.consume()
                                        if (swipeOffsetY <= -28f) {
                                            if (!hapticCrossed) {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            }
                                            currentOnMiniPlayerClick()
                                        }
                                    }
                                    MiniPlayerDragAxis.NONE -> Unit
                                }
                                break
                            }

                            val delta = change.positionChange()
                            totalDx += delta.x
                            totalDy += delta.y

                            if (axis == MiniPlayerDragAxis.NONE) {
                                if (abs(totalDx) > slop && abs(totalDx) >= abs(totalDy)) {
                                    axis = MiniPlayerDragAxis.HORIZONTAL
                                } else if (totalDy < -slop && abs(totalDy) > abs(totalDx)) {
                                    axis = MiniPlayerDragAxis.VERTICAL_UP
                                }
                            }

                            when (axis) {
                                MiniPlayerDragAxis.HORIZONTAL -> {
                                    change.consume()
                                    val nextX = (swipeOffsetX + delta.x * 0.56f).coerceIn(-96f, 96f)
                                    swipeOffsetX = nextX
                                    if (!hapticCrossed && abs(nextX) >= 58f) {
                                        hapticCrossed = true
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                }
                                MiniPlayerDragAxis.VERTICAL_UP -> {
                                    change.consume()
                                    val nextY = (swipeOffsetY + delta.y * 0.48f).coerceIn(-46f, 4f)
                                    swipeOffsetY = nextY
                                    if (!hapticCrossed && nextY <= -28f) {
                                        hapticCrossed = true
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                }
                                MiniPlayerDragAxis.NONE -> Unit
                            }
                        }
                    } finally {
                        swipeOffsetX = 0f
                        swipeOffsetY = 0f
                    }
                }
            },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 10.dp, end = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CoverArt(
                    url = playerState.resolvedCoverUrl ?: track.picUrl,
                    cornerRadius = 10.dp,
                    placeholderSeed = track.id,
                    modifier = Modifier.size(38.dp),
                )

                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.name,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryText,
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
                            fontSize = 11.sp,
                            color = if (activeLyric != null) subtitleHighlight else secondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                if (wide) LiquidPlaybackControlButton(
                    icon = Icons.Rounded.SkipPrevious,
                    contentDescription = "上一首",
                    onClick = onSwipePrevious,
                    iconSize = 23.dp,
                    buttonSize = 40.dp,
                    tint = primaryText,
                    animateIconChange = false,
                )
                Spacer(Modifier.width(4.dp))

                LiquidPlaybackControlButton(
                    icon = if (playerState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = "播放/暂停",
                    onClick = onTogglePlay,
                    iconSize = 25.dp,
                    buttonSize = 40.dp,
                    haloSize = 38.dp,
                    tint = primaryText,
                    haloColor = if (isImmersive || colors.isDark) Color.White.copy(alpha = 0.20f) else Color.Black.copy(alpha = 0.10f),
                    isLoading = playerState.isResolvingUrl || playerState.isBuffering,
                    loadingTint = colors.accent,
                    animateIconChange = true,
                )

                if (!inline) LiquidPlaybackControlButton(
                    icon = Icons.Rounded.SkipNext,
                    contentDescription = "下一首",
                    onClick = onSkipNext,
                    iconSize = 23.dp,
                    buttonSize = 38.dp,
                    haloSize = 36.dp,
                    tint = primaryText,
                    haloColor = if (isImmersive || colors.isDark) Color.White.copy(alpha = 0.20f) else Color.Black.copy(alpha = 0.10f),
                    animateIconChange = false,
                )
            }

            // Delicate inner glass progress hairline along bottom chord
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
                    .height(1.8.dp)
                    .clip(CircleShape)
                    .background(dividerLine),
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
