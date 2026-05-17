package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tal.hebrewdino.ui.components.station.SagaImageMatchGameStationContent
import com.tal.hebrewdino.ui.domain.LessonChoice
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.StationUiSpec
import com.tal.hebrewdino.ui.layout.ScreenFit

@Composable
internal fun ImageMatchQuestionRenderer(
    current: Question.ImageMatchQuestion,
    stationUiSpec: StationUiSpec,
    isCompactLandscapePhone: Boolean,
    headerInstructionFontScale: Float,
    listenOnlyTemporaryHintLetter: String?,
    contentKey: Int,
    enabled: Boolean,
    shakePx: Float,
    entryPulseEpoch: Int,
    hintCorrectChoiceId: String?,
    hintPulseEpoch: Int,
    captionSizeMultiplier: Float,
    pictureSizeMultiplier: Float,
    innerPictureScaleForChoice: (LessonChoice) -> Float,
    chapterId: Int?,
    stationId: Int?,
    onAttempt: (String) -> Boolean,
    entryPulseScale: Float,
    modifier: Modifier = Modifier,
) {
    val rawHeaderInstructionText = stationUiSpec.imageMatchHeaderInstructionOverride
    val displayHeaderInstructionText =
        if (isCompactLandscapePhone &&
            stationUiSpec.imageMatchCompactLandscapeRtlWrapHeaderInstruction &&
            rawHeaderInstructionText != null
        ) {
            ScreenFit.rtlUnicodeWrap(rawHeaderInstructionText)
        } else {
            rawHeaderInstructionText
        }

    SagaImageMatchGameStationContent(
        question = current,
        headerInstructionText = displayHeaderInstructionText,
        headerInstructionFontScale = headerInstructionFontScale,
        headerPromptWord = current.targetWord.takeIf { stationUiSpec.imageMatchShowHeaderPromptWord },
        showTargetLetterChip = stationUiSpec.imageMatchShowTargetLetterChip,
        listenOnlyTemporaryHintLetter = listenOnlyTemporaryHintLetter,
        headerTopPaddingDp = stationUiSpec.imageMatchHeaderTopPaddingDp.toInt(),
        readableInstructionHeaderPanel = stationUiSpec.imageMatchHeaderReadablePanel,
        targetLetterChipOffsetYDp = stationUiSpec.imageMatchTargetLetterChipOffsetYDp.toInt(),
        contentKey = contentKey,
        enabled = enabled,
        shakePx = shakePx,
        entryPulseEpoch = if (stationUiSpec.imageMatchSuppressEntryPulseEpoch) 0 else entryPulseEpoch,
        hintCorrectChoiceId = hintCorrectChoiceId,
        hintPulseEpoch = hintPulseEpoch,
        showWordCaptions = !stationUiSpec.imageMatchHideWordCaptions,
        captionSizeMultiplier = captionSizeMultiplier,
        pictureSizeMultiplier = pictureSizeMultiplier,
        innerPictureScaleForChoice = innerPictureScaleForChoice,
        chapterId = chapterId,
        stationId = stationId,
        onAttempt = onAttempt,
        entryPulseScale = entryPulseScale,
        verticalNudgeDp = stationUiSpec.imageMatchVerticalNudgeDp.dp,
        modifier = modifier,
    )
}
