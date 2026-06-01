package com.tal.hebrewdino.ui.screens

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.data.PlayerAddress
import com.tal.hebrewdino.ui.domain.Chapter2Config
import com.tal.hebrewdino.ui.domain.Chapter2LetterPoolSpec
import com.tal.hebrewdino.ui.domain.DevTools
import com.tal.hebrewdino.ui.domain.StationQuizPlans

private const val Ch2Title = "פרק 2 - מוצאים עקבות לביצה הורודה"

@Composable
fun Chapter2LevelScreen(
    stationId: Int,
    onBack: () -> Unit,
    onComplete: (stationId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
    suppressInGameDinoProgress: Boolean = false,
    companionCharacter: DinoCharacter? = null,
    playerAddress: PlayerAddress? = null,
    modifier: Modifier = Modifier,
) {
    val station = stationId.coerceIn(1, Chapter2Config.STATION_COUNT)
    if (companionCharacter == null) {
        val isDebuggable = DevTools.enabled(LocalContext.current)
        val msg =
            "Missing selected companion for station gameplay. chapterId=2 stationId=$station context=Chapter2LevelScreen companionCharacter=null"
        Log.e(
            "MissingContent",
            msg,
        )
        if (isDebuggable) throw IllegalStateException(msg)
    }
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
        chapter1CompanionCharacter = companionCharacter,
        chapter1PlayerAddress = playerAddress,
        modifier = modifier,
    )
}
