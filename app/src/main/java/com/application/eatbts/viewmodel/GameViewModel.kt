package com.application.eatbts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.application.eatbts.data.DataManager
import com.application.eatbts.data.GameConfig
import com.application.eatbts.data.Level
import com.application.eatbts.data.PoolMode
import com.application.eatbts.data.PromptLine
import com.application.eatbts.data.PromptPack
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

private const val MAX_SKIPS_PER_PLAYER = 3

enum class CardPhase {
    FaceDown,
    Flipping,
    ShowingTruth,
    ShowingDare,
    DeckEmpty,
}

data class GameUiState(
    val phase: CardPhase = CardPhase.FaceDown,
    val currentPrompt: String = "",
    /** Valid while phase is [CardPhase.Flipping] after [onFlipMidpoint]. */
    val pendingRevealIsTruth: Boolean = true,
    val truthsRemaining: Int = 0,
    val daresRemaining: Int = 0,
    val totalInSession: Int = 0,
    val cardsRevealed: Int = 0,
    /** Parallel to [playerNames]; skip budget per player. */
    val skipsRemaining: List<Int> = listOf(MAX_SKIPS_PER_PLAYER, MAX_SKIPS_PER_PLAYER),
    /** Index into [playerNames] for the current turn. */
    val currentPlayerIndex: Int = 0,
    /** Display names for each player (2–4 entries in multi-player mode). */
    val playerNames: List<String> = listOf("", ""),
    /** Set before flip; null means user must pick Truth or Dare (unless auto-filled). */
    val selectedChoice: TruthDareChoice? = null,
    val level: Level = Level.MILD,
    val lastWasDare: Boolean = false,
    /** From session [GameConfig]; used for timer bar denominator. */
    val turnTimerSeconds: Int = 30,
    /** Countdown while phase is ShowingTruth/ShowingDare; null otherwise. */
    val timerRemainingSeconds: Int? = null,
    /** In-session: skip applies extra “penalty” semantics when on (uses a skip). */
    val penaltyEnabled: Boolean = true,
)

