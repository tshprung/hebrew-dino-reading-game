package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.domain.Chapter5Config
import com.tal.hebrewdino.ui.domain.Chapter5LetterPoolSpec
import com.tal.hebrewdino.ui.domain.StationQuizPlans

private const val Ch5Title = "פרק 5 - הביצה השלישית"

@Composable
fun Chapter5LevelScreen(
    stationId: Int,
    onBack: () -> Unit,
    onComplete: (stationId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
    onLettersHelp: (() -> Unit)? = null,
    onDebugStationAdvance: (() -> Unit)? = null,
    suppressInGameDinoProgress: Boolean = false,
    collectedEggStripCount: Int = 0,
    modifier: Modifier = Modifier,
) {
    val station = stationId.coerceIn(1, Chapter5Config.STATION_COUNT)
    LetterQuizStationScreen(
        stationId = station,
        chapterId = 5,
        chapterTitle = Ch5Title,
        stageLabel = "תחנה $station",
        plan = StationQuizPlans.chapter5(station),
        letterPoolSpec = Chapter5LetterPoolSpec,
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
