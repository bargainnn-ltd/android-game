package com.spicynights.games.ui.dice

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

private val DicePurpleLight = Color(0xFFF8BBFF)
private val DicePurpleMid = Color(0xFFCE93D8)
private val DicePurpleCore = Color(0xFFAB47BC)
private val DicePurpleDeep = Color(0xFF4A148C)
private val GlowPink = Color(0x66FF4081)

private val DieFaceShape = RoundedCornerShape(18.dp)

/**
 * Glossy d6-style die face with glow, 3-axis tumble while [isRolling], face hidden until settled.
 */
@Composable
fun D6CouplesDie(
    categoryLabel: String,
    faceWord: String?,
    rollKey: Int?,
    isRolling: Boolean,
    modifier: Modifier = Modifier,
    dieHeight: Dp = 132.dp,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val pressed by interactionSource.collectIsPressedAsState()
    val showSparkles = hovered || pressed

    val spinTransition = rememberInfiniteTransition(label = "spin")
    val spinAngle by spinTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(260, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "spinAngle",
    )
    val shakeTransition = rememberInfiniteTransition(label = "shake")
    val shake by shakeTransition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(48, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shake",
    )

    val density = LocalDensity.current
    val rad = spinAngle * (Math.PI / 180f).toFloat()
    val rotX = if (isRolling) sin(rad * 2.35f) * 40f + cos(rad * 1.15f) * 14f else 0f
    val rotY = if (isRolling) cos(rad * 1.85f) * 44f + sin(rad * 1.42f) * 18f else 0f
    val rotZ = if (isRolling) spinAngle * 6.2f else 0f
    val offsetXd = if (isRolling) shake else 0f
    val offsetYd = if (isRolling) -shake * 0.65f else 0f

    val pop = remember { Animatable(1f) }
    LaunchedEffect(rollKey, isRolling) {
        if (!isRolling && rollKey != null) {
            pop.snapTo(0.88f)
            pop.animateTo(
                1f,
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            )
        }
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            categoryLabel,
            color = Color.White.copy(alpha = 0.75f),
            style = MaterialTheme.typography.labelSmall,
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .hoverable(interactionSource)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { },
                ),
            contentAlignment = Alignment.Center,
        ) {
            FlirtyPointerOverlay(showSparkles = showSparkles) {
                BoxWithConstraints(
                    modifier = Modifier
                        .height(dieHeight)
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    val side = minOf(maxWidth, dieHeight)
                    Box(
                        modifier = Modifier
                            .size(side)
                            .graphicsLayer {
                                transformOrigin = TransformOrigin(0.5f, 0.5f)
                                cameraDistance = 22f * density.density
                                rotationX = rotX
                                rotationY = rotY
                                rotationZ = rotZ
                                translationX = offsetXd
                                translationY = offsetYd
                                scaleX = pop.value
                                scaleY = pop.value
                            }
                            .drawBehind {
                                val cx = size.width / 2f
                                val cy = size.height / 2f
                                val r = hypot(size.width, size.height) * 0.55f
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(GlowPink, Color.Transparent),
                                        center = Offset(cx, cy),
                                        radius = r,
                                    ),
                                    radius = r,
                                    center = Offset(cx, cy),
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .shadow(10.dp, DieFaceShape, ambientColor = Color.Black.copy(alpha = 0.35f), spotColor = Color.Black.copy(alpha = 0.45f))
                                .clip(DieFaceShape),
                        ) {
                            Canvas(Modifier.fillMaxSize()) {
                                val cr = 18.dp.toPx()
                                drawRoundRect(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            DicePurpleLight,
                                            DicePurpleMid,
                                            DicePurpleCore,
                                            DicePurpleDeep,
                                        ),
                                        start = Offset(0f, 0f),
                                        end = Offset(size.width, size.height),
                                    ),
                                    size = Size(size.width, size.height),
                                    cornerRadius = CornerRadius(cr, cr),
                                )
                                val inset = size.minDimension * 0.1f
                                val innerCr = (cr - inset * 0.5f).coerceAtLeast(4f)
                                drawRoundRect(
                                    color = Color.White.copy(alpha = 0.2f),
                                    topLeft = Offset(inset, inset),
                                    size = Size(size.width - 2f * inset, size.height - 2f * inset),
                                    cornerRadius = CornerRadius(innerCr, innerCr),
                                    style = Stroke(width = 2.dp.toPx()),
                                )
                                drawLine(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.5f),
                                            Color.Transparent,
                                        ),
                                        start = Offset(inset * 1.2f, inset * 1.1f),
                                        end = Offset(size.width * 0.55f, size.height * 0.48f),
                                    ),
                                    start = Offset(inset, inset * 0.9f),
                                    end = Offset(size.width * 0.52f, size.height * 0.45f),
                                    strokeWidth = 3f,
                                )
                            }
                            val display = when {
                                isRolling -> "···"
                                faceWord != null -> faceWord
                                else -> "—"
                            }
                            Text(
                                display,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(horizontal = 10.dp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                            )
                        }
                    }
                }
            }
        }
    }
}
