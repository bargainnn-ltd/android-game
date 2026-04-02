package com.spicynights.games.ui.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spicynights.games.R
import com.spicynights.games.ui.theme.ModColors
import com.spicynights.games.ui.theme.NeonTokens

@Composable
fun ModScreenBackground(
    brush: Brush,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush),
    ) {
        content()
    }
}

@Composable
fun TruthDareSpeechBubbles(
    isTruth: Boolean,
    isMaleTurn: Boolean,
    modifier: Modifier = Modifier,
) {
    val selectedMale = Color(0xFFFF6F00)
    val selectedFemale = Color(0xFFFF4081)
    val muted = Color(0xFF5D4037).copy(alpha = 0.55f)
    val selectedAccent = if (isMaleTurn) selectedMale else selectedFemale
    val bubbleShape = RoundedCornerShape(12.dp)
    val truthLabel = stringResource(R.string.truth_label)
    val dareLabel = stringResource(R.string.dare_label)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(4.dp)
                .border(2.dp, ModColors.StrokeBlack, bubbleShape)
                .background(
                    if (isTruth) ModColors.BubbleYellow else ModColors.BubbleYellowMuted,
                    bubbleShape,
                )
                .padding(horizontal = 10.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = truthLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                ),
                color = if (isTruth) selectedAccent else muted,
                textAlign = TextAlign.Center,
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(4.dp)
                .border(2.dp, ModColors.StrokeBlack, bubbleShape)
                .background(
                    if (!isTruth) ModColors.BubbleYellow else ModColors.BubbleYellowMuted,
                    bubbleShape,
                )
                .padding(horizontal = 10.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = dareLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                ),
                color = if (!isTruth) selectedAccent else muted,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun ModFooterPillButton(
    text: String,
    isBlack: Boolean,
    leadingIconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String? = null,
) {
    val shape = RoundedCornerShape(percent = 50)
    val bg = if (isBlack) NeonTokens.NeonMagenta else NeonTokens.BgElevated
    val fg = if (isBlack) Color.White else NeonTokens.TextPrimary
    val borderCol = if (isBlack) NeonTokens.NeonMagenta else NeonTokens.NeonCyan.copy(alpha = 0.55f)
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(start = 10.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(color = fg.copy(alpha = 0.18f)),
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick,
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 6.dp)
                    .offset(x = (-2).dp)
                    .clip(shape)
                    .background(bg.copy(alpha = if (enabled) 1f else 0.42f))
                    .border(2.dp, borderCol, shape)
                    .padding(start = 40.dp, end = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                    ),
                    color = fg.copy(alpha = if (enabled) 1f else 0.55f),
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                )
            }
            Image(
                painter = painterResource(leadingIconRes),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(44.dp)
                    .offset(x = (-4).dp),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
fun ModChoicePillButton(
    text: String,
    isBlack: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String? = null,
) {
    val shape = RoundedCornerShape(percent = 50)
    val bg = if (isBlack) NeonTokens.NeonMagenta else NeonTokens.BgElevated
    val fg = if (isBlack) Color.White else NeonTokens.TextPrimary
    val borderCol = if (isBlack) NeonTokens.NeonMagenta else NeonTokens.NeonCyan.copy(alpha = 0.55f)
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(shape)
            .background(bg.copy(alpha = if (enabled) 1f else 0.45f))
            .border(2.dp, borderCol, shape)
            .then(
                if (testTag != null) Modifier.testTag(testTag) else Modifier,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = fg.copy(alpha = 0.2f)),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = fg.copy(alpha = if (enabled) 1f else 0.55f),
        )
    }
}

@Composable
fun CardTiltWrapper(
    tiltDegrees: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.graphicsLayer {
            rotationZ = tiltDegrees
        },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
