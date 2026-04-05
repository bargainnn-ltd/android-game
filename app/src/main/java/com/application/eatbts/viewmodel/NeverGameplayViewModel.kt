package com.application.eatbts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.application.eatbts.data.DataManager
import com.application.eatbts.data.Level
import com.application.eatbts.session.SessionSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NeverUiState(
    val playerNames: List<String>,
    val intensityLine: String,
    /** Shown as mood sub-label (e.g. intensity). */
    val moodLabel: String,
    val drinkingRulesOn: Boolean,
    val currentPrompt: String?,
    val deckRemaining: Int,
    /** Total prompts in deck at session start (for ROUND x OF y). */
    val totalPrompts: Int,
    val turnIndex: Int,
    /** Per player: null = not set, true = have done, false = never */
    val playerAnswers: List<Boolean?>,
    val error: String?,
    /** From session setup: show countdown per prompt when true. */
    val turnTimerEnabled: Boolean,
    /** Seconds per turn when [turnTimerEnabled]; 0 when off. */
    val turnTimerSecondsTotal: Int,
) {
    val currentReaderName: String
        get() = playerNames.getOrElse(turnIndex % playerNames.size.coerceAtLeast(1)) { "?" }
}

private fun buildNeverIntensityLine(s: SessionSnapshot): String =
    buildString {
        append(s.intensityLabel)
        append(" • ")
        append(if (s.drinkingRulesOn) "Drinking on" else "Drinking off")
    }

class NeverGameplayViewModel(
    private val dataManager: DataManager,
    snapshot: SessionSnapshot?,
) : ViewModel() {
    private val snap = snapshot ?: defaultSnapshot()
    private val resolvedNames = snap.playerNames.ifEmpty { listOf("Player 1", "Player 2") }
    private val timerSec = snap.turnTimerSeconds.coerceIn(10, 120)
    private val timerEnabled = snap.turnTimerOn && timerSec > 0

    private val _state = MutableStateFlow(
        NeverUiState(
            playerNames = resolvedNames,
            intensityLine = buildNeverIntensityLine(snap),
            moodLabel = snap.intensityLabel,
            drinkingRulesOn = snap.drinkingRulesOn,
            currentPrompt = null,
            deckRemaining = 0,
            totalPrompts = 0,
            turnIndex = 0,
            playerAnswers = List(resolvedNames.size) { null },
            error = null,
            turnTimerEnabled = timerEnabled,
            turnTimerSecondsTotal = if (timerEnabled) timerSec else 0,
        ),
    )
    val state: StateFlow<NeverUiState> = _state.asStateFlow()

    private var deck: MutableList<String> = mutableListOf()

    init {
        viewModelScope.launch {
            dataManager.loadNeverPrompts(snap.level).fold(
                onSuccess = { prompts ->
                    val total = prompts.size
                    deck = prompts.shuffled().toMutableList()
                    val first = deck.removeFirstOrNull()
                    _state.update {
                        it.copy(
                            currentPrompt = first,
                            deckRemaining = deck.size,
                            totalPrompts = total,
                            playerAnswers = List(resolvedNames.size) { null },
                        )
                    }
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            currentPrompt = null,
                            error = e.message,
                            deckRemaining = 0,
                        )
                    }
                },
            )
        }
    }

    fun setPlayerAnswer(playerIndex: Int, hasDone: Boolean) {
        _state.update { s ->
            if (playerIndex !in s.playerNames.indices) return@update s
            val next = s.playerAnswers.toMutableList()
            next[playerIndex] = hasDone
            s.copy(playerAnswers = next)
        }
    }

    fun nextPrompt() {
        val names = _state.value.playerNames
        if (deck.isEmpty()) {
            _state.update {
                it.copy(currentPrompt = null, deckRemaining = 0)
            }
            return
        }
        val next = deck.removeFirst()
        _state.update {
            it.copy(
                currentPrompt = next,
                deckRemaining = deck.size,
                totalPrompts = it.totalPrompts,
                turnIndex = (it.turnIndex + 1) % names.size.coerceAtLeast(1),
                playerAnswers = List(names.size) { null },
            )
        }
    }

    fun setAllPlayersAnswer(hasDone: Boolean) {
        val n = _state.value.playerNames.size
        _state.update { s ->
            s.copy(playerAnswers = List(n) { hasDone })
        }
    }

    companion object {
        fun defaultSnapshot(): SessionSnapshot = SessionSnapshot(
            intensityLabel = "Spicy",
            drinkingRulesOn = false,
            includeTruths = true,
            includeDares = true,
            turnTimerOn = false,
            level = Level.SPICY,
            turnTimerSeconds = 30,
            playerNames = listOf("Player 1", "Player 2"),
        )

        fun factory(
            snapshot: SessionSnapshot?,
            dataManager: DataManager,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass != NeverGameplayViewModel::class.java) throw IllegalArgumentException()
                return NeverGameplayViewModel(dataManager, snapshot) as T
            }
        }
    }
}
