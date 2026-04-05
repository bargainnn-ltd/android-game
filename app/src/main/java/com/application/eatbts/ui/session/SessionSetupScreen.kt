package com.application.eatbts.ui.session

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.application.eatbts.R
import com.application.eatbts.data.GameConfig
import com.application.eatbts.data.Level
import com.application.eatbts.data.PoolMode
import com.application.eatbts.data.online.OnlineTruthDareSession
import com.application.eatbts.data.local.AppPreferencesRepository
import com.application.eatbts.navigation.SessionGameMode
import com.application.eatbts.session.SessionSnapshot
import com.application.eatbts.ui.theme.NeonTokens
import com.application.eatbts.ui.theme.themeScreenBackgroundBrush
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

data class SessionPlayerUi(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val selected: Boolean = true,
)

private enum class SessionIntensityChoice {
    SPICY,
    EXTRA_SPICY,
}

private enum class PlayModeChoice {
    PASS_AND_PLAY,
    HOST_ONLINE,
}

private enum class PromptMix {
    TRUTH_ONLY,
    DARE_ONLY,
    BOTH,
}

private fun PromptMix.toIncludeFlags(): Pair<Boolean, Boolean> = when (this) {
    PromptMix.TRUTH_ONLY -> true to false
    PromptMix.DARE_ONLY -> false to true
    PromptMix.BOTH -> true to true
}

