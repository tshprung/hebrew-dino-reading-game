package com.tal.hebrewdino.ui.domain

/** Shared quiz-plan builders for drag stations (avoids order/plan circular imports). */
object Season2DragStationQuizPlans {
    fun dragWordToPicture(
        wordCatalogIds: List<String>,
        theme: Season2StationTheme,
        pairCount: Int = 2,
        questionCount: Int = 4,
    ): StationQuizPlan =
        StationQuizPlan(
            mode = StationQuizMode.DragWordToPicture,
            questionCount = questionCount,
            initialGroupIndex = 0,
            season2WordCatalogIds = wordCatalogIds,
            season2StationTheme = theme,
            dragWordToPicturePairCount = pairCount.coerceIn(2, 3),
        )

    fun dragMissingLetter(
        wordCatalogIds: List<String>,
        letters: List<String>,
        theme: Season2StationTheme,
        missingIndex: Int = 0,
        questionCount: Int = 4,
        optionCount: Int = 4,
    ): StationQuizPlan =
        StationQuizPlan(
            mode = StationQuizMode.DragMissingLetter,
            questionCount = questionCount,
            initialGroupIndex = 0,
            season2WordCatalogIds = wordCatalogIds,
            season2StationTheme = theme,
            season2AdvancedDistractorLetters = letters,
            dragMissingLetterIndex = missingIndex,
            optionCount = optionCount,
        )
}
