package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tal.hebrewdino.ui.components.station.FindLetterGridStationContent
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.StationUiSpec

@Composable
internal fun FindLetterGridQuestionRenderer(
    current: Question.FindLetterGridQuestion,
    listenOnly: Boolean,
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
    modifier: Modifier = Modifier,
) {
    FindLetterGridStationContent(
        question = current,
        modifier = modifier,
        listenOnly = listenOnly,
        isSagaRevealStation = isSagaRevealStation,
        sagaUsesFindGridAudioStaging = sagaUsesFindGridAudioStaging,
        stationUiSpec = stationUiSpec,
        chapter3ContextWordHint = chapter3ContextWordHint,
        floatingTargetLetterHint = floatingTargetLetterHint,
        episode4TargetCellsHintEpoch = episode4TargetCellsHintEpoch,
        hintPulseEpoch = hintPulseEpoch,
        enabled = enabled,
        contentKey = contentKey,
        entryPulseScale = entryPulseScale,
        optionsShakePx = optionsShakePx,
        onSagaGridLetterTapped = onSagaGridLetterTapped,
        onCorrectTap = onCorrectTap,
        onCellTapped = onCellTapped,
        onCompleted = onCompleted,
    )
}
