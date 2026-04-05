package com.application.eatbts

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.core.content.getSystemService
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.application.eatbts.ads.AdsManager
import com.application.eatbts.data.GameConfig
import com.application.eatbts.data.local.AppThemePreference
import com.application.eatbts.ui.game.GameScreen
import com.application.eatbts.ui.game.OnlineTruthDareScreen
import com.application.eatbts.ui.navigation.AppBottomBar
import com.application.eatbts.ui.navigation.BottomNavStyle
import com.application.eatbts.ui.theme.CoupleGamesTheme
import com.application.eatbts.viewmodel.GameViewModel
import com.application.eatbts.viewmodel.OnlineTruthDareViewModel

class GameActivity : ComponentActivity() {

    private var soundPool: SoundPool? = null
    private var flipSoundId: Int = 0
    private var soundLoadSuccessCount: Int = 0
    private var soundsReady: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        initSounds()

        val app = application as CoupleGamesApp
        val matchId = intent.getStringExtra(EXTRA_MATCH_ID)
        val localConfig: GameConfig? = if (matchId == null) extractConfig() else null
        if (matchId == null && localConfig == null) {
            finish()
            return
        }

        setContent {
            val prefs = app.preferencesRepository
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
                    val minBottomForNavBar = 80.dp
                    val extraBottom = max(0.dp, minBottomForNavBar - padding.calculateBottomPadding())
                    val contentModifier = Modifier.padding(padding).padding(bottom = extraBottom)
                    if (matchId != null) {
                        val onlineVm: OnlineTruthDareViewModel = viewModel(
                            factory = OnlineTruthDareViewModel.factory(app, matchId),
                        )
                        val onlineUi by onlineVm.uiState.collectAsStateWithLifecycle()
                        LaunchedEffect(onlineUi.matchCompleted) {
                            if (onlineUi.matchCompleted) {
                                AdsManager.showInterstitialIfReady(this@GameActivity)
                                val lb = getString(R.string.play_games_leaderboard_id)
                                app.playGamesManager.submitScore(
                                    this@GameActivity,
                                    lb,
                                    onlineUi.cardsRevealed.coerceAtLeast(1).toLong(),
                                )
                            }
                        }
                        OnlineTruthDareScreen(
                            modifier = contentModifier,
                            viewModel = onlineVm,
                            onFlipSound = { if (soundOn) playFlipSound() },
                            onClickSound = { if (soundOn) playFlipSound() },
                            onNavigateHome = { finish() },
                        )
                    } else {
                        val localVm: GameViewModel = viewModel(
                            factory = GameViewModel.factory(app, localConfig!!),
                        )
                        GameScreen(
                            modifier = contentModifier,
                            viewModel = localVm,
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
    }

    private fun extractConfig(): GameConfig? {
        return if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra(EXTRA_CONFIG, GameConfig::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_CONFIG)
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
        const val EXTRA_MATCH_ID = "match_id"

        fun createIntent(context: Context, config: GameConfig): Intent =
            Intent(context, GameActivity::class.java).putExtra(EXTRA_CONFIG, config)

        fun createOnlineIntent(context: Context, matchId: String): Intent =
            Intent(context, GameActivity::class.java).putExtra(EXTRA_MATCH_ID, matchId)
    }
}
