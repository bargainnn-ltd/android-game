package com.application.eatbts.ui.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.application.eatbts.R
import com.application.eatbts.navigation.Routes
import com.application.eatbts.ui.hub.HubLandingColors
import com.application.eatbts.ui.theme.NeonTokens
import com.application.eatbts.ui.theme.themeIsLight

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
        Triple(Routes.Hub, Icons.Filled.SportsEsports, R.string.nav_games),
        Triple(Routes.Social, Icons.Filled.Groups, R.string.nav_social),
        Triple(Routes.Store, Icons.Filled.ShoppingBag, R.string.nav_store),
        Triple(Routes.Profile, Icons.Filled.Person, R.string.nav_profile),
    )
    val scheme = MaterialTheme.colorScheme
    NavigationBar(
        containerColor = if (themeIsLight()) scheme.surface else HubLandingColors.Black,
        contentColor = if (themeIsLight()) scheme.onSurface else HubLandingColors.White,
    ) {
        items.forEach { (route, icon, labelRes) ->
            val selected = selectedRoute == route
            NavigationBarItem(
                modifier = Modifier.testTag("bottom_nav_$route"),
                selected = selected,
                onClick = { onNavigate(route) },
                icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp)) },
                label = { Text(stringResource(labelRes)) },
                colors = hubNavItemColors(),
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
    val scheme = MaterialTheme.colorScheme
    NavigationBar(
        containerColor = if (themeIsLight()) scheme.surface.copy(alpha = 0.98f) else NeonTokens.NavBarContainer,
        contentColor = if (themeIsLight()) scheme.onSurface else NeonTokens.TextPrimary,
    ) {
        items.forEach { (route, icon, labelRes) ->
            NavigationBarItem(
                modifier = Modifier.testTag("bottom_nav_$route"),
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
    val scheme = MaterialTheme.colorScheme
    NavigationBar(
        containerColor = if (themeIsLight()) scheme.surface.copy(alpha = 0.98f) else NeonTokens.NavBarContainer,
        contentColor = if (themeIsLight()) scheme.onSurface else NeonTokens.TextPrimary,
    ) {
        items.forEach { (route, icon, labelRes) ->
            NavigationBarItem(
                modifier = Modifier.testTag("bottom_nav_$route"),
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
private fun hubNavItemColors(): NavigationBarItemColors {
    val scheme = MaterialTheme.colorScheme
    return NavigationBarItemDefaults.colors(
        selectedIconColor = HubLandingColors.BrandPurple,
        selectedTextColor = HubLandingColors.BrandPurple,
        unselectedIconColor = if (themeIsLight()) scheme.onSurfaceVariant else HubLandingColors.TextDim,
        unselectedTextColor = if (themeIsLight()) scheme.onSurfaceVariant else HubLandingColors.TextDim,
        indicatorColor = HubLandingColors.BrandPurple.copy(alpha = 0.28f),
    )
}

@Composable
private fun navItemColors(): NavigationBarItemColors {
    val scheme = MaterialTheme.colorScheme
    return NavigationBarItemDefaults.colors(
        selectedIconColor = NeonTokens.NeonMagenta,
        selectedTextColor = NeonTokens.NeonMagenta,
        unselectedIconColor = if (themeIsLight()) scheme.onSurfaceVariant else NeonTokens.TextDim,
        unselectedTextColor = if (themeIsLight()) scheme.onSurfaceVariant else NeonTokens.TextDim,
        indicatorColor = NeonTokens.NavIndicator,
    )
}
