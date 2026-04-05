package com.application.eatbts.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Cyberpunk / neon palette shared across screens (visual layer only).
 */
object NeonTokens {
    val BgVoid = Color(0xFF050508)
    val BgDeep = Color(0xFF0A0A12)
    val BgMid = Color(0xFF12121D)
    val BgElevated = Color(0xFF1A1A28)

    /** Primary neon — magenta / hot pink */
    val NeonMagenta = Color(0xFFFF2E95)
    val NeonMagentaDim = Color(0x99FF2E95)

    /** Secondary neon — cyan / teal */
    val NeonCyan = Color(0xFF00E5FF)
    val NeonCyanDim = Color(0x9900E5FF)

    val GlassFill = Color(0x14FFFFFF)
    val GlassBorderStrong = Color(0x33FFFFFF)
    val TextPrimary = Color(0xFFF5F5F7)
    val TextMuted = Color(0xFFB0B0C0)
    val TextDim = Color(0xFF6A6A7A)

    val NavBarContainer = Color(0xF20A0A12)
    val NavIndicator = Color(0x40FF2E95)

    fun screenBackgroundBrush(): Brush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0D1528),
            BgMid,
            Color(0xFF0A0618),
            BgVoid,
        ),
    )

    fun screenBackgroundBrushAlt(): Brush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1A0F2E),
            BgDeep,
            Color(0xFF080810),
        ),
    )

    fun glassBorderBrush(accent: Color): Brush = Brush.linearGradient(
        colors = listOf(accent.copy(alpha = 0.65f), accent.copy(alpha = 0.12f)),
    )

    fun magentaGlowBrush(): Brush = Brush.horizontalGradient(
        colors = listOf(NeonMagenta, Color(0xFFE040FB)),
    )

    fun cyanAccentBrush(): Brush = Brush.horizontalGradient(
        colors = listOf(NeonCyan, Color(0xFF00B8D4)),
    )
}
