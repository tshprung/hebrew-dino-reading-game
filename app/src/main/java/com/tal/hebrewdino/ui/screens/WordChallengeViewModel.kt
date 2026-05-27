package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tal.hebrewdino.ui.domain.ChallengeType
import com.tal.hebrewdino.ui.domain.HebrewSyllabus
import com.tal.hebrewdino.ui.domain.WordChallenge
import com.tal.hebrewdino.ui.domain.WordChallengeRepository
import com.tal.hebrewdino.ui.domain.economy.StationRoundCompleted
import com.tal.hebrewdino.ui.economy.RewardEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

@Immutable
data class WordChallengeUiState(
    val challenges: List<WordChallenge>,
    val challengeType: ChallengeType,
    val chapterIndex: Int,
    val index: Int,
    val selectedCorrectOption: String?,
    val wrongAttemptToken: Int,
    val lastWrongOption: String?,
    val isRoundComplete: Boolean,
    val isInputLocked: Boolean,
    val pendingCorrectToken: Int,
) {
    val current: WordChallenge? get() = challenges.getOrNull(index)
    val total: Int get() = challenges.size
    val questionNumber: Int get() = (index + 1).coerceAtMost(total)
}

class WordChallengeViewModel(
    private val challengeType: ChallengeType = ChallengeType.ODD_ONE_OUT,
    private val chapterIndex: Int = 0,
    private val rewardEngine: RewardEngine,
    private val stationRoundCompleted: StationRoundCompleted?,
) : ViewModel() {
    private val chapterLetters: List<String> =
        HebrewSyllabus.chapterOrNull(chapterIndex)?.letters
            ?: HebrewSyllabus.chapters.first().letters

    private val challenges: List<WordChallenge> =
        when (challengeType) {
            ChallengeType.LETTER_RECOGNITION ->
                WordChallengeRepository.letterRecognitionForChapter(chapterLetters)
            ChallengeType.PHONEMIC_ISOLATION ->
                WordChallengeRepository.phonemicIsolationForChapter(chapterLetters)
            ChallengeType.ODD_ONE_OUT -> WordChallengeRepository.oddOneOutHebrew
            ChallengeType.RHYME -> WordChallengeRepository.rhymesHebrew
            ChallengeType.WORD_MATCH -> emptyList()
            ChallengeType.IMAGE_MATCH -> emptyList()
        }
            .asSequence()
            .filter { it.challengeType == challengeType }
            .filter {
                when (challengeType) {
                    ChallengeType.LETTER_RECOGNITION,
                    ChallengeType.PHONEMIC_ISOLATION,
                    -> it.options.size >= 3 && it.options.contains(it.correctOption)
                    else -> it.options.size == 4 && it.options.contains(it.correctOption)
                }
            }
            .map { challenge ->
                val seeded = Random(challenge.id.hashCode())
                when (challengeType) {
                    ChallengeType.LETTER_RECOGNITION,
                    ChallengeType.PHONEMIC_ISOLATION,
                    ->
                        challenge.copy(
                            options =
                                pickThreeShuffledOptions(
                                    allOptions = challenge.options,
                                    correctOption = challenge.correctOption,
                                    rng = seeded,
                                ),
                        )
                    else -> challenge.copy(options = challenge.options.shuffled(seeded))
                }
            }
            .toList()
            .let { base ->
                if (base.isEmpty()) return@let emptyList()
                val desired =
                    when (challengeType) {
                        ChallengeType.LETTER_RECOGNITION,
                        ChallengeType.PHONEMIC_ISOLATION,
                        -> WordChallengeRepository.LETTER_STATION_ROUNDS
                        else -> 5
                    }
                if (base.size >= desired) {
                    base.take(desired)
                } else {
                    List(desired) { i ->
                        val src = base[i % base.size]
                        src.copy(id = "${src.id}_r$i")
                    }
                }
            }

    private val _uiState =
        MutableStateFlow(
            WordChallengeUiState(
                challenges = challenges,
                challengeType = challengeType,
                chapterIndex = chapterIndex,
                index = 0,
                selectedCorrectOption = null,
                wrongAttemptToken = 0,
                lastWrongOption = null,
                isRoundComplete = challenges.isEmpty(),
                isInputLocked = false,
                pendingCorrectToken = 0,
            ),
        )
    val uiState: StateFlow<WordChallengeUiState> = _uiState.asStateFlow()

    private val _finishEvents: MutableSharedFlow<Unit> = MutableSharedFlow(extraBufferCapacity = 1)
    val finishEvents: SharedFlow<Unit> = _finishEvents.asSharedFlow()

    private var unlockJob: Job? = null

    fun onOptionSelected(option: String) {
        val state = _uiState.value
        if (state.isRoundComplete) return
        if (state.isInputLocked) return
        val current = state.current ?: return
        if (state.selectedCorrectOption != null) return

        unlockJob?.cancel()
        _uiState.update { it.copy(isInputLocked = true) }

        if (option == current.correctOption) {
            _uiState.update {
                it.copy(
                    selectedCorrectOption = option,
                    lastWrongOption = null,
                    pendingCorrectToken = it.pendingCorrectToken + 1,
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    wrongAttemptToken = it.wrongAttemptToken + 1,
                    lastWrongOption = option,
                )
            }
            val token = state.wrongAttemptToken + 1
            val idx = state.index
            unlockJob =
                viewModelScope.launch {
                    delay(420L)
                    _uiState.update { s ->
                        if (s.isRoundComplete) return@update s
                        if (s.index != idx) return@update s
                        if (s.selectedCorrectOption != null) return@update s
                        if (s.wrongAttemptToken != token) return@update s
                        s.copy(isInputLocked = false)
                    }
                }
        }
    }

    fun confirmAdvanceAfterCorrect(expectedToken: Int) {
        val state = _uiState.value
        if (state.isRoundComplete) return
        if (state.pendingCorrectToken != expectedToken) return
        if (state.selectedCorrectOption.isNullOrBlank()) return

        val isLast = state.index >= state.challenges.lastIndex
        if (isLast) {
            _uiState.update { s ->
                s.copy(
                    selectedCorrectOption = null,
                    lastWrongOption = null,
                    isRoundComplete = true,
                    isInputLocked = true,
                )
            }
            viewModelScope.launch {
                stationRoundCompleted?.let { rewardEngine.grantStationRoundCompleted(it) }
                _finishEvents.tryEmit(Unit)
            }
        } else {
            _uiState.update { s ->
                s.copy(
                    index = s.index + 1,
                    selectedCorrectOption = null,
                    lastWrongOption = null,
                    isInputLocked = false,
                )
            }
        }
    }

    private companion object {
        fun pickThreeShuffledOptions(
            allOptions: List<String>,
            correctOption: String,
            rng: Random,
        ): List<String> {
            val wrong = allOptions.filter { it != correctOption }.distinct()
            val distractors = wrong.shuffled(rng).take(2)
            val picked =
                if (distractors.size >= 2) {
                    distractors
                } else {
                    (distractors + wrong).distinct().take(2)
                }
            return (listOf(correctOption) + picked).shuffled(rng)
        }
    }

    class Factory(
        private val challengeType: ChallengeType,
        private val chapterIndex: Int = 0,
        private val rewardEngine: RewardEngine,
    ) : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WordChallengeViewModel::class.java)) {
                val stationId =
                    when (challengeType) {
                        ChallengeType.LETTER_RECOGNITION -> 1
                        ChallengeType.PHONEMIC_ISOLATION -> 2
                        else -> 0
                    }
                val completed =
                    if (stationId > 0) {
                        StationRoundCompleted(
                            chapterIndex = chapterIndex,
                            stationId = stationId,
                        )
                    } else {
                        null
                    }
                @Suppress("UNCHECKED_CAST")
                return WordChallengeViewModel(
                    challengeType = challengeType,
                    chapterIndex = chapterIndex,
                    rewardEngine = rewardEngine,
                    stationRoundCompleted = completed,
                ) as T
            }
            error("Unknown ViewModel class: $modelClass")
        }
    }
}
