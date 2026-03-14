package com.microhabits.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Brand colors ──────────────────────────────────────────────────────────────
val Indigo500    = Color(0xFF6C63FF)
val Indigo300    = Color(0xFF9D97FF)
val Indigo100    = Color(0xFFE8E7FF)
val Green400     = Color(0xFF2ECF8A)
val Pink400      = Color(0xFFFF6584)
val Amber400     = Color(0xFFFFB547)
val BgDark       = Color(0xFF0F0F1A)
val SurfaceDark  = Color(0xFF1A1A2E)
val Surface2Dark = Color(0xFF252540)
val TextMuted    = Color(0xFF8B8AA8)

// ── Dark color scheme ─────────────────────────────────────────────────────────
private val DarkColors = darkColorScheme(
    primary          = Indigo500,
    onPrimary        = Color.White,
    primaryContainer = Surface2Dark,
    secondary        = Green400,
    onSecondary      = Color(0xFF003322),
    tertiary         = Pink400,
    background       = BgDark,
    surface          = SurfaceDark,
    surfaceVariant   = Surface2Dark,
    onBackground     = Color(0xFFF0EFF8),
    onSurface        = Color(0xFFF0EFF8),
    onSurfaceVariant = TextMuted,
    outline          = Color(0x28FFFFFF),
    error            = Color(0xFFFF5252)
)

// ── Typography ─────────────────────────────────────────────────────────────────
val AppTypography = Typography(
    displayLarge  = TextStyle(fontWeight = FontWeight.Bold,   fontSize = 32.sp, letterSpacing = (-0.5).sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold,   fontSize = 26.sp),
    headlineMedium= TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    titleLarge    = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    titleMedium   = TextStyle(fontWeight = FontWeight.Medium, fontSize = 15.sp),
    bodyLarge     = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium    = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodySmall     = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, color = TextMuted),
    labelLarge    = TextStyle(fontWeight = FontWeight.Medium, fontSize = 13.sp, letterSpacing = 0.5.sp),
    labelSmall    = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 1.2.sp)
)

@Composable
fun MicroHabitsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography  = AppTypography,
        content     = content
    )
}
