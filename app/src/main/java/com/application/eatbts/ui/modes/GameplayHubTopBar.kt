package com.application.eatbts.ui.modes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.application.eatbts.R
import com.application.eatbts.ui.hub.HubLandingColors
import com.application.eatbts.ui.theme.themeHubPrimaryText

/** "THE" / gradient "SPICY" / "SPINNER" — same treatment as the Spicy Spinner gameplay headline. */
@Composable
fun SpicySpinnerBarTitle(
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleSmall,
) {
    val spicyBrush =
        Brush.horizontalGradient(
            listOf(HubLandingColors.BrandPurple, HubLandingColors.SpicyPink),
        )
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.spicy_title_the),
            style = style,
            fontWeight = FontWeight.Bold,
            color = themeHubPrimaryText(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = stringResource(R.string.spicy_title_spicy),
            style = style.merge(TextStyle(brush = spicyBrush)),
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = stringResource(R.string.spicy_title_spinner),
            style = style,
            fontWeight = FontWeight.Bold,
            color = themeHubPrimaryText(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun GameplayHubTopBar(
    title: String,
    onOpenMenu: () -> Unit,
    onNotifications: () -> Unit = {},
    modifier: Modifier = Modifier,
    centerTitle: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onOpenMenu) {
            Icon(
                Icons.Filled.Menu,
                contentDescription = stringResource(R.string.cd_hub_menu),
                tint = themeHubPrimaryText(),
            )
        }
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            if (centerTitle != null) {
                centerTitle()
            } else {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = HubLandingColors.BrandPurple,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                )
            }
        }
        IconButton(onClick = onNotifications) {
            Icon(
                Icons.Filled.Notifications,
                contentDescription = stringResource(R.string.cd_hub_notifications),
                tint = themeHubPrimaryText(),
            )
        }
    }
}
