package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tal.hebrewdino.ui.domain.LetterPoolSpec
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.StationQuizPlan

internal class GameViewModel(
    plan: StationQuizPlan,
    letterPoolSpec: LetterPoolSpec,
) : ViewModel() {
    val session: LevelSession = LevelSession(plan = plan, letterPoolSpec = letterPoolSpec)

    var phase: GamePhase by mutableStateOf(GamePhase.Intro)
    var inputLocked: Boolean by mutableStateOf(true)

    class Factory(
        private val plan: StationQuizPlan,
        private val letterPoolSpec: LetterPoolSpec,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return GameViewModel(plan = plan, letterPoolSpec = letterPoolSpec) as T
            }
            error("Unknown ViewModel class: $modelClass")
        }
    }
}
