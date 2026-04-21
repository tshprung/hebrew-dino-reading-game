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

    /** Chapters 2–4 use the same six-station structure as chapter 1; only letters and end-of-road art differ. */
    fun chapter2(stationId: Int): StationQuizPlan = Chapter1StationOrder.quizPlan(stationId)

    fun chapter3(stationId: Int): StationQuizPlan = Chapter1StationOrder.quizPlan(stationId)

    fun chapter4(stationId: Int): StationQuizPlan = Chapter1StationOrder.quizPlan(stationId)
}
