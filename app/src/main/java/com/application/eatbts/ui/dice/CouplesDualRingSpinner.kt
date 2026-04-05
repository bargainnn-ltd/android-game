package com.application.eatbts.ui.dice

import android.graphics.Canvas
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/** Matches [runRollAnimation] delay in Spicy Spinner screen. */
const val SpicySpinnerSpinDurationMs = 2800

/** Longer ring uses [SpicySpinnerSpinDurationMs] + offset; keep UI [isRolling] until both finish. */
const val SpicySpinnerSpinTotalMs = SpicySpinnerSpinDurationMs + 220

private const val Segments = 8
private const val SweepPerSegment = 360f / Segments

private fun normalizeDeg(d: Float): Float {
    var x = d % 360f
    if (x < 0f) x += 360f
    return x
}

/** Clockwise rotation so segment [value] center aligns with the top pointer. */
private fun absoluteRotationForSegment(value: Int): Float {
    val v = value.coerceIn(1, Segments)
    val segmentCenterDeg = -67.5f + (v - 1) * SweepPerSegment
    val r = -90f - segmentCenterDeg
    return normalizeDeg(r)
}

private fun computeNextRotation(current: Float, value: Int, fullSpins: Int): Float {
    val targetMod = absoluteRotationForSegment(value)
    val curMod = normalizeDeg(current % 360f)
    val delta = (targetMod - curMod + 360f) % 360f
    return current + fullSpins * 360f + delta
}

private fun Path.addAnnulusWedge(
    cx: Float,
    cy: Float,
    innerR: Float,
    outerR: Float,
    startDeg: Float,
    sweepDeg: Float,
) {
    val startRad = Math.toRadians(startDeg.toDouble())
    val endRad = Math.toRadians((startDeg + sweepDeg).toDouble())
    val xio = cx + innerR * cos(startRad).toFloat()
    val yio = cy + innerR * sin(startRad).toFloat()
    val xoo = cx + outerR * cos(startRad).toFloat()
    val yoo = cy + outerR * sin(startRad).toFloat()
    moveTo(xio, yio)
    lineTo(xoo, yoo)
    val outerRect = Rect(cx - outerR, cy - outerR, cx + outerR, cy + outerR)
    arcTo(outerRect, startDeg, sweepDeg, forceMoveTo = false)
    val xie = cx + innerR * cos(endRad).toFloat()
    val yie = cy + innerR * sin(endRad).toFloat()
    lineTo(xie, yie)
    val innerRect = Rect(cx - innerR, cy - innerR, cx + innerR, cy + innerR)
    arcTo(innerRect, startDeg + sweepDeg, -sweepDeg, forceMoveTo = false)
    close()
}

/** Max horizontal width for label text inside a ring segment (chord of the arc, with margin). */
private fun segmentLabelMaxWidth(labelR: Float): Float {
    val halfSweepRad = Math.toRadians((SweepPerSegment / 2f).toDouble())
    return (2f * labelR * sin(halfSweepRad).toFloat() * 0.88f).coerceAtLeast(24f)
}

/**
 * Draws [text] centered at ([cx], [cy]), shrinking single-line size or using up to 2 lines via [StaticLayout].
 */
private fun Canvas.drawSegmentLabel(
    text: String,
    cx: Float,
    cy: Float,
    maxWidth: Float,
    baseTextSize: Float,
    minTextSize: Float,
) {
    val paint = TextPaint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        isFakeBoldText = true
        textAlign = android.graphics.Paint.Align.LEFT
    }
    var size = baseTextSize
    while (size >= minTextSize) {
        paint.textSize = size
        if (paint.measureText(text) <= maxWidth) {
            paint.textAlign = android.graphics.Paint.Align.CENTER
            drawText(text, cx, cy + size / 3f, paint)
            return
        }
        size -= 0.75f
    }
    paint.textSize = minTextSize
    @Suppress("DEPRECATION")
    val layout = StaticLayout(
        text,
        paint,
        maxWidth.toInt().coerceAtLeast(1),
        Layout.Alignment.ALIGN_CENTER,
        1f,
        0f,
        false,
    )
    save()
    translate(cx - layout.width / 2f, cy - layout.height / 2f)
    layout.draw(this)
    restore()
}

