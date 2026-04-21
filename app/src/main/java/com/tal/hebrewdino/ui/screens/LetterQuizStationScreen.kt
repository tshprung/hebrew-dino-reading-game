package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tal.hebrewdino.ui.domain.LetterPoolSpec
import com.tal.hebrewdino.ui.domain.StationQuizPlan

@Composable
fun LetterQuizStationScreen(
    stationId: Int,
    chapterId: Int,
    chapterTitle: String,
    stageLabel: String,
    plan: StationQuizPlan,
    letterPoolSpec: LetterPoolSpec,
    backgroundRes: Int,
    onBack: () -> Unit,
    onComplete: (stationId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
    onLettersHelp: (() -> Unit)? = null,
    onDebugStationAdvance: (() -> Unit)? = null,
    suppressInGameDinoProgress: Boolean = false,
    modifier: Modifier = Modifier,
) {
    GameScreen(
        stationId = stationId,
        chapterId = chapterId,
        chapterTitle = chapterTitle,
        stageLabel = stageLabel,
        plan = plan,
        letterPoolSpec = letterPoolSpec,
        backgroundRes = backgroundRes,
        onBack = onBack,
        onComplete = onComplete,
        onLettersHelp = onLettersHelp,
        onDebugStationAdvance = onDebugStationAdvance,
        suppressInGameDinoProgress = suppressInGameDinoProgress,
        modifier = modifier,
    )
}
