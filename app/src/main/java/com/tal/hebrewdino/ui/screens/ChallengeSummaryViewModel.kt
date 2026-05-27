package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tal.hebrewdino.ui.domain.economy.PendingRewardEvent
import com.tal.hebrewdino.ui.domain.cosmetics.accessoryCelebrationSpokenForTts
import com.tal.hebrewdino.ui.domain.economy.FoodRewardKind
import com.tal.hebrewdino.ui.domain.economy.fanfareDisplayTextForFood
import com.tal.hebrewdino.ui.economy.RewardEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class ChallengeSummaryUiState(
    val event: PendingRewardEvent? = null,
    val displayText: String = "",
    val accessoryCelebrationSpeech: String = "",
    val presentationStarted: Boolean = false,
    val presentationFinished: Boolean = false,
)

class ChallengeSummaryViewModel(
    private val rewardEngine: RewardEngine,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChallengeSummaryUiState())
    val uiState: StateFlow<ChallengeSummaryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val event = rewardEngine.peekPendingEvent()
            _uiState.value =
                ChallengeSummaryUiState(
                    event = event,
                    displayText =
                        event?.let { ev ->
                            fanfareDisplayTextForFood(
                                ev.applesCount,
                                FoodRewardKind(
                                    nameSingularHe = ev.foodNameSingularHe,
                                    namePluralHe = ev.foodNamePluralHe,
                                    emoji = ev.foodEmoji,
                                ),
                            )
                        } ?: "",
                    accessoryCelebrationSpeech =
                        event?.accessoryUnlockId?.let(::accessoryCelebrationSpokenForTts).orEmpty(),
                )
        }
    }

    fun onPresentationStarted() {
        _uiState.update { it.copy(presentationStarted = true) }
    }

    fun onPresentationFinished() {
        val eventId = _uiState.value.event?.eventId ?: return
        if (_uiState.value.presentationFinished) return
        _uiState.update { it.copy(presentationFinished = true) }
        viewModelScope.launch {
            rewardEngine.markPresented(eventId)
        }
    }

    class Factory(
        private val rewardEngine: RewardEngine,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChallengeSummaryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ChallengeSummaryViewModel(rewardEngine) as T
            }
            error("Unknown ViewModel class: $modelClass")
        }
    }
}
