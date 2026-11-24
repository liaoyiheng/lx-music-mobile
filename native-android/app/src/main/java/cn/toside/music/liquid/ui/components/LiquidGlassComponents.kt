package cn.toside.music.liquid.ui.components

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.toside.music.liquid.data.model.MaterialStyle
import cn.toside.music.liquid.data.model.MusicInfo
import cn.toside.music.liquid.data.model.MusicSources
import cn.toside.music.liquid.ui.theme.LocalHazeState
import cn.toside.music.liquid.ui.theme.LocalLiquidColors
import cn.toside.music.liquid.ui.theme.LocalMaterialStyle
import coil.compose.SubcomposeAsyncImage
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeChild
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * iOS 26 弹簧按压反馈：按下瞬间 Q 弹微缩至 [pressedScale]，松手带阻尼弹簧回弹。
 */
fun Modifier.bouncyClick(
    pressedScale: Float = 0.955f,
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = 0.52f,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "bouncy_scale",
    )
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick,
        )
}

/**
 * iOS 26 真液态玻璃容器：
 * - LIQUID_GLASS 模式：使用 Haze 在 Android 12+ 上实时 GPU RenderEffect 高斯模糊底层滚动内容 + 顶部镜面高光描边 + 零脏灰阴影
 * - CLASSIC_FROST 模式：柔和磨砂半透明
 * - SOLID_MATTE 模式：高对比度纯色哑光悬浮卡片
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    dense: Boolean = false,
    darkOverride: Boolean? = null,
    blurRadius: Dp = 26.dp,
    borderWidth: Dp = 1.dp,
    useBlur: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = LocalLiquidColors.current
    val styleMode = LocalMaterialStyle.current
    val hazeState = LocalHazeState.current
    val isDark = darkOverride ?: colors.isDark

    val tintColor = when (styleMode) {
        MaterialStyle.LIQUID_GLASS -> when {
            isDark && dense -> Color(0xB8191924)
            isDark -> Color(0x7A1C1C28)
            dense -> Color(0xCCFFFFFF)
            else -> Color(0x9EFFFFFF)
        }
        MaterialStyle.CLASSIC_FROST -> when {
            isDark -> Color(0xCC181822)
            else -> Color(0xE0FFFFFF)
        }
        MaterialStyle.SOLID_MATTE -> when {
            isDark -> Color(0xFF1A1A24)
            else -> Color(0xFFFFFFFF)
        }
    }

    val rimTop = if (isDark) Color(0x52FFFFFF) else Color(0xF0FFFFFF)
    val rimBottom = if (isDark) Color(0x16FFFFFF) else Color(0x36D0D4E0)
    val specularBrush = remember(isDark) {
        Brush.verticalGradient(
            0f to rimTop,
            0.35f to rimTop.copy(alpha = rimTop.alpha * 0.45f),
            1f to rimBottom,
        )
    }

    val innerSheen = remember(isDark, styleMode) {
        if (styleMode == MaterialStyle.SOLID_MATTE) {
            Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
        } else if (isDark) {
            Brush.verticalGradient(
                0f to Color(0x18FFFFFF),
                0.45f to Color(0x04FFFFFF),
                1f to Color(0x0A000000),
            )
        } else {
            Brush.verticalGradient(
                0f to Color(0x55FFFFFF),
                0.42f to Color(0x0EFFFFFF),
                1f to Color(0x08000000),
            )
        }
    }

    val canUseRealBlur = useBlur && hazeState != null && styleMode != MaterialStyle.SOLID_MATTE
    val effectiveBlur = if (styleMode == MaterialStyle.LIQUID_GLASS) blurRadius else 14.dp

    val baseMod = modifier
        .clip(shape)
        .let { m ->
            if (canUseRealBlur && hazeState != null) {
                m.hazeChild(
                    state = hazeState,
                    style = HazeStyle(
                        backgroundColor = if (isDark) Color(0xFF12121A) else Color(0xFFF4F4F8),
                        tint = HazeTint(tintColor),
                        blurRadius = effectiveBlur,
                        noiseFactor = if (styleMode == MaterialStyle.LIQUID_GLASS) 0.04f else 0f,
                        fallbackTint = HazeTint(
                            if (isDark) Color(0xDC1A1A24) else Color(0xEBFFFFFF)
                        ),
                    ),
                )
            } else {
                m.background(tintColor)
            }
        }
        .background(innerSheen)
        .border(width = borderWidth, brush = specularBrush, shape = shape)

    Box(modifier = baseMod, content = content)
}

/**
 * 播放页 GPU 实时流体渐变网格背景：
 * 4 个由专辑封面提取并智能提亮增艳的光球在 Canvas 中缓慢漂移融合，切歌时 650ms 丝滑融色，彻底消灭死黑和 GPU 过载。
 */
