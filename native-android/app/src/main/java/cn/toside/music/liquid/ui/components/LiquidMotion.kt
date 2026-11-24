package cn.toside.music.liquid.ui.components

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * 洛雪原生液态玻璃 v2 统一物理动效系统：
 * - 四档 Apple HIG 物理弹簧常量（Snappy / Gentle / Bouncy / Fluid）
 * - iOS 层次推入/返回转场、Tab 轻缩放交叉淡入、气泡菜单展开、控件错峰入场规范
 */
object LiquidMotion {

    // Core spring physical parameters
    const val SnappyDamping: Float = 0.78f
    const val SnappyStiffness: Float = 520f

    const val GentleDamping: Float = 0.86f
    const val GentleStiffness: Float = 320f

    const val BouncyDamping: Float = 0.58f
    const val BouncyStiffness: Float = 420f

    const val FluidDamping: Float = 0.92f
    const val FluidStiffness: Float = 220f

    // Timing & choreography constants
    const val HighlightFadeOutMs: Int = 180
    const val MeshColorCrossfadeMs: Int = 650
    const val TabFadeInDurationMs: Int = 220
    const val TabFadeOutDurationMs: Int = 160
    const val TabEnterScale: Float = 0.985f

    const val UnderPageParallaxFraction: Float = -0.22f
    const val UnderPageMaxDimAlpha: Float = 0.18f

    const val StaggerStepMs: Int = 36
    const val MaxStaggerItems: Int = 8

    // Pre-built Float SpringSpecs
    val SpringSnappy: SpringSpec<Float> = spring(
        dampingRatio = SnappyDamping,
        stiffness = SnappyStiffness,
    )

    val SpringGentle: SpringSpec<Float> = spring(
        dampingRatio = GentleDamping,
        stiffness = GentleStiffness,
    )

    val SpringBouncy: SpringSpec<Float> = spring(
        dampingRatio = BouncyDamping,
        stiffness = BouncyStiffness,
    )

    val SpringFluid: SpringSpec<Float> = spring(
        dampingRatio = FluidDamping,
        stiffness = FluidStiffness,
    )

    /** 按钮按压回弹、开关、胶囊滑块、气泡菜单弹出 */
    fun <T> springSnappy(visibilityThreshold: T? = null): SpringSpec<T> = spring(
        dampingRatio = SnappyDamping,
        stiffness = SnappyStiffness,
        visibilityThreshold = visibilityThreshold,
    )

    /** 页面推入/返回、MiniPlayer↔全屏展开、搜索栏变形 */
    fun <T> springGentle(visibilityThreshold: T? = null): SpringSpec<T> = spring(
        dampingRatio = GentleDamping,
        stiffness = GentleStiffness,
        visibilityThreshold = visibilityThreshold,
    )

    /** 播放/暂停封面呼吸缩放、红心收藏跳动、切歌反馈 */
    fun <T> springBouncy(visibilityThreshold: T? = null): SpringSpec<T> = spring(
        dampingRatio = BouncyDamping,
        stiffness = BouncyStiffness,
        visibilityThreshold = visibilityThreshold,
    )

    /** 歌词滚动聚焦、流光背景色相渐变、主题色切换 */
    fun <T> springFluid(visibilityThreshold: T? = null): SpringSpec<T> = spring(
        dampingRatio = FluidDamping,
        stiffness = FluidStiffness,
        visibilityThreshold = visibilityThreshold,
    )

    /** 计算列表项/控件错峰入场延迟 */
    fun staggerDelayMs(
        index: Int,
        stepMs: Int = StaggerStepMs,
        maxItems: Int = MaxStaggerItems,
    ): Int = index.coerceIn(0, maxItems) * stepMs

    /** iOS 层次推入：新页从右侧 +100% 弹性推入至 0% */
    fun detailEnterTransition(): EnterTransition =
        slideInHorizontally(
            animationSpec = springGentle(visibilityThreshold = IntOffset(1, 1)),
            initialOffsetX = { fullWidth -> fullWidth },
        ) + fadeIn(tween(durationMillis = 200, easing = FastOutSlowInEasing))

