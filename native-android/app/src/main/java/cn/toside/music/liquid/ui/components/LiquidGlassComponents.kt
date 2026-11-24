package cn.toside.music.liquid.ui.components

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import cn.toside.music.liquid.data.model.MaterialStyle
import cn.toside.music.liquid.data.model.MusicInfo
import cn.toside.music.liquid.ui.theme.LocalHazeState
import cn.toside.music.liquid.ui.theme.LocalLiquidColors
import cn.toside.music.liquid.ui.theme.LocalMaterialStyle
import coil.compose.SubcomposeAsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeChild
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * iOS 26 弹簧按压反馈（兼容旧调用方）：按下瞬间 Q 弹微缩至 [pressedScale]，松手带阻尼弹簧回弹。
 */
fun Modifier.bouncyClick(
    pressedScale: Float = 0.955f,
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) pressedScale else 1f,
        animationSpec = LiquidMotion.springBouncy(),
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
 * iOS 26 双层真液态玻璃容器：
 * - 强化浅色/深色分离度：浅色提升乳白透光度并叠加顶部内发光描边与细微外缘分割线，彻底解决底层列表透穿发灰问题
 * - 深色模式强化黑曜石玻璃层次与顶边高光
 * - 支持 [tintOverride] 与 [blurRadiusOverride]，并兼容全部旧参数
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    dense: Boolean = false,
    darkOverride: Boolean? = null,
    blurRadius: Dp = 30.dp,
    borderWidth: Dp = 1.dp,
    useBlur: Boolean = true,
    tintOverride: Color? = null,
    blurRadiusOverride: Dp? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = LocalLiquidColors.current
    val styleMode = LocalMaterialStyle.current
    val hazeState = LocalHazeState.current
    val isDark = darkOverride ?: colors.isDark

    val canUseRealBlur = useBlur && hazeState != null && styleMode != MaterialStyle.SOLID_MATTE

    val tintColor = tintOverride ?: when (styleMode) {
        MaterialStyle.LIQUID_GLASS -> when {
            canUseRealBlur && isDark && dense -> Color(0xD81A1A24)
            canUseRealBlur && isDark -> Color(0xB416161F)
            canUseRealBlur && dense -> Color(0xEBFFFFFF)
            canUseRealBlur -> Color(0xD6FFFFFF)
            // In-page translucent glass cards (not floating over a Haze source)
            isDark && dense -> Color(0xE01C1C24)
            isDark -> Color(0xC8181822)
            dense -> Color(0xF6FFFFFF)
            else -> Color(0xEBFFFFFF)
        }
        MaterialStyle.CLASSIC_FROST -> when {
            isDark -> Color(0xD4181822)
            else -> Color(0xF0FFFFFF)
        }
        MaterialStyle.SOLID_MATTE -> when {
            isDark -> Color(0xFF1C1C1F)
            else -> Color(0xFFFFFFFF)
        }
    }

    val rimTop = if (isDark) {
        if (canUseRealBlur) Color(0x5CFFFFFF) else Color(0x3DFFFFFF)
    } else {
        Color(0xFAFFFFFF)
    }
    val rimBottom = if (isDark) {
        Color(0x1AFFFFFF)
    } else {
        if (canUseRealBlur) Color(0x3CD0D4E2) else Color(0x28BCC2D2)
    }
    val outerHairline = if (isDark) Color(0x2E000000) else Color(0x14000000)

    val specularBrush = remember(isDark, canUseRealBlur) {
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
                0f to Color(0x22FFFFFF),
                0.42f to Color(0x05FFFFFF),
                1f to Color(0x0E000000),
            )
        } else {
            Brush.verticalGradient(
                0f to Color(0x66FFFFFF),
                0.42f to Color(0x12FFFFFF),
                1f to Color(0x06000000),
            )
        }
    }

    val effectiveBlur = blurRadiusOverride ?: when (styleMode) {
        MaterialStyle.LIQUID_GLASS -> if (dense) maxOf(blurRadius, colors.glassDenseBlurRadius) else blurRadius
        MaterialStyle.CLASSIC_FROST -> 16.dp
        MaterialStyle.SOLID_MATTE -> 0.dp
    }

    val fallbackTintColor = tintOverride?.copy(alpha = tintOverride.alpha.coerceAtLeast(0.86f))
        ?: if (isDark) Color(0xE0181822) else Color(0xEEFFFFFF)

    val baseMod = modifier
        .border(width = 0.5.dp, color = outerHairline, shape = shape)
        .clip(shape)
        .let { m ->
            if (canUseRealBlur && hazeState != null) {
                m.hazeChild(
                    state = hazeState,
                    style = HazeStyle(
                        backgroundColor = if (isDark) Color(0xFF0E0E14) else Color(0xFFF6F6FA),
                        tint = HazeTint(tintColor),
                        blurRadius = effectiveBlur,
                        noiseFactor = if (styleMode == MaterialStyle.LIQUID_GLASS) 0.045f else 0f,
                        fallbackTint = HazeTint(fallbackTintColor),
                    ),
                )
            } else {
                m.background(tintColor)
            }
        }
        .background(innerSheen)
        .border(width = borderWidth, brush = specularBrush, shape = shape)

    Box(modifier = baseMod) {
        if (canUseRealBlur) {
            CompositionLocalProvider(LocalHazeState provides null) {
                content()
            }
        } else {
            content()
        }
    }
}

