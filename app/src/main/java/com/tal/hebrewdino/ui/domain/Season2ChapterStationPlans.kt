package com.tal.hebrewdino.ui.domain

/**
 * Six-station arc per Season 2 chapter (3–6): warm-up familiar stations + advanced focus + finale.
 * Chapters 1–2 keep [Season2Chapter1StationOrder].
 */
object Season2ChapterStationPlans {
    const val STATION_COUNT: Int = Season2Chapter1StationOrder.STATION_COUNT

    data class ChapterStationContext(
        val chapterIndex: Int,
        val wordCatalogIds: List<String>,
        val letters: List<String>,
        val memoryMatchLetters: List<String>,
        val theme: Season2StationTheme,
    )

    enum class StationKind {
        PopBalloons,
        PickLetter,
        PictureStartsWith,
        MemoryMatch,
        WordParts,
        PictureToWord,
        MissingFirstLetter,
        Rhyming,
        WhichWordStartsWith,
        MatchLetterToWord,
    }

    fun contextFor(chapterIndex: Int): ChapterStationContext? =
        when (chapterIndex) {
            3 ->
                ChapterStationContext(
                    chapterIndex = 3,
                    wordCatalogIds = Season2ChapterContent.ch3Words,
                    letters = Season2ChapterContent.ch3Letters,
                    memoryMatchLetters = Season2ChapterContent.ch3Letters,
                    theme = Season2StationTheme.StegosaurusPlates,
                )
            4 ->
                ChapterStationContext(
                    chapterIndex = 4,
                    wordCatalogIds = Season2ChapterContent.ch4Words,
                    letters = Season2ChapterContent.ch4Letters,
                    memoryMatchLetters = Season2ChapterContent.ch4Letters.take(6),
                    theme = Season2StationTheme.HighLeaves,
                )
            5 ->
                ChapterStationContext(
                    chapterIndex = 5,
                    wordCatalogIds = Season2ChapterContent.ch5Words,
                    letters = Season2ChapterContent.ch5Letters,
                    memoryMatchLetters = Season2ChapterContent.ch5Letters.take(6),
                    theme = Season2StationTheme.LetterArmor,
                )
            6 ->
                ChapterStationContext(
                    chapterIndex = 6,
                    wordCatalogIds = Season2ChapterContent.ch6Words,
                    letters = Season2ChapterContent.ch6Letters,
                    memoryMatchLetters = Season2ChapterContent.ch6Letters.take(6),
                    theme = Season2StationTheme.UnderwaterBubbles,
                )
            else -> null
        }

    fun stationKind(chapterIndex: Int, stationId: Int): StationKind {
        require(chapterIndex in 3..6) { "stationKind only for chapters 3–6" }
        return when (chapterIndex) {
            3 ->
                when (stationId) {
                    1 -> StationKind.PopBalloons
                    2 -> StationKind.PickLetter
                    3 -> StationKind.PictureStartsWith
                    4 -> StationKind.MemoryMatch
                    5 -> StationKind.WordParts
                    6 -> StationKind.WordParts
                    else -> error("stationId=$stationId")
                }
            4 ->
                when (stationId) {
                    1 -> StationKind.PopBalloons
                    2 -> StationKind.PickLetter
                    3 -> StationKind.PictureStartsWith
                    4 -> StationKind.MemoryMatch
                    5 -> StationKind.PictureToWord
                    6 -> StationKind.MatchLetterToWord
                    else -> error("stationId=$stationId")
                }
            5 ->
                when (stationId) {
                    1 -> StationKind.PopBalloons
                    2 -> StationKind.PickLetter
                    3 -> StationKind.PictureStartsWith
                    4 -> StationKind.MemoryMatch
                    5 -> StationKind.MissingFirstLetter
                    6 -> StationKind.WhichWordStartsWith
                    else -> error("stationId=$stationId")
                }
            6 ->
                when (stationId) {
                    1 -> StationKind.PopBalloons
                    2 -> StationKind.PickLetter
                    3 -> StationKind.PictureStartsWith
                    4 -> StationKind.MemoryMatch
                    5 -> StationKind.Rhyming
                    6 -> StationKind.PictureToWord
                    else -> error("stationId=$stationId")
                }
            else -> error("chapterIndex=$chapterIndex")
        }
    }

    fun quizPlan(ctx: ChapterStationContext, stationId: Int): StationQuizPlan =
        quizPlan(
            chapterIndex = ctx.chapterIndex,
            wordCatalogIds = ctx.wordCatalogIds,
            letters = ctx.letters,
            theme = ctx.theme,
            stationId = stationId,
        )

