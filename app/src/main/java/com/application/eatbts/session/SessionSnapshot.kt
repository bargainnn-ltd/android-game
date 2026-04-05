package com.application.eatbts.session

import com.application.eatbts.data.Level

/**
 * Session choices passed from Screen 3 to in-app gameplay routes (Never, Spicy Spinner, WYR).
 * Set immediately before [androidx.navigation.NavController.navigate]; consumed on arrival.
 */
data class SessionSnapshot(
    val intensityLabel: String,
    val drinkingRulesOn: Boolean,
    val includeTruths: Boolean,
    val includeDares: Boolean,
    val turnTimerOn: Boolean,
    /** Resolved deck tier; matches Truth or Dare session intensity. */
    val level: Level,
    /** Seconds for debate/timer when [turnTimerOn] is true (same as Truth or Dare session). */
    val turnTimerSeconds: Int,
    val playerNames: List<String>,
)

object SessionStateHolder {
    @Volatile
    var pending: SessionSnapshot? = null
}
