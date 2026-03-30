package com.spicynights.games

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.spicynights.games.data.GameConfig
import com.spicynights.games.data.local.AppThemePreference
import com.spicynights.games.navigation.MainNavHost
import com.spicynights.games.ui.onboarding.AgeVerificationScreen
import com.spicynights.games.ui.theme.SpicyNightsTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as SpicyNightsApp
        val prefs = app.preferencesRepository

        setContent {
            val appTheme by prefs.appThemePreference.collectAsStateWithLifecycle(
                initialValue = AppThemePreference.MIDNIGHT,
            )
            SpicyNightsTheme(appTheme = appTheme) {
                val scope = rememberCoroutineScope()
                val ageVerified by prefs.ageVerified.collectAsStateWithLifecycle(initialValue = false)
                val climaxUnlocked by prefs.climaxUnlocked.collectAsStateWithLifecycle(initialValue = false)

                val gameLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult(),
                ) { }

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
                        prefs = prefs,
                        climaxUnlocked = climaxUnlocked,
                        onUnlockClimax = {
                            scope.launch { prefs.setClimaxUnlocked(true) }
                        },
                        onStartGame = { config: GameConfig ->
                            gameLauncher.launch(GameActivity.createIntent(this@MainActivity, config))
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
