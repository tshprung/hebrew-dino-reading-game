package com.tal.hebrewdino.ui.screens

sealed interface GameScreenAction {
    data object OnReplayPressed : GameScreenAction

    data object OnHintPressed : GameScreenAction

    data class OnPickLetter(val letter: String) : GameScreenAction

    data class OnPickImageMatchChoice(val choiceId: String) : GameScreenAction

    data class OnPopBalloon(val letter: String) : GameScreenAction
}