@Composable
fun SessionSetupScreen(
    gameMode: SessionGameMode,
    extremeUnlocked: Boolean,
    onUnlockExtreme: () -> Unit,
    prefs: AppPreferencesRepository,
    defaultTurnTimerSeconds: Int,
    onBack: () -> Unit,
    onStartTruthDare: (GameConfig) -> Unit,
    onStartOnlineMatchmaking: (OnlineTruthDareSession) -> Unit = { },
    onStartInAppMode: (SessionSnapshot) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var showExtremeDialog by remember { mutableStateOf(false) }
    var showAddPlayerDialog by remember { mutableStateOf(false) }
    var newPlayerNameInput by remember { mutableStateOf("") }

    val players = remember { mutableStateListOf<SessionPlayerUi>() }

    LaunchedEffect(Unit) {
        val names = prefs.sessionPlayerNames.first()
        players.clear()
        names.forEach { name ->
            if (name.isNotBlank()) {
                players.add(SessionPlayerUi(name = name.trim()))
            }
        }
    }
    var playMode by remember { mutableStateOf(PlayModeChoice.PASS_AND_PLAY) }
    var intensity by remember { mutableStateOf(SessionIntensityChoice.EXTRA_SPICY) }
    var drinkingOn by remember { mutableStateOf(true) }
    var promptMix by remember { mutableStateOf(PromptMix.BOTH) }
    var timerOn by remember { mutableStateOf(false) }

    fun levelForIntensity(): Level = when (intensity) {
        SessionIntensityChoice.SPICY -> Level.SPICY
        SessionIntensityChoice.EXTRA_SPICY ->
            if (extremeUnlocked) Level.EXTREME else Level.SPICY
    }

    fun trySetIntensity(choice: SessionIntensityChoice) {
        if (choice == SessionIntensityChoice.EXTRA_SPICY && !extremeUnlocked) {
            showExtremeDialog = true
            return
        }
        intensity = choice
    }

    fun startGame() {
        val selectedPlayers = players.filter { it.selected }
        if (selectedPlayers.size < 2) return
        val names = selectedPlayers.map { it.name.trim().ifBlank { "Player" } }
        val level = levelForIntensity()
        val intensityLabel = when (intensity) {
            SessionIntensityChoice.SPICY -> "Spicy"
            SessionIntensityChoice.EXTRA_SPICY -> "Extra Spicy"
        }
        val timerSec = if (timerOn) defaultTurnTimerSeconds.coerceIn(10, 120) else 0

        scope.launch {
            prefs.setCategoryPartyDrinking(drinkingOn)
        }

        val (includeTruths, includeDares) = promptMix.toIncludeFlags()
        val snapshot = SessionSnapshot(
            intensityLabel = intensityLabel,
            drinkingRulesOn = drinkingOn,
            includeTruths = includeTruths,
            includeDares = includeDares,
            turnTimerOn = timerOn,
            level = level,
            turnTimerSeconds = timerSec,
            playerNames = names,
        )

        when (gameMode) {
            SessionGameMode.TRUTH_DARE -> {
                onStartTruthDare(
                    GameConfig(
                        firstTurnIsPlayerOne = true,
                        level = level,
                        poolMode = PoolMode.ALL,
                        includeTruths = includeTruths,
                        includeDares = includeDares,
                        playerNames = names,
                        firstPlayerIndex = 0,
                        drinkingRulesEnabled = drinkingOn,
                        turnTimerSeconds = timerSec,
                    ),
                )
            }
            SessionGameMode.NEVER, SessionGameMode.SPICY_SPINNER, SessionGameMode.WYR -> {
                onStartInAppMode(snapshot)
            }
        }
    }

    fun startOnlineMatch() {
        val selectedPlayers = players.filter { it.selected }
        if (selectedPlayers.size < 2) return
        val names = selectedPlayers.map { it.name.trim().ifBlank { "Player" } }
        val level = levelForIntensity()
        val (includeTruths, includeDares) = promptMix.toIncludeFlags()
        val timerSec = if (timerOn) defaultTurnTimerSeconds.coerceIn(10, 120) else 0
        scope.launch {
            prefs.setCategoryPartyDrinking(drinkingOn)
        }
        onStartOnlineMatchmaking(
            OnlineTruthDareSession(
                displayName = names.first(),
                level = level,
                includeTruths = includeTruths,
                includeDares = includeDares,
                turnTimerSeconds = timerSec,
            ),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeScreenBackgroundBrush())
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("session_setup_screen"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_couple_games_logo),
                    contentDescription = stringResource(R.string.cd_app_logo),
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.session_setup_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            IconButton(onClick = { }) {
                Icon(
                    Icons.Outlined.Menu,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        // Who's Playing
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.session_whos_playing),
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    stringResource(R.string.session_n_selected, players.count { it.selected }),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (players.size < 4) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .border(BorderStroke(2.dp, Color.Gray.copy(alpha = 0.5f)), CircleShape)
                            .clickable {
                                newPlayerNameInput = ""
                                showAddPlayerDialog = true
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = Color.Gray)
                    }
                    Text(
                        stringResource(R.string.session_add_new),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier.width(72.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
            players.forEachIndexed { index, p ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box {
                        val ringColor = if (p.selected) NeonTokens.NeonMagenta else Color.Transparent
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .border(BorderStroke(3.dp, ringColor), CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    players[index] = p.copy(selected = !p.selected)
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = p.name.take(1).uppercase(),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        if (p.selected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(NeonTokens.NeonMagenta),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        }
                    }
                    Text(
                        p.name,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(72.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.session_play_mode),
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PlayModeSegment(
                label = stringResource(R.string.session_pass_and_play),
                selected = playMode == PlayModeChoice.PASS_AND_PLAY,
                onClick = { playMode = PlayModeChoice.PASS_AND_PLAY },
                modifier = Modifier.weight(1f),
            )
            PlayModeSegment(
                label = stringResource(R.string.session_host_online),
                selected = playMode == PlayModeChoice.HOST_ONLINE,
                enabled = gameMode == SessionGameMode.TRUTH_DARE,
                onClick = { if (gameMode == SessionGameMode.TRUTH_DARE) playMode = PlayModeChoice.HOST_ONLINE },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.session_intensity_level),
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IntensityCard(
                title = stringResource(R.string.session_spicy_card),
                subtitle = stringResource(R.string.session_spicy_desc),
                selected = intensity == SessionIntensityChoice.SPICY,
                accent = false,
                onClick = { trySetIntensity(SessionIntensityChoice.SPICY) },
                modifier = Modifier.weight(1f),
            )
            IntensityCard(
                title = stringResource(R.string.session_extra_spicy_card),
                subtitle = stringResource(R.string.session_extra_spicy_desc),
                selected = intensity == SessionIntensityChoice.EXTRA_SPICY,
                accent = true,
                onClick = { trySetIntensity(SessionIntensityChoice.EXTRA_SPICY) },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.session_content_settings),
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        ContentRow(
            title = stringResource(R.string.session_drinking_rules),
            subtitle = stringResource(R.string.session_drinking_subtitle),
            checked = drinkingOn,
            onCheckedChange = { drinkingOn = it },
        )
        if (gameMode == SessionGameMode.TRUTH_DARE) {
            Text(
                stringResource(R.string.session_prompt_mix),
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                stringResource(R.string.session_prompt_mix_subtitle),
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PromptMixSegment(
                    label = stringResource(R.string.session_prompt_mix_truth_only),
                    selected = promptMix == PromptMix.TRUTH_ONLY,
                    onClick = { promptMix = PromptMix.TRUTH_ONLY },
                    modifier = Modifier.weight(1f),
                )
                PromptMixSegment(
                    label = stringResource(R.string.session_prompt_mix_dare_only),
                    selected = promptMix == PromptMix.DARE_ONLY,
                    onClick = { promptMix = PromptMix.DARE_ONLY },
                    modifier = Modifier.weight(1f),
                )
                PromptMixSegment(
                    label = stringResource(R.string.session_prompt_mix_both),
                    selected = promptMix == PromptMix.BOTH,
                    onClick = { promptMix = PromptMix.BOTH },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        ContentRow(
            title = stringResource(R.string.session_turn_timer_row),
            subtitle = stringResource(R.string.session_turn_timer_subtitle, defaultTurnTimerSeconds),
            checked = timerOn,
            onCheckedChange = { timerOn = it },
        )

        Spacer(Modifier.height(28.dp))
        val canStart = players.count { it.selected } >= 2
        val onlineTruthDare = gameMode == SessionGameMode.TRUTH_DARE && playMode == PlayModeChoice.HOST_ONLINE
        Button(
            onClick = {
                if (onlineTruthDare) startOnlineMatch() else startGame()
            },
            enabled = canStart && (playMode == PlayModeChoice.PASS_AND_PLAY || onlineTruthDare),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("session_start_game"),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black,
                disabledContainerColor = Color.White.copy(alpha = 0.35f),
            ),
        ) {
            Text(
                if (onlineTruthDare) stringResource(R.string.session_find_opponent) else stringResource(R.string.start_game),
                fontWeight = FontWeight.Bold,
            )
            Icon(
                Icons.Filled.PlayArrow,
                contentDescription = null,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Text(
            stringResource(R.string.session_start_disclaimer),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
        )
    }

    if (showExtremeDialog) {
        AlertDialog(
            onDismissRequest = { showExtremeDialog = false },
            title = { Text(stringResource(R.string.extreme_lock_title)) },
            text = { Text(stringResource(R.string.extreme_lock_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUnlockExtreme()
                        intensity = SessionIntensityChoice.EXTRA_SPICY
                        showExtremeDialog = false
                    },
                ) {
                    Text(stringResource(R.string.confirm_unlock))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExtremeDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    if (showAddPlayerDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddPlayerDialog = false
                newPlayerNameInput = ""
            },
            title = { Text(stringResource(R.string.session_add_player_title)) },
            text = {
                OutlinedTextField(
                    value = newPlayerNameInput,
                    onValueChange = { newPlayerNameInput = it },
                    label = { Text(stringResource(R.string.session_add_player_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                val canAdd = newPlayerNameInput.trim().isNotEmpty() && players.size < 4
                TextButton(
                    enabled = canAdd,
                    onClick = {
                        val trimmed = newPlayerNameInput.trim()
                        if (trimmed.isNotEmpty() && players.size < 4) {
                            players.add(SessionPlayerUi(name = trimmed))
                            scope.launch {
                                prefs.setSessionPlayerNames(players.map { it.name })
                            }
                        }
                        newPlayerNameInput = ""
                        showAddPlayerDialog = false
                    },
                ) {
                    Text(stringResource(R.string.session_add_player_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        newPlayerNameInput = ""
                        showAddPlayerDialog = false
                    },
                ) {
                    Text(stringResource(R.string.session_add_player_cancel))
                }
            },
        )
    }
}

@Composable
private fun PlayModeSegment(
    label: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val border = if (selected) BorderStroke(2.dp, NeonTokens.NeonMagenta) else BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = scheme.surfaceVariant,
        border = border,
    ) {
        Text(
            label,
            modifier = Modifier.padding(14.dp),
            color = if (enabled) scheme.onSurface else Color.Gray,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PromptMixSegment(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val border = if (selected) BorderStroke(2.dp, NeonTokens.NeonMagenta) else BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = scheme.surfaceVariant,
        border = border,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = NeonTokens.NeonMagenta,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Text(
                label,
                color = scheme.onSurface,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun IntensityCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    accent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val borderColor = when {
        selected && accent -> NeonTokens.NeonMagenta
        selected -> NeonTokens.NeonCyan
        else -> Color.Gray.copy(alpha = 0.25f)
    }
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = scheme.surfaceVariant,
        border = BorderStroke(2.dp, borderColor),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (accent && selected) NeonTokens.NeonMagenta else scheme.onSurface,
                )
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .border(BorderStroke(2.dp, Color.Gray), CircleShape)
                        .background(if (selected) NeonTokens.NeonMagenta else Color.Transparent),
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
private fun ContentRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = scheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, color = scheme.onSurface, fontWeight = FontWeight.Medium)
                Text(subtitle, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = NeonTokens.NeonMagenta,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.Gray.copy(alpha = 0.4f),
                ),
            )
        }
    }
}
