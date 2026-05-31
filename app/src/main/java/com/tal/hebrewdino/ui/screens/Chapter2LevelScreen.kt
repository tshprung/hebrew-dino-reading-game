package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.data.PlayerAddress
import com.tal.hebrewdino.ui.domain.Chapter2Config
import com.tal.hebrewdino.ui.domain.Chapter2LetterPoolSpec
import com.tal.hebrewdino.ui.domain.StationQuizPlans

private const val Ch2Title = "פרק 2 - מוצאים עקבות לביצה הורודה"

@Composable
fun Chapter2LevelScreen(
    stationId: Int,
    onBack: () -> Unit,
    onComplete: (stationId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
    suppressInGameDinoProgress: Boolean = false,
    playerAddress: PlayerAddress? = null,
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
        backgroundRes = R.drawable.chapter2_level_overlay,
        onBack = onBack,
        onComplete = onComplete,
        suppressInGameDinoProgress = suppressInGameDinoProgress,
        chapter1PlayerAddress = playerAddress,
        modifier = modifier,
    )
}
