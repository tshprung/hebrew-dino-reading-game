package com.tal.hebrewdino.ui.domain

/**
 * **Single source of truth** for Chapter 1 journey station → quiz shape.
 *
 * Order (do not reorder without product sign-off):
 * 1. Find all target letters — [StationQuizMode.FindLetterGrid] → [Question.FindLetterGridQuestion]
 * 2. Balloon pop — [StationQuizMode.PopBalloons] → [Question.PopBalloonsQuestion]
 * 3. Find letters (larger grid) — [StationQuizMode.FindLetterGrid]
 * 4. Image match — [StationQuizMode.ImageMatch] → [Question.ImageMatchQuestion]
 * 5. Image match — [StationQuizMode.ImageMatch]
 * 6. Image match (same as 4–5; finale word slots reserved for later chapters).
 *
 * Routing for level UI: [StationQuizPlans.chapter1] delegates here.
 */
object Chapter1StationOrder {
    const val TAP_LETTER: Int = 1
    const val BALLOON_POP: Int = 2
    const val REVEAL_THEN_CHOOSE: Int = 3
    const val PICTURE_PICK_ONE: Int = 4
    const val PICTURE_PICK_ALL: Int = 5
    const val FINALE_PICTURE_LETTER_MATCH: Int = 6

    fun quizPlan(stationId: Int): StationQuizPlan {
        return when (stationId.coerceIn(1, Chapter1Config.STATION_COUNT)) {
            TAP_LETTER -> StationQuizPlan(StationQuizMode.FindLetterGrid, questionCount = 3, initialGroupIndex = 0)
            BALLOON_POP -> StationQuizPlan(StationQuizMode.PopBalloons, questionCount = 4, initialGroupIndex = 0)
            REVEAL_THEN_CHOOSE -> StationQuizPlan(StationQuizMode.FindLetterGrid, questionCount = 5, initialGroupIndex = 1)
            PICTURE_PICK_ONE -> StationQuizPlan(StationQuizMode.ImageMatch, questionCount = 4, initialGroupIndex = 1)
            PICTURE_PICK_ALL -> StationQuizPlan(StationQuizMode.ImageMatch, questionCount = 4, initialGroupIndex = 2)
            FINALE_PICTURE_LETTER_MATCH ->
                StationQuizPlan(StationQuizMode.ImageMatch, questionCount = 4, initialGroupIndex = 3)
            else ->
                error(
                    "Chapter 1 station out of range 1..${Chapter1Config.STATION_COUNT} after coerce (raw=$stationId)",
                )
        }
    }
}
