package cn.toside.music.liquid.ui.adaptive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class LiquidWidthSizeClass { Compact, Medium, Expanded }
enum class LiquidHeightSizeClass { Compact, Medium, Expanded }

enum class LiquidNavigationMode {
    BottomDock,      // Compact (<600dp) or phone portrait
    TopFloatingTabs, // Medium (600-839dp), compact-height landscape, or Expanded with sidebar collapsed
    LeftSidebar,     // Expanded (>=840dp and non-compact height) with sidebar expanded
}

@Immutable
data class LiquidWindowClass(
    val widthSizeClass: LiquidWidthSizeClass = LiquidWidthSizeClass.Compact,
    val heightSizeClass: LiquidHeightSizeClass = LiquidHeightSizeClass.Medium,
    val widthDp: Dp = 411.dp,
    val heightDp: Dp = 915.dp,
    val isCompactHeight: Boolean = heightDp < 480.dp,
    val horizontalMargin: Dp = when (widthSizeClass) {
        LiquidWidthSizeClass.Compact -> 20.dp
        LiquidWidthSizeClass.Medium -> 28.dp
        LiquidWidthSizeClass.Expanded -> 36.dp
    },
    val gridMinCellSize: Dp = when (widthSizeClass) {
        LiquidWidthSizeClass.Compact -> 156.dp
        LiquidWidthSizeClass.Medium -> 164.dp
        LiquidWidthSizeClass.Expanded -> 176.dp
    },
    val prefersTwoPaneDetail: Boolean =
        widthSizeClass == LiquidWidthSizeClass.Expanded && !isCompactHeight,
    val prefersHorizontalDetailHero: Boolean =
        widthSizeClass == LiquidWidthSizeClass.Medium || isCompactHeight,
    val prefersTwoPanePlayer: Boolean =
        widthSizeClass == LiquidWidthSizeClass.Expanded ||
            (widthSizeClass == LiquidWidthSizeClass.Medium && widthDp >= 720.dp) ||
            isCompactHeight,
) {
    fun resolveNavigationMode(sidebarExpanded: Boolean): LiquidNavigationMode = when {
        widthSizeClass == LiquidWidthSizeClass.Compact -> LiquidNavigationMode.BottomDock
        isCompactHeight -> LiquidNavigationMode.TopFloatingTabs
        widthSizeClass == LiquidWidthSizeClass.Expanded && sidebarExpanded -> LiquidNavigationMode.LeftSidebar
        else -> LiquidNavigationMode.TopFloatingTabs
    }
}

@Composable
fun rememberLiquidWindowClass(): LiquidWindowClass {
    val cfg = LocalConfiguration.current
    val w = cfg.screenWidthDp.dp
    val h = cfg.screenHeightDp.dp
    val widthClass = when {
        w >= 840.dp -> LiquidWidthSizeClass.Expanded
        w >= 600.dp -> LiquidWidthSizeClass.Medium
        else -> LiquidWidthSizeClass.Compact
    }
    val heightClass = when {
        h >= 900.dp -> LiquidHeightSizeClass.Expanded
        h >= 480.dp -> LiquidHeightSizeClass.Medium
        else -> LiquidHeightSizeClass.Compact
    }
    return LiquidWindowClass(
        widthSizeClass = widthClass,
        heightSizeClass = heightClass,
        widthDp = w,
        heightDp = h,
        isCompactHeight = h < 480.dp,
    )
}

/** Provides current window breakpoint class; defaults to standard phone portrait for preview/test safety. */
val LocalLiquidWindowClass = compositionLocalOf { LiquidWindowClass() }

/** Extra top clearance required when TopFloatingTabs capsule is floating above screen headers. */
val LocalTopBarClearance = compositionLocalOf { 0.dp }

/** Optional NestedScrollConnection driving iOS 26 scroll-to-minimize bottom dock on Compact screens. */
val LocalDockScrollConnection = compositionLocalOf<NestedScrollConnection?> { null }

/** Callback allowing full-bleed detail screens to report extracted cover palette color to the MiniPlayer. */
val LocalImmersiveTintReporter = compositionLocalOf<(Color?) -> Unit> { {} }
