package com.tal.hebrewdino.ui.domain

/**
 * Season 2 finale station (UX station 6): lighter wrong feedback — no coach bubble on every mistake.
 */
object Season2Station6FeedbackPolicy {
    const val CONSECUTIVE_WRONG_INSTRUCTION_THRESHOLD: Int = 2

    /** UX station index 6 in the Season 2 six-station arc (Ch1–3 finale). */
    fun isSeason2FinaleStation(season2UxStationId: Int?): Boolean =
        season2UxStationId == Season2Chapter1StationOrder.MATCH_LETTER_TO_WORD

    fun shouldSkipCoachBubble(
        season2UxStationId: Int?,
        isSeason2Quiz: Boolean,
    ): Boolean = isSeason2Quiz && isSeason2FinaleStation(season2UxStationId)

    fun shouldReplayInstructionAfterWrong(
        consecutiveWrongInRound: Int,
        season2UxStationId: Int?,
        isSeason2Quiz: Boolean,
    ): Boolean =
        shouldSkipCoachBubble(season2UxStationId, isSeason2Quiz) &&
            consecutiveWrongInRound >= CONSECUTIVE_WRONG_INSTRUCTION_THRESHOLD
}
