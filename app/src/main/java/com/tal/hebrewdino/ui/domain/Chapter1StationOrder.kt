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
 * 6. Picture–letter match summary — custom UI (same three cards as each [Question.ImageMatchQuestion] round);
 * session still uses [StationQuizMode.ImageMatch] for progression, with the same caption/picture plan flags as station 5.
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
            // Station 1: give more rounds to practice.
            TAP_LETTER -> StationQuizPlan(StationQuizMode.PickLetter, questionCount = 6, initialGroupIndex = 0)
            // Station 2: a bit longer.
            BALLOON_POP -> StationQuizPlan(StationQuizMode.PopBalloons, questionCount = 6, initialGroupIndex = 0)
            // Station 3: a bit longer.
            REVEAL_THEN_CHOOSE -> StationQuizPlan(StationQuizMode.FindLetterGrid, questionCount = 7, initialGroupIndex = 0)
            PICTURE_PICK_ONE ->
                StationQuizPlan(
                    mode = StationQuizMode.PictureStartsWith,
                    questionCount = 8,
                    initialGroupIndex = 1,
                    // Same caption/picture tuning as stations 5–6 so the single picture card matches ImageMatch.
                    imageMatchCaptionSizeMultiplier = 1.5f,
                    imageMatchPictureSizeMultiplier = 1f,
                )
            PICTURE_PICK_ALL ->
                StationQuizPlan(
                    mode = StationQuizMode.ImageMatch,
                    // Station 5: add 2 more tries (again).
                    questionCount = 8,
                    initialGroupIndex = 2,
                    imageMatchAlwaysThreeChoices = true,
                    // Station 5: keep the card (box) size stable; scale the picture *inside* the card in UI.
                    imageMatchCaptionSizeMultiplier = 1.5f,
                    imageMatchPictureSizeMultiplier = 1f,
                )
            FINALE_PICTURE_LETTER_MATCH ->
                StationQuizPlan(
                    mode = StationQuizMode.ImageMatch,
                    questionCount = 6,
                    initialGroupIndex = 3,
                    // Same card/caption tuning as station 5; station 6 UI reuses those choices from each question.
                    imageMatchAlwaysThreeChoices = true,
                    imageMatchCaptionSizeMultiplier = 1.5f,
                    imageMatchPictureSizeMultiplier = 1f,
                    chapter1Station6ForbidAutoAndCarTogether = true,
                )
            else ->
                error(
                    "Chapter 1 station out of range 1..${Chapter1Config.STATION_COUNT} after coerce (raw=$stationId)",
                )
        }
    }
}
