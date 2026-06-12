package com.tal.hebrewdino.ui.domain

/**
 * Season 2 — Chapters 1–2 station order (shared UX numbering).
 *
 * Chapter 1: Pop → Pick → Picture → DragWord → WhichWord → DragMissing
 * Chapter 2: Pick → DragWord → Picture → Memory → DragMissing → WordParts
 */
object Season2Chapter1StationOrder {
    const val POP_BALLOONS: Int = 1
    const val PICK_LETTER: Int = 2
    const val PICTURE_STARTS_WITH: Int = 3
    /** Ch2 only — custom memory-match screen at UX station 4. */
    const val MEMORY_MATCH: Int = 4
    const val WHICH_WORD_STARTS_WITH: Int = 5
    /** UX station 6 — finale; kind varies by chapter (see [quizPlan]). */
    const val FINALE_STATION: Int = 6
    /** @deprecated Use [FINALE_STATION]; kept for call-site compatibility. */
    const val MATCH_LETTER_TO_WORD: Int = FINALE_STATION

    const val STATION_COUNT: Int = 6

    fun isMemoryMatchStation(chapterIndex: Int, stationId: Int): Boolean =
        when (chapterIndex) {
            2 -> stationId == MEMORY_MATCH
            7 -> stationId == 3
            else -> false
        }

    fun quizPlan(chapterIndex: Int, stationId: Int): StationQuizPlan {
        val chapterDef =
            Season2ChapterRegistry.chapter(chapterIndex)
                ?: error("Season2 chapter $chapterIndex not in registry")
        val sid = stationId.coerceIn(1, STATION_COUNT)
        return when (chapterIndex) {
            1 -> chapter1QuizPlan(sid, chapterDef)
            2 -> chapter2QuizPlan(sid, chapterDef)
            else -> error("Season2Chapter1StationOrder only covers chapters 1–2")
        }
    }

    private fun chapter1QuizPlan(
        stationId: Int,
        chapterDef: Season2ChapterDefinition,
    ): StationQuizPlan =
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
                Season2DragStationQuizPlans.dragWordToPicture(
                    wordCatalogIds = chapterDef.wordCatalogIds,
                    theme = chapterDef.stationTheme,
                    pairCount = 2,
                    questionCount = 4,
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
            FINALE_STATION ->
                Season2DragStationQuizPlans.dragMissingLetter(
                    wordCatalogIds = chapterDef.wordCatalogIds,
                    letters = chapterDef.letters,
                    theme = chapterDef.stationTheme,
                    missingIndex = 0,
                    questionCount = 4,
                    optionCount = 3,
                )
            else -> error("Unexpected Season2 ch1 stationId=$stationId")
        }

    private fun chapter2QuizPlan(
        stationId: Int,
        chapterDef: Season2ChapterDefinition,
    ): StationQuizPlan =
        when (stationId) {
            // UX station 1 — PickLetter (not [POP_BALLOONS], which shares index 1).
            1 ->
                StationQuizPlan(
                    mode = StationQuizMode.PickLetter,
                    questionCount = 4,
                    initialGroupIndex = 0,
                    optionCount = 5,
                    sortOptionLetters = true,
                )
            2 ->
                Season2DragStationQuizPlans.dragWordToPicture(
                    wordCatalogIds = chapterDef.wordCatalogIds,
                    theme = chapterDef.stationTheme,
                    pairCount = 2,
                    questionCount = 4,
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
            5 ->
                Season2DragStationQuizPlans.dragMissingLetter(
                    wordCatalogIds = chapterDef.wordCatalogIds,
                    letters = chapterDef.letters,
                    theme = chapterDef.stationTheme,
                    missingIndex = 0,
                    questionCount = 4,
                )
            FINALE_STATION ->
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
            else -> error("Unexpected Season2 ch2 stationId=$stationId")
        }
}
