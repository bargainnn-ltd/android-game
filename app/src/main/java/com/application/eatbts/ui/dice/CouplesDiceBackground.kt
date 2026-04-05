package com.application.eatbts.ui.dice

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin

private val RomanticDeep = Color(0xFF2D0A1C)
private val RomanticRose = Color(0xFF8E2D56)
private val RomanticPink = Color(0xFFC2185B)
private val RomanticLilac = Color(0xFF6A1B9A)
private val RomanticMauve = Color(0xFF4A148C)

/**
 * Soft romantic gradient + floating sparkles/hearts as light dots (performance-friendly).
 */
@Composable
fun CouplesDiceBackground(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "particles")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "phase",
    )

    Box(modifier = modifier) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(RomanticDeep, RomanticRose, RomanticPink, RomanticLilac, RomanticMauve),
                    start = Offset(0f, 0f),
                    end = Offset(size.width * 1.1f, size.height),
                ),
            )
            val n = 18
            val t = phase * 2f * Math.PI.toFloat()
            for (i in 0 until n) {
                val baseX = size.width * (0.05f + (i * 47 % 90) / 100f)
                val baseY = size.height * (0.05f + (i * 61 % 90) / 100f)
                val ox = 22f * sin(t + i * 0.7f)
                val oy = 18f * cos(t * 0.85f + i * 0.4f)
                val x = baseX + ox
                val y = baseY + oy
                val alpha = 0.15f + 0.12f * sin(t + i)
                if (i % 4 == 0) {
                    drawCircle(
                        color = Color(0xFFFFC1E3).copy(alpha = alpha),
                        radius = 2.5f + (i % 3),
                        center = Offset(x, y),
                    )
                } else {
                    rotate(degrees = (i * 24 + phase * 360f) % 360f, pivot = Offset(x, y)) {
                        drawCircle(
                            color = Color(0xFFFFE0F0).copy(alpha = alpha + 0.1f),
                            radius = 1.8f,
                            center = Offset(x, y),
                        )
                        drawCircle(
                            color = Color(0xFFFFE0F0).copy(alpha = alpha * 0.6f),
                            radius = 1.2f,
                            center = Offset(x + 3f, y),
                        )
                    }
                }
            }
        }
    }
}
