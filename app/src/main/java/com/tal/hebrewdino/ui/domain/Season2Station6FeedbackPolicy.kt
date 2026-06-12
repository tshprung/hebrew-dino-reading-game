package com.tal.hebrewdino.ui.domain

/**
 * Season 2 quiz stations: companion focus line after two consecutive mistakes (Ch1 St6 model).
 */
object Season2Station6FeedbackPolicy {
    const val CONSECUTIVE_WRONG_INSTRUCTION_THRESHOLD: Int = 2

    fun shouldUseCompanionFocusFeedback(isSeason2Quiz: Boolean): Boolean = isSeason2Quiz

    fun shouldSkipCoachBubble(isSeason2Quiz: Boolean): Boolean = shouldUseCompanionFocusFeedback(isSeason2Quiz)

    fun shouldReplayInstructionAfterWrong(
        consecutiveWrongInRound: Int,
        isSeason2Quiz: Boolean,
    ): Boolean =
        shouldUseCompanionFocusFeedback(isSeason2Quiz) &&
            consecutiveWrongInRound >= CONSECUTIVE_WRONG_INSTRUCTION_THRESHOLD
}
