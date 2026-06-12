package com.tal.hebrewdino.ui.domain

/**
 * Season 2 — Chapters 1–2 station order (shared UX numbering).
 *
 * Product order:
 * 1. Pop Balloons
 * 2. Pick Letter
 * 3. Picture Starts With
 * 4. Memory Match (custom screen)
 * 5. Which Word Starts With Letter (ImageMatch)
 * 6. Chapter finale ramp:
 *    - Ch1: Picture → written word ([Season2AdvancedStationMode.PictureToWord])
 *    - Ch2: Visible word parts tutorial ([Season2AdvancedStationMode.WordParts] + [Season2WordPartsPresentationMode.VisibleWordParts])
 */
object Season2Chapter1StationOrder {
    const val POP_BALLOONS: Int = 1
    const val PICK_LETTER: Int = 2
    const val PICTURE_STARTS_WITH: Int = 3
    const val MEMORY_MATCH: Int = 4
    const val WHICH_WORD_STARTS_WITH: Int = 5
    /** UX station 6 — finale; kind varies by chapter (see [quizPlan]). */
    const val FINALE_STATION: Int = 6
    /** @deprecated Use [FINALE_STATION]; kept for call-site compatibility. */
    const val MATCH_LETTER_TO_WORD: Int = FINALE_STATION

    const val STATION_COUNT: Int = 6

    fun quizPlan(chapterIndex: Int, stationId: Int): StationQuizPlan {
        val sid = stationId.coerceIn(1, STATION_COUNT)
        if (sid == FINALE_STATION) {
            return finaleQuizPlan(chapterIndex)
        }
        return sharedWarmupQuizPlan(sid)
    }

    private fun finaleQuizPlan(chapterIndex: Int): StationQuizPlan {
        val chapterDef =
            Season2ChapterRegistry.chapter(chapterIndex)
                ?: error("Season2 chapter $chapterIndex not in registry")
        return when (chapterIndex) {
            1 ->
                Season2AdvancedStationPlans.toStationQuizPlan(
                    Season2AdvancedStationPlan(
                        mode = Season2AdvancedStationMode.PictureToWord,
                        wordCatalogIds = chapterDef.wordCatalogIds,
                        questionCount = 6,
                        theme = chapterDef.stationTheme,
                    ),
                )
            2 ->
                Season2AdvancedStationPlans.toStationQuizPlan(
                    Season2AdvancedStationPlan(
                        mode = Season2AdvancedStationMode.WordParts,
                        wordCatalogIds = chapterDef.wordCatalogIds,
                        questionCount =
                            Season2WordPartsCatalog
                                .maxUniqueRounds(
                                    chapterDef.wordCatalogIds,
                                    Season2WordPartsPresentationMode.VisibleWordParts,
                                ).coerceAtMost(6),
                        theme = chapterDef.stationTheme,
                        wordPartsPresentationMode = Season2WordPartsPresentationMode.VisibleWordParts,
                    ),
                )
            else -> error("Finale plan only for Season2 chapters 1–2")
        }
    }

    private fun sharedWarmupQuizPlan(stationId: Int): StationQuizPlan =
        when (stationId) {
            POP_BALLOONS ->
                StationQuizPlan(
                    mode = StationQuizMode.PopBalloons,
                    questionCount = 3,
                    initialGroupIndex = 0,
                    optionCount = 10,
                )
            PICK_LETTER ->
                StationQuizPlan(
                    mode = StationQuizMode.PickLetter,
                    questionCount = 4,
                    initialGroupIndex = 0,
                    optionCount = 5,
                    sortOptionLetters = true,
                )
            PICTURE_STARTS_WITH ->
                StationQuizPlan(
                    mode = StationQuizMode.PictureStartsWith,
                    questionCount = 4,
                    initialGroupIndex = 0,
                    optionCount = 4,
                    sortOptionLetters = true,
                )
            MEMORY_MATCH ->
                StationQuizPlan(
                    mode = StationQuizMode.PickLetter,
                    questionCount = 1,
                    initialGroupIndex = 0,
                )
            WHICH_WORD_STARTS_WITH ->
                StationQuizPlan(
                    mode = StationQuizMode.ImageMatch,
                    questionCount = 4,
                    initialGroupIndex = 0,
                    imageMatchAlwaysThreeChoices = true,
                    imageMatchCaptionSizeMultiplier = 1.5f,
                    imageMatchPictureSizeMultiplier = 1f,
                    imageMatchChoiceCount = 3,
                )
            else -> error("Unexpected Season2 stationId=$stationId")
        }
}