/**
 * 封面异步取色结果：供全出血详情页羽化融色、全屏播放页流光背景及沉浸 MiniPlayer 复用
 */
@Immutable
data class CoverPaletteColors(
    val primary: Color,
    val secondary: Color,
    val darkMuted: Color,
    val darkVibrant: Color,
    val dominant: Color,
    val vibrant: Color,
    val accent: Color,
    val onBackground: Color = Color.White,
    val isFromCover: Boolean = false,
) {
    val meshColors: List<Color>
        get() = listOf(vibrant, darkVibrant, dominant, darkMuted)
}

private val DefaultCoverPaletteDark = CoverPaletteColors(
    primary = Color(0xFF3A1A28),
    secondary = Color(0xFF1A1426),
    darkMuted = Color(0xFF1D2436),
    darkVibrant = Color(0xFF4A2545),
    dominant = Color(0xFF32233C),
    vibrant = Color(0xFF7B2846),
    accent = Color(0xFFFF375F),
    onBackground = Color.White,
    isFromCover = false,
)

private val DefaultCoverPaletteLight = CoverPaletteColors(
    primary = Color(0xFF4F1828),
    secondary = Color(0xFF231629),
    darkMuted = Color(0xFF283044),
    darkVibrant = Color(0xFF6B223B),
    dominant = Color(0xFF44283D),
    vibrant = Color(0xFF9E2B4E),
    accent = Color(0xFFFA2D48),
    onBackground = Color.White,
    isFromCover = false,
)

private fun tuneCoverColor(
    rgb: Int,
    minSaturation: Float,
    maxSaturation: Float,
    minValue: Float,
    maxValue: Float,
): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(rgb, hsv)
    hsv[1] = (hsv[1] * 1.18f).coerceIn(minSaturation, maxSaturation)
    hsv[2] = hsv[2].coerceIn(minValue, maxValue)
    return Color(android.graphics.Color.HSVToColor(hsv))
}

/**
 * 通过 Coil + Palette 异步安全提取专辑/歌单封面主色，并执行 650ms 跨色平滑渐变：
 * - 强制 allowHardware(false) 与 ARGB_8888 转换，彻底避免 Android 12+ Hardware Bitmap 崩溃
 * - 默认提供优雅深邃兜底色，确保白色图文始终高对比可读
 */
