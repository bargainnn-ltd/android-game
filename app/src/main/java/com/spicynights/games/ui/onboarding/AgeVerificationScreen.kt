package com.spicynights.games.ui.onboarding

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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import com.spicynights.games.ui.theme.NeonTokens

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
            .background(NeonTokens.screenBackgroundBrush())
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .testTag("age_verification_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = NeonTokens.GlassFill,
                border = BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        listOf(NeonTokens.NeonMagenta.copy(alpha = 0.7f), NeonTokens.NeonCyan.copy(alpha = 0.4f)),
                    ),
                ),
            ) {
                Text(
                    text = stringResource(R.string.age_adults_only),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = NeonTokens.TextPrimary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
            Spacer(Modifier.size(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = NeonTokens.NeonMagenta.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, NeonTokens.NeonMagenta.copy(alpha = 0.85f)),
            ) {
                Text(
                    text = "18+",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = NeonTokens.NeonMagenta,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.age_verify_banner),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = NeonTokens.TextPrimary,
        )
        Text(
            text = stringResource(R.string.age_are_you_18),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = NeonTokens.NeonCyan,
            modifier = Modifier.padding(top = 6.dp),
        )
        Spacer(Modifier.height(16.dp))
        Image(
            painter = painterResource(R.drawable.ic_spicy_night_logo),
            contentDescription = stringResource(R.string.cd_app_logo),
            modifier = Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, NeonTokens.NeonMagenta.copy(alpha = 0.55f), RoundedCornerShape(20.dp)),
            contentScale = ContentScale.Crop,
        )
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = stringResource(R.string.spicynights_tagline),
            style = MaterialTheme.typography.labelMedium,
            color = NeonTokens.TextMuted,
        )
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = Color(0xFFFF5252))
            Text(
                text = stringResource(R.string.age_verification_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = NeonTokens.TextPrimary,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Box(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .height(3.dp)
                .fillMaxWidth(0.35f)
                .background(
                    Brush.horizontalGradient(
                        listOf(NeonTokens.NeonMagenta, NeonTokens.NeonCyan),
                    ),
                    RoundedCornerShape(2.dp),
                ),
        )
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = NeonTokens.GlassFill,
            border = BorderStroke(
                1.dp,
                NeonTokens.glassBorderBrush(NeonTokens.NeonMagenta),
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.age_disclaimer_main),
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeonTokens.TextPrimary.copy(alpha = 0.95f),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NeonTokens.BgElevated.copy(alpha = 0.85f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.age_disclaimer_sub),
                        style = MaterialTheme.typography.bodySmall,
                        color = NeonTokens.TextMuted,
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
            color = NeonTokens.TextDim,
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
                            color = NeonTokens.BgElevated.copy(alpha = 0.7f),
                            border = BorderStroke(
                                1.dp,
                                NeonTokens.NeonCyan.copy(alpha = 0.22f),
                            ),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = "$emoji  $label",
                                style = MaterialTheme.typography.labelMedium,
                                color = NeonTokens.TextPrimary.copy(alpha = 0.92f),
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
                colors = CheckboxDefaults.colors(
                    checkedColor = NeonTokens.NeonMagenta,
                    uncheckedColor = NeonTokens.TextMuted,
                    checkmarkColor = Color.White,
                ),
            )
            Column(modifier = Modifier.padding(start = 4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.age_consent_prefix),
                        style = MaterialTheme.typography.bodySmall,
                        color = NeonTokens.TextPrimary.copy(alpha = 0.92f),
                    )
                    TextButton(
                        onClick = { uriHandler.openUri(termsUrl) },
                        modifier = Modifier.padding(0.dp),
                    ) {
                        Text(stringResource(R.string.link_terms), color = NeonTokens.NeonCyan)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("and ", color = NeonTokens.TextPrimary.copy(alpha = 0.92f), style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { uriHandler.openUri(privacyUrl) }) {
                        Text(stringResource(R.string.link_privacy), color = NeonTokens.NeonCyan)
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
                containerColor = NeonTokens.NeonMagenta,
                disabledContainerColor = NeonTokens.NeonMagenta.copy(alpha = 0.35f),
            ),
        ) {
            Text(stringResource(R.string.age_enter), fontWeight = FontWeight.Bold)
        }
        OutlinedButton(
            onClick = onUnderAgeExit,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50),
            border = BorderStroke(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(NeonTokens.NeonMagenta.copy(alpha = 0.85f), NeonTokens.NeonCyan.copy(alpha = 0.5f)),
                ),
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = NeonTokens.TextPrimary,
            ),
        ) {
            Text(stringResource(R.string.age_exit))
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.age_footer_addiction),
            style = MaterialTheme.typography.labelSmall,
            color = NeonTokens.TextDim,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.age_footer_links),
            style = MaterialTheme.typography.labelSmall,
            color = NeonTokens.NeonCyan.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
