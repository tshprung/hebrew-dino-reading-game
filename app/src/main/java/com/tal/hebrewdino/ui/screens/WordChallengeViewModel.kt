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
            .take(5)
            .toList()

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
            ),
        )
    val uiState: StateFlow<WordChallengeUiState> = _uiState.asStateFlow()

    private val _finishEvents: MutableSharedFlow<Unit> = MutableSharedFlow(extraBufferCapacity = 1)
    val finishEvents: SharedFlow<Unit> = _finishEvents.asSharedFlow()

    private var advanceJob: Job? = null

    fun onOptionSelected(option: String) {
        val state = _uiState.value
        if (state.isRoundComplete) return
        val current = state.current ?: return
        if (state.selectedCorrectOption != null) return

        if (option == current.correctOption) {
            _uiState.update { it.copy(selectedCorrectOption = option, lastWrongOption = null) }
            advanceJob?.cancel()
            advanceJob =
                viewModelScope.launch {
                    delay(1000L)
                    _uiState.update { s ->
                        val nextIndex = s.index + 1
                        if (nextIndex >= s.challenges.size) {
                            s.copy(
                                index = nextIndex,
                                selectedCorrectOption = null,
                                lastWrongOption = null,
                                isRoundComplete = true,
                            )
                        } else {
                            s.copy(
                                index = nextIndex,
                                selectedCorrectOption = null,
                                lastWrongOption = null,
                            )
                        }
                    }
                    val finalState = _uiState.value
                    if (finalState.isRoundComplete) {
                        rewardHandler.onRoundComplete()
                        _finishEvents.tryEmit(Unit)
                    }
                }
        } else {
            _uiState.update {
                it.copy(
                    wrongAttemptToken = it.wrongAttemptToken + 1,
                    lastWrongOption = option,
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
