package com.tal.hebrewdino.ui.domain

data class StationQuizPlan(
    val mode: StationQuizMode,
    val questionCount: Int,
    val initialGroupIndex: Int,
    /** Chapter 1 station 5: always three picture cards. */
    val imageMatchAlwaysThreeChoices: Boolean = false,
    /** Multiplier on word caption size under image-match cards (e.g. 1.5f for station 5). */
    val imageMatchCaptionSizeMultiplier: Float = 1f,
    /** Multiplier on image card width/height (e.g. 2f for station 5). */
    val imageMatchPictureSizeMultiplier: Float = 1f,
)

object StationQuizPlans {
    /** Chapter 1 — see [Chapter1StationOrder] for station indices and `Question` subtype mapping. */
    fun chapter1(stationId: Int): StationQuizPlan = Chapter1StationOrder.quizPlan(stationId)

    /** Chapter 2 stations 1–3 and 5 (not picture-match or finale). */
    fun chapter2LetterOnly(stationId: Int): StationQuizPlan? =
        when (stationId) {
            1 -> StationQuizPlan(StationQuizMode.FindLetterGrid, questionCount = 3, initialGroupIndex = 0)
            2 -> StationQuizPlan(StationQuizMode.PopBalloons, questionCount = 4, initialGroupIndex = 0)
            3 -> StationQuizPlan(StationQuizMode.FindLetterGrid, questionCount = 5, initialGroupIndex = 0)
            5 -> StationQuizPlan(StationQuizMode.FindLetterGrid, questionCount = 5, initialGroupIndex = 1)
            else -> null
        }

    /** Chapter 3 path stations replaced by letter quizzes: 1, 3, 5. */
    fun chapter3LetterOnly(stationId: Int): StationQuizPlan? =
        when (stationId) {
            1 -> StationQuizPlan(StationQuizMode.FindLetterGrid, questionCount = 3, initialGroupIndex = 0)
            3 -> StationQuizPlan(StationQuizMode.FindLetterGrid, questionCount = 5, initialGroupIndex = 0)
            5 -> StationQuizPlan(StationQuizMode.PopBalloons, questionCount = 6, initialGroupIndex = 1)
            else -> null
        }
}
