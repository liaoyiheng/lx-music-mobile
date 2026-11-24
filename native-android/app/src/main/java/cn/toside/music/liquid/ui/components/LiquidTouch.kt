package cn.toside.music.liquid.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.toside.music.liquid.ui.theme.LocalLiquidColors
import kotlinx.coroutines.launch

/**
 * 液态玻璃按钮 / 胶囊 / 圆形图标物理按压反馈：
 * - 彻底禁用 Material 默认灰黑 Ripple（indication = null）
 * - 按下瞬间弹簧缩至 [pressedScale] + 裁剪至 [shape] 的镜面高光扫亮 + 轻触觉震动
 * - 松手按 SpringSnappy 弹性回弹，高光在 180ms 内柔滑消散
 */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.liquidPress(
    enabled: Boolean = true,
    pressedScale: Float = 0.92f,
    highlightAlpha: Float = 0.14f,
    shape: Shape = CircleShape,
    haptic: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
): Modifier = composed {
    val hapticFeedback = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(pressed, enabled, haptic) {
        if (pressed && enabled && haptic) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) pressedScale else 1f,
        animationSpec = LiquidMotion.springSnappy(),
        label = "liquid_press_scale",
    )
    val glowAlpha by animateFloatAsState(
        targetValue = if (pressed && enabled) highlightAlpha else 0f,
        animationSpec = if (pressed) tween(55) else tween(LiquidMotion.HighlightFadeOutMs, easing = FastOutSlowInEasing),
        label = "liquid_press_glow",
    )

    val interactiveMod = if (onLongClick != null) {
        combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onLongClick = onLongClick,
            onClick = onClick,
        )
    } else {
        clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick,
        )
    }

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .drawWithContent {
            drawContent()
            if (glowAlpha > 0.004f) {
                val outline = shape.createOutline(size, layoutDirection, this)
                drawOutline(
                    outline = outline,
                    brush = Brush.verticalGradient(
                        0f to Color.White.copy(alpha = glowAlpha),
                        0.55f to Color.White.copy(alpha = glowAlpha * 0.55f),
                        1f to Color.White.copy(alpha = glowAlpha * 0.18f),
                    ),
                )
            }
        }
        .then(interactiveMod)
}

/**
 * 海报大卡 / 专辑封面方卡按压反馈：
 * - 按下缩至 [pressedScale]（默认 0.965f）+ 轻压暗与顶部微光，绝不拦截横向/纵向滚动手势
 */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.liquidCardPress(
    enabled: Boolean = true,
    pressedScale: Float = 0.965f,
    shape: Shape = RoundedCornerShape(16.dp),
    haptic: Boolean = false,
    highlightAlpha: Float = 0.08f,
    dimAlpha: Float = 0.05f,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
): Modifier = composed {
    val hapticFeedback = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(pressed, enabled, haptic) {
        if (pressed && enabled && haptic) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) pressedScale else 1f,
        animationSpec = LiquidMotion.springSnappy(),
        label = "liquid_card_scale",
    )
    val pressProgress by animateFloatAsState(
        targetValue = if (pressed && enabled) 1f else 0f,
        animationSpec = if (pressed) tween(60) else tween(LiquidMotion.HighlightFadeOutMs, easing = FastOutSlowInEasing),
        label = "liquid_card_overlay",
    )

    val interactiveMod = if (onLongClick != null) {
        combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onLongClick = onLongClick,
            onClick = onClick,
        )
    } else {
        clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick,
        )
    }

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .drawWithContent {
            drawContent()
            if (pressProgress > 0.004f) {
                val outline = shape.createOutline(size, layoutDirection, this)
                drawOutline(
                    outline = outline,
                    brush = Brush.verticalGradient(
                        0f to Color.White.copy(alpha = highlightAlpha * pressProgress),
                        0.42f to Color.Transparent,
                        1f to Color.Black.copy(alpha = dimAlpha * pressProgress),
                    ),
                )
            }
        }
        .then(interactiveMod)
}

