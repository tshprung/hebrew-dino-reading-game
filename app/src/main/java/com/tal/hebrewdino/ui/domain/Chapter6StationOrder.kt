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
                    imageMatchPictureSizeMultiplier = 0.85f,
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
                Season1DragStationQuizPlans.dragWordToPicture(
                    questionCount = 5,
                    pairCount = 3,
                    initialGroupIndex = 0,
                )
            4 ->
                Season1DragStationQuizPlans.dragMissingLetter(questionCount = 6).copy(
                    season2WordCatalogIds = Chapter6Config.dragMissingLetterWordCatalogIds(),
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

}