    fun quizPlan(
        chapterIndex: Int,
        wordCatalogIds: List<String>,
        letters: List<String>,
        theme: Season2StationTheme,
        stationId: Int,
    ): StationQuizPlan {
        val sid = stationId.coerceIn(1, STATION_COUNT)
        if (sid == Season2Chapter1StationOrder.MEMORY_MATCH) {
            return Season2Chapter1StationOrder.quizPlan(chapterIndex = 1, stationId = sid)
        }
        return when (stationKind(chapterIndex, sid)) {
            StationKind.PopBalloons ->
                StationQuizPlan(
                    mode = StationQuizMode.PopBalloons,
                    questionCount = 3,
                    initialGroupIndex = 0,
                    optionCount = 10,
                )
            StationKind.PickLetter ->
                StationQuizPlan(
                    mode = StationQuizMode.PickLetter,
                    questionCount = 4,
                    initialGroupIndex = 0,
                    optionCount = 5,
                    sortOptionLetters = true,
                )
            StationKind.PictureStartsWith ->
                StationQuizPlan(
                    mode = StationQuizMode.PictureStartsWith,
                    questionCount = 4,
                    initialGroupIndex = 0,
                    optionCount = if (chapterIndex == 3) 5 else 4,
                    sortOptionLetters = true,
                )
            StationKind.WhichWordStartsWith ->
                StationQuizPlan(
                    mode = StationQuizMode.ImageMatch,
                    questionCount = 4,
                    initialGroupIndex = 0,
                    imageMatchAlwaysThreeChoices = true,
                    imageMatchCaptionSizeMultiplier = 1.5f,
                    imageMatchPictureSizeMultiplier = 1f,
                    imageMatchChoiceCount = 3,
                )
            StationKind.MatchLetterToWord ->
                StationQuizPlan(
                    mode = StationQuizMode.ImageMatch,
                    questionCount = 4,
                    initialGroupIndex = 0,
                    imageMatchAlwaysThreeChoices = true,
                    imageMatchCaptionSizeMultiplier = 1.5f,
                    imageMatchPictureSizeMultiplier = 1f,
                    imageMatchChoiceCount = 3,
                )
            StationKind.WordParts -> {
                val wordPartsMode =
                    when (chapterIndex) {
                        3 ->
                            when (sid) {
                                5 -> Season2WordPartsPresentationMode.GuidedWordParts
                                6 -> Season2WordPartsPresentationMode.HiddenWordPartsChallenge
                                else -> null
                            }
                        else -> null
                    }
                val wordPartsCatalogIds =
                    if (chapterIndex == 3 && sid in listOf(5, 6)) {
                        Season2WordPartsCatalog.wordCatalogIdsForChapter3WordParts(wordCatalogIds)
                    } else {
                        wordCatalogIds
                    }
                advancedPlan(
                    mode = Season2AdvancedStationMode.WordParts,
                    wordCatalogIds = wordPartsCatalogIds,
                    theme = theme,
                    questionCount =
                        wordPartsMode?.let { mode ->
                            Season2WordPartsCatalog.maxUniqueRounds(wordPartsCatalogIds, mode).coerceAtMost(6)
                        } ?: 4,
                    wordPartsPresentationMode = wordPartsMode,
                )
            }
            StationKind.PictureToWord ->
                advancedPlan(
                    mode = Season2AdvancedStationMode.PictureToWord,
                    wordCatalogIds = wordCatalogIds,
                    theme = theme,
                )
            StationKind.MissingFirstLetter ->
                advancedPlan(
                    mode = Season2AdvancedStationMode.MissingFirstLetter,
                    wordCatalogIds = wordCatalogIds,
                    theme = theme,
                    distractorLetters = letters,
                )
            StationKind.Rhyming ->
                advancedPlan(
                    mode = Season2AdvancedStationMode.Rhyming,
                    wordCatalogIds = wordCatalogIds,
                    theme = theme,
                    questionCount =
                        Season2RhymePairCatalog.pairsForWordIds(wordCatalogIds).size.coerceAtMost(6),
                )
            StationKind.MemoryMatch ->
                error("Memory match has no quiz plan")
        }
    }

    private fun advancedPlan(
        mode: Season2AdvancedStationMode,
        wordCatalogIds: List<String>,
        theme: Season2StationTheme,
        distractorLetters: List<String> = emptyList(),
        questionCount: Int = 4,
        wordPartsPresentationMode: Season2WordPartsPresentationMode? = null,
    ): StationQuizPlan {
        val advanced =
            Season2AdvancedStationPlan(
                mode = mode,
                wordCatalogIds = wordCatalogIds,
                questionCount = questionCount,
                distractorLetters = distractorLetters,
                theme = theme,
                wordPartsPresentationMode = wordPartsPresentationMode,
            )
        return Season2AdvancedStationPlans.toStationQuizPlan(advanced)
    }

    fun validateAllStations(ctx: ChapterStationContext): List<String> =
        (1..STATION_COUNT).flatMap { sid ->
            validateStation(ctx, sid).map { issue -> "st$sid: $issue" }
        }

    fun validateStation(ctx: ChapterStationContext, stationId: Int): List<String> {
        val issues = mutableListOf<String>()
        val kind =
            runCatching { stationKind(ctx.chapterIndex, stationId) }.fold(
                onSuccess = { it },
                onFailure = { return listOf(it.message ?: "invalid station") },
            )
        if (kind == StationKind.MemoryMatch) {
            if (ctx.memoryMatchLetters.size < 4) {
                issues.add("memory match needs at least 4 letters")
            }
            issues.addAll(Season2StationContentValidator.validateLetters(ctx.memoryMatchLetters))
            return issues
        }
        return runCatching {
            quizPlan(
                chapterIndex = ctx.chapterIndex,
                wordCatalogIds = ctx.wordCatalogIds,
                letters = ctx.letters,
                theme = ctx.theme,
                stationId = stationId,
            )
        }.fold(
            onSuccess = { plan ->
                if (plan.season2AdvancedMode != null) {
                    val advanced =
                        Season2AdvancedStationPlan(
                            mode = plan.season2AdvancedMode,
                            wordCatalogIds = plan.season2WordCatalogIds ?: ctx.wordCatalogIds,
                            questionCount = plan.questionCount,
                            distractorLetters = plan.season2AdvancedDistractorLetters,
                            theme = plan.season2StationTheme,
                        )
                    issues.addAll(Season2StationContentValidator.validateAdvancedPlan(advanced))
                }
                issues
            },
            onFailure = { e -> listOf("plan: ${e.message}") },
        )
    }
}