/**
 * Apple Music 标志性列表行触控反馈：
 * - 按住时在 [cornerRadius]（默认 10dp）圆角内浮现柔和半透明高亮底色（深色/沉浸 white@10%，浅色 black@6%）
 * - 叠加 0.988f 极微按压缩放，松手 180ms 柔和消散，可选长按
 */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.liquidRowPress(
    immersiveOnDark: Boolean = false,
    cornerRadius: Dp = 10.dp,
    enabled: Boolean = true,
    haptic: Boolean = false,
    highlightHorizontalInset: Dp = 0.dp,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
): Modifier = composed {
    val colors = LocalLiquidColors.current
    val hapticFeedback = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(pressed, enabled, haptic) {
        if (pressed && enabled && haptic) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.988f else 1f,
        animationSpec = LiquidMotion.springSnappy(),
        label = "liquid_row_scale",
    )
    val highlightFraction by animateFloatAsState(
        targetValue = if (pressed && enabled) 1f else 0f,
        animationSpec = if (pressed) tween(60) else tween(LiquidMotion.HighlightFadeOutMs, easing = FastOutSlowInEasing),
        label = "liquid_row_highlight",
    )

    val baseHighlightColor = if (immersiveOnDark || colors.isDark) {
        colors.immersiveRowHighlight
    } else {
        colors.rowHighlight
    }

    val interactiveMod = if (onLongClick != null) {
        combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onLongClick = onLongClick,
            onClick = onClick,
        )
    } else {
        clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick,
        )
    }

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .drawBehind {
            if (highlightFraction > 0.005f) {
                val insetPx = highlightHorizontalInset.toPx()
                val radiusPx = cornerRadius.toPx()
                val drawWidth = (size.width - insetPx * 2f).coerceAtLeast(0f)
                drawRoundRect(
                    color = baseHighlightColor.copy(alpha = baseHighlightColor.alpha * highlightFraction),
                    topLeft = Offset(insetPx, 0f),
                    size = Size(drawWidth, size.height),
                    cornerRadius = CornerRadius(radiusPx, radiusPx),
                )
            }
        }
        .then(interactiveMod)
}

/**
 * Apple Music 标志性播放控制按钮（上一首 / 播放暂停 / 下一首）：
 * - 按下瞬间图标缩至 0.84f，同时背后浮现 48dp 半透明柔焦圆形光晕
 * - 松手光晕微扩淡出、图标弹性回弹，并支持 AnimatedContent 播放/暂停图标形变
 */
@Composable
fun LiquidPlaybackControlButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 34.dp,
    buttonSize: Dp = 52.dp,
    haloSize: Dp = 48.dp,
    tint: Color = Color.White,
    haloColor: Color = Color.White.copy(alpha = 0.24f),
    enabled: Boolean = true,
    isLoading: Boolean = false,
    loadingTint: Color = tint,
    animateIconChange: Boolean = true,
    containerColor: Color = Color.Transparent,
    containerBorderColor: Color = Color.Transparent,
    containerBorderWidth: Dp = 1.dp,
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val iconScale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.84f else 1f,
        animationSpec = LiquidMotion.springSnappy(),
        label = "playback_btn_scale",
    )

    val haloAlpha = remember { Animatable(0f) }
    val haloScale = remember { Animatable(0.84f) }
    var wasPressed by remember { mutableStateOf(false) }

    LaunchedEffect(pressed, enabled) {
        if (pressed && enabled) {
            wasPressed = true
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            launch { haloAlpha.animateTo(1f, tween(65)) }
            launch {
                haloScale.snapTo(0.78f)
                haloScale.animateTo(1f, LiquidMotion.springSnappy())
            }
        } else if (wasPressed) {
            launch { haloScale.animateTo(1.22f, tween(220, easing = FastOutSlowInEasing)) }
            launch { haloAlpha.animateTo(0f, tween(220, easing = FastOutSlowInEasing)) }
        }
    }

    Box(
        modifier = modifier
            .size(buttonSize)
            .clip(CircleShape)
            .let { m ->
                if (containerColor != Color.Transparent) m.background(containerColor, CircleShape) else m
            }
            .let { m ->
                if (containerBorderColor != Color.Transparent) {
                    m.border(containerBorderWidth, containerBorderColor, CircleShape)
                } else m
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        // 48dp soft-focus circular optical halo behind icon
        if (haloAlpha.value > 0.005f) {
            Box(
                modifier = Modifier
                    .size(haloSize)
                    .graphicsLayer {
                        alpha = haloAlpha.value
                        scaleX = haloScale.value
                        scaleY = haloScale.value
                    }
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                haloColor,
                                haloColor.copy(alpha = haloColor.alpha * 0.48f),
                                Color.Transparent,
                            ),
                        ),
                        shape = CircleShape,
                    ),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = iconScale
                    scaleY = iconScale
                },
            contentAlignment = Alignment.Center,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(iconSize * 0.72f),
                    strokeWidth = 2.4.dp,
                    color = loadingTint,
                )
            } else if (animateIconChange) {
                AnimatedContent(
                    targetState = icon,
                    transitionSpec = {
                        (scaleIn(initialScale = 0.72f, animationSpec = LiquidMotion.springBouncy()) +
                            fadeIn(tween(140))) togetherWith
                            (scaleOut(targetScale = 0.72f, animationSpec = tween(110)) +
                                fadeOut(tween(110)))
                    },
                    label = "playback_icon_morph",
                ) { targetIcon ->
                    Icon(
                        imageVector = targetIcon,
                        contentDescription = contentDescription,
                        tint = tint,
                        modifier = Modifier.size(iconSize),
                    )
                }
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = tint,
                    modifier = Modifier.size(iconSize),
                )
            }
        }
    }
}

