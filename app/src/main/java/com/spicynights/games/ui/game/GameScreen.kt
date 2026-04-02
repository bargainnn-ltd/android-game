package com.spicynights.games.ui.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.ripple
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spicynights.games.R
import com.spicynights.games.data.Level
import com.spicynights.games.ui.theme.ModColors
import com.spicynights.games.ui.theme.NeonTokens
import com.spicynights.games.viewmodel.CardPhase
import com.spicynights.games.viewmodel.GameViewModel
import com.spicynights.games.viewmodel.TruthDareChoice
import kotlinx.coroutines.launch

private val MaleIconColor = Color(0xFF64B5F6)
private val MaleBgColor = Color(0xFF1A3F6E)
private val FemaleIconColor = Color(0xFFFF80AB)
private val FemaleBgColor = Color(0xFF5D1A3A)

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onFlipSound: () -> Unit,
    onClickSound: () -> Unit,
    onHapticLight: () -> Unit,
    onHapticStrong: () -> Unit,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val rotation = remember { Animatable(0f) }
    val shake = remember { Animatable(0f) }
    val rotValue by produceState(initialValue = 0f, rotation) {
        snapshotFlow { rotation.value }.collect { value = it }
    }
    val shakeX by produceState(initialValue = 0f, shake) {
        snapshotFlow { shake.value }.collect { value = it }
    }
    val scope = rememberCoroutineScope()
    var houseRulesExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(ui.phase) {
        when (ui.phase) {
            CardPhase.Flipping -> {
                rotation.snapTo(0f)
                onFlipSound()
                // Constant angular velocity across 90° avoids a velocity mismatch from FastOutSlowIn on both halves.
                val halfFlipSpec = tween<Float>(durationMillis = 220, easing = LinearEasing)
                rotation.animateTo(90f, halfFlipSpec)
                viewModel.onFlipMidpoint()
                val snap = viewModel.uiState.value
                if (snap.phase == CardPhase.DeckEmpty) {
                    rotation.animateTo(0f, tween(200, easing = FastOutSlowInEasing))
                    return@LaunchedEffect
                }
                val extremeDare = snap.level == Level.EXTREME && !snap.pendingRevealIsTruth
                if (extremeDare) onHapticStrong() else onHapticLight()
                rotation.animateTo(180f, halfFlipSpec)
                viewModel.onFlipComplete()
                shake.snapTo(0f)
                shake.animateTo(8f, tween(40))
                shake.animateTo(-6f, tween(40))
                shake.animateTo(0f, tween(60))
            }

            CardPhase.FaceDown -> {
                if (rotation.value > 0f) {
                    rotation.animateTo(0f, tween(280, easing = FastOutSlowInEasing))
                }
            }

            CardPhase.ShowingTruth, CardPhase.ShowingDare -> {
                if (rotation.value < 180f) rotation.snapTo(180f)
            }

            CardPhase.DeckEmpty -> {
                rotation.snapTo(0f)
            }
        }
    }

    val infinite = rememberInfiniteTransition(label = "glow")
    val pulse by infinite.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    val idx = ui.currentPlayerIndex.coerceIn(0, (ui.playerNames.size - 1).coerceAtLeast(0))
    val currentPlayerLabel = ui.playerNames.getOrElse(idx) { "" }.ifBlank {
        stringResource(R.string.player_default_n, idx + 1)
    }
    val isAlternateStyle = idx % 2 == 0
    val intensityLabel = when (ui.level) {
        Level.MILD -> stringResource(R.string.intensity_mild)
        Level.SPICY -> stringResource(R.string.intensity_spicy)
        Level.EXTREME -> stringResource(R.string.intensity_extreme)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NeonTokens.screenBackgroundBrush())
            .testTag("game_screen"),
    ) {
        GameTopBar(
            title = stringResource(R.string.game_screen_title),
            onBack = {
                onClickSound()
                onNavigateHome()
            },
            onBookmark = { onClickSound() },
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Spacer(Modifier.height(2.dp))
            TruthDareChoicePanel(
                phase = ui.phase,
                pendingRevealIsTruth = ui.pendingRevealIsTruth,
                truthsRemaining = ui.truthsRemaining,
                daresRemaining = ui.daresRemaining,
                onTruth = {
                    onClickSound()
                    viewModel.setTruthDareChoice(TruthDareChoice.TRUTH)
                },
                onDare = {
                    onClickSound()
                    viewModel.setTruthDareChoice(TruthDareChoice.DARE)
                },
            )

            if (ui.phase == CardPhase.FaceDown) {
                ChooseYourFateLine(currentPlayerLabel = currentPlayerLabel)
            }

            PlayerTimerRow(
                currentPlayerLabel = currentPlayerLabel,
                isMaleTurn = isAlternateStyle,
                turnTimerSeconds = ui.turnTimerSeconds,
                timerRemainingSeconds = ui.timerRemainingSeconds,
                phase = ui.phase,
            )

            Text(
                text = stringResource(
                    R.string.progress_format,
                    ui.cardsRevealed.coerceAtLeast(0),
                    ui.totalInSession.coerceAtLeast(0),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = NeonTokens.TextMuted,
                modifier = Modifier.testTag("progress_label"),
            )

            when (ui.phase) {
                CardPhase.DeckEmpty -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.deck_empty),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = NeonTokens.TextPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                                .testTag("deck_empty"),
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
                    val isMale = isAlternateStyle

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    ) {
                        CardTiltWrapper(tiltDegrees = -2.5f, modifier = Modifier.fillMaxSize()) {
                            TruthDareCard(
                                prompt = ui.currentPrompt,
                                isTruth = displayIsTruth,
                                rotationY = rotValue,
                                glowPulse = if (ui.level == Level.EXTREME) pulse else 1f,
                                shakeOffsetX = shakeX,
                                isMaleTurn = isMale,
                                extremeBorderPulse = ui.level == Level.EXTREME,
                                lightPromptStyle = true,
                                intensityLabel = intensityLabel,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("truth_dare_card"),
                            )
                        }
                    }
                }
            }

            PenaltyRow(
                checked = ui.penaltyEnabled,
                onCheckedChange = { viewModel.setPenaltyEnabled(it) },
            )

            HouseRulesAccordion(
                expanded = houseRulesExpanded,
                onExpandedChange = { houseRulesExpanded = it },
            )

            if (ui.phase == CardPhase.ShowingTruth || ui.phase == CardPhase.ShowingDare) {
                GameplayActions(
                    skipsLeft = ui.skipsRemaining.getOrElse(idx) { 0 },
                    penaltyEnabled = ui.penaltyEnabled,
                    onSkip = {
                        onClickSound()
                        viewModel.onSkip()
                    },
                    onCompleted = {
                        onClickSound()
                        scope.launch { viewModel.onNext() }
                    },
                    onNextPlayer = {
                        onClickSound()
                        scope.launch { viewModel.onNext() }
                    },
                )
            }
        }
    }
}

