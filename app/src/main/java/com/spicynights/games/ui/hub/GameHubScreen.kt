package com.spicynights.games.ui.hub

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spicynights.games.R

@Composable
fun GameHubScreen(
    onOpenMenu: () -> Unit,
    onNotifications: () -> Unit = {},
    onGameNever: () -> Unit,
    onGameTruthDare: () -> Unit,
    onGameSpicySpinner: () -> Unit,
    onGameWyr: () -> Unit,
    onCustomDeck: () -> Unit,
) {
    val bg = Brush.verticalGradient(
        listOf(HubLandingColors.Black, HubLandingColors.Charcoal, HubLandingColors.Black),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("game_hub_screen"),
        ) {
            HubTopBar(
                onOpenMenu = onOpenMenu,
                onNotifications = onNotifications,
            )
            Spacer(Modifier.height(20.dp))
            HubHero()
            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HubNeverCard(
                    onPlay = onGameNever,
                    modifier = Modifier.weight(1f),
                )
                HubSpicyCard(
                    onPlay = onGameSpicySpinner,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HubWyrCard(
                    onPlay = onGameWyr,
                    modifier = Modifier.weight(1f),
                )
                HubTruthDareCardBody(
                    onPlay = onGameTruthDare,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(10.dp))
            HubTruthDareStrip()
            Spacer(Modifier.height(20.dp))
            HubCustomDeckCard(onStartBuilding = onCustomDeck)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HubTopBar(
    onOpenMenu: () -> Unit,
    onNotifications: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onOpenMenu) {
            Icon(
                Icons.Filled.Menu,
                contentDescription = stringResource(R.string.cd_hub_menu),
                tint = HubLandingColors.White,
            )
        }
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                color = HubLandingColors.BrandPurple,
                letterSpacing = 0.5.sp,
                maxLines = 1,
            )
        }
        IconButton(onClick = onNotifications) {
            Icon(
                Icons.Filled.Notifications,
                contentDescription = stringResource(R.string.cd_hub_notifications),
                tint = HubLandingColors.White,
            )
        }
    }
}

@Composable
private fun HubHero() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.hub_landing_session_level),
            style = MaterialTheme.typography.labelMedium,
            color = HubLandingColors.BrandPurple.copy(alpha = 0.85f),
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.hub_landing_headline_1),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = HubLandingColors.White,
            letterSpacing = 2.sp,
        )
        Text(
            text = stringResource(R.string.hub_landing_headline_2),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = HubLandingColors.BrandPurple,
            letterSpacing = 2.sp,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.hub_landing_subhead),
            style = MaterialTheme.typography.bodyMedium,
            color = HubLandingColors.BodyGrey,
            lineHeight = 22.sp,
        )
    }
}

@Composable
private fun HubNeverCard(
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, HubLandingColors.BrandPurple.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
            .testTag("hub_game_never"),
    ) {
        Column(
            Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF1A1520),
                            HubLandingColors.Surface,
                        ),
                    ),
                    shape = RoundedCornerShape(24.dp),
                )
                .padding(12.dp),
        ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    HubPill(
                        text = stringResource(R.string.hub_landing_tag_classic),
                        container = HubLandingColors.BrandPurple,
                        content = Color.White,
                    )
                    HubPill(
                        text = stringResource(R.string.hub_landing_tag_players),
                        container = HubLandingColors.SurfaceElevated,
                        content = HubLandingColors.White,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.hub_landing_never_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = HubLandingColors.White,
                    letterSpacing = 0.5.sp,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.hub_landing_never_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = HubLandingColors.BodyGrey,
                    lineHeight = 18.sp,
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onPlay,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HubLandingColors.BrandPurple,
                        contentColor = Color.White,
                    ),
                ) {
                    Text(
                        stringResource(R.string.hub_landing_play_now),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                    )
                }
        }
    }
}

