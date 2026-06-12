package com.tal.hebrewdino.ui.screens



import androidx.compose.runtime.Composable

import androidx.compose.runtime.collectAsState

import androidx.compose.runtime.getValue

import androidx.compose.runtime.remember

import androidx.compose.runtime.rememberCoroutineScope

import androidx.compose.ui.Modifier

import androidx.compose.ui.platform.LocalContext

import com.tal.hebrewdino.R

import com.tal.hebrewdino.ui.data.DinoCharacter

import com.tal.hebrewdino.ui.data.PlayerAddress

import com.tal.hebrewdino.ui.data.Season2ProgressPrefs

import com.tal.hebrewdino.ui.domain.Chapter1StationOrder

import com.tal.hebrewdino.ui.domain.Season2Chapter1RevealOrder
import com.tal.hebrewdino.ui.domain.Season2ChapterFlowPolicy
import com.tal.hebrewdino.ui.domain.Season2Chapter1StationOrder

import com.tal.hebrewdino.ui.domain.Season2ChapterRegistry

import com.tal.hebrewdino.ui.domain.Season2ChapterStationPlans

import com.tal.hebrewdino.ui.domain.Season2Copy
import com.tal.hebrewdino.ui.domain.Season2IntroFlow

import com.tal.hebrewdino.ui.domain.Season2StationTheme

import com.tal.hebrewdino.ui.domain.Season2StationThemeCopy

import com.tal.hebrewdino.ui.domain.StationQuizPlan

import kotlinx.coroutines.launch



@Composable

fun Season2ChapterStationScreen(

    chapterId: Int,

    stationId: Int,

    companionCharacter: DinoCharacter,

    playerAddress: PlayerAddress,

    onBack: () -> Unit,

    onComplete: (requestChapterCelebration: Boolean, mapReturnCaptionCompletedCount: Int?) -> Unit,

    modifier: Modifier = Modifier,

) {

    val chapterDef =

        remember(chapterId) { Season2ChapterRegistry.chapter(chapterId) }

            ?: return

    require(chapterDef.isPlayable) { "Chapter $chapterId is not playable" }



    val context = LocalContext.current

    val season2Progress = remember(context) { Season2ProgressPrefs(context.applicationContext) }

    val completedStations by season2Progress.completedStationsFlow(chapterId).collectAsState(initial = emptySet())

    val alreadyCompleted = stationId in completedStations

    val scope = rememberCoroutineScope()



    val routing =

        remember(chapterId, stationId, chapterDef) {

            resolveStationRouting(chapterId = chapterId, stationId = stationId, chapterDef = chapterDef)

        }



    fun markDoneAndExit() {

        scope.launch {

            val wasStationDone = stationId in completedStations

            val chapterWasComplete =

                completedStations.size >= Season2Chapter1RevealOrder.STATION_COUNT

            season2Progress.markStationCompleted(chapterId, stationId)

            val requestCelebration =
                if (chapterId in 1..6) {
                    Season2ChapterFlowPolicy.shouldRequestFirstTimeChapterReward(
                        stationId = stationId,
                        wasStationAlreadyDone = wasStationDone,
                        chapterWasCompleteBefore = chapterWasComplete,
                    )
                } else {
                    Season2IntroFlow.shouldRequestChapterCelebration(
                        stationId = stationId,
                        wasStationAlreadyDone = wasStationDone,
                        chapterWasCompleteBefore = chapterWasComplete,
                    )
                }

            if (requestCelebration && !chapterWasComplete) {
                season2Progress.markChapterCompleted(chapterId)
            }

            val mapCaptionCount =
                if (!wasStationDone) {
                    (completedStations + stationId).size
                } else {
                    null
                }
            onComplete(requestCelebration, mapCaptionCount)

        }

    }



    val stageLabel = remember(chapterId, stationId, chapterDef.stationTheme) {

        themedStageLabel(stationId = stationId, theme = chapterDef.stationTheme)

    }



    when (stationId) {

        Season2Chapter1StationOrder.MEMORY_MATCH -> {

            Season2MemoryMatchStationScreen(

                letters = chapterDef.memoryMatchLetters,

                rounds = 3,

                companionCharacter = companionCharacter,

                playerAddress = playerAddress,

                onBack = onBack,

                onMarkCompleted = { markDoneAndExit() },

                modifier = modifier,

            )

        }

        else -> {

            Season2ScopedLetterStationScreen(

                chapterId = chapterId,

                stationId = stationId,

                gameplayStationId = routing.gameplayStationId,

                gameplayChapterId = routing.gameplayChapterId,

                plan = routing.plan,

                letterPoolSpec = chapterDef.letterPoolSpec,

                stageLabel = stageLabel,

                companionCharacter = companionCharacter,

                playerAddress = playerAddress,

                suppressInGameDinoProgress = alreadyCompleted,

                onBack = onBack,

                onMarkCompleted = { markDoneAndExit() },

                modifier = modifier,

            )

        }

    }

}



