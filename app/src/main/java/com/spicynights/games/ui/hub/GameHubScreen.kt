package com.spicynights.games.ui.hub

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.spicynights.games.R
import com.spicynights.games.ui.theme.ModColors

private val CosmicTop = Color(0xFF0D1528)
private val CosmicMid = Color(0xFF1A0F35)
private val CosmicBottom = Color(0xFF0A0618)
private val TitleGradientA = Color(0xFFE1BEE7)
private val TitleGradientB = Color(0xFF64B5F6)

private data class HubGameTile(
    val titleRes: Int,
    val descRes: Int,
    val emoji: String,
    val accent: Color,
    val onClick: () -> Unit,
    val testTag: String? = null,
)

private data class FeedItem(val titleRes: Int, val subtitleRes: Int, val emoji: String)

@Composable
fun GameHubScreen(
    onOpenMenu: () -> Unit,
    onGameNever: () -> Unit,
    onGameTruthDare: () -> Unit,
    onGameSpicySpinner: () -> Unit,
    onGameWyr: () -> Unit,
    onHowToPlay: () -> Unit,
    onFavorites: () -> Unit,
    onSettings: () -> Unit,
) {
    val games = remember {
        listOf(
            HubGameTile(
                titleRes = R.string.game_never,
                descRes = R.string.hub_tile_never_desc,
                emoji = "🥂",
                accent = Color(0xFFCE93D8),
                onClick = onGameNever,
                testTag = "hub_game_never",
            ),
            HubGameTile(
                titleRes = R.string.game_truth_dare,
                descRes = R.string.hub_tile_truth_dare_desc,
                emoji = "🎭",
                accent = Color(0xFF7986CB),
                onClick = onGameTruthDare,
            ),
            HubGameTile(
                titleRes = R.string.game_spicy_spinner,
                descRes = R.string.hub_tile_spicy_desc,
                emoji = "🎡",
                accent = Color(0xFFEF5350),
                onClick = onGameSpicySpinner,
            ),
            HubGameTile(
                titleRes = R.string.game_wyr,
                descRes = R.string.hub_tile_wyr_desc,
                emoji = "⚖",
                accent = Color(0xFFFFB74D),
                onClick = onGameWyr,
            ),
        )
    }

    val feedItems = remember {
        listOf(
            FeedItem(R.string.hub_feed_new_title, R.string.hub_feed_new_sub, "🥂"),
            FeedItem(R.string.hub_feed_popular_title, R.string.hub_feed_popular_sub, "🎭"),
            FeedItem(R.string.hub_feed_icebreaker_title, R.string.hub_feed_icebreaker_sub, "✨"),
        )
    }

    val bgBrush = remember {
        Brush.verticalGradient(
            listOf(
                CosmicTop,
                Color(0xFF15102A),
                CosmicMid,
                CosmicBottom,
            ),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush),
    ) {
        StarryBackdrop(Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("game_hub_screen"),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_spicy_night_logo),
                    contentDescription = stringResource(R.string.cd_app_logo),
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall.merge(
                        TextStyle(
                            brush = Brush.linearGradient(
                                listOf(TitleGradientA, TitleGradientB),
                            ),
                        ),
                    ),
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.hub_welcome_line),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.hub_ready_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Text(
                text = stringResource(R.string.hub_ready_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.72f),
                modifier = Modifier.padding(top = 6.dp),
            )
            Spacer(Modifier.height(18.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                games.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        row.forEach { tile ->
                            HubGameGlassCard(
                                tile = tile,
                                modifier = Modifier
                                    .weight(1f)
                                    .then(
                                        tile.testTag?.let { Modifier.testTag(it) } ?: Modifier,
                                    ),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
            Text(
                stringResource(R.string.quick_links),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.45f),
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                QuickLinkChip("?", stringResource(R.string.how_to_play_title), onHowToPlay)
                QuickLinkChip("⚙", stringResource(R.string.nav_settings), onSettings)
                QuickLinkChip("◆", stringResource(R.string.hub_link_premium), onFavorites)
            }

            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onFavorites() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    stringResource(R.string.hub_feed_section),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                )
            }
            Spacer(Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(end = 8.dp),
            ) {
                items(feedItems) { item ->
                    ActivityFeedCard(item)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StarryBackdrop(modifier: Modifier = Modifier) {
    val starAlpha = 0.28f
    val positions = remember {
        listOf(
            0.04f to 0.06f, 0.14f to 0.11f, 0.22f to 0.04f, 0.31f to 0.14f, 0.42f to 0.08f,
            0.55f to 0.12f, 0.68f to 0.05f, 0.78f to 0.15f, 0.9f to 0.07f, 0.95f to 0.18f,
            0.08f to 0.22f, 0.19f to 0.28f, 0.35f to 0.24f, 0.48f to 0.3f, 0.62f to 0.26f,
            0.75f to 0.32f, 0.88f to 0.25f, 0.12f to 0.42f, 0.28f to 0.48f, 0.52f to 0.45f,
            0.72f to 0.5f, 0.25f to 0.62f, 0.45f to 0.68f, 0.65f to 0.72f, 0.85f to 0.65f,
        )
    }
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        positions.forEach { (nx, ny) ->
            drawCircle(
                color = Color.White.copy(alpha = starAlpha),
                radius = 1.3f,
                center = Offset(nx * w, ny * h),
            )
        }
    }
}

@Composable
private fun HubGameGlassCard(
    tile: HubGameTile,
    modifier: Modifier = Modifier,
) {
    val accent = tile.accent
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.07f),
        modifier = modifier
            .heightIn(min = 168.dp)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(accent.copy(alpha = 0.55f), accent.copy(alpha = 0.15f)),
                ),
                shape = RoundedCornerShape(20.dp),
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = tile.onClick),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(tile.emoji, style = MaterialTheme.typography.headlineMedium)
                PlayBadge(accent)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(tile.titleRes),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.38f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    stringResource(tile.descRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.82f),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
                )
            }
        }
    }
}

@Composable
private fun PlayBadge(accent: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.linearGradient(
                    listOf(accent.copy(alpha = 0.95f), accent.copy(alpha = 0.45f)),
                ),
            )
            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Filled.PlayArrow,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun QuickLinkChip(symbol: String, label: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.05f),
        modifier = Modifier
            .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
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
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun ActivityFeedCard(item: FeedItem) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.06f),
        modifier = Modifier
            .width(200.dp)
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(item.emoji, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(item.titleRes),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Text(
                stringResource(item.subtitleRes),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.65f),
                modifier = Modifier.padding(top = 4.dp),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