@Composable
fun CouplesDualRingSpinner(
    bodyRoll: Int?,
    actionRoll: Int?,
    isRolling: Boolean,
    animationKey: Int,
    outerLabel: String,
    innerLabel: String,
    outerLabels: Array<out String>,
    innerLabels: Array<out String>,
    modifier: Modifier = Modifier,
) {
    val outerRotation = remember { Animatable(0f) }
    val innerRotation = remember { Animatable(0f) }

    val outerPalette = remember {
        listOf(
            Color(0xFFFF2E95),
            Color(0xFFB042F5),
            Color(0xFF2A2A32),
            Color(0xFFAB47BC),
            Color(0xFFFF2E95),
            Color(0xFFB042F5),
            Color(0xFF2A2A32),
            Color(0xFFE040FB),
        )
    }
    val innerPalette = remember {
        listOf(
            Color(0xFFAD1457),
            Color(0xFFB042F5),
            Color(0xFF2A2A32),
            Color(0xFFE91E63),
            Color(0xFFC2185B),
            Color(0xFF8E24AA),
            Color(0xFF2A2A32),
            Color(0xFFFF2E95),
        )
    }

    LaunchedEffect(animationKey) {
        val b = bodyRoll ?: return@LaunchedEffect
        val a = actionRoll ?: return@LaunchedEffect
        coroutineScope {
            val oTarget = computeNextRotation(outerRotation.value, b, fullSpins = 4)
            val iTarget = computeNextRotation(innerRotation.value, a, fullSpins = 5)
            val outerJob = async {
                outerRotation.animateTo(
                    oTarget,
                    tween(SpicySpinnerSpinDurationMs, easing = FastOutSlowInEasing),
                )
            }
            val innerJob = async {
                innerRotation.animateTo(
                    iTarget,
                    tween(SpicySpinnerSpinDurationMs + 180, easing = FastOutSlowInEasing),
                )
            }
            outerJob.await()
            innerJob.await()
        }
    }

    val highlightOuter = if (!isRolling && bodyRoll != null) bodyRoll else null
    val highlightInner = if (!isRolling && actionRoll != null) actionRoll else null

    val density = LocalDensity.current
    val labelPx = with(density) { 14.sp.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(4.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Outer ring (body)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                    rotationZ = outerRotation.value
                },
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val rMax = size.minDimension / 2f
            val outerOuterR = rMax * 0.92f
            val outerInnerR = rMax * 0.58f
            val labelR = (outerOuterR + outerInnerR) / 2f
            val strokeW = 2.dp.toPx()

            for (i in 0 until Segments) {
                val start = -90f + i * SweepPerSegment
                val segValue = i + 1
                val base = outerPalette[i]
                val fill = when {
                    highlightOuter == segValue -> base.copy(alpha = 1f)
                    highlightOuter != null -> base.copy(alpha = 0.55f)
                    else -> base.copy(alpha = 0.85f)
                }
                val path = Path().apply {
                    addAnnulusWedge(cx, cy, outerInnerR, outerOuterR, start, SweepPerSegment)
                }
                drawPath(path, fill)
                drawPath(path, Color.White.copy(alpha = 0.35f), style = Stroke(width = strokeW))
                val mid = start + SweepPerSegment / 2f
                val rad = Math.toRadians(mid.toDouble())
                val tx = cx + labelR * cos(rad).toFloat()
                val ty = cy + labelR * sin(rad).toFloat()
                val labelText = outerLabels.getOrElse(i) { "$segValue" }
                val maxW = segmentLabelMaxWidth(labelR)
                drawContext.canvas.nativeCanvas.drawSegmentLabel(
                    labelText,
                    tx,
                    ty,
                    maxW,
                    baseTextSize = labelPx.coerceIn(18f, 28f),
                    minTextSize = 9f,
                )
            }
        }

        // Inner ring (action)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                    rotationZ = innerRotation.value
                },
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val rMax = size.minDimension / 2f
            val innerOuterR = rMax * 0.52f
            val innerInnerR = rMax * 0.22f
            val labelR = (innerOuterR + innerInnerR) / 2f
            val strokeW = 2.dp.toPx()

            for (i in 0 until Segments) {
                val start = -90f + i * SweepPerSegment
                val segValue = i + 1
                val base = innerPalette[i]
                val fill = when {
                    highlightInner == segValue -> base.copy(alpha = 1f)
                    highlightInner != null -> base.copy(alpha = 0.55f)
                    else -> base.copy(alpha = 0.85f)
                }
                val path = Path().apply {
                    addAnnulusWedge(cx, cy, innerInnerR, innerOuterR, start, SweepPerSegment)
                }
                drawPath(path, fill)
                drawPath(path, Color.White.copy(alpha = 0.35f), style = Stroke(width = strokeW))
                val mid = start + SweepPerSegment / 2f
                val rad = Math.toRadians(mid.toDouble())
                val tx = cx + labelR * cos(rad).toFloat()
                val ty = cy + labelR * sin(rad).toFloat()
                val labelText = innerLabels.getOrElse(i) { "$segValue" }
                val maxW = segmentLabelMaxWidth(labelR)
                drawContext.canvas.nativeCanvas.drawSegmentLabel(
                    labelText,
                    tx,
                    ty,
                    maxW,
                    baseTextSize = (labelPx * 0.82f).coerceIn(14f, 22f),
                    minTextSize = 8f,
                )
            }
        }

        // Pointer + center labels (not rotated)
        Canvas(Modifier.fillMaxWidth().aspectRatio(1f)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val rMax = size.minDimension / 2f
            val pointerPath = Path().apply {
                val tipY = cy - rMax * 0.94f
                val halfW = rMax * 0.06f
                moveTo(cx, tipY - rMax * 0.04f)
                lineTo(cx - halfW, tipY + rMax * 0.07f)
                lineTo(cx + halfW, tipY + rMax * 0.07f)
                close()
            }
            drawPath(pointerPath, Color(0xFFFFE082))
            drawPath(pointerPath, Color.White.copy(alpha = 0.6f), style = Stroke(width = 2.dp.toPx()))

            drawContext.canvas.nativeCanvas.apply {
                val titlePaint = android.graphics.Paint().apply {
                    color = Color.White.copy(alpha = 0.9f).toArgb()
                    textSize = (labelPx * 0.55f).coerceIn(11f, 14f)
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                val subPaint = android.graphics.Paint().apply {
                    color = Color.White.copy(alpha = 0.65f).toArgb()
                    textSize = (labelPx * 0.45f).coerceIn(9f, 12f)
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText(outerLabel, cx, cy - rMax * 0.06f + titlePaint.textSize / 3f, titlePaint)
                drawText(innerLabel, cx, cy + rMax * 0.1f + subPaint.textSize / 3f, subPaint)
            }
        }
    }
}
