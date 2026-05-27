package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tal.hebrewdino.ui.domain.HebrewSyllabus
import com.tal.hebrewdino.ui.domain.hebrewLetterBase
import com.tal.hebrewdino.ui.domain.letterWithRandomNiqqudForFalling
import com.tal.hebrewdino.ui.domain.economy.StationRoundCompleted
import com.tal.hebrewdino.ui.economy.RewardEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

@Immutable
data class FallingLetter(
    val id: Int,
    val text: String,
    val lane: Int,
    val xInLane01: Float,
    val yPx: Float,
)

@Immutable
data class FallingLettersUiState(
    val targetLetter: String,
    val roundIndex: Int,
    val roundsTotal: Int,
    val caughtInRound: Int,
    val quotaInRound: Int,
    val letters: List<FallingLetter>,
    val feedbackLetterId: Int?,
    val feedbackIsCorrect: Boolean?,
    val feedbackToken: Int,
    val successFlashToken: Int,
    val shakeToken: Int,
    val isComplete: Boolean,
    val inputsLocked: Boolean,
    val roundBreakToken: Int,
)

class FallingLettersViewModel(
    chapterLetters: List<String> = HebrewSyllabus.chapters[0].letters,
    private val rng: Random = Random.Default,
    private val rewardEngine: RewardEngine,
    private val stationRoundCompleted: StationRoundCompleted,
    private val targetsPerRound: Int = 5,
) : ViewModel() {
    private val alphabet: List<String> =
        chapterLetters.distinct().filter { it.isNotBlank() }.ifEmpty {
            HebrewSyllabus.chapters[0].letters
        }
    private val roundsTotal: Int = 3
    private val roundTargets: List<String> = alphabet.shuffled(rng).distinct().take(roundsTotal)

    private var nextId: Int = 1
    private var gameHeightPx: Float = 0f
    private var chipHeightPx: Float = 60f
    private val minLaneGapFactor: Float = 1.28f
    /** Max falling distractors while the round target is already visible. */
    private val maxDistractorsOnScreen: Int = 4
    private var spawnDelayMs: Long = 0L
    private var pendingRoundBreakToken: Int = 0
    private var pendingRoundBreakRoundIndex: Int = 0

    private val _finishEvents: MutableSharedFlow<Unit> = MutableSharedFlow(extraBufferCapacity = 1)
    val finishEvents: SharedFlow<Unit> = _finishEvents.asSharedFlow()

    private val _roundBreakEvents: MutableSharedFlow<Int> = MutableSharedFlow(extraBufferCapacity = 1)
    val roundBreakEvents: SharedFlow<Int> = _roundBreakEvents.asSharedFlow()

    private var tickJob: Job? = null

    private var waveSpeedPxPerTick: Float = 6.2f
    private var tickMs: Long = 90L
    private val wrongDelayMs: Long = 260L

    private val _uiState: MutableStateFlow<FallingLettersUiState> = MutableStateFlow(initialState())
    val uiState = _uiState.asStateFlow()

    private fun initialState(): FallingLettersUiState {
        val target = roundTargets.firstOrNull() ?: alphabet.first()
        spawnDelayMs = 0L
        return FallingLettersUiState(
            targetLetter = target,
            roundIndex = 0,
            roundsTotal = roundsTotal,
            caughtInRound = 0,
            quotaInRound = targetsPerRound,
            letters = emptyList(),
            feedbackLetterId = null,
            feedbackIsCorrect = null,
            feedbackToken = 0,
            successFlashToken = 0,
            shakeToken = 0,
            isComplete = false,
            inputsLocked = false,
            roundBreakToken = 0,
        )
    }

    fun setViewport(
        heightPx: Float,
        chipHeightPx: Float,
    ) {
        val h = heightPx.coerceAtLeast(0f)
        gameHeightPx = h
        this.chipHeightPx = chipHeightPx.coerceAtLeast(0f)
        if (tickJob == null && h > 0f) {
            tickJob =
                viewModelScope.launch {
                    while (true) {
                        delay(tickMs)
                        tickOnce()
                    }
                }
        }
    }

    fun stopTicker() {
        tickJob?.cancel()
        tickJob = null
    }

    fun onLetterClicked(letterId: Int) {
        val state = _uiState.value
        if (state.isComplete) return
        if (state.inputsLocked) return
        val clicked = state.letters.firstOrNull { it.id == letterId } ?: return
        val correct = hebrewLetterBase(clicked.text) == hebrewLetterBase(state.targetLetter)
        _uiState.value =
            state.copy(
                feedbackLetterId = clicked.id,
                feedbackIsCorrect = correct,
                feedbackToken = state.feedbackToken + 1,
                successFlashToken = if (correct) state.successFlashToken + 1 else state.successFlashToken,
                shakeToken = if (!correct) state.shakeToken + 1 else state.shakeToken,
                inputsLocked = true,
            )
        if (correct) {
            val nextCaught = (state.caughtInRound + 1).coerceAtMost(state.quotaInRound)
            val afterCatch =
                _uiState.value.copy(
                    caughtInRound = nextCaught,
                )
            _uiState.value = afterCatch

            if (nextCaught >= state.quotaInRound) {
                val token = pendingRoundBreakToken + 1
                pendingRoundBreakToken = token
                pendingRoundBreakRoundIndex = state.roundIndex
                _uiState.value =
                    afterCatch.copy(
                        letters = emptyList(),
                        inputsLocked = true,
                        roundBreakToken = token,
                    )
            } else {
                viewModelScope.launch {
                    delay(160L)
                    val s = _uiState.value
                    _uiState.value =
                        s.copy(
                            letters = s.letters.filterNot { it.id == clicked.id },
                            feedbackLetterId = null,
                            feedbackIsCorrect = null,
                            inputsLocked = false,
                            roundBreakToken = 0,
                        )
                }
            }
        } else {
            viewModelScope.launch {
                delay(wrongDelayMs)
                val s = _uiState.value
                _uiState.value =
                    s.copy(
                        letters = s.letters.filterNot { it.id == clicked.id },
                        feedbackLetterId = null,
                        feedbackIsCorrect = null,
                        inputsLocked = false,
                        roundBreakToken = 0,
                    )
            }
        }
    }

    fun onRoundBreakFinished(token: Int) {
        val state = _uiState.value
        if (state.isComplete) return
        if (token != pendingRoundBreakToken) return
        if (state.roundIndex != pendingRoundBreakRoundIndex) return

        val isLastRound = state.roundIndex >= (state.roundsTotal - 1)
        if (isLastRound) {
            _uiState.value = state.copy(isComplete = true, inputsLocked = true)
            stopTicker()
            viewModelScope.launch {
                rewardEngine.grantStationRoundCompleted(stationRoundCompleted)
                _finishEvents.tryEmit(Unit)
            }
            return
        }

        val nextRoundIndex = state.roundIndex + 1
        val nextTarget = roundTargets.getOrNull(nextRoundIndex) ?: alphabet.first()
        spawnDelayMs = 0L
        _uiState.value =
            state.copy(
                roundIndex = nextRoundIndex,
                targetLetter = nextTarget,
                caughtInRound = 0,
                letters = emptyList(),
                feedbackLetterId = null,
                feedbackIsCorrect = null,
                inputsLocked = false,
                roundBreakToken = 0,
            )
    }

    private fun tickOnce() {
        val state = _uiState.value
        if (state.isComplete) {
            stopTicker()
            return
        }
        if (state.inputsLocked) return
        val bottomY = (gameHeightPx - chipHeightPx).coerceAtLeast(0f)
        val moved =
            state.letters
                .asSequence()
                .map { l -> l.copy(yPx = l.yPx + waveSpeedPxPerTick) }
                .filter { l -> bottomY <= 0f || l.yPx < bottomY }
                .toList()

        var nextState = state.copy(letters = moved)

        if (bottomY > 0f) {
            spawnDelayMs -= tickMs
            if (spawnDelayMs <= 0L) {
                val letters = nextState.letters.toMutableList()
                val targetBase = hebrewLetterBase(nextState.targetLetter)
                val hasTarget =
                    letters.any { hebrewLetterBase(it.text) == targetBase }
                if (!hasTarget) {
                    spawnOne(
                        targetLetter = nextState.targetLetter,
                        existing = letters,
                        spawnKind = SpawnKind.TARGET,
                    )?.let { letters += it }
                } else {
                    spawnOne(
                        targetLetter = nextState.targetLetter,
                        existing = letters,
                        spawnKind = SpawnKind.DISTRACTOR,
                    )?.let { letters += it }
                }
                nextState = nextState.copy(letters = letters)
                spawnDelayMs = randomSpawnDelayMs()
            }
        }

        _uiState.value = nextState
    }

    private enum class SpawnKind {
        TARGET,
        DISTRACTOR,
    }

    private fun spawnOne(
        targetLetter: String,
        existing: List<FallingLetter>,
        spawnKind: SpawnKind,
    ): FallingLetter? {
        val gap = chipHeightPx * minLaneGapFactor
        val laneMinY = FloatArray(3) { 0f }
        for (lane in 0..2) {
            laneMinY[lane] =
                existing
                    .asSequence()
                    .filter { it.lane == lane }
                    .map { it.yPx }
                    .minOrNull()
                    ?: 0f
        }
        val laneOrder = MutableList(3) { it }.also { it.shuffle(rng) }
        var pickedLane: Int? = null
        for (lane in laneOrder) {
            val minY = laneMinY[lane]
            if (minY > gap * 0.35f) {
                pickedLane = lane
                break
            }
        }
        if (pickedLane == null) {
            pickedLane = laneOrder[0]
        }

        val lane = pickedLane.coerceIn(0, 2)
        val minY = laneMinY[lane]
        val startY = if (minY <= 0f) (minY - gap) else -gap

        val targetBase = hebrewLetterBase(targetLetter)
        val targetOnScreen = existing.count { hebrewLetterBase(it.text) == targetBase }
        val distractorOnScreen = existing.size - targetOnScreen

        val text =
            when (spawnKind) {
                SpawnKind.TARGET -> {
                    if (targetOnScreen >= 1) return null
                    letterWithRandomNiqqudForFalling(targetLetter, rng)
                }
                SpawnKind.DISTRACTOR -> {
                    if (targetOnScreen < 1) return null
                    if (distractorOnScreen >= maxDistractorsOnScreen) return null
                    if (rng.nextFloat() > 0.28f) return null
                    letterWithRandomNiqqudForFalling(randomDistractorFor(targetLetter), rng)
                }
            }

        return FallingLetter(
            id = nextId++,
            text = text,
            lane = lane,
            xInLane01 = rng.nextFloat().coerceIn(0f, 1f),
            yPx = startY,
        )
    }

    private fun randomDistractorFor(targetLetter: String): String {
        val targetBase = hebrewLetterBase(targetLetter)
        val pool = alphabet.filter { hebrewLetterBase(it) != targetBase }
        if (pool.isEmpty()) return alphabet.firstOrNull() ?: targetLetter
        return pool[rng.nextInt(pool.size)]
    }

    private fun randomSpawnDelayMs(): Long = 130L + rng.nextInt(110)

}
