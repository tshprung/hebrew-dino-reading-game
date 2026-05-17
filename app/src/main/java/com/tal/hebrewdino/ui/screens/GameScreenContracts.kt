package com.tal.hebrewdino.ui.screens

data class GameScreenUiState(
    val isIntroPhase: Boolean,
    val isPlayPhase: Boolean,
    val isInputLocked: Boolean,
    val hintOverlayLetter: String?,
    val showTargetLetterChip: Boolean,
)

sealed interface GameScreenAction {
    data object OnReplayPressed : GameScreenAction

    data object OnHintPressed : GameScreenAction

    data class OnPickLetter(val letter: String) : GameScreenAction

    data class OnPickImageMatchChoice(val choiceId: String) : GameScreenAction

    data class OnPopBalloon(val letter: String) : GameScreenAction
}
