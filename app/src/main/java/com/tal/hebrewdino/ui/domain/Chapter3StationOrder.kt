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
                    imageMatchCaptionSizeMultiplier = 1f,
                    imageMatchPictureSizeMultiplier = 1f,
                    sortPictureStartsWithOptionLetters = true,
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
            3 ->
                StationQuizPlan(
                    mode = StationQuizMode.PopBalloons,
                    // Station 3 balloons: one round per word.
                    // Product request: 5 unique words per run; each word appears once.
                    questionCount = 5,
                    initialGroupIndex = 0,
                    popAllLettersInWord = true,
                )
            4 ->
                StationQuizPlan(
                    mode = StationQuizMode.PickLetter,
                    questionCount = 9,
                    initialGroupIndex = 0,
                    pickLetterOptionCount = 6,
                    highlightedLetterInWordPickLetter = true,
                    imageMatchCaptionSizeMultiplier = 1.5f,
                    imageMatchPictureSizeMultiplier = 1f,
                )
            5 ->
                StationQuizPlan(
                    mode = StationQuizMode.PickLetter,
                    questionCount = 12,
                    initialGroupIndex = 0,
                    pickLetterOptionCount = 6,
                    chapter3AudioLetterRecognition = true,
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
