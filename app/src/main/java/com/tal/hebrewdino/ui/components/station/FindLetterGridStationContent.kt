package com.tal.hebrewdino.ui.components.station

import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.IntOffset
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.InstructionPanelStyle
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.StationInstructionCopy
import com.tal.hebrewdino.ui.domain.StationUiSpec
import com.tal.hebrewdino.ui.domain.TrainingV1Config
import com.tal.hebrewdino.ui.game.FindLetterGridGame
import com.tal.hebrewdino.ui.layout.ScreenFit

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
    onCorrectTap: (() -> Unit)?,
    onCellTapped: (Int) -> Unit,
    onCompleted: () -> Unit,
) {
    val isCompactLandscapePhone = ScreenFit.isCompactLandscapePhone()
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
        compactLandscapeTwoColumn =
            isCompactLandscapePhone &&
                (stationUiSpec.chapterId == 1 ||
                    stationUiSpec.chapterId == 2 ||
                    stationUiSpec.chapterId == 4 ||
                    stationUiSpec.chapterId == 5) &&
                stationUiSpec.stationId == Chapter1StationOrder.REVEAL_THEN_CHOOSE,
        cellSideScale =
            when {
                isCompactLandscapePhone -> 0.9f
                isSagaRevealStation -> 0.9f
                else -> 1f
            },
        contentNudgeDownFraction = 0f,
        onLetterTapped = onSagaGridLetterTapped,
        onCorrectTap = onCorrectTap,
        hintPulseEpoch = hintPulseEpoch,
        hintHeaderPeakScale = if (sagaUsesFindGridAudioStaging) 1.30f else 1.12f,
        gridLetterSizeMultiplier =
            when {
                sagaUsesFindGridAudioStaging -> 1.5f
                stationUiSpec.chapterId == TrainingV1Config.CHAPTER_ID &&
                    stationUiSpec.stationId == TrainingV1Config.STATION_FIND_HEARD_LETTER_IN_GRID -> 1.5f
                else -> 1f
            },
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
