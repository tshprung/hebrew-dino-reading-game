package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.domain.Chapter2Config
import com.tal.hebrewdino.ui.domain.Chapter2LetterPoolSpec
import com.tal.hebrewdino.ui.domain.StationQuizPlans

private const val Ch2Title = "פרק 2 - מצא את הביצה הורודה"

@Composable
fun Chapter2LevelScreen(
    stationId: Int,
    onBack: () -> Unit,
    onComplete: (stationId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
    onLettersHelp: (() -> Unit)? = null,
    onDebugStationAdvance: (() -> Unit)? = null,
    suppressInGameDinoProgress: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val station = stationId.coerceIn(1, Chapter2Config.STATION_COUNT)
    LetterQuizStationScreen(
        stationId = station,
        chapterId = 2,
        chapterTitle = Ch2Title,
        stageLabel = "תחנה $station",
        plan = StationQuizPlans.chapter2(station),
        letterPoolSpec = Chapter2LetterPoolSpec,
        backgroundRes = R.drawable.mountain_bg_chapter2,
        onBack = onBack,
        onComplete = onComplete,
        onLettersHelp = onLettersHelp,
        onDebugStationAdvance = onDebugStationAdvance,
        suppressInGameDinoProgress = suppressInGameDinoProgress,
        modifier = modifier,
    )
}
