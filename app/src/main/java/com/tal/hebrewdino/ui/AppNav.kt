package com.tal.hebrewdino.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import com.tal.hebrewdino.ui.domain.Chapter2Config
import com.tal.hebrewdino.ui.domain.Chapter3Config
import com.tal.hebrewdino.ui.domain.Chapter4Config
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
    val beachIntroSeen by progress.beachIntroSeenFlow.collectAsState(initial = false)
    val beachOutroSeen by progress.beachOutroSeenFlow.collectAsState(initial = false)
    val chapter1LettersIntroSeen by progress.chapter1LettersIntroSeenFlow.collectAsState(initial = false)
    val chapter2IntroSeen by progress.chapter2IntroSeenFlow.collectAsState(initial = false)
    val chapter2LettersIntroSeen by progress.chapter2LettersIntroSeenFlow.collectAsState(initial = false)
    val chapter2UnlockedStation by progress.chapter2UnlockedStationFlow.collectAsState(initial = 1)
    val chapter2CompletedStations by progress.chapter2CompletedStationsFlow.collectAsState(initial = emptySet())
    val chapter2Completed by progress.chapter2CompletedFlow.collectAsState(initial = false)
    val chapter3IntroSeen by progress.chapter3IntroSeenFlow.collectAsState(initial = false)
    val chapter3LettersIntroSeen by progress.chapter3LettersIntroSeenFlow.collectAsState(initial = false)
    val chapter3UnlockedStation by progress.chapter3UnlockedStationFlow.collectAsState(initial = 1)
    val chapter3CompletedStations by progress.chapter3CompletedStationsFlow.collectAsState(initial = emptySet())
    val chapter3Completed by progress.chapter3CompletedFlow.collectAsState(initial = false)
    val chapter4IntroSeen by progress.chapter4IntroSeenFlow.collectAsState(initial = false)
    val chapter4LettersIntroSeen by progress.chapter4LettersIntroSeenFlow.collectAsState(initial = false)
    val chapter4UnlockedStation by progress.chapter4UnlockedStationFlow.collectAsState(initial = 1)
    val chapter4CompletedStations by progress.chapter4CompletedStationsFlow.collectAsState(initial = emptySet())
    val chapter4Completed by progress.chapter4CompletedFlow.collectAsState(initial = false)
    val unlockedLevel by progress.unlockedLevelFlow.collectAsState(initial = 1)
    val completedLevels by progress.completedLevelsFlow.collectAsState(initial = emptySet())

    val unlockedChapter =
        when {
            chapter4Completed -> 4
            chapter3Completed -> 4
            chapter2Completed -> 3
            beachOutroSeen -> 2
            else -> 1
        }
    /** Chapter 4 story unlocks after chapter 3 is finished; “בקרוב” shows until then. */
    val chapter4ComingSoon = !chapter3Completed
    val maxSelectableChapterId = if (chapter3Completed) 4 else 3

    val startDestination = NavRoutes.Chapters

    LaunchedEffect(Unit) {
        progress.repairChapter2ProgressIfNeeded()
        progress.repairChapter3ProgressIfNeeded()
        progress.repairChapter4ProgressIfNeeded()
        prefs.setCharacter(DinoCharacter.Dino)
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(NavRoutes.Chapters) {
            ChaptersScreen(
                unlockedChapter = unlockedChapter,
                chapter4ComingSoon = chapter4ComingSoon,
                maxSelectableChapterId = maxSelectableChapterId,
                chaptersProgress =
                    ChaptersProgress(
                        chapter1Completed = beachOutroSeen,
                        chapter2Completed = chapter2Completed,
                        chapter3Completed = chapter3Completed,
                        chapter4Completed = chapter4Completed,
                    ),
                onOpenSettings = { navController.navigate(NavRoutes.Settings) },
                onOpenChapter = { chapterId ->
                    when (chapterId) {
                        1 -> {
                            val next =
                                when {
                                    !beachIntroSeen -> NavRoutes.StoryIntro
                                    !chapter1LettersIntroSeen -> NavRoutes.ChapterLettersIntro
                                    else -> NavRoutes.Journey
                                }
                            navController.navigate(next)
                        }
                        2 -> {
                            val next =
                                when {
                                    !beachOutroSeen -> NavRoutes.Chapters // locked; shouldn't happen
                                    !chapter2IntroSeen -> NavRoutes.Ch2Intro
                                    !chapter2LettersIntroSeen -> NavRoutes.Ch2Letters
                                    else -> NavRoutes.Ch2Journey
                                }
                            navController.navigate(next)
                        }
                        3 -> {
                            val next =
                                when {
                                    !chapter2Completed -> NavRoutes.Chapters
                                    !chapter3IntroSeen -> NavRoutes.Ch3Intro
                                    !chapter3LettersIntroSeen -> NavRoutes.Ch3Letters
                                    else -> NavRoutes.Ch3Journey
                                }
                            if (next != NavRoutes.Chapters) navController.navigate(next)
                        }
                        4 -> {
                            if (chapter3Completed) {
                                val next =
                                    when {
                                        !chapter4IntroSeen -> NavRoutes.Ch4Intro
                                        !chapter4LettersIntroSeen -> NavRoutes.Ch4Letters
                                        else -> NavRoutes.Ch4Journey
                                    }
                                navController.navigate(next)
                            }
                        }
                        in 5..10 -> Unit
                    }
                },
            )
        }

        composable(NavRoutes.StoryIntro) {
            ForestIntroScreen(
                character = DinoCharacter.Dino,
                onContinue = {
                    scope.launch { progress.markBeachIntroSeen() }
                    navController.navigate(NavRoutes.ChapterLettersIntro) {
                        popUpTo(NavRoutes.StoryIntro) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
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
                onBack = { navController.popBackStack() },
            )
        }

        composable(NavRoutes.Ch2Intro) {
            Chapter2IntroScreen(
                onContinue = {
                    scope.launch { progress.markChapter2IntroSeen() }
                    navController.navigate(NavRoutes.Ch2Letters) {
                        popUpTo(NavRoutes.Ch2Intro) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
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
                onBack = { navController.popBackStack() },
            )
        }

        composable(NavRoutes.Ch2Journey) {
            JourneyScreen(
                unlockedLevel = chapter2UnlockedStation,
                completedLevels = chapter2CompletedStations,
                totalLevels = Chapter2Config.STATION_COUNT,
                headerTitle = "פרק 2 - מצא את הביצה הורודה",
                headerSubtitle = "בדרך לביצה הורודה — ${Chapter2Config.STATION_COUNT} תחנות",
                endMarker = JourneyEndMarker.PinkEgg,
                backgroundRes = R.drawable.mountain_bg_chapter2,
                onPlayLevel = { stationId ->
                    navController.navigate("${NavRoutes.Ch2Level}/$stationId")
                },
                onBack = { navController.navigate(NavRoutes.Chapters) { popUpTo(NavRoutes.Ch2Journey) { inclusive = true } } },
                onDebugUnlockNext = {
                    scope.launch { progress.debugUnlockNextChapter2Station() }
                },
            )
        }

        composable(NavRoutes.Ch3Intro) {
            Chapter3IntroScreen(
                onContinue = {
                    scope.launch { progress.markChapter3IntroSeen() }
                    navController.navigate(NavRoutes.Ch3Letters) {
                        popUpTo(NavRoutes.Ch3Intro) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
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
                onBack = { navController.popBackStack() },
            )
        }

        composable(NavRoutes.Ch3Journey) {
            JourneyScreen(
                unlockedLevel = chapter3UnlockedStation,
                completedLevels = chapter3CompletedStations,
                totalLevels = Chapter3Config.STATION_COUNT,
                headerTitle = "פרק 3 - מצא את הביצה הסגולה",
                headerSubtitle = "בדרך לביצה הסגולה — ${Chapter3Config.STATION_COUNT} תחנות",
                headerSubtitleCompact = true,
                endMarker = JourneyEndMarker.PurpleEgg,
                backgroundRes = R.drawable.mountain_bg_chapter3,
                onPlayLevel = { stationId ->
                    navController.navigate("${NavRoutes.Ch3Level}/$stationId")
                },
                onBack = { navController.navigate(NavRoutes.Chapters) { popUpTo(NavRoutes.Ch3Journey) { inclusive = true } } },
                onDebugUnlockNext = {
                    scope.launch { progress.debugUnlockNextChapter3Station() }
                },
            )
        }

        composable(NavRoutes.Journey) {
            JourneyScreen(
                unlockedLevel = unlockedLevel,
                completedLevels = completedLevels,
                endMarkerReached = beachOutroSeen,
                onPlayLevel = { levelId ->
                    navController.navigate("${NavRoutes.Level}/$levelId")
                },
                onBack = {
                    navController.navigate(NavRoutes.Chapters) { popUpTo(NavRoutes.Journey) { inclusive = true } }
                },
                onDebugUnlockNext = {
                    scope.launch { progress.debugUnlockNextChapter1Station() }
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
                onLettersHelp = null,
                onDebugStationAdvance =
                    if (isDebuggable) {
                        { scope.launch { progress.debugUnlockNextChapter1Station() } }
                    } else {
                        null
                    },
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
            RewardScreen(
                levelId = levelId,
                correct = correct,
                mistakes = mistakes,
                onBackToMap = {
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
            RewardScreen(
                levelId = stationId,
                correct = correct,
                mistakes = mistakes,
                onBackToMap = {
                    if (stationId >= Chapter2Config.STATION_COUNT) {
                        navController.navigate(NavRoutes.Ch2StoryOutro) {
                            popUpTo(NavRoutes.Ch2Journey) { inclusive = true }
                        }
                    } else {
                        navController.navigate(NavRoutes.Ch2Journey) {
                            popUpTo(NavRoutes.Ch2Journey) { inclusive = true }
                        }
                    }
                },
            )
        }

        composable(NavRoutes.Ch2StoryOutro) {
            Chapter2OutroScreen(
                onContinue = {
                    navController.navigate(NavRoutes.Chapters) {
                        popUpTo(NavRoutes.Ch2StoryOutro) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
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
            RewardScreen(
                levelId = stationId,
                correct = correct,
                mistakes = mistakes,
                onBackToMap = {
                    if (stationId >= Chapter3Config.STATION_COUNT) {
                        navController.navigate(NavRoutes.Ch3StoryOutro) {
                            popUpTo(NavRoutes.Ch3Journey) { inclusive = true }
                        }
                    } else {
                        navController.navigate(NavRoutes.Ch3Journey) {
                            popUpTo(NavRoutes.Ch3Journey) { inclusive = true }
                        }
                    }
                },
            )
        }

        composable(NavRoutes.Ch3StoryOutro) {
            Chapter3OutroScreen(
                onContinue = {
                    navController.navigate(NavRoutes.Chapters) {
                        popUpTo(NavRoutes.Ch3StoryOutro) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(NavRoutes.Ch4Intro) {
            Chapter4IntroScreen(
                onContinue = {
                    scope.launch { progress.markChapter4IntroSeen() }
                    navController.navigate(NavRoutes.Ch4Letters) {
                        popUpTo(NavRoutes.Ch4Intro) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
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
                onBack = { navController.popBackStack() },
            )
        }

        composable(NavRoutes.Ch4Journey) {
            JourneyScreen(
                unlockedLevel = chapter4UnlockedStation,
                completedLevels = chapter4CompletedStations,
                totalLevels = Chapter4Config.STATION_COUNT,
                headerTitle = "פרק 4 - חיזוק חכם",
                headerSubtitle = "אותיות ומילים — ${Chapter4Config.STATION_COUNT} תחנות",
                headerSubtitleCompact = true,
                endMarker = JourneyEndMarker.BigEgg,
                backgroundRes = R.drawable.mountain_bg_chapter4,
                onPlayLevel = { stationId ->
                    navController.navigate("${NavRoutes.Ch4Level}/$stationId")
                },
                onBack = { navController.navigate(NavRoutes.Chapters) { popUpTo(NavRoutes.Ch4Journey) { inclusive = true } } },
                onDebugUnlockNext =
                    if (isDebuggable) {
                        { scope.launch { progress.debugUnlockNextChapter4Station() } }
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
            RewardScreen(
                levelId = stationId,
                correct = correct,
                mistakes = mistakes,
                onBackToMap = {
                    if (stationId >= Chapter4Config.STATION_COUNT) {
                        navController.navigate(NavRoutes.Ch4Outro) {
                            popUpTo(NavRoutes.Ch4Journey) { inclusive = true }
                        }
                    } else {
                        navController.navigate(NavRoutes.Ch4Journey) {
                            popUpTo(NavRoutes.Ch4Journey) { inclusive = true }
                        }
                    }
                },
            )
        }

        composable(NavRoutes.Ch4Outro) {
            Chapter4OutroScreen(
                onContinue = {
                    navController.navigate(NavRoutes.Chapters) {
                        popUpTo(NavRoutes.Ch4Outro) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
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
                onBack = { navController.popBackStack() },
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
                onBack = { navController.popBackStack() },
            )
        }
    }
}


