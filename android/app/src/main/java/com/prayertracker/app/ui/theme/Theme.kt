package com.prayertracker.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Brand palette (warm, light, minimal — matches the reference) ────────────
val Coral = Color(0xFFEC5E3F)      // accent: selected states, highlights, rings
val Ink = Color(0xFF171614)        // near-black: primary buttons, headings
val Warm = Color(0xFFF4F2EE)       // app background
val CardWhite = Color(0xFFFFFFFF)  // cards
val Muted = Color(0xFF8C877D)      // secondary/label text
val Track = Color(0xFFEBE7E0)      // unfilled tracks, chips, dividers

private val BrandColors = lightColorScheme(
    primary = Coral,
    onPrimary = Color.White,
    secondary = Ink,               // used for black pill buttons
    onSecondary = Color.White,
    background = Warm,
    onBackground = Ink,
    surface = CardWhite,
    onSurface = Ink,
    surfaceVariant = Track,
    onSurfaceVariant = Muted,
    outline = Track,
    outlineVariant = Track,
    error = Color(0xFFC0392B),
    onError = Color.White,
)

private val BrandShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(34.dp),
)

private val BrandType: Typography = Typography().run {
    copy(
        displaySmall = displaySmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-1).sp),
        headlineLarge = headlineLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
        headlineSmall = headlineSmall.copy(fontWeight = FontWeight.Bold),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.Bold),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold),
        labelSmall = labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
    )
}

/** Small uppercase caption used for section labels (BODYWEIGHT / CALORIES style). */
val CaptionLabel = TextStyle(
    fontSize = 11.sp,
    fontWeight = FontWeight.SemiBold,
    letterSpacing = 1.4.sp,
)

/**
 * Brand-locked light theme (the reference is a single light look), so we don't follow the
 * system dark setting or dynamic color — the palette above is the identity.
 */
@Composable
fun PrayerTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BrandColors,
        shapes = BrandShapes,
        typography = BrandType,
        content = content,
    )
}
