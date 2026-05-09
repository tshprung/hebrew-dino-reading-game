package com.tal.hebrewdino.ui.domain

/**
 * Episode 3 reuses the same six interaction *types* as the saga arc, but reorders stations 1–3
 * for a slightly more advanced flow (grid → balloons → spell-by-letter), while keeping stations 4–6
 * aligned with [Chapter1StationOrder] picture / image / finale tuning.
 */
object Chapter3StationOrder {
    fun quizPlan(stationId: Int): StationQuizPlan {
        return when (stationId.coerceIn(1, Chapter3Config.STATION_COUNT)) {
            1 ->
                StationQuizPlan(
                    mode = StationQuizMode.PictureStartsWith,
                    questionCount = 8,
                    initialGroupIndex = 1,
                    imageMatchCaptionSizeMultiplier = 1.5f,
                    imageMatchPictureSizeMultiplier = 1f,
                    sortOptionLetters = true,
                )
            2 ->
                StationQuizPlan(
                    // Reuse ImageMatch question shape but render as MatchLetterToWord UI.
                    mode = StationQuizMode.ImageMatch,
                    questionCount = 6,
                    initialGroupIndex = 2,
                    imageMatchAlwaysThreeChoices = false,
                    imageMatchChoiceCount = 3,
                    imageMatchCaptionSizeMultiplier = 1f,
                    imageMatchPictureSizeMultiplier = 1f,
                )
            // Station 3 — any-letter-in-word balloons (PopBalloons generator uses word content).
            3 ->
                StationQuizPlan(
                    mode = StationQuizMode.PopBalloons,
                    questionCount = 5,
                    initialGroupIndex = 0,
                    popAllLettersInWord = true,
                    optionCount = 10,
                )
            // Station 4 — find highlighted letter in word.
            4 ->
                StationQuizPlan(
                    mode = StationQuizMode.PickLetter,
                    questionCount = Chapter3EpisodeContent.STATION_4_ROUND_COUNT,
                    initialGroupIndex = 0,
                    highlightedLetterInWordPickLetter = true,
                    optionCount = 6,
                )
            // Station 5 — audio recognition pick-letter.
            5 ->
                StationQuizPlan(
                    mode = StationQuizMode.PickLetter,
                    questionCount = 10,
                    initialGroupIndex = 0,
                    chapter3AudioLetterRecognition = true,
                    optionCount = 6,
                )
            6 ->
                StationQuizPlan(
                    mode = StationQuizMode.ImageMatch,
                    questionCount = 8,
                    initialGroupIndex = 3,
                    imageMatchAlwaysThreeChoices = false,
                    imageMatchChoiceCount = 4,
                    imageMatchCaptionSizeMultiplier = 1.5f,
                    imageMatchPictureSizeMultiplier = 1f,
                )
            else ->
                error(
                    "Chapter 3 station out of range 1..${Chapter3Config.STATION_COUNT} after coerce (raw=$stationId)",
                )
        }
    }
}
