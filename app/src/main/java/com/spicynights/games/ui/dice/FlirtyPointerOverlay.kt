package com.spicynights.games.ui.dice

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Sparkle burst when [showSparkles] is true (hover on desktop or finger down on the die).
 */
@Composable
fun FlirtyPointerOverlay(
    showSparkles: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        content()
        if (showSparkles) {
            Canvas(Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val pts = listOf(
                    Offset(cx - size.width * 0.35f, cy - size.height * 0.2f),
                    Offset(cx + size.width * 0.35f, cy - size.height * 0.15f),
                    Offset(cx, cy + size.height * 0.35f),
                    Offset(cx - size.width * 0.4f, cy + size.height * 0.1f),
                    Offset(cx + size.width * 0.4f, cy + size.height * 0.2f),
                )
                pts.forEachIndexed { i, p ->
                    drawCircle(
                        color = Color(0xFFFF80AB).copy(alpha = 0.45f),
                        radius = 3f + (i % 2),
                        center = p,
                    )
                    drawCircle(
                        color = Color(0xFFFFE082).copy(alpha = 0.35f),
                        radius = 2f,
                        center = Offset(p.x + 6f, p.y - 4f),
                        style = Stroke(width = 1f),
                    )
                }
            }
        }
    }
}
