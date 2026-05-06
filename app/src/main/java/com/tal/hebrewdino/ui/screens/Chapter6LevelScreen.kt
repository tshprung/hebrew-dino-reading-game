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
    onLettersHelp: (() -> Unit)? = null,
    onDebugStationAdvance: (() -> Unit)? = null,
    suppressInGameDinoProgress: Boolean = false,
    collectedEggStripCount: Int = 0,
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
        backgroundRes = R.drawable.forest_bg_journey_road,
        onBack = onBack,
        onComplete = onComplete,
        onLettersHelp = onLettersHelp,
        onDebugStationAdvance = onDebugStationAdvance,
        suppressInGameDinoProgress = suppressInGameDinoProgress,
        collectedEggStripCount = collectedEggStripCount,
        modifier = modifier,
    )
}

