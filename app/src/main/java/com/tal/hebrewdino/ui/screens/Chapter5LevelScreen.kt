package com.tal.hebrewdino.ui.screens

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.data.PlayerAddress
import com.tal.hebrewdino.ui.domain.Chapter5Config
import com.tal.hebrewdino.ui.domain.Chapter5LetterPoolSpec
import com.tal.hebrewdino.ui.domain.DevTools
import com.tal.hebrewdino.ui.domain.StationQuizPlans

private const val Ch5Title = "פרק 5 - הביצה השלישית"

@Composable
fun Chapter5LevelScreen(
    stationId: Int,
    onBack: () -> Unit,
    onComplete: (stationId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
    suppressInGameDinoProgress: Boolean = false,
    companionCharacter: DinoCharacter? = null,
    playerAddress: PlayerAddress? = null,
    modifier: Modifier = Modifier,
) {
    val station = stationId.coerceIn(1, Chapter5Config.STATION_COUNT)
    if (companionCharacter == null) {
        val isDebuggable = DevTools.enabled(LocalContext.current)
        val msg =
            "Missing selected companion for station gameplay. chapterId=5 stationId=$station context=Chapter5LevelScreen companionCharacter=null"
        Log.e(
            "MissingContent",
            msg,
        )
        if (isDebuggable) throw IllegalStateException(msg)
    }
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
        suppressInGameDinoProgress = suppressInGameDinoProgress,
        chapter1CompanionCharacter = companionCharacter,
        chapter1PlayerAddress = playerAddress,
        modifier = modifier,
    )
}
