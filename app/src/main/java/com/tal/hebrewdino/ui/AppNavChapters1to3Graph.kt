package com.tal.hebrewdino.ui

import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.domain.Chapter1Config
import com.tal.hebrewdino.ui.domain.Chapter2Config
import com.tal.hebrewdino.ui.domain.Chapter3Config
import com.tal.hebrewdino.ui.domain.DevTools
import com.tal.hebrewdino.ui.screens.Chapter1DinoCompanionEggOutroScreen
import com.tal.hebrewdino.ui.screens.Chapter1DinoCompanionIntroScreen
import com.tal.hebrewdino.ui.screens.Chapter1LettersIntroScreen
import com.tal.hebrewdino.ui.screens.Chapter1MidBoostScreen
import com.tal.hebrewdino.ui.screens.Chapter2IntroScreen
import com.tal.hebrewdino.ui.screens.Chapter2LettersIntroScreen
import com.tal.hebrewdino.ui.screens.Chapter2LevelScreen
import com.tal.hebrewdino.ui.screens.Chapter2MidBoostScreen
import com.tal.hebrewdino.ui.screens.Chapter2OutroScreen
import com.tal.hebrewdino.ui.screens.Chapter3IntroScreen
import com.tal.hebrewdino.ui.screens.Chapter3LettersIntroScreen
import com.tal.hebrewdino.ui.screens.Chapter3LevelScreen
import com.tal.hebrewdino.ui.screens.Chapter3MidBoostScreen
import com.tal.hebrewdino.ui.screens.Chapter3OutroScreen
import com.tal.hebrewdino.ui.screens.ForestIntroScreen
import com.tal.hebrewdino.ui.screens.JourneyEndMarker
import com.tal.hebrewdino.ui.screens.JourneyScreen
import com.tal.hebrewdino.ui.screens.LevelScreen
import com.tal.hebrewdino.ui.screens.RewardScreen
import kotlinx.coroutines.launch

