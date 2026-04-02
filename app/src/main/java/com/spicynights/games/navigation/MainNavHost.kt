package com.spicynights.games.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.spicynights.games.data.GameConfig
import com.spicynights.games.data.local.AppPreferencesRepository
import com.spicynights.games.session.SessionStateHolder
import com.spicynights.games.ui.help.HowToPlayScreen
import com.spicynights.games.ui.hub.GameHubScreen
import com.spicynights.games.ui.modes.SpicySpinnerGameplayScreen
import com.spicynights.games.ui.modes.NeverGameplayScreen
import com.spicynights.games.ui.modes.WyrGameplayScreen
import com.spicynights.games.ui.navigation.AppBottomBar
import com.spicynights.games.ui.navigation.BottomNavStyle
import com.spicynights.games.ui.session.SessionSetupScreen
import com.spicynights.games.ui.settings.SettingsScreen

@Composable
fun MainNavHost(
    navController: NavHostController,
    prefs: AppPreferencesRepository,
    extremeUnlocked: Boolean,
    onUnlockExtreme: () -> Unit,
    onStartGame: (GameConfig) -> Unit,
    modifier: Modifier = Modifier,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.Hub
    val turnTimerSeconds by prefs.turnTimerSeconds.collectAsStateWithLifecycle(initialValue = 30)

    val bottomStyle = when (currentRoute) {
        Routes.NeverGameplay, Routes.SpicySpinnerGameplay, Routes.WyrGameplay -> BottomNavStyle.GAMEPLAY
        Routes.Settings -> BottomNavStyle.SETTINGS_APP
        else -> BottomNavStyle.HUB
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            AppBottomBar(
                style = bottomStyle,
                selectedRoute = currentRoute,
                onNavigate = { route ->
                    when (route) {
                        Routes.Hub -> navController.navigate(Routes.Hub) {
                            launchSingleTop = true
                            popUpTo(Routes.Hub) { inclusive = false }
                        }
                        Routes.Settings -> navController.navigate(Routes.Settings) { launchSingleTop = true }
                        "decks_stub", "saved_stub", "profile_stub", "leaderboard_stub", "play_stub" -> {
                            navController.navigate(Routes.Hub) { launchSingleTop = true }
                        }
                        else -> navController.navigate(Routes.Hub) { launchSingleTop = true }
                    }
                },
            )
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Hub,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            composable(Routes.Hub) {
                GameHubScreen(
                    onOpenMenu = { },
                    onQuickSession = {
                        navController.navigate(sessionSetupRoute(SessionGameMode.QUICK_SESSION))
                    },
                    onGameNever = {
                        navController.navigate(sessionSetupRoute(SessionGameMode.NEVER))
                    },
                    onGameTruthDare = {
                        navController.navigate(sessionSetupRoute(SessionGameMode.TRUTH_DARE))
                    },
                    onGameSpicySpinner = {
                        navController.navigate(sessionSetupRoute(SessionGameMode.SPICY_SPINNER))
                    },
                    onGameWyr = {
                        navController.navigate(sessionSetupRoute(SessionGameMode.WYR))
                    },
                    onHowToPlay = { navController.navigate(Routes.HowToPlay) },
                    onFavorites = { },
                    onSettings = { navController.navigate(Routes.Settings) },
                )
            }
            composable(
                route = Routes.SessionSetup,
                arguments = listOf(
                    navArgument("gameMode") { type = NavType.StringType },
                ),
            ) { entry ->
                val arg = entry.arguments?.getString("gameMode")
                val mode = SessionGameMode.fromRouteArg(arg)
                SessionSetupScreen(
                    gameMode = mode,
                    extremeUnlocked = extremeUnlocked,
                    onUnlockExtreme = onUnlockExtreme,
                    prefs = prefs,
                    defaultTurnTimerSeconds = turnTimerSeconds,
                    onBack = { navController.popBackStack() },
                    onStartTruthDare = { config ->
                        onStartGame(config)
                        navController.popBackStack()
                    },
                    onStartInAppMode = { snapshot ->
                        SessionStateHolder.pending = snapshot
                        navController.popBackStack()
                        when (mode) {
                            SessionGameMode.NEVER ->
                                navController.navigate(Routes.NeverGameplay) { launchSingleTop = true }
                            SessionGameMode.SPICY_SPINNER ->
                                navController.navigate(Routes.SpicySpinnerGameplay) { launchSingleTop = true }
                            SessionGameMode.WYR ->
                                navController.navigate(Routes.WyrGameplay) { launchSingleTop = true }
                            else -> {}
                        }
                    },
                )
            }
            composable(Routes.NeverGameplay) { NeverGameplayScreen() }
            composable(Routes.SpicySpinnerGameplay) {
                SpicySpinnerGameplayScreen(
                    prefs = prefs,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.WyrGameplay) { WyrGameplayScreen() }
            composable(Routes.HowToPlay) {
                HowToPlayScreen(
                    onGoToSettings = {
                        navController.navigate(Routes.Settings) {
                            popUpTo(Routes.Hub) { inclusive = false }
                        }
                    },
                )
            }
            composable(Routes.Settings) {
                SettingsScreen(
                    prefs = prefs,
                    extremeUnlocked = extremeUnlocked,
                    onUnlockExtreme = onUnlockExtreme,
                    onResetSession = { },
                )
            }
        }
    }
}
