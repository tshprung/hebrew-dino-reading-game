package com.tal.hebrewdino.ui.domain

/**
 * Chapter 6: review/consolidation.
 *
 * Active Chapter 6 is being aligned structurally with the standard six-station arc.
 * Prior Chapter 6-specific variants are preserved below as training candidates (not used by active Chapter 6).
 *
 * Station mapping is declared in StationBehaviorRegistry; this file only defines quiz plan metadata.
 */
object Chapter6StationOrder {
    fun quizPlan(stationId: Int): StationQuizPlan {
        return when (stationId.coerceIn(1, Chapter6Config.STATION_COUNT)) {
            1 ->
                StationQuizPlan(
                    mode = StationQuizMode.PictureStartsWith,
                    questionCount = 8,
                    initialGroupIndex = 1,
                    imageMatchCaptionSizeMultiplier = 1.5f,
                    imageMatchPictureSizeMultiplier = 1f,
                    optionCount = 5,
                    sortOptionLetters = true,
                )
            2 ->
                StationQuizPlan(
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
                    questionCount = 5,
                    initialGroupIndex = 0,
                    popAllLettersInWord = true,
                    optionCount = 10,
                )
            4 ->
                StationQuizPlan(
                    mode = StationQuizMode.PickLetter,
                    questionCount = Chapter3EpisodeContent.STATION_4_ROUND_COUNT,
                    initialGroupIndex = 0,
                    highlightedLetterInWordPickLetter = true,
                    optionCount = 6,
                )
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
                error("Chapter 6 station out of range 1..${Chapter6Config.STATION_COUNT} after coerce (raw=$stationId)")
        }
    }

    fun trainingCandidateQuizPlan(stationId: Int): StationQuizPlan {
        return when (stationId.coerceIn(1, Chapter6Config.STATION_COUNT)) {
            1 ->
                StationQuizPlan(
                    mode = StationQuizMode.PickLetter,
                    questionCount = 10,
                    initialGroupIndex = 0,
                    listenOnlyTargetPrompt = true,
                    optionCount = 6,
                )
            2 ->
                StationQuizPlan(
                    mode = StationQuizMode.PickLetter,
                    questionCount = 8,
                    initialGroupIndex = 0,
                    highlightedLetterInWordPickLetter = true,
                    optionCount = 6,
                )
            3 ->
                StationQuizPlan(
                    mode = StationQuizMode.PopBalloons,
                    questionCount = 8,
                    initialGroupIndex = 0,
                    popAllLettersInWord = true,
                    optionCount = 10,
                )
            4 ->
                StationQuizPlan(
                    mode = StationQuizMode.PictureStartsWith,
                    questionCount = 8,
                    initialGroupIndex = 1,
                    listenOnlyTargetPrompt = false,
                    imageMatchCaptionSizeMultiplier = 1.5f,
                    imageMatchPictureSizeMultiplier = 1f,
                )
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