@Composable
private fun GameTopBar(
    title: String,
    onBack: () -> Unit,
    onBookmark: () -> Unit,
) {
    Surface(
        color = NeonTokens.BgDeep,
        shadowElevation = 6.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("home_btn")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_home),
                    tint = Color.White,
                )
            }
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_spicy_night_logo),
                    contentDescription = stringResource(R.string.cd_app_logo),
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
            }
            IconButton(onClick = onBookmark, modifier = Modifier.testTag("bookmark_prompt_btn")) {
                Icon(
                    imageVector = Icons.Filled.BookmarkBorder,
                    contentDescription = stringResource(R.string.cd_bookmark_prompt),
                    tint = Color.White.copy(alpha = 0.9f),
                )
            }
        }
    }
}

@Composable
private fun TruthDareChoicePanel(
    phase: CardPhase,
    pendingRevealIsTruth: Boolean,
    truthsRemaining: Int,
    daresRemaining: Int,
    onTruth: () -> Unit,
    onDare: () -> Unit,
) {
    val faceDown = phase == CardPhase.FaceDown
    if (!faceDown) {
        return
    }
    val selectedTruth = when (phase) {
        CardPhase.ShowingTruth -> true
        CardPhase.ShowingDare -> false
        CardPhase.Flipping -> pendingRevealIsTruth
        else -> null
    }
    val truthLabel = stringResource(R.string.truth_label)
    val dareLabel = stringResource(R.string.dare_label)
    val bracketTruth = "[$truthLabel]"
    val bracketDare = "[$dareLabel]"
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            TruthDareHeroCard(
                accent = NeonTokens.NeonCyan,
                bracketLabel = bracketTruth,
                selected = selectedTruth == true,
                enabled = truthsRemaining > 0,
                onClick = onTruth,
                icon = {
                    Icon(
                        Icons.AutoMirrored.Outlined.HelpOutline,
                        contentDescription = null,
                        tint = NeonTokens.NeonCyan.copy(alpha = if (truthsRemaining > 0) 1f else 0.35f),
                        modifier = Modifier.size(40.dp),
                    )
                },
                modifier = Modifier.weight(1f).testTag("mode_truth"),
            )
            Text(
                text = stringResource(R.string.wyr_or),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = NeonTokens.TextPrimary,
            )
            TruthDareHeroCard(
                accent = NeonTokens.NeonMagenta,
                bracketLabel = bracketDare,
                selected = selectedTruth == false,
                enabled = daresRemaining > 0,
                onClick = onDare,
                icon = {
                    Icon(
                        Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        tint = NeonTokens.NeonMagenta.copy(alpha = if (daresRemaining > 0) 1f else 0.35f),
                        modifier = Modifier.size(40.dp),
                    )
                },
                modifier = Modifier.weight(1f).testTag("mode_dare"),
            )
        }
    }
}