@Composable
fun rememberCoverPalette(
    imageUrl: String?,
    isDark: Boolean = true,
): CoverPaletteColors {
    val context = LocalContext.current
    val fallback = if (isDark) DefaultCoverPaletteDark else DefaultCoverPaletteLight
    var targetPalette by remember(isDark) { mutableStateOf(fallback) }

    LaunchedEffect(imageUrl, isDark) {
        if (imageUrl.isNullOrBlank()) {
            targetPalette = fallback
            return@LaunchedEffect
        }

        val extracted = withContext(Dispatchers.IO) {
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .size(160)
                .allowHardware(false)
                .bitmapConfig(Bitmap.Config.ARGB_8888)
                .build()

            val result = runCatching { context.imageLoader.execute(request) }.getOrNull()
            val rawBmp = ((result as? SuccessResult)?.drawable as? BitmapDrawable)?.bitmap
            if (rawBmp == null || rawBmp.isRecycled) return@withContext null

            val safeBmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                rawBmp.config == Bitmap.Config.HARDWARE
            ) {
                rawBmp.copy(Bitmap.Config.ARGB_8888, false)
            } else {
                rawBmp
            } ?: return@withContext null

            try {
                val palette = Palette.from(safeBmp).maximumColorCount(16).generate()
                val rawDarkVibrant = palette.darkVibrantSwatch?.rgb
                val rawDarkMuted = palette.darkMutedSwatch?.rgb
                val rawDominant = palette.dominantSwatch?.rgb
                val rawVibrant = palette.vibrantSwatch?.rgb
                val rawMuted = palette.mutedSwatch?.rgb

                val safePrimaryRgb = rawDarkVibrant
                    ?: rawDominant
                    ?: rawDarkMuted
                    ?: rawMuted
                    ?: rawVibrant
                    ?: palette.lightVibrantSwatch?.rgb
                val safeSecondaryRgb = rawDarkMuted
                    ?: rawMuted
                    ?: rawDarkVibrant
                    ?: rawDominant
                    ?: safePrimaryRgb

                if (safePrimaryRgb == null || safeSecondaryRgb == null) {
                    null
                } else {
                    val maxBgVal = if (isDark) 0.34f else 0.40f
                    val minBgVal = if (isDark) 0.14f else 0.18f

                    val primary = tuneCoverColor(
                        rgb = safePrimaryRgb,
                        minSaturation = 0.24f,
                        maxSaturation = 0.82f,
                        minValue = minBgVal,
                        maxValue = maxBgVal,
                    )
                    val secondary = tuneCoverColor(
                        rgb = safeSecondaryRgb,
                        minSaturation = 0.20f,
                        maxSaturation = 0.72f,
                        minValue = (minBgVal * 0.8f).coerceAtLeast(0.10f),
                        maxValue = (maxBgVal * 0.78f),
                    )
                    val darkMuted = tuneCoverColor(
                        rgb = rawDarkMuted ?: rawMuted ?: safeSecondaryRgb,
                        minSaturation = 0.20f,
                        maxSaturation = 0.68f,
                        minValue = 0.14f,
                        maxValue = 0.34f,
                    )
                    val darkVibrant = tuneCoverColor(
                        rgb = rawDarkVibrant ?: rawVibrant ?: safePrimaryRgb,
                        minSaturation = 0.32f,
                        maxSaturation = 0.86f,
                        minValue = 0.22f,
                        maxValue = 0.46f,
                    )
                    val dominant = tuneCoverColor(
                        rgb = rawDominant ?: safePrimaryRgb,
                        minSaturation = 0.26f,
                        maxSaturation = 0.80f,
                        minValue = 0.24f,
                        maxValue = 0.50f,
                    )
                    val vibrant = tuneCoverColor(
                        rgb = rawVibrant ?: rawDarkVibrant ?: rawDominant ?: safePrimaryRgb,
                        minSaturation = 0.36f,
                        maxSaturation = 0.88f,
                        minValue = 0.30f,
                        maxValue = 0.64f,
                    )
                    val accent = tuneCoverColor(
                        rgb = rawVibrant ?: palette.lightVibrantSwatch?.rgb ?: rawDarkVibrant ?: safePrimaryRgb,
                        minSaturation = 0.54f,
                        maxSaturation = 0.94f,
                        minValue = 0.62f,
                        maxValue = 0.95f,
                    )

                    CoverPaletteColors(
                        primary = primary,
                        secondary = secondary,
                        darkMuted = darkMuted,
                        darkVibrant = darkVibrant,
                        dominant = dominant,
                        vibrant = vibrant,
                        accent = accent,
                        onBackground = Color.White,
                        isFromCover = true,
                    )
                }
            } finally {
                if (safeBmp !== rawBmp && !safeBmp.isRecycled) {
                    safeBmp.recycle()
                }
            }
        }

        targetPalette = extracted ?: fallback
    }

    val colorAnimSpec = tween<Color>(
        durationMillis = LiquidMotion.MeshColorCrossfadeMs,
        easing = FastOutSlowInEasing,
    )
    val animPrimary by animateColorAsState(targetPalette.primary, colorAnimSpec, label = "cp_primary")
    val animSecondary by animateColorAsState(targetPalette.secondary, colorAnimSpec, label = "cp_secondary")
    val animDarkMuted by animateColorAsState(targetPalette.darkMuted, colorAnimSpec, label = "cp_dark_muted")
    val animDarkVibrant by animateColorAsState(targetPalette.darkVibrant, colorAnimSpec, label = "cp_dark_vibrant")
    val animDominant by animateColorAsState(targetPalette.dominant, colorAnimSpec, label = "cp_dominant")
    val animVibrant by animateColorAsState(targetPalette.vibrant, colorAnimSpec, label = "cp_vibrant")
    val animAccent by animateColorAsState(targetPalette.accent, colorAnimSpec, label = "cp_accent")

    return CoverPaletteColors(
        primary = animPrimary,
        secondary = animSecondary,
        darkMuted = animDarkMuted,
        darkVibrant = animDarkVibrant,
        dominant = animDominant,
        vibrant = animVibrant,
        accent = animAccent,
        onBackground = Color.White,
        isFromCover = targetPalette.isFromCover,
    )
}

