package com.tal.hebrewdino.ui.domain

import androidx.compose.ui.unit.dp

/** Approved S2 WordParts layout and round-timing (Ch2-St6 model → all WordParts stations). */
object Season2WordPartsUxPolicy {
    val TargetWordDownDp = 38.dp
    /** Physical left — away from hero image (≈2.5cm). */
    val HintPhysicalLeftDp = 95.dp
    val InstructionPhysicalLeftDp = 19.dp
    val ImagePhysicalLeftDp = 38.dp
    val ImageDownDp = 19.dp
    val OptionsPhysicalLeftDp = 19.dp
    val OptionsDownDp = 38.dp
    /** Extra physical-left nudge for split options when hint equation is visible. */
    val HintOptionsExtraPhysicalLeftDp = Season2WarmupStationQaPolicy.HalfCmPhysicalLeftDp

    /** Post-praise hold before round advance (default WordParts used 1_400ms). */
    const val CorrectPostPraiseHoldMs: Long = 700L

    fun usesApprovedLayout(presentationMode: Season2WordPartsPresentationMode): Boolean = true

    fun filterCorrectSplitImmediatelyBeforeAudio(presentationMode: Season2WordPartsPresentationMode): Boolean =
        usesApprovedLayout(presentationMode)
}
