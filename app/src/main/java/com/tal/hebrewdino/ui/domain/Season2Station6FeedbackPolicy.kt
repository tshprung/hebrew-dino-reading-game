package com.tal.hebrewdino.ui.domain

/**
 * Companion focus line after two consecutive mistakes (before full coach bubble path).
 */
object Season2Station6FeedbackPolicy {
    const val CONSECUTIVE_WRONG_INSTRUCTION_THRESHOLD: Int = 2

    fun shouldUseCompanionFocusFeedback(companionCoachEnabled: Boolean): Boolean = companionCoachEnabled

    fun shouldReplayInstructionAfterWrong(
        consecutiveWrongInRound: Int,
        companionCoachEnabled: Boolean,
    ): Boolean =
        shouldUseCompanionFocusFeedback(companionCoachEnabled) &&
            consecutiveWrongInRound >= CONSECUTIVE_WRONG_INSTRUCTION_THRESHOLD
}