private data class Season2StationRouting(

    val gameplayStationId: Int,

    val gameplayChapterId: Int,

    val plan: StationQuizPlan,

)



private fun resolveStationRouting(

    chapterId: Int,

    stationId: Int,

    chapterDef: com.tal.hebrewdino.ui.domain.Season2ChapterDefinition,

): Season2StationRouting {

    if (chapterId >= 3) {

        val ctx =

            chapterDef.stationContext

                ?: error("Chapter $chapterId missing station context")

        return Season2StationRouting(

            gameplayStationId = stationId,

            gameplayChapterId = chapterDef.gameplayChapterId,

            plan = Season2ChapterStationPlans.quizPlan(ctx, stationId),

        )

    }

    if (stationId == Season2Chapter1StationOrder.FINALE_STATION) {
        return Season2StationRouting(
            gameplayStationId = stationId,
            gameplayChapterId = chapterDef.gameplayChapterId,
            plan = Season2Chapter1StationOrder.quizPlan(chapterId, stationId),
        )
    }

    val gameplayStationId =
        when (stationId) {
            Season2Chapter1StationOrder.POP_BALLOONS -> Chapter1StationOrder.BALLOON_POP
            Season2Chapter1StationOrder.PICK_LETTER -> Chapter1StationOrder.TAP_LETTER
            Season2Chapter1StationOrder.PICTURE_STARTS_WITH -> 1
            Season2Chapter1StationOrder.WHICH_WORD_STARTS_WITH -> Chapter1StationOrder.PICTURE_PICK_ALL
            else -> stationId
        }

    val gameplayChapterId =
        when (stationId) {
            Season2Chapter1StationOrder.PICTURE_STARTS_WITH -> 3
            else -> 1
        }

    val plan =
        when (gameplayChapterId) {
            3 -> com.tal.hebrewdino.ui.domain.StationQuizPlans.chapter3(gameplayStationId)
            else -> com.tal.hebrewdino.ui.domain.StationQuizPlans.chapter1(gameplayStationId)
        }

    return Season2StationRouting(
        gameplayStationId = gameplayStationId,
        gameplayChapterId = gameplayChapterId,
        plan = plan,
    )

}



private fun themedStageLabel(stationId: Int, theme: Season2StationTheme): String {

    val base = "\u200Fתחנה $stationId"

    val suffix = Season2StationThemeCopy.stageLabelSuffix(theme, stationId)

    return if (suffix != null) base + suffix else base

}



@Composable

private fun Season2ScopedLetterStationScreen(

    chapterId: Int,

    stationId: Int,

    gameplayStationId: Int,

    gameplayChapterId: Int,

    plan: StationQuizPlan,

    letterPoolSpec: com.tal.hebrewdino.ui.domain.LetterPoolSpec,

    stageLabel: String,

    companionCharacter: DinoCharacter,

    playerAddress: PlayerAddress,

    suppressInGameDinoProgress: Boolean,

    onBack: () -> Unit,

    onMarkCompleted: () -> Unit,

    modifier: Modifier = Modifier,

) {

    val title = "\u200F${Season2Copy.SeasonTitle} · ${Season2Copy.MysteryMapTitle}"



    LetterQuizStationScreen(

        stationId = gameplayStationId,

        chapterId = gameplayChapterId,

        chapterTitle = title,

        stageLabel = stageLabel,

        plan = plan,

        letterPoolSpec = letterPoolSpec,

        backgroundRes = R.drawable.forest_bg_level_overlay,

        onBack = onBack,

        onComplete = { _, _, _ ->

            onMarkCompleted()

        },

        suppressInGameDinoProgress = suppressInGameDinoProgress,

        chapter1CompanionCharacter = companionCharacter,

        chapter1PlayerAddress = playerAddress,

        season2Chapter1StationId = stationId,

        modifier = modifier,

    )

}