@Composable
private fun HubSpicyCard(
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = HubLandingColors.Surface,
        modifier = modifier
            .fillMaxWidth()
            .testTag("hub_game_spicy"),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.hub_landing_high_stakes),
                    style = MaterialTheme.typography.labelMedium,
                    color = HubLandingColors.HighStakesRed,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                )
                Icon(
                    Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = HubLandingColors.SpicyOrange,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.hub_landing_spicy_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = HubLandingColors.White,
                letterSpacing = 0.5.sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.hub_landing_spicy_desc),
                style = MaterialTheme.typography.bodySmall,
                color = HubLandingColors.BodyGrey,
                lineHeight = 18.sp,
            )
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = onPlay,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(2.dp, HubLandingColors.SpicyOrange),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = HubLandingColors.SpicyOrange,
                ),
            ) {
                Text(
                    stringResource(R.string.hub_landing_play_now),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )
            }
        }
    }
}

@Composable
private fun HubWyrCard(
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = HubLandingColors.Surface,
        modifier = modifier
            .fillMaxWidth()
            .testTag("hub_game_wyr"),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.SwapHoriz,
                    contentDescription = null,
                    tint = HubLandingColors.TrendingYellow,
                    modifier = Modifier.size(22.dp),
                )
                HubPill(
                    text = stringResource(R.string.hub_landing_trending),
                    container = HubLandingColors.TrendingYellow,
                    content = Color(0xFF1A1A1A),
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.hub_landing_wyr_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = HubLandingColors.White,
                letterSpacing = 0.5.sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.hub_landing_wyr_desc),
                style = MaterialTheme.typography.bodySmall,
                color = HubLandingColors.BodyGrey,
                lineHeight = 18.sp,
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onPlay,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HubLandingColors.SurfaceElevated,
                    contentColor = HubLandingColors.White,
                ),
            ) {
                Text(
                    stringResource(R.string.hub_landing_play_now),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )
            }
        }
    }
}

@Composable
private fun HubTruthDareCardBody(
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = HubLandingColors.Surface,
        modifier = modifier
            .fillMaxWidth()
            .testTag("hub_game_truth_dare"),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = stringResource(R.string.hub_landing_community_pick),
                style = MaterialTheme.typography.labelMedium,
                color = HubLandingColors.BrandPurple,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.hub_landing_truth_dare_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = HubLandingColors.White,
                letterSpacing = 0.5.sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.hub_landing_truth_dare_desc),
                style = MaterialTheme.typography.bodySmall,
                color = HubLandingColors.BodyGrey,
                lineHeight = 18.sp,
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onPlay,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HubLandingColors.BrandPurple,
                    contentColor = Color.White,
                ),
            ) {
                Text(
                    stringResource(R.string.hub_landing_play_now),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )
            }
        }
    }
}

@Composable
private fun HubTruthDareStrip() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = HubLandingColors.SurfaceElevated,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.hub_landing_truth_strip_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = HubLandingColors.White,
                letterSpacing = 1.sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.hub_landing_truth_strip_sub),
                style = MaterialTheme.typography.labelMedium,
                color = HubLandingColors.TextDim,
                letterSpacing = 1.sp,
            )
        }
    }
}

@Composable
private fun HubCustomDeckCard(onStartBuilding: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            HubLandingColors.BrandPurpleDark.copy(alpha = 0.5f),
                            HubLandingColors.Surface,
                            HubLandingColors.BrandPurple.copy(alpha = 0.25f),
                        ),
                    ),
                )
                .padding(20.dp),
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.hub_landing_deck_title_1),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = HubLandingColors.White,
                    )
                    Text(
                        text = stringResource(R.string.hub_landing_deck_title_2),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = HubLandingColors.DeckGold,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.hub_landing_deck_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = HubLandingColors.BodyGrey,
                    lineHeight = 22.sp,
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onStartBuilding,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black,
                    ),
                ) {
                    Text(
                        stringResource(R.string.hub_landing_start_building),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun HubPill(
    text: String,
    container: Color,
    content: Color,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = content,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(container)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        letterSpacing = 0.8.sp,
    )
}