class GameViewModel(
    private val dataManager: DataManager,
    private val config: GameConfig,
    private val random: Random = Random(System.nanoTime()),
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var turnTimerJob: Job? = null

    private var truthsQueue: MutableList<String> = mutableListOf()
    private var daresQueue: MutableList<String> = mutableListOf()
    private var playerCount: Int = 2
    private var currentPlayerIndexInternal: Int = 0

    init {
        startSession()
    }

    private fun resolvedPlayerNames(config: GameConfig): List<String> {
        val fromList = config.playerNames.map { it.trim() }.filter { it.isNotEmpty() }
        if (fromList.size >= 2) return fromList.take(4)
        val a = config.maleName.trim().ifEmpty { "Player 1" }
        val b = config.femaleName.trim().ifEmpty { "Player 2" }
        return listOf(a, b)
    }

    private fun initialTurnIndex(config: GameConfig, n: Int): Int {
        if (config.playerNames.size >= 2) {
            return config.firstPlayerIndex.coerceIn(0, n - 1)
        }
        return if (config.firstTurnIsPlayerOne) 0 else 1.coerceIn(0, n - 1)
    }

    fun startSession() {
        val pack = if (config.useCustomSessionPool) {
            PromptPack(
                truths = if (config.includeTruths) {
                    config.sessionTruths.map { PromptLine(it, 0) }
                } else {
                    emptyList()
                },
                dares = if (config.includeDares) {
                    config.sessionDares.map { PromptLine(it, 0) }
                } else {
                    emptyList()
                },
            )
        } else {
            dataManager.buildSessionPack(
                dataManager.loadLevel(config.level).getOrElse {
                    _uiState.update { it.copy(phase = CardPhase.DeckEmpty) }
                    return
                },
                emptyList(),
                emptyList(),
            )
        }
        val effectivePoolMode = if (config.useCustomSessionPool) PoolMode.ALL else config.poolMode
        val (t, d) = buildSessionQueues(
            pack = pack,
            includeTruths = config.includeTruths,
            includeDares = config.includeDares,
            poolMode = effectivePoolMode,
            random = random,
        )
        truthsQueue = t
        daresQueue = d
        val total = truthsQueue.size + daresQueue.size
        val names = resolvedPlayerNames(config)
        playerCount = names.size.coerceIn(2, 4)
        val skips = List(playerCount) { MAX_SKIPS_PER_PLAYER }
        currentPlayerIndexInternal = initialTurnIndex(config, playerCount)
        val initialChoice = autoFillChoiceIfOnlyOneType()
        turnTimerJob?.cancel()
        turnTimerJob = null
        _uiState.value = GameUiState(
            phase = when {
                total == 0 -> CardPhase.DeckEmpty
                initialChoice != null -> CardPhase.Flipping
                else -> CardPhase.FaceDown
            },
            truthsRemaining = truthsQueue.size,
            daresRemaining = daresQueue.size,
            totalInSession = total,
            cardsRevealed = 0,
            skipsRemaining = skips,
            currentPlayerIndex = currentPlayerIndexInternal,
            playerNames = names.take(playerCount),
            selectedChoice = initialChoice,
            level = config.level,
            turnTimerSeconds = config.turnTimerSeconds.coerceAtLeast(0),
            timerRemainingSeconds = null,
            penaltyEnabled = true,
        )
    }

    fun setPenaltyEnabled(enabled: Boolean) {
        _uiState.update { it.copy(penaltyEnabled = enabled) }
    }

    fun setTruthDareChoice(choice: TruthDareChoice) {
        val s = _uiState.value
        if (s.phase != CardPhase.FaceDown) return
        val ok = when (choice) {
            TruthDareChoice.TRUTH -> truthsQueue.isNotEmpty()
            TruthDareChoice.DARE -> daresQueue.isNotEmpty()
        }
        if (!ok) return
        _uiState.update {
            it.copy(
                selectedChoice = choice,
                phase = CardPhase.Flipping,
            )
        }
    }

    /**
     * Called when the card has rotated to 90°; phase stays [CardPhase.Flipping] until [onFlipComplete].
     */
    fun onFlipMidpoint() {
        if (_uiState.value.phase != CardPhase.Flipping) return
        val choice = _uiState.value.selectedChoice ?: run {
            _uiState.update { it.copy(phase = CardPhase.FaceDown) }
            return
        }
        val pick = pickTruthOrDare(choice)
        if (pick == null) {
            cancelTurnTimer()
            _uiState.update { it.copy(phase = CardPhase.DeckEmpty, currentPrompt = "") }
            return
        }
        val (isTruth, text) = pick
        _uiState.update {
            it.copy(
                currentPrompt = text,
                truthsRemaining = truthsQueue.size,
                daresRemaining = daresQueue.size,
                cardsRevealed = it.cardsRevealed + 1,
                lastWasDare = !isTruth,
                pendingRevealIsTruth = isTruth,
            )
        }
    }

    /** Called after the second half of the flip animation (card fully revealed). */
    fun onFlipComplete() {
        val s = _uiState.value
        if (s.phase != CardPhase.Flipping) return
        if (s.currentPrompt.isEmpty() && truthsQueue.isEmpty() && daresQueue.isEmpty()) {
            cancelTurnTimer()
            _uiState.update { it.copy(phase = CardPhase.DeckEmpty) }
            return
        }
        val isTruth = s.pendingRevealIsTruth
        _uiState.update {
            it.copy(
                phase = if (isTruth) CardPhase.ShowingTruth else CardPhase.ShowingDare,
            )
        }
        startTurnTimer()
    }

    private fun cancelTurnTimer() {
        turnTimerJob?.cancel()
        turnTimerJob = null
        _uiState.update { it.copy(timerRemainingSeconds = null) }
    }

    private fun startTurnTimer() {
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

    fun onNext() {
        when (_uiState.value.phase) {
            CardPhase.ShowingTruth, CardPhase.ShowingDare -> {
                cancelTurnTimer()
                currentPlayerIndexInternal = (currentPlayerIndexInternal + 1) % playerCount
                if (truthsQueue.isEmpty() && daresQueue.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            phase = CardPhase.DeckEmpty,
                            currentPrompt = "",
                            currentPlayerIndex = currentPlayerIndexInternal,
                            selectedChoice = null,
                        )
                    }
                } else {
                    val nextChoice = autoFillChoiceIfOnlyOneType()
                    _uiState.update {
                        it.copy(
                            phase = if (nextChoice != null) {
                                CardPhase.Flipping
                            } else {
                                CardPhase.FaceDown
                            },
                            currentPrompt = "",
                            truthsRemaining = truthsQueue.size,
                            daresRemaining = daresQueue.size,
                            currentPlayerIndex = currentPlayerIndexInternal,
                            selectedChoice = nextChoice,
                        )
                    }
                }
            }
            else -> {}
        }
    }

    fun onSkip() {
        val s = _uiState.value
        if (s.phase != CardPhase.ShowingTruth && s.phase != CardPhase.ShowingDare) return
        val idx = s.currentPlayerIndex.coerceIn(0, s.playerNames.lastIndex.coerceAtLeast(0))
        if (s.penaltyEnabled) {
            val canSkip = s.skipsRemaining.getOrElse(idx) { 0 } > 0
            if (!canSkip) return
            _uiState.update {
                val nextSkips = it.skipsRemaining.toMutableList()
                if (idx < nextSkips.size) {
                    nextSkips[idx] = (nextSkips[idx] - 1).coerceAtLeast(0)
                }
                it.copy(skipsRemaining = nextSkips)
            }
        }
        onNext()
    }

    private fun autoFillChoiceIfOnlyOneType(): TruthDareChoice? = when {
        truthsQueue.isNotEmpty() && daresQueue.isEmpty() -> TruthDareChoice.TRUTH
        daresQueue.isNotEmpty() && truthsQueue.isEmpty() -> TruthDareChoice.DARE
        else -> null
    }

    private fun pickTruthOrDare(choice: TruthDareChoice): Pair<Boolean, String>? {
        return when (choice) {
            TruthDareChoice.TRUTH -> {
                if (truthsQueue.isEmpty()) return null
                val t = truthsQueue.removeAt(0)
                true to t
            }
            TruthDareChoice.DARE -> {
                if (daresQueue.isEmpty()) return null
                val d = daresQueue.removeAt(0)
                false to d
            }
        }
    }

    companion object {
        fun factory(
            app: com.application.eatbts.CoupleGamesApp,
            config: GameConfig,
            random: Random = Random(System.nanoTime()),
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return GameViewModel(app.dataManager, config, random) as T
            }
        }
    }
}

