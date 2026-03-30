package com.spicynights.games.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GameConfig(
    /** Legacy: used when [playerNames] has fewer than 2 entries. */
    val firstTurnIsPlayerOne: Boolean,
    val level: Level,
    val poolMode: PoolMode,
    val includeTruths: Boolean,
    val includeDares: Boolean,
    /** Display name for male (player 1); empty uses default label in game. */
    val maleName: String = "",
    /** Display name for female (player 2); empty uses default label in game. */
    val femaleName: String = "",
    /**
     * When size is 2–4, Truth & Dare uses round-robin turns with these display names.
     * When empty or size &lt; 2, [maleName] / [femaleName] and [firstTurnIsPlayerOne] apply.
     */
    val playerNames: List<String> = emptyList(),
    /** Index into [playerNames] for the first turn; ignored for legacy two-name mode. */
    val firstPlayerIndex: Int = 0,
    /** Session toggle; future prompt filtering / analytics. */
    val drinkingRulesEnabled: Boolean = true,
    /**
     * When true, [sessionTruths] / [sessionDares] replace loading from assets for this activity only.
     * [poolMode] is ignored in the ViewModel (treated as full custom subset).
     */
    val useCustomSessionPool: Boolean = false,
    val sessionTruths: List<String> = emptyList(),
    val sessionDares: List<String> = emptyList(),
    /** Default turn countdown (seconds) from Settings; used for in-game timer bar. */
    val turnTimerSeconds: Int = 30,
) : Parcelable
