package com.tal.hebrewdino.ui

import androidx.activity.compose.BackHandler
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.domain.Chapter4Config
import com.tal.hebrewdino.ui.domain.Chapter5Config
import com.tal.hebrewdino.ui.domain.Chapter6Config
import com.tal.hebrewdino.ui.domain.DevTools
import com.tal.hebrewdino.ui.screens.Chapter4IntroScreen
import com.tal.hebrewdino.ui.screens.Chapter4LettersIntroScreen
import com.tal.hebrewdino.ui.screens.Chapter4LevelScreen
import com.tal.hebrewdino.ui.screens.Chapter4MidBoostScreen
import com.tal.hebrewdino.ui.screens.Chapter4OutroScreen
import com.tal.hebrewdino.ui.screens.Chapter5IntroScreen
import com.tal.hebrewdino.ui.screens.Chapter5LettersIntroScreen
import com.tal.hebrewdino.ui.screens.Chapter5LevelScreen
import com.tal.hebrewdino.ui.screens.Chapter5MidBoostScreen
import com.tal.hebrewdino.ui.screens.Chapter5OutroScreen
import com.tal.hebrewdino.ui.screens.Chapter6IntroScreen
import com.tal.hebrewdino.ui.screens.Chapter6LettersIntroScreen
import com.tal.hebrewdino.ui.screens.Chapter6LevelScreen
import com.tal.hebrewdino.ui.screens.Chapter6MidBoostScreen
import com.tal.hebrewdino.ui.screens.Chapter6OutroScreen
import com.tal.hebrewdino.ui.screens.JourneyEndMarker
import com.tal.hebrewdino.ui.screens.JourneyScreen
import com.tal.hebrewdino.ui.screens.RewardScreen
import kotlinx.coroutines.launch

