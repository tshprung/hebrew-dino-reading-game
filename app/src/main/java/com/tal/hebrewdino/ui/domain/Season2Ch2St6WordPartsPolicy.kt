package com.tal.hebrewdino.ui.domain

import androidx.compose.ui.unit.dp

/** Ch2 UX station 6 — visible word-parts tutorial (unique [VisibleWordParts] presentation). */
object Season2Ch2St6WordPartsPolicy {
    val TargetWordDownDp = 38.dp
    /** Physical left — away from hero image (≈2.5cm). */
    val HintPhysicalLeftDp = 95.dp
    val InstructionPhysicalLeftDp = 19.dp
    val ImagePhysicalLeftDp = 38.dp
    val ImageDownDp = 19.dp
    val OptionsPhysicalLeftDp = 19.dp
    val OptionsDownDp = 38.dp

    /** Post-praise hold before round advance (default WordParts uses 1_400ms). */
    const val CorrectPostPraiseHoldMs: Long = 700L

    fun isCh2St6WordParts(presentationMode: Season2WordPartsPresentationMode): Boolean =
        presentationMode == Season2WordPartsPresentationMode.VisibleWordParts

    fun filterCorrectSplitImmediatelyBeforeAudio(presentationMode: Season2WordPartsPresentationMode): Boolean =
        isCh2St6WordParts(presentationMode)

    /** Chapter reward overlay only on first-time St6 completion (no replay reward). */
    fun shouldRequestFirstTimeChapterReward(
        stationId: Int,
        wasStationAlreadyDone: Boolean,
        chapterWasCompleteBefore: Boolean,
    ): Boolean =
        stationId == Season2Chapter1StationOrder.FINALE_STATION &&
            !wasStationAlreadyDone &&
            !chapterWasCompleteBefore
}
