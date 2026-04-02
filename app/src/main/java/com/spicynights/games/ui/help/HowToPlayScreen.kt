package com.spicynights.games.ui.help

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spicynights.games.R
import com.spicynights.games.ui.theme.NeonTokens
import com.spicynights.games.ui.theme.themeScreenBackgroundBrush

@Composable
fun HowToPlayScreen(
    onGoToSettings: () -> Unit,
) {
    val expanded = remember { mutableStateListOf(false, false, false, false) }
    val guides = listOf(
        R.string.game_never to "🥂",
        R.string.game_truth_dare to "🎭",
        R.string.game_spicy_spinner to "🎡",
        R.string.game_wyr to "⚖",
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeScreenBackgroundBrush())
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_spicy_night_logo),
            contentDescription = stringResource(R.string.cd_app_logo),
            modifier = Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(20.dp)),
            contentScale = ContentScale.Crop,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.how_to_play_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
        )
        Text(
            stringResource(R.string.how_to_play_subtitle),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.how_to_master_title), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.how_to_master_body), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
        Spacer(Modifier.height(20.dp))
        Text(stringResource(R.string.game_guides), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        guides.forEachIndexed { index, (titleRes, emoji) ->
            val isOpen = expanded[index]
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        expanded[index] = !expanded[index]
                    },
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(emoji, modifier = Modifier.padding(end = 8.dp))
                        Text(stringResource(titleRes), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                        Icon(Icons.Filled.ExpandMore, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (isOpen) {
                        Text(
                            stringResource(R.string.mode_placeholder_body),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(stringResource(R.string.intensity_levels), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                IntensityLine(NeonTokens.NeonCyan, stringResource(R.string.intensity_mild), "Icebreakers and light fun.")
                IntensityLine(NeonTokens.NeonMagenta, stringResource(R.string.intensity_spicy), "Flirty and suggestive.")
                IntensityLine(Color(0xFFE040FB), stringResource(R.string.intensity_extreme), "Explicit and wild.")
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(stringResource(R.string.house_rules), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
            Text(
                "Everyone gets 1 free skip per game.",
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(16.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
            Text(
                "Establish a safe word to end a turn immediately.",
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(16.dp),
            )
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onGoToSettings,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Icon(Icons.Filled.Settings, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text(stringResource(R.string.go_to_settings), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun IntensityLine(dot: Color, title: String, desc: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(dot, CircleShape),
        )
        Column(Modifier.padding(start = 10.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            Text(desc, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}