/**
 * 播放页 100% 不透明 GPU 实时流体渐变网格背景：
 * - 100% 不透明基底彻底阻断底层页面透穿
 * - 4 团由封面提取的主色光球缓慢流动并在切歌时执行 650ms 跨色融合
 */
@Composable
fun FluidMeshBackground(
    colors: List<Color>,
    modifier: Modifier = Modifier,
    animateMovement: Boolean = true,
) {
    val crossfadeSpec = tween<Color>(
        durationMillis = LiquidMotion.MeshColorCrossfadeMs,
        easing = FastOutSlowInEasing,
    )
    val c0 by animateColorAsState(colors.getOrElse(0) { Color(0xFF4A2545) }, crossfadeSpec, label = "mesh0")
    val c1 by animateColorAsState(colors.getOrElse(1) { Color(0xFF223354) }, crossfadeSpec, label = "mesh1")
    val c2 by animateColorAsState(colors.getOrElse(2) { Color(0xFF7B2846) }, crossfadeSpec, label = "mesh2")
    val c3 by animateColorAsState(colors.getOrElse(3) { Color(0xFF1D2436) }, crossfadeSpec, label = "mesh3")

    val phase = if (animateMovement) {
        val infinite = rememberInfiniteTransition(label = "fluid_mesh")
        val animatedPhase by infinite.animateFloat(
            initialValue = 0f,
            targetValue = (2.0 * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 16_000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "mesh_phase",
        )
        animatedPhase
    } else {
        0.7f
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0E0E15)),
    ) {
        val w = size.width
        val h = size.height
        val maxDim = maxOf(w, h)

        // 100% opaque atmospheric gradient base blocking all underlying content bleed-through
        drawRect(
            brush = Brush.verticalGradient(
                0f to c3.copy(alpha = 1f),
                0.5f to c0.copy(alpha = 1f),
                1f to Color(0xFF0E0E15),
            ),
        )

        val p1 = Offset(
            x = w * (0.24f + 0.18f * cos(phase)),
            y = h * (0.20f + 0.12f * sin(phase)),
        )
        val p2 = Offset(
            x = w * (0.78f + 0.16f * sin(phase * 0.8f + 1.1f)),
            y = h * (0.33f + 0.15f * cos(phase * 0.8f + 0.4f)),
        )
        val p3 = Offset(
            x = w * (0.34f + 0.20f * sin(phase * 1.1f + 2.3f)),
            y = h * (0.75f + 0.14f * cos(phase * 0.9f)),
        )
        val p4 = Offset(
            x = w * (0.75f + 0.15f * cos(phase * 0.7f + 3.1f)),
            y = h * (0.83f + 0.10f * sin(phase * 1.2f)),
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(c0.copy(alpha = 0.88f), c0.copy(alpha = 0.25f), Color.Transparent),
                center = p1,
                radius = maxDim * 0.74f,
            ),
            radius = maxDim * 0.74f,
            center = p1,
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(c1.copy(alpha = 0.84f), c1.copy(alpha = 0.22f), Color.Transparent),
                center = p2,
                radius = maxDim * 0.70f,
            ),
            radius = maxDim * 0.70f,
            center = p2,
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(c2.copy(alpha = 0.78f), c2.copy(alpha = 0.18f), Color.Transparent),
                center = p3,
                radius = maxDim * 0.68f,
            ),
            radius = maxDim * 0.68f,
            center = p3,
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(c3.copy(alpha = 0.68f), Color.Transparent),
                center = p4,
                radius = maxDim * 0.64f,
            ),
            radius = maxDim * 0.64f,
            center = p4,
        )

        // Legibility scrim keeping white typography & controls crisp across bright covers
        drawRect(
            brush = Brush.verticalGradient(
                0f to Color(0x28000000),
                0.5f to Color(0x1C000000),
                1f to Color(0x52000000),
            ),
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
            Color(0xFFFA2D48) to Color(0xFFFF6B81),
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
 * - 居中 216dp 大封面 + 柔和投影
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
                            ),
                        ),
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
                        ),
                    ),
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
                        .liquidPress(
                            pressedScale = 0.95f,
                            highlightAlpha = 0.18f,
                            shape = RoundedCornerShape(16.dp),
                            onClick = onPlayAll,
                        ),
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
                        .liquidPress(
                            pressedScale = 0.95f,
                            highlightAlpha = 0.14f,
                            shape = RoundedCornerShape(16.dp),
                            onClick = onShuffleAll,
                        ),
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
 * 弹簧滑块分段控件（歌单/排行榜、歌曲/歌单、设置开关等）
 */
