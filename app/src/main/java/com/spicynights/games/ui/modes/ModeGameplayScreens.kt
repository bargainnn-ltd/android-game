package com.spicynights.games.ui.modes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spicynights.games.data.local.AppPreferencesRepository
import com.spicynights.games.R
import com.spicynights.games.data.DataManager
import com.spicynights.games.session.SessionStateHolder
import com.spicynights.games.viewmodel.SpicySpinnerGameplayViewModel
import com.spicynights.games.viewmodel.NeverGameplayViewModel
import com.spicynights.games.ui.dice.CouplesDiceBackground
import com.spicynights.games.ui.dice.CouplesDualRingSpinner
import com.spicynights.games.ui.dice.DiceSoundEffects
import com.spicynights.games.ui.dice.SpicySpinnerSpinTotalMs
import com.spicynights.games.ui.dice.FlirtyPointerOverlay
import com.spicynights.games.viewmodel.CouplesDiceRules
import com.spicynights.games.viewmodel.WyrGameplayViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val NeverAccent = Color(0xFF4DD0E1)
private val DicePink = Color(0xFFFF2E95)
private val CardBg = Color(0xFF2C2C35)

private val WyrScreenBg = Color(0xFF1A1A1A)
private val WyrOptionBlue = Color(0xFF0099FF)
private val WyrOptionRed = Color(0xFFE62E2E)
private val WyrCardRadius = 28.dp
private val WyrOrBadgeSize = 56.dp

