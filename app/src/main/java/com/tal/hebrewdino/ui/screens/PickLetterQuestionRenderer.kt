package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.tal.hebrewdino.ui.components.station.PickLetterStationContent
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.HebrewLetterOrder
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.StationUiSpec
import com.tal.hebrewdino.ui.domain.TrainingInstructionCopy
import com.tal.hebrewdino.ui.domain.TrainingV1Config
import com.tal.hebrewdino.ui.data.PlayerAddress

@Composable
internal fun PickLetterQuestionRenderer(
    current: Question.PopBalloonsQuestion,
    stationUiSpec: StationUiSpec,
    chapter1PlayerAddress: PlayerAddress?,
    listenOnly: Boolean,
    isSagaEpisode: Boolean,
    sagaUsesPickLetterAudioStaging: Boolean,
    chapterId: Int,
    stationId: Int,
    highlightedInWordWord: String?,
    highlightedInWordSlotIndex: Int?,
    isChapter3HighlightedLetterInWordStation: Boolean,
    isChapter3AudioLetterRecognitionStation: Boolean,
    station1PinnedCorrectLetter: String?,
    entryPulseScale: Float,
    enabled: Boolean,
    shakePx: Float,
    wrongTapsThisQuestion: Int,
    hintPulseEpoch: Int,
    correctTapPulseLetter: String?,
    correctTapPulseEpoch: Int,
    temporaryHintLetter: String?,
    onRepeatLetterClick: () -> Unit,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val resolvedPickLetterInstructionOverride =
        when {
            chapterId == TrainingV1Config.CHAPTER_ID &&
                stationId == TrainingV1Config.STATION_HEAR_LETTER_CHOOSE &&
                chapter1PlayerAddress != null ->
                TrainingInstructionCopy.pickLetter(chapter1PlayerAddress)
            (chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5) &&
                stationId == Chapter1StationOrder.TAP_LETTER &&
                chapter1PlayerAddress != null ->
                when (chapter1PlayerAddress) {
                    PlayerAddress.Boy -> "\u200Fבחר את האות:"
                    PlayerAddress.Girl -> "\u200Fבחרי את האות:"
                }
            else -> stationUiSpec.pickLetterInstructionOverride
        }
    Column(
        modifier = modifier.fillMaxSize().scale(entryPulseScale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        val pickLetterOptions = HebrewLetterOrder.sortForDisplay(current.options)
        val letterOptionsExtraTopPaddingDp =
            when {
                isChapter3AudioLetterRecognitionStation -> 6.dp
                isChapter3HighlightedLetterInWordStation -> SixStationArcHalfCmNudge
                else -> 0.dp
            }
        val pickLetterBoxTopPaddingDp =
            when {
                sagaUsesPickLetterAudioStaging -> SixStationArcHalfCmNudge
                chapterId == 6 && stationId == 4 -> SixStationArcHalfCmNudge
                chapterId == 6 && stationId == 5 -> SixStationArcHalfCmNudge
                chapterId == TrainingV1Config.CHAPTER_ID && stationId == TrainingV1Config.STATION_HEAR_LETTER_CHOOSE -> SixStationArcHalfCmNudge
                else -> 0.dp
            }
        val pinnedCorrect = station1PinnedCorrectLetter
        val pickLetterDisplayOptions =
            if (isChapter3AudioLetterRecognitionStation && !enabled && pinnedCorrect != null) {
                listOf(current.correctAnswer)
            } else if (isChapter3AudioLetterRecognitionStation && correctTapPulseLetter != null) {
                listOf(correctTapPulseLetter)
            } else if (isChapter3HighlightedLetterInWordStation && !enabled && pinnedCorrect != null) {
                listOf(current.correctAnswer)
            } else if (isChapter3HighlightedLetterInWordStation && correctTapPulseLetter != null) {
                listOf(correctTapPulseLetter)
            } else if (!enabled &&
                pinnedCorrect != null
            ) {
                listOf(pinnedCorrect)
            } else {
                pickLetterOptions
            }

        PickLetterStationContent(
            question = current,
            highlightedInWordWord = highlightedInWordWord,
            highlightedInWordSlotIndex = highlightedInWordSlotIndex,
            highlightedInWordInstruction = stationUiSpec.pickLetterHighlightedInWordInstruction,
            showTargetLetterChip = !listenOnly && !isChapter3HighlightedLetterInWordStation,
            temporaryHintLetter = temporaryHintLetter,
            showListenOnlyHebrewPanel = stationUiSpec.pickLetterListenOnlyHebrewPanel,
            listenOnlyPanelInstruction = stationUiSpec.pickLetterListenOnlyInstructionText,
            repeatLetterButtonLabel =
                if (isChapter3AudioLetterRecognitionStation) {
                    null
                } else {
                    stationUiSpec.pickLetterRepeatLetterButtonLabel
                },
            pickLetterInstructionOverride = resolvedPickLetterInstructionOverride,
            pickLetterSagaStation1CompactPreamble = stationUiSpec.pickLetterSagaStation1CompactPreamble,
            showSagaStation1CompactPreamble =
                isSagaEpisode &&
                    stationId == Chapter1StationOrder.TAP_LETTER &&
                    stationUiSpec.pickLetterSagaStation1CompactPreamble != null &&
                    resolvedPickLetterInstructionOverride == null &&
                    !isChapter3AudioLetterRecognitionStation &&
                    !stationUiSpec.pickLetterListenOnlyHebrewPanel &&
                    !isChapter3HighlightedLetterInWordStation,
            boxTopPaddingDp = pickLetterBoxTopPaddingDp,
            letterOptionsExtraTopPaddingDp = letterOptionsExtraTopPaddingDp,
            enabled = enabled,
            shakePx = shakePx,
            correctPulseLetter = correctTapPulseLetter ?: current.correctAnswer.takeIf { wrongTapsThisQuestion >= 2 },
            correctPulseEpoch = hintPulseEpoch + correctTapPulseEpoch,
            letterOptions = pickLetterDisplayOptions,
            chapterId = chapterId,
            stationId = stationId,
            strongLetterButtonFeedback = isChapter3AudioLetterRecognitionStation,
            onRepeatLetterClick = onRepeatLetterClick,
            onPick = onPick,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
