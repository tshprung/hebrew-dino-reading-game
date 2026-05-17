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

@Composable
internal fun PickLetterQuestionRenderer(
    current: Question.PopBalloonsQuestion,
    stationUiSpec: StationUiSpec,
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
    Column(
        modifier = modifier.fillMaxSize().scale(entryPulseScale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        val pickLetterOptions =
            if (sagaUsesPickLetterAudioStaging &&
                station1PinnedCorrectLetter != null &&
                !isChapter3HighlightedLetterInWordStation &&
                !isChapter3AudioLetterRecognitionStation &&
                stationUiSpec.pickLetterAllowPinnedCorrectShortcut
            ) {
                listOf(station1PinnedCorrectLetter)
            } else {
                HebrewLetterOrder.sortForDisplay(current.options)
            }
        val letterOptionsExtraTopPaddingDp =
            when {
                isChapter3AudioLetterRecognitionStation -> 6.dp
                isChapter3HighlightedLetterInWordStation -> SixStationArcHalfCmNudge
                else -> 0.dp
            }
        val pickLetterBoxTopPaddingDp =
            if (sagaUsesPickLetterAudioStaging) SixStationArcHalfCmNudge else 0.dp
        val pickLetterDisplayOptions =
            if (isChapter3AudioLetterRecognitionStation && correctTapPulseLetter != null) {
                listOf(correctTapPulseLetter)
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
            sagaUsesPickLetterAudioStaging = sagaUsesPickLetterAudioStaging,
            station1PinnedCorrectLetter = station1PinnedCorrectLetter,
            pickLetterInstructionOverride = stationUiSpec.pickLetterInstructionOverride,
            pickLetterSagaStation1CompactPreamble = stationUiSpec.pickLetterSagaStation1CompactPreamble,
            showSagaStation1CompactPreamble =
                isSagaEpisode &&
                    stationId == Chapter1StationOrder.TAP_LETTER &&
                    stationUiSpec.pickLetterSagaStation1CompactPreamble != null &&
                    stationUiSpec.pickLetterInstructionOverride == null &&
                    !isChapter3AudioLetterRecognitionStation &&
                    !stationUiSpec.pickLetterListenOnlyHebrewPanel &&
                    !isChapter3HighlightedLetterInWordStation,
            pickLetterAllowPinnedCorrectShortcut = stationUiSpec.pickLetterAllowPinnedCorrectShortcut,
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
