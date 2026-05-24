package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.domain.Chapter6Config
import com.tal.hebrewdino.ui.domain.Chapter6LetterPoolSpec
import com.tal.hebrewdino.ui.domain.StationQuizPlans

private const val Ch6Title = "פרק 6 - חוזרים הביתה"

@Composable
fun Chapter6LevelScreen(
    stationId: Int,
    onBack: () -> Unit,
    onComplete: (stationId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
    suppressInGameDinoProgress: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val station = stationId.coerceIn(1, Chapter6Config.STATION_COUNT)
    LetterQuizStationScreen(
        stationId = station,
        chapterId = 6,
        chapterTitle = Ch6Title,
        stageLabel = "תחנה $station",
        plan = StationQuizPlans.chapter6(station),
        letterPoolSpec = Chapter6LetterPoolSpec,
        backgroundRes = R.drawable.ch3_journey_bg,
        onBack = onBack,
        onComplete = onComplete,
        suppressInGameDinoProgress = suppressInGameDinoProgress,
        modifier = modifier,
    )
}

