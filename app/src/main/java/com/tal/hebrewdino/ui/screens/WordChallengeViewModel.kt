package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tal.hebrewdino.ui.domain.ChallengeType
import com.tal.hebrewdino.ui.domain.WordChallenge
import com.tal.hebrewdino.ui.domain.WordChallengeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class WordChallengeUiState(
    val challenges: List<WordChallenge>,
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

class WordChallengeViewModel : ViewModel() {
    private val challenges: List<WordChallenge> =
        WordChallengeRepository.oddOneOutHebrew
            .asSequence()
            .filter { it.challengeType == ChallengeType.ODD_ONE_OUT }
            .filter { it.options.size == 4 && it.options.contains(it.correctOption) }
            .take(5)
            .toList()

    private val _uiState =
        MutableStateFlow(
            WordChallengeUiState(
                challenges = challenges,
                index = 0,
                selectedCorrectOption = null,
                wrongAttemptToken = 0,
                lastWrongOption = null,
                isRoundComplete = challenges.isEmpty(),
            ),
        )
    val uiState: StateFlow<WordChallengeUiState> = _uiState.asStateFlow()

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
}

