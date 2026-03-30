package com.spicynights.games.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spicynights.games.data.DataManager
import com.spicynights.games.data.party.WyrPairDto
import com.spicynights.games.session.SessionSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WyrUiState(
    val optionA: String?,
    val optionB: String?,
    val cardsRemaining: Int,
    val debateTimerEnabled: Boolean,
    val timerSecondsTotal: Int,
    val error: String?,
)

class WyrGameplayViewModel(
    private val dataManager: DataManager,
    snapshot: SessionSnapshot?,
) : ViewModel() {
    private val snap = snapshot ?: NeverGameplayViewModel.defaultSnapshot()

    private val deck: MutableList<WyrPairDto> = mutableListOf()

    private val _state = MutableStateFlow(
        WyrUiState(
            optionA = null,
            optionB = null,
            cardsRemaining = 0,
            debateTimerEnabled = snap.turnTimerOn,
            timerSecondsTotal = snap.turnTimerSeconds.coerceIn(10, 120),
            error = null,
        ),
    )
    val state: StateFlow<WyrUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            dataManager.loadWyrPairs(snap.level).fold(
                onSuccess = { pairs ->
                    deck.clear()
                    deck.addAll(pairs.shuffled())
                    showNextCardInternal()
                },
                onFailure = { e ->
                    _state.update { it.copy(error = e.message) }
                },
            )
        }
    }

    fun nextCard() {
        if (deck.isEmpty()) {
            _state.update { it.copy(optionA = null, optionB = null, cardsRemaining = 0) }
            return
        }
        showNextCardInternal()
    }

    private fun showNextCardInternal() {
        if (deck.isEmpty()) {
            _state.update { it.copy(optionA = null, optionB = null, cardsRemaining = 0) }
            return
        }
        val card = deck.removeFirst()
        _state.update {
            it.copy(
                optionA = card.a,
                optionB = card.b,
                cardsRemaining = deck.size,
            )
        }
    }

    fun setDebateTimerEnabled(enabled: Boolean) {
        _state.update { it.copy(debateTimerEnabled = enabled) }
    }

    companion object {
        fun factory(
            snapshot: SessionSnapshot?,
            dataManager: DataManager,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass != WyrGameplayViewModel::class.java) throw IllegalArgumentException()
                return WyrGameplayViewModel(dataManager, snapshot) as T
            }
        }
    }
}
