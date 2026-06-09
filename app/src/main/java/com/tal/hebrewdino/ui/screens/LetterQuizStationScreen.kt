package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.data.PlayerAddress
import com.tal.hebrewdino.ui.domain.LetterPoolSpec
import com.tal.hebrewdino.ui.domain.StationQuizPlan

@Composable
fun LetterQuizStationScreen(
    stationId: Int,
    chapterId: Int,
    chapterTitle: String,
    stageLabel: String,
    stationHeaderMode: StationHeaderMode = StationHeaderMode.None,
    topChromeProgressOverride: Pair<Int, Int>? = null,
    plan: StationQuizPlan,
    letterPoolSpec: LetterPoolSpec,
    backgroundRes: Int,
    onBack: () -> Unit,
    onComplete: (stationId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
    suppressInGameDinoProgress: Boolean = false,
    chapter1CompanionCharacter: DinoCharacter? = null,
    chapter1PlayerAddress: PlayerAddress? = null,
    season2Chapter1StationId: Int? = null,
    modifier: Modifier = Modifier,
) {
    GameScreen(
        stationId = stationId,
        chapterId = chapterId,
        chapterTitle = chapterTitle,
        stageLabel = stageLabel,
        stationHeaderMode = stationHeaderMode,
        topChromeProgressOverride = topChromeProgressOverride,
        plan = plan,
        letterPoolSpec = letterPoolSpec,
        backgroundRes = backgroundRes,
        onBack = onBack,
        onComplete = onComplete,
        // Station screens should not show letters help / debug / collected eggs in the top bar.
        suppressInGameDinoProgress = suppressInGameDinoProgress,
        chapter1CompanionCharacter = chapter1CompanionCharacter,
        chapter1PlayerAddress = chapter1PlayerAddress,
        season2Chapter1StationId = season2Chapter1StationId,
        modifier = modifier,
    )
}
