package com.spicynights.games.ui.modes

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spicynights.games.data.local.AppPreferencesRepository
import com.spicynights.games.R
import com.spicynights.games.data.DataManager
import com.spicynights.games.session.SessionStateHolder
import com.spicynights.games.viewmodel.SpicySpinnerGameplayViewModel
import com.spicynights.games.viewmodel.NeverGameplayViewModel
import com.spicynights.games.ui.dice.CouplesDualRingSpinner
import com.spicynights.games.ui.dice.DiceSoundEffects
import com.spicynights.games.ui.dice.SpicySpinnerSpinTotalMs
import com.spicynights.games.ui.dice.FlirtyPointerOverlay
import com.spicynights.games.ui.sound.FlipSoundEffects
import com.spicynights.games.viewmodel.CouplesDiceRules
import com.spicynights.games.viewmodel.WyrGameplayViewModel
import com.spicynights.games.ui.hub.HubLandingColors
import com.spicynights.games.ui.theme.NeonTokens
import com.spicynights.games.viewmodel.SpicySpinLogEntry
import kotlin.math.absoluteValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val WyrCardRadius = 28.dp
private val WyrOrBadgeSize = 56.dp

@Composable
fun NeverGameplayScreen(
    prefs: AppPreferencesRepository,
    onOpenMenu: () -> Unit = {},
) {
    val context = LocalContext.current.applicationContext
    val snapshot = remember {
        SessionStateHolder.pending.also { SessionStateHolder.pending = null }
    }
    val dataManager = remember { DataManager(context) }
    val vm: NeverGameplayViewModel = viewModel(
        factory = NeverGameplayViewModel.factory(snapshot, dataManager),
    )
    val state by vm.state.collectAsStateWithLifecycle()

    val flipSound = remember { FlipSoundEffects(context) }
    DisposableEffect(Unit) {
        onDispose { flipSound.release() }
    }
    val soundOn by prefs.soundEffectsEnabled.collectAsStateWithLifecycle(initialValue = true)

    var timerLeft by remember { mutableIntStateOf(state.turnTimerSecondsTotal) }
    LaunchedEffect(state.currentPrompt, state.turnIndex, state.turnTimerEnabled, state.turnTimerSecondsTotal) {
        if (state.turnTimerEnabled && state.currentPrompt != null && state.turnTimerSecondsTotal > 0) {
            timerLeft = state.turnTimerSecondsTotal
            while (timerLeft > 0) {
                delay(1000)
                timerLeft--
            }
        }
    }

    val bg = Brush.verticalGradient(
        listOf(HubLandingColors.Black, HubLandingColors.Charcoal, HubLandingColors.Black),
    )
    val roundNum =
        when {
            state.totalPrompts == 0 -> 0
            state.currentPrompt == null && state.deckRemaining == 0 -> state.totalPrompts
            else -> (state.totalPrompts - state.deckRemaining).coerceAtLeast(1)
        }
    val progress =
        if (state.totalPrompts > 0) {
            (state.totalPrompts - state.deckRemaining).toFloat() / state.totalPrompts.toFloat()
        } else {
            0f
        }

    Box(Modifier.fillMaxSize().background(bg)) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            GameplayHubTopBar(onOpenMenu = onOpenMenu)
            Text(
                stringResource(R.string.spicy_current_player),
                style = MaterialTheme.typography.labelMedium,
                color = HubLandingColors.BrandPurple.copy(alpha = 0.85f),
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
            )
            Text(
                state.currentReaderName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = HubLandingColors.White,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.never_round_fmt, roundNum, state.totalPrompts.coerceAtLeast(1)),
                    style = MaterialTheme.typography.labelLarge,
                    color = HubLandingColors.BodyGrey,
                    fontWeight = FontWeight.Bold,
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        stringResource(R.string.never_mood_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = HubLandingColors.TextDim,
                        modifier = Modifier.padding(end = 6.dp),
                    )
                    Text(
                        state.moodLabel,
                        style = MaterialTheme.typography.titleSmall,
                        fontStyle = FontStyle.Italic,
                        color = HubLandingColors.HighStakesRed.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = HubLandingColors.BrandPurple,
                trackColor = HubLandingColors.SurfaceElevated,
            )
            if (state.turnTimerEnabled && state.currentPrompt != null && state.turnTimerSecondsTotal > 0) {
                Text(
                    stringResource(R.string.never_turn_timer_fmt, timerLeft),
                    color = HubLandingColors.DeckGold,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            state.error?.let {
                Text(it, color = Color(0xFFFF5252), style = MaterialTheme.typography.bodySmall)
            }
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(HubLandingColors.BrandPurple.copy(alpha = 0.18f), Color.Transparent),
                                    radius = 420f,
                                ),
                            ),
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(28.dp),
                        color = HubLandingColors.Surface,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    ) {
                        Column(
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(18.dp),
                        ) {
                            Text(
                                stringResource(R.string.never_category_pill),
                                style = MaterialTheme.typography.labelMedium,
                                color = HubLandingColors.HighStakesRed.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                            )
                            Spacer(Modifier.height(12.dp))
                            NeverPromptAnnotated(
                                text = state.currentPrompt ?: stringResource(R.string.never_deck_empty),
                            )
                            if (state.drinkingRulesOn && state.currentPrompt != null) {
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "🥂  ${stringResource(R.string.never_sip_hint)}",
                                    color = HubLandingColors.DeckGold,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        state.playerNames.take(4).forEach { name ->
                            Surface(
                                modifier = Modifier.size(34.dp),
                                shape = CircleShape,
                                color = HubLandingColors.SurfaceElevated,
                            ) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        name.take(1).uppercase(),
                                        color = HubLandingColors.White,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                        if (state.playerNames.size > 4) {
                            Surface(
                                modifier = Modifier.size(34.dp),
                                shape = CircleShape,
                                color = HubLandingColors.BrandPurple.copy(alpha = 0.45f),
                            ) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        "+${state.playerNames.size - 4}",
                                        color = HubLandingColors.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                    }
                    TextButton(onClick = { }) {
                        Text(
                            stringResource(R.string.never_flag_question),
                            color = HubLandingColors.TextDim,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    NeverQuickCircle(
                        label = stringResource(R.string.never_quick_have),
                        icon = Icons.Filled.LocalFireDepartment,
                        onClick = { vm.setAllPlayersAnswer(true) },
                    )
                    NeverQuickCircle(
                        label = stringResource(R.string.never_quick_never),
                        icon = Icons.Filled.Close,
                        onClick = { vm.setAllPlayersAnswer(false) },
                    )
                }
                Text(
                    stringResource(R.string.house_rules_game_title),
                    color = HubLandingColors.TextDim,
                    style = MaterialTheme.typography.labelSmall,
                )
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    itemsIndexed(state.playerNames) { index, name ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = HubLandingColors.Surface.copy(alpha = 0.9f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    name,
                                    color = HubLandingColors.White,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    val sel = state.playerAnswers.getOrNull(index)
                                    NeverRoundToggle(
                                        selected = sel == true,
                                        icon = Icons.Filled.LocalFireDepartment,
                                        onClick = { vm.setPlayerAnswer(index, true) },
                                    )
                                    NeverRoundToggle(
                                        selected = sel == false,
                                        icon = Icons.Filled.Close,
                                        onClick = { vm.setPlayerAnswer(index, false) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
            val canNext = state.currentPrompt != null
            OutlinedButton(
                onClick = {
                    if (soundOn) flipSound.playFlip()
                    vm.nextPrompt()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = canNext,
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(2.dp, HubLandingColors.DeckGold),
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        contentColor = HubLandingColors.DeckGold,
                    ),
            ) {
                Text(
                    stringResource(R.string.never_next_prompt_caps),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 8.dp).size(16.dp),
                    tint = HubLandingColors.DeckGold,
                )
            }
        }
    }
}

private data class NeverHighlightParts(val prefix: String, val highlight: String, val suffix: String)

private fun parseNeverHighlight(text: String): NeverHighlightParts? {
    val m = Regex("(?i)(never\\s+have\\s+i\\s+ever\\s*)(.+)").find(text.trim()) ?: return null
    val pre = m.groupValues[1]
    val rest = m.groupValues[2].trim()
    if (rest.isEmpty()) return NeverHighlightParts(pre, "", "")
    val punct = rest.indexOfFirst { it == '.' || it == ',' || it == ';' || it == '!' }
    val hiEnd = if (punct > 0) punct else minOf(rest.length, 40)
    val hi = rest.take(hiEnd).trim()
    val suf = rest.drop(hiEnd).trim()
    return NeverHighlightParts(pre, hi, suf)
}

@Composable
private fun NeverPromptAnnotated(text: String) {
    val parts = remember(text) { parseNeverHighlight(text) }
    if (parts == null) {
        Text(
            text,
            color = HubLandingColors.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            lineHeight = 28.sp,
        )
    } else {
        Text(
            buildAnnotatedString {
                append(parts.prefix)
                withStyle(
                    SpanStyle(
                        color = HubLandingColors.BrandPurple,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                    ),
                ) {
                    append(parts.highlight)
                }
                if (parts.suffix.isNotEmpty()) {
                    append(" ")
                    append(parts.suffix)
                }
            },
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            lineHeight = 28.sp,
        )
    }
}

@Composable
private fun NeverQuickCircle(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Surface(
            shape = CircleShape,
            color = HubLandingColors.SurfaceElevated,
            modifier = Modifier.size(76.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, tint = HubLandingColors.White, modifier = Modifier.size(32.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            label,
            color = HubLandingColors.White,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun NeverRoundToggle(
    selected: Boolean,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val border = if (selected) HubLandingColors.BrandPurple else Color.White.copy(alpha = 0.2f)
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) HubLandingColors.BrandPurple.copy(alpha = 0.35f) else HubLandingColors.SurfaceElevated,
        border = BorderStroke(2.dp, border),
        modifier = Modifier.size(48.dp),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = HubLandingColors.White, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun SpicyLogAge(atMillis: Long): String {
    val elapsed = (System.currentTimeMillis() - atMillis) / 1000L
    return when {
        elapsed < 60 -> stringResource(R.string.spicy_log_just_now)
        elapsed < 3600 ->
            stringResource(
                R.string.spicy_log_mins_ago,
                (elapsed / 60).toInt().coerceAtLeast(1),
            )
        else ->
            stringResource(
                R.string.spicy_log_hours_ago,
                (elapsed / 3600).toInt().coerceAtLeast(1),
            )
    }
}

@Composable
private fun SpicyActivityLogRow(
    entry: SpicySpinLogEntry,
    bodies: Array<out String>,
    actions: Array<out String>,
) {
    val b = bodies.getOrNull(entry.bodyIndex - 1) ?: "—"
    val a = actions.getOrNull(entry.actionIndex - 1) ?: "—"
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = HubLandingColors.BrandPurple.copy(alpha = 0.85f),
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "${b.uppercase()} / ${a.uppercase()}",
                color = HubLandingColors.White,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                SpicyLogAge(entry.timeMillis),
                color = HubLandingColors.TextDim,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
fun SpicySpinnerGameplayScreen(
    prefs: AppPreferencesRepository,
    onBack: () -> Unit = {},
    onOpenMenu: () -> Unit = {},
    onViewStore: () -> Unit = {},
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
    val flipSound = remember { FlipSoundEffects(context) }
    DisposableEffect(Unit) {
        onDispose {
            diceSound.release()
            flipSound.release()
        }
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
            if (soundOn) flipSound.playFlip()
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

    val spicyBg =
        Brush.verticalGradient(
            listOf(HubLandingColors.Black, HubLandingColors.Charcoal, HubLandingColors.Black),
        )
    val spicyTitleGradient =
        Brush.horizontalGradient(
            listOf(HubLandingColors.BrandPurple, HubLandingColors.SpicyPink),
        )
    val spinButtonBrush =
        Brush.horizontalGradient(
            listOf(HubLandingColors.BrandPurple, HubLandingColors.BrandPurpleDark),
        )

    Box(Modifier.fillMaxSize().background(spicyBg)) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            GameplayHubTopBar(onOpenMenu = onOpenMenu)
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
            Text(
                stringResource(R.string.spicy_live_session),
                style = MaterialTheme.typography.labelMedium,
                color = HubLandingColors.DeckGold,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
            )
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
                Text(
                    stringResource(R.string.spicy_title_the),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = HubLandingColors.White,
                )
                Text(
                    stringResource(R.string.spicy_title_spicy),
                    style =
                        MaterialTheme.typography.headlineSmall.merge(
                            TextStyle(brush = spicyTitleGradient),
                        ),
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    stringResource(R.string.spicy_title_spinner),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = HubLandingColors.White,
                )
            }
            Text(
                stringResource(R.string.spicy_current_player),
                style = MaterialTheme.typography.labelMedium,
                color = HubLandingColors.BrandPurple.copy(alpha = 0.85f),
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
            )
            Text(
                currentName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = HubLandingColors.White,
            )
            Text(
                stringResource(R.string.dice_turns_fmt, state.turnsCompleted, state.maxTurns),
                color = HubLandingColors.TextDim,
                style = MaterialTheme.typography.labelSmall,
            )
            Text(state.intensityLine, color = HubLandingColors.TextDim, style = MaterialTheme.typography.labelSmall)
            if (state.turnTimerEnabled && state.bodyRoll != null && state.actionTimerSeconds > 0) {
                Text(
                    stringResource(R.string.dice_timer_fmt, timerLeft),
                    color = HubLandingColors.DeckGold,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 300.dp),
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
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = HubLandingColors.Surface,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.spicy_current_result),
                        style = MaterialTheme.typography.labelSmall,
                        color = HubLandingColors.TextDim,
                        letterSpacing = 1.sp,
                    )
                    Spacer(Modifier.height(6.dp))
                    when {
                        isRolling -> {
                            Text("…", color = HubLandingColors.BodyGrey, style = MaterialTheme.typography.titleMedium)
                        }
                        state.sessionComplete -> {
                            Text(
                                stringResource(R.string.dice_session_complete),
                                color = HubLandingColors.DeckGold,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        state.bodyRoll != null && state.actionRoll != null && bodyLabel != null && actionLabel != null -> {
                            Text(
                                "${bodyLabel.uppercase()} / ${actionLabel.uppercase()}",
                                color = HubLandingColors.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        else -> {
                            Text(
                                stringResource(R.string.spicy_ready_spin),
                                color = HubLandingColors.DeckGold,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
            FlirtyPointerOverlay(
                showSparkles = rollHovered || rollPressed,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(28.dp), spotColor = HubLandingColors.BrandPurple),
            ) {
                Button(
                    onClick = { runRollAnimation { vm.rollDice() } },
                    enabled = !state.sessionComplete && !isRolling,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .hoverable(rollInteraction),
                    interactionSource = rollInteraction,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White,
                        ),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(28.dp),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(spinButtonBrush, RoundedCornerShape(28.dp))
                                .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            stringResource(R.string.spicy_spin_now),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                        )
                    }
                }
            }
            Text(
                stringResource(R.string.dice_tap_to_spin),
                color = HubLandingColors.TextDim,
                style = MaterialTheme.typography.labelSmall,
            )

            if (state.activityLog.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = HubLandingColors.Surface,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            stringResource(R.string.spicy_activity_log),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = HubLandingColors.White,
                        )
                        Spacer(Modifier.height(10.dp))
                        state.activityLog.forEach { entry ->
                            SpicyActivityLogRow(entry = entry, bodies = bodies, actions = actions)
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = HubLandingColors.SurfaceElevated,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, HubLandingColors.DeckGold.copy(alpha = 0.35f)),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.spicy_premium),
                        style = MaterialTheme.typography.labelMedium,
                        color = HubLandingColors.DeckGold,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                    )
                    Text(
                        stringResource(R.string.spicy_dirty_packs),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = HubLandingColors.White,
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = onViewStore,
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = HubLandingColors.Surface,
                                contentColor = HubLandingColors.White,
                            ),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(stringResource(R.string.spicy_view_store), fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (state.isDoubleRoll && !state.freeChoiceActive) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = HubLandingColors.Surface,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, HubLandingColors.SpicyOrange.copy(alpha = 0.5f)),
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            stringResource(R.string.dice_double_roll_title),
                            color = HubLandingColors.SpicyOrange,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            stringResource(R.string.dice_double_roll_body),
                            color = HubLandingColors.White,
                            modifier = Modifier.padding(top = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Button(
                            onClick = { vm.confirmFreeChoice() },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HubLandingColors.BrandPurple),
                        ) {
                            Text(stringResource(R.string.dice_free_choice_confirm))
                        }
                    }
                }
            }
            if (state.isDoubleRoll && state.freeChoiceActive) {
                Text(
                    stringResource(R.string.dice_free_choice_hint),
                    color = HubLandingColors.White,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Text(
                stringResource(R.string.dice_consent_line),
                color = HubLandingColors.TextDim,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(4.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { rulesExpanded = !rulesExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.dice_rules_title),
                    color = HubLandingColors.White,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    if (rulesExpanded) "▲" else "▼",
                    color = HubLandingColors.TextDim,
                )
            }
            if (rulesExpanded) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = 120.dp)
                            .verticalScroll(rememberScrollState()),
                ) {
                    rules.forEach { line ->
                        Text(
                            "• $line",
                            color = HubLandingColors.BodyGrey,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
            }

            Spacer(Modifier.height(4.dp))
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
                        colors = ButtonDefaults.buttonColors(containerColor = HubLandingColors.SurfaceElevated),
                    ) {
                        Text(stringResource(R.string.dice_back))
                    }
                }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { runRollAnimation { vm.reRoll() } },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = HubLandingColors.SurfaceElevated),
                        enabled = state.sessionReRollsRemaining > 0 && state.bodyRoll != null && !isRolling,
                    ) {
                        Text(stringResource(R.string.dice_reroll_fmt, state.sessionReRollsRemaining))
                    }
                    Button(
                        onClick = { vm.nextPlayer() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = HubLandingColors.BrandPurple),
                        enabled = state.bodyRoll != null && !isRolling,
                    ) {
                        Text(stringResource(R.string.next_player))
                    }
                }
            }
        }
    }
}

private fun wyrIllustrativeStats(optionA: String?, optionB: String?): Triple<Int, Int, String> {
    if (optionA.isNullOrEmpty()) return Triple(50, 50, "—")
    val h = (optionA + (optionB ?: "")).hashCode().absoluteValue
    val pctA = 38 + (h % 25)
    val votes = listOf("1.2M", "684K", "982K", "1.1M", "890K")[h % 5]
    return Triple(pctA, (100 - pctA).coerceIn(0, 100), votes)
}

@Composable
fun WyrGameplayScreen(
    prefs: AppPreferencesRepository,
    onOpenMenu: () -> Unit = {},
) {
    val appContext = LocalContext.current.applicationContext
    val ctx = LocalContext.current
    val snapshot = remember {
        SessionStateHolder.pending.also { SessionStateHolder.pending = null }
    }
    val dataManager = remember { DataManager(appContext) }
    val vm: WyrGameplayViewModel = viewModel(
        factory = WyrGameplayViewModel.factory(snapshot, dataManager),
    )
    val state by vm.state.collectAsStateWithLifecycle()

    val flipSound = remember { FlipSoundEffects(appContext) }
    DisposableEffect(Unit) {
        onDispose { flipSound.release() }
    }
    val soundOn by prefs.soundEffectsEnabled.collectAsStateWithLifecycle(initialValue = true)
    val scope = rememberCoroutineScope()

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

    val (pctA, pctB, votesLabel) = remember(state.optionA, state.optionB) {
        wyrIllustrativeStats(state.optionA, state.optionB)
    }
    val totalPairs = state.totalPairs.coerceAtLeast(1)
    val roundNum = (totalPairs - state.cardsRemaining).coerceIn(1, totalPairs)

    val wyrBg =
        Brush.verticalGradient(
            listOf(HubLandingColors.Black, HubLandingColors.Charcoal, HubLandingColors.Black),
        )

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(wyrBg)
                .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GameplayHubTopBar(onOpenMenu = onOpenMenu)
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
        Spacer(Modifier.height(8.dp))
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = HubLandingColors.Surface,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        ) {
            Text(
                stringResource(R.string.wyr_round_fmt, roundNum, totalPairs),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = HubLandingColors.White,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(16.dp))
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(HubLandingColors.BrandPurple.copy(alpha = 0.2f), Color.Transparent),
                            radius = 280f,
                        ),
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(R.string.wyr_title_would_you),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = HubLandingColors.White,
                )
                Text(
                    stringResource(R.string.wyr_title_rather),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = HubLandingColors.BrandPurple,
                )
            }
        }
        Spacer(Modifier.height(18.dp))
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
        WyrOptionCard(
            label = stringResource(R.string.wyr_option_a_label),
            labelColor = HubLandingColors.BrandPurple,
            bodyText = state.optionA ?: stringResource(R.string.wyr_deck_empty),
            percent = pctA,
            votesLabel = votesLabel,
            accentIcon = Icons.Filled.Bolt,
            buttonText = stringResource(R.string.wyr_choose_reality),
            isOutlined = false,
            onPick = {
                if (soundOn) flipSound.playFlip()
                vm.pickOptionA()
            },
        )
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier.size(WyrOrBadgeSize),
                shape = CircleShape,
                color = HubLandingColors.SurfaceElevated,
                border = BorderStroke(2.dp, Color.White.copy(alpha = 0.15f)),
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.wyr_or),
                        color = HubLandingColors.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        WyrOptionCard(
            label = stringResource(R.string.wyr_option_b_label),
            labelColor = HubLandingColors.WyrCoral,
            bodyText = state.optionB ?: "—",
            percent = pctB,
            votesLabel = votesLabel,
            accentIcon = Icons.AutoMirrored.Filled.MenuBook,
            buttonText = stringResource(R.string.wyr_select_wisdom),
            isOutlined = true,
            onPick = {
                if (soundOn) flipSound.playFlip()
                vm.pickOptionB()
            },
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick = {
                    if (soundOn) flipSound.playFlip()
                    vm.nextCard()
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, HubLandingColors.TextDim),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = HubLandingColors.White),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.size(6.dp))
                Text(stringResource(R.string.wyr_skip), fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = {
                    val a = state.optionA ?: return@OutlinedButton
                    val b = state.optionB ?: return@OutlinedButton
                    val send =
                        Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, ctx.getString(R.string.wyr_share_subject))
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "$a\n\n${ctx.getString(R.string.wyr_or)}\n\n$b",
                            )
                        }
                    ctx.startActivity(Intent.createChooser(send, null))
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, HubLandingColors.TextDim),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = HubLandingColors.White),
            ) {
                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(6.dp))
                Text(stringResource(R.string.wyr_share), fontWeight = FontWeight.Bold)
            }
        }
        if (state.debateTimerEnabled && cardVisible) {
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.wyr_timer_fmt, timerLeft),
                modifier = Modifier.fillMaxWidth(),
                color = HubLandingColors.DeckGold,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                stringResource(R.string.debate_timer),
                color = HubLandingColors.BodyGrey,
                style = MaterialTheme.typography.bodyMedium,
            )
            Switch(
                checked = state.debateTimerEnabled,
                onCheckedChange = { vm.setDebateTimerEnabled(it) },
            )
        }
        Text(
            stringResource(R.string.wyr_stat_illusory),
            color = HubLandingColors.TextDim,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(R.string.wyr_next_card) + " · ${state.cardsRemaining}",
            color = HubLandingColors.TextDim,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 8.dp),
            textAlign = TextAlign.Center,
        )
        }
        Button(
            onClick = {
                scope.launch {
                    if (soundOn) flipSound.playFlip()
                    vm.nextCard()
                }
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black,
                ),
        ) {
            Text(stringResource(R.string.wyr_next_card), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun WyrOptionCard(
    label: String,
    labelColor: Color,
    bodyText: String,
    percent: Int,
    votesLabel: String,
    accentIcon: ImageVector,
    buttonText: String,
    isOutlined: Boolean,
    onPick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(WyrCardRadius),
        color = HubLandingColors.Surface,
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, labelColor.copy(alpha = 0.35f)),
    ) {
        Box(Modifier.fillMaxWidth()) {
            Icon(
                accentIcon,
                contentDescription = null,
                tint = labelColor.copy(alpha = 0.12f),
                modifier =
                    Modifier
                        .align(Alignment.CenterEnd)
                        .size(120.dp)
                        .offset(x = 24.dp),
            )
            Column(Modifier.padding(18.dp)) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                    color = labelColor,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )
                Spacer(Modifier.height(10.dp))
                val bodyScroll = rememberScrollState()
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = 100.dp)
                            .verticalScroll(bodyScroll),
                ) {
                    Text(
                        bodyText,
                        color = HubLandingColors.White,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp,
                    )
                }
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            stringResource(R.string.wyr_global_choice),
                            style = MaterialTheme.typography.labelSmall,
                            color = HubLandingColors.TextDim,
                        )
                        Text(
                            "$percent%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = labelColor,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            stringResource(R.string.wyr_total_votes),
                            style = MaterialTheme.typography.labelSmall,
                            color = HubLandingColors.TextDim,
                        )
                        Text(
                            votesLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = HubLandingColors.White,
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                if (isOutlined) {
                    OutlinedButton(
                        onClick = onPick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        border = BorderStroke(2.dp, HubLandingColors.WyrCoral),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HubLandingColors.WyrCoral),
                    ) {
                        Text(buttonText, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onPick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HubLandingColors.BrandPurple),
                    ) {
                        Text(buttonText, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