    /** iOS 层次返回：顶层详情页从 0% 滑出至右侧 +100% */
    fun detailExitTransition(): ExitTransition =
        slideOutHorizontally(
            animationSpec = springGentle(visibilityThreshold = IntOffset(1, 1)),
            targetOffsetX = { fullWidth -> fullWidth },
        ) + fadeOut(tween(durationMillis = 180))

    /** iOS 底层页面联动退出：随详情页推入同步左移 0% → -22% */
    fun detailUnderPageExitTransition(): ExitTransition =
        slideOutHorizontally(
            animationSpec = springGentle(visibilityThreshold = IntOffset(1, 1)),
            targetOffsetX = { fullWidth -> (fullWidth * UnderPageParallaxFraction).roundToInt() },
        ) + fadeOut(tween(durationMillis = 220))

    /** iOS 底层页面联动恢复：随详情页返回从 -22% 平滑还原至 0% */
    fun detailUnderPageEnterTransition(): EnterTransition =
        slideInHorizontally(
            animationSpec = springGentle(visibilityThreshold = IntOffset(1, 1)),
            initialOffsetX = { fullWidth -> (fullWidth * UnderPageParallaxFraction).roundToInt() },
        ) + fadeIn(tween(durationMillis = 220))

    /** 主导航 Tab 切换：轻缩放 (0.985 → 1.0) + 交叉淡入 */
    fun tabSwitchTransition(): ContentTransform {
        val enter = fadeIn(tween(durationMillis = TabFadeInDurationMs, easing = FastOutSlowInEasing)) +
            scaleIn(
                initialScale = TabEnterScale,
                animationSpec = springGentle(),
            )
        val exit = fadeOut(tween(durationMillis = TabFadeOutDurationMs)) +
            scaleOut(
                targetScale = TabEnterScale,
                animationSpec = tween(durationMillis = TabFadeOutDurationMs),
            )
        return enter togetherWith exit
    }

    /** 全屏播放页垂直展开 */
    fun modalSheetEnterTransition(): EnterTransition =
        slideInVertically(
            animationSpec = springGentle(visibilityThreshold = IntOffset(1, 1)),
            initialOffsetY = { fullHeight -> fullHeight },
        ) + fadeIn(tween(durationMillis = 180))

    /** 全屏播放页垂直收起 */
    fun modalSheetExitTransition(): ExitTransition =
        slideOutVertically(
            animationSpec = springGentle(visibilityThreshold = IntOffset(1, 1)),
            targetOffsetY = { fullHeight -> fullHeight },
        ) + fadeOut(tween(durationMillis = 180))

    /** 液态玻璃气泡菜单弹性展开（0.84 → 1.0 + fade） */
    fun popoverEnterTransition(origin: TransformOrigin): EnterTransition =
        scaleIn(
            initialScale = 0.84f,
            transformOrigin = origin,
            animationSpec = springSnappy(),
        ) + fadeIn(tween(durationMillis = 150))

    /** 液态玻璃气泡菜单收拢 */
    fun popoverExitTransition(origin: TransformOrigin): ExitTransition =
        scaleOut(
            targetScale = 0.84f,
            transformOrigin = origin,
            animationSpec = tween(durationMillis = 140),
        ) + fadeOut(tween(durationMillis = 130))
}

/**
 * 基于 graphicsLayer 的硬件加速错峰入场动画（避免布局阶段重测卡顿）
 */
fun Modifier.liquidStaggerEntrance(
    index: Int,
    visible: Boolean = true,
    slideDistance: Dp = 14.dp,
    stepMs: Int = LiquidMotion.StaggerStepMs,
): Modifier = composed {
    var revealed by remember(index) { mutableStateOf(false) }

    LaunchedEffect(visible, index) {
        if (visible) {
            val delayMs = LiquidMotion.staggerDelayMs(index = index, stepMs = stepMs)
            if (delayMs > 0) delay(delayMs.toLong())
            revealed = true
        } else {
            revealed = false
        }
    }

    val progress by animateFloatAsState(
        targetValue = if (revealed && visible) 1f else 0f,
        animationSpec = LiquidMotion.springGentle(),
        label = "stagger_entrance_$index",
    )

    this.graphicsLayer {
        alpha = progress.coerceIn(0f, 1f)
        translationY = (1f - progress) * slideDistance.toPx()
    }
}
