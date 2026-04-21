package com.tal.hebrewdino.ui.domain

/**
 * **Single source of truth** for Chapter 1 journey station → quiz shape.
 *
 * Order (do not reorder without product sign-off):
 * 1. Find all target letters — [StationQuizMode.FindLetterGrid] → [Question.FindLetterGridQuestion]
 * 2. Balloon pop — [StationQuizMode.PopBalloons] → [Question.PopBalloonsQuestion]
 * 3. Find letters (larger grid) — [StationQuizMode.FindLetterGrid]
 * 4. Picture → first letter — [StationQuizMode.PictureStartsWith] → [Question.PictureStartsWithQuestion]
 * 5. Image match (three cards, larger captions/pictures) — [StationQuizMode.ImageMatch]
 * 6. Picture–letter match summary — custom UI; session still uses [StationQuizMode.ImageMatch] for progression.
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
            TAP_LETTER -> StationQuizPlan(StationQuizMode.PickLetter, questionCount = 3, initialGroupIndex = 0)
            BALLOON_POP -> StationQuizPlan(StationQuizMode.PopBalloons, questionCount = 4, initialGroupIndex = 0)
            REVEAL_THEN_CHOOSE -> StationQuizPlan(StationQuizMode.FindLetterGrid, questionCount = 5, initialGroupIndex = 0)
            PICTURE_PICK_ONE ->
                StationQuizPlan(StationQuizMode.PictureStartsWith, questionCount = 4, initialGroupIndex = 1)
            PICTURE_PICK_ALL ->
                StationQuizPlan(
                    mode = StationQuizMode.ImageMatch,
                    questionCount = 4,
                    initialGroupIndex = 2,
                    imageMatchAlwaysThreeChoices = true,
                    // Station 5: keep the card (box) size stable; scale the picture *inside* the card in UI.
                    imageMatchCaptionSizeMultiplier = 1.5f,
                    imageMatchPictureSizeMultiplier = 1f,
                )
            FINALE_PICTURE_LETTER_MATCH ->
                StationQuizPlan(StationQuizMode.ImageMatch, questionCount = 4, initialGroupIndex = 3)
            else ->
                error(
                    "Chapter 1 station out of range 1..${Chapter1Config.STATION_COUNT} after coerce (raw=$stationId)",
                )
        }
    }
}
