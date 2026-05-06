package com.tal.hebrewdino.ui.components.station

import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.IntOffset
import com.tal.hebrewdino.ui.domain.InstructionPanelStyle
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.StationInstructionCopy
import com.tal.hebrewdino.ui.domain.StationUiSpec
import com.tal.hebrewdino.ui.game.FindLetterGridGame

/**
 * Saga find-letter grid station UI for [GameScreen]: wires [FindLetterGridGame] from session-derived
 * flags and [StationUiSpec]. Audio, session advancement, and feedback jobs remain in the caller.
 */
@Composable
fun FindLetterGridStationContent(
    question: Question.FindLetterGridQuestion,
    modifier: Modifier = Modifier,
    listenOnly: Boolean,
    /** Precomputed: saga arc station 3 (find grid / reveal-then-choose). */
    isSagaRevealStation: Boolean,
    sagaUsesFindGridAudioStaging: Boolean,
    stationUiSpec: StationUiSpec,
    chapter3ContextWordHint: String?,
    floatingTargetLetterHint: String?,
    episode4TargetCellsHintEpoch: Int,
    hintPulseEpoch: Int,
    enabled: Boolean,
    contentKey: Int,
    entryPulseScale: Float,
    optionsShakePx: Float,
    onSagaGridLetterTapped: ((String) -> Unit)?,
    onCellTapped: (Int) -> Unit,
    onCompleted: () -> Unit,
) {
    val suppressHeaderTargetLetter =
        sagaUsesFindGridAudioStaging && stationUiSpec.findGridSuppressHeaderTargetLetter
    val inlineInstructionText =
        if (isSagaRevealStation) {
            stationUiSpec.findGridInlineInstructionOverride
                ?: if (listenOnly) {
                    StationInstructionCopy.FindGridListenFirst
                } else {
                    StationInstructionCopy.FindGridVisibleTarget
                }
        } else {
            null
        }

    FindLetterGridGame(
        question = question,
        hideListenOnlyHeaderTargetLetter = stationUiSpec.findGridHideListenOnlyHeaderTargetLetter,
        floatingTargetLetterHint = floatingTargetLetterHint,
        episode4TargetCellsHintEpoch = episode4TargetCellsHintEpoch,
        contextWordHint = chapter3ContextWordHint,
        suppressHeaderTargetLetter = suppressHeaderTargetLetter,
        inlineInstructionText = inlineInstructionText,
        inlineInstructionReadablePanel =
            stationUiSpec.findGridInlineInstructionPanelStyle == InstructionPanelStyle.WhiteRounded,
        cellSideScale =
            if (isSagaRevealStation) {
                0.9f
            } else {
                1f
            },
        contentNudgeDownFraction =
            if (isSagaRevealStation) {
                0.05f
            } else {
                0f
            },
        onLetterTapped = onSagaGridLetterTapped,
        hintPulseEpoch = hintPulseEpoch,
        hintHeaderPeakScale = if (sagaUsesFindGridAudioStaging) 1.30f else 1.12f,
        gridLetterSizeMultiplier = if (sagaUsesFindGridAudioStaging) 1.5f else 1f,
        correctCellPeakScale = if (sagaUsesFindGridAudioStaging) 1.30f else 1.12f,
        onCellTapped = onCellTapped,
        onCompleted = onCompleted,
        enabled = enabled,
        contentKey = contentKey,
        modifier =
            modifier
                .scale(entryPulseScale)
                .offset { IntOffset(optionsShakePx.toInt(), 0) },
    )
}
