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
    stationHeaderMode: StationHeaderMode = StationHeaderMode.None,
    topChromeProgressOverride: Pair<Int, Int>? = null,
    plan: StationQuizPlan,
    letterPoolSpec: LetterPoolSpec,
    backgroundRes: Int,
    onBack: () -> Unit,
    onComplete: (stationId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
    suppressInGameDinoProgress: Boolean = false,
    modifier: Modifier = Modifier,
) {
    GameScreen(
        stationId = stationId,
        chapterId = chapterId,
        chapterTitle = chapterTitle,
        stageLabel = stageLabel,
        stationHeaderMode = stationHeaderMode,
        topChromeProgressOverride = topChromeProgressOverride,
        plan = plan,
        letterPoolSpec = letterPoolSpec,
        backgroundRes = backgroundRes,
        onBack = onBack,
        onComplete = onComplete,
        // Station screens should not show letters help / debug / collected eggs in the top bar.
        suppressInGameDinoProgress = suppressInGameDinoProgress,
        modifier = modifier,
    )
}
