package com.tal.hebrewdino.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tal.hebrewdino.ui.domain.Chapter1Config
import com.tal.hebrewdino.ui.domain.CollectedEggs
import com.tal.hebrewdino.ui.domain.Chapter2Config
import com.tal.hebrewdino.ui.domain.Chapter3Config
import com.tal.hebrewdino.ui.domain.Chapter4Config
import com.tal.hebrewdino.ui.domain.Chapter5Config
import com.tal.hebrewdino.ui.domain.Chapter6Config
import com.tal.hebrewdino.ui.data.CharacterPrefs
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.data.ProgressPrefs
import com.tal.hebrewdino.ui.screens.Chapter1LettersIntroScreen
import com.tal.hebrewdino.ui.screens.Chapter2LettersIntroScreen
import com.tal.hebrewdino.ui.screens.Chapter3IntroScreen
import com.tal.hebrewdino.ui.screens.Chapter3LettersIntroScreen
import com.tal.hebrewdino.ui.screens.Chapter3LevelScreen
import com.tal.hebrewdino.ui.screens.Chapter4LettersIntroScreen
import com.tal.hebrewdino.ui.screens.Chapter4LevelScreen
import com.tal.hebrewdino.ui.screens.ChaptersProgress
import com.tal.hebrewdino.ui.screens.ChaptersScreen
import com.tal.hebrewdino.ui.screens.Chapter2IntroScreen
import com.tal.hebrewdino.ui.screens.Chapter2OutroScreen
import com.tal.hebrewdino.ui.screens.Chapter3OutroScreen
import com.tal.hebrewdino.ui.screens.Chapter4IntroScreen
import com.tal.hebrewdino.ui.screens.Chapter4OutroScreen
import com.tal.hebrewdino.ui.screens.Chapter1MidBoostScreen
import com.tal.hebrewdino.ui.screens.Chapter2MidBoostScreen
import com.tal.hebrewdino.ui.screens.Chapter3MidBoostScreen
import com.tal.hebrewdino.ui.screens.Chapter4MidBoostScreen
import com.tal.hebrewdino.ui.screens.Chapter5IntroScreen
import com.tal.hebrewdino.ui.screens.Chapter5LettersIntroScreen
import com.tal.hebrewdino.ui.screens.Chapter5LevelScreen
import com.tal.hebrewdino.ui.screens.Chapter5MidBoostScreen
import com.tal.hebrewdino.ui.screens.Chapter5OutroScreen
import com.tal.hebrewdino.ui.screens.Chapter6IntroScreen
import com.tal.hebrewdino.ui.screens.Chapter6LettersIntroScreen
import com.tal.hebrewdino.ui.screens.Chapter6LevelScreen
import com.tal.hebrewdino.ui.screens.Chapter6OutroScreen
import com.tal.hebrewdino.ui.screens.ForestIntroScreen
import com.tal.hebrewdino.ui.screens.ForestOutroScreen
import com.tal.hebrewdino.ui.screens.JourneyEndMarker
import com.tal.hebrewdino.ui.screens.JourneyScreen
import com.tal.hebrewdino.ui.screens.LevelScreen
import com.tal.hebrewdino.ui.screens.RewardScreen
import com.tal.hebrewdino.ui.screens.SettingsScreen
import com.tal.hebrewdino.ui.screens.Chapter2LevelScreen
import com.tal.hebrewdino.R
import android.content.pm.ApplicationInfo
import kotlinx.coroutines.launch

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    val prefs = CharacterPrefs(context)
    val progress = ProgressPrefs(context)
    val scope = rememberCoroutineScope()
    val beachOutroSeen by progress.beachOutroSeenFlow.collectAsState(initial = false)
    val chapter1MidBoostSeen by progress.chapter1MidBoostSeenFlow.collectAsState(initial = false)
    val chapter2MidBoostSeen by progress.chapter2MidBoostSeenFlow.collectAsState(initial = false)
    val chapter2UnlockedStation by progress.chapter2UnlockedStationFlow.collectAsState(initial = 1)
    val chapter2CompletedStations by progress.chapter2CompletedStationsFlow.collectAsState(initial = emptySet())
    val chapter2Completed by progress.chapter2CompletedFlow.collectAsState(initial = false)
    val chapter3MidBoostSeen by progress.chapter3MidBoostSeenFlow.collectAsState(initial = false)
    val chapter3UnlockedStation by progress.chapter3UnlockedStationFlow.collectAsState(initial = 1)
    val chapter3CompletedStations by progress.chapter3CompletedStationsFlow.collectAsState(initial = emptySet())
    val chapter3Completed by progress.chapter3CompletedFlow.collectAsState(initial = false)
    val chapter4IntroSeen by progress.chapter4IntroSeenFlow.collectAsState(initial = false)
    val chapter4LettersIntroSeen by progress.chapter4LettersIntroSeenFlow.collectAsState(initial = false)
    val chapter4MidBoostSeen by progress.chapter4MidBoostSeenFlow.collectAsState(initial = false)
    val chapter4UnlockedStation by progress.chapter4UnlockedStationFlow.collectAsState(initial = 1)
    val chapter4CompletedStations by progress.chapter4CompletedStationsFlow.collectAsState(initial = emptySet())
    val chapter4Completed by progress.chapter4CompletedFlow.collectAsState(initial = false)
    val chapter5IntroSeen by progress.chapter5IntroSeenFlow.collectAsState(initial = false)
    val chapter5LettersIntroSeen by progress.chapter5LettersIntroSeenFlow.collectAsState(initial = false)
    val chapter5MidBoostSeen by progress.chapter5MidBoostSeenFlow.collectAsState(initial = false)
    val chapter5UnlockedStation by progress.chapter5UnlockedStationFlow.collectAsState(initial = 1)
    val chapter5CompletedStations by progress.chapter5CompletedStationsFlow.collectAsState(initial = emptySet())
    val chapter5Completed by progress.chapter5CompletedFlow.collectAsState(initial = false)
    val chapter6IntroSeen by progress.chapter6IntroSeenFlow.collectAsState(initial = false)
    val chapter6LettersIntroSeen by progress.chapter6LettersIntroSeenFlow.collectAsState(initial = false)
    val chapter6UnlockedStation by progress.chapter6UnlockedStationFlow.collectAsState(initial = 1)
    val chapter6CompletedStations by progress.chapter6CompletedStationsFlow.collectAsState(initial = emptySet())
    val chapter6Completed by progress.chapter6CompletedFlow.collectAsState(initial = false)
    val unlockedLevel by progress.unlockedLevelFlow.collectAsState(initial = 1)
    val completedLevels by progress.completedLevelsFlow.collectAsState(initial = emptySet())
    val chapter1AllStationsComplete =
        (1..Chapter1Config.STATION_COUNT).all { station -> completedLevels.contains(station) }
    val chapter2AllStationsComplete =
        (1..Chapter2Config.STATION_COUNT).all { station -> chapter2CompletedStations.contains(station) }
    val chapter3AllStationsComplete =
        (1..Chapter3Config.STATION_COUNT).all { station -> chapter3CompletedStations.contains(station) }
    val chapter4AllStationsComplete =
        (1..Chapter4Config.STATION_COUNT).all { station -> chapter4CompletedStations.contains(station) }
    val chapter5AllStationsComplete =
        (1..Chapter5Config.STATION_COUNT).all { station -> chapter5CompletedStations.contains(station) }
    val chapter6AllStationsComplete =
        (1..Chapter6Config.STATION_COUNT).all { station -> chapter6CompletedStations.contains(station) }
    /** First-egg / chapter-1 “done” for strip: outro seen, or all six stations finished (even before outro). */
    val chapter1ProgressForStrip = beachOutroSeen || chapter1AllStationsComplete
    val collectedEggStripCount =
        CollectedEggs.stripCount(
            beachOutroSeen = chapter1ProgressForStrip,
            chapter3Completed = chapter3Completed,
            chapter5Completed = chapter5Completed,
        )

    val unlockedChapter =
        when {
            chapter6Completed -> 6
            chapter5Completed -> 6
            chapter4Completed -> 5
            chapter3Completed -> 4
            chapter2Completed -> 3
            beachOutroSeen || chapter1AllStationsComplete -> 2
            else -> 1
        }
    /** Chapter 4 story unlocks after chapter 3 is finished; “בקרוב” shows until then. */
    val chapter4ComingSoon = !chapter3Completed
    /** Chapter 5 unlocks after chapter 4 is finished. */
    val chapter5ComingSoon = !chapter4Completed
    /** Chapter 6 unlocks after chapter 5 is finished. */
    val chapter6ComingSoon = !chapter5Completed
    val maxSelectableChapterId =
        when {
            chapter5Completed -> 6
            chapter4Completed -> 5
            chapter3Completed -> 4
            else -> 3
        }

    val startDestination = NavRoutes.Chapters

    LaunchedEffect(Unit) {
        progress.repairChapter2ProgressIfNeeded()
        progress.repairChapter3ProgressIfNeeded()
        progress.repairChapter4ProgressIfNeeded()
        progress.repairChapter5ProgressIfNeeded()
        progress.repairChapter6ProgressIfNeeded()
        prefs.setCharacter(DinoCharacter.Dino)
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(NavRoutes.Chapters) {
            ChaptersScreen(
                unlockedChapter = unlockedChapter,
                chapter4ComingSoon = chapter4ComingSoon,
                chapter5ComingSoon = chapter5ComingSoon,
                chapter6ComingSoon = chapter6ComingSoon,
                maxSelectableChapterId = maxSelectableChapterId,
                chaptersProgress =
                    ChaptersProgress(
                        chapter1Completed = chapter1ProgressForStrip,
                        chapter2Completed = chapter2Completed,
                        chapter3Completed = chapter3Completed,
                        chapter4Completed = chapter4Completed,
                        chapter5Completed = chapter5Completed,
                        chapter6Completed = chapter6Completed,
                    ),
                onOpenSettings = { navController.navigate(NavRoutes.Settings) },
                onOpenChapter = { chapterId ->
                    when (chapterId) {
                        1 -> {
                            // Always show chapter intro on entry (user request).
                            navController.navigate(NavRoutes.StoryIntro)
                        }
                        2 -> {
                            val canEnterChapter2 = beachOutroSeen || chapter1AllStationsComplete
                            if (canEnterChapter2) {
                                // Always show chapter intro on entry (user request).
                                navController.navigate(NavRoutes.Ch2Intro)
                            }
                        }
                        3 -> {
                            val next =
                                when {
                                    !chapter2Completed -> NavRoutes.Chapters
                                    else -> NavRoutes.Ch3Intro
                                }
                            if (next != NavRoutes.Chapters) navController.navigate(next)
                        }
                        4 -> {
                            if (chapter3Completed) {
                                // Always show chapter intro on entry, then letters (same behavior as chapters 1–3).
                                navController.navigate(NavRoutes.Ch4Intro)
                            }
                        }
                        5 -> {
                            if (chapter4Completed) {
                                // Always show chapter intro on entry, then letters (same behavior as chapters 1–3).
                                navController.navigate(NavRoutes.Ch5Intro)
                            }
                        }
                        6 -> {
                            if (chapter5Completed) {
                                navController.navigate(NavRoutes.Ch6Intro)
                            }
                        }
                        in 7..10 -> Unit
                    }
                },
            )
        }

        composable(NavRoutes.StoryIntro) {
            ForestIntroScreen(
                character = DinoCharacter.Dino,
                onContinue = {
                    scope.launch { progress.markBeachIntroSeen() }
                    // After intro, always show letters presentation, then continue to journey.
                    navController.navigate(NavRoutes.ChapterLettersIntro) {
                        popUpTo(NavRoutes.StoryIntro) { inclusive = true }
                    }
                },
            )
        }

        composable(NavRoutes.ChapterLettersIntro) {
            Chapter1LettersIntroScreen(
                onContinue = {
                    scope.launch { progress.markChapter1LettersIntroSeen() }
                    navController.navigate(NavRoutes.Journey) {
                        popUpTo(NavRoutes.ChapterLettersIntro) { inclusive = true }
                    }
                },
            )
        }

        composable(NavRoutes.Ch2Intro) {
            Chapter2IntroScreen(
                onContinue = {
                    scope.launch { progress.markChapter2IntroSeen() }
                    // After intro, always show letters presentation, then continue to journey.
                    navController.navigate(NavRoutes.Ch2Letters) {
                        popUpTo(NavRoutes.Ch2Intro) { inclusive = true }
                    }
                },
            )
        }

        composable(NavRoutes.Ch2Letters) {
            Chapter2LettersIntroScreen(
                onContinue = {
                    scope.launch { progress.markChapter2LettersIntroSeen() }
                    navController.navigate(NavRoutes.Ch2Journey) {
                        popUpTo(NavRoutes.Ch2Letters) { inclusive = true }
                    }
                },
            )
        }

        composable(NavRoutes.Ch2Journey) {
            JourneyScreen(
                unlockedLevel = chapter2UnlockedStation,
                completedLevels = chapter2CompletedStations,
                // After finishing all stations, Dino should stand by the tracks end marker and stay there.
                endMarkerReached = chapter2Completed || chapter2AllStationsComplete,
                totalLevels = Chapter2Config.STATION_COUNT,
                headerTitle = "פרק 2 - מוצאים עקבות לביצה הורודה",
                collectedEggStripCount = collectedEggStripCount,
                endMarker = JourneyEndMarker.Tracks,
                backgroundRes = R.drawable.chapter2_journey_road,
                onPlayLevel = { stationId ->
                    navController.navigate("${NavRoutes.Ch2Level}/$stationId")
                },
                onBack = { navController.navigate(NavRoutes.Chapters) { popUpTo(NavRoutes.Ch2Journey) { inclusive = true } } },
                onLettersHelp = {
                    navController.navigate(NavRoutes.Ch2Letters) { launchSingleTop = true }
                },
                onDebugUnlockNext = {
                    scope.launch {
                        val completedStationId = progress.debugUnlockNextChapter2Station()
                        when {
                            completedStationId == 3 && !chapter2MidBoostSeen -> {
                                navController.navigate(NavRoutes.Ch2MidBoost) {
                                    popUpTo(NavRoutes.Ch2Journey) { inclusive = false }
                                }
                            }
                            completedStationId >= Chapter2Config.STATION_COUNT -> {
                                navController.navigate(NavRoutes.Ch2StoryOutro) {
                                    popUpTo(NavRoutes.Ch2Journey) { inclusive = true }
                                }
                            }
                        }
                    }
                },
            )
        }

        composable(NavRoutes.Ch3Intro) {
            Chapter3IntroScreen(
                eggStripCount = collectedEggStripCount,
                onContinue = {
                    scope.launch { progress.markChapter3IntroSeen() }
                    // After intro, always show letters presentation, then continue to journey.
                    navController.navigate(NavRoutes.Ch3Letters) {
                        popUpTo(NavRoutes.Ch3Intro) { inclusive = true }
                    }
                },
            )
        }

        composable(NavRoutes.Ch3Letters) {
            Chapter3LettersIntroScreen(
                onContinue = {
                    scope.launch { progress.markChapter3LettersIntroSeen() }
                    navController.navigate(NavRoutes.Ch3Journey) {
                        popUpTo(NavRoutes.Ch3Letters) { inclusive = true }
                    }
                },
            )
        }

        composable(NavRoutes.Ch3Journey) {
            JourneyScreen(
                unlockedLevel = chapter3UnlockedStation,
                completedLevels = chapter3CompletedStations,
                endMarkerReached = chapter3Completed || chapter3AllStationsComplete,
                totalLevels = Chapter3Config.STATION_COUNT,
                headerTitle = "פרק 3 - מצא את הביצה הורודה",
                collectedEggStripCount = collectedEggStripCount,
                endMarker = JourneyEndMarker.PinkEgg,
                backgroundRes = R.drawable.ch3_journey_bg,
                onPlayLevel = { stationId ->
                    navController.navigate("${NavRoutes.Ch3Level}/$stationId")
                },
                onBack = { navController.navigate(NavRoutes.Chapters) { popUpTo(NavRoutes.Ch3Journey) { inclusive = true } } },
                onLettersHelp = {
                    navController.navigate(NavRoutes.Ch3Letters) { launchSingleTop = true }
                },
                onDebugUnlockNext = {
                    scope.launch {
                        val completedStationId = progress.debugUnlockNextChapter3Station()
                        when {
                            completedStationId == 3 && !chapter3MidBoostSeen -> {
                                navController.navigate(NavRoutes.Ch3MidBoost) {
                                    popUpTo(NavRoutes.Ch3Journey) { inclusive = false }
                                }
                            }
                            completedStationId >= Chapter3Config.STATION_COUNT -> {
                                navController.navigate(NavRoutes.Ch3StoryOutro) {
                                    popUpTo(NavRoutes.Ch3Journey) { inclusive = true }
                                }
                            }
                        }
                    }
                },
            )
        }

        composable(NavRoutes.Journey) {
            JourneyScreen(
                unlockedLevel = unlockedLevel,
                completedLevels = completedLevels,
                collectedEggStripCount = collectedEggStripCount,
                // Stand at the egg once all stations are done, not only after the beach outro (JourneyEndWalk still plays first-time finale).
                endMarkerReached = beachOutroSeen || chapter1AllStationsComplete,
                onPlayLevel = { levelId ->
                    navController.navigate("${NavRoutes.Level}/$levelId")
                },
                onBack = {
                    navController.navigate(NavRoutes.Chapters) { popUpTo(NavRoutes.Journey) { inclusive = true } }
                },
                onDebugUnlockNext = {
                    scope.launch {
                        val completedStationId = progress.debugUnlockNextChapter1Station()
                        when {
                            completedStationId == 3 && !chapter1MidBoostSeen -> {
                                navController.navigate(NavRoutes.Ch1MidBoost) {
                                    popUpTo(NavRoutes.Journey) { inclusive = false }
                                }
                            }
                            completedStationId >= Chapter1Config.STATION_COUNT && !beachOutroSeen -> {
                                navController.navigate(NavRoutes.JourneyEndWalk) {
                                    popUpTo(NavRoutes.Journey) { inclusive = true }
                                }
                            }
                        }
                    }
                },
                onLettersHelp = {
                    navController.navigate(NavRoutes.ChapterLettersIntro) { launchSingleTop = true }
                },
            )
        }

        composable(NavRoutes.JourneyEndWalk) {
            JourneyScreen(
                unlockedLevel = unlockedLevel,
                completedLevels = completedLevels,
                collectedEggStripCount = collectedEggStripCount,
                endMarkerReached = false,
                endWalkThenContinue = true,
                onEndWalkComplete = {
                    // After Dino reaches the egg, show the finale story screen (then it returns to Chapters).
                    navController.navigate(NavRoutes.StoryOutro) {
                        popUpTo(NavRoutes.JourneyEndWalk) { inclusive = true }
                    }
                },
                onPlayLevel = { levelId ->
                    navController.navigate("${NavRoutes.Level}/$levelId")
                },
                onBack = {
                    navController.navigate(NavRoutes.Chapters) { popUpTo(NavRoutes.JourneyEndWalk) { inclusive = true } }
                },
                onDebugUnlockNext = {
                    scope.launch { progress.debugUnlockNextChapter1Station() }
                },
                onLettersHelp = {
                    navController.navigate(NavRoutes.ChapterLettersIntro) { launchSingleTop = true }
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
                onBack = { navController.popBackStack() },
                onLettersHelp = {
                    navController.navigate(NavRoutes.Ch2Letters) { launchSingleTop = true }
                },
                onDebugStationAdvance =
                    if (isDebuggable) {
                        { scope.launch { progress.debugUnlockNextChapter2Station() } }
                    } else {
                        null
                    },
                collectedEggStripCount = collectedEggStripCount,
                suppressInGameDinoProgress = chapter2CompletedStations.contains(stationId),
                onComplete = { completedStationId, correctCount, mistakeCount ->
                    scope.launch {
                        progress.markChapter2CompletedStation(completedStationId)
                        progress.unlockChapter2AtLeast(completedStationId + 1)
                        if (completedStationId >= Chapter2Config.STATION_COUNT) progress.markChapter2Completed()
                    }
                    navController.navigate("${NavRoutes.Ch2Reward}/$completedStationId/$correctCount/$mistakeCount")
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
                onBack = { navController.popBackStack() },
                onLettersHelp = {
                    navController.navigate(NavRoutes.Ch3Letters) { launchSingleTop = true }
                },
                onDebugStationAdvance = null,
                suppressInGameDinoProgress = chapter3CompletedStations.contains(stationId),
                onComplete = { completedStationId, correctCount, mistakeCount ->
                    scope.launch {
                        progress.markChapter3CompletedStation(completedStationId)
                        progress.unlockChapter3AtLeast(completedStationId + 1)
                        if (completedStationId >= Chapter3Config.STATION_COUNT) {
                            progress.markChapter3Completed()
                        }
                    }
                    navController.navigate("${NavRoutes.Ch3Reward}/$completedStationId/$correctCount/$mistakeCount")
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
                onBack = { navController.popBackStack() },
                onLettersHelp = {
                    navController.navigate(NavRoutes.ChapterLettersIntro) { launchSingleTop = true }
                },
                onDebugStationAdvance =
                    if (isDebuggable) {
                        { scope.launch { progress.debugUnlockNextChapter1Station() } }
                    } else {
                        null
                    },
                collectedEggStripCount = collectedEggStripCount,
                suppressInGameDinoProgress = completedLevels.contains(levelId),
                onComplete = { completedLevelId, correctCount, mistakeCount ->
                    scope.launch {
                        progress.markCompleted(completedLevelId)
                        progress.unlockAtLeast(
                            (completedLevelId + 1).coerceAtMost(Chapter1Config.STATION_COUNT),
                        )
                    }
                    // Always show the reward screen; finale flow continues after reward.
                    navController.navigate("${NavRoutes.Reward}/$completedLevelId/$correctCount/$mistakeCount")
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
                if (levelId == 3 && !chapter1MidBoostSeen) {
                    navController.navigate(NavRoutes.Ch1MidBoost) {
                        popUpTo(NavRoutes.Reward) { inclusive = true }
                    }
                    return@backToMap
                }
                val isChapterEnd = levelId >= Chapter1Config.STATION_COUNT
                if (isChapterEnd && !beachOutroSeen) {
                    // Episode 1 finale: after reward, return to journey to watch Dino walk to the egg,
                    // then JourneyEndWalk will show the outro summary screen.
                    navController.navigate(NavRoutes.JourneyEndWalk) {
                        popUpTo(NavRoutes.Journey) { inclusive = true }
                    }
                } else {
                    navController.navigate(NavRoutes.Journey) {
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
            )
        }

        composable(NavRoutes.Ch1MidBoost) {
            Chapter1MidBoostScreen(
                onContinue = {
                    scope.launch { progress.markChapter1MidBoostSeen() }
                    navController.navigate(NavRoutes.Journey) {
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
                if (stationId == 3 && !chapter2MidBoostSeen) {
                    navController.navigate(NavRoutes.Ch2MidBoost) {
                        popUpTo("${NavRoutes.Ch2Reward}/$stationId/$correct/$mistakes") { inclusive = true }
                    }
                    return@backToMap
                }
                if (stationId >= Chapter2Config.STATION_COUNT) {
                    navController.navigate(NavRoutes.Ch2StoryOutro) {
                        popUpTo(NavRoutes.Ch2Journey) { inclusive = true }
                    }
                } else {
                    navController.navigate(NavRoutes.Ch2Journey) {
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
                    scope.launch { progress.markChapter2MidBoostSeen() }
                    navController.navigate(NavRoutes.Ch2Journey) {
                        popUpTo(NavRoutes.Ch2MidBoost) { inclusive = true }
                    }
                },
            )
        }

        composable(NavRoutes.Ch3MidBoost) {
            Chapter3MidBoostScreen(
                eggStripCount = collectedEggStripCount,
                onContinue = {
                    scope.launch { progress.markChapter3MidBoostSeen() }
                    navController.navigate(NavRoutes.Ch3Journey) {
                        popUpTo(NavRoutes.Ch3MidBoost) { inclusive = true }
                    }
                },
            )
        }

        composable(NavRoutes.Ch4MidBoost) {
            Chapter4MidBoostScreen(
                eggStripCount = collectedEggStripCount,
                onContinue = {
                    scope.launch { progress.markChapter4MidBoostSeen() }
                    navController.navigate(NavRoutes.Ch4Journey) {
                        popUpTo(NavRoutes.Ch4MidBoost) { inclusive = true }
                    }
                },
            )
        }

        composable(NavRoutes.Ch2StoryOutro) {
            Chapter2OutroScreen(
                onContinue = {
                    // Safety: ensure chapter-2 completion is persisted even if the last-station completion
                    // path was bypassed (e.g. debug flows / navigation edge cases).
                    scope.launch { progress.markChapter2Completed() }
                    navController.navigate(NavRoutes.Chapters) {
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
                if (stationId == 3 && !chapter3MidBoostSeen) {
                    navController.navigate(NavRoutes.Ch3MidBoost) {
                        popUpTo("${NavRoutes.Ch3Reward}/$stationId/$correct/$mistakes") { inclusive = true }
                    }
                    return@backToMap
                }
                if (stationId >= Chapter3Config.STATION_COUNT) {
                    navController.navigate(NavRoutes.Ch3StoryOutro) {
                        popUpTo(NavRoutes.Ch3Journey) { inclusive = true }
                    }
                } else {
                    navController.navigate(NavRoutes.Ch3Journey) {
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
                eggStripCount = collectedEggStripCount,
                onContinue = {
                    navController.navigate(NavRoutes.Chapters) {
                        popUpTo(NavRoutes.Ch3StoryOutro) { inclusive = true }
                    }
                },
            )
        }

        composable(NavRoutes.Ch4Intro) {
            Chapter4IntroScreen(
                eggStripCount = collectedEggStripCount,
                onContinue = {
                    scope.launch { progress.markChapter4IntroSeen() }
                    navController.navigate(NavRoutes.Ch4Letters) {
                        popUpTo(NavRoutes.Ch4Intro) { inclusive = true }
                    }
                },
            )
        }

        composable(NavRoutes.Ch4Letters) {
            Chapter4LettersIntroScreen(
                onContinue = {
                    scope.launch { progress.markChapter4LettersIntroSeen() }
                    navController.navigate(NavRoutes.Ch4Journey) {
                        popUpTo(NavRoutes.Ch4Letters) { inclusive = true }
                    }
                },
            )
        }

        composable(NavRoutes.Ch4Journey) {
            JourneyScreen(
                unlockedLevel = chapter4UnlockedStation,
                completedLevels = chapter4CompletedStations,
                endMarkerReached = chapter4Completed || chapter4AllStationsComplete,
                totalLevels = Chapter4Config.STATION_COUNT,
                headerTitle = "פרק 4 - סיבוך בדרך",
                headerSubtitle = null,
                headerSubtitleCompact = true,
                collectedEggStripCount = collectedEggStripCount,
                endMarker = JourneyEndMarker.ClueLetterPe,
                backgroundRes = R.drawable.forest_bg_journey_road,
                onPlayLevel = { stationId ->
                    navController.navigate("${NavRoutes.Ch4Level}/$stationId")
                },
                onBack = { navController.navigate(NavRoutes.Chapters) { popUpTo(NavRoutes.Ch4Journey) { inclusive = true } } },
                onLettersHelp = {
                    navController.navigate(NavRoutes.Ch4Letters) { launchSingleTop = true }
                },
                onDebugUnlockNext =
                    if (isDebuggable) {
                        {
                            scope.launch {
                                val completedStationId = progress.debugUnlockNextChapter4Station()
                                when {
                                    completedStationId == 3 && !chapter4MidBoostSeen -> {
                                        navController.navigate(NavRoutes.Ch4MidBoost) {
                                            popUpTo(NavRoutes.Ch4Journey) { inclusive = false }
                                        }
                                    }
                                    completedStationId >= Chapter4Config.STATION_COUNT -> {
                                        navController.navigate(NavRoutes.Ch4Outro) {
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
                onBack = { navController.popBackStack() },
                onLettersHelp = {
                    navController.navigate(NavRoutes.Ch4Letters) { launchSingleTop = true }
                },
                onDebugStationAdvance =
                    if (isDebuggable) {
                        { scope.launch { progress.debugUnlockNextChapter4Station() } }
                    } else {
                        null
                    },
                collectedEggStripCount = collectedEggStripCount,
                suppressInGameDinoProgress = chapter4CompletedStations.contains(stationId),
                onComplete = { completedStationId, correctCount, mistakeCount ->
                    scope.launch {
                        progress.markChapter4CompletedStation(completedStationId)
                        progress.unlockChapter4AtLeast(completedStationId + 1)
                        if (completedStationId >= Chapter4Config.STATION_COUNT) {
                            progress.markChapter4Completed()
                        }
                    }
                    navController.navigate("${NavRoutes.Ch4Reward}/$completedStationId/$correctCount/$mistakeCount")
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
                if (stationId == 3 && !chapter4MidBoostSeen) {
                    navController.navigate(NavRoutes.Ch4MidBoost) {
                        popUpTo("${NavRoutes.Ch4Reward}/$stationId/$correct/$mistakes") { inclusive = true }
                    }
                    return@backToMap
                }
                if (stationId >= Chapter4Config.STATION_COUNT) {
                    navController.navigate(NavRoutes.Ch4Outro) {
                        popUpTo(NavRoutes.Ch4Journey) { inclusive = true }
                    }
                } else {
                    navController.navigate(NavRoutes.Ch4Journey) {
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
            )
        }

        composable(NavRoutes.Ch4Outro) {
            Chapter4OutroScreen(
                eggStripCount = collectedEggStripCount,
                onContinue = {
                    navController.navigate(NavRoutes.Chapters) {
                        popUpTo(NavRoutes.Ch4Outro) { inclusive = true }
                    }
                },
            )
        }

        composable(NavRoutes.Ch5MidBoost) {
            Chapter5MidBoostScreen(
                eggStripCount = collectedEggStripCount,
                onContinue = {
                    scope.launch { progress.markChapter5MidBoostSeen() }
                    navController.navigate(NavRoutes.Ch5Journey) {
                        popUpTo(NavRoutes.Ch5MidBoost) { inclusive = true }
                    }
                },
            )
        }

        composable(NavRoutes.Ch5Intro) {
            Chapter5IntroScreen(
                eggStripCount = collectedEggStripCount,
                onContinue = {
                    scope.launch { progress.markChapter5IntroSeen() }
                    navController.navigate(NavRoutes.Ch5Letters) {
                        popUpTo(NavRoutes.Ch5Intro) { inclusive = true }
                    }
                },
            )
        }

        composable(NavRoutes.Ch5Letters) {
            Chapter5LettersIntroScreen(
                onContinue = {
                    scope.launch { progress.markChapter5LettersIntroSeen() }
                    navController.navigate(NavRoutes.Ch5Journey) {
                        popUpTo(NavRoutes.Ch5Letters) { inclusive = true }
                    }
                },
            )
        }

        composable(NavRoutes.Ch5Journey) {
            JourneyScreen(
                unlockedLevel = chapter5UnlockedStation,
                completedLevels = chapter5CompletedStations,
                endMarkerReached = chapter5Completed || chapter5AllStationsComplete,
                totalLevels = Chapter5Config.STATION_COUNT,
                headerTitle = "פרק 5 - הביצה השלישית",
                headerSubtitle = null,
                headerSubtitleCompact = true,
                collectedEggStripCount = collectedEggStripCount,
                // Third egg on the map should match the purple egg asset.
                endMarker = JourneyEndMarker.PurpleEgg,
                backgroundRes = R.drawable.forest_bg_journey_road,
                onPlayLevel = { stationId ->
                    navController.navigate("${NavRoutes.Ch5Level}/$stationId")
                },
                onBack = { navController.navigate(NavRoutes.Chapters) { popUpTo(NavRoutes.Ch5Journey) { inclusive = true } } },
                onLettersHelp = {
                    navController.navigate(NavRoutes.Ch5Letters) { launchSingleTop = true }
                },
                onDebugUnlockNext =
                    if (isDebuggable) {
                        {
                            scope.launch {
                                val completedStationId = progress.debugUnlockNextChapter5Station()
                                when {
                                    completedStationId == 3 && !chapter5MidBoostSeen -> {
                                        navController.navigate(NavRoutes.Ch5MidBoost) {
                                            popUpTo(NavRoutes.Ch5Journey) { inclusive = false }
                                        }
                                    }
                                    completedStationId >= Chapter5Config.STATION_COUNT -> {
                                        navController.navigate(NavRoutes.Ch5Outro) {
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
                onBack = { navController.popBackStack() },
                onLettersHelp = {
                    navController.navigate(NavRoutes.Ch5Letters) { launchSingleTop = true }
                },
                onDebugStationAdvance =
                    if (isDebuggable) {
                        { scope.launch { progress.debugUnlockNextChapter5Station() } }
                    } else {
                        null
                    },
                collectedEggStripCount = collectedEggStripCount,
                suppressInGameDinoProgress = chapter5CompletedStations.contains(stationId),
                onComplete = { completedStationId, correctCount, mistakeCount ->
                    scope.launch {
                        progress.markChapter5CompletedStation(completedStationId)
                        progress.unlockChapter5AtLeast(completedStationId + 1)
                        if (completedStationId >= Chapter5Config.STATION_COUNT) {
                            progress.markChapter5Completed()
                        }
                    }
                    navController.navigate("${NavRoutes.Ch5Reward}/$completedStationId/$correctCount/$mistakeCount")
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
                if (stationId == 3 && !chapter5MidBoostSeen) {
                    navController.navigate(NavRoutes.Ch5MidBoost) {
                        popUpTo("${NavRoutes.Ch5Reward}/$stationId/$correct/$mistakes") { inclusive = true }
                    }
                    return@backToMap
                }
                if (stationId >= Chapter5Config.STATION_COUNT) {
                    navController.navigate(NavRoutes.Ch5Outro) {
                        popUpTo(NavRoutes.Ch5Journey) { inclusive = true }
                    }
                } else {
                    navController.navigate(NavRoutes.Ch5Journey) {
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
            )
        }

        composable(NavRoutes.Ch5Outro) {
            Chapter5OutroScreen(
                eggStripCount = collectedEggStripCount,
                onContinue = {
                    navController.navigate(NavRoutes.Chapters) {
                        popUpTo(NavRoutes.Ch5Outro) { inclusive = true }
                    }
                },
            )
        }

        composable(NavRoutes.Ch6Intro) {
            Chapter6IntroScreen(
                eggStripCount = collectedEggStripCount,
                onContinue = {
                    scope.launch { progress.markChapter6IntroSeen() }
                    navController.navigate(NavRoutes.Ch6Letters) {
                        popUpTo(NavRoutes.Ch6Intro) { inclusive = true }
                    }
                },
            )
        }

        composable(NavRoutes.Ch6Letters) {
            Chapter6LettersIntroScreen(
                onContinue = {
                    scope.launch { progress.markChapter6LettersIntroSeen() }
                    navController.navigate(NavRoutes.Ch6Journey) {
                        popUpTo(NavRoutes.Ch6Letters) { inclusive = true }
                    }
                },
            )
        }

        composable(NavRoutes.Ch6Journey) {
            JourneyScreen(
                unlockedLevel = chapter6UnlockedStation,
                completedLevels = chapter6CompletedStations,
                endMarkerReached = chapter6Completed || chapter6AllStationsComplete,
                totalLevels = Chapter6Config.STATION_COUNT,
                headerTitle = "פרק 6 - חוזרים הביתה",
                headerSubtitle = null,
                headerSubtitleCompact = true,
                collectedEggStripCount = collectedEggStripCount,
                endMarker = JourneyEndMarker.HomeCave,
                backgroundRes = R.drawable.forest_bg_journey_road,
                onPlayLevel = { stationId ->
                    navController.navigate("${NavRoutes.Ch6Level}/$stationId")
                },
                onBack = { navController.navigate(NavRoutes.Chapters) { popUpTo(NavRoutes.Ch6Journey) { inclusive = true } } },
                onLettersHelp = { navController.navigate(NavRoutes.Ch6Letters) { launchSingleTop = true } },
                onDebugUnlockNext =
                    if (isDebuggable) {
                        {
                            scope.launch {
                                val completedStationId = progress.debugUnlockNextChapter6Station()
                                if (completedStationId >= Chapter6Config.STATION_COUNT) {
                                    navController.navigate(NavRoutes.Ch6Outro) {
                                        popUpTo(NavRoutes.Ch6Journey) { inclusive = true }
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
                onBack = { navController.popBackStack() },
                onLettersHelp = { navController.navigate(NavRoutes.Ch6Letters) { launchSingleTop = true } },
                onDebugStationAdvance = null,
                collectedEggStripCount = collectedEggStripCount,
                suppressInGameDinoProgress = chapter6CompletedStations.contains(stationId),
                onComplete = { completedStationId, correctCount, mistakeCount ->
                    scope.launch {
                        progress.markChapter6CompletedStation(completedStationId)
                        progress.unlockChapter6AtLeast(completedStationId + 1)
                        if (completedStationId >= Chapter6Config.STATION_COUNT) {
                            progress.markChapter6Completed()
                        }
                    }
                    navController.navigate("${NavRoutes.Ch6Reward}/$completedStationId/$correctCount/$mistakeCount")
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
            val backToMap: () -> Unit = {
                if (stationId >= Chapter6Config.STATION_COUNT) {
                    navController.navigate(NavRoutes.Ch6Outro) {
                        popUpTo(NavRoutes.Ch6Journey) { inclusive = true }
                    }
                } else {
                    navController.navigate(NavRoutes.Ch6Journey) {
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
            )
        }

        composable(NavRoutes.Ch6Outro) {
            Chapter6OutroScreen(
                eggStripCount = collectedEggStripCount,
                onContinue = {
                    navController.navigate(NavRoutes.Chapters) {
                        popUpTo(NavRoutes.Ch6Outro) { inclusive = true }
                    }
                },
            )
        }

        composable(NavRoutes.StoryOutro) {
            ForestOutroScreen(
                character = DinoCharacter.Dino,
                onContinue = {
                    scope.launch { progress.markBeachOutroSeen() }
                    navController.navigate(NavRoutes.Chapters) {
                        popUpTo(NavRoutes.StoryOutro) { inclusive = true }
                    }
                },
            )
        }

        composable(NavRoutes.Settings) {
            SettingsScreen(
                onResetAll = {
                    scope.launch {
                        progress.resetAll()
                        prefs.setCharacter(DinoCharacter.Dino)
                        navController.navigate(NavRoutes.Chapters) {
                            popUpTo(NavRoutes.Settings) { inclusive = true }
                        }
                    }
                },
                onResetChapters = { chapterIds ->
                    scope.launch {
                        progress.resetChapters(chapterIds)
                        navController.navigate(NavRoutes.Chapters) {
                            popUpTo(NavRoutes.Settings) { inclusive = true }
                        }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
    }
}


