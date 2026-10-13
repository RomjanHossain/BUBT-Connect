package com.corethink.bubtconnect.ui.design

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Design tokens from "Student App UI.dc" — BUBT Companion.
 *
 * LIGHT / DARK
 * Primary            #2563EB / #60A5FA
 * Primary container  #DBEAFE / #1B2B52
 * Surface            #FFFFFF / #121212
 * Surface container  #F3F5F9 / #1C1C20
 * Card (elevated)    #FFFFFF + shadow / #1E1E23 tonal
 * On surface         #0F172A / #EDEFF3
 * On surface variant #64748B / #9AA4B2
 * Outline            #E2E8F0 / #33343A
 * Success            #10B981 (same)
 * Warning            #F59E0B (same)
 * Error/Supplementary #EF4444 (same)
 *
 * Status colors render as tinted containers (18% in dark) with
 * darker/lighter text for 4.5:1 contrast.
 */

// ---------------------------------------------------------------- primary
val BubtPrimaryLight = Color(0xFF2563EB)
val BubtPrimaryDark = Color(0xFF60A5FA)

val BubtPrimaryContainerLight = Color(0xFFDBEAFE)
val BubtOnPrimaryContainerLight = Color(0xFF1E3A8A)

val BubtPrimaryContainerDark = Color(0xFF1B2B52)
val BubtOnPrimaryContainerDark = Color(0xFFDBEAFE)

val BubtOnPrimaryDark = Color(0xFF0B1220)

// ---------------------------------------------------------------- surface
val BubtSurfaceLight = Color(0xFFFFFFFF)
val BubtSurfaceContainerLight = Color(0xFFF3F5F9)
val BubtCardElevatedLight = Color(0xFFFFFFFF)

val BubtSurfaceDark = Color(0xFF121212)
val BubtSurfaceContainerDark = Color(0xFF1C1C20)
val BubtCardElevatedDark = Color(0xFF1E1E23)

val BubtOnSurfaceLight = Color(0xFF0F172A)
val BubtOnSurfaceDark = Color(0xFFEDEFF3)

val BubtOnSurfaceVariantLight = Color(0xFF64748B)
val BubtOnSurfaceVariantDark = Color(0xFF9AA4B2)

val BubtOutlineLight = Color(0xFFE2E8F0)
val BubtOutlineDark = Color(0xFF33343A)

// ------------------------------------------------- status (same in both)
val BubtSuccess = Color(0xFF10B981)
val BubtWarning = Color(0xFFF59E0B)
val BubtError = Color(0xFFEF4444)

// Status containers — 18% tint, with 4.5:1 text on top.
val BubtSuccessContainerLight = Color(0xFFD1FAE5)
val BubtOnSuccessContainerLight = Color(0xFF047857)
val BubtSuccessContainerDark = Color(0xFF123026)
val BubtOnSuccessContainerDark = Color(0xFF6EE7B7)

val BubtWarningContainerLight = Color(0xFFFEF3C7)
val BubtOnWarningContainerLight = Color(0xFF92400E)
val BubtWarningContainerDark = Color(0xFF3B2B11)
val BubtOnWarningContainerDark = Color(0xFFFCD34D)

val BubtErrorContainerLight = Color(0xFFFEE2E2)
val BubtOnErrorContainerLight = Color(0xFFB91C1C)
val BubtErrorContainerDark = Color(0xFF3A1B1B)
val BubtOnErrorContainerDark = Color(0xFFFCA5A5)

// Error text used on plain surfaces — darkened to clear 4.5:1.
val BubtErrorTextLight = Color(0xFFDC2626)
val BubtErrorTextDark = Color(0xFFFCA5A5)

// ------------------------------------------------------------- supporting
val BubtBackgroundLight = BubtSurfaceLight
val BubtBackgroundDark = BubtSurfaceDark

val LightColorSchemeM3 = lightColorScheme(
    primary = BubtPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = BubtPrimaryContainerLight,
    onPrimaryContainer = BubtOnPrimaryContainerLight,
    secondary = BubtPrimaryLight,
    onSecondary = Color.White,
    secondaryContainer = BubtPrimaryContainerLight,
    onSecondaryContainer = BubtOnPrimaryContainerLight,
    tertiary = BubtWarning,
    onTertiary = Color.White,
    tertiaryContainer = BubtWarningContainerLight,
    onTertiaryContainer = BubtOnWarningContainerLight,
    error = BubtError,
    onError = Color.White,
    errorContainer = BubtErrorContainerLight,
    onErrorContainer = BubtOnErrorContainerLight,
    background = BubtBackgroundLight,
    onBackground = BubtOnSurfaceLight,
    surface = BubtSurfaceLight,
    onSurface = BubtOnSurfaceLight,
    surfaceVariant = BubtSurfaceContainerLight,
    onSurfaceVariant = BubtOnSurfaceVariantLight,
    outline = BubtOutlineLight,
    outlineVariant = BubtOutlineLight,
    scrim = Color.Black,
    inverseSurface = BubtOnSurfaceLight,
    inverseOnSurface = BubtSurfaceLight,
    inversePrimary = BubtPrimaryDark,
    surfaceDim = Color(0xFFDDE3EC),
    surfaceBright = BubtSurfaceLight,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8FAFC),
    surfaceContainer = BubtSurfaceContainerLight,
    surfaceContainerHigh = Color(0xFFECEFF4),
    surfaceContainerHighest = Color(0xFFE4E9F0)
)

val DarkColorSchemeM3 = darkColorScheme(
    primary = BubtPrimaryDark,
    onPrimary = BubtOnPrimaryDark,
    primaryContainer = BubtPrimaryContainerDark,
    onPrimaryContainer = BubtOnPrimaryContainerDark,
    secondary = BubtPrimaryDark,
    onSecondary = BubtOnPrimaryDark,
    secondaryContainer = BubtPrimaryContainerDark,
    onSecondaryContainer = BubtOnPrimaryContainerDark,
    tertiary = BubtWarning,
    onTertiary = BubtOnPrimaryDark,
    tertiaryContainer = BubtWarningContainerDark,
    onTertiaryContainer = BubtOnWarningContainerDark,
    error = BubtError,
    onError = Color.White,
    errorContainer = BubtErrorContainerDark,
    onErrorContainer = BubtOnErrorContainerDark,
    background = BubtBackgroundDark,
    onBackground = BubtOnSurfaceDark,
    surface = BubtSurfaceDark,
    onSurface = BubtOnSurfaceDark,
    surfaceVariant = BubtSurfaceContainerDark,
    onSurfaceVariant = BubtOnSurfaceVariantDark,
    outline = BubtOutlineDark,
    outlineVariant = BubtOutlineDark,
    scrim = Color.Black,
    inverseSurface = BubtOnSurfaceDark,
    inverseOnSurface = BubtSurfaceDark,
    inversePrimary = BubtPrimaryLight,
    surfaceDim = BubtSurfaceDark,
    surfaceBright = Color(0xFF38383C),
    surfaceContainerLowest = Color(0xFF0C0C0C),
    surfaceContainerLow = Color(0xFF161618),
    surfaceContainer = BubtSurfaceContainerDark,
    surfaceContainerHigh = Color(0xFF232327),
    surfaceContainerHighest = BubtCardElevatedDark
)
