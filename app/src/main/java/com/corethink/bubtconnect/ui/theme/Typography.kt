package com.corethink.bubtconnect.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.corethink.bubtconnect.ui.design.PoppinsFontFamily

@Stable
data class BUBTTypography(
    val displayLarge: TextStyle = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 48.sp
    ),
    val displayMedium: TextStyle = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    val displaySmall: TextStyle = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    val headlineLarge: TextStyle = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    val headlineMedium: TextStyle = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    val headlineSmall: TextStyle = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp
    ),
    val titleLarge: TextStyle = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    val titleMedium: TextStyle = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    val titleSmall: TextStyle = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 18.sp
    ),
    val bodyLarge: TextStyle = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    val bodyMedium: TextStyle = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    val bodySmall: TextStyle = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    val labelLarge: TextStyle = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    val labelMedium: TextStyle = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    val labelSmall: TextStyle = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp
    ),
    val buttonLarge: TextStyle = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    val buttonMedium: TextStyle = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    val buttonSmall: TextStyle = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp
    )
)

val LocalBUBTTypography = staticCompositionLocalOf { BUBTTypography() }