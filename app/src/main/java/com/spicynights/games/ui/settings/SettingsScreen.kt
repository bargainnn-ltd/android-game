package com.spicynights.games.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spicynights.games.BuildConfig
import com.spicynights.games.R
import com.spicynights.games.data.local.AppPreferencesRepository
import com.spicynights.games.data.local.AppThemePreference
import com.spicynights.games.data.local.DefaultIntensity
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    prefs: AppPreferencesRepository,
    climaxUnlocked: Boolean,
    onUnlockClimax: () -> Unit,
    onResetSession: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    var showClimaxDialog by remember { mutableStateOf(false) }

    val defaultIntensity by prefs.defaultIntensity.collectAsStateWithLifecycle(initialValue = DefaultIntensity.SPICY.storageValue)
    val romance by prefs.categoryRomance.collectAsStateWithLifecycle(initialValue = true)
    val party by prefs.categoryPartyDrinking.collectAsStateWithLifecycle(initialValue = true)
    val nsfw by prefs.categoryNsfw.collectAsStateWithLifecycle(initialValue = false)
    val timerSec by prefs.turnTimerSeconds.collectAsStateWithLifecycle(initialValue = 30)
    val soundOn by prefs.soundEffectsEnabled.collectAsStateWithLifecycle(initialValue = true)
    val hapticsOn by prefs.hapticFeedbackEnabled.collectAsStateWithLifecycle(initialValue = true)
    val themePref by prefs.appThemePreference.collectAsStateWithLifecycle(initialValue = AppThemePreference.MIDNIGHT)

    var sliderValue by remember(timerSec) { mutableIntStateOf(timerSec) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.ic_spicy_night_logo),
                contentDescription = stringResource(R.string.cd_app_logo),
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.settings_subtitle), style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            }
        }
        Spacer(Modifier.height(20.dp))
        SectionTitle(stringResource(R.string.settings_game_prefs))
        IntensityRow(defaultIntensity) { v ->
            if (v == DefaultIntensity.EXTREME.storageValue && !climaxUnlocked) {
                showClimaxDialog = true
            } else {
                scope.launch { prefs.setDefaultIntensity(v) }
            }
        }
        Text(stringResource(R.string.settings_default_intensity_hint), style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
        ToggleRow(stringResource(R.string.settings_category_romance), romance) { scope.launch { prefs.setCategoryRomance(it) } }
        ToggleRow(stringResource(R.string.settings_category_party), party) { scope.launch { prefs.setCategoryPartyDrinking(it) } }
        ToggleRow(stringResource(R.string.settings_category_nsfw), nsfw) { scope.launch { prefs.setCategoryNsfw(it) } }
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.settings_turn_timer), color = Color.White, fontWeight = FontWeight.Medium)
        Text("${sliderValue}s", color = Color(0xFFFF9800), style = MaterialTheme.typography.titleMedium)
        Slider(
            value = sliderValue.toFloat(),
            onValueChange = { sliderValue = it.toInt() },
            onValueChangeFinished = {
                scope.launch { prefs.setTurnTimerSeconds(sliderValue) }
            },
            valueRange = 10f..120f,
            steps = 21,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFFF9800),
                activeTrackColor = Color(0xFFFF9800),
            ),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("10s", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
            Text("60s", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
            Text("120s", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
        }
        Text(stringResource(R.string.settings_turn_timer_hint), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Spacer(Modifier.height(20.dp))
        SectionTitle(stringResource(R.string.settings_app_experience))
        ToggleRow(stringResource(R.string.settings_sound), soundOn) { scope.launch { prefs.setSoundEffectsEnabled(it) } }
        ToggleRow(stringResource(R.string.settings_haptics), hapticsOn) { scope.launch { prefs.setHapticFeedbackEnabled(it) } }
        Spacer(Modifier.height(8.dp))
        Text("Theme", color = Color.White, fontWeight = FontWeight.Medium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.settings_theme_midnight),
                selected = themePref == AppThemePreference.MIDNIGHT,
                onClick = { scope.launch { prefs.setAppThemePreference(AppThemePreference.MIDNIGHT) } },
            )
            ThemeCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.settings_theme_twilight),
                selected = themePref == AppThemePreference.TWILIGHT,
                onClick = { scope.launch { prefs.setAppThemePreference(AppThemePreference.TWILIGHT) } },
            )
        }
        Spacer(Modifier.height(12.dp))
        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF1A1A1A), modifier = Modifier.fillMaxWidth()) {
            Row(
                Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("🌐", modifier = Modifier.padding(end = 8.dp))
                Text(stringResource(R.string.settings_language), color = Color.White, modifier = Modifier.weight(1f))
                Text("English ›", color = Color.Gray)
            }
        }
        Spacer(Modifier.height(20.dp))
        SectionTitle(stringResource(R.string.settings_data_legal))
        TextButtonLink(stringResource(R.string.settings_privacy)) { uriHandler.openUri("https://example.com/privacy") }
        TextButtonLink(stringResource(R.string.settings_terms)) { uriHandler.openUri("https://example.com/terms") }
        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF2A2510), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("⚠️ ${stringResource(R.string.settings_adult_card)}", color = Color(0xFFFFC107), fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.settings_adult_card_body), color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onResetSession, modifier = Modifier.fillMaxWidth()) {
            androidx.compose.material3.Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text(stringResource(R.string.settings_reset_session))
        }
        OutlinedButton(
            onClick = { scope.launch { prefs.clearFavorites() } },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
        ) {
            androidx.compose.material3.Icon(Icons.Filled.DeleteForever, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text(stringResource(R.string.settings_clear_favorites))
        }
        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.settings_version_fmt, BuildConfig.VERSION_NAME),
            color = Color.Gray,
            style = MaterialTheme.typography.labelMedium,
        )
        Text(stringResource(R.string.settings_made_with), color = Color.Gray, style = MaterialTheme.typography.labelSmall)
    }

    if (showClimaxDialog) {
        AlertDialog(
            onDismissRequest = { showClimaxDialog = false },
            title = { Text(stringResource(R.string.climax_lock_title)) },
            text = { Text(stringResource(R.string.climax_lock_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUnlockClimax()
                        scope.launch { prefs.setDefaultIntensity(DefaultIntensity.EXTREME.storageValue) }
                        showClimaxDialog = false
                    },
                ) {
                    Text(stringResource(R.string.confirm_unlock))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClimaxDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = Color.Gray, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
private fun IntensityRow(selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf(
            DefaultIntensity.MILD.storageValue to stringResource(R.string.intensity_mild),
            DefaultIntensity.SPICY.storageValue to stringResource(R.string.intensity_spicy),
            DefaultIntensity.EXTREME.storageValue to stringResource(R.string.intensity_extreme),
        ).forEach { (value, label) ->
            val sel = selected == value
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (sel) Color(0xFF3A2A10) else Color(0xFF1A1A1A),
                border = if (sel) BorderStroke(2.dp, Color(0xFFFF9800)) else null,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(value) },
            ) {
                Text(
                    label,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                    color = if (sel) Color(0xFFFF9800) else Color.White,
                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
private fun ThemeCard(
    modifier: Modifier,
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) Color(0xFF1A3D1A) else Color(0xFF1A1A1A),
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) Text("✓", color = Color(0xFF4CAF50))
            }
            Text(title, color = Color.White, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = Color.White, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun TextButtonLink(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF64B5F6)),
    ) {
        Text(text)
    }
}
