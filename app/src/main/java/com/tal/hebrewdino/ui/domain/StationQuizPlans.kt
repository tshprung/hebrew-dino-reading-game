package com.tal.hebrewdino.ui.domain

data class StationQuizPlan(
    val mode: StationQuizMode,
    val questionCount: Int,
    val initialGroupIndex: Int,
)

object StationQuizPlans {
    fun chapter1(stationId: Int): StationQuizPlan =
        when (stationId.coerceIn(1, Chapter1Config.STATION_COUNT)) {
            1 -> StationQuizPlan(StationQuizMode.TapChoice, questionCount = 3, initialGroupIndex = 0)
            2 -> StationQuizPlan(StationQuizMode.PopBalloons, questionCount = 4, initialGroupIndex = 0)
            3 -> StationQuizPlan(StationQuizMode.RevealTiles, questionCount = 5, initialGroupIndex = 1)
            4 -> StationQuizPlan(StationQuizMode.TapChoice, questionCount = 5, initialGroupIndex = 1)
            5 -> StationQuizPlan(StationQuizMode.PopBalloons, questionCount = 6, initialGroupIndex = 2)
            else -> StationQuizPlan(StationQuizMode.PictureLetterMatch, questionCount = 3, initialGroupIndex = 3)
        }

    /** Chapter 2 stations 1–3 and 5 (not picture-match or finale). */
    fun chapter2LetterOnly(stationId: Int): StationQuizPlan? =
        when (stationId) {
            1 -> StationQuizPlan(StationQuizMode.TapChoice, questionCount = 3, initialGroupIndex = 0)
            2 -> StationQuizPlan(StationQuizMode.PopBalloons, questionCount = 4, initialGroupIndex = 0)
            3 -> StationQuizPlan(StationQuizMode.RevealTiles, questionCount = 5, initialGroupIndex = 0)
            5 -> StationQuizPlan(StationQuizMode.TapChoice, questionCount = 5, initialGroupIndex = 1)
            else -> null
        }

    /** Chapter 3 path stations replaced by letter quizzes: 1, 3, 5. */
    fun chapter3LetterOnly(stationId: Int): StationQuizPlan? =
        when (stationId) {
            1 -> StationQuizPlan(StationQuizMode.TapChoice, questionCount = 3, initialGroupIndex = 0)
            3 -> StationQuizPlan(StationQuizMode.RevealTiles, questionCount = 5, initialGroupIndex = 0)
            5 -> StationQuizPlan(StationQuizMode.PopBalloons, questionCount = 6, initialGroupIndex = 1)
            else -> null
        }
}
