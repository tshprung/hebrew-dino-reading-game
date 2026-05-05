package com.tal.hebrewdino.ui.components.station

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.tal.hebrewdino.ui.domain.LessonChoice
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.game.ImageMatchGame
import com.tal.hebrewdino.ui.game.ImageToWordGame

/**
 * Chapter 3 station 6: image → word choice UI for [com.tal.hebrewdino.ui.screens.GameScreen].
 */
@Composable
fun Chapter3Station6ImageToWordStationContent(
    question: Question.ImageMatchQuestion,
    contentKey: Int,
    enabled: Boolean,
    entryPulseScale: Float,
    optionsShakePx: Float,
    onWordPressed: (String) -> Unit,
    onAttempt: (String) -> Boolean,
    modifier: Modifier = Modifier,
) {
    ImageToWordGame(
        question = question,
        contentKey = contentKey,
        enabled = enabled,
        instructionText = "איזו מילה מתאימה לתמונה של:",
        onWordPressed = onWordPressed,
        onAttempt = onAttempt,
        modifier =
            modifier
                .fillMaxSize()
                .scale(entryPulseScale)
                .offset { IntOffset(optionsShakePx.toInt(), 0) },
    )
}

/**
 * Standard [ImageMatchGame] station UI (saga station 5 and similar) for [com.tal.hebrewdino.ui.screens.GameScreen].
 */
@Composable
fun SagaImageMatchGameStationContent(
    question: Question.ImageMatchQuestion,
    headerInstructionText: String?,
    headerInstructionFontScale: Float,
    headerPromptWord: String?,
    showTargetLetterChip: Boolean,
    listenOnlyTemporaryHintLetter: String?,
    headerTopPaddingDp: Int,
    readableInstructionHeaderPanel: Boolean,
    targetLetterChipOffsetYDp: Int,
    contentKey: Int,
    enabled: Boolean,
    shakePx: Float,
    entryPulseEpoch: Int,
    hintCorrectChoiceId: String?,
    hintPulseEpoch: Int,
    showWordCaptions: Boolean,
    captionSizeMultiplier: Float,
    pictureSizeMultiplier: Float,
    innerPictureScaleForChoice: (LessonChoice) -> Float,
    chapterId: Int?,
    stationId: Int?,
    onAttempt: (String) -> Boolean,
    entryPulseScale: Float,
    verticalNudgeDp: Dp,
    modifier: Modifier = Modifier,
) {
    ImageMatchGame(
        question = question,
        headerInstructionText = headerInstructionText,
        headerInstructionFontScale = headerInstructionFontScale,
        headerPromptWord = headerPromptWord,
        showTargetLetterChip = showTargetLetterChip,
        listenOnlyTemporaryHintLetter = listenOnlyTemporaryHintLetter,
        headerTopPaddingDp = headerTopPaddingDp,
        readableInstructionHeaderPanel = readableInstructionHeaderPanel,
        targetLetterChipOffsetYDp = targetLetterChipOffsetYDp,
        contentKey = contentKey,
        enabled = enabled,
        shakePx = shakePx,
        entryPulseEpoch = entryPulseEpoch,
        hintCorrectChoiceId = hintCorrectChoiceId,
        hintPulseEpoch = hintPulseEpoch,
        showWordCaptions = showWordCaptions,
        captionSizeMultiplier = captionSizeMultiplier,
        pictureSizeMultiplier = pictureSizeMultiplier,
        innerPictureScaleForChoice = innerPictureScaleForChoice,
        chapterId = chapterId,
        stationId = stationId,
        onAttempt = onAttempt,
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
