package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.data.Season2ProgressPrefs
import com.tal.hebrewdino.ui.domain.Season2Chapter1LetterPoolSpec
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.Season2Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.Season2ChapterIds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun Season2ChapterStationScreen(
    chapterId: Int,
    stationId: Int,
    onBack: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val season2Progress = remember(context) { Season2ProgressPrefs(context.applicationContext) }
    val completedStations by season2Progress.completedStationsFlow(chapterId).collectAsState(initial = emptySet())
    val alreadyCompleted = stationId in completedStations
    val scope = rememberCoroutineScope()

    // For now: only Season 2 Chapter 1 is playable (Tyrannosaurus).
    val gameplayStationId =
        when (stationId) {
            // Product order for Season 2, but station mechanics should match Season 1 Chapter 1:
            // - S2 st1 = S1 ch1 st2 (balloons)
            Season2Chapter1StationOrder.POP_BALLOONS -> Chapter1StationOrder.BALLOON_POP
            // - S2 st2 = S1 ch1 st1 (pick letter)
            Season2Chapter1StationOrder.PICK_LETTER -> Chapter1StationOrder.TAP_LETTER
            // Station 3 uses the exact visuals of Season 1 Chapter 3 station 1.
            Season2Chapter1StationOrder.PICTURE_STARTS_WITH -> 1
            // - S2 st5 = S1 ch1 st5 (which word starts with)
            Season2Chapter1StationOrder.WHICH_WORD_STARTS_WITH -> Chapter1StationOrder.PICTURE_PICK_ALL
            // - S2 st6 = S1 ch1 st6 (match letter to word)
            Season2Chapter1StationOrder.MATCH_LETTER_TO_WORD -> Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH
            else -> stationId
        }
    // UX: some stations have chapter-specific layout tuning; map to the closest Season 1 station to reuse
    // the exact same visuals (per request).
    val gameplayChapterId =
        when (stationId) {
            // Season 1 Chapter 3 station 1 is the "gold standard" layout for this question type.
            Season2Chapter1StationOrder.PICTURE_STARTS_WITH -> 3
            else -> 1
        }

    fun markDoneAndExit() {
        scope.launch {
            season2Progress.markStationCompleted(chapterId, stationId)
            onComplete()
        }
    }

    when (stationId) {
        Season2Chapter1StationOrder.MEMORY_MATCH -> {
            Season2MemoryMatchStationScreen(
                // Include extra familiar letters so memory match can run 2–3 rounds without feeling repetitive.
                letters = listOf("ז", "י", "ס", "ע", "מ", "ל"),
                rounds = 3,
                onBack = onBack,
                onMarkCompleted = { markDoneAndExit() },
                modifier = modifier,
            )
        }
        else -> {
            Season2ScopedLetterStationScreen(
                chapterId = chapterId,
                gameplayStationId = gameplayStationId,
                gameplayChapterId = gameplayChapterId,
                suppressInGameDinoProgress = alreadyCompleted,
                onBack = onBack,
                onMarkCompleted = { markDoneAndExit() },
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun Season2ScopedLetterStationScreen(
    chapterId: Int,
    gameplayStationId: Int,
    gameplayChapterId: Int,
    suppressInGameDinoProgress: Boolean,
    onBack: () -> Unit,
    onMarkCompleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val plan =
        remember(gameplayChapterId, gameplayStationId) {
            when (gameplayChapterId) {
                3 -> com.tal.hebrewdino.ui.domain.StationQuizPlans.chapter3(gameplayStationId)
                else -> com.tal.hebrewdino.ui.domain.StationQuizPlans.chapter1(gameplayStationId)
            }
        }

    // Keep copy minimal; Season 2 UX is driven by the puzzle board + reveal.
    val title = "\u200Fעונה 2 · פרק $chapterId"
    val stage = "\u200Fתחנה $chapterId-$gameplayStationId"

    LetterQuizStationScreen(
        stationId = gameplayStationId,
        chapterId = gameplayChapterId,
        chapterTitle = title,
        stageLabel = stage,
        plan = plan,
        letterPoolSpec = Season2Chapter1LetterPoolSpec,
        backgroundRes = R.drawable.forest_bg_level_overlay,
        onBack = onBack,
        onComplete = { _, _, _ ->
            onMarkCompleted()
        },
        suppressInGameDinoProgress = suppressInGameDinoProgress,
        modifier = modifier,
    )
}

