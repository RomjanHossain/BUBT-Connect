package com.corethink.bubtconnect.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Stable
data class BUBTColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val success: Color,
    val onSuccess: Color,
    val error: Color,
    val onError: Color,
    val warning: Color,
    val onWarning: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceElevated: Color,
    val onSurfaceElevated: Color,
    val border: Color,
    val divider: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val isDark: Boolean
)

@Stable
data class BUBTSpacing(
    val xs: Float = 4f,
    val sm: Float = 8f,
    val md: Float = 12f,
    val lg: Float = 16f,
    val xl: Float = 20f,
    val xxl: Float = 24f,
    val xxxl: Float = 32f
)

@Stable
data class BUBTRadius(
    val xs: Float = 4f,
    val sm: Float = 8f,
    val md: Float = 12f,
    val lg: Float = 16f,
    val xl: Float = 20f,
    val full: Float = 9999f
)

@Stable
data class BUBTElevation(
    val none: Float = 0f,
    val sm: Float = 1f,
    val md: Float = 4f,
    val lg: Float = 8f,
    val xl: Float = 12f
)

private val LightBUBTColors = BUBTColors(
    primary = BUBT_Navy,
    onPrimary = Color.White,
    primaryContainer = BUBT_Navy.copy(alpha = 0.08f),
    onPrimaryContainer = BUBT_Navy,
    secondary = BUBT_Blue,
    onSecondary = Color.White,
    secondaryContainer = BUBT_Blue.copy(alpha = 0.08f),
    onSecondaryContainer = BUBT_Blue,
    success = BUBT_Green,
    onSuccess = Color.White,
    error = BUBT_Red,
    onError = Color.White,
    warning = Color(0xFFF59E0B),
    onWarning = Color.White,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceElevated = SurfaceElevatedLight,
    onSurfaceElevated = TextPrimaryLight,
    border = BorderLight,
    divider = DividerLight,
    textPrimary = TextPrimaryLight,
    textSecondary = TextSecondaryLight,
    textTertiary = TextTertiaryLight,
    isDark = false
)

private val DarkBUBTColors = BUBTColors(
    primary = BUBT_Cyan,
    onPrimary = Color.White,
    primaryContainer = BUBT_Cyan.copy(alpha = 0.16f),
    onPrimaryContainer = Color.White,
    secondary = BUBT_Blue,
    onSecondary = Color.White,
    secondaryContainer = BUBT_Blue.copy(alpha = 0.16f),
    onSecondaryContainer = Color.White,
    success = BUBT_Green,
    onSuccess = Color.White,
    error = Color(0xFFF87171),
    onError = BackgroundDark,
    warning = Color(0xFFFBBF24),
    onWarning = BackgroundDark,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceElevated = SurfaceElevatedDark,
    onSurfaceElevated = TextPrimaryDark,
    border = BorderDark,
    divider = DividerDark,
    textPrimary = TextPrimaryDark,
    textSecondary = TextSecondaryDark,
    textTertiary = TextTertiaryDark,
    isDark = true
)

val LocalBUBTColors = staticCompositionLocalOf { LightBUBTColors }
val LocalBUBTSpacing = staticCompositionLocalOf { BUBTSpacing() }
val LocalBUBTRadius = staticCompositionLocalOf { BUBTRadius() }
val LocalBUBTElevation = staticCompositionLocalOf { BUBTElevation() }

@Composable
fun BubtConnectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkBUBTColors else LightBUBTColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalBUBTColors provides colors,
        LocalBUBTSpacing provides BUBTSpacing(),
        LocalBUBTRadius provides BUBTRadius(),
        LocalBUBTElevation provides BUBTElevation(),
        LocalBUBTTypography provides BUBTTypography(),
        content = content
    )
}

object BubtTheme {
    val colors: BUBTColors
        @Composable
        get() = LocalBUBTColors.current

    val spacing: BUBTSpacing
        @Composable
        get() = LocalBUBTSpacing.current

    val radius: BUBTRadius
        @Composable
        get() = LocalBUBTRadius.current

    val elevation: BUBTElevation
        @Composable
        get() = LocalBUBTElevation.current

    val typography: BUBTTypography
        @Composable
        get() = LocalBUBTTypography.current
}