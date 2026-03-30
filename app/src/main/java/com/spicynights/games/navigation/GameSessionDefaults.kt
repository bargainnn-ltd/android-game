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
    climaxUnlocked: Boolean,
): GameConfig {
    val level = when (defaultIntensity) {
        DefaultIntensity.MILD.storageValue -> Level.TRIALS
        DefaultIntensity.SPICY.storageValue -> Level.WANDERINGS
        DefaultIntensity.EXTREME.storageValue ->
            if (climaxUnlocked) Level.CLIMAX else Level.WANDERINGS
        else -> Level.TRIALS
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
