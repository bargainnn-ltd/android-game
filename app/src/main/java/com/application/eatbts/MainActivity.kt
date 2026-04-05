package com.application.eatbts

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.application.eatbts.data.GameConfig
import com.application.eatbts.data.local.AppThemePreference
import com.application.eatbts.navigation.MainNavHost
import com.application.eatbts.ui.onboarding.AgeVerificationScreen
import com.application.eatbts.ui.theme.CoupleGamesTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as CoupleGamesApp
        val prefs = app.preferencesRepository

        setContent {
            val appTheme by prefs.appThemePreference.collectAsStateWithLifecycle(
                initialValue = AppThemePreference.MIDNIGHT,
            )
            val light = appTheme == AppThemePreference.LIGHT
            SideEffect {
                val window = this@MainActivity.window
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = light
                    isAppearanceLightNavigationBars = light
                }
                window.navigationBarColor = if (light) Color.White.toArgb() else android.graphics.Color.BLACK
            }
            CoupleGamesTheme(appTheme = appTheme) {
                val scope = rememberCoroutineScope()
                val ageVerified by prefs.ageVerified.collectAsStateWithLifecycle(initialValue = false)
                val extremeUnlocked by prefs.extremeUnlocked.collectAsStateWithLifecycle(initialValue = false)

                val gameLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult(),
                ) { }

                val onlineGameLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult(),
                ) { }

                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                ) { }

                LaunchedEffect(ageVerified) {
                    if (ageVerified && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                if (!ageVerified) {
                    AgeVerificationScreen(
                        onAgeVerified = {
                            scope.launch { prefs.setAgeVerified(true) }
                        },
                        onUnderAgeExit = { finish() },
                    )
                } else {
                    val navController = rememberNavController()
                    MainNavHost(
                        navController = navController,
                        app = app,
                        prefs = prefs,
                        extremeUnlocked = extremeUnlocked,
                        onUnlockExtreme = {
                            scope.launch { prefs.setExtremeUnlocked(true) }
                        },
                        onStartGame = { config: GameConfig ->
                            gameLauncher.launch(GameActivity.createIntent(this@MainActivity, config))
                        },
                        onStartOnlineGame = { matchId ->
                            onlineGameLauncher.launch(
                                GameActivity.createOnlineIntent(this@MainActivity, matchId),
                            )
                        },
                        onOpenNotificationSettings = {
                            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                                }
                            } else {
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", packageName, null)
                                }
                            }
                            startActivity(intent)
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
