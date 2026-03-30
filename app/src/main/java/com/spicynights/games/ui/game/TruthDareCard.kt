package com.spicynights.games.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spicynights.games.R
import com.spicynights.games.ui.theme.ModColors

@Composable
fun TruthDareCard(
    prompt: String,
    isTruth: Boolean,
    rotationY: Float,
    glowPulse: Float,
    shakeOffsetX: Float,
    isMaleTurn: Boolean,
    climaxBorderPulse: Boolean = false,
    /** Screen 5: white elevated card, dark text, tag row. */
    lightPromptStyle: Boolean = false,
    intensityLabel: String = "",
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val cameraDistance = 12f * density.density
    val showBack = rotationY <= 90f

    Box(
        modifier = modifier
            .graphicsLayer {
                translationX = shakeOffsetX
                this.cameraDistance = cameraDistance
                this.rotationY = rotationY
                transformOrigin = TransformOrigin(0.5f, 0.5f)
                scaleX = if (rotationY > 90f) -1f else 1f
            },
        contentAlignment = Alignment.Center,
    ) {
        val shape = RoundedCornerShape(28.dp)
        val cardFill = when {
            lightPromptStyle && showBack -> Color(0xFF2A1828)
            lightPromptStyle && !showBack -> Color.White
            else -> ModColors.cardFill(isMaleTurn)
        }
        val borderColor = when {
            lightPromptStyle && !showBack -> Color(0xFFE0D8E8)
            climaxBorderPulse -> {
                lerp(
                    ModColors.StrokeBlack,
                    ModColors.ClimaxGlow,
                    ((glowPulse - 0.85f) / 0.15f).coerceIn(0f, 1f),
                )
            }
            else -> ModColors.StrokeBlack
        }
        val borderW = if (lightPromptStyle && !showBack) 2.dp else 3.dp
        Surface(
            modifier = Modifier
                .fillMaxSize(0.92f)
                .border(borderW, borderColor, shape)
                .clip(shape),
            shadowElevation = 16.dp,
            color = Color.Transparent,
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(cardFill)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                if (showBack) {
                    CardBackPattern(modifier = Modifier.fillMaxSize())
                    Text(
                        text = "♥",
                        style = MaterialTheme.typography.displaySmall,
                        color = Color.White.copy(alpha = 0.25f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center),
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (lightPromptStyle) {
                            PromptTagsRow(
                                isTruth = isTruth,
                                intensityLabel = intensityLabel,
                            )
                            Spacer(Modifier.height(8.dp))
                        } else {
                            TruthDareSpeechBubbles(
                                isTruth = isTruth,
                                isMaleTurn = isMaleTurn,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        val promptColor = if (lightPromptStyle) ModColors.StrokeBlack else Color.White
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = prompt,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    lineHeight = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = promptColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PromptTagsRow(
    isTruth: Boolean,
    intensityLabel: String,
) {
    val truth = stringResource(R.string.truth_label)
    val dare = stringResource(R.string.dare_label)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isTruth) Color(0xFFE8E0FF) else Color(0xFFFFE0ED),
            border = BorderStroke(1.dp, ModColors.StrokeBlack.copy(alpha = 0.12f)),
        ) {
            Text(
                text = if (isTruth) truth else dare,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                color = ModColors.StrokeBlack,
            )
        }
        if (intensityLabel.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFFFF8E1),
                border = BorderStroke(1.dp, ModColors.StrokeBlack.copy(alpha = 0.15f)),
            ) {
                Text(
                    text = intensityLabel,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = ModColors.StrokeBlack,
                )
            }
        }
    }
}

@Composable
private fun CardBackPattern(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawRect(color = Color.Black.copy(alpha = 0.1f), size = size)
        val dot = Color.White.copy(alpha = 0.08f)
        var y = 0f
        while (y < size.height) {
            var x = 0f
            while (x < size.width) {
                drawCircle(dot, radius = 3.dp.toPx(), center = Offset(x, y))
                x += 28.dp.toPx()
            }
            y += 28.dp.toPx()
        }
    }
}
