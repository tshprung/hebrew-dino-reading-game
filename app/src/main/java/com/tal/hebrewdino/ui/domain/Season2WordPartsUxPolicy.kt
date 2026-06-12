package com.tal.hebrewdino.ui.domain

import androidx.compose.ui.unit.dp

/** Approved S2 WordParts layout and round-timing (Ch2-St6 model → all WordParts stations). */
object Season2WordPartsUxPolicy {
    val TargetWordDownDp = 38.dp
    /** Physical left — away from hero image (≈3cm; +0.5cm from prior Ch2-St6 QA). */
    val HintPhysicalLeftDp = 114.dp
    val InstructionPhysicalLeftDp = 19.dp
    val ImagePhysicalLeftDp = 38.dp
    val ImageDownDp = 19.dp
    val OptionsPhysicalLeftDp = 19.dp
    val OptionsDownDp = 38.dp

    /** Post-praise hold before round advance (default WordParts used 1_400ms). */
    const val CorrectPostPraiseHoldMs: Long = 700L

    fun usesApprovedLayout(presentationMode: Season2WordPartsPresentationMode): Boolean = true

    fun filterCorrectSplitImmediatelyBeforeAudio(presentationMode: Season2WordPartsPresentationMode): Boolean =
        usesApprovedLayout(presentationMode)
}
