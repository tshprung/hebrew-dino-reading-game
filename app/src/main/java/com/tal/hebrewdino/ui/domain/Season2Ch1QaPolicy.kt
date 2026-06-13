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

    fun shouldKeepPopBalloonsInputUnlockedDuringFeedback(season2UxStationId: Int?): Boolean =
        Season2StationQaPolicy.shouldKeepPopBalloonsInputUnlockedDuringFeedback(season2UxStationId)

    fun shouldCancelPreviousFeedbackOnPopBalloonsTap(): Boolean = false

    fun shouldAllowTapDuringPopBalloonsWrongRecover(season2UxStationId: Int?): Boolean =
        Season2StationQaPolicy.shouldAllowTapDuringPopBalloonsWrongRecover(season2UxStationId)

    /** WhichWordStartsWith compact layout — S1 PICTURE_PICK_ALL, S2 gameplay, training. */
    fun isWhichWordStartsWithLayoutStation(chapterId: Int?, stationId: Int?): Boolean {
        if (chapterId == null || stationId == null) return false
        val (resolvedChapterId, resolvedStationId) = Season2SourceStation.resolveForBehavior(chapterId, stationId)
        if ((resolvedChapterId == 1 || resolvedChapterId == 2 || resolvedChapterId == 4 || resolvedChapterId == 5) &&
            SixStationArcQaPolicy.isSagaWhichWordStartsWithStation(resolvedChapterId, resolvedStationId)
        ) {
            return true
        }
        if (Season2StationUx.isWhichWordStartsWithStation(chapterId, stationId)) return true
        return false
    }

    fun shouldHideFinaleHintButton(
        gameplayChapterId: Int,
        season2UxStationId: Int?,
    ): Boolean = isCh1FinalePictureToWord(gameplayChapterId, season2UxStationId)

    /** Disabled — S2 PictureStartsWith uses Ch1 st4 parity (readable panel + address-aware intro). */
    fun isPictureStartsWithLayoutPilot(
    ): Boolean = false

    /** Every wrong balloon tap plays address-aware try-again after the popped letter. */
    fun shouldPlayTryAgainInPopBalloonsSfx(): Boolean = true

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

    /** All chapters: narrator map-entry clip only (Ch1 model — no puzzle-explain before entry). */
    fun shouldPlayPuzzleExplainBeforeMapEntry(): Boolean = false

    fun isCh1FinalePictureToWord(
        gameplayChapterId: Int,
        season2UxStationId: Int?,
    ): Boolean =
        season2UxStationId == Season2Chapter1StationOrder.FINALE_STATION &&
            gameplayChapterId == Season2ChapterIds.Chapter1Tyrannosaurus
}
