package com.tal.hebrewdino.ui.screens

internal object HintPulseActions {
    fun registerWrongTapForHintPulse(
        wrongTapsThisQuestion: Int,
        hintPulseEpoch: Int,
    ): Pair<Int, Int> {
        val updatedWrongTaps = wrongTapsThisQuestion + 1
        val updatedHintEpoch = if (updatedWrongTaps >= 2) hintPulseEpoch + 1 else hintPulseEpoch
        return updatedWrongTaps to updatedHintEpoch
    }
}