@Composable
fun FluidMeshBackground(
    colors: List<Color>,
    modifier: Modifier = Modifier,
) {
    val c0 by animateColorAsState(colors.getOrElse(0) { Color(0xFF4A2545) }, tween(650), label = "mesh0")
    val c1 by animateColorAsState(colors.getOrElse(1) { Color(0xFF223354) }, tween(650), label = "mesh1")
    val c2 by animateColorAsState(colors.getOrElse(2) { Color(0xFF7B2846) }, tween(650), label = "mesh2")
    val c3 by animateColorAsState(colors.getOrElse(3) { Color(0xFF1D2436) }, tween(650), label = "mesh3")

    val infinite = rememberInfiniteTransition(label = "fluid_mesh")
    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "mesh_phase",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val maxDim = maxOf(w, h)

        // Deep atmospheric base derived from primary palette color (never pure flat #000)
        drawRect(
            brush = Brush.verticalGradient(
                0f to c3.copy(alpha = 1f),
                0.5f to c0.copy(alpha = 0.85f),
                1f to Color(0xFF0E0E15),
            )
        )

        val p1 = Offset(
            x = w * (0.25f + 0.18f * cos(phase)),
            y = h * (0.20f + 0.12f * sin(phase)),
        )
        val p2 = Offset(
            x = w * (0.78f + 0.16f * sin(phase * 0.8f + 1.1f)),
            y = h * (0.34f + 0.15f * cos(phase * 0.8f + 0.4f)),
        )
        val p3 = Offset(
            x = w * (0.35f + 0.20f * sin(phase * 1.1f + 2.3f)),
            y = h * (0.76f + 0.14f * cos(phase * 0.9f)),
        )
        val p4 = Offset(
            x = w * (0.74f + 0.15f * cos(phase * 0.7f + 3.1f)),
            y = h * (0.82f + 0.10f * sin(phase * 1.2f)),
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(c0.copy(alpha = 0.88f), c0.copy(alpha = 0.25f), Color.Transparent),
                center = p1,
                radius = maxDim * 0.72f,
            ),
            radius = maxDim * 0.72f,
            center = p1,
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(c1.copy(alpha = 0.82f), c1.copy(alpha = 0.22f), Color.Transparent),
                center = p2,
                radius = maxDim * 0.68f,
            ),
            radius = maxDim * 0.68f,
            center = p2,
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(c2.copy(alpha = 0.76f), c2.copy(alpha = 0.18f), Color.Transparent),
                center = p3,
                radius = maxDim * 0.66f,
            ),
            radius = maxDim * 0.66f,
            center = p3,
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(c3.copy(alpha = 0.65f), Color.Transparent),
                center = p4,
                radius = maxDim * 0.62f,
            ),
            radius = maxDim * 0.62f,
            center = p4,
        )

        // Subtle legibility veil keeping bright covers comfortable for white text without crushing colors
        drawRect(
            brush = Brush.verticalGradient(
                0f to Color(0x28000000),
                0.5f to Color(0x1E000000),
                1f to Color(0x52000000),
            )
        )
    }
}

