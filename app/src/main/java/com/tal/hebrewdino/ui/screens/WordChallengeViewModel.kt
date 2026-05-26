package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tal.hebrewdino.ui.domain.ChallengeType
import com.tal.hebrewdino.ui.domain.WordChallenge
import com.tal.hebrewdino.ui.domain.WordChallengeRepository
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
    private val rewardHandler: RewardHandler = RewardHandler { },
) : ViewModel() {
    private val challenges: List<WordChallenge> =
        when (challengeType) {
            ChallengeType.LETTER_RECOGNITION -> WordChallengeRepository.letterRecognitionHebrewChapter1
            ChallengeType.PHONEMIC_ISOLATION -> WordChallengeRepository.phonemicIsolationHebrewChapter1Station2
            ChallengeType.ODD_ONE_OUT -> WordChallengeRepository.oddOneOutHebrew
            ChallengeType.RHYME -> WordChallengeRepository.rhymesHebrew
            ChallengeType.WORD_MATCH -> emptyList()
            ChallengeType.IMAGE_MATCH -> emptyList()
        }
            .asSequence()
            .filter { it.challengeType == challengeType }
            .filter { it.options.size == 4 && it.options.contains(it.correctOption) }
            .map { challenge ->
                val seeded = Random(challenge.id.hashCode())
                challenge.copy(options = challenge.options.shuffled(seeded))
            }
            .toList()
            .let { base ->
                if (base.isEmpty()) return@let emptyList()
                val desired =
                    when (challengeType) {
                        ChallengeType.LETTER_RECOGNITION,
                        ChallengeType.PHONEMIC_ISOLATION,
                        -> 8
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
                rewardHandler.onRoundComplete()
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

    fun interface RewardHandler {
        suspend fun onRoundComplete()
    }

    class Factory(
        private val challengeType: ChallengeType,
        private val rewardHandler: RewardHandler = RewardHandler { },
    ) : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WordChallengeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return WordChallengeViewModel(challengeType = challengeType, rewardHandler = rewardHandler) as T
            }
            error("Unknown ViewModel class: $modelClass")
        }
    }
}
