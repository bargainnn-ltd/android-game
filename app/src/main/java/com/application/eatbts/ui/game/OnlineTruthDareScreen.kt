package com.application.eatbts.ui.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.application.eatbts.R
import com.application.eatbts.data.Level
import com.application.eatbts.ui.theme.themeScreenBackgroundBrush
import com.application.eatbts.viewmodel.CardPhase
import com.application.eatbts.viewmodel.OnlineGameUiState
import com.application.eatbts.viewmodel.OnlineTruthDareViewModel
import com.application.eatbts.viewmodel.TruthDareChoice
import kotlinx.coroutines.launch

@Composable
fun OnlineTruthDareScreen(
    viewModel: OnlineTruthDareViewModel,
    onFlipSound: () -> Unit,
    onClickSound: () -> Unit,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(ui.phase) {
        when (ui.phase) {
            CardPhase.Flipping -> {
                rotation.snapTo(0f)
                onFlipSound()
                rotation.animateTo(90f, tween(220, easing = LinearEasing))
                rotation.animateTo(180f, tween(220, easing = LinearEasing))
            }
            CardPhase.FaceDown -> {
                if (rotation.value > 0f) {
                    rotation.animateTo(0f, tween(280, easing = FastOutSlowInEasing))
                }
            }
            CardPhase.ShowingTruth, CardPhase.ShowingDare -> {
                if (rotation.value < 180f) rotation.snapTo(180f)
            }
            CardPhase.DeckEmpty -> rotation.snapTo(0f)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeScreenBackgroundBrush()),
    ) {
        OnlineGameTopBar(onBack = { onClickSound(); onNavigateHome() })
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (ui.loading) {
                Text(stringResource(R.string.online_loading_match), color = MaterialTheme.colorScheme.onBackground)
            } else {
            ui.error?.let { err ->
                Text(err, color = MaterialTheme.colorScheme.error)
            }
            OnlineTurnBanner(ui = ui)
            if (ui.phase == CardPhase.FaceDown && ui.isMyTurn) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = {
                            onClickSound()
                            viewModel.setTruthDareChoice(TruthDareChoice.TRUTH)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = ui.truthsRemaining > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3F6E)),
                    ) { Text(stringResource(R.string.choice_truth)) }
                    Button(
                        onClick = {
                            onClickSound()
                            viewModel.setTruthDareChoice(TruthDareChoice.DARE)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = ui.daresRemaining > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D1A3A)),
                    ) { Text(stringResource(R.string.choice_dare)) }
                }
            }
            Text(
                stringResource(R.string.progress_format, ui.cardsRevealed.coerceAtLeast(0), ui.totalInSession.coerceAtLeast(0)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            when (ui.phase) {
                CardPhase.DeckEmpty -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.deck_empty),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                else -> {
                    val displayIsTruth = when (ui.phase) {
                        CardPhase.ShowingTruth -> true
                        CardPhase.ShowingDare -> false
                        CardPhase.Flipping -> ui.pendingRevealIsTruth
                        else -> true
                    }
                    val intensityLabel = when (ui.level) {
                        Level.MILD -> stringResource(R.string.intensity_mild)
                        Level.SPICY -> stringResource(R.string.intensity_spicy)
                        Level.EXTREME -> stringResource(R.string.intensity_extreme)
                    }
                    val idx = ui.currentPlayerIndex.coerceIn(0, (ui.playerNames.size - 1).coerceAtLeast(0))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    ) {
                        TruthDareCard(
                            prompt = ui.currentPrompt,
                            isTruth = displayIsTruth,
                            rotationY = rotation.value,
                            glowPulse = 1f,
                            shakeOffsetX = 0f,
                            isMaleTurn = idx % 2 == 0,
                            extremeBorderPulse = ui.level == Level.EXTREME,
                            lightPromptStyle = true,
                            intensityLabel = intensityLabel,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
            if (ui.phase == CardPhase.ShowingTruth || ui.phase == CardPhase.ShowingDare) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val idx = ui.currentPlayerIndex.coerceIn(0, (ui.playerNames.size - 1).coerceAtLeast(0))
                    val skips = ui.skipsRemaining.getOrElse(idx) { 0 }
                    Button(
                        onClick = {
                            onClickSound()
                            viewModel.onSkip()
                        },
                        enabled = ui.isMyTurn && (!ui.penaltyEnabled || skips > 0),
                        modifier = Modifier.weight(1f),
                    ) { Text(stringResource(R.string.skip)) }
                    Button(
                        onClick = {
                            onClickSound()
                            scope.launch { viewModel.onNext() }
                        },
                        enabled = ui.isMyTurn,
                        modifier = Modifier.weight(1f),
                    ) { Text(stringResource(R.string.next_card)) }
                }
            }
            Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun OnlineGameTopBar(onBack: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(color = scheme.surface, shadowElevation = 6.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = scheme.onSurface)
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_couple_games_logo),
                    contentDescription = stringResource(R.string.cd_app_logo),
                    modifier = Modifier
                        .height(28.dp)
                        .padding(end = 8.dp),
                )
                Text(
                    stringResource(R.string.online_game_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                )
            }
            Spacer(Modifier.width(48.dp))
        }
    }
}

@Composable
private fun OnlineTurnBanner(ui: OnlineGameUiState) {
    val label = when {
        ui.matchCompleted -> stringResource(R.string.online_match_completed)
        !ui.isMyTurn -> stringResource(R.string.online_waiting_opponent)
        ui.phase == CardPhase.FaceDown -> stringResource(R.string.online_your_turn_pick)
        else -> stringResource(R.string.online_your_turn_reveal)
    }
    Text(
        label,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                RoundedCornerShape(12.dp),
            )
            .padding(12.dp),
        textAlign = TextAlign.Center,
    )
}
