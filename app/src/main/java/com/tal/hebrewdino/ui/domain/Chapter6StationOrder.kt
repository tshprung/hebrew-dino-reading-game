package com.tal.hebrewdino.ui.domain

/**
 * Chapter 6: review/consolidation. Reuses existing templates; no new mechanics.
 *
 * Station mapping is declared in StationBehaviorRegistry; this file only defines quiz plan metadata.
 */
object Chapter6StationOrder {
    fun quizPlan(stationId: Int): StationQuizPlan {
        return when (stationId.coerceIn(1, Chapter6Config.STATION_COUNT)) {
            // Station 1 — listen-first pick letter (review; uses Chapter 6 config letters).
            1 ->
                StationQuizPlan(
                    mode = StationQuizMode.PickLetter,
                    questionCount = 10,
                    initialGroupIndex = 0,
                    listenOnlyTargetPrompt = true,
                    optionCount = 6,
                )
            // Station 2 — find highlighted letter in word (review; uses spell data from episode content).
            2 ->
                StationQuizPlan(
                    mode = StationQuizMode.PickLetter,
                    questionCount = 8,
                    initialGroupIndex = 0,
                    highlightedLetterInWordPickLetter = true,
                    optionCount = 6,
                )
            // Station 3 — any-letter-in-word balloons (review; uses balloon word data).
            3 ->
                StationQuizPlan(
                    mode = StationQuizMode.PopBalloons,
                    questionCount = 8,
                    initialGroupIndex = 0,
                    popAllLettersInWord = true,
                    optionCount = 10,
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

