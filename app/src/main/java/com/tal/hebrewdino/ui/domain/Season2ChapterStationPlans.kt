package com.tal.hebrewdino.ui.domain

/**
 * Six-station arc per Season 2 chapter (3–6): warm-up familiar stations + advanced focus + finale.
 * Chapters 1–2 keep [Season2Chapter1StationOrder].
 */
object Season2ChapterStationPlans {
    const val STATION_COUNT: Int = 6

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
        /** Future: drag word cards onto matching pictures. */
        DragWordToPicture,
        /** Future: drag missing letter into partial word. */
        DragMissingLetter,
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
            7 ->
                ChapterStationContext(
                    chapterIndex = 7,
                    wordCatalogIds = Season2ChapterContent.ch7Words,
                    letters = Season2ChapterContent.ch7Letters,
                    memoryMatchLetters = Season2ChapterContent.ch7Letters.take(6),
                    theme = Season2StationTheme.FlyingSky,
                )
            else -> null
        }

    fun stationKind(chapterIndex: Int, stationId: Int): StationKind {
        require(chapterIndex in 3..7) { "stationKind only for chapters 3–7" }
        return when (chapterIndex) {
            3 ->
                when (stationId) {
                    1 -> StationKind.PickLetter
                    2 -> StationKind.PictureStartsWith
                    3 -> StationKind.DragMissingLetter
                    4 -> StationKind.DragWordToPicture
                    5 -> StationKind.WordParts
                    6 -> StationKind.WordParts
                    else -> error("stationId=$stationId")
                }
            4 ->
                when (stationId) {
                    1 -> StationKind.PopBalloons
                    2 -> StationKind.PickLetter
                    3 -> StationKind.DragWordToPicture
                    4 -> StationKind.DragMissingLetter
                    5 -> StationKind.PictureToWord
                    6 -> StationKind.MatchLetterToWord
                    else -> error("stationId=$stationId")
                }
            5 ->
                when (stationId) {
                    1 -> StationKind.PickLetter
                    2 -> StationKind.DragWordToPicture
                    3 -> StationKind.DragMissingLetter
                    4 -> StationKind.WhichWordStartsWith
                    5 -> StationKind.Rhyming
                    6 -> StationKind.WordParts
                    else -> error("stationId=$stationId")
                }
            6 ->
                when (stationId) {
                    1 -> StationKind.PickLetter
                    2 -> StationKind.DragWordToPicture
                    3 -> StationKind.DragMissingLetter
                    4 -> StationKind.Rhyming
                    5 -> StationKind.PictureToWord
                    6 -> StationKind.MatchLetterToWord
                    else -> error("stationId=$stationId")
                }
            7 ->
                when (stationId) {
                    1 -> StationKind.DragWordToPicture
                    2 -> StationKind.DragMissingLetter
                    3 -> StationKind.MemoryMatch
                    4 -> StationKind.Rhyming
                    5 -> StationKind.WordParts
                    6 -> StationKind.MatchLetterToWord
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
        val kind = stationKind(chapterIndex, sid)
        return when (kind) {
            StationKind.PopBalloons ->
                StationQuizPlan(
                    mode = StationQuizMode.PopBalloons,
                    questionCount = Season2WarmupStationQaPolicy.PopBalloonsQuestionCount,
                    initialGroupIndex = 0,
                    optionCount = 10,
                )
            StationKind.PickLetter ->
                StationQuizPlan(
                    mode = StationQuizMode.PickLetter,
                    questionCount = Season2WarmupStationQaPolicy.PickLetterQuestionCount,
                    initialGroupIndex = 0,
                    optionCount = 5,
                    sortOptionLetters = true,
                )
            StationKind.PictureStartsWith ->
                StationQuizPlan(
                    mode = StationQuizMode.PictureStartsWith,
                    questionCount = Season2WarmupStationQaPolicy.PictureStartsWithQuestionCount,
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
            StationKind.DragWordToPicture ->
                Season2DragStationQuizPlans.dragWordToPicture(
                    wordCatalogIds = wordCatalogIds,
                    theme = theme,
                    pairCount = dragWordPairCount(chapterIndex, wordCatalogIds),
                    questionCount = 4,
                )
            StationKind.DragMissingLetter ->
                Season2DragStationQuizPlans.dragMissingLetter(
                    wordCatalogIds = Season2DragMissingLetterWordPools.wordCatalogIds(chapterIndex),
                    letters = letters,
                    theme = theme,
                    missingIndex = dragMissingLetterIndex(),
                    questionCount = 4,
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
                        5 ->
                            when (sid) {
                                6 -> Season2WordPartsPresentationMode.GuidedWordParts
                                else -> null
                            }
                        7 ->
                            when (sid) {
                                5 -> Season2WordPartsPresentationMode.HiddenWordPartsChallenge
                                else -> null
                            }
                        else -> null
                    }
                val wordPartsCatalogIds =
                    when {
                        chapterIndex == 3 && sid in listOf(5, 6) ->
                            Season2WordPartsCatalog.wordCatalogIdsForChapter3WordParts(wordCatalogIds)
                        chapterIndex == 5 && sid == 6 ->
                            Season2WordPartsCatalog.wordCatalogIdsForChapter5WordParts(wordCatalogIds)
                        else -> wordCatalogIds
                    }
                advancedPlan(
                    mode = Season2AdvancedStationMode.WordParts,
                    wordCatalogIds = wordPartsCatalogIds,
                    theme = theme,
                    questionCount =
                        wordPartsMode?.let { mode ->
                            Season2WordPartsCatalog
                                .maxUniqueRounds(
                                    wordPartsCatalogIds,
                                    mode,
                                    stationChapterIndex = chapterIndex,
                                    stationId = sid,
                                ).coerceAtMost(6)
                        } ?: 4,
                    wordPartsPresentationMode = wordPartsMode,
                    wordPartsStationChapterIndex = chapterIndex,
                    wordPartsStationId = sid,
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
            StationKind.Rhyming -> {
                val rhymeCatalogIds =
                    Season2RhymePairCatalog.wordCatalogIdsForRhymingStation(chapterIndex, wordCatalogIds)
                val pairs =
                    Season2RhymePairCatalog.pairsForStation(chapterIndex, sid)
                        ?: error("no rhyme pairs for chapter $chapterIndex station $sid")
                advancedPlan(
                    mode = Season2AdvancedStationMode.Rhyming,
                    wordCatalogIds = rhymeCatalogIds,
                    theme = theme,
                    questionCount = pairs.size,
                    rhymeStationChapterIndex = chapterIndex,
                    rhymeStationId = sid,
                )
            }
            StationKind.MemoryMatch ->
                error("Memory match has no quiz plan")
        }.let { base ->
            if (!Season2SourceStation.shouldAlignQuizPlan(kind)) {
                base
            } else {
                Season2SourceStation.alignQuizPlanToSource(
                    Season2ChapterIds.chapterGameplayId(chapterIndex),
                    sid,
                    base,
                )
            }
        }
    }

    private fun dragWordPairCount(chapterIndex: Int, wordCatalogIds: List<String>): Int =
        when (chapterIndex) {
            3, 7 -> if (wordCatalogIds.size >= 3) 3 else 2
            else -> 2
        }

    private fun dragMissingLetterIndex(): Int = 0

    private fun advancedPlan(
        mode: Season2AdvancedStationMode,
        wordCatalogIds: List<String>,
        theme: Season2StationTheme,
        distractorLetters: List<String> = emptyList(),
        questionCount: Int = 4,
        wordPartsPresentationMode: Season2WordPartsPresentationMode? = null,
        wordPartsStationChapterIndex: Int? = null,
        wordPartsStationId: Int? = null,
        rhymeStationChapterIndex: Int? = null,
        rhymeStationId: Int? = null,
    ): StationQuizPlan {
        val advanced =
            Season2AdvancedStationPlan(
                mode = mode,
                wordCatalogIds = wordCatalogIds,
                questionCount = questionCount,
                distractorLetters = distractorLetters,
                theme = theme,
                wordPartsPresentationMode = wordPartsPresentationMode,
                wordPartsStationChapterIndex = wordPartsStationChapterIndex,
                wordPartsStationId = wordPartsStationId,
                rhymeStationChapterIndex = rhymeStationChapterIndex,
                rhymeStationId = rhymeStationId,
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
                            wordPartsPresentationMode = plan.season2WordPartsPresentationMode,
                            wordPartsStationChapterIndex = plan.season2WordPartsStationChapterIndex,
                            wordPartsStationId = plan.season2WordPartsStationId,
                            rhymeStationChapterIndex = plan.season2RhymeStationChapterIndex,
                            rhymeStationId = plan.season2RhymeStationId,
                        )
                    issues.addAll(Season2StationContentValidator.validateAdvancedPlan(advanced))
                }
                issues
            },
            onFailure = { e -> listOf("plan: ${e.message}") },
        )
    }
}
