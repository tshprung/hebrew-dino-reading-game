package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tal.hebrewdino.ui.components.station.MatchLetterToWordStationContent
import com.tal.hebrewdino.ui.domain.LessonChoice
import com.tal.hebrewdino.ui.domain.StationUiSpec

@Composable
internal fun MatchLetterToWordQuestionRenderer(
    choices: List<LessonChoice>,
    stationUiSpec: StationUiSpec,
    isCompactLandscapePhone: Boolean,
    choicePairLimit: Int,
    contentKey: Int,
    enabled: Boolean,
    entryPulseScale: Float,
    captionSizeMultiplier: Float,
    chapterId: Int,
    stationId: Int,
    instructions: String,
    instructionReadablePanel: Boolean,
    letterTileSizeMultiplier: Float,
    innerPictureScaleForChoice: (LessonChoice) -> Float,
    onWordPressed: (String) -> Unit,
    onLetterPressed: (String) -> Unit,
    onCorrectMatch: (String) -> Unit,
    onWrongMatch: (String, String) -> Unit,
    onMatchAttempt: (Boolean) -> Unit,
    onSolved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MatchLetterToWordStationContent(
        choices = choices,
        choicePairLimit = choicePairLimit,
        contentKey = contentKey,
        enabled = enabled,
        compactWideSpread = isCompactLandscapePhone && stationUiSpec.matchLetterCompactWideSpread,
        letterTileSizeMultiplier = letterTileSizeMultiplier,
        onWordPressed = onWordPressed,
        onLetterPressed = onLetterPressed,
        onCorrectMatch = onCorrectMatch,
        onWrongMatch = onWrongMatch,
        onMatchAttempt = onMatchAttempt,
        innerPictureScaleForChoice = innerPictureScaleForChoice,
        captionSizeMultiplier = captionSizeMultiplier,
        chapterId = chapterId,
        stationId = stationId,
        instructionReadablePanel = instructionReadablePanel,
        instructions = instructions,
        onSolved = onSolved,
        entryPulseScale = entryPulseScale,
        verticalNudgeDp = stationUiSpec.matchLetterVerticalNudgeDp.dp,
        modifier = modifier,
    )
}