/**
 * Sorts by tier ascending, then shuffles within each tier run.
 * If every line has [PromptLine.tier] == 0, behaves as a full shuffle (legacy string-only packs).
 */
internal fun orderPromptLines(lines: List<PromptLine>, random: Random): List<PromptLine> {
    if (lines.isEmpty()) return emptyList()
    if (lines.all { it.tier == 0 }) {
        return lines.shuffled(random)
    }
    val sorted = lines.sortedBy { it.tier }
    val result = mutableListOf<PromptLine>()
    var i = 0
    while (i < sorted.size) {
        val tier = sorted[i].tier
        val run = mutableListOf<PromptLine>()
        while (i < sorted.size && sorted[i].tier == tier) {
            run.add(sorted[i])
            i++
        }
        run.shuffle(random)
        result.addAll(run)
    }
    return result
}

internal fun buildSessionQueues(
    pack: PromptPack,
    includeTruths: Boolean,
    includeDares: Boolean,
    poolMode: PoolMode,
    random: Random,
): Pair<MutableList<String>, MutableList<String>> {
    var truths = if (includeTruths) pack.truths.toMutableList() else mutableListOf()
    var dares = if (includeDares) pack.dares.toMutableList() else mutableListOf()
    truths = orderPromptLines(truths, random).toMutableList()
    dares = orderPromptLines(dares, random).toMutableList()
    if (poolMode == PoolMode.ALL) {
        return Pair(
            truths.map { it.text }.toMutableList(),
            dares.map { it.text }.toMutableList(),
        )
    }
    val n = when (poolMode) {
        PoolMode.RANDOM_20 -> 20
        PoolMode.RANDOM_50 -> 50
        PoolMode.ALL -> Int.MAX_VALUE
    }
    val totalAvailable = truths.size + dares.size
    val take = minOf(n, totalAvailable)
    val tagged = mutableListOf<Pair<Boolean, PromptLine>>()
    truths.forEach { tagged.add(true to it) }
    dares.forEach { tagged.add(false to it) }
    tagged.shuffle(random)
    val selected = tagged.take(take)
    val tOutLines = selected.filter { it.first }.map { it.second }
    val dOutLines = selected.filter { !it.first }.map { it.second }
    return Pair(
        orderPromptLines(tOutLines, random).map { it.text }.toMutableList(),
        orderPromptLines(dOutLines, random).map { it.text }.toMutableList(),
    )
}
