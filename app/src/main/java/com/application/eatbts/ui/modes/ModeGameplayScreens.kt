package com.application.eatbts.ui.modes

import android.content.Intent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.application.eatbts.data.local.AppPreferencesRepository
import com.application.eatbts.R
import com.application.eatbts.data.DataManager
import com.application.eatbts.session.SessionStateHolder
import com.application.eatbts.viewmodel.SpicySpinnerGameplayViewModel
import com.application.eatbts.viewmodel.NeverGameplayViewModel
import com.application.eatbts.ui.dice.CouplesDualRingSpinner
import com.application.eatbts.ui.dice.DiceSoundEffects
import com.application.eatbts.ui.dice.SpicySpinnerSpinTotalMs
import com.application.eatbts.ui.dice.FlirtyPointerOverlay
import com.application.eatbts.ui.sound.FlipSoundEffects
import com.application.eatbts.viewmodel.CouplesDiceRules
import com.application.eatbts.viewmodel.WyrGameplayViewModel
import com.application.eatbts.ui.hub.HubLandingColors
import com.application.eatbts.ui.theme.NeonTokens
import com.application.eatbts.ui.theme.themeHubCardElevated
import com.application.eatbts.ui.theme.themeHubCardSurface
import com.application.eatbts.ui.theme.themeHubLandingBrush
import com.application.eatbts.ui.theme.themeHubPrimaryText
import com.application.eatbts.ui.theme.themeHubSecondaryText
import com.application.eatbts.ui.theme.themeHubTertiaryText
import com.application.eatbts.viewmodel.SpicySpinLogEntry
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
    val scope = rememberCoroutineScope()
    val rotation = remember { Animatable(0f) }
    val rotY by produceState(initialValue = 0f, rotation) {
        snapshotFlow { rotation.value }.collect { value = it }
    }
    val density = LocalDensity.current
    val flipCameraDistance = 12f * density.density
    var neverFlipBusy by remember { mutableStateOf(false) }

    fun launchNeverFlipThenAdvance(advance: () -> Unit) {
        if (neverFlipBusy) return
        scope.launch {
            neverFlipBusy = true
            try {
                if (soundOn) flipSound.playFlip()
                val half = tween<Float>(durationMillis = 220, easing = LinearEasing)
                rotation.animateTo(90f, half)
                advance()
                rotation.animateTo(180f, half)
                rotation.snapTo(0f)
            } finally {
                neverFlipBusy = false
            }
        }
    }

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

    val bg = themeHubLandingBrush()
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
            GameplayHubTopBar(
                title = stringResource(R.string.game_never),
                onOpenMenu = onOpenMenu,
            )
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
                color = themeHubPrimaryText(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.never_round_fmt, roundNum, state.totalPrompts.coerceAtLeast(1)),
                    style = MaterialTheme.typography.labelLarge,
                    color = themeHubSecondaryText(),
                    fontWeight = FontWeight.Bold,
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        stringResource(R.string.never_mood_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = themeHubTertiaryText(),
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
                trackColor = themeHubCardElevated(),
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
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    cameraDistance = flipCameraDistance
                                    rotationY = rotY
                                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                                    scaleX = if (rotY > 90f) -1f else 1f
                                },
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(28.dp),
                            color = themeHubCardSurface(),
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
                                color = themeHubCardElevated(),
                            ) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        name.take(1).uppercase(),
                                        color = themeHubPrimaryText(),
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
                                        color = themeHubPrimaryText(),
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
                            color = themeHubTertiaryText(),
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
                    color = themeHubTertiaryText(),
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
                            color = themeHubCardSurface().copy(alpha = 0.9f),
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
                                    color = themeHubPrimaryText(),
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
                onClick = { launchNeverFlipThenAdvance { vm.nextPrompt() } },
                modifier = Modifier.fillMaxWidth(),
                enabled = canNext && !neverFlipBusy,
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
            color = themeHubPrimaryText(),
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
            color = themeHubCardElevated(),
            modifier = Modifier.size(76.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, tint = themeHubPrimaryText(), modifier = Modifier.size(32.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            label,
            color = themeHubPrimaryText(),
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
        color = if (selected) HubLandingColors.BrandPurple.copy(alpha = 0.35f) else themeHubCardElevated(),
        border = BorderStroke(2.dp, border),
        modifier = Modifier.size(48.dp),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = themeHubPrimaryText(), modifier = Modifier.size(22.dp))
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
                color = themeHubPrimaryText(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                SpicyLogAge(entry.timeMillis),
                color = themeHubTertiaryText(),
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

    val spicyBg = themeHubLandingBrush()
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
            GameplayHubTopBar(
                title = stringResource(R.string.game_spicy_spinner),
                onOpenMenu = onOpenMenu,
                centerTitle = { SpicySpinnerBarTitle() },
            )
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.spicy_current_player),
                    style = MaterialTheme.typography.labelMedium,
                    color = HubLandingColors.BrandPurple.copy(alpha = 0.85f),
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    currentName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = themeHubPrimaryText(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text =
                    "${stringResource(R.string.dice_turns_fmt, state.turnsCompleted, state.maxTurns)} · ${state.intensityLine}",
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 14.dp),
                color = themeHubTertiaryText(),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (state.turnTimerEnabled && state.bodyRoll != null && state.actionTimerSeconds > 0) {
                Text(
                    stringResource(R.string.dice_timer_fmt, timerLeft),
                    color = HubLandingColors.DeckGold,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(28.dp))
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
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
                color = themeHubCardSurface(),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.spicy_current_result),
                        style = MaterialTheme.typography.labelSmall,
                        color = themeHubTertiaryText(),
                        letterSpacing = 1.sp,
                    )
                    Spacer(Modifier.height(6.dp))
                    when {
                        isRolling -> {
                            Text("…", color = themeHubSecondaryText(), style = MaterialTheme.typography.titleMedium)
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
                                color = themeHubPrimaryText(),
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
                color = themeHubTertiaryText(),
                style = MaterialTheme.typography.labelSmall,
            )

            if (state.activityLog.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = themeHubCardSurface(),
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            stringResource(R.string.spicy_activity_log),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = themeHubPrimaryText(),
                        )
                        Spacer(Modifier.height(10.dp))
                        state.activityLog.forEach { entry ->
                            SpicyActivityLogRow(entry = entry, bodies = bodies, actions = actions)
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }

            if (state.isDoubleRoll && !state.freeChoiceActive) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = themeHubCardSurface(),
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
                            color = themeHubPrimaryText(),
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
                    color = themeHubPrimaryText(),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Text(
                stringResource(R.string.dice_consent_line),
                color = themeHubTertiaryText(),
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
                    color = themeHubPrimaryText(),
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    if (rulesExpanded) "▲" else "▼",
                    color = themeHubTertiaryText(),
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
                            color = themeHubSecondaryText(),
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
                        colors = ButtonDefaults.buttonColors(containerColor = themeHubCardElevated()),
                    ) {
                        Text(stringResource(R.string.dice_back))
                    }
                }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { runRollAnimation { vm.reRoll() } },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = themeHubCardElevated()),
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
    val rotation = remember { Animatable(0f) }
    val rotY by produceState(initialValue = 0f, rotation) {
        snapshotFlow { rotation.value }.collect { value = it }
    }
    val density = LocalDensity.current
    val flipCameraDistance = 12f * density.density
    var wyrFlipBusy by remember { mutableStateOf(false) }

    fun launchWyrFlipThenAdvance(advance: () -> Unit) {
        if (wyrFlipBusy) return
        scope.launch {
            wyrFlipBusy = true
            try {
                if (soundOn) flipSound.playFlip()
                val half = tween<Float>(durationMillis = 220, easing = LinearEasing)
                rotation.animateTo(90f, half)
                advance()
                rotation.animateTo(180f, half)
                rotation.snapTo(0f)
            } finally {
                wyrFlipBusy = false
            }
        }
    }

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

    val totalPairs = state.totalPairs.coerceAtLeast(1)
    val roundNum = (totalPairs - state.cardsRemaining).coerceIn(1, totalPairs)

    val wyrBg = themeHubLandingBrush()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(wyrBg)
                .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GameplayHubTopBar(
            title = stringResource(R.string.game_wyr),
            onOpenMenu = onOpenMenu,
        )
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
            color = themeHubCardSurface(),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        ) {
            Text(
                stringResource(R.string.wyr_round_fmt, roundNum, totalPairs),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = themeHubPrimaryText(),
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
                    color = themeHubPrimaryText(),
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
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        cameraDistance = flipCameraDistance
                        rotationY = rotY
                        transformOrigin = TransformOrigin(0.5f, 0.5f)
                        scaleX = if (rotY > 90f) -1f else 1f
                    },
        ) {
            WyrCombinedOptionCard(
                optionALabel = stringResource(R.string.wyr_option_a_label),
                optionBLabel = stringResource(R.string.wyr_option_b_label),
                bodyA = state.optionA ?: stringResource(R.string.wyr_deck_empty),
                bodyB = state.optionB ?: "—",
                onPickTop = { launchWyrFlipThenAdvance { vm.pickOptionA() } },
                onPickBottom = { launchWyrFlipThenAdvance { vm.pickOptionB() } },
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick = { launchWyrFlipThenAdvance { vm.nextCard() } },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, themeHubTertiaryText()),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = themeHubPrimaryText()),
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
                border = BorderStroke(1.dp, themeHubTertiaryText()),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = themeHubPrimaryText()),
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
                color = themeHubSecondaryText(),
                style = MaterialTheme.typography.bodyMedium,
            )
            Switch(
                checked = state.debateTimerEnabled,
                onCheckedChange = { vm.setDebateTimerEnabled(it) },
            )
        }
        Text(
            stringResource(R.string.wyr_stat_illusory),
            color = themeHubTertiaryText(),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(R.string.wyr_next_card) + " · ${state.cardsRemaining}",
            color = themeHubTertiaryText(),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 8.dp),
            textAlign = TextAlign.Center,
        )
        }
        Button(
            onClick = { launchWyrFlipThenAdvance { vm.nextCard() } },
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
private fun WyrCombinedOptionCard(
    optionALabel: String,
    optionBLabel: String,
    bodyA: String,
    bodyB: String,
    onPickTop: () -> Unit,
    onPickBottom: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val purple = HubLandingColors.BrandPurple
    val coral = HubLandingColors.WyrCoral
    val shape = RoundedCornerShape(WyrCardRadius)
    val orFloatOffset = WyrOrBadgeSize / 2
    val bodyStyle =
        MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold,
            lineHeight = 30.sp,
        )
    Box(
        modifier
            .fillMaxWidth()
            .padding(top = orFloatOffset),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .clip(shape)
                    .border(2.dp, Color.White.copy(alpha = 0.14f), shape),
        ) {
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        purple.copy(alpha = 0.45f),
                                        themeHubCardSurface().copy(alpha = 0.92f),
                                    ),
                            ),
                        )
                        .clickable(onClick = onPickTop),
            ) {
                Box(Modifier.fillMaxSize()) {
                    Icon(
                        Icons.Filled.Bolt,
                        contentDescription = null,
                        tint = purple.copy(alpha = 0.1f),
                        modifier =
                            Modifier
                                .align(Alignment.Center)
                                .size(120.dp),
                    )
                    val scrollA = rememberScrollState()
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center)
                                .verticalScroll(scrollA)
                                .padding(horizontal = 16.dp)
                                .padding(top = 8.dp, bottom = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            optionALabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = purple,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = bodyA,
                            color = Color.White,
                            style = bodyStyle,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        themeHubCardSurface().copy(alpha = 0.92f),
                                        coral.copy(alpha = 0.45f),
                                    ),
                            ),
                        )
                        .clickable(onClick = onPickBottom),
            ) {
                Box(Modifier.fillMaxSize()) {
                    Icon(
                        Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = coral.copy(alpha = 0.1f),
                        modifier =
                            Modifier
                                .align(Alignment.Center)
                                .size(120.dp),
                    )
                    val scrollB = rememberScrollState()
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center)
                                .verticalScroll(scrollB)
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            optionBLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = coral,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = bodyB,
                            color = Color.White,
                            style = bodyStyle,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
        Surface(
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = -orFloatOffset)
                    .size(WyrOrBadgeSize)
                    .shadow(12.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.35f), spotColor = Color.Black.copy(alpha = 0.4f)),
            shape = CircleShape,
            color = themeHubCardElevated(),
            border = BorderStroke(2.dp, Color.White.copy(alpha = 0.22f)),
            tonalElevation = 6.dp,
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.wyr_or),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
