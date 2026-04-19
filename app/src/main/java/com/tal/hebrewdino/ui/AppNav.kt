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
import com.tal.hebrewdino.ui.data.CharacterPrefs
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.data.ProgressPrefs
import com.tal.hebrewdino.ui.screens.Chapter1LettersIntroScreen
import com.tal.hebrewdino.ui.screens.Chapter2LettersIntroScreen
import com.tal.hebrewdino.ui.screens.Chapter3IntroScreen
import com.tal.hebrewdino.ui.screens.Chapter3LettersIntroScreen
import com.tal.hebrewdino.ui.screens.Chapter3LevelScreen
import com.tal.hebrewdino.ui.screens.ChaptersProgress
import com.tal.hebrewdino.ui.screens.ChaptersScreen
import com.tal.hebrewdino.ui.screens.Chapter2IntroScreen
import com.tal.hebrewdino.ui.screens.ForestIntroScreen
import com.tal.hebrewdino.ui.screens.ForestOutroScreen
import com.tal.hebrewdino.ui.screens.JourneyEndMarker
import com.tal.hebrewdino.ui.screens.JourneyScreen
import com.tal.hebrewdino.ui.screens.LevelScreen
import com.tal.hebrewdino.ui.screens.RewardScreen
import com.tal.hebrewdino.ui.screens.SettingsScreen
import com.tal.hebrewdino.ui.screens.Chapter2LevelScreen
import com.tal.hebrewdino.R
import kotlinx.coroutines.launch

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val context = LocalContext.current
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
    val unlockedLevel by progress.unlockedLevelFlow.collectAsState(initial = 1)
    val completedLevels by progress.completedLevelsFlow.collectAsState(initial = emptySet())

    val unlockedChapter =
        when {
            chapter3Completed -> 3
            chapter2Completed -> 3
            beachOutroSeen -> 2
            else -> 1
        }
    val chapter4ComingSoon = chapter3Completed

    val startDestination = Routes.Chapters

    LaunchedEffect(Unit) {
        progress.repairChapter3ProgressIfNeeded()
        prefs.setCharacter(DinoCharacter.Dino)
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.Chapters) {
            ChaptersScreen(
                unlockedChapter = unlockedChapter,
                chapter4ComingSoon = chapter4ComingSoon,
                chaptersProgress =
                    ChaptersProgress(
                        chapter1Completed = beachOutroSeen,
                        chapter2Completed = chapter2Completed,
                        chapter3Completed = chapter3Completed,
                    ),
                onOpenSettings = { navController.navigate(Routes.Settings) },
                onOpenChapter = { chapterId ->
                    when (chapterId) {
                        1 -> {
                            val next =
                                when {
                                    !beachIntroSeen -> Routes.StoryIntro
                                    !chapter1LettersIntroSeen -> Routes.ChapterLettersIntro
                                    else -> Routes.Journey
                                }
                            navController.navigate(next)
                        }
                        2 -> {
                            val next =
                                when {
                                    !beachOutroSeen -> Routes.Chapters // locked; shouldn't happen
                                    !chapter2IntroSeen -> Routes.Ch2Intro
                                    !chapter2LettersIntroSeen -> Routes.Ch2Letters
                                    else -> Routes.Ch2Journey
                                }
                            navController.navigate(next)
                        }
                        3 -> {
                            val next =
                                when {
                                    !chapter2Completed -> Routes.Chapters
                                    !chapter3IntroSeen -> Routes.Ch3Intro
                                    !chapter3LettersIntroSeen -> Routes.Ch3Letters
                                    else -> Routes.Ch3Journey
                                }
                            if (next != Routes.Chapters) navController.navigate(next)
                        }
                        in 4..10 -> Unit
                    }
                },
            )
        }

        composable(Routes.StoryIntro) {
            ForestIntroScreen(
                character = DinoCharacter.Dino,
                onContinue = {
                    scope.launch { progress.markBeachIntroSeen() }
                    navController.navigate(Routes.ChapterLettersIntro) {
                        popUpTo(Routes.StoryIntro) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.ChapterLettersIntro) {
            Chapter1LettersIntroScreen(
                onContinue = {
                    scope.launch { progress.markChapter1LettersIntroSeen() }
                    navController.navigate(Routes.Journey) {
                        popUpTo(Routes.ChapterLettersIntro) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.Ch2Intro) {
            Chapter2IntroScreen(
                onContinue = {
                    scope.launch { progress.markChapter2IntroSeen() }
                    navController.navigate(Routes.Ch2Letters) {
                        popUpTo(Routes.Ch2Intro) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.Ch2Letters) {
            Chapter2LettersIntroScreen(
                onContinue = {
                    scope.launch { progress.markChapter2LettersIntroSeen() }
                    navController.navigate(Routes.Ch2Journey) {
                        popUpTo(Routes.Ch2Letters) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.Ch2Journey) {
            JourneyScreen(
                unlockedLevel = chapter2UnlockedStation,
                completedLevels = chapter2CompletedStations,
                totalLevels = Chapter2Config.STATION_COUNT,
                headerTitle = "פרק 2 - חוזרים הביתה",
                headerSubtitle = "הדרך חזרה לקן — ${Chapter2Config.STATION_COUNT} תחנות",
                endMarker = JourneyEndMarker.HomeCave,
                companionImageRes = R.drawable.mom_idle,
                onPlayLevel = { stationId ->
                    navController.navigate("${Routes.Ch2Level}/$stationId")
                },
                onBack = { navController.navigate(Routes.Chapters) { popUpTo(Routes.Ch2Journey) { inclusive = true } } },
                onDebugUnlockNext = {
                    scope.launch {
                        val last = Chapter2Config.STATION_COUNT
                        val next =
                            (1..last).firstOrNull { !chapter2CompletedStations.contains(it) } ?: last
                        progress.markChapter2CompletedStation(next)
                        progress.unlockChapter2AtLeast(next + 1)
                        if (next >= last) progress.markChapter2Completed()
                    }
                },
            )
        }

        composable(Routes.Ch3Intro) {
            Chapter3IntroScreen(
                onContinue = {
                    scope.launch { progress.markChapter3IntroSeen() }
                    navController.navigate(Routes.Ch3Letters) {
                        popUpTo(Routes.Ch3Intro) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.Ch3Letters) {
            Chapter3LettersIntroScreen(
                onContinue = {
                    scope.launch { progress.markChapter3LettersIntroSeen() }
                    navController.navigate(Routes.Ch3Journey) {
                        popUpTo(Routes.Ch3Letters) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.Ch3Journey) {
            JourneyScreen(
                unlockedLevel = chapter3UnlockedStation,
                completedLevels = chapter3CompletedStations,
                totalLevels = Chapter3Config.STATION_COUNT,
                playableLevels = Chapter3Config.MAX_PLAYABLE_STATION,
                headerTitle = "פרק 3 - מצא את החבר",
                headerSubtitle = "בדרך עם דינו — ${Chapter3Config.STATION_COUNT} תחנות",
                headerSubtitleCompact = true,
                endMarker = JourneyEndMarker.HomeCave,
                onPlayLevel = { stationId ->
                    navController.navigate("${Routes.Ch3Level}/$stationId")
                },
                onBack = { navController.navigate(Routes.Chapters) { popUpTo(Routes.Ch3Journey) { inclusive = true } } },
                onDebugUnlockNext = null,
            )
        }

        composable(Routes.Journey) {
            JourneyScreen(
                unlockedLevel = unlockedLevel,
                completedLevels = completedLevels,
                onPlayLevel = { levelId ->
                    navController.navigate("${Routes.Level}/$levelId")
                },
                onBack = {
                    navController.navigate(Routes.Chapters) { popUpTo(Routes.Journey) { inclusive = true } }
                },
                onDebugUnlockNext = {
                    scope.launch {
                        val last = Chapter1Config.STATION_COUNT
                        val next =
                            (1..last).firstOrNull { !completedLevels.contains(it) } ?: last
                        progress.markCompleted(next)
                        progress.unlockAtLeast((next + 1).coerceAtMost(last))
                    }
                },
            )
        }

        composable(
            route = "${Routes.Ch2Level}/{stationId}",
            arguments = listOf(navArgument("stationId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val stationId = backStackEntry.arguments?.getInt("stationId") ?: 1
            Chapter2LevelScreen(
                stationId = stationId,
                onBack = { navController.popBackStack() },
                onComplete = { completedStationId, correctCount, mistakeCount ->
                    scope.launch {
                        progress.markChapter2CompletedStation(completedStationId)
                        progress.unlockChapter2AtLeast(completedStationId + 1)
                        if (completedStationId >= Chapter2Config.STATION_COUNT) progress.markChapter2Completed()
                    }
                    navController.navigate("${Routes.Ch2Reward}/$completedStationId/$correctCount/$mistakeCount")
                },
            )
        }

        composable(
            route = "${Routes.Ch3Level}/{stationId}",
            arguments = listOf(navArgument("stationId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val rawId = backStackEntry.arguments?.getInt("stationId") ?: 1
            if (rawId > Chapter3Config.MAX_PLAYABLE_STATION) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
                Spacer(modifier = Modifier.size(0.dp))
            } else {
                Chapter3LevelScreen(
                    stationId = rawId,
                    onBack = { navController.popBackStack() },
                    onComplete = { completedStationId, correctCount, mistakeCount ->
                        scope.launch {
                            progress.markChapter3CompletedStation(completedStationId)
                            progress.unlockChapter3AtLeast(completedStationId + 1)
                            if (completedStationId >= Chapter3Config.STATION_COUNT) {
                                progress.markChapter3Completed()
                            }
                        }
                        navController.navigate("${Routes.Ch3Reward}/$completedStationId/$correctCount/$mistakeCount")
                    },
                )
            }
        }

        composable(
            route = "${Routes.Level}/{levelId}",
            arguments = listOf(navArgument("levelId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val levelId = backStackEntry.arguments?.getInt("levelId") ?: 1
            LevelScreen(
                levelId = levelId,
                onBack = { navController.popBackStack() },
                onComplete = { completedLevelId, correctCount, mistakeCount ->
                    scope.launch {
                        progress.markCompleted(completedLevelId)
                        progress.unlockAtLeast(
                            (completedLevelId + 1).coerceAtMost(Chapter1Config.STATION_COUNT),
                        )
                    }
                    navController.navigate("${Routes.Reward}/$completedLevelId/$correctCount/$mistakeCount")
                },
            )
        }

        composable(
            route = "${Routes.Reward}/{levelId}/{correct}/{mistakes}",
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
                        navController.navigate(Routes.StoryOutro) {
                            popUpTo(Routes.Journey) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Routes.Journey) {
                            popUpTo(Routes.Journey) { inclusive = true }
                        }
                    }
                },
            )
        }

        composable(
            route = "${Routes.Ch2Reward}/{stationId}/{correct}/{mistakes}",
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
                        navController.navigate(Routes.Chapters) {
                            popUpTo(Routes.Ch2Journey) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Routes.Ch2Journey) {
                            popUpTo(Routes.Ch2Journey) { inclusive = true }
                        }
                    }
                },
            )
        }

        composable(
            route = "${Routes.Ch3Reward}/{stationId}/{correct}/{mistakes}",
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
                        navController.navigate(Routes.Chapters) {
                            popUpTo(Routes.Ch3Journey) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Routes.Ch3Journey) {
                            popUpTo(Routes.Ch3Journey) { inclusive = true }
                        }
                    }
                },
            )
        }

        composable(Routes.StoryOutro) {
            ForestOutroScreen(
                character = DinoCharacter.Dino,
                onContinue = {
                    scope.launch { progress.markBeachOutroSeen() }
                    navController.navigate(Routes.Chapters) {
                        popUpTo(Routes.StoryOutro) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.Settings) {
            SettingsScreen(
                onResetAll = {
                    scope.launch {
                        progress.resetAll()
                        prefs.setCharacter(DinoCharacter.Dino)
                        navController.navigate(Routes.Chapters) {
                            popUpTo(Routes.Settings) { inclusive = true }
                        }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
    }
}

private object Routes {
    const val Chapters = "chapters"
    const val StoryIntro = "story_intro"
    const val ChapterLettersIntro = "chapter_letters_intro"
    const val Journey = "journey"
    const val Level = "level"
    const val Ch2Intro = "ch2_intro"
    const val Ch2Letters = "ch2_letters"
    const val Ch2Journey = "ch2_journey"
    const val Ch2Level = "ch2_level"
    const val Reward = "reward"
    const val Ch2Reward = "ch2_reward"
    const val Ch3Intro = "ch3_intro"
    const val Ch3Letters = "ch3_letters"
    const val Ch3Journey = "ch3_journey"
    const val Ch3Level = "ch3_level"
    const val Ch3Reward = "ch3_reward"
    const val StoryOutro = "story_outro"
    const val Settings = "settings"
}