@Composable
fun NeverGameplayScreen() {
    val context = LocalContext.current.applicationContext
    val snapshot = remember {
        SessionStateHolder.pending.also { SessionStateHolder.pending = null }
    }
    val dataManager = remember { DataManager(context) }
    val vm: NeverGameplayViewModel = viewModel(
        factory = NeverGameplayViewModel.factory(snapshot, dataManager),
    )
    val state by vm.state.collectAsStateWithLifecycle()

    val bg = Brush.verticalGradient(listOf(Color(0xFF121218), Color(0xFF0A0A10)))
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            stringResource(R.string.never_game_title),
            style = MaterialTheme.typography.titleLarge,
            color = NeverAccent,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Surface(shape = RoundedCornerShape(24.dp), color = Color(0xFF2A2A32)) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("👤", modifier = Modifier.padding(end = 8.dp))
                Column {
                    Text(
                        stringResource(R.string.never_reads_prompt, state.currentReaderName),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                    )
                    Text(
                        state.currentReaderName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(state.intensityLine, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(16.dp))
        state.error?.let {
            Text(it, color = Color(0xFFFF5252), style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
        }
        Surface(shape = RoundedCornerShape(16.dp), color = CardBg, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    stringResource(R.string.never_have_prefix).uppercase(),
                    color = NeverAccent,
                    style = MaterialTheme.typography.labelMedium,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    state.currentPrompt ?: stringResource(R.string.never_deck_empty),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
                if (state.drinkingRulesOn && state.currentPrompt != null) {
                    Spacer(Modifier.height(12.dp))
                    Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFF1E2A30)) {
                        Text(
                            "🥂  ${stringResource(R.string.never_sip_hint)}",
                            modifier = Modifier.padding(12.dp),
                            color = NeverAccent,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.house_rules_game_title),
            color = Color.Gray,
            style = MaterialTheme.typography.labelSmall,
        )
        state.playerNames.forEachIndexed { index, name ->
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(name, color = Color.White, modifier = Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val sel = state.playerAnswers.getOrNull(index)
                    FilterChipStyle(
                        label = stringResource(R.string.never_i_have),
                        selected = sel == true,
                        onClick = { vm.setPlayerAnswer(index, true) },
                    )
                    FilterChipStyle(
                        label = stringResource(R.string.never_never),
                        selected = sel == false,
                        onClick = { vm.setPlayerAnswer(index, false) },
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { vm.nextPrompt() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
            enabled = state.currentPrompt != null,
        ) {
            Text(stringResource(R.string.never_next_prompt))
        }
    }
}

@Composable
private fun FilterChipStyle(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) NeverAccent.copy(alpha = 0.35f) else Color(0xFF3A3A44),
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = if (selected) Color.White else Color.White.copy(alpha = 0.85f),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
fun SpicySpinnerGameplayScreen(
    prefs: AppPreferencesRepository,
    onBack: () -> Unit = {},
) {
    val snapshot = remember {
        SessionStateHolder.pending.also { SessionStateHolder.pending = null }
    }
    val vm: SpicySpinnerGameplayViewModel = viewModel(
        factory = SpicySpinnerGameplayViewModel.factory(snapshot),
    )
    val state by vm.state.collectAsStateWithLifecycle()
    val soundOn by prefs.soundEffectsEnabled.collectAsStateWithLifecycle(initialValue = true)
    val context = LocalContext.current
    val diceSound = remember { DiceSoundEffects(context) }
    DisposableEffect(Unit) {
        onDispose { diceSound.release() }
    }
    var isRolling by remember { mutableStateOf(false) }
    var spinGeneration by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val rollInteraction = remember { MutableInteractionSource() }
    val rollHovered by rollInteraction.collectIsHoveredAsState()
    val rollPressed by rollInteraction.collectIsPressedAsState()

    fun runRollAnimation(rollAction: () -> Unit) {
        scope.launch {
            isRolling = true
            if (soundOn) diceSound.playRoll()
            rollAction()
            spinGeneration++
            delay(SpicySpinnerSpinTotalMs.toLong())
            isRolling = false
            if (soundOn) diceSound.playDing()
        }
    }

    val bodies = stringArrayResource(R.array.dice_body_parts)
    val actions = stringArrayResource(R.array.dice_actions)
    val rules = stringArrayResource(R.array.dice_rules)

    var rulesExpanded by remember { mutableStateOf(false) }
    var timerLeft by remember { mutableIntStateOf(state.actionTimerSeconds) }

    LaunchedEffect(state.bodyRoll, state.actionRoll, state.turnIndex, state.turnTimerEnabled) {
        if (state.turnTimerEnabled && state.bodyRoll != null && state.actionTimerSeconds > 0) {
            timerLeft = state.actionTimerSeconds
            while (timerLeft > 0) {
                delay(1000)
                timerLeft--
            }
        }
    }

    val names = state.playerNames
    val currentName = names.getOrElse(state.turnIndex % names.size.coerceAtLeast(1)) { "?" }

    val bodyLabel = state.bodyRoll?.let { bodies.getOrNull(it - 1) }
    val actionLabel = state.actionRoll?.let { actions.getOrNull(it - 1) }

    Box(Modifier.fillMaxSize()) {
        CouplesDiceBackground(Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
        Text(
            stringResource(R.string.game_spicy_spinner),
            color = DicePink,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            stringResource(R.string.dice_couples_subtitle),
            color = Color.Gray,
            style = MaterialTheme.typography.labelSmall,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            if (currentName.equals("You", ignoreCase = true)) {
                stringResource(R.string.dice_your_turn)
            } else {
                stringResource(R.string.dice_named_turn, currentName)
            },
            color = Color.White,
            fontWeight = FontWeight.Medium,
        )
        Text(
            stringResource(R.string.dice_turns_fmt, state.turnsCompleted, state.maxTurns),
            color = Color.Gray,
            style = MaterialTheme.typography.labelSmall,
        )
        Text(state.intensityLine, color = Color.Gray, style = MaterialTheme.typography.labelSmall)
        if (state.turnTimerEnabled && state.bodyRoll != null && state.actionTimerSeconds > 0) {
            Text(
                stringResource(R.string.dice_timer_fmt, timerLeft),
                color = Color(0xFFFF9800),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            CouplesDualRingSpinner(
                bodyRoll = state.bodyRoll,
                actionRoll = state.actionRoll,
                isRolling = isRolling,
                animationKey = spinGeneration,
                outerLabel = stringResource(R.string.dice_body_die),
                innerLabel = stringResource(R.string.dice_action_die),
                outerLabels = bodies,
                innerLabels = actions,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (!isRolling && state.bodyRoll != null && state.actionRoll != null) {
            Text(
                stringResource(
                    R.string.dice_result_pair_words_fmt,
                    bodyLabel ?: "—",
                    actionLabel ?: "—",
                ),
                color = DicePink,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Spacer(Modifier.height(6.dp))
        FlirtyPointerOverlay(
            showSparkles = rollHovered || rollPressed,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Button(
                onClick = { runRollAnimation { vm.rollDice() } },
                enabled = !state.sessionComplete && !isRolling,
                modifier = Modifier
                    .fillMaxWidth()
                    .hoverable(rollInteraction),
                interactionSource = rollInteraction,
                colors = ButtonDefaults.buttonColors(containerColor = DicePink),
            ) {
                Text(stringResource(R.string.dice_spin))
            }
        }
        Text(
            stringResource(R.string.dice_tap_to_spin),
            color = Color.Gray,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 2.dp),
        )
        Spacer(Modifier.height(8.dp))

        Surface(shape = RoundedCornerShape(16.dp), color = CardBg, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                when {
                    isRolling -> {
                        Text(
                            "…",
                            color = Color.Gray,
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }
                    state.sessionComplete -> {
                        Text(
                            stringResource(R.string.dice_session_complete),
                            color = Color(0xFF81C784),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            stringResource(R.string.dice_session_complete_body, CouplesDiceRules.TURNS_PER_PLAYER),
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    state.isDoubleRoll && !state.freeChoiceActive -> {
                        Text(
                            stringResource(R.string.dice_double_roll_title),
                            color = Color(0xFFFF9800),
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            stringResource(R.string.dice_double_roll_body),
                            color = Color.White,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        Button(
                            onClick = { vm.confirmFreeChoice() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DicePink),
                        ) {
                            Text(stringResource(R.string.dice_free_choice_confirm))
                        }
                    }
                    state.isDoubleRoll && state.freeChoiceActive -> {
                        Text(
                            stringResource(R.string.dice_free_choice_hint),
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    state.bodyRoll != null && state.actionRoll != null && bodyLabel != null && actionLabel != null -> {
                        Text(
                            stringResource(R.string.dice_instruction_natural, actionLabel, bodyLabel.lowercase()),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    else -> {
                        Text(
                            "—",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
                Text(
                    stringResource(R.string.dice_consent_line),
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        Spacer(Modifier.height(6.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { rulesExpanded = !rulesExpanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.dice_rules_title),
                color = Color.White,
                fontWeight = FontWeight.Medium,
            )
            Text(
                if (rulesExpanded) "▲" else "▼",
                color = Color.Gray,
            )
        }
        if (rulesExpanded) {
            rules.forEach { line ->
                Text(
                    "• $line",
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        if (state.sessionComplete) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { vm.resetSession() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                ) {
                    Text(stringResource(R.string.dice_play_again))
                }
                Button(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                ) {
                    Text(stringResource(R.string.dice_back))
                }
            }
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { runRollAnimation { vm.reRoll() } },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                    enabled = state.sessionReRollsRemaining > 0 && state.bodyRoll != null && !isRolling,
                ) {
                    Text(stringResource(R.string.dice_reroll_fmt, state.sessionReRollsRemaining))
                }
                Button(
                    onClick = { vm.nextPlayer() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = DicePink),
                    enabled = state.bodyRoll != null && !isRolling,
                ) {
                    Text(stringResource(R.string.next_player))
                }
            }
        }
        }
    }
}

@Composable
fun WyrGameplayScreen() {
    val context = LocalContext.current.applicationContext
    val snapshot = remember {
        SessionStateHolder.pending.also { SessionStateHolder.pending = null }
    }
    val dataManager = remember { DataManager(context) }
    val vm: WyrGameplayViewModel = viewModel(
        factory = WyrGameplayViewModel.factory(snapshot, dataManager),
    )
    val state by vm.state.collectAsStateWithLifecycle()

    var timerLeft by remember { mutableIntStateOf(state.timerSecondsTotal) }
    val cardVisible = state.optionA != null && state.optionB != null

    LaunchedEffect(state.optionA, state.optionB, state.debateTimerEnabled, state.timerSecondsTotal) {
        if (cardVisible && state.debateTimerEnabled) {
            timerLeft = state.timerSecondsTotal
            while (timerLeft > 0) {
                delay(1000)
                timerLeft--
            }
        }
    }

    val topShape = RoundedCornerShape(topStart = WyrCardRadius, topEnd = WyrCardRadius)
    val bottomShape = RoundedCornerShape(bottomStart = WyrCardRadius, bottomEnd = WyrCardRadius)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WyrScreenBg)
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(28.dp))
        Text(
            stringResource(R.string.game_wyr),
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(R.string.wyr_spicy_deck),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            color = Color.White.copy(alpha = 0.45f),
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        state.error?.let {
            Text(
                it,
                color = Color(0xFFFF5252),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .heightIn(min = 280.dp),
        ) {
            Column(Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(topShape)
                        .background(WyrOptionBlue)
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.optionA ?: stringResource(R.string.wyr_deck_empty),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(bottomShape)
                        .background(WyrOptionRed)
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.optionB ?: "—",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(WyrOrBadgeSize)
                    .clip(CircleShape)
                    .background(WyrScreenBg),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.wyr_or),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        if (state.debateTimerEnabled && cardVisible) {
            Text(
                stringResource(R.string.wyr_timer_fmt, timerLeft),
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFFFB74D),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                stringResource(R.string.debate_timer),
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyMedium,
            )
            Switch(
                checked = state.debateTimerEnabled,
                onCheckedChange = { vm.setDebateTimerEnabled(it) },
            )
        }
        Text(
            stringResource(R.string.wyr_next_card) + " · ${state.cardsRemaining}",
            color = Color.White.copy(alpha = 0.45f),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 12.dp),
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = { vm.nextCard() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black,
            ),
        ) {
            Text(stringResource(R.string.wyr_next_card), fontWeight = FontWeight.Bold)
        }
    }
}