@Composable
private fun TruthDareHeroCard(
    accent: Color,
    bracketLabel: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val brush = NeonTokens.glassBorderBrush(accent.copy(alpha = if (selected) 0.95f else 0.5f))
    val bg = when {
        !enabled -> NeonTokens.BgVoid.copy(alpha = 0.35f)
        selected -> accent.copy(alpha = 0.2f)
        else -> NeonTokens.BgElevated.copy(alpha = 0.75f)
    }
    Column(
        modifier = modifier
            .heightIn(min = 132.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, brush, RoundedCornerShape(16.dp))
            .background(bg)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = accent.copy(alpha = 0.25f)),
                onClick = onClick,
            )
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = bracketLabel,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = accent.copy(alpha = if (enabled) 1f else 0.4f),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            icon()
        }
        Text(
            text = bracketLabel,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = accent.copy(alpha = if (enabled) 1f else 0.4f),
        )
    }
}

@Composable
private fun ChooseYourFateLine(currentPlayerLabel: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("choose_fate_line"),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.game_choose_fate_prefix),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = NeonTokens.TextPrimary,
        )
        Text(
            text = "[$currentPlayerLabel]",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = NeonTokens.NeonCyan,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PlayerTimerRow(
    currentPlayerLabel: String,
    isMaleTurn: Boolean,
    turnTimerSeconds: Int,
    timerRemainingSeconds: Int?,
    phase: CardPhase,
) {
    val active = phase == CardPhase.ShowingTruth || phase == CardPhase.ShowingDare
    val total = turnTimerSeconds
    val remaining = timerRemainingSeconds ?: if (total > 0) total else 0
    val progress = if (active && timerRemainingSeconds != null && total > 0) {
        (remaining.coerceAtLeast(0).toFloat() / total.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val bg = if (isMaleTurn) MaleBgColor else FemaleBgColor
    val fg = if (isMaleTurn) MaleIconColor else FemaleIconColor

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = bg,
            modifier = Modifier.size(36.dp),
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = if (isMaleTurn) "♂" else "♀",
                    style = MaterialTheme.typography.titleMedium,
                    color = fg,
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.turn_line, currentPlayerLabel),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = NeonTokens.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("current_player_label"),
            )
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = NeonTokens.NeonMagenta,
                trackColor = NeonTokens.BgElevated.copy(alpha = 0.65f),
            )
            Text(
                text = if (total <= 0) {
                    stringResource(R.string.timer_disabled_hint)
                } else {
                    stringResource(R.string.timer_remaining_fmt, remaining.coerceAtLeast(0), total)
                },
                style = MaterialTheme.typography.labelSmall,
                color = NeonTokens.TextDim,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun PenaltyRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.penalty_row_label),
            style = MaterialTheme.typography.bodyMedium,
            color = NeonTokens.TextMuted,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = NeonTokens.NeonMagenta,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = NeonTokens.BgElevated.copy(alpha = 0.8f),
            ),
            modifier = Modifier.testTag("penalty_switch"),
        )
    }
}

@Composable
private fun HouseRulesAccordion(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, NeonTokens.GlassBorderStrong, RoundedCornerShape(12.dp))
            .background(NeonTokens.BgElevated)
            .testTag("house_rules"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandedChange(!expanded) }
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.house_rules_game_title),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = NeonTokens.TextPrimary,
            )
            Text(
                text = if (expanded) "▲" else "▼",
                color = NeonTokens.TextDim,
            )
        }
        if (expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 120.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = stringResource(R.string.house_rules_game_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = NeonTokens.TextMuted,
                    modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun GameplayActions(
    skipsLeft: Int,
    penaltyEnabled: Boolean,
    onSkip: () -> Unit,
    onCompleted: () -> Unit,
    onNextPlayer: () -> Unit,
) {
    val skipEnabled = if (penaltyEnabled) skipsLeft > 0 else true
    val chickenLabel = if (penaltyEnabled) {
        stringResource(
            R.string.skip_count_format,
            stringResource(R.string.action_chicken_out),
            skipsLeft,
        )
    } else {
        stringResource(R.string.action_chicken_out)
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ModWhiteOutlinePillButton(
                text = chickenLabel,
                onClick = onSkip,
                modifier = Modifier.weight(1f).testTag("skip_btn"),
                enabled = skipEnabled,
            )
            ModDoneGreenPillButton(
                text = stringResource(R.string.action_done),
                onClick = onCompleted,
                modifier = Modifier.weight(1f).testTag("completed_btn"),
            )
        }
        ModFooterPillButton(
            text = stringResource(R.string.next_player),
            isBlack = true,
            leadingIconRes = R.drawable.mod_icon_rainbow_cloud,
            onClick = onNextPlayer,
            testTag = "next_btn",
        )
    }
}
