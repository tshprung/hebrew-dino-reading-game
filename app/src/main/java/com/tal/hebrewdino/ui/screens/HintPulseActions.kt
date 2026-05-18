package com.tal.hebrewdino.ui.screens

internal object HintPulseActions {
    fun registerWrongTapForHintPulse(gameViewModel: GameViewModel) {
        val (updatedWrongTaps, updatedHintEpoch) =
            registerWrongTapForHintPulse(
                wrongTapsThisQuestion = gameViewModel.wrongTapsThisQuestion,
                hintPulseEpoch = gameViewModel.hintPulseEpoch,
            )
        gameViewModel.wrongTapsThisQuestion = updatedWrongTaps
        gameViewModel.hintPulseEpoch = updatedHintEpoch
    }

    fun registerWrongTapForHintPulse(
        wrongTapsThisQuestion: Int,
        hintPulseEpoch: Int,
    ): Pair<Int, Int> {
        val updatedWrongTaps = wrongTapsThisQuestion + 1
        val updatedHintEpoch = if (updatedWrongTaps >= 2) hintPulseEpoch + 1 else hintPulseEpoch
        return updatedWrongTaps to updatedHintEpoch
    }
}

