package com.spicynights.games.ui.hub

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spicynights.games.R
import com.spicynights.games.ui.theme.ModColors

@Composable
fun GameHubScreen(
    onOpenMenu: () -> Unit,
    onQuickSession: () -> Unit,
    onGameNever: () -> Unit,
    onGameTruthDare: () -> Unit,
    onGameDirtyDice: () -> Unit,
    onGameWyr: () -> Unit,
    onHowToPlay: () -> Unit,
    onFavorites: () -> Unit,
    onSettings: () -> Unit,
) {
    val bg = Brush.verticalGradient(listOf(Color(0xFF1A0F28), Color(0xFF120820)))
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("game_hub_screen"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onOpenMenu) {
                Icon(Icons.Outlined.Menu, contentDescription = null, tint = Color.White)
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
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop,
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.hub_game_hub),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
            Box {
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.Notifications, contentDescription = null, tint = Color.White)
                }
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp, end = 8.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF2E95))
                        .align(Alignment.TopEnd),
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.hub_ready_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Text(
            text = stringResource(R.string.hub_ready_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.75f),
            modifier = Modifier.padding(top = 4.dp),
        )
        Spacer(Modifier.height(16.dp))
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF2A1835),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("⚡", style = MaterialTheme.typography.headlineMedium)
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(
                        stringResource(R.string.hub_quick_session),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        stringResource(R.string.hub_quick_session_sub),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                    )
                }
                Button(
                    onClick = onQuickSession,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.testTag("hub_quick_session"),
                ) {
                    Text(stringResource(R.string.hub_start_now))
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        val games = listOf(
            Triple(R.string.game_never, "🥂", Color(0xFF2196F3)) to onGameNever,
            Triple(R.string.game_truth_dare, "🎭", Color(0xFF9C27B0)) to onGameTruthDare,
            Triple(R.string.game_dirty_dice, "🎲", Color(0xFFE91E63)) to onGameDirtyDice,
            Triple(R.string.game_wyr, "⚖", Color(0xFFFFC107)) to onGameWyr,
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            games.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    row.forEach { (triple, onClick) ->
                        val (nameRes, emoji, tint) = triple
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color(0xFF2A1A35),
                            modifier = Modifier
                                .weight(1f)
                                .clickable(onClick = onClick),
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(emoji, style = MaterialTheme.typography.headlineSmall)
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(tint.copy(alpha = 0.25f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            Icons.Filled.PlayArrow,
                                            contentDescription = null,
                                            tint = tint,
                                        )
                                    }
                                }
                                Text(
                                    stringResource(nameRes),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.quick_links),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f),
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickLinkChip("?", stringResource(R.string.how_to_play_title), onHowToPlay)
            QuickLinkChip("♥", stringResource(R.string.nav_saved), onFavorites)
            QuickLinkChip("⚙", stringResource(R.string.nav_settings), onSettings)
        }
    }
}

@Composable
private fun QuickLinkChip(symbol: String, label: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF2C2438),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(symbol, color = ModColors.BubbleYellow)
            Text(
                label,
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}