@Composable
fun CoverArt(
    url: String?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    placeholderSeed: String = "",
    contentDescription: String? = null,
) {
    val colors = LocalLiquidColors.current
    val shape = RoundedCornerShape(cornerRadius)
    val gradientPair = remember(placeholderSeed, colors.isDark) {
        val palettes = listOf(
            Color(0xFFFA243C) to Color(0xFFFF6B81),
            Color(0xFF5E5CE6) to Color(0xFF9F7AEA),
            Color(0xFF0A84FF) to Color(0xFF5AC8FA),
            Color(0xFFFF9F0A) to Color(0xFFFF5E3A),
            Color(0xFF30D158) to Color(0xFF2AC9B3),
            Color(0xFFBF5AF2) to Color(0xFFFF375F),
        )
        palettes[(placeholderSeed.hashCode() and 0x7fffffff) % palettes.size]
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(Brush.linearGradient(listOf(gradientPair.first, gradientPair.second))),
        contentAlignment = Alignment.Center,
    ) {
        if (!url.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = url,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.65f),
                        modifier = Modifier.size(26.dp),
                    )
                },
                error = {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.78f),
                        modifier = Modifier.size(26.dp),
                    )
                },
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

/**
 * 杂志编辑风沉浸式歌单 Hero 头部（本地歌单、在线歌单、排行榜统一复用）：
 * - 顶部全出血模糊封面融色背板，丝滑渐隐进页面画布（无硬切分界线）
 * - 居中 210dp 大封面 + 柔和投影
 * - 大写编辑风眉标 + 粗体标题 + 副标题/简介
 * - 液态玻璃「播放」主色胶囊 +「随机播放」玻璃胶囊
 */
@Composable
fun EditorialHeroHeader(
    title: String,
    eyebrow: String,
    subtitle: String,
    coverUrl: String?,
    description: String? = null,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
    trailingAction: (@Composable () -> Unit)? = null,
) {
    val colors = LocalLiquidColors.current

    Box(modifier = Modifier.fillMaxWidth()) {
        // Blurred ambient cover backdrop melting smoothly into canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(410.dp),
        ) {
            if (!coverUrl.isNullOrBlank()) {
                SubcomposeAsyncImage(
                    model = coverUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .let { m ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                m.blur(48.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                            } else m
                        }
                        .graphicsLayer { alpha = if (colors.isDark) 0.48f else 0.34f },
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    colors.accent.copy(alpha = if (colors.isDark) 0.32f else 0.18f),
                                    Color.Transparent,
                                ),
                            )
                        )
                )
            }

            // Seamless vertical fade to page background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to colors.background.copy(alpha = 0.18f),
                            0.55f to colors.background.copy(alpha = 0.72f),
                            1.0f to colors.background,
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 60.dp, start = 20.dp, end = 20.dp, bottom = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Centered magazine cover with diffused colored glow beneath
            Box(
                modifier = Modifier
                    .size(216.dp)
                    .drawBehind {
                        drawRoundRect(
                            color = colors.glassShadow.copy(alpha = if (colors.isDark) 0.55f else 0.24f),
                            topLeft = Offset(10f, 22f),
                            size = size.copy(width = size.width - 20f, height = size.height - 10f),
                            cornerRadius = CornerRadius(24.dp.toPx()),
                        )
                    },
            ) {
                CoverArt(
                    url = coverUrl,
                    cornerRadius = 20.dp,
                    placeholderSeed = title,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = eyebrow.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = colors.accent,
                letterSpacing = 1.4.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            if (subtitle.isNotBlank()) {
                Spacer(Modifier.height(5.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (!description.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = description.trim(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textTertiary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Primary Play button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.accent)
                        .bouncyClick(onClick = onPlayAll),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                        Text(
                            text = "播放",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.5.sp,
                        )
                    }
                }

                // Secondary Liquid Glass Shuffle button
                LiquidGlassSurface(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .bouncyClick(onClick = onShuffleAll),
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shuffle,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(19.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "随机播放",
                            color = colors.accent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.5.sp,
                        )
                    }
                }
            }

            if (trailingAction != null) {
                Spacer(Modifier.height(10.dp))
                trailingAction()
            }
        }
    }
}

