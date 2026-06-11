package com.tal.hebrewdino.ui.domain

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Season 2 Chapter 1 only — QA polish rules (unit-testable). */
object Season2Ch1QaPolicy {
    const val CoachInstructionToWordGapMs: Long = 60L

    val FinaleExtraDownDp: Dp = 19.dp

    /** S2 Ch1 PictureStartsWith layout pilot — shift cards/options below instruction. */
    val PictureStartsWithLayoutPilotCardsDownDp: Dp = 38.dp

    /** S2 Ch1 WhichWordStartsWith — instruction nudged up ~3mm from prior position. */
    val WhichWordStartsWithInstructionUpDp: Dp = 11.dp

    /** S2 Ch1 WhichWordStartsWith — cards below instruction (23dp prior minus 19dp up). */
    val WhichWordStartsWithLayoutPilotCardsDownDp: Dp = 4.dp

    val WhichWordStartsWithInstructionBgAlpha: Float = 0.72f

    /** S2 Ch1 WhichWordStartsWith — instruction font 30% smaller than 28/32sp base. */
    const val WhichWordStartsWithInstructionFontScale: Float = 0.7f

    val WhichWordStartsWithInstructionBgHorizontalPaddingDp: Dp = 7.dp

    val WhichWordStartsWithInstructionBgVerticalPaddingDp: Dp = 3.dp

    val WhichWordStartsWithInstructionBgCornerRadiusDp: Dp = 12.dp

    /** S2 Ch1 WhichWordStartsWith — target letter chip nudged up ~2mm (instruction/cards unchanged). */
    val WhichWordStartsWithLetterUpDp: Dp = 8.dp

    /**
     * S2 Ch1 finale — physical width reserved for companion at screen end (Dino BottomEnd).
     * Content column lives in the remaining area; no start/end padding nudges on learning UI.
     */
    val FinaleDinoReservedWidthDp: Dp = 120.dp

    fun isCh1PopBalloonsStation(season2UxStationId: Int?): Boolean =
        season2UxStationId == Season2Chapter1StationOrder.POP_BALLOONS

    fun shouldKeepPopBalloonsInputUnlockedDuringFeedback(season2UxStationId: Int?): Boolean =
        isCh1PopBalloonsStation(season2UxStationId)

    fun shouldCancelPreviousFeedbackOnPopBalloonsTap(season2QuizBalloons: Boolean): Boolean =
        season2QuizBalloons

    fun shouldAllowTapDuringPopBalloonsWrongRecover(season2UxStationId: Int?): Boolean =
        isCh1PopBalloonsStation(season2UxStationId)

    /** WhichWordStartsWith compact layout — S1 PICTURE_PICK_ALL, S2 gameplay, training. */
    fun isWhichWordStartsWithLayoutStation(chapterId: Int?, stationId: Int?): Boolean {
        if (chapterId == null || stationId == null) return false
        if ((chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5) &&
            stationId == Chapter1StationOrder.PICTURE_PICK_ALL
        ) {
            return true
        }
        if (Season2StationUx.isWhichWordStartsWithStation(chapterId, stationId)) return true
        if (chapterId == TrainingV1Config.CHAPTER_ID &&
            stationId == TrainingV1Config.STATION_WHICH_WORD_STARTS_WITH_LETTER
        ) {
            return true
        }
        return false
    }

    fun isWhichWordStartsWithLayoutPilot(season2UxStationId: Int?): Boolean =
        season2UxStationId == Season2Chapter1StationOrder.WHICH_WORD_STARTS_WITH

    fun shouldOrchestrateWhichWordCorrectPraiseInStation(season2UxStationId: Int?): Boolean =
        isWhichWordStartsWithUx(season2UxStationId)

    fun shouldHideFinaleHintButton(
        gameplayChapterId: Int,
        season2UxStationId: Int?,
    ): Boolean = isCh1FinalePictureToWord(gameplayChapterId, season2UxStationId)

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
        season2UxStationId in
            setOf(
                Season2Chapter1StationOrder.PICTURE_STARTS_WITH,
                Season2Chapter1StationOrder.WHICH_WORD_STARTS_WITH,
            )

    /** Chapter reward overlay only on first-time St6 completion (no replay reward). */
    fun shouldRequestFirstTimeChapterReward(
        registryChapterId: Int,
        stationId: Int,
        wasStationAlreadyDone: Boolean,
        chapterWasCompleteBefore: Boolean,
    ): Boolean =
        registryChapterId == 1 &&
            stationId == Season2Chapter1StationOrder.FINALE_STATION &&
            !wasStationAlreadyDone &&
            !chapterWasCompleteBefore

    /** Ch1 map entry: map-entry clip only — skip puzzle-explain duplicate. */
    fun shouldPlayPuzzleExplainBeforeMapEntry(registryChapterId: Int): Boolean = registryChapterId != 1

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
