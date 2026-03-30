package com.spicynights.games.ui.modes

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spicynights.games.R
import com.spicynights.games.session.SessionStateHolder

@Composable
fun NeverGameplayScreen() {
    val snap = SessionStateHolder.pending
    SessionStateHolder.pending = null
    val bg = Brush.verticalGradient(listOf(Color(0xFF121218), Color(0xFF0A0A10)))
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text("I Have Never", style = MaterialTheme.typography.titleLarge, color = Color(0xFF4DD0E1), fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Surface(shape = RoundedCornerShape(24.dp), color = Color(0xFF2A2A32)) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("👤", modifier = Modifier.padding(end = 8.dp))
                Column {
                    Text("CURRENT TURN", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("Sarah's Turn", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        val sessionLine = snap?.let { s ->
            buildString {
                append(s.intensityLabel)
                append(" • ")
                append(if (s.drinkingRulesOn) "Drinking On" else "Drinking Off")
            }
        } ?: "Extra Spicy • Drinking Rules On"
        Text(sessionLine, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(16.dp))
        Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF2C2C35), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("NEVER HAVE I EVER…", color = Color(0xFF4DD0E1), style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.never_sample_prompt), color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(12.dp))
                Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFF1E2A30)) {
                    Text(
                        "🥂  ${stringResource(R.string.never_sip_hint)}",
                        modifier = Modifier.padding(12.dp),
                        color = Color(0xFF4DD0E1),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Guilty", color = Color.White)
            Text("Who did it? ▼", color = Color.White)
            Text("Innocent", color = Color.White)
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = {}, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)) {
            Text("Next Prompt →")
        }
    }
}

@Composable
fun DirtyDiceGameplayScreen() {
    val snap = SessionStateHolder.pending
    SessionStateHolder.pending = null
    val bg = Brush.verticalGradient(listOf(Color(0xFF180818), Color(0xFF0D0510)))
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Dirty Dice", color = Color(0xFFFF2E95), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            snap?.playerNames?.firstOrNull()?.let { name -> "${name}'s Turn" } ?: "Mike's Turn",
            color = Color.White,
        )
        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(Color(0xFFFF2E95), Color(0xFF7C4DFF)))),
            contentAlignment = Alignment.Center,
        ) {
            Text("⚄", style = MaterialTheme.typography.displayLarge, color = Color.White)
        }
        Text(stringResource(R.string.dice_tap_to_roll), color = Color.Gray, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(24.dp))
        Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF2C2C35), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Rolled a 5", color = Color(0xFFFF2E95), style = MaterialTheme.typography.labelMedium)
                    Text("🔥 Spicy", color = Color(0xFFFF9800))
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.never_sample_prompt),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                Text("Group Action   Mandatory", color = Color.Red, style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {}, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333))) {
                Text("Re-roll (2)")
            }
            Button(onClick = {}, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))) {
                Text("Next Player")
            }
        }
    }
}

@Composable
fun WyrGameplayScreen() {
    val snap = SessionStateHolder.pending
    SessionStateHolder.pending = null
    var debateOn by remember { mutableStateOf(snap?.turnTimerOn == true) }
    val bg = Brush.verticalGradient(listOf(Color(0xFF101010), Color(0xFF080808)))
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text("Would You Rather", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.wyr_spicy_deck), color = Color.Gray, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.debate_timer), color = Color.White, modifier = Modifier.weight(1f))
            Switch(checked = debateOn, onCheckedChange = { debateOn = it })
        }
        Spacer(Modifier.height(16.dp))
        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF2A2A2E), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("A", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                Text(stringResource(R.string.wyr_option_a), color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        Text("vs", modifier = Modifier.align(Alignment.CenterHorizontally).padding(8.dp), color = Color.Gray)
        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF2A2A2E), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("B", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                Text(stringResource(R.string.wyr_option_b), color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) {
                Text("Skip", color = Color.White)
            }
            Button(onClick = {}, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))) {
                Text("Next Card →")
            }
        }
    }
}
