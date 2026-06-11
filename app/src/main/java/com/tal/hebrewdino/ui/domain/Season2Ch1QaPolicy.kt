package com.tal.hebrewdino.ui.domain

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Season 2 Chapter 1 only — QA polish rules (unit-testable). */
object Season2Ch1QaPolicy {
    const val CoachInstructionToWordGapMs: Long = 60L

    val FinaleExtraDownDp: Dp = 19.dp

    val FinaleContentEndPaddingDp: Dp = 152.dp

    /** S2 Ch1 PictureStartsWith layout pilot — shift cards/options below instruction. */
    val PictureStartsWithLayoutPilotCardsDownDp: Dp = 38.dp

    fun isPictureStartsWithLayoutPilot(
        gameplayChapterId: Int,
        stationId: Int,
    ): Boolean =
        gameplayChapterId == Season2ChapterIds.Chapter1Tyrannosaurus &&
            Season2StationUx.stationKindForGameplayChapter(gameplayChapterId, stationId) ==
                Season2ChapterStationPlans.StationKind.PictureStartsWith

    fun shouldPlayTryAgainInPopBalloonsSfx(season2QuizBalloons: Boolean): Boolean = !season2QuizBalloons

    fun shouldOrchestrateMapEntryFromChapterList(
        progressHydrated: Boolean,
        showChapterIntroOverlay: Boolean,
        entryFromChapterSelect: Boolean,
        mapReturnCaptionEvent: Long,
        mapEntryInstructionSpoken: Boolean,
        suppressBecauseStationReturn: Boolean,
    ): Boolean =
        progressHydrated &&
            !showChapterIntroOverlay &&
            entryFromChapterSelect &&
            mapReturnCaptionEvent == 0L &&
            !mapEntryInstructionSpoken &&
            !suppressBecauseStationReturn

    fun shouldUseCompletedReplayTilesEntryVoice(
        chapterFullyRevealed: Boolean,
        entryFromChapterSelect: Boolean,
    ): Boolean = chapterFullyRevealed && entryFromChapterSelect

    fun shouldSkipAdvanceRoundInterRoundFeedback(
        season2UxStationId: Int?,
        isLast: Boolean,
    ): Boolean =
        season2UxStationId in
            setOf(
                Season2Chapter1StationOrder.WHICH_WORD_STARTS_WITH,
                Season2Chapter1StationOrder.FINALE_STATION,
            ) &&
            !isLast

    fun shouldSkipAdvanceRoundPraiseBecausePlayedInStation(season2UxStationId: Int?): Boolean =
        season2UxStationId == Season2Chapter1StationOrder.PICTURE_STARTS_WITH

    fun shouldSkipPictureToWordAssetPraiseOnLastRound(
        season2UxStationId: Int?,
        isLast: Boolean,
    ): Boolean = season2UxStationId == Season2Chapter1StationOrder.FINALE_STATION && isLast

    fun useTightBetweenRoundTiming(season2UxStationId: Int?): Boolean =
        season2UxStationId in
            setOf(
                Season2Chapter1StationOrder.WHICH_WORD_STARTS_WITH,
                Season2Chapter1StationOrder.FINALE_STATION,
            )

    fun isWhichWordStartsWithUx(season2UxStationId: Int?): Boolean =
        season2UxStationId == Season2Chapter1StationOrder.WHICH_WORD_STARTS_WITH

    fun shouldReplayPictureToWordCoachWithInstruction(
        season2UxStationId: Int?,
        gameplayChapterId: Int,
    ): Boolean =
        season2UxStationId == Season2Chapter1StationOrder.FINALE_STATION &&
            gameplayChapterId == Season2ChapterIds.Chapter1Tyrannosaurus

    fun isCh1FinalePictureToWord(
        gameplayChapterId: Int,
        season2UxStationId: Int?,
    ): Boolean =
        season2UxStationId == Season2Chapter1StationOrder.FINALE_STATION &&
            gameplayChapterId == Season2ChapterIds.Chapter1Tyrannosaurus
}
