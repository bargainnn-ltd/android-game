package com.spicynights.games

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.compose.ui.unit.max
import androidx.compose.ui.platform.LocalView
import androidx.core.content.getSystemService
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spicynights.games.data.GameConfig
import com.spicynights.games.data.local.AppThemePreference
import com.spicynights.games.ui.game.GameScreen
import com.spicynights.games.ui.navigation.AppBottomBar
import com.spicynights.games.ui.navigation.BottomNavStyle
import com.spicynights.games.ui.theme.CoupleGamesTheme
import com.spicynights.games.viewmodel.GameViewModel

class GameActivity : ComponentActivity() {

    private val config: GameConfig by lazy {
        if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra(EXTRA_CONFIG, GameConfig::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_CONFIG)
        } ?: error("Missing GameConfig")
    }

    private val viewModel: GameViewModel by viewModels {
        GameViewModel.factory(application as CoupleGamesApp, config)
    }

    private var soundPool: SoundPool? = null
    private var flipSoundId: Int = 0
    private var soundLoadSuccessCount: Int = 0
    private var soundsReady: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        initSounds()

        setContent {
            val prefs = (application as CoupleGamesApp).preferencesRepository
            val appTheme by prefs.appThemePreference.collectAsStateWithLifecycle(
                initialValue = AppThemePreference.MIDNIGHT,
            )
            val light = appTheme == AppThemePreference.LIGHT
            SideEffect {
                val window = this@GameActivity.window
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = light
                    isAppearanceLightNavigationBars = light
                }
                window.navigationBarColor = if (light) Color.White.toArgb() else android.graphics.Color.BLACK
            }
            val soundOn by prefs.soundEffectsEnabled.collectAsStateWithLifecycle(initialValue = true)
            val hapticsOn by prefs.hapticFeedbackEnabled.collectAsStateWithLifecycle(initialValue = true)
            CoupleGamesTheme(appTheme = appTheme) {
                val view = LocalView.current
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        AppBottomBar(
                            style = BottomNavStyle.SETTINGS_APP,
                            selectedRoute = "saved_stub",
                            onNavigate = { finish() },
                        )
                    },
                ) { padding ->
                    // Edge-to-edge can under-report bottom inset; never let content draw under the nav bar.
                    val minBottomForNavBar = 80.dp
                    val extraBottom = max(0.dp, minBottomForNavBar - padding.calculateBottomPadding())
                    GameScreen(
                        modifier = Modifier.padding(padding).padding(bottom = extraBottom),
                        viewModel = viewModel,
                        onFlipSound = { if (soundOn) playFlipSound() },
                        onClickSound = { if (soundOn) playFlipSound() },
                        onHapticLight = {
                            if (hapticsOn) {
                                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            }
                        },
                        onHapticStrong = {
                            if (hapticsOn) {
                                vibrateStrong()
                            }
                        },
                        onNavigateHome = { finish() },
                    )
                }
            }
        }
    }

    private fun initSounds() {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attrs)
            .build()
        soundPool?.let { sp ->
            soundsReady = false
            soundLoadSuccessCount = 0
            sp.setOnLoadCompleteListener { _, _, status ->
                if (status == 0) {
                    soundLoadSuccessCount++
                    if (soundLoadSuccessCount >= 1) {
                        soundsReady = true
                    }
                }
            }
            flipSoundId = sp.load(this, R.raw.flip, 1)
        }
    }

    private fun playFlipSound() {
        if (!soundsReady || flipSoundId == 0) return
        soundPool?.play(flipSoundId, 0.85f, 0.85f, 1, 0, 1f)
    }

    private fun vibrateStrong() {
        val v = getSystemService<Vibrator>() ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(40)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool?.release()
        soundPool = null
        soundsReady = false
    }

    companion object {
        const val EXTRA_CONFIG = "game_config"

        fun createIntent(context: Context, config: GameConfig): Intent =
            Intent(context, GameActivity::class.java).putExtra(EXTRA_CONFIG, config)
    }
}
