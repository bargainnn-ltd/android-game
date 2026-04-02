package com.spicynights.games.ui.onboarding

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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spicynights.games.R

private val PurpleGradient = Brush.verticalGradient(
    listOf(Color(0xFF1E1035), Color(0xFF0D1528)),
)

@Composable
fun AgeVerificationScreen(
    onAgeVerified: () -> Unit,
    onUnderAgeExit: () -> Unit,
    termsUrl: String = "https://bargainn.io/terms-of-service",
    privacyUrl: String = "https://bargainn.io/privacy-policy",
) {
    var accepted by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PurpleGradient)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .testTag("age_verification_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(16.dp))
        Image(
            painter = painterResource(R.drawable.ic_spicy_night_logo),
            contentDescription = stringResource(R.string.cd_app_logo),
            modifier = Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, Color(0x66FF2E95), RoundedCornerShape(20.dp)),
            contentScale = ContentScale.Crop,
        )
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = stringResource(R.string.spicynights_tagline),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.55f),
        )
        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = Color(0xFFFF5252))
            Text(
                text = stringResource(R.string.age_verification_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Box(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .height(3.dp)
                .fillMaxWidth(0.35f)
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFFFF2E95), Color(0xFF7C4DFF))),
                    RoundedCornerShape(2.dp),
                ),
        )
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF2C2C35),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.age_disclaimer_main),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.92f),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1A1A22),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.age_disclaimer_sub),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.age_includes_games),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        val games = listOf(
            stringResource(R.string.game_never) to "🥂",
            stringResource(R.string.game_truth_dare) to "🎭",
            stringResource(R.string.game_spicy_spinner) to "🎡",
            stringResource(R.string.game_wyr) to "⚖",
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            games.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    row.forEach { (label, emoji) ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF252530),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = "$emoji  $label",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                maxLines = 2,
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Checkbox(
                checked = accepted,
                onCheckedChange = { accepted = it },
                modifier = Modifier.testTag("age_checkbox"),
            )
            Column(modifier = Modifier.padding(start = 4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.age_consent_prefix),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                    TextButton(
                        onClick = { uriHandler.openUri(termsUrl) },
                        modifier = Modifier.padding(0.dp),
                    ) {
                        Text(stringResource(R.string.link_terms), color = Color(0xFF64B5F6))
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("and ", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { uriHandler.openUri(privacyUrl) }) {
                        Text(stringResource(R.string.link_privacy), color = Color(0xFF64B5F6))
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onAgeVerified,
            enabled = accepted,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("age_enter"),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF2E95),
                disabledContainerColor = Color(0x66FF2E95),
            ),
        ) {
            Text(stringResource(R.string.age_enter), fontWeight = FontWeight.Bold)
        }
        OutlinedButton(
            onClick = onUnderAgeExit,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50),
        ) {
            Text(stringResource(R.string.age_exit), color = Color.White)
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.age_footer_addiction),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.45f),
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.age_footer_links),
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF64B5F6),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