/**
 * 收藏红心物理跳动按钮：
 * - 点赞执行 1.0 → 0.78 → 1.24 → 1.0 弹性跳动 + 轻扩散光环 + 清脆触觉反馈
 */
@Composable
fun LiquidFavoriteHeartButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    iconSize: Dp = 20.dp,
    activeTint: Color = LocalLiquidColors.current.accent,
    inactiveTint: Color = Color.White.copy(alpha = 0.86f),
    containerColor: Color = Color.White.copy(alpha = 0.14f),
    showContainer: Boolean = true,
) {
    val haptic = LocalHapticFeedback.current
    val heartScale = remember { Animatable(1f) }
    val ringProgress = remember { Animatable(0f) }
    var previousFavorite by remember { mutableStateOf(isFavorite) }

    LaunchedEffect(isFavorite) {
        if (isFavorite && !previousFavorite) {
            heartScale.snapTo(1f)
            heartScale.animateTo(0.78f, tween(65, easing = FastOutSlowInEasing))
            launch {
                ringProgress.snapTo(0f)
                ringProgress.animateTo(1f, tween(380, easing = FastOutSlowInEasing))
                ringProgress.snapTo(0f)
            }
            heartScale.animateTo(1.24f, tween(125, easing = FastOutSlowInEasing))
            heartScale.animateTo(1f, LiquidMotion.springBouncy())
        } else if (!isFavorite && previousFavorite) {
            heartScale.animateTo(0.84f, tween(70))
            heartScale.animateTo(1f, LiquidMotion.springSnappy())
        }
        previousFavorite = isFavorite
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .let { m ->
                if (showContainer) m.background(containerColor, CircleShape) else m
            }
            .drawBehind {
                val p = ringProgress.value
                if (p > 0.01f && p < 0.99f) {
                    val minR = iconSize.toPx() * 0.42f
                    val maxR = this.size.minDimension * 0.68f
                    val radius = minR + (maxR - minR) * p
                    val ringAlpha = (1f - p) * 0.65f
                    drawCircle(
                        color = activeTint.copy(alpha = ringAlpha),
                        radius = radius,
                        style = Stroke(width = (2.2.dp.toPx() * (1f - p * 0.6f)).coerceAtLeast(0.8f)),
                    )
                }
            }
            .liquidPress(
                pressedScale = 0.86f,
                highlightAlpha = 0.16f,
                shape = CircleShape,
                haptic = false,
                onClick = {
                    haptic.performHapticFeedback(
                        if (!isFavorite) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove
                    )
                    onClick()
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
            contentDescription = if (isFavorite) "取消收藏" else "收藏",
            tint = if (isFavorite) activeTint else inactiveTint,
            modifier = Modifier
                .size(iconSize)
                .graphicsLayer {
                    scaleX = heartScale.value
                    scaleY = heartScale.value
                },
        )
    }
}