/**
 * 弹簧滑块分段控件（歌单/排行榜、歌曲/歌单等）
 */
@Composable
fun LiquidSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalLiquidColors.current
    if (items.isEmpty()) return

    BoxWithConstraints(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (colors.isDark) Color(0xFF1D1D28) else Color(0x14000000))
            .padding(3.dp),
    ) {
        val segmentWidth = maxWidth / items.size
        val indicatorOffset by animateDpAsState(
            targetValue = segmentWidth * selectedIndex.coerceIn(0, items.lastIndex),
            animationSpec = spring(
                dampingRatio = 0.72f,
                stiffness = Spring.StiffnessMediumLow,
            ),
            label = "segment_slide",
        )

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(segmentWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(17.dp))
                .background(if (colors.isDark) Color(0xFF2E2E3E) else Color.White)
                .border(
                    width = 0.5.dp,
                    color = if (colors.isDark) Color(0x28FFFFFF) else Color(0x14000000),
                    shape = RoundedCornerShape(17.dp),
                ),
        )

        Row(modifier = Modifier.fillMaxSize()) {
            items.forEachIndexed { idx, label ->
                val active = idx == selectedIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onSelect(idx) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        fontSize = 13.5.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                        color = if (active) colors.textPrimary else colors.textSecondary,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
fun PlayingEqualizer(
    isPlaying: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val infinite = rememberInfiniteTransition(label = "eq")
    val b1 by infinite.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "eq1",
    )
    val b2 by infinite.animateFloat(
        initialValue = 0.9f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(tween(510, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "eq2",
    )
    val b3 by infinite.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(370, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "eq3",
    )

    Row(
        modifier = modifier.height(14.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        val heights = if (isPlaying) listOf(b1, b2, b3) else listOf(0.4f, 0.7f, 0.45f)
        heights.forEach { frac ->
            Box(
                modifier = Modifier
                    .width(2.6.dp)
                    .fillMaxHeight(frac)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color),
            )
        }
    }
}

@Composable
fun TrackRow(
    music: MusicInfo,
    index: Int? = null,
    isCurrent: Boolean,
    isPlaying: Boolean,
    showAlbum: Boolean = true,
    showDuration: Boolean = true,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalLiquidColors.current
    val qualityBadge = remember(music.id) { music.highestQualityBadge() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .bouncyClick(pressedScale = 0.985f, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (index != null) {
            Box(
                modifier = Modifier.width(26.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (isCurrent) {
                    PlayingEqualizer(isPlaying = isPlaying, color = colors.accent)
                } else {
                    Text(
                        text = "${index + 1}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textTertiary,
                    )
                }
            }
        }

        Box(modifier = Modifier.size(48.dp)) {
            CoverArt(
                url = music.picUrl,
                cornerRadius = 10.dp,
                placeholderSeed = music.id,
                modifier = Modifier.fillMaxSize(),
            )
            if (isCurrent && index == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x66000000)),
                    contentAlignment = Alignment.Center,
                ) {
                    PlayingEqualizer(isPlaying = isPlaying, color = Color.White)
                }
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = music.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 15.5.sp,
                    color = if (isCurrent) colors.accent else colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (qualityBadge == "Hi-Res" || qualityBadge == "无损") {
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.accentSoft)
                            .padding(horizontal = 5.dp, vertical = 1.dp),
                    ) {
                        Text(
                            text = qualityBadge,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                        )
                    }
                }
            }

            Spacer(Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.badgeBg)
                        .padding(horizontal = 4.5.dp, vertical = 1.dp),
                ) {
                    Text(
                        text = MusicSources.shortBadge(music.source),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textSecondary,
                    )
                }
                Spacer(Modifier.width(6.dp))
                val sub = buildString {
                    append(music.singer.ifBlank { "未知歌手" })
                    if (showAlbum && music.albumName.isNotBlank()) {
                        append(" · ")
                        append(music.albumName)
                    }
                }
                Text(
                    text = sub,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.5.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (showDuration && !music.interval.isNullOrBlank()) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = music.interval,
                        fontSize = 11.5.sp,
                        color = colors.textTertiary,
                    )
                }
            }
        }

        IconButton(
            onClick = onMoreClick,
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.MoreHoriz,
                contentDescription = "更多",
                tint = colors.textTertiary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
fun GlassCircleIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    darkOverride: Boolean? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    LiquidGlassSurface(
        shape = CircleShape,
        darkOverride = darkOverride,
        modifier = modifier
            .size(size)
            .bouncyClick(pressedScale = 0.90f, onClick = onClick),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center, content = content)
    }
}

