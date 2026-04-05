package com.application.eatbts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.application.eatbts.CoupleGamesApp
import com.application.eatbts.data.DataManager
import com.application.eatbts.data.Level
import com.application.eatbts.data.PoolMode
import com.application.eatbts.firebase.AnalyticsLogger
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

private const val MAX_SKIPS = 3

data class OnlineGameUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val phase: CardPhase = CardPhase.FaceDown,
    val currentPrompt: String = "",
    val pendingRevealIsTruth: Boolean = true,
    val truthsRemaining: Int = 0,
    val daresRemaining: Int = 0,
    val totalInSession: Int = 0,
    val cardsRevealed: Int = 0,
    val skipsRemaining: List<Int> = listOf(MAX_SKIPS, MAX_SKIPS),
    val currentPlayerIndex: Int = 0,
    val playerNames: List<String> = listOf("", ""),
    val selectedChoice: TruthDareChoice? = null,
    val level: Level = Level.MILD,
    val turnTimerSeconds: Int = 30,
    val timerRemainingSeconds: Int? = null,
    val penaltyEnabled: Boolean = true,
    val myUid: String? = null,
    val currentTurnUid: String? = null,
    val isMyTurn: Boolean = false,
    val matchCompleted: Boolean = false,
)

class OnlineTruthDareViewModel(
    private val dataManager: DataManager,
    private val analytics: AnalyticsLogger,
    private val matchId: String,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : ViewModel() {

    private val matchRef = db.collection("matches").document(matchId)

    private val _uiState = MutableStateFlow(OnlineGameUiState())
    val uiState: StateFlow<OnlineGameUiState> = _uiState.asStateFlow()

    private var truthsQueue: MutableList<String> = mutableListOf()
    private var daresQueue: MutableList<String> = mutableListOf()
    private var playerCount: Int = 2
    private var listener: ListenerRegistration? = null
    private var turnTimerJob: Job? = null
    private var matchEndLogged: Boolean = false

    init {
        val uid = auth.currentUser?.uid
        _uiState.update { it.copy(myUid = uid) }
        listener = matchRef.addSnapshotListener { snap, e ->
            if (e != null) {
                _uiState.update { it.copy(loading = false, error = e.message) }
                return@addSnapshotListener
            }
            if (snap == null || !snap.exists()) {
                _uiState.update { it.copy(loading = false, error = "Match not found") }
                return@addSnapshotListener
            }
            onMatchSnapshot(snap.data ?: emptyMap())
        }
    }

    private fun onMatchSnapshot(data: Map<String, Any?>) {
        val status = data["status"] as? String ?: "active"
            if (status == "completed") {
            cancelTurnTimer()
            _uiState.update {
                it.copy(loading = false, matchCompleted = true, phase = CardPhase.DeckEmpty)
            }
            if (!matchEndLogged) {
                matchEndLogged = true
                analytics.logMatchEnd(matchId)
            }
            return
        }

        val players = (data["players"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
        val names = (data["player_names"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
        val turnUid = data["current_turn"] as? String
        val level = levelFromKey(data["level"] as? String)
        val seed = (data["shuffle_seed"] as? Number)?.toLong() ?: 0L
        val timerSec = (data["turn_timer_seconds"] as? Number)?.toInt() ?: 30
        val includeTruths = data["include_truths"] as? Boolean ?: true
        val includeDares = data["include_dares"] as? Boolean ?: true

        @Suppress("UNCHECKED_CAST")
        val gs = data["game_state"] as? Map<String, Any?> ?: emptyMap()

        playerCount = players.size.coerceIn(2, 4)
        rebuildQueuesIfNeeded(level, seed, includeTruths, includeDares)

        val tr = (gs["truths_remaining"] as? Number)?.toInt() ?: 0
        val dr = (gs["dares_remaining"] as? Number)?.toInt() ?: 0
        syncQueuesToCounts(tr, dr)

        val phase = phaseFromString(gs["phase"] as? String)
        val myUid = auth.currentUser?.uid
        val idx = (gs["current_player_index"] as? Number)?.toInt() ?: 0
        val skips = (gs["skips_remaining"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() }
            ?: List(playerCount) { MAX_SKIPS }

        val revealed = (gs["cards_revealed"] as? Number)?.toInt() ?: 0
        _uiState.update {
            it.copy(
                loading = false,
                error = null,
                phase = phase,
                currentPrompt = gs["current_prompt"] as? String ?: "",
                pendingRevealIsTruth = gs["pending_reveal_is_truth"] as? Boolean ?: true,
                truthsRemaining = tr,
                daresRemaining = dr,
                totalInSession = (tr + dr + revealed).coerceAtLeast(0),
                cardsRevealed = revealed,
                skipsRemaining = skips.take(playerCount).ifEmpty { List(playerCount) { MAX_SKIPS } },
                currentPlayerIndex = idx.coerceIn(0, playerCount - 1),
                playerNames = names.take(playerCount).ifEmpty { List(playerCount) { "Player" } },
                selectedChoice = choiceFromString(gs["selected_choice"] as? String),
                level = level,
                turnTimerSeconds = timerSec,
                currentTurnUid = turnUid,
                isMyTurn = myUid != null && turnUid == myUid,
                matchCompleted = false,
            )
        }

        if (phase == CardPhase.ShowingTruth || phase == CardPhase.ShowingDare) {
            startTurnTimerIfNeeded()
        } else {
            cancelTurnTimer()
        }
    }

    private fun rebuildQueuesIfNeeded(level: Level, seed: Long, includeTruths: Boolean, includeDares: Boolean) {
        if (seed == 0L) return
        val pack = dataManager.buildSessionPack(
            dataManager.loadLevel(level).getOrElse { return },
            emptyList(),
            emptyList(),
        )
        val random = Random(seed)
        val (t, d) = buildSessionQueues(
            pack = pack,
            includeTruths = includeTruths,
            includeDares = includeDares,
            poolMode = PoolMode.ALL,
            random = random,
        )
        truthsQueue = t
        daresQueue = d
    }

    private fun syncQueuesToCounts(tr: Int, dr: Int) {
        while (truthsQueue.size > tr) truthsQueue.removeAt(0)
        while (daresQueue.size > dr) daresQueue.removeAt(0)
    }

    private fun phaseFromString(s: String?): CardPhase = when (s) {
        "face_down" -> CardPhase.FaceDown
        "flipping" -> CardPhase.Flipping
        "showing_truth" -> CardPhase.ShowingTruth
        "showing_dare" -> CardPhase.ShowingDare
        "deck_empty" -> CardPhase.DeckEmpty
        else -> CardPhase.FaceDown
    }

    private fun phaseToString(p: CardPhase): String = when (p) {
        CardPhase.FaceDown -> "face_down"
        CardPhase.Flipping -> "flipping"
        CardPhase.ShowingTruth -> "showing_truth"
        CardPhase.ShowingDare -> "showing_dare"
        CardPhase.DeckEmpty -> "deck_empty"
    }

    private fun choiceFromString(s: String?): TruthDareChoice? = when (s) {
        "truth" -> TruthDareChoice.TRUTH
        "dare" -> TruthDareChoice.DARE
        else -> null
    }

    private fun levelFromKey(key: String?): Level = when (key) {
        "SPICY" -> Level.SPICY
        "EXTREME" -> Level.EXTREME
        else -> Level.MILD
    }

    fun setPenaltyEnabled(enabled: Boolean) {
        _uiState.update { it.copy(penaltyEnabled = enabled) }
    }

    fun setTruthDareChoice(choice: TruthDareChoice) {
        val s = _uiState.value
        if (!s.isMyTurn || s.phase != CardPhase.FaceDown) return
        viewModelScope.launch {
            runCatching {
                commitChoice(choice)
            }.onFailure {
                _uiState.update { st -> st.copy(error = it.message) }
            }
        }
    }

    private suspend fun commitChoice(choice: TruthDareChoice) {
        val uid = auth.currentUser?.uid ?: return
        syncQueuesToCounts(_uiState.value.truthsRemaining, _uiState.value.daresRemaining)
        val ok = when (choice) {
            TruthDareChoice.TRUTH -> truthsQueue.isNotEmpty()
            TruthDareChoice.DARE -> daresQueue.isNotEmpty()
        }
        if (!ok) return

        db.runTransaction { tx ->
            val snap = tx.get(matchRef)
            if (!snap.exists()) throw FirebaseFirestoreException("missing", FirebaseFirestoreException.Code.ABORTED, null)
            if (snap.getString("current_turn") != uid) {
                throw FirebaseFirestoreException("turn", FirebaseFirestoreException.Code.ABORTED, null)
            }
            @Suppress("UNCHECKED_CAST")
            val gs = snap.get("game_state") as? Map<String, Any?> ?: emptyMap()
            if (gs["phase"] != "face_down") {
                throw FirebaseFirestoreException("phase", FirebaseFirestoreException.Code.ABORTED, null)
            }
            val tr = (gs["truths_remaining"] as? Number)?.toInt() ?: 0
            val dr = (gs["dares_remaining"] as? Number)?.toInt() ?: 0
            if (tr != truthsQueue.size || dr != daresQueue.size) {
                throw FirebaseFirestoreException("sync", FirebaseFirestoreException.Code.ABORTED, null)
            }
            val isTruth = choice == TruthDareChoice.TRUTH
            val prompt = when (choice) {
                TruthDareChoice.TRUTH -> truthsQueue.firstOrNull()
                TruthDareChoice.DARE -> daresQueue.firstOrNull()
            } ?: throw FirebaseFirestoreException("empty", FirebaseFirestoreException.Code.ABORTED, null)
            val newTr = if (isTruth) tr - 1 else tr
            val newDr = if (isTruth) dr else dr - 1
            val revealed = ((gs["cards_revealed"] as? Number)?.toInt() ?: 0) + 1
            val newPhase = if (isTruth) "showing_truth" else "showing_dare"
            val newGs = gs.toMutableMap()
            newGs["phase"] = newPhase
            newGs["current_prompt"] = prompt
            newGs["pending_reveal_is_truth"] = isTruth
            newGs["truths_remaining"] = newTr
            newGs["dares_remaining"] = newDr
            newGs["cards_revealed"] = revealed
            newGs["selected_choice"] = null
            tx.update(
                matchRef,
                mapOf(
                    "game_state" to newGs,
                    "updated_at" to FieldValue.serverTimestamp(),
                ),
            )
            null
        }.await()
        analytics.logMatchTurn(matchId)
    }

    fun onNext() {
        val s = _uiState.value
        if (!s.isMyTurn) return
        if (s.phase != CardPhase.ShowingTruth && s.phase != CardPhase.ShowingDare) return
        viewModelScope.launch {
            runCatching { commitNext() }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    private suspend fun commitNext() {
        val uid = auth.currentUser?.uid ?: return
        syncQueuesToCounts(_uiState.value.truthsRemaining, _uiState.value.daresRemaining)
        db.runTransaction { tx ->
            val snap = tx.get(matchRef)
            if (!snap.exists()) throw FirebaseFirestoreException("missing", FirebaseFirestoreException.Code.ABORTED, null)
            if (snap.getString("current_turn") != uid) {
                throw FirebaseFirestoreException("turn", FirebaseFirestoreException.Code.ABORTED, null)
            }
            @Suppress("UNCHECKED_CAST")
            val gs = snap.get("game_state") as? Map<String, Any?> ?: emptyMap()
            val phase = gs["phase"] as? String
            if (phase != "showing_truth" && phase != "showing_dare") {
                throw FirebaseFirestoreException("phase", FirebaseFirestoreException.Code.ABORTED, null)
            }
            val players = (snap.get("players") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            val n = players.size.coerceAtLeast(2)
            val idx = (gs["current_player_index"] as? Number)?.toInt() ?: 0
            val newIdx = (idx + 1) % n
            val newTurn = players[newIdx]
            var tr = (gs["truths_remaining"] as? Number)?.toInt() ?: 0
            var dr = (gs["dares_remaining"] as? Number)?.toInt() ?: 0
            if (tr != truthsQueue.size || dr != daresQueue.size) {
                throw FirebaseFirestoreException("sync", FirebaseFirestoreException.Code.ABORTED, null)
            }
            val newGs = gs.toMutableMap()
            val revealedBase = (gs["cards_revealed"] as? Number)?.toInt() ?: 0
            if (tr <= 0 && dr <= 0) {
                newGs["phase"] = "deck_empty"
                newGs["current_prompt"] = ""
                tx.update(
                    matchRef,
                    mapOf(
                        "game_state" to newGs,
                        "status" to "completed",
                        "current_turn" to newTurn,
                        "updated_at" to FieldValue.serverTimestamp(),
                    ),
                )
            } else if (dr == 0 && tr > 0) {
                val text = truthsQueue.firstOrNull()
                    ?: throw FirebaseFirestoreException("empty", FirebaseFirestoreException.Code.ABORTED, null)
                newGs["phase"] = "showing_truth"
                newGs["current_prompt"] = text
                newGs["pending_reveal_is_truth"] = true
                newGs["truths_remaining"] = tr - 1
                newGs["cards_revealed"] = revealedBase + 1
                newGs["selected_choice"] = null
                newGs["current_player_index"] = newIdx
                tx.update(
                    matchRef,
                    mapOf(
                        "game_state" to newGs,
                        "current_turn" to newTurn,
                        "updated_at" to FieldValue.serverTimestamp(),
                    ),
                )
            } else if (tr == 0 && dr > 0) {
                val text = daresQueue.firstOrNull()
                    ?: throw FirebaseFirestoreException("empty", FirebaseFirestoreException.Code.ABORTED, null)
                newGs["phase"] = "showing_dare"
                newGs["current_prompt"] = text
                newGs["pending_reveal_is_truth"] = false
                newGs["dares_remaining"] = dr - 1
                newGs["cards_revealed"] = revealedBase + 1
                newGs["selected_choice"] = null
                newGs["current_player_index"] = newIdx
                tx.update(
                    matchRef,
                    mapOf(
                        "game_state" to newGs,
                        "current_turn" to newTurn,
                        "updated_at" to FieldValue.serverTimestamp(),
                    ),
                )
            } else {
                newGs["phase"] = "face_down"
                newGs["current_prompt"] = ""
                newGs["selected_choice"] = null
                newGs["current_player_index"] = newIdx
                tx.update(
                    matchRef,
                    mapOf(
                        "game_state" to newGs,
                        "current_turn" to newTurn,
                        "updated_at" to FieldValue.serverTimestamp(),
                    ),
                )
            }
            null
        }.await()
    }

    fun onSkip() {
        val s = _uiState.value
        if (!s.isMyTurn) return
        if (s.phase != CardPhase.ShowingTruth && s.phase != CardPhase.ShowingDare) return
        viewModelScope.launch {
            runCatching { commitSkip() }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    private suspend fun commitSkip() {
        val uid = auth.currentUser?.uid ?: return
        syncQueuesToCounts(_uiState.value.truthsRemaining, _uiState.value.daresRemaining)
        val penalty = _uiState.value.penaltyEnabled
        db.runTransaction { tx ->
            val snap = tx.get(matchRef)
            if (!snap.exists()) throw FirebaseFirestoreException("missing", FirebaseFirestoreException.Code.ABORTED, null)
            if (snap.getString("current_turn") != uid) {
                throw FirebaseFirestoreException("turn", FirebaseFirestoreException.Code.ABORTED, null)
            }
            @Suppress("UNCHECKED_CAST")
            val gs = snap.get("game_state") as? Map<String, Any?> ?: emptyMap()
            val idx = (gs["current_player_index"] as? Number)?.toInt() ?: 0
            @Suppress("UNCHECKED_CAST")
            val skips = (gs["skips_remaining"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() }?.toMutableList()
                ?: mutableListOf(MAX_SKIPS, MAX_SKIPS)
            if (penalty) {
                if (idx >= skips.size || skips[idx] <= 0) {
                    throw FirebaseFirestoreException("skip", FirebaseFirestoreException.Code.ABORTED, null)
                }
                skips[idx] = skips[idx] - 1
            }
            val players = (snap.get("players") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            val n = players.size.coerceAtLeast(2)
            val newIdx = (idx + 1) % n
            val newTurn = players[newIdx]
            var tr = (gs["truths_remaining"] as? Number)?.toInt() ?: 0
            var dr = (gs["dares_remaining"] as? Number)?.toInt() ?: 0
            if (tr != truthsQueue.size || dr != daresQueue.size) {
                throw FirebaseFirestoreException("sync", FirebaseFirestoreException.Code.ABORTED, null)
            }
            val newGs = gs.toMutableMap()
            newGs["skips_remaining"] = skips
            val revealedBase = (gs["cards_revealed"] as? Number)?.toInt() ?: 0
            if (tr <= 0 && dr <= 0) {
                newGs["phase"] = "deck_empty"
                newGs["current_prompt"] = ""
                tx.update(
                    matchRef,
                    mapOf(
                        "game_state" to newGs,
                        "status" to "completed",
                        "current_turn" to newTurn,
                        "updated_at" to FieldValue.serverTimestamp(),
                    ),
                )
            } else if (dr == 0 && tr > 0) {
                val text = truthsQueue.firstOrNull()
                    ?: throw FirebaseFirestoreException("empty", FirebaseFirestoreException.Code.ABORTED, null)
                newGs["phase"] = "showing_truth"
                newGs["current_prompt"] = text
                newGs["pending_reveal_is_truth"] = true
                newGs["truths_remaining"] = tr - 1
                newGs["cards_revealed"] = revealedBase + 1
                newGs["selected_choice"] = null
                newGs["current_player_index"] = newIdx
                tx.update(
                    matchRef,
                    mapOf(
                        "game_state" to newGs,
                        "current_turn" to newTurn,
                        "updated_at" to FieldValue.serverTimestamp(),
                    ),
                )
            } else if (tr == 0 && dr > 0) {
                val text = daresQueue.firstOrNull()
                    ?: throw FirebaseFirestoreException("empty", FirebaseFirestoreException.Code.ABORTED, null)
                newGs["phase"] = "showing_dare"
                newGs["current_prompt"] = text
                newGs["pending_reveal_is_truth"] = false
                newGs["dares_remaining"] = dr - 1
                newGs["cards_revealed"] = revealedBase + 1
                newGs["selected_choice"] = null
                newGs["current_player_index"] = newIdx
                tx.update(
                    matchRef,
                    mapOf(
                        "game_state" to newGs,
                        "current_turn" to newTurn,
                        "updated_at" to FieldValue.serverTimestamp(),
                    ),
                )
            } else {
                newGs["phase"] = "face_down"
                newGs["current_prompt"] = ""
                newGs["selected_choice"] = null
                newGs["current_player_index"] = newIdx
                tx.update(
                    matchRef,
                    mapOf(
                        "game_state" to newGs,
                        "current_turn" to newTurn,
                        "updated_at" to FieldValue.serverTimestamp(),
                    ),
                )
            }
            null
        }.await()
    }

    private fun startTurnTimerIfNeeded() {
        cancelTurnTimer()
        val total = _uiState.value.turnTimerSeconds
        if (total <= 0) return
        _uiState.update { it.copy(timerRemainingSeconds = total) }
        turnTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val cur = _uiState.value
                if (cur.phase != CardPhase.ShowingTruth && cur.phase != CardPhase.ShowingDare) break
                val rem = cur.timerRemainingSeconds ?: break
                if (rem <= 1) {
                    _uiState.update { it.copy(timerRemainingSeconds = 0) }
                    break
                }
                _uiState.update { it.copy(timerRemainingSeconds = rem - 1) }
            }
        }
    }

    private fun cancelTurnTimer() {
        turnTimerJob?.cancel()
        turnTimerJob = null
        _uiState.update { it.copy(timerRemainingSeconds = null) }
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
        cancelTurnTimer()
    }

    companion object {
        fun factory(
            app: CoupleGamesApp,
            matchId: String,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OnlineTruthDareViewModel(
                    app.dataManager,
                    app.analyticsLogger,
                    matchId,
                ) as T
            }
        }
    }
}