@Composable
fun LiquidSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalLiquidColors.current
    val haptic = LocalHapticFeedback.current
    if (items.isEmpty()) return

    val targetIndex = selectedIndex.coerceIn(0, items.lastIndex)
    BoxWithConstraints(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (colors.isDark) Color(0xFF1D1D28) else Color(0x14000000))
            .padding(3.dp),
    ) {
        val segmentWidth = maxWidth / items.size
        val animatedIndex by animateFloatAsState(
            targetValue = targetIndex.toFloat(),
            animationSpec = LiquidMotion.springSnappy(),
            label = "segment_slide_idx",
        )
        val travelDelta = abs(targetIndex.toFloat() - animatedIndex).coerceIn(0f, 1.2f)
        val stretchX = 1f + travelDelta * 0.12f
        val indicatorOffset = segmentWidth * animatedIndex

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(segmentWidth)
                .fillMaxHeight()
                .graphicsLayer {
                    scaleX = stretchX
                    scaleY = 1f - travelDelta * 0.03f
                }
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
                val active = idx == targetIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            if (idx != targetIndex) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            onSelect(idx)
                        },
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

/**
 * 3 柱动态音频指示器：
 * - 播放态按独立节奏跳动
 * - 暂停态平滑回落至静止基线（非生硬截断）
 */
