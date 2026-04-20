package com.tal.hebrewdino.ui.components.learning

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tal.hebrewdino.ui.domain.LetterPoolSpec
import com.tal.hebrewdino.ui.domain.StationQuizMode
import com.tal.hebrewdino.ui.domain.StationQuizPlan
import com.tal.hebrewdino.ui.screens.GameScreen

/** Chapter 2–3 finale: slot words + letter pool (same engine as chapter 1 finale). */
@Composable
fun PictureLetterMatchStation(
    stationId: Int,
    chapterId: Int,
    chapterTitle: String,
    questionCount: Int,
    initialGroupIndex: Int,
    letterPoolSpec: LetterPoolSpec,
    backgroundRes: Int,
    onBack: () -> Unit,
    onComplete: (stationId: Int, correctCount: Int, mistakeCount: Int) -> Unit,
    onLettersHelp: (() -> Unit)? = null,
    onDebugStationAdvance: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    GameScreen(
        stationId = stationId,
        chapterId = chapterId,
        chapterTitle = chapterTitle,
        stageLabel = "תחנה $stationId",
        plan =
            StationQuizPlan(
                mode = StationQuizMode.FinaleSlot,
                questionCount = questionCount,
                initialGroupIndex = initialGroupIndex,
            ),
        letterPoolSpec = letterPoolSpec,
        backgroundRes = backgroundRes,
        onBack = onBack,
        onComplete = onComplete,
        onLettersHelp = onLettersHelp,
        onDebugStationAdvance = onDebugStationAdvance,
        modifier = modifier,
    )
}
