package com.tal.hebrewdino.ui.domain

/**
 * Factory for Season 2 advanced station plans (engines ready; chapter wiring is a later batch).
 */
data class Season2AdvancedStationPlan(
    val mode: Season2AdvancedStationMode,
    val wordCatalogIds: List<String>,
    val questionCount: Int = 5,
    val initialGroupIndex: Int = 0,
    val distractorLetters: List<String> = emptyList(),
    val theme: Season2StationTheme = Season2StationTheme.Standard,
    val wordPartsPresentationMode: Season2WordPartsPresentationMode? = null,
) {
    init {
        val missing = Season2StationContentValidator.validateAdvancedPlan(this)
        require(missing.isEmpty()) {
            "Invalid Season2AdvancedStationPlan ($mode): ${missing.joinToString()}"
        }
    }
}

object Season2AdvancedStationPlans {
    fun pictureToWordPlan(wordCatalogIds: List<String>, theme: Season2StationTheme = Season2StationTheme.HighLeaves) =
        Season2AdvancedStationPlan(
            mode = Season2AdvancedStationMode.PictureToWord,
            wordCatalogIds = wordCatalogIds,
            theme = theme,
        )

    fun missingFirstLetterPlan(
        wordCatalogIds: List<String>,
        distractorLetters: List<String>,
        theme: Season2StationTheme = Season2StationTheme.LetterArmor,
    ) = Season2AdvancedStationPlan(
        mode = Season2AdvancedStationMode.MissingFirstLetter,
        wordCatalogIds = wordCatalogIds,
        distractorLetters = distractorLetters,
        theme = theme,
    )

    fun wordPartsPlan(
        wordCatalogIds: List<String>,
        theme: Season2StationTheme = Season2StationTheme.StegosaurusPlates,
    ) = Season2AdvancedStationPlan(
        mode = Season2AdvancedStationMode.WordParts,
        wordCatalogIds = wordCatalogIds,
        theme = theme,
    )

    fun rhymingPlan(
        wordCatalogIds: List<String>,
        theme: Season2StationTheme = Season2StationTheme.UnderwaterBubbles,
    ) = Season2AdvancedStationPlan(
        mode = Season2AdvancedStationMode.Rhyming,
        wordCatalogIds = wordCatalogIds,
        theme = theme,
    )

    fun toStationQuizPlan(advanced: Season2AdvancedStationPlan): StationQuizPlan {
        val baseMode =
            when (advanced.mode) {
                Season2AdvancedStationMode.PictureToWord -> StationQuizMode.ImageMatch
                else -> StationQuizMode.PickLetter
            }
        return StationQuizPlan(
            mode = baseMode,
            questionCount = advanced.questionCount,
            initialGroupIndex = advanced.initialGroupIndex,
            imageMatchChoiceCount = if (advanced.mode == Season2AdvancedStationMode.PictureToWord) 3 else null,
            optionCount = 3,
            season2AdvancedMode = advanced.mode,
            season2WordCatalogIds = advanced.wordCatalogIds,
            season2AdvancedDistractorLetters = advanced.distractorLetters,
            season2StationTheme = advanced.theme,
            season2WordPartsPresentationMode = advanced.wordPartsPresentationMode,
        )
    }

}