@Composable
fun PlayingEqualizer(
    isPlaying: Boolean = true,
    color: Color = LocalLiquidColors.current.accent,
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

    val activeWeight by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0f,
        animationSpec = LiquidMotion.springGentle(),
        label = "eq_active_weight",
    )

    val restHeights = listOf(0.24f, 0.34f, 0.22f)
    val liveHeights = listOf(b1, b2, b3)

    Row(
        modifier = modifier.height(14.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        for (i in 0..2) {
            val rest = restHeights[i]
            val live = liveHeights[i]
            val frac = (rest + (live - rest) * activeWeight).coerceIn(0.18f, 1f)
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

/**
 * Apple Music v2 极简曲目列表行：
 * - 接入 liquidRowPress（10dp 圆角柔和半透明高亮 + 0.988f 按压缩放 + 可选长按）
 * - 支持沉浸深色模式（[immersiveOnDark]）、可隐藏封面（[showArtwork]）、热门圆点（[isPopular]）
 * - 0.5dp 左缩进极细分隔线（[showDivider]）与极简「...」玻璃按压钮，去除多余红底标签与白底卡片
 * - 完全向后兼容原有调用方参数
 */
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
    showArtwork: Boolean = true,
    isPopular: Boolean = false,
    immersiveOnDark: Boolean = false,
    showDivider: Boolean = true,
    onLongClick: (() -> Unit)? = null,
) {
    val colors = LocalLiquidColors.current

    val primaryTextColor = if (immersiveOnDark) {
        colors.immersiveTextPrimary
    } else {
        colors.textPrimary
    }
    val secondaryTextColor = if (immersiveOnDark) {
        colors.immersiveTextSecondary
    } else {
        colors.textSecondary
    }
    val tertiaryTextColor = if (immersiveOnDark) {
        colors.immersiveTextTertiary
    } else {
        colors.textTertiary
    }
    val activeTitleColor = if (immersiveOnDark) {
        Color(0xFFFF6E87)
    } else {
        colors.accent
    }
    val dividerColor = if (immersiveOnDark) {
        colors.immersiveDivider
    } else {
        colors.divider
    }

    val hasLeadingSlot = index != null || isPopular
    val dividerStartIndent = when {
        showArtwork && hasLeadingSlot -> 114.dp
        showArtwork -> 74.dp
        index != null -> 56.dp
        isPopular -> 32.dp
        else -> 16.dp
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .liquidRowPress(
                    immersiveOnDark = immersiveOnDark,
                    cornerRadius = 10.dp,
                    highlightHorizontalInset = 8.dp,
                    onLongClick = onLongClick,
                    onClick = onClick,
                )
                .padding(
                    start = if (hasLeadingSlot && !showArtwork) 12.dp else 16.dp,
                    end = 14.dp,
                    top = if (showArtwork) 8.dp else 9.dp,
                    bottom = if (showArtwork) 8.dp else 9.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (hasLeadingSlot) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier.width(10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isPopular) {
                            Box(
                                modifier = Modifier
                                    .size(4.5.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (immersiveOnDark) {
                                            Color.White.copy(alpha = 0.72f)
                                        } else {
                                            colors.accent.copy(alpha = 0.85f)
                                        },
                                    ),
                            )
                        }
                    }

                    if (index != null) {
                        Box(
                            modifier = Modifier.width(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isCurrent) {
                                PlayingEqualizer(
                                    isPlaying = isPlaying,
                                    color = if (immersiveOnDark) Color.White else colors.accent,
                                )
                            } else {
                                Text(
                                    text = "${index + 1}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = tertiaryTextColor,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                }
            }

            if (showArtwork) {
                Box(modifier = Modifier.size(46.dp)) {
                    CoverArt(
                        url = music.picUrl,
                        cornerRadius = 9.dp,
                        placeholderSeed = music.id,
                        modifier = Modifier.fillMaxSize(),
                    )
                    if (isCurrent && index == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(9.dp))
                                .background(Color(0x66000000)),
                            contentAlignment = Alignment.Center,
                        ) {
                            PlayingEqualizer(isPlaying = isPlaying, color = Color.White)
                        }
                    }
                }
                Spacer(Modifier.width(12.dp))
            } else if (isCurrent && index == null) {
                PlayingEqualizer(
                    isPlaying = isPlaying,
                    color = if (immersiveOnDark) Color.White else colors.accent,
                    modifier = Modifier.padding(end = 10.dp),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = music.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 15.sp,
                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isCurrent) activeTitleColor else primaryTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(2.dp))

                val subtitle = buildString {
                    append(music.singer.ifBlank { "未知歌手" })
                    if (showAlbum && music.albumName.isNotBlank()) {
                        append(" · ")
                        append(music.albumName)
                    }
                    if (showDuration && !music.interval.isNullOrBlank()) {
                        append(" · ")
                        append(music.interval)
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.5.sp,
                    color = secondaryTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.width(6.dp))

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .liquidPress(
                        pressedScale = 0.86f,
                        highlightAlpha = if (immersiveOnDark) 0.18f else 0.12f,
                        shape = CircleShape,
                        haptic = true,
                        onClick = onMoreClick,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreHoriz,
                    contentDescription = "更多",
                    tint = if (immersiveOnDark) Color.White.copy(alpha = 0.68f) else tertiaryTextColor,
                    modifier = Modifier.size(19.dp),
                )
            }
        }

        if (showDivider) {
            HorizontalDivider(
                color = dividerColor,
                thickness = 0.5.dp,
                modifier = Modifier.padding(start = dividerStartIndent, end = 16.dp),
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
    blurRadius: Dp = 30.dp,
    useBlur: Boolean = true,
    tintOverride: Color? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    LiquidGlassSurface(
        shape = CircleShape,
        darkOverride = darkOverride,
        blurRadius = blurRadius,
        useBlur = useBlur,
        tintOverride = tintOverride,
        modifier = modifier
            .size(size)
            .liquidPress(
                pressedScale = 0.90f,
                highlightAlpha = 0.18f,
                shape = CircleShape,
                haptic = true,
                onClick = onClick,
            ),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center, content = content)
    }
}

/**
 * 液态玻璃胶囊操作钮（例如详情页右上「搜索 + 更多」双键胶囊、音源切换胶囊、收藏按钮等）
 */
@Composable
fun GlassCapsuleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 38.dp,
    enabled: Boolean = true,
    darkOverride: Boolean? = null,
    dense: Boolean = false,
    blurRadius: Dp = 30.dp,
    useBlur: Boolean = true,
    tintOverride: Color? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp),
    content: @Composable RowScope.() -> Unit,
) {
    LiquidGlassSurface(
        shape = CircleShape,
        dense = dense,
        darkOverride = darkOverride,
        blurRadius = blurRadius,
        useBlur = useBlur,
        tintOverride = tintOverride,
        modifier = modifier
            .height(height)
            .graphicsLayer { alpha = if (enabled) 1f else 0.55f }
            .liquidPress(
                enabled = enabled,
                pressedScale = 0.94f,
                highlightAlpha = 0.16f,
                shape = CircleShape,
                haptic = true,
                onClick = onClick,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            content = content,
        )
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
 * - 常态为 4dp 细胶囊；手指按下瞬间弹簧增粗至 8dp + 进度轨道发光 + 轻触觉震动
 * - 松手弹簧收细回 4dp 并提交 seek
 * - 传入 [durationMs] 时自动在轨道下方渲染随拖拽微亮放大的时间文字与居中音质胶囊槽位 [centerBadge]
 * - 保持与旧调用方（PlayerOverlay / LockScreenActivity）完全兼容
 */
@Composable
fun LiquidPlaybackScrubber(
    progress: Float,
    onScrubbingChange: (isScrubbing: Boolean, fraction: Float) -> Unit,
    onScrubFinished: (fraction: Float) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.White,
    trackColor: Color = Color.White.copy(alpha = 0.24f),
    durationMs: Long? = null,
    positionMs: Long? = null,
    showRemainingTime: Boolean = true,
    centerBadge: (@Composable () -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current
    val currentProgress by rememberUpdatedState(progress)
    val currentOnScrubbingChange by rememberUpdatedState(onScrubbingChange)
    val currentOnScrubFinished by rememberUpdatedState(onScrubFinished)
    var trackWidthPx by remember { mutableFloatStateOf(1f) }
    var isScrubbing by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(progress.coerceIn(0f, 1f)) }

    val displayFraction = (if (isScrubbing) dragFraction else progress).coerceIn(0f, 1f)
    val barHeight by animateDpAsState(
        targetValue = if (isScrubbing) 8.dp else 4.dp,
        animationSpec = LiquidMotion.springSnappy(),
        label = "scrubber_height",
    )
    val glowAlpha by animateFloatAsState(
        targetValue = if (isScrubbing) 1f else 0f,
        animationSpec = if (isScrubbing) tween(80) else tween(LiquidMotion.HighlightFadeOutMs),
        label = "scrubber_glow",
    )
    val activeAlpha by animateFloatAsState(
        targetValue = if (isScrubbing) 1f else 0.90f,
        animationSpec = tween(140),
        label = "scrubber_alpha",
    )
    val timeLabelScale by animateFloatAsState(
        targetValue = if (isScrubbing) 1.07f else 1f,
        animationSpec = LiquidMotion.springSnappy(),
        label = "scrubber_time_scale",
    )
    val timeLabelAlpha by animateFloatAsState(
        targetValue = if (isScrubbing) 0.96f else 0.68f,
        animationSpec = tween(140),
        label = "scrubber_time_alpha",
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
                .onSizeChanged { size ->
                    if (size.width > 0) trackWidthPx = size.width.toFloat()
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        isScrubbing = true
                        val startFrac = (down.position.x / trackWidthPx).coerceIn(0f, 1f)
                        dragFraction = startFrac
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        currentOnScrubbingChange(true, startFrac)

                        var committed = false
                        try {
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                if (!change.pressed) {
                                    val finalFrac = (change.position.x / trackWidthPx).coerceIn(0f, 1f)
                                    dragFraction = finalFrac
                                    change.consume()
                                    isScrubbing = false
                                    currentOnScrubbingChange(false, finalFrac)
                                    currentOnScrubFinished(finalFrac)
                                    committed = true
                                    break
                                }
                                if (change.isConsumed) break
                                val frac = (change.position.x / trackWidthPx).coerceIn(0f, 1f)
                                dragFraction = frac
                                change.consume()
                                currentOnScrubbingChange(true, frac)
                            }
                        } finally {
                            if (!committed) {
                                isScrubbing = false
                                currentOnScrubbingChange(false, currentProgress.coerceIn(0f, 1f))
                            }
                        }
                    }
                },
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barHeight)
                    .drawBehind {
                        if (glowAlpha > 0.01f) {
                            val fillW = (size.width * displayFraction).coerceAtLeast(size.height)
                            val padY = 3.dp.toPx()
                            drawRoundRect(
                                color = activeColor.copy(alpha = 0.28f * glowAlpha),
                                topLeft = Offset(0f, -padY),
                                size = Size(fillW, size.height + padY * 2f),
                                cornerRadius = CornerRadius((size.height + padY * 2f) / 2f),
                            )
                        }
                    }
                    .clip(CircleShape)
                    .background(if (isScrubbing) trackColor.copy(alpha = (trackColor.alpha + 0.10f).coerceAtMost(0.45f)) else trackColor),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(displayFraction.coerceAtLeast(0.008f))
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    activeColor.copy(alpha = activeAlpha * 0.86f),
                                    activeColor.copy(alpha = activeAlpha),
                                ),
                            ),
                        ),
                )
            }
        }

        if (durationMs != null) {
            Spacer(Modifier.height(2.dp))
            val safeDuration = durationMs.coerceAtLeast(0L)
            val shownPos = if (isScrubbing || positionMs == null) {
                (displayFraction * safeDuration).toLong()
            } else {
                positionMs.coerceIn(0L, safeDuration)
            }
            val rightText = if (showRemainingTime) {
                "-${formatMillis((safeDuration - shownPos).coerceAtLeast(0L))}"
            } else {
                formatMillis(safeDuration)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = formatMillis(shownPos),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = activeColor.copy(alpha = timeLabelAlpha),
                    modifier = Modifier.graphicsLayer {
                        scaleX = timeLabelScale
                        scaleY = timeLabelScale
                        transformOrigin = TransformOrigin(0f, 0.5f)
                    },
                )
                if (centerBadge != null) {
                    centerBadge()
                }
                Text(
                    text = rightText,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = activeColor.copy(alpha = timeLabelAlpha),
                    modifier = Modifier.graphicsLayer {
                        scaleX = timeLabelScale
                        scaleY = timeLabelScale
                        transformOrigin = TransformOrigin(1f, 0.5f)
                    },
                )
            }
        }
    }
}
