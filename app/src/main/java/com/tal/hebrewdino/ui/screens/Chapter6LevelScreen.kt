package com.tal.hebrewdino.ui.screens

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.data.PlayerAddress
import com.tal.hebrewdino.ui.domain.DevTools
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
    companionCharacter: DinoCharacter? = null,
    playerAddress: PlayerAddress? = null,
    modifier: Modifier = Modifier,
) {
    val station = stationId.coerceIn(1, Chapter6Config.STATION_COUNT)
    if (companionCharacter == null) {
        val isDebuggable = DevTools.enabled(LocalContext.current)
        val msg =
            "Missing selected companion for station gameplay. chapterId=6 stationId=$station context=Chapter6LevelScreen companionCharacter=null"
        Log.e(
            "MissingContent",
            msg,
        )
        if (isDebuggable) throw IllegalStateException(msg)
    }
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
        chapter1CompanionCharacter = companionCharacter,
        chapter1PlayerAddress = playerAddress,
        modifier = modifier,
    )
}

