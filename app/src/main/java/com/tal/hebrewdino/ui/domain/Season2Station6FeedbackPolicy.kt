package com.tal.hebrewdino.ui.domain

/**
 * Season 2 quiz stations: companion focus line after two consecutive mistakes (Ch1 St6 model).
 */
object Season2Station6FeedbackPolicy {
    const val CONSECUTIVE_WRONG_INSTRUCTION_THRESHOLD: Int = 2

    /** UX station index 6 in the Season 2 six-station arc (Ch1–3 finale). */
    fun isSeason2FinaleStation(season2UxStationId: Int?): Boolean =
        season2UxStationId == Season2Chapter1StationOrder.MATCH_LETTER_TO_WORD

    fun shouldUseCompanionFocusFeedback(isSeason2Quiz: Boolean): Boolean = isSeason2Quiz

    fun shouldUseLightWordChoiceFeedback(
        season2UxStationId: Int?,
        isSeason2Quiz: Boolean,
        season2AdvancedMode: Season2AdvancedStationMode? = null,
    ): Boolean = shouldUseCompanionFocusFeedback(isSeason2Quiz)

    fun shouldSkipCoachBubble(
        season2UxStationId: Int?,
        isSeason2Quiz: Boolean,
        season2AdvancedMode: Season2AdvancedStationMode? = null,
    ): Boolean = shouldUseCompanionFocusFeedback(isSeason2Quiz)

    fun shouldReplayInstructionAfterWrong(
        consecutiveWrongInRound: Int,
        season2UxStationId: Int?,
        isSeason2Quiz: Boolean,
        season2AdvancedMode: Season2AdvancedStationMode? = null,
    ): Boolean =
        shouldUseCompanionFocusFeedback(isSeason2Quiz) &&
            consecutiveWrongInRound >= CONSECUTIVE_WRONG_INSTRUCTION_THRESHOLD
}
