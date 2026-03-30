package com.spicynights.games.session

/**
 * Session choices passed from Screen 3 to in-app gameplay routes (Never, Dice, WYR).
 * Set immediately before [androidx.navigation.NavController.navigate]; consumed on arrival.
 */
data class SessionSnapshot(
    val intensityLabel: String,
    val drinkingRulesOn: Boolean,
    val includeTruths: Boolean,
    val includeDares: Boolean,
    val turnTimerOn: Boolean,
    val playerNames: List<String>,
)

object SessionStateHolder {
    @Volatile
    var pending: SessionSnapshot? = null
}
