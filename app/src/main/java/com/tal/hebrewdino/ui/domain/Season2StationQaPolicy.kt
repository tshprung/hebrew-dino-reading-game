package com.tal.hebrewdino.ui.domain

/** Station-type QA policies propagated from approved S2 Ch1 behavior to Ch2–6 (and Ch1 via kind resolution). */
object Season2StationQaPolicy {
    fun stationKind(
        gameplayChapterId: Int,
        season2UxStationId: Int?,
    ): Season2ChapterStationPlans.StationKind? {
        if (season2UxStationId == null) return null
        if (Season2StationAudio.isSeason2GameplayChapter(gameplayChapterId)) {
            return Season2StationUx.stationKindForGameplayChapter(gameplayChapterId, season2UxStationId)
        }
        if (gameplayChapterId in earlyArcGameplayChapterIds) {
            return earlyArcStationKind(season2UxStationId)
        }
        return null
    }

    /** S1 six-station arc + training resolve QA policies by UX station index, not physical slot. */
    private val earlyArcGameplayChapterIds: Set<Int> =
        setOf(1, 2, 3, 4, 5, TrainingV1Config.CHAPTER_ID)

    /** Ch2 st1–5 routes through S1 gameplay chapter ids — resolve by UX station index. */
    private fun earlyArcStationKind(season2UxStationId: Int): Season2ChapterStationPlans.StationKind? =
        when (season2UxStationId) {
            Season2Chapter1StationOrder.POP_BALLOONS ->
                Season2ChapterStationPlans.StationKind.PopBalloons
            Season2Chapter1StationOrder.PICK_LETTER ->
                Season2ChapterStationPlans.StationKind.PickLetter
            Season2Chapter1StationOrder.PICTURE_STARTS_WITH ->
                Season2ChapterStationPlans.StationKind.PictureStartsWith
            Season2Chapter1StationOrder.WHICH_WORD_STARTS_WITH ->
                Season2ChapterStationPlans.StationKind.WhichWordStartsWith
            else -> null
        }

    fun isPopBalloonsStation(season2UxStationId: Int?): Boolean =
        season2UxStationId == Season2Chapter1StationOrder.POP_BALLOONS

    fun shouldKeepPopBalloonsInputUnlockedDuringFeedback(season2UxStationId: Int?): Boolean =
        isPopBalloonsStation(season2UxStationId)

    fun shouldAllowTapDuringPopBalloonsWrongRecover(season2UxStationId: Int?): Boolean =
        isPopBalloonsStation(season2UxStationId)

    fun isWhichWordStartsWithStation(
        gameplayChapterId: Int,
        season2UxStationId: Int?,
    ): Boolean =
        stationKind(gameplayChapterId, season2UxStationId) ==
            Season2ChapterStationPlans.StationKind.WhichWordStartsWith

    fun isPictureToWordStation(
        gameplayChapterId: Int,
        season2UxStationId: Int?,
    ): Boolean =
        stationKind(gameplayChapterId, season2UxStationId) ==
            Season2ChapterStationPlans.StationKind.PictureToWord

    fun shouldOrchestrateWhichWordCorrectPraiseInStation(
        gameplayChapterId: Int,
        season2UxStationId: Int?,
    ): Boolean = isWhichWordStartsWithStation(gameplayChapterId, season2UxStationId)

    fun shouldSkipAdvanceRoundPraiseBecausePlayedInStation(
        gameplayChapterId: Int,
        season2UxStationId: Int?,
    ): Boolean {
        val kind = stationKind(gameplayChapterId, season2UxStationId) ?: return false
        return kind in inStationOrchestratedAudioKinds
    }

    /**
     * Ch2–6 early arc routes st1–5 through S1 gameplay chapter ids (1/3). Suppress S1 saga
     * episode1/narrator praise so in-station and post-focus praise are not clipped or doubled.
     */
    fun shouldSuppressSagaEpisodeAdvancePraise(isSeason2QuizChapter: Boolean): Boolean =
        isSeason2QuizChapter

