package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.domain.Chapter3Config
import com.tal.hebrewdino.ui.domain.Chapter3LetterPoolSpec
import com.tal.hebrewdino.ui.domain.StationQuizPlans

private const val Ch3Title = "פרק 3 - מצא את הביצה הסגולה"

@Composable
fun Chapter3LevelScreen(
    stationId: Int,
    onBack: () -> Unit,
    onComplete: (stationId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
    onLettersHelp: (() -> Unit)? = null,
    onDebugStationAdvance: (() -> Unit)? = null,
    suppressInGameDinoProgress: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val station = stationId.coerceIn(1, Chapter3Config.STATION_COUNT)
    LetterQuizStationScreen(
        stationId = station,
        chapterId = 3,
        chapterTitle = Ch3Title,
        stageLabel = "תחנה $station",
        plan = StationQuizPlans.chapter3(station),
        letterPoolSpec = Chapter3LetterPoolSpec,
        backgroundRes = R.drawable.mountain_bg_chapter3,
        onBack = onBack,
        onComplete = onComplete,
        onLettersHelp = onLettersHelp,
        onDebugStationAdvance = onDebugStationAdvance,
        suppressInGameDinoProgress = suppressInGameDinoProgress,
        modifier = modifier,
    )
}
