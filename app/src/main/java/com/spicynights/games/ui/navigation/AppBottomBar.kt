package com.spicynights.games.ui.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.spicynights.games.R
import com.spicynights.games.navigation.Routes
import com.spicynights.games.ui.theme.NeonTokens

enum class BottomNavStyle {
    HUB,
    GAMEPLAY,
    SETTINGS_APP,
}

@Composable
fun AppBottomBar(
    style: BottomNavStyle,
    selectedRoute: String,
    onNavigate: (String) -> Unit,
) {
    when (style) {
        BottomNavStyle.HUB -> HubBottomBar(selectedRoute, onNavigate)
        BottomNavStyle.GAMEPLAY -> GameplayBottomBar(selectedRoute, onNavigate)
        BottomNavStyle.SETTINGS_APP -> SettingsAppBottomBar(selectedRoute, onNavigate)
    }
}

@Composable
private fun HubBottomBar(
    selectedRoute: String,
    onNavigate: (String) -> Unit,
) {
    val items = listOf(
        Triple(Routes.Hub, Icons.Filled.Home, R.string.nav_hub),
    )
    NavigationBar(
        containerColor = NeonTokens.NavBarContainer,
        contentColor = NeonTokens.TextPrimary,
    ) {
        items.forEach { (route, icon, labelRes) ->
            val selected = selectedRoute == route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(route) },
                icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp)) },
                label = { Text(stringResource(labelRes)) },
                colors = navItemColors(),
            )
        }
    }
}

@Composable
private fun GameplayBottomBar(
    selectedRoute: String,
    onNavigate: (String) -> Unit,
) {
    val items = listOf(
        Triple(Routes.Hub, Icons.Filled.Explore, R.string.nav_explore),
        Triple(Routes.Settings, Icons.Filled.Settings, R.string.nav_settings),
    )
    NavigationBar(
        containerColor = NeonTokens.NavBarContainer,
        contentColor = NeonTokens.TextPrimary,
    ) {
        items.forEach { (route, icon, labelRes) ->
            NavigationBarItem(
                selected = selectedRoute == route,
                onClick = { onNavigate(route) },
                icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp)) },
                label = { Text(stringResource(labelRes)) },
                colors = navItemColors(),
            )
        }
    }
}

@Composable
private fun SettingsAppBottomBar(
    selectedRoute: String,
    onNavigate: (String) -> Unit,
) {
    val items = listOf(
        Triple(Routes.Hub, Icons.Filled.Explore, R.string.nav_explore),
        Triple(Routes.Settings, Icons.Filled.Settings, R.string.nav_settings),
    )
    NavigationBar(
        containerColor = NeonTokens.NavBarContainer,
        contentColor = NeonTokens.TextPrimary,
    ) {
        items.forEach { (route, icon, labelRes) ->
            NavigationBarItem(
                selected = selectedRoute == route,
                onClick = { onNavigate(route) },
                icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp)) },
                label = { Text(stringResource(labelRes)) },
                colors = navItemColors(),
            )
        }
    }
}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = NeonTokens.NeonMagenta,
    selectedTextColor = NeonTokens.NeonMagenta,
    unselectedIconColor = NeonTokens.TextDim,
    unselectedTextColor = NeonTokens.TextDim,
    indicatorColor = NeonTokens.NavIndicator,
)
