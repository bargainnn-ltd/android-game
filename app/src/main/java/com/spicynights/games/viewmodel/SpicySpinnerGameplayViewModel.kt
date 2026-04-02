package com.spicynights.games.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spicynights.games.session.SessionSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

data class SpicySpinnerUiState(
    val playerNames: List<String>,
    val intensityLine: String,
    val turnIndex: Int,
    val bodyRoll: Int?,
    val actionRoll: Int?,
    /** True when both rings land on the same number — roller picks any body + action. */
    val isDoubleRoll: Boolean,
    /** After a double, user confirmed they will choose freely. */
    val freeChoiceActive: Boolean,
    val sessionReRollsRemaining: Int,
    /** Completed turns (each Next player); game ends at [maxTurns]. */
    val turnsCompleted: Int,
    val maxTurns: Int,
    val sessionComplete: Boolean,
    /** Seconds for action timer when session had timer on; 0 = disabled. */
    val actionTimerSeconds: Int,
    val turnTimerEnabled: Boolean,
    /** Random index into `dice_spicy_modifiers` string array, or null. */
    val modifierIndex: Int?,
)

class SpicySpinnerGameplayViewModel(
    snapshot: SessionSnapshot?,
) : ViewModel() {
    private val snap = snapshot ?: NeverGameplayViewModel.defaultSnapshot()
    private val random = Random.Default

    private val names = snap.playerNames.ifEmpty { listOf("Player 1", "Player 2") }
    private val maxTurns = CouplesDiceRules.maxTurns(names.size)

    private val timerSec = snap.turnTimerSeconds.coerceIn(10, 120)
    private val spicyModifierCount = 8

    private val _state = MutableStateFlow(
        SpicySpinnerUiState(
            playerNames = names,
            intensityLine = "${snap.intensityLabel} • ${if (snap.drinkingRulesOn) "Drinking on" else "Drinking off"}",
            turnIndex = 0,
            bodyRoll = null,
            actionRoll = null,
            isDoubleRoll = false,
            freeChoiceActive = false,
            sessionReRollsRemaining = MAX_SESSION_REROLLS,
            turnsCompleted = 0,
            maxTurns = maxTurns,
            sessionComplete = false,
            actionTimerSeconds = if (snap.turnTimerOn && timerSec > 0) timerSec else 0,
            turnTimerEnabled = snap.turnTimerOn && timerSec > 0,
            modifierIndex = null,
        ),
    )
    val state: StateFlow<SpicySpinnerUiState> = _state.asStateFlow()

    fun rollDice() {
        val s = _state.value
        if (s.sessionComplete) return
        val b = random.nextInt(1, CouplesDiceRules.SIDES + 1)
        val a = random.nextInt(1, CouplesDiceRules.SIDES + 1)
        val double = CouplesDiceRules.isDoubleRoll(b, a)
        val mod = if (!double) random.nextInt(0, spicyModifierCount) else null
        _state.update {
            it.copy(
                bodyRoll = b,
                actionRoll = a,
                isDoubleRoll = double,
                freeChoiceActive = false,
                modifierIndex = mod,
            )
        }
    }

    fun confirmFreeChoice() {
        val s = _state.value
        if (!s.isDoubleRoll) return
        _state.update { it.copy(freeChoiceActive = true) }
    }

    fun reRoll() {
        val s = _state.value
        if (s.sessionComplete || s.sessionReRollsRemaining <= 0) return
        _state.update { it.copy(sessionReRollsRemaining = it.sessionReRollsRemaining - 1) }
        rollDice()
    }

    fun nextPlayer() {
        val s = _state.value
        if (s.sessionComplete) return
        val n = s.playerNames.size.coerceAtLeast(1)
        val nextTurn = (s.turnIndex + 1) % n
        val completed = s.turnsCompleted + 1
        val done = completed >= s.maxTurns
        _state.update {
            it.copy(
                turnIndex = nextTurn,
                bodyRoll = null,
                actionRoll = null,
                isDoubleRoll = false,
                freeChoiceActive = false,
                turnsCompleted = completed,
                sessionComplete = done,
                modifierIndex = null,
            )
        }
    }

    fun resetSession() {
        _state.update {
            it.copy(
                turnIndex = 0,
                bodyRoll = null,
                actionRoll = null,
                isDoubleRoll = false,
                freeChoiceActive = false,
                sessionReRollsRemaining = MAX_SESSION_REROLLS,
                turnsCompleted = 0,
                sessionComplete = false,
                modifierIndex = null,
            )
        }
    }

    companion object {
        private const val MAX_SESSION_REROLLS = 2

        fun factory(snapshot: SessionSnapshot?): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass != SpicySpinnerGameplayViewModel::class.java) throw IllegalArgumentException()
                    return SpicySpinnerGameplayViewModel(snapshot) as T
                }
            }
    }
}
