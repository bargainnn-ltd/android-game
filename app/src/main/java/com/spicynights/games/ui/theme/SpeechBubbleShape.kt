package com.spicynights.games.ui.theme

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

/**
 * Rounded rectangle with a triangular tail on the right edge (MOD category chips).
 */
class SpeechBubbleShape(
    private val cornerRadius: Dp,
    private val tailWidth: Dp,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val r = cornerRadius.value * density.density
        val tw = tailWidth.value * density.density
        val w = size.width - tw
        val h = size.height
        val ty = h * 0.5f
        val th = h * 0.14f
        val path = Path().apply {
            moveTo(r, 0f)
            lineTo(w - r, 0f)
            quadraticTo(w, 0f, w, r)
            lineTo(w, (ty - th).coerceAtLeast(r))
            lineTo(size.width, ty)
            lineTo(w, (ty + th).coerceAtMost(h - r))
            lineTo(w, h - r)
            quadraticTo(w, h, w - r, h)
            lineTo(r, h)
            quadraticTo(0f, h, 0f, h - r)
            lineTo(0f, r)
            quadraticTo(0f, 0f, r, 0f)
            close()
        }
        return Outline.Generic(path)
    }
}
