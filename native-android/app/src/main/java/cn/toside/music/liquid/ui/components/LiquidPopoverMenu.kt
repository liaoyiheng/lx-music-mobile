package cn.toside.music.liquid.ui.components

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import cn.toside.music.liquid.ui.theme.LocalHazeState
import cn.toside.music.liquid.ui.theme.LocalLiquidColors

/**
 * iOS 26 液态玻璃气泡菜单选项模型
 */
@Immutable
data class LiquidPopoverItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector? = null,
    val selected: Boolean = false,
    val destructive: Boolean = false,
    val showDividerAbove: Boolean = false,
    val onClick: () -> Unit,
)

private fun resolvePopoverTransformOrigin(alignment: Alignment): TransformOrigin = when (alignment) {
    Alignment.TopStart -> TransformOrigin(0.08f, 0.04f)
    Alignment.TopCenter -> TransformOrigin(0.5f, 0.04f)
    Alignment.TopEnd -> TransformOrigin(0.92f, 0.04f)
    Alignment.CenterStart -> TransformOrigin(0.08f, 0.5f)
    Alignment.Center -> TransformOrigin(0.5f, 0.5f)
    Alignment.CenterEnd -> TransformOrigin(0.92f, 0.5f)
    Alignment.BottomStart -> TransformOrigin(0.08f, 0.96f)
    Alignment.BottomCenter -> TransformOrigin(0.5f, 0.96f)
    Alignment.BottomEnd -> TransformOrigin(0.92f, 0.96f)
    else -> TransformOrigin(0.90f, 0.06f)
}

/**
 * iOS 26 液态玻璃气泡菜单：
 * - 从触发按钮锚点以 [TransformOrigin] 弹性展开（scale 0.84 → 1.0 + alpha 0 → 1，SpringSnappy）
 * - 支持全屏内嵌模式（[usePopup] = false，直接穿透使用当前窗口实时 Haze 模糊）与锚点窗口模式（[usePopup] = true，可作 DropdownMenu 原位替代）
 * - 顶部镜面高光描边、0.5dp 缩进分隔线与 Apple Music 10dp 圆角行按压反馈
 */
@Composable
fun LiquidPopoverMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<LiquidPopoverItem>,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.TopEnd,
    anchorTransformOrigin: TransformOrigin? = null,
    offset: IntOffset = IntOffset(0, 0),
    width: Dp = 196.dp,
    darkOverride: Boolean? = null,
    usePopup: Boolean = true,
    dismissOnItemClick: Boolean = true,
) {
    if (items.isEmpty()) return

    val transitionState = remember { MutableTransitionState(false) }
    transitionState.targetState = expanded
    if (!transitionState.currentState && !transitionState.targetState) return

    val transition = updateTransition(transitionState, label = "liquid_popover")
    val scale by transition.animateFloat(
        transitionSpec = {
            if (targetState) LiquidMotion.springSnappy()
            else tween(durationMillis = 140)
        },
        label = "popover_scale",
    ) { visible -> if (visible) 1f else 0.84f }

    val alpha by transition.animateFloat(
        transitionSpec = {
            if (targetState) tween(durationMillis = 150)
            else tween(durationMillis = 130)
        },
        label = "popover_alpha",
    ) { visible -> if (visible) 1f else 0f }

    val resolvedOrigin = anchorTransformOrigin ?: resolvePopoverTransformOrigin(alignment)

    if (usePopup) {
        Popup(
            alignment = alignment,
            offset = offset,
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(focusable = true),
        ) {
            // Popup lives in an isolated Android Window; disable cross-window Haze capture
            // while preserving rich frosted glass styling and soft drop shadow.
            CompositionLocalProvider(LocalHazeState provides null) {
                Box(modifier = Modifier.padding(14.dp)) {
                    LiquidPopoverSurface(
                        items = items,
                        width = width,
                        darkOverride = darkOverride,
                        useBlur = false,
                        onDismissRequest = onDismissRequest,
                        dismissOnItemClick = dismissOnItemClick,
                        modifier = modifier.graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                            transformOrigin = resolvedOrigin
                        },
                    )
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismissRequest,
                    ),
            )
            Box(
                modifier = Modifier
                    .align(alignment)
                    .offset { offset },
            ) {
                LiquidPopoverSurface(
                    items = items,
                    width = width,
                    darkOverride = darkOverride,
                    useBlur = true,
                    onDismissRequest = onDismissRequest,
                    dismissOnItemClick = dismissOnItemClick,
                    modifier = modifier.graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                        transformOrigin = resolvedOrigin
                    },
                )
            }
        }
    }
}

@Composable
fun LiquidPopoverSurface(
    items: List<LiquidPopoverItem>,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 196.dp,
    darkOverride: Boolean? = null,
    useBlur: Boolean = true,
    dismissOnItemClick: Boolean = true,
) {
    val colors = LocalLiquidColors.current
    val isDark = darkOverride ?: colors.isDark
    val shape = RoundedCornerShape(22.dp)

    val popoverTint = if (isDark) {
        if (useBlur) Color(0xCC282830) else Color(0xE02C2C32)
    } else {
        if (useBlur) Color(0xD8F6F7FF) else Color(0xEDFFFFFF)
    }
    val shadowColor = if (isDark) Color(0x88000000) else Color(0x2E0A0E1A)

    val primaryText = if (isDark) Color(0xF0FFFFFF) else Color(0xFF1C1C1E)
    val secondaryText = if (isDark) Color(0x9EFFFFFF) else Color(0xFF636366)
    val dividerColor = if (isDark) Color(0x1EFFFFFF) else Color(0x14000000)

    val hasCheckColumn = items.any { it.selected && it.icon == null }
    val hasIconColumn = items.any { it.icon != null }
    val dividerStartIndent = when {
        hasIconColumn -> 44.dp
        hasCheckColumn -> 40.dp
        else -> 16.dp
    }

    LiquidGlassSurface(
        shape = shape,
        dense = true,
        darkOverride = isDark,
        blurRadius = 32.dp,
        borderWidth = 0.8.dp,
        useBlur = useBlur,
        tintOverride = popoverTint,
        modifier = modifier
            .width(width)
            .shadow(
                elevation = 18.dp,
                shape = shape,
                clip = false,
                ambientColor = shadowColor,
                spotColor = shadowColor,
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
        ) {
            items.forEachIndexed { index, item ->
                if (item.showDividerAbove && index > 0) {
                    HorizontalDivider(
                        color = dividerColor,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = dividerStartIndent, end = 14.dp),
                    )
                }

                val itemTint = when {
                    item.destructive -> colors.destructive
                    item.selected -> colors.accent
                    else -> primaryText
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp)
                        .liquidRowPress(
                            immersiveOnDark = isDark,
                            cornerRadius = 10.dp,
                            haptic = true,
                            onClick = {
                                item.onClick()
                                if (dismissOnItemClick) {
                                    onDismissRequest()
                                }
                            },
                        )
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (hasIconColumn) {
                        Box(
                            modifier = Modifier.width(20.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (item.icon != null) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = itemTint,
                                    modifier = Modifier.size(18.dp),
                                )
                            } else if (item.selected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                    } else if (hasCheckColumn) {
                        Box(
                            modifier = Modifier.width(18.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (item.selected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 14.sp,
                            fontWeight = if (item.selected) FontWeight.Bold else FontWeight.Medium,
                            color = itemTint,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (!item.subtitle.isNullOrBlank()) {
                            Text(
                                text = item.subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                color = secondaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    if (hasIconColumn && item.icon != null && item.selected) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}