internal fun NavGraphBuilder.chapterOneToThreeGraph(host: AppNavHostState) {
    composable(NavRoutes.Ch1DinoCompanionIntro) {
        Chapter1DinoCompanionIntroScreen(
            companionCharacter = host.companionCharacter,
            playerAddress = host.playerAddress,
            onContinue = {
                host.navController.navigate(NavRoutes.ChapterLettersIntro) {
                    popUpTo(NavRoutes.Ch1DinoCompanionIntro) { inclusive = true }
                }
            },
        )
    }

    composable(NavRoutes.StoryIntro) {
        ForestIntroScreen(
            character = host.companionCharacter,
            onContinue = {
                host.scope.launch { host.progress.markBeachIntroSeen() }
                host.navController.navigate(NavRoutes.Ch1DinoCompanionIntro) {
                    popUpTo(NavRoutes.StoryIntro) { inclusive = true }
                }
            },
        )
    }

    composable(NavRoutes.ChapterLettersIntro) {
        Chapter1LettersIntroScreen(
            onContinue = {
                host.scope.launch { host.progress.markChapter1LettersIntroSeen() }
                host.navController.navigate(NavRoutes.Journey) {
                    popUpTo(NavRoutes.ChapterLettersIntro) { inclusive = true }
                }
            },
        )
    }

    composable(NavRoutes.Ch2Intro) {
        Chapter2IntroScreen(
            onContinue = {
                host.scope.launch { host.progress.markChapter2IntroSeen() }
                // After intro, always show letters presentation, then continue to journey.
                host.navController.navigate(NavRoutes.Ch2Letters) {
                    popUpTo(NavRoutes.Ch2Intro) { inclusive = true }
                }
            },
        )
    }

    composable(NavRoutes.Ch2Letters) {
        Chapter2LettersIntroScreen(
            onContinue = {
                host.scope.launch { host.progress.markChapter2LettersIntroSeen() }
                host.navController.navigate(NavRoutes.Ch2Journey) {
                    popUpTo(NavRoutes.Ch2Letters) { inclusive = true }
                }
            },
        )
    }

    composable(NavRoutes.Ch2Journey) {
        JourneyScreen(
            unlockedLevel = host.chapter2UnlockedStation,
            completedLevels = host.chapter2CompletedStations,
            // After finishing all stations, Dino should stand by the tracks end marker and stay there.
            endMarkerReached = host.chapter2Completed || host.chapter2AllStationsComplete,
            totalLevels = Chapter2Config.STATION_COUNT,
            headerTitle = "פרק 2 - מוצאים עקבות לביצה הורודה",
            collectedEggStripCount = host.collectedEggStripCount,
            endMarker = JourneyEndMarker.Tracks,
            backgroundRes = R.drawable.chapter2_journey_road,
            onPlayLevel = { stationId ->
                host.navController.navigate("${NavRoutes.Ch2Level}/$stationId")
            },
            onBack = { host.navController.navigate(NavRoutes.Chapters) { popUpTo(NavRoutes.Ch2Journey) { inclusive = true } } },
            onLettersHelp = {
                host.navController.navigate(NavRoutes.Ch2Letters) { launchSingleTop = true }
            },
            onDebugUnlockNext =
                if (DevTools.enabled(host.context)) {
                    {
                        host.scope.launch {
                            val completedStationId = host.progress.debugUnlockNextChapter2Station()
                            when {
                                completedStationId == 3 && !host.chapter2MidBoostSeen -> {
                                    host.navController.navigate(NavRoutes.Ch2MidBoost) {
                                        popUpTo(NavRoutes.Ch2Journey) { inclusive = false }
                                    }
                                }
                                completedStationId >= Chapter2Config.STATION_COUNT -> {
                                    host.navController.navigate(NavRoutes.Ch2StoryOutro) {
                                        popUpTo(NavRoutes.Ch2Journey) { inclusive = true }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    null
                },
        )
    }

    composable(NavRoutes.Ch3Intro) {
        Chapter3IntroScreen(
            onContinue = {
                host.scope.launch { host.progress.markChapter3IntroSeen() }
                // After intro, always show letters presentation, then continue to journey.
                host.navController.navigate(NavRoutes.Ch3Letters) {
                    popUpTo(NavRoutes.Ch3Intro) { inclusive = true }
                }
            },
        )
    }

    composable(NavRoutes.Ch3Letters) {
        Chapter3LettersIntroScreen(
            onContinue = {
                host.scope.launch { host.progress.markChapter3LettersIntroSeen() }
                host.navController.navigate(NavRoutes.Ch3Journey) {
                    popUpTo(NavRoutes.Ch3Letters) { inclusive = true }
                }
            },
        )
    }

    composable(NavRoutes.Ch3Journey) {
        JourneyScreen(
            unlockedLevel = host.chapter3UnlockedStation,
            completedLevels = host.chapter3CompletedStations,
            endMarkerReached = host.chapter3Completed || host.chapter3AllStationsComplete,
            totalLevels = Chapter3Config.STATION_COUNT,
            headerTitle = "פרק 3 - מצא את הביצה הורודה",
            collectedEggStripCount = host.collectedEggStripCount,
            endMarker = JourneyEndMarker.PinkEgg,
            backgroundRes = R.drawable.ch3_journey_bg,
            onPlayLevel = { stationId ->
                host.navController.navigate("${NavRoutes.Ch3Level}/$stationId")
            },
            onBack = { host.navController.navigate(NavRoutes.Chapters) { popUpTo(NavRoutes.Ch3Journey) { inclusive = true } } },
            onLettersHelp = {
                host.navController.navigate(NavRoutes.Ch3Letters) { launchSingleTop = true }
            },
            onDebugUnlockNext =
                if (DevTools.enabled(host.context)) {
                    {
                        host.scope.launch {
                            val completedStationId = host.progress.debugUnlockNextChapter3Station()
                            when {
                                completedStationId == 3 && !host.chapter3MidBoostSeen -> {
                                    host.navController.navigate(NavRoutes.Ch3MidBoost) {
                                        popUpTo(NavRoutes.Ch3Journey) { inclusive = false }
                                    }
                                }
                                completedStationId >= Chapter3Config.STATION_COUNT -> {
                                    host.navController.navigate(NavRoutes.Ch3StoryOutro) {
                                        popUpTo(NavRoutes.Ch3Journey) { inclusive = true }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    null
                },
        )
    }

    composable(NavRoutes.Journey) {
        JourneyScreen(
            unlockedLevel = host.unlockedLevel,
            completedLevels = host.completedLevels,
            useCompanionDinoOnMap = true,
            companionCharacter = host.companionCharacter,
            collectedEggStripCount = host.collectedEggStripCount,
            // Stand at the egg once all stations are done, not only after the beach outro (JourneyEndWalk still plays first-time finale).
            endMarkerReached = host.beachOutroSeen || host.chapter1AllStationsComplete,
            onPlayLevel = { levelId ->
                host.navController.navigate("${NavRoutes.Level}/$levelId")
            },
            onBack = {
                host.navController.navigate(NavRoutes.Chapters) { popUpTo(NavRoutes.Journey) { inclusive = true } }
            },
            onDebugUnlockNext =
                if (DevTools.enabled(host.context)) {
                    {
                        host.scope.launch {
                            val completedStationId = host.progress.debugUnlockNextChapter1Station()
                            when {
                                completedStationId == 3 && !host.chapter1MidBoostSeen -> {
                                    host.navController.navigate(NavRoutes.Ch1MidBoost) {
                                        popUpTo(NavRoutes.Journey) { inclusive = false }
                                    }
                                }
                                completedStationId >= Chapter1Config.STATION_COUNT && !host.beachOutroSeen -> {
                                    host.navController.navigate(NavRoutes.JourneyEndWalk) {
                                        popUpTo(NavRoutes.Journey) { inclusive = true }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    null
                },
            onLettersHelp = {
                host.navController.navigate(NavRoutes.ChapterLettersIntro) { launchSingleTop = true }
            },
        )
    }

    composable(NavRoutes.JourneyEndWalk) {
        JourneyScreen(
            unlockedLevel = host.unlockedLevel,
            completedLevels = host.completedLevels,
            useCompanionDinoOnMap = true,
            companionCharacter = host.companionCharacter,
            collectedEggStripCount = host.collectedEggStripCount,
            endMarkerReached = false,
            endWalkThenContinue = true,
            onEndWalkComplete = {
                // After Dino reaches the egg, show the finale story screen (then it returns to Chapters).
                host.navController.navigate(NavRoutes.StoryOutro) {
                    popUpTo(NavRoutes.JourneyEndWalk) { inclusive = true }
                }
            },
            onPlayLevel = { levelId ->
                host.navController.navigate("${NavRoutes.Level}/$levelId")
            },
            onBack = {
                host.navController.navigate(NavRoutes.Chapters) { popUpTo(NavRoutes.JourneyEndWalk) { inclusive = true } }
            },
            onDebugUnlockNext =
                if (DevTools.enabled(host.context)) {
                    { host.scope.launch { host.progress.debugUnlockNextChapter1Station() } }
                } else {
                    null
                },
            onLettersHelp = {
                host.navController.navigate(NavRoutes.ChapterLettersIntro) { launchSingleTop = true }
            },
        )
    }

    composable(
        route = "${NavRoutes.Ch2Level}/{stationId}",
        arguments = listOf(navArgument("stationId") { type = NavType.IntType }),
    ) { backStackEntry ->
        val stationId = backStackEntry.arguments?.getInt("stationId") ?: 1
        Chapter2LevelScreen(
            stationId = stationId,
            onBack = { host.navController.popBackStack() },
            suppressInGameDinoProgress = host.chapter2CompletedStations.contains(stationId),
            onComplete = { completedStationId, correctCount, mistakeCount ->
                host.scope.launch {
                    host.progress.markChapter2CompletedStation(completedStationId)
                    host.progress.unlockChapter2AtLeast(completedStationId + 1)
                    if (completedStationId >= Chapter2Config.STATION_COUNT) host.progress.markChapter2Completed()
                }
                host.navController.navigate("${NavRoutes.Ch2Reward}/$completedStationId/$correctCount/$mistakeCount")
            },
        )
    }

    composable(
        route = "${NavRoutes.Ch3Level}/{stationId}",
        arguments = listOf(navArgument("stationId") { type = NavType.IntType }),
    ) { backStackEntry ->
        val stationId = backStackEntry.arguments?.getInt("stationId") ?: 1
        Chapter3LevelScreen(
            stationId = stationId,
            onBack = { host.navController.popBackStack() },
            suppressInGameDinoProgress = host.chapter3CompletedStations.contains(stationId),
            onComplete = { completedStationId, correctCount, mistakeCount ->
                host.scope.launch {
                    host.progress.markChapter3CompletedStation(completedStationId)
                    host.progress.unlockChapter3AtLeast(completedStationId + 1)
                    if (completedStationId >= Chapter3Config.STATION_COUNT) {
                        host.progress.markChapter3Completed()
                    }
                }
                host.navController.navigate("${NavRoutes.Ch3Reward}/$completedStationId/$correctCount/$mistakeCount")
            },
        )
    }

    composable(
        route = "${NavRoutes.Level}/{levelId}",
        arguments = listOf(navArgument("levelId") { type = NavType.IntType }),
    ) { backStackEntry ->
        val levelId = backStackEntry.arguments?.getInt("levelId") ?: 1
        LevelScreen(
            levelId = levelId,
            onBack = { host.navController.popBackStack() },
            suppressInGameDinoProgress = host.completedLevels.contains(levelId),
            chapter1CompanionCharacter = host.companionCharacter,
            chapter1PlayerAddress = host.playerAddress,
            onComplete = { completedLevelId, correctCount, mistakeCount ->
                host.scope.launch {
                    host.progress.markCompleted(completedLevelId)
                    host.progress.unlockAtLeast(
                        (completedLevelId + 1).coerceAtMost(Chapter1Config.STATION_COUNT),
                    )
                }
                // Always show the reward screen; finale flow continues after reward.
                host.navController.navigate("${NavRoutes.Reward}/$completedLevelId/$correctCount/$mistakeCount")
            },
        )
    }

    composable(
        route = "${NavRoutes.Reward}/{levelId}/{correct}/{mistakes}",
        arguments = listOf(
            navArgument("levelId") { type = NavType.IntType },
            navArgument("correct") { type = NavType.IntType },
            navArgument("mistakes") { type = NavType.IntType },
        ),
    ) { backStackEntry ->
        val levelId = backStackEntry.arguments?.getInt("levelId") ?: 1
        val correct = backStackEntry.arguments?.getInt("correct") ?: 0
        val mistakes = backStackEntry.arguments?.getInt("mistakes") ?: 0
        val backToMap: () -> Unit = backToMap@{
            if (levelId == 3 && !host.chapter1MidBoostSeen) {
                host.navController.navigate(NavRoutes.Ch1MidBoost) {
                    popUpTo(NavRoutes.Reward) { inclusive = true }
                }
                return@backToMap
            }
            val isChapterEnd = levelId >= Chapter1Config.STATION_COUNT
            if (isChapterEnd && !host.beachOutroSeen) {
                // Episode 1 finale: after reward, return to journey to watch Dino walk to the egg,
                // then JourneyEndWalk will show the outro summary screen.
                host.navController.navigate(NavRoutes.JourneyEndWalk) {
                    popUpTo(NavRoutes.Journey) { inclusive = true }
                }
            } else {
                host.navController.navigate(NavRoutes.Journey) {
                    popUpTo(NavRoutes.Journey) { inclusive = true }
                }
            }
        }
        BackHandler { backToMap() }
        RewardScreen(
            levelId = levelId,
            correct = correct,
            mistakes = mistakes,
            onBackToMap = backToMap,
            chapter1DinoCompanionPilot = true,
            chapter1CompanionCharacter = host.companionCharacter,
        )
    }

    composable(NavRoutes.Ch1MidBoost) {
        Chapter1MidBoostScreen(
            companionCharacter = host.companionCharacter,
            playerAddress = host.playerAddress,
            onContinue = {
                host.scope.launch { host.progress.markChapter1MidBoostSeen() }
                host.navController.navigate(NavRoutes.Journey) {
                    popUpTo(NavRoutes.Ch1MidBoost) { inclusive = true }
                }
            },
        )
    }

    composable(
        route = "${NavRoutes.Ch2Reward}/{stationId}/{correct}/{mistakes}",
        arguments = listOf(
            navArgument("stationId") { type = NavType.IntType },
            navArgument("correct") { type = NavType.IntType },
            navArgument("mistakes") { type = NavType.IntType },
        ),
    ) { backStackEntry ->
        val stationId = backStackEntry.arguments?.getInt("stationId") ?: 1
        val correct = backStackEntry.arguments?.getInt("correct") ?: 0
        val mistakes = backStackEntry.arguments?.getInt("mistakes") ?: 0
        val backToMap: () -> Unit = backToMap@{
            if (stationId == 3 && !host.chapter2MidBoostSeen) {
                host.navController.navigate(NavRoutes.Ch2MidBoost) {
                    popUpTo("${NavRoutes.Ch2Reward}/$stationId/$correct/$mistakes") { inclusive = true }
                }
                return@backToMap
            }
            if (stationId >= Chapter2Config.STATION_COUNT) {
                host.navController.navigate(NavRoutes.Ch2StoryOutro) {
                    popUpTo(NavRoutes.Ch2Journey) { inclusive = true }
                }
            } else {
                host.navController.navigate(NavRoutes.Ch2Journey) {
                    popUpTo(NavRoutes.Ch2Journey) { inclusive = true }
                }
            }
        }
        BackHandler { backToMap() }
        RewardScreen(
            levelId = stationId,
            correct = correct,
            mistakes = mistakes,
            // Match gameplay (`Chapter2LevelScreen`) so station-complete doesn't feel like a different "camera".
            backgroundRes = R.drawable.chapter2_level_overlay,
            onBackToMap = backToMap,
        )
    }

    composable(NavRoutes.Ch2MidBoost) {
        Chapter2MidBoostScreen(
            onContinue = {
                host.scope.launch { host.progress.markChapter2MidBoostSeen() }
                host.navController.navigate(NavRoutes.Ch2Journey) {
                    popUpTo(NavRoutes.Ch2MidBoost) { inclusive = true }
                }
            },
        )
    }

    composable(NavRoutes.Ch3MidBoost) {
        Chapter3MidBoostScreen(
            onContinue = {
                host.scope.launch { host.progress.markChapter3MidBoostSeen() }
                host.navController.navigate(NavRoutes.Ch3Journey) {
                    popUpTo(NavRoutes.Ch3MidBoost) { inclusive = true }
                }
            },
        )
    }

    composable(NavRoutes.Ch2StoryOutro) {
        Chapter2OutroScreen(
            onContinue = {
                // Safety: ensure chapter-2 completion is persisted even if the last-station completion
                // path was bypassed (e.g. debug flows / navigation edge cases).
                host.scope.launch { host.progress.markChapter2Completed() }
                host.navController.navigate(NavRoutes.Chapters) {
                    popUpTo(NavRoutes.Ch2StoryOutro) { inclusive = true }
                }
            },
        )
    }

    composable(
        route = "${NavRoutes.Ch3Reward}/{stationId}/{correct}/{mistakes}",
        arguments = listOf(
            navArgument("stationId") { type = NavType.IntType },
            navArgument("correct") { type = NavType.IntType },
            navArgument("mistakes") { type = NavType.IntType },
        ),
    ) { backStackEntry ->
        val stationId = backStackEntry.arguments?.getInt("stationId") ?: 1
        val correct = backStackEntry.arguments?.getInt("correct") ?: 0
        val mistakes = backStackEntry.arguments?.getInt("mistakes") ?: 0
        val backToMap: () -> Unit = backToMap@{
            if (stationId == 3 && !host.chapter3MidBoostSeen) {
                host.navController.navigate(NavRoutes.Ch3MidBoost) {
                    popUpTo("${NavRoutes.Ch3Reward}/$stationId/$correct/$mistakes") { inclusive = true }
                }
                return@backToMap
            }
            if (stationId >= Chapter3Config.STATION_COUNT) {
                host.navController.navigate(NavRoutes.Ch3StoryOutro) {
                    popUpTo(NavRoutes.Ch3Journey) { inclusive = true }
                }
            } else {
                host.navController.navigate(NavRoutes.Ch3Journey) {
                    popUpTo(NavRoutes.Ch3Journey) { inclusive = true }
                }
            }
        }
        BackHandler { backToMap() }
        RewardScreen(
            levelId = stationId,
            correct = correct,
            mistakes = mistakes,
            backgroundRes = R.drawable.ch3_reward_bg,
            onBackToMap = backToMap,
        )
    }

    composable(NavRoutes.Ch3StoryOutro) {
        Chapter3OutroScreen(
            onContinue = {
                host.navController.navigate(NavRoutes.Chapters) {
                    popUpTo(NavRoutes.Ch3StoryOutro) { inclusive = true }
                }
            },
        )
    }

    composable(NavRoutes.StoryOutro) {
        Chapter1DinoCompanionEggOutroScreen(
            companionCharacter = host.companionCharacter,
            playerAddress = host.playerAddress,
            onContinue = {
                host.scope.launch { host.progress.markBeachOutroSeen() }
                host.navController.navigate(NavRoutes.Chapters) {
                    popUpTo(NavRoutes.StoryOutro) { inclusive = true }
                }
            },
        )
    }
}
