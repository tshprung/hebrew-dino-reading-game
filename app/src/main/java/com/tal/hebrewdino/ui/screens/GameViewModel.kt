package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.graphics.Color
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
    var wrongTapsThisQuestion: Int by mutableIntStateOf(0)
    var hintPulseEpoch: Int by mutableIntStateOf(0)
    var correctTapPulseEpoch: Int by mutableIntStateOf(0)
    var correctTapPulseLetter: String? by mutableStateOf(null)
    var station4WrongFlashEpoch: Int by mutableIntStateOf(0)
    var station4WrongFlashLetter: String? by mutableStateOf(null)
    var station4PinnedCorrectLetter: String? by mutableStateOf(null)
    var station1PinnedCorrectLetter: String? by mutableStateOf(null)
    var station2PinnedBalloonLetter: String? by mutableStateOf(null)
    var station2PinnedBalloonColor: Color? by mutableStateOf(null)
    var station2CorrectPopCount: Int by mutableIntStateOf(0)
    var shakeEpoch: Int by mutableIntStateOf(0)
    var entryPulseEpoch: Int by mutableIntStateOf(0)
    var completionCallbackFired: Boolean by mutableStateOf(false)
    var dinoVisual: DinoVisual by mutableStateOf(DinoVisual.Idle)
    var dinoTalking: Boolean by mutableStateOf(false)
    var jumpFrameIndex: Int by mutableIntStateOf(0)

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
