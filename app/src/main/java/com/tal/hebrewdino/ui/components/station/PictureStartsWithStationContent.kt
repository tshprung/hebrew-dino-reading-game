package com.tal.hebrewdino.ui.components.station

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.game.PictureStartsWithGame

/**
 * Picture-starts-with station UI for [com.tal.hebrewdino.ui.screens.GameScreen].
 * Caller owns session, audio jobs, and [onPickLetter] behavior.
 */
@Composable
fun PictureStartsWithStationContent(
    question: Question.PictureStartsWithQuestion,
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
    /** Non-zero for saga station 4 picture nudge (matches GameScreen SixStationArcHalfCmNudge). */
    verticalNudgeDp: Dp,
    modifier: Modifier = Modifier,
) {
    PictureStartsWithGame(
        question = question,
        instructionText = instructionText,
        instructionReadablePanel = instructionReadablePanel,
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
        chapterId = chapterId,
        stationId = stationId,
        hintCorrectLetter = hintCorrectLetter,
        hintPulseEpoch = hintPulseEpoch,
        correctPulseLetter = correctPulseLetter,
        correctPulseEpoch = correctPulseEpoch,
        wrongFlashLetter = wrongFlashLetter,
        wrongFlashEpoch = wrongFlashEpoch,
        onPickLetter = onPickLetter,
        modifier =
            modifier
                .fillMaxSize()
                .scale(entryPulseScale)
                .then(
                    if (verticalNudgeDp > 0.dp) {
                        Modifier.offset(y = verticalNudgeDp)
                    } else {
                        Modifier
                    },
                ),
    )
}
