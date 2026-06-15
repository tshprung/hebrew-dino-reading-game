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
    val wordPartsStationChapterIndex: Int? = null,
    val wordPartsStationId: Int? = null,
    val rhymeStationChapterIndex: Int? = null,
    val rhymeStationId: Int? = null,
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
            season2WordPartsStationChapterIndex = advanced.wordPartsStationChapterIndex,
            season2WordPartsStationId = advanced.wordPartsStationId,
            season2RhymeStationChapterIndex = advanced.rhymeStationChapterIndex,
            season2RhymeStationId = advanced.rhymeStationId,
        )
    }

}
