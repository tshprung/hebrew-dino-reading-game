package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tal.hebrewdino.ui.components.station.Chapter3Station6ImageToWordStationContent
import com.tal.hebrewdino.ui.domain.Question

@Composable
internal fun ImageToWordQuestionRenderer(
    current: Question.ImageMatchQuestion,
    contentKey: Int,
    enabled: Boolean,
    entryPulseScale: Float,
    optionsShakePx: Float,
    instructionText: String,
    chapterId: Int,
    stationId: Int,
    trainingRoundIndex: Int? = null,
    onPictureTapReplayWord: (() -> Unit)?,
    onWordPressed: (String) -> Unit,
    onAttempt: (String) -> Boolean,
    modifier: Modifier = Modifier,
) {
    Chapter3Station6ImageToWordStationContent(
        question = current,
        contentKey = contentKey,
        enabled = enabled,
        entryPulseScale = entryPulseScale,
        optionsShakePx = optionsShakePx,
        instructionText = instructionText,
        chapterId = chapterId,
        stationId = stationId,
        trainingRoundIndex = trainingRoundIndex,
        onPictureTapReplayWord = onPictureTapReplayWord,
        onAttempt = onAttempt,
        modifier = modifier,
    )
}
