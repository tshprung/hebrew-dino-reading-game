package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tal.hebrewdino.ui.domain.LetterPoolSpec
import com.tal.hebrewdino.ui.domain.StationQuizPlan

@Composable
fun LetterQuizStationScreen(
    stationId: Int,
    chapterTitle: String,
    stageLabel: String,
    plan: StationQuizPlan,
    letterPoolSpec: LetterPoolSpec,
    onBack: () -> Unit,
    onComplete: (stationId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
    onLettersHelp: (() -> Unit)? = null,
    onDebugStationAdvance: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    GameScreen(
        stationId = stationId,
        chapterTitle = chapterTitle,
        stageLabel = stageLabel,
        plan = plan,
        letterPoolSpec = letterPoolSpec,
        onBack = onBack,
        onComplete = onComplete,
        onLettersHelp = onLettersHelp,
        onDebugStationAdvance = onDebugStationAdvance,
        modifier = modifier,
    )
}
