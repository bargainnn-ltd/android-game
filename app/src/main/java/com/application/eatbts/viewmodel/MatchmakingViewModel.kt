package com.application.eatbts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.application.eatbts.CoupleGamesApp
import com.application.eatbts.data.DataManager
import com.application.eatbts.data.PoolMode
import com.application.eatbts.data.online.OnlineTruthDareSession
import com.application.eatbts.firebase.AnalyticsLogger
import com.application.eatbts.firebase.MatchmakingRepository
import com.application.eatbts.firebase.MatchmakingTicketParams
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.random.Random

sealed class MatchmakingUiState {
    data object Idle : MatchmakingUiState()
    data object NeedAuth : MatchmakingUiState()
    data class Searching(val ticketId: String) : MatchmakingUiState()
    data class Matched(val matchId: String) : MatchmakingUiState()
    data class Error(val message: String) : MatchmakingUiState()
}

class MatchmakingViewModel(
    private val dataManager: DataManager,
    private val analytics: AnalyticsLogger,
    private val matchmaking: MatchmakingRepository,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : ViewModel() {

    private val _state = MutableStateFlow<MatchmakingUiState>(MatchmakingUiState.Idle)
    val state: StateFlow<MatchmakingUiState> = _state.asStateFlow()

    private var queueJob: Job? = null
    private var lastSession: OnlineTruthDareSession? = null
    private var myTicketId: String? = null

    fun start(session: OnlineTruthDareSession) {
        lastSession = session
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _state.value = MatchmakingUiState.NeedAuth
            return
        }
        viewModelScope.launch {
            runCatching {
                val level = session.level
                val pack = dataManager.buildSessionPack(
                    dataManager.loadLevel(level).getOrThrow(),
                    emptyList(),
                    emptyList(),
                )
                val seed = Random.nextLong()
                val random = Random(seed)
                val (t, d) = buildSessionQueues(
                    pack = pack,
                    includeTruths = session.includeTruths,
                    includeDares = session.includeDares,
                    poolMode = PoolMode.ALL,
                    random = random,
                )
                val params = MatchmakingTicketParams(
                    shuffleSeed = seed,
                    truthsRemaining = t.size,
                    daresRemaining = d.size,
                    levelKey = level.name,
                    includeTruths = session.includeTruths,
                    includeDares = session.includeDares,
                    turnTimerSeconds = session.turnTimerSeconds,
                )
                val ticketId = matchmaking.enqueueTicket(
                    uid = uid,
                    displayName = session.displayName,
                    gameMode = MatchmakingRepository.GAME_MODE_TRUTH_DARE,
                    params = params,
                )
                myTicketId = ticketId
                _state.value = MatchmakingUiState.Searching(ticketId)
                observeQueue(uid, ticketId)
            }.onFailure { e ->
                _state.value = MatchmakingUiState.Error(e.message ?: "Matchmaking failed")
            }
        }
    }

    private fun observeQueue(uid: String, ticketId: String) {
        queueJob?.cancel()
        queueJob = matchmaking.seekingTicketsFlow(MatchmakingRepository.GAME_MODE_TRUTH_DARE)
            .onEach { tickets ->
                if (_state.value is MatchmakingUiState.Matched) return@onEach
                val stillQueued = tickets.any { it.id == ticketId }
                if (!stillQueued && _state.value is MatchmakingUiState.Searching) {
                    val mid = matchmaking.fetchLatestActiveMatchForPlayer(uid)
                    if (mid != null) {
                        _state.value = MatchmakingUiState.Matched(mid)
                        analytics.logMatchStart(mid)
                        queueJob?.cancel()
                    }
                    return@onEach
                }
                viewModelScope.launch {
                    runCatching {
                        val created = matchmaking.tryPairOldest(
                            myUid = uid,
                            myTicketId = ticketId,
                            tickets = tickets,
                            gameMode = MatchmakingRepository.GAME_MODE_TRUTH_DARE,
                        )
                        if (created != null) {
                            _state.value = MatchmakingUiState.Matched(created)
                            analytics.logMatchStart(created)
                            queueJob?.cancel()
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun cancelSearch() {
        queueJob?.cancel()
        queueJob = null
        val tid = myTicketId
        myTicketId = null
        if (tid != null) {
            viewModelScope.launch {
                runCatching { matchmaking.deleteTicket(tid) }
            }
        }
        _state.value = MatchmakingUiState.Idle
    }

    fun retryLastSession() {
        lastSession?.let { start(it) }
    }

    override fun onCleared() {
        super.onCleared()
        queueJob?.cancel()
    }

    companion object {
        fun factory(app: CoupleGamesApp): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MatchmakingViewModel(
                    app.dataManager,
                    app.analyticsLogger,
                    MatchmakingRepository(),
                ) as T
            }
        }
    }
}
