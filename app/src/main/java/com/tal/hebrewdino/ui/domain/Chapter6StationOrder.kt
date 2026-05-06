package com.tal.hebrewdino.ui.domain

/**
 * Chapter 6: review/consolidation. Reuses existing templates; no new mechanics.
 *
 * Station mapping is declared in StationBehaviorRegistry; this file only defines quiz plan metadata.
 */
object Chapter6StationOrder {
    fun quizPlan(stationId: Int): StationQuizPlan {
        return when (stationId.coerceIn(1, Chapter6Config.STATION_COUNT)) {
            // Station 1 — listen and choose the letter (same shape as Episode 4/5 station 1).
            1 ->
                StationQuizPlan(
                    mode = StationQuizMode.PickLetter,
                    questionCount = 8,
                    initialGroupIndex = 0,
                    pickLetterOptionCount = 6,
                    listenOnlyTargetPrompt = true,
                )
            // Station 2 — pick the highlighted letter in a word (reuse Ch3 st4 behavior).
            2 ->
                StationQuizPlan(
                    mode = StationQuizMode.PickLetter,
                    questionCount = 9,
                    initialGroupIndex = 0,
                    pickLetterOptionCount = 6,
                    listenOnlyTargetPrompt = false,
                    highlightedLetterInWordPickLetter = true,
                )
            // Station 3 — pop all letters appearing in the word (reuse Ch3 st3 behavior).
            3 ->
                StationQuizPlan(
                    mode = StationQuizMode.PopBalloons,
                    questionCount = 5,
                    initialGroupIndex = 0,
                    listenOnlyTargetPrompt = false,
                    popAllLettersInWord = true,
                )
            // Station 4 — picture starts with (review; keep manageable options).
            4 ->
                StationQuizPlan(
                    mode = StationQuizMode.PictureStartsWith,
                    questionCount = 8,
                    initialGroupIndex = 1,
                    listenOnlyTargetPrompt = false,
                    imageMatchCaptionSizeMultiplier = 1.5f,
                    imageMatchPictureSizeMultiplier = 1f,
                )
            // Station 5 — image match (listen-first review).
            5 ->
                StationQuizPlan(
                    mode = StationQuizMode.ImageMatch,
                    questionCount = 8,
                    initialGroupIndex = 2,
                    listenOnlyTargetPrompt = true,
                    imageMatchAlwaysThreeChoices = true,
                    imageMatchCaptionSizeMultiplier = 1.5f,
                    imageMatchPictureSizeMultiplier = 1f,
                )
            // Station 6 — finale match (same generator shape as station 5).
            6 ->
                StationQuizPlan(
                    mode = StationQuizMode.ImageMatch,
                    questionCount = 6,
                    initialGroupIndex = 3,
                    listenOnlyTargetPrompt = false,
                    imageMatchAlwaysThreeChoices = true,
                    imageMatchCaptionSizeMultiplier = 1.5f,
                    imageMatchPictureSizeMultiplier = 1f,
                )
            else ->
                error("Chapter 6 station out of range 1..${Chapter6Config.STATION_COUNT} after coerce (raw=$stationId)")
        }
    }
}