    fun shouldSkipAdvanceRoundInterRoundFeedback(
        gameplayChapterId: Int,
        season2UxStationId: Int?,
        isLast: Boolean,
    ): Boolean {
        if (season2UxStationId == null) return false
        val kind = stationKind(gameplayChapterId, season2UxStationId) ?: return false
        if (kind in inStationOrchestratedAudioKinds) return true
        if (isLast) return false
        return kind in
            setOf(
                Season2ChapterStationPlans.StationKind.WhichWordStartsWith,
                Season2ChapterStationPlans.StationKind.PictureToWord,
                Season2ChapterStationPlans.StationKind.WordParts,
                Season2ChapterStationPlans.StationKind.MatchLetterToWord,
                Season2ChapterStationPlans.StationKind.MissingFirstLetter,
                Season2ChapterStationPlans.StationKind.Rhyming,
            )
    }

    private val inStationOrchestratedAudioKinds: Set<Season2ChapterStationPlans.StationKind> =
        setOf(
            Season2ChapterStationPlans.StationKind.PopBalloons,
            Season2ChapterStationPlans.StationKind.PickLetter,
            Season2ChapterStationPlans.StationKind.PictureStartsWith,
            Season2ChapterStationPlans.StationKind.WhichWordStartsWith,
        )

    fun useTightBetweenRoundTiming(
        gameplayChapterId: Int,
        season2UxStationId: Int?,
    ): Boolean {
        if (season2UxStationId == null) return false
        val kind = stationKind(gameplayChapterId, season2UxStationId) ?: return false
        return kind in
            setOf(
                Season2ChapterStationPlans.StationKind.WhichWordStartsWith,
                Season2ChapterStationPlans.StationKind.PictureToWord,
                Season2ChapterStationPlans.StationKind.WordParts,
                Season2ChapterStationPlans.StationKind.MatchLetterToWord,
                Season2ChapterStationPlans.StationKind.MissingFirstLetter,
                Season2ChapterStationPlans.StationKind.Rhyming,
            )
    }

    fun shouldSkipPictureToWordAssetPraiseOnLastRound(
        gameplayChapterId: Int,
        season2UxStationId: Int?,
        isLast: Boolean,
    ): Boolean = isLast && isPictureToWordStation(gameplayChapterId, season2UxStationId)

    fun shouldReplayPictureToWordCoachWithInstruction(
        gameplayChapterId: Int,
        season2UxStationId: Int?,
    ): Boolean = isPictureToWordStation(gameplayChapterId, season2UxStationId)

    /** Ch2–6 station-type audit rows for unit tests. */
    fun expectedStationKind(
        registryChapterIndex: Int,
        uxStationId: Int,
    ): Season2ChapterStationPlans.StationKind? =
        when (registryChapterIndex) {
            1 ->
                when (uxStationId) {
                    1 -> Season2ChapterStationPlans.StationKind.PopBalloons
                    2 -> Season2ChapterStationPlans.StationKind.PickLetter
                    3 -> Season2ChapterStationPlans.StationKind.PictureStartsWith
                    4 -> Season2ChapterStationPlans.StationKind.DragWordToPicture
                    5 -> Season2ChapterStationPlans.StationKind.WhichWordStartsWith
                    6 -> Season2ChapterStationPlans.StationKind.DragMissingLetter
                    else -> null
                }
            2 ->
                when (uxStationId) {
                    1 -> Season2ChapterStationPlans.StationKind.PickLetter
                    2 -> Season2ChapterStationPlans.StationKind.DragWordToPicture
                    3 -> Season2ChapterStationPlans.StationKind.PictureStartsWith
                    4 -> Season2ChapterStationPlans.StationKind.MemoryMatch
                    5 -> Season2ChapterStationPlans.StationKind.DragMissingLetter
                    6 -> Season2ChapterStationPlans.StationKind.WordParts
                    else -> null
                }
            in 3..7 -> Season2ChapterStationPlans.stationKind(registryChapterIndex, uxStationId)
            else -> null
        }
}
