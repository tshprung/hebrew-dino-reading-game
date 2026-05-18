package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.domain.Chapter4Config
import com.tal.hebrewdino.ui.domain.Chapter4LetterPoolSpec
import com.tal.hebrewdino.ui.domain.StationQuizPlans

private const val Ch4Title = "פרק 4 - סיבוך בדרך"

@Composable
fun Chapter4LevelScreen(
    stationId: Int,
    onBack: () -> Unit,
    onComplete: (stationId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
    suppressInGameDinoProgress: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val station = stationId.coerceIn(1, Chapter4Config.STATION_COUNT)
    LetterQuizStationScreen(
        stationId = station,
        chapterId = 4,
        chapterTitle = Ch4Title,
        stageLabel = "תחנה $station",
        plan = StationQuizPlans.chapter4(station),
        letterPoolSpec = Chapter4LetterPoolSpec,
        backgroundRes = R.drawable.forest_bg_journey_road,
        onBack = onBack,
        onComplete = onComplete,
        suppressInGameDinoProgress = suppressInGameDinoProgress,
        modifier = modifier,
    )
}
