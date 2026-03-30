package com.spicynights.games.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.spicynights.games.data.Level

object ModColors {
    val CardRed = Color(0xFFE63946)
    val CardRedDeep = Color(0xFFC4302B)
    val CardRedMaleTint = Color(0xFFE64A4A)
    val CardRedFemaleTint = Color(0xFFE62E5C)
    val StrokeBlack = Color(0xFF1A1A1A)
    val BubbleYellow = Color(0xFFFFEB3B)
    val BubbleYellowMuted = Color(0xFFFFF59D)
    val FooterBlack = Color(0xFF1A1A1A)
    val FooterWhite = Color(0xFFFFFFFF)
    val HubCardRed = Color(0xFFE62129)
    val HubGradientTop = Color(0xFFFF2E95)
    val HubGradientBottom = Color(0xFF8B1538)
    val SetupGradientTop = Color(0xFFFF1E82)
    val SetupGradientBottom = Color(0xFF6B0A20)
    val GlassOverlay = Color(0xFF000000).copy(alpha = 0.45f)
    val ClimaxGlow = Color(0xFFFF1744)

    fun cardFill(isMaleTurn: Boolean): Color {
        val base = CardRed
        val tint = if (isMaleTurn) CardRedMaleTint else CardRedFemaleTint
        return lerp(base, tint, 0.22f)
    }

    fun gameBackgroundBrush(level: Level): Brush {
        return when (level) {
            Level.TRIALS -> Brush.verticalGradient(
                colors = listOf(Color(0xFFFF3D7A), Color(0xFF7D1028)),
            )
            Level.WANDERINGS -> Brush.verticalGradient(
                colors = listOf(Color(0xFFFF2E8A), Color(0xFF5C0A18)),
            )
            Level.CLIMAX -> Brush.verticalGradient(
                colors = listOf(Color(0xFFFF1E6E), Color(0xFF3D050F)),
            )
        }
    }

    fun setupScreenBrush(): Brush = Brush.verticalGradient(
        colors = listOf(SetupGradientTop, SetupGradientBottom),
    )

    fun disclaimerBrush(): Brush = Brush.verticalGradient(
        colors = listOf(HubGradientTop.copy(alpha = 0.95f), HubGradientBottom),
    )
}
