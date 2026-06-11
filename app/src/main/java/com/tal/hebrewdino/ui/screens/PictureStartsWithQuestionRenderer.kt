package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tal.hebrewdino.ui.components.station.PictureStartsWithStationContent
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.Season2Ch1QaPolicy
import com.tal.hebrewdino.ui.domain.StationUiSpec
import com.tal.hebrewdino.ui.layout.ScreenFit

@Composable
internal fun PictureStartsWithQuestionRenderer(
    current: Question.PictureStartsWithQuestion,
    stationUiSpec: StationUiSpec,
    isCompactLandscapePhone: Boolean,
    instructionText: String,
    instructionReadablePanel: Boolean,
    showWordCaption: Boolean,
    onPictureTapReplayWord: (() -> Unit)?,
    temporaryStartingLetterHint: String?,
    pinnedCorrectLetter: String?,
    enabled: Boolean,
    shakePx: Float,
    entryPulseEpoch: Int,
    promptWordSizeMultiplier: Float,
    innerPictureScale: Float,
    pictureSizeMultiplier: Float,
    sortOptionLetters: Boolean,
    chapterId: Int,
    stationId: Int,
    hintCorrectLetter: String?,
    hintPulseEpoch: Int,
    correctPulseLetter: String?,
    correctPulseEpoch: Int,
    wrongFlashLetter: String?,
    wrongFlashEpoch: Int,
    onPickLetter: (String) -> Unit,
    entryPulseScale: Float,
    modifier: Modifier = Modifier,
) {
    val layoutPilot =
        chapterId != null &&
            stationId != null &&
            Season2Ch1QaPolicy.isPictureStartsWithLayoutPilot(chapterId, stationId)
    val displayInstructionText =
        if (isCompactLandscapePhone && stationUiSpec.pictureStartsWithCompactLandscapeRtlWrapInstruction) {
            ScreenFit.rtlUnicodeWrap(instructionText)
        } else {
            instructionText
        }

    PictureStartsWithStationContent(
        question = current,
        instructionText = displayInstructionText,
        instructionReadablePanel = instructionReadablePanel && !layoutPilot,
        useCh1LayoutPilot = layoutPilot,
        showWordCaption = showWordCaption,
        onPictureTapReplayWord = onPictureTapReplayWord,
        temporaryStartingLetterHint = temporaryStartingLetterHint,
        pinnedCorrectLetter = pinnedCorrectLetter,
        enabled = enabled,
        shakePx = shakePx,
        entryPulseEpoch = entryPulseEpoch,
        promptWordSizeMultiplier = promptWordSizeMultiplier,
        innerPictureScale = innerPictureScale,
        pictureSizeMultiplier = pictureSizeMultiplier,
        sortOptionLetters = sortOptionLetters,
        chapterId = chapterId,
        stationId = stationId,
        hintCorrectLetter = hintCorrectLetter,
        hintPulseEpoch = hintPulseEpoch,
        correctPulseLetter = correctPulseLetter,
        correctPulseEpoch = correctPulseEpoch,
        wrongFlashLetter = wrongFlashLetter,
        wrongFlashEpoch = wrongFlashEpoch,
        onPickLetter = onPickLetter,
        entryPulseScale = entryPulseScale,
        verticalNudgeDp = stationUiSpec.pictureStartsWithVerticalNudgeDp.dp,
        modifier = modifier,
    )
}
