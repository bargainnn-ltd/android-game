package com.application.eatbts.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.application.eatbts.CoupleGamesApp
import com.application.eatbts.data.GameConfig
import com.application.eatbts.data.local.AppPreferencesRepository
import com.application.eatbts.session.OnlineMatchmakingHolder
import com.application.eatbts.session.SessionStateHolder
import com.application.eatbts.ui.help.HowToPlayScreen
import com.application.eatbts.ui.hub.GameHubScreen
import com.application.eatbts.ui.matchmaking.MatchmakingScreen
import com.application.eatbts.ui.modes.SpicySpinnerGameplayScreen
import com.application.eatbts.ui.modes.NeverGameplayScreen
import com.application.eatbts.ui.modes.WyrGameplayScreen
import com.application.eatbts.ui.navigation.AppBottomBar
import com.application.eatbts.ui.navigation.BottomNavStyle
import com.application.eatbts.ui.profile.ProfileScreen
import com.application.eatbts.ui.session.SessionSetupScreen
import com.application.eatbts.ui.settings.SettingsScreen
import kotlinx.coroutines.launch

@Composable
private fun HubLandingContent(
    navController: NavHostController,
    onOpenNotificationSettings: () -> Unit,
) {
    GameHubScreen(
        onOpenMenu = { navController.navigate(Routes.Settings) { launchSingleTop = true } },
        onNotifications = onOpenNotificationSettings,
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
        onCustomDeck = {
            // Custom prompt pool lives under session/settings; Settings is the closest entry until a dedicated builder exists.
            navController.navigate(Routes.Settings) { launchSingleTop = true }
        },
    )
}

@Composable
fun MainNavHost(
    navController: NavHostController,
    app: CoupleGamesApp,
    prefs: AppPreferencesRepository,
    extremeUnlocked: Boolean,
    onUnlockExtreme: () -> Unit,
    onStartGame: (GameConfig) -> Unit,
    onStartOnlineGame: (matchId: String) -> Unit,
    onOpenNotificationSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.Hub
    val turnTimerSeconds by prefs.turnTimerSeconds.collectAsStateWithLifecycle(initialValue = 30)

    val bottomStyle = when (currentRoute) {
        Routes.NeverGameplay, Routes.SpicySpinnerGameplay, Routes.WyrGameplay -> BottomNavStyle.GAMEPLAY
        Routes.Settings -> BottomNavStyle.SETTINGS_APP
        Routes.Matchmaking -> BottomNavStyle.SETTINGS_APP
        else -> BottomNavStyle.HUB
    }

    val bottomSelectedRoute = when (bottomStyle) {
        BottomNavStyle.HUB -> when (currentRoute) {
            Routes.Social -> Routes.Social
            Routes.Store -> Routes.Store
            Routes.Profile -> Routes.Profile
            else -> Routes.Hub
        }
        else -> currentRoute
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            AppBottomBar(
                style = bottomStyle,
                selectedRoute = bottomSelectedRoute,
                onNavigate = { route ->
                    when (route) {
                        Routes.Settings -> navController.navigate(Routes.Settings) { launchSingleTop = true }
                        Routes.Hub -> {
                            if (navController.currentBackStackEntry?.destination?.route != Routes.Hub) {
                                if (!navController.popBackStack(Routes.Hub, inclusive = false, saveState = false)) {
                                    navController.navigate(Routes.Hub) { launchSingleTop = true }
                                }
                            }
                        }
                        Routes.Social, Routes.Store, Routes.Profile -> {
                            navController.navigate(route) {
                                popUpTo(Routes.Hub) {
                                    saveState = true
                                    inclusive = false
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        "decks_stub", "saved_stub", "profile_stub", "leaderboard_stub", "play_stub" -> {
                            if (!navController.popBackStack(Routes.Hub, inclusive = false, saveState = false)) {
                                navController.navigate(Routes.Hub) { launchSingleTop = true }
                            }
                        }
                        else -> {
                            if (!navController.popBackStack(Routes.Hub, inclusive = false, saveState = false)) {
                                navController.navigate(Routes.Hub) { launchSingleTop = true }
                            }
                        }
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
                HubLandingContent(navController, onOpenNotificationSettings)
            }
            composable(Routes.Social) {
                HubLandingContent(navController, onOpenNotificationSettings)
            }
            composable(Routes.Store) {
                HubLandingContent(navController, onOpenNotificationSettings)
            }
            composable(Routes.Profile) {
                ProfileScreen(
                    app = app,
                    onOpenSettings = { navController.navigate(Routes.Settings) { launchSingleTop = true } },
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
                    onStartOnlineMatchmaking = { session ->
                        scope.launch {
                            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                            if (auth.currentUser == null) {
                                runCatching {
                                    app.authRepository.signInAnonymously()
                                    val u = auth.currentUser ?: return@launch
                                    app.playerProfileRepository.upsertPlayer(
                                        userId = u.uid,
                                        name = u.displayName ?: "Guest",
                                        avatarUrl = u.photoUrl?.toString(),
                                    )
                                    app.analyticsLogger.logSignUp("anonymous")
                                }
                            }
                            OnlineMatchmakingHolder.pendingSession = session
                            navController.navigate(Routes.Matchmaking) { launchSingleTop = true }
                        }
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
            composable(Routes.NeverGameplay) {
                NeverGameplayScreen(
                    prefs = prefs,
                    onOpenMenu = { navController.navigate(Routes.Settings) { launchSingleTop = true } },
                )
            }
            composable(Routes.SpicySpinnerGameplay) {
                SpicySpinnerGameplayScreen(
                    prefs = prefs,
                    onBack = { navController.popBackStack() },
                    onOpenMenu = { navController.navigate(Routes.Settings) { launchSingleTop = true } },
                )
            }
            composable(Routes.WyrGameplay) {
                WyrGameplayScreen(
                    prefs = prefs,
                    onOpenMenu = { navController.navigate(Routes.Settings) { launchSingleTop = true } },
                )
            }
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
            composable(Routes.Matchmaking) {
                MatchmakingScreen(
                    app = app,
                    onBack = { navController.popBackStack() },
                    onMatched = { matchId ->
                        onStartOnlineGame(matchId)
                        navController.popBackStack()
                    },
                )
            }
        }
    }
}
