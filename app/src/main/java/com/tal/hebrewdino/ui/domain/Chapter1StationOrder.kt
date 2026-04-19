package com.tal.hebrewdino.ui.domain

/**
 * **Single source of truth** for Chapter 1 journey station → quiz shape.
 *
 * Order (do not reorder without product sign-off):
 * 1. Tap correct letter — [StationQuizMode.TapChoice] → [Question.TapChoiceQuestion]
 * 2. Balloon pop — [StationQuizMode.PopBalloons] → [Question.PopBalloonsQuestion]
 * 3. Reveal then choose — [StationQuizMode.RevealTiles] → [Question.RevealTilesQuestion]
 * 4. Picture pick one — [StationQuizMode.PicturePickOne] → [Question.PicturePickOneQuestion]
 * 5. Picture pick all (2 of N) — [StationQuizMode.PicturePickAll] → [Question.PicturePickAllQuestion]
 * 6. Finale picture ↔ letter (2 pairs) — [StationQuizMode.PictureLetterMatch] → [Question.PictureLetterMatchQuestion]
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
            TAP_LETTER -> StationQuizPlan(StationQuizMode.TapChoice, questionCount = 3, initialGroupIndex = 0)
            BALLOON_POP -> StationQuizPlan(StationQuizMode.PopBalloons, questionCount = 4, initialGroupIndex = 0)
            REVEAL_THEN_CHOOSE -> StationQuizPlan(StationQuizMode.RevealTiles, questionCount = 5, initialGroupIndex = 1)
            PICTURE_PICK_ONE -> StationQuizPlan(StationQuizMode.PicturePickOne, questionCount = 4, initialGroupIndex = 1)
            PICTURE_PICK_ALL -> StationQuizPlan(StationQuizMode.PicturePickAll, questionCount = 4, initialGroupIndex = 2)
            FINALE_PICTURE_LETTER_MATCH ->
                StationQuizPlan(StationQuizMode.PictureLetterMatch, questionCount = 3, initialGroupIndex = 3)
            else ->
                error(
                    "Chapter 1 station out of range 1..${Chapter1Config.STATION_COUNT} after coerce (raw=$stationId)",
                )
        }
    }
}