internal fun NavGraphBuilder.chapterFourToSixGraph(host: AppNavHostState) {
    composable(NavRoutes.Ch4MidBoost) {
        Chapter4MidBoostScreen(
            onContinue = {
                host.scope.launch { host.progress.markChapter4MidBoostSeen() }
                host.navController.navigate(NavRoutes.Ch4Journey) {
                    popUpTo(NavRoutes.Ch4MidBoost) { inclusive = true }
                }
            },
        )
    }

    composable(NavRoutes.Ch4Intro) {
        Chapter4IntroScreen(
            onContinue = {
                host.scope.launch { host.progress.markChapter4IntroSeen() }
                host.navController.navigate(NavRoutes.Ch4Letters) {
                    popUpTo(NavRoutes.Ch4Intro) { inclusive = true }
                }
            },
        )
    }

    composable(NavRoutes.Ch4Letters) {
        Chapter4LettersIntroScreen(
            onContinue = {
                host.scope.launch { host.progress.markChapter4LettersIntroSeen() }
                host.navController.navigate(NavRoutes.Ch4Journey) {
                    popUpTo(NavRoutes.Ch4Letters) { inclusive = true }
                }
            },
        )
    }

    composable(NavRoutes.Ch4Journey) {
        JourneyScreen(
            unlockedLevel = host.chapter4UnlockedStation,
            completedLevels = host.chapter4CompletedStations,
            useSelectedCompanionOnMap = true,
            companionCharacter = host.companionCharacter,
            endMarkerReached = host.chapter4Completed || host.chapter4AllStationsComplete,
            totalLevels = Chapter4Config.STATION_COUNT,
            headerTitle = "פרק 4 - סיבוך בדרך",
            collectedEggStripCount = host.collectedEggStripCount,
            endMarker = JourneyEndMarker.ClueLetterPe,
            backgroundRes = R.drawable.forest_bg_journey_road,
            onPlayLevel = { stationId ->
                host.navController.navigate("${NavRoutes.Ch4Level}/$stationId")
            },
            onBack = { host.navController.navigate(NavRoutes.Chapters) { popUpTo(NavRoutes.Ch4Journey) { inclusive = true } } },
            onLettersHelp = {
                host.navController.navigate(NavRoutes.Ch4Letters) { launchSingleTop = true }
            },
            onDebugUnlockNext =
                if (DevTools.enabled(host.context)) {
                    {
                        host.scope.launch {
                            val completedStationId = host.progress.debugUnlockNextChapter4Station()
                            when {
                                completedStationId == 3 && !host.chapter4MidBoostSeen -> {
                                    host.navController.navigate(NavRoutes.Ch4MidBoost) {
                                        popUpTo(NavRoutes.Ch4Journey) { inclusive = false }
                                    }
                                }
                                completedStationId >= Chapter4Config.STATION_COUNT -> {
                                    host.navController.navigate(NavRoutes.Ch4Outro) {
                                        popUpTo(NavRoutes.Ch4Journey) { inclusive = true }
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

    composable(
        route = "${NavRoutes.Ch4Level}/{stationId}",
        arguments = listOf(navArgument("stationId") { type = NavType.IntType }),
    ) { backStackEntry ->
        val stationId = backStackEntry.arguments?.getInt("stationId") ?: 1
        Chapter4LevelScreen(
            stationId = stationId,
            onBack = { host.navController.popBackStack() },
            suppressInGameDinoProgress = host.chapter4CompletedStations.contains(stationId),
            playerAddress = host.playerAddress,
            onComplete = { completedStationId, correctCount, mistakeCount ->
                host.scope.launch {
                    host.progress.markChapter4CompletedStation(completedStationId)
                    host.progress.unlockChapter4AtLeast(completedStationId + 1)
                    if (completedStationId >= Chapter4Config.STATION_COUNT) {
                        host.progress.markChapter4Completed()
                    }
                }
                host.navController.navigate("${NavRoutes.Ch4Reward}/$completedStationId/$correctCount/$mistakeCount")
            },
        )
    }

    composable(
        route = "${NavRoutes.Ch4Reward}/{stationId}/{correct}/{mistakes}",
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
            if (stationId == 3 && !host.chapter4MidBoostSeen) {
                host.navController.navigate(NavRoutes.Ch4MidBoost) {
                    popUpTo("${NavRoutes.Ch4Reward}/$stationId/$correct/$mistakes") { inclusive = true }
                }
                return@backToMap
            }
            if (stationId >= Chapter4Config.STATION_COUNT) {
                host.navController.navigate(NavRoutes.Ch4Outro) {
                    popUpTo(NavRoutes.Ch4Journey) { inclusive = true }
                }
            } else {
                host.navController.navigate(NavRoutes.Ch4Journey) {
                    popUpTo(NavRoutes.Ch4Journey) { inclusive = true }
                }
            }
        }
        BackHandler { backToMap() }
        RewardScreen(
            levelId = stationId,
            correct = correct,
            mistakes = mistakes,
            onBackToMap = backToMap,
            showSelectedCompanionPortrait = true,
            selectedCompanionCharacter = host.companionCharacter,
        )
    }

    composable(NavRoutes.Ch4Outro) {
        Chapter4OutroScreen(
            onContinue = {
                host.navController.navigate(NavRoutes.Chapters) {
                    popUpTo(NavRoutes.Ch4Outro) { inclusive = true }
                }
            },
        )
    }

    composable(NavRoutes.Ch5MidBoost) {
        Chapter5MidBoostScreen(
            onContinue = {
                host.scope.launch { host.progress.markChapter5MidBoostSeen() }
                host.navController.navigate(NavRoutes.Ch5Journey) {
                    popUpTo(NavRoutes.Ch5MidBoost) { inclusive = true }
                }
            },
        )
    }

    composable(NavRoutes.Ch5Intro) {
        Chapter5IntroScreen(
            onContinue = {
                host.scope.launch { host.progress.markChapter5IntroSeen() }
                host.navController.navigate(NavRoutes.Ch5Letters) {
                    popUpTo(NavRoutes.Ch5Intro) { inclusive = true }
                }
            },
        )
    }

    composable(NavRoutes.Ch5Letters) {
        Chapter5LettersIntroScreen(
            onContinue = {
                host.scope.launch { host.progress.markChapter5LettersIntroSeen() }
                host.navController.navigate(NavRoutes.Ch5Journey) {
                    popUpTo(NavRoutes.Ch5Letters) { inclusive = true }
                }
            },
        )
    }

    composable(NavRoutes.Ch5Journey) {
        JourneyScreen(
            unlockedLevel = host.chapter5UnlockedStation,
            completedLevels = host.chapter5CompletedStations,
            useSelectedCompanionOnMap = true,
            companionCharacter = host.companionCharacter,
            endMarkerReached = host.chapter5Completed || host.chapter5AllStationsComplete,
            totalLevels = Chapter5Config.STATION_COUNT,
            headerTitle = "פרק 5 - הביצה השלישית",
            collectedEggStripCount = host.collectedEggStripCount,
            // Third egg on the map should match the purple egg asset.
            endMarker = JourneyEndMarker.PurpleEgg,
            backgroundRes = R.drawable.forest_bg_journey_road,
            onPlayLevel = { stationId ->
                host.navController.navigate("${NavRoutes.Ch5Level}/$stationId")
            },
            onBack = { host.navController.navigate(NavRoutes.Chapters) { popUpTo(NavRoutes.Ch5Journey) { inclusive = true } } },
            onLettersHelp = {
                host.navController.navigate(NavRoutes.Ch5Letters) { launchSingleTop = true }
            },
            onDebugUnlockNext =
                if (DevTools.enabled(host.context)) {
                    {
                        host.scope.launch {
                            val completedStationId = host.progress.debugUnlockNextChapter5Station()
                            when {
                                completedStationId == 3 && !host.chapter5MidBoostSeen -> {
                                    host.navController.navigate(NavRoutes.Ch5MidBoost) {
                                        popUpTo(NavRoutes.Ch5Journey) { inclusive = false }
                                    }
                                }
                                completedStationId >= Chapter5Config.STATION_COUNT -> {
                                    host.navController.navigate(NavRoutes.Ch5Outro) {
                                        popUpTo(NavRoutes.Ch5Journey) { inclusive = true }
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

    composable(
        route = "${NavRoutes.Ch5Level}/{stationId}",
        arguments = listOf(navArgument("stationId") { type = NavType.IntType }),
    ) { backStackEntry ->
        val stationId = backStackEntry.arguments?.getInt("stationId") ?: 1
        Chapter5LevelScreen(
            stationId = stationId,
            onBack = { host.navController.popBackStack() },
            suppressInGameDinoProgress = host.chapter5CompletedStations.contains(stationId),
            playerAddress = host.playerAddress,
            onComplete = { completedStationId, correctCount, mistakeCount ->
                host.scope.launch {
                    host.progress.markChapter5CompletedStation(completedStationId)
                    host.progress.unlockChapter5AtLeast(completedStationId + 1)
                    if (completedStationId >= Chapter5Config.STATION_COUNT) {
                        host.progress.markChapter5Completed()
                    }
                }
                host.navController.navigate("${NavRoutes.Ch5Reward}/$completedStationId/$correctCount/$mistakeCount")
            },
        )
    }

    composable(
        route = "${NavRoutes.Ch5Reward}/{stationId}/{correct}/{mistakes}",
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
            if (stationId == 3 && !host.chapter5MidBoostSeen) {
                host.navController.navigate(NavRoutes.Ch5MidBoost) {
                    popUpTo("${NavRoutes.Ch5Reward}/$stationId/$correct/$mistakes") { inclusive = true }
                }
                return@backToMap
            }
            if (stationId >= Chapter5Config.STATION_COUNT) {
                host.navController.navigate(NavRoutes.Ch5Outro) {
                    popUpTo(NavRoutes.Ch5Journey) { inclusive = true }
                }
            } else {
                host.navController.navigate(NavRoutes.Ch5Journey) {
                    popUpTo(NavRoutes.Ch5Journey) { inclusive = true }
                }
            }
        }
        BackHandler { backToMap() }
        RewardScreen(
            levelId = stationId,
            correct = correct,
            mistakes = mistakes,
            onBackToMap = backToMap,
            showSelectedCompanionPortrait = true,
            selectedCompanionCharacter = host.companionCharacter,
        )
    }

    composable(NavRoutes.Ch5Outro) {
        Chapter5OutroScreen(
            onContinue = {
                host.navController.navigate(NavRoutes.Chapters) {
                    popUpTo(NavRoutes.Ch5Outro) { inclusive = true }
                }
            },
        )
    }

    composable(NavRoutes.Ch6MidBoost) {
        Chapter6MidBoostScreen(
            onContinue = {
                host.scope.launch { host.progress.markChapter6MidBoostSeen() }
                host.navController.navigate(NavRoutes.Ch6Journey) {
                    popUpTo(NavRoutes.Ch6MidBoost) { inclusive = true }
                }
            },
        )
    }

    composable(NavRoutes.Ch6Intro) {
        Chapter6IntroScreen(
            onContinue = {
                host.scope.launch { host.progress.markChapter6IntroSeen() }
                host.navController.navigate(NavRoutes.Ch6Letters) {
                    popUpTo(NavRoutes.Ch6Intro) { inclusive = true }
                }
            },
        )
    }

    composable(NavRoutes.Ch6Letters) {
        Chapter6LettersIntroScreen(
            onContinue = {
                host.scope.launch { host.progress.markChapter6LettersIntroSeen() }
                host.navController.navigate(NavRoutes.Ch6Journey) {
                    popUpTo(NavRoutes.Ch6Letters) { inclusive = true }
                }
            },
        )
    }

    composable(NavRoutes.Ch6Journey) {
        JourneyScreen(
            unlockedLevel = host.chapter6UnlockedStation,
            completedLevels = host.chapter6CompletedStations,
            useSelectedCompanionOnMap = true,
            companionCharacter = host.companionCharacter,
            endMarkerReached = host.chapter6Completed || host.chapter6AllStationsComplete,
            totalLevels = Chapter6Config.STATION_COUNT,
            headerTitle = "פרק 6 - חוזרים הביתה",
            collectedEggStripCount = host.collectedEggStripCount,
            endMarker = JourneyEndMarker.Mom,
            backgroundRes = R.drawable.forest_bg_journey_road,
            onPlayLevel = { stationId ->
                host.navController.navigate("${NavRoutes.Ch6Level}/$stationId")
            },
            onBack = { host.navController.navigate(NavRoutes.Chapters) { popUpTo(NavRoutes.Ch6Journey) { inclusive = true } } },
            onLettersHelp = { host.navController.navigate(NavRoutes.Ch6Letters) { launchSingleTop = true } },
            onDebugUnlockNext =
                if (DevTools.enabled(host.context)) {
                    {
                        host.scope.launch {
                            val completedStationId = host.progress.debugUnlockNextChapter6Station()
                            when {
                                completedStationId == 3 && !host.chapter6MidBoostSeen -> {
                                    host.navController.navigate(NavRoutes.Ch6MidBoost) {
                                        popUpTo(NavRoutes.Ch6Journey) { inclusive = false }
                                    }
                                }
                                completedStationId >= Chapter6Config.STATION_COUNT -> {
                                    host.navController.navigate(NavRoutes.Ch6Outro) {
                                        popUpTo(NavRoutes.Ch6Journey) { inclusive = true }
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

    composable(
        route = "${NavRoutes.Ch6Level}/{stationId}",
        arguments = listOf(navArgument("stationId") { type = NavType.IntType }),
    ) { backStackEntry ->
        val stationId = backStackEntry.arguments?.getInt("stationId") ?: 1
        Chapter6LevelScreen(
            stationId = stationId,
            onBack = { host.navController.popBackStack() },
            suppressInGameDinoProgress = host.chapter6CompletedStations.contains(stationId),
            onComplete = { completedStationId, correctCount, mistakeCount ->
                host.scope.launch {
                    host.progress.markChapter6CompletedStation(completedStationId)
                    host.progress.unlockChapter6AtLeast(completedStationId + 1)
                    if (completedStationId >= Chapter6Config.STATION_COUNT) {
                        host.progress.markChapter6Completed()
                    }
                }
                host.navController.navigate("${NavRoutes.Ch6Reward}/$completedStationId/$correctCount/$mistakeCount")
            },
        )
    }

    composable(
        route = "${NavRoutes.Ch6Reward}/{stationId}/{correct}/{mistakes}",
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
            if (stationId == 3 && !host.chapter6MidBoostSeen) {
                host.navController.navigate(NavRoutes.Ch6MidBoost) {
                    popUpTo("${NavRoutes.Ch6Reward}/$stationId/$correct/$mistakes") { inclusive = true }
                }
                return@backToMap
            }
            if (stationId >= Chapter6Config.STATION_COUNT) {
                host.navController.navigate(NavRoutes.Ch6Outro) {
                    popUpTo(NavRoutes.Ch6Journey) { inclusive = true }
                }
            } else {
                host.navController.navigate(NavRoutes.Ch6Journey) {
                    popUpTo(NavRoutes.Ch6Journey) { inclusive = true }
                }
            }
        }
        BackHandler { backToMap() }
        RewardScreen(
            levelId = stationId,
            correct = correct,
            mistakes = mistakes,
            onBackToMap = backToMap,
            showSelectedCompanionPortrait = true,
            selectedCompanionCharacter = host.companionCharacter,
        )
    }

    composable(NavRoutes.Ch6Outro) {
        Chapter6OutroScreen(
            onContinue = {
                host.navController.navigate(NavRoutes.Chapters) {
                    popUpTo(NavRoutes.Ch6Outro) { inclusive = true }
                }
            },
        )
    }
}
