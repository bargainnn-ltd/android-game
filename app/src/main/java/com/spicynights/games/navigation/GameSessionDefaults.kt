package com.spicynights.games.navigation

import com.spicynights.games.data.GameConfig
import com.spicynights.games.data.Level
import com.spicynights.games.data.PoolMode
import com.spicynights.games.data.local.DefaultIntensity

/**
 * Builds [GameConfig] from global Settings (default intensity → level, turn timer, full pool).
 */
fun gameConfigFromSessionPrefs(
    defaultIntensity: Int,
    turnTimerSeconds: Int,
    extremeUnlocked: Boolean,
): GameConfig {
    val level = when (defaultIntensity) {
        DefaultIntensity.MILD.storageValue -> Level.MILD
        DefaultIntensity.SPICY.storageValue -> Level.SPICY
        DefaultIntensity.EXTREME.storageValue ->
            if (extremeUnlocked) Level.EXTREME else Level.SPICY
        else -> Level.MILD
    }
    return GameConfig(
        firstTurnIsPlayerOne = true,
        level = level,
        poolMode = PoolMode.ALL,
        includeTruths = true,
        includeDares = true,
        turnTimerSeconds = turnTimerSeconds,
    )
}
