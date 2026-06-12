package com.tal.hebrewdino.ui.domain

/** Conservative drag-station quiz plans for Season 1 (later chapters only). */
object Season1DragStationQuizPlans {
    fun dragWordToPicture(
        questionCount: Int = 4,
        pairCount: Int = 2,
        initialGroupIndex: Int = 0,
    ): StationQuizPlan =
        StationQuizPlan(
            mode = StationQuizMode.DragWordToPicture,
            questionCount = questionCount,
            initialGroupIndex = initialGroupIndex,
            dragWordToPicturePairCount = pairCount.coerceIn(2, 3),
        )

    fun dragMissingLetter(
        questionCount: Int = 4,
        optionCount: Int = 4,
        initialGroupIndex: Int = 0,
    ): StationQuizPlan =
        StationQuizPlan(
            mode = StationQuizMode.DragMissingLetter,
            questionCount = questionCount,
            initialGroupIndex = initialGroupIndex,
            dragMissingLetterIndex = 0,
            optionCount = optionCount,
        )
}
