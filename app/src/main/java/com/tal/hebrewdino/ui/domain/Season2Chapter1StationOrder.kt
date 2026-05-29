package com.tal.hebrewdino.ui.domain

/**
 * Season 2 — Chapter 1 (Tyrannosaurus) station order.
 *
 * Product order:
 * 1. Pop Balloons
 * 2. Pick Letter
 * 3. Picture Starts With
 * 4. Memory Match (custom screen; not a [StationQuizPlan])
 * 5. Which Word Starts With Letter (ImageMatch)
 * 6. Match Letter To Word (MatchLetterToWord template backed by ImageMatch questions)
 */
object Season2Chapter1StationOrder {
    const val POP_BALLOONS: Int = 1
    const val PICK_LETTER: Int = 2
    const val PICTURE_STARTS_WITH: Int = 3
    const val MEMORY_MATCH: Int = 4
    const val WHICH_WORD_STARTS_WITH: Int = 5
    const val MATCH_LETTER_TO_WORD: Int = 6

    const val STATION_COUNT: Int = 6

    fun quizPlan(stationId: Int): StationQuizPlan {
        return when (stationId.coerceIn(1, STATION_COUNT)) {
            POP_BALLOONS ->
                StationQuizPlan(
                    mode = StationQuizMode.PopBalloons,
                    questionCount = 3,
                    initialGroupIndex = 0,
                    optionCount = 10,
                )
            PICK_LETTER ->
                StationQuizPlan(
                    mode = StationQuizMode.PickLetter,
                    questionCount = 4,
                    initialGroupIndex = 0,
                    optionCount = 5,
                    sortOptionLetters = true,
                )
            PICTURE_STARTS_WITH ->
                StationQuizPlan(
                    mode = StationQuizMode.PictureStartsWith,
                    questionCount = 4,
                    initialGroupIndex = 0,
                    optionCount = 4,
                    sortOptionLetters = true,
                )
            MEMORY_MATCH ->
                // Custom memory game screen; the plan isn't used but we keep a safe default.
                StationQuizPlan(
                    mode = StationQuizMode.PickLetter,
                    questionCount = 1,
                    initialGroupIndex = 0,
                )
            WHICH_WORD_STARTS_WITH ->
                StationQuizPlan(
                    mode = StationQuizMode.ImageMatch,
                    questionCount = 4,
                    initialGroupIndex = 0,
                    imageMatchAlwaysThreeChoices = true,
                    imageMatchCaptionSizeMultiplier = 1.5f,
                    imageMatchPictureSizeMultiplier = 1f,
                    imageMatchChoiceCount = 3,
                )
            MATCH_LETTER_TO_WORD ->
                StationQuizPlan(
                    mode = StationQuizMode.ImageMatch,
                    questionCount = 4,
                    initialGroupIndex = 0,
                    imageMatchAlwaysThreeChoices = true,
                    imageMatchCaptionSizeMultiplier = 1.5f,
                    imageMatchPictureSizeMultiplier = 1f,
                    imageMatchChoiceCount = 3,
                )
            else -> error("Unexpected Season2 stationId=$stationId")
        }
    }
}