fun formatMillis(ms: Long): String {
    val totalSec = (ms.coerceAtLeast(0L) / 1000L)
    val min = totalSec / 60L
    val sec = totalSec % 60L
    return "%d:%02d".format(min, sec)
}

/**
 * Apple Music iOS 26 风格弹性液态进度条：
 * - 静息态为 5.5dp 精致半透明细胶囊轨道，无突兀圆点滑块
 * - 手指按压/拖拽时弹簧膨胀至 10.5dp 并提亮高光折射，松手顺滑回弹并提交 seek
 * - 同时支持点击快进与水平拖拽微调，附带系统轻触觉反馈
 * - 供全屏播放页 PlayerOverlay 与沉浸式锁屏页 LockScreenActivity 共享复用
 */
@Composable
fun LiquidPlaybackScrubber(
    progress: Float,
    onScrubbingChange: (isScrubbing: Boolean, fraction: Float) -> Unit,
    onScrubFinished: (fraction: Float) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.White,
    trackColor: Color = Color.White.copy(alpha = 0.24f),
) {
    val haptic = LocalHapticFeedback.current
    var trackWidthPx by remember { mutableFloatStateOf(1f) }
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(progress.coerceIn(0f, 1f)) }

    val displayFraction = (if (isDragging) dragFraction else progress).coerceIn(0f, 1f)
    val barHeight by animateDpAsState(
        targetValue = if (isDragging) 10.5.dp else 5.5.dp,
        animationSpec = spring(
            dampingRatio = 0.62f,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "scrubber_height",
    )
    val activeAlpha by animateFloatAsState(
        targetValue = if (isDragging) 1f else 0.88f,
        animationSpec = tween(140),
        label = "scrubber_alpha",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .onSizeChanged { size ->
                if (size.width > 0) trackWidthPx = size.width.toFloat()
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val frac = (offset.x / trackWidthPx).coerceIn(0f, 1f)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onScrubFinished(frac)
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { startOffset ->
                        isDragging = true
                        val frac = (startOffset.x / trackWidthPx).coerceIn(0f, 1f)
                        dragFraction = frac
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onScrubbingChange(true, frac)
                    },
                    onDragEnd = {
                        val finalFrac = dragFraction.coerceIn(0f, 1f)
                        isDragging = false
                        onScrubbingChange(false, finalFrac)
                        onScrubFinished(finalFrac)
                    },
                    onDragCancel = {
                        isDragging = false
                        onScrubbingChange(false, progress.coerceIn(0f, 1f))
                    },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        val frac = (change.position.x / trackWidthPx).coerceIn(0f, 1f)
                        dragFraction = frac
                        onScrubbingChange(true, frac)
                    },
                )
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(CircleShape)
                .background(if (isDragging) trackColor.copy(alpha = 0.34f) else trackColor),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(displayFraction.coerceAtLeast(0.008f))
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                activeColor.copy(alpha = activeAlpha * 0.85f),
                                activeColor.copy(alpha = activeAlpha),
                            ),
                        ),
                    ),
            )
        }
    }
}
