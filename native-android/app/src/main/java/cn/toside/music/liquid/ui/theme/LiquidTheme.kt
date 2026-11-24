package cn.toside.music.liquid.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import cn.toside.music.liquid.data.model.MaterialStyle
import cn.toside.music.liquid.data.model.ThemeMode
import dev.chrisbanes.haze.HazeState

@Immutable
data class LiquidColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val accent: Color,
    val accentSoft: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val divider: Color,
    val glassTint: Color,
    val glassTintDense: Color,
    val glassRimTop: Color,
    val glassRimBottom: Color,
    val glassShadow: Color,
    val glassPillActive: Color,
    val badgeBg: Color,
    val chipBackground: Color,
    val glassBorder: Color,
    val destructive: Color,
    val success: Color,
)

val LightLiquidColors = LiquidColors(
    isDark = false,
    background = Color(0xFFF5F5F8),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFFFFFFF),
    accent = Color(0xFFFA243C),
    accentSoft = Color(0x18FA243C),
    textPrimary = Color(0xFF111116),
    textSecondary = Color(0xFF63636D),
    textTertiary = Color(0xFF93939E),
    divider = Color(0x14000000),
    glassTint = Color(0xA6FFFFFF),
    glassTintDense = Color(0xCCFFFFFF),
    glassRimTop = Color(0xE8FFFFFF),
    glassRimBottom = Color(0x38FFFFFF),
    glassShadow = Color(0x180C1020),
    glassPillActive = Color(0xE6FFFFFF),
    badgeBg = Color(0x10000000),
    chipBackground = Color(0x10000000),
    glassBorder = Color(0x22000000),
    destructive = Color(0xFFFF3B30),
    success = Color(0xFF34C759),
)

val DarkLiquidColors = LiquidColors(
    isDark = true,
    background = Color(0xFF09090D),
    surface = Color(0xFF15151C),
    surfaceElevated = Color(0xFF1E1E28),
    accent = Color(0xFFFF375F),
    accentSoft = Color(0x28FF375F),
    textPrimary = Color(0xFFF5F5FA),
    textSecondary = Color(0xFFA0A0AE),
    textTertiary = Color(0xFF6D6D7B),
    divider = Color(0x1AFFFFFF),
    glassTint = Color(0x881A1A24),
    glassTintDense = Color(0xB8181822),
    glassRimTop = Color(0x46FFFFFF),
    glassRimBottom = Color(0x14FFFFFF),
    glassShadow = Color(0x55000000),
    glassPillActive = Color(0x2EFFFFFF),
    badgeBg = Color(0x1AFFFFFF),
    chipBackground = Color(0x1AFFFFFF),
    glassBorder = Color(0x2EFFFFFF),
    destructive = Color(0xFFFF453A),
    success = Color(0xFF30D158),
)

val LocalLiquidColors = staticCompositionLocalOf { LightLiquidColors }
val LocalMaterialStyle = staticCompositionLocalOf { MaterialStyle.LIQUID_GLASS }
val LocalHazeState = staticCompositionLocalOf<HazeState?> { null }

private val LiquidTypography = Typography(
    displaySmall = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.6).sp,
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.4).sp,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 25.sp,
        letterSpacing = (-0.3).sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.15).sp,
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 21.sp,
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.5.sp,
        lineHeight = 19.sp,
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.5.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.2.sp,
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.4.sp,
    ),
)

@Composable
fun LxLiquidTheme(
    themeMode: ThemeMode,
    materialStyle: MaterialStyle,
    hazeState: HazeState?,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemDark
    }
    val colors = if (isDark) DarkLiquidColors else LightLiquidColors

    val materialScheme = if (isDark) {
        darkColorScheme(
            primary = colors.accent,
            background = colors.background,
            surface = colors.surface,
            onPrimary = Color.White,
            onBackground = colors.textPrimary,
            onSurface = colors.textPrimary,
            surfaceVariant = colors.surfaceElevated,
            onSurfaceVariant = colors.textSecondary,
        )
    } else {
        lightColorScheme(
            primary = colors.accent,
            background = colors.background,
            surface = colors.surface,
            onPrimary = Color.White,
            onBackground = colors.textPrimary,
            onSurface = colors.textPrimary,
            surfaceVariant = colors.surfaceElevated,
            onSurfaceVariant = colors.textSecondary,
        )
    }

    CompositionLocalProvider(
        LocalLiquidColors provides colors,
        LocalMaterialStyle provides materialStyle,
        LocalHazeState provides hazeState,
    ) {
        MaterialTheme(
            colorScheme = materialScheme,
            typography = LiquidTypography,
            content = content,
        )
    }
}
