package com.tal.hebrewdino.ui

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tal.hebrewdino.BuildConfig
import com.tal.hebrewdino.ui.data.AudioPrefs
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.data.Season2ProgressPrefs
import com.tal.hebrewdino.ui.domain.Season2Chapter1StationOrder
import com.tal.hebrewdino.ui.screens.ChaptersScreen
import com.tal.hebrewdino.ui.screens.OpeningScreen
import com.tal.hebrewdino.ui.screens.Season2ChapterSelectScreen
import com.tal.hebrewdino.ui.screens.Season2ChapterStationScreen
import com.tal.hebrewdino.ui.screens.Season2PuzzleMapPrototypeScreen
import com.tal.hebrewdino.ui.screens.SeasonsScreen
import com.tal.hebrewdino.ui.screens.SettingsScreen
import com.tal.hebrewdino.ui.screens.TrainingV1CompleteScreen
import com.tal.hebrewdino.ui.screens.TrainingV1IntroScreen
import com.tal.hebrewdino.ui.screens.TrainingV1RoundScreen
import com.tal.hebrewdino.ui.domain.TrainingV1Config
import kotlinx.coroutines.launch

internal fun NavGraphBuilder.systemAndTrainingGraph(host: AppNavHostState) {
    composable(NavRoutes.Opening) {
        val context = LocalContext.current
        OpeningScreen(
            onPlay = {
                host.navController.navigate(NavRoutes.Seasons) {
                    launchSingleTop = true
                }
            },
            onOpenSettings = { host.navController.navigate(NavRoutes.Settings) },
            onExit = { (context as? android.app.Activity)?.finish() },
        )
    }

    composable(NavRoutes.Seasons) {
        SeasonsScreen(
            onOpenSeason1 = {
                host.navController.navigate(NavRoutes.Chapters) { launchSingleTop = true }
            },
            onOpenSeason2 =
                if (BuildConfig.DEBUG) {
                    {
                        host.navController.navigate(NavRoutes.Season2ChapterSelect) {
                            launchSingleTop = true
                        }
                    }
                } else {
                    {}
                },
            onBackToOpening = {
                host.navController.navigate(NavRoutes.Opening) {
                    popUpTo(NavRoutes.Opening) { inclusive = false }
                    launchSingleTop = true
                }
            },
        )
    }

    composable(NavRoutes.Season2ChapterSelect) {
        Season2ChapterSelectScreen(
            onBack = { host.navController.popBackStack() },
            onOpenChapter = { chapterId ->
                host.navController.navigate(NavRoutes.season2PuzzleMapPrototype(chapterId)) {
                    launchSingleTop = true
                }
            },
        )
    }

    composable(
        route = NavRoutes.Season2PuzzleMapPrototype,
        arguments = listOf(navArgument("chapterId") { type = NavType.IntType }),
    ) { backStackEntry ->
        val chapterId = backStackEntry.arguments?.getInt("chapterId") ?: 1
        Season2PuzzleMapPrototypeScreen(
            chapterId = chapterId,
            onBack = { host.navController.popBackStack() },
            onOpenStation = { stationId ->
                host.navController.navigate(NavRoutes.season2ChapterStation(chapterId, stationId)) {
                    launchSingleTop = true
                }
            },
        )
    }

    composable(
        route = NavRoutes.Season2ChapterStation,
        arguments =
            listOf(
                navArgument("chapterId") { type = NavType.IntType },
                navArgument("stationId") { type = NavType.IntType },
            ),
    ) { backStackEntry ->
        val chapterId = backStackEntry.arguments?.getInt("chapterId") ?: 1
        val stationId = backStackEntry.arguments?.getInt("stationId") ?: Season2Chapter1StationOrder.POP_BALLOONS
        Season2ChapterStationScreen(
            chapterId = chapterId,
            stationId = stationId,
            onBack = { host.navController.popBackStack() },
            onComplete = {
                // Return to the puzzle board; it will reveal the next piece based on saved progress.
                host.navController.popBackStack()
            },
        )
    }

    composable(NavRoutes.Chapters) {
        ChaptersScreen(
            unlockedChapter = host.unlockedChapter,
            chapter4ComingSoon = host.chapter4ComingSoon,
            chapter5ComingSoon = host.chapter5ComingSoon,
            chapter6ComingSoon = host.chapter6ComingSoon,
            maxSelectableChapterId = host.maxSelectableChapterId,
            chaptersProgress = host.chaptersProgress,
            onBackToSeasons = {
                host.navController.navigate(NavRoutes.Seasons) {
                    popUpTo(NavRoutes.Seasons) { inclusive = false }
                    launchSingleTop = true
                }
            },
            onOpenSettings = { host.navController.navigate(NavRoutes.Settings) },
            onOpenChapter = { chapterId ->
                when (chapterId) {
                    1 -> {
                        // Season 1 Ch.1: Dino companion intro, then existing story intro.
                        host.navController.navigate(NavRoutes.Ch1DinoCompanionIntro)
                    }
                    2 -> {
                        val canEnterChapter2 = host.beachOutroSeen || host.chapter1AllStationsComplete
                        if (canEnterChapter2) {
                            // Always show chapter intro on entry (user request).
                            host.navController.navigate(NavRoutes.Ch2Intro)
                        }
                    }
                    3 -> {
                        val next =
                            when {
                                !host.chapter2Completed -> NavRoutes.Chapters
                                else -> NavRoutes.Ch3Intro
                            }
                        if (next != NavRoutes.Chapters) host.navController.navigate(next)
                    }
                    4 -> {
                        if (host.chapter3Completed) {
                            // Always show chapter intro on entry, then letters (same behavior as chapters 1–3).
                            host.navController.navigate(NavRoutes.Ch4Intro)
                        }
                    }
                    5 -> {
                        if (host.chapter4Completed) {
                            // Always show chapter intro on entry, then letters (same behavior as chapters 1–3).
                            host.navController.navigate(NavRoutes.Ch5Intro)
                        }
                    }
                    6 -> {
                        if (host.chapter5Completed) {
                            host.navController.navigate(NavRoutes.Ch6Intro)
                        }
                    }
                    7 -> {
                        if (host.chapter6Completed) {
                            host.navController.navigate(NavRoutes.TrainingIntro)
                        }
                    }
                    in 8..10 -> Unit
                }
            },
        )
    }

    composable(NavRoutes.TrainingIntro) {
        TrainingV1IntroScreen(
            onContinue = { host.navController.navigate("${NavRoutes.TrainingRound}/1") },
        )
    }

    composable(
        route = "${NavRoutes.TrainingRound}/{roundIndex}",
        arguments = listOf(navArgument("roundIndex") { type = NavType.IntType }),
    ) { backStackEntry ->
        val roundIndex = backStackEntry.arguments?.getInt("roundIndex") ?: 1
        TrainingV1RoundScreen(
            roundIndex = roundIndex,
            onBack = {
                host.navController.navigate(NavRoutes.Chapters) {
                    popUpTo(NavRoutes.Chapters) { inclusive = false }
                    launchSingleTop = true
                }
            },
            onRoundComplete = {
                if (roundIndex >= TrainingV1Config.TOTAL_ROUNDS) {
                    host.navController.navigate(NavRoutes.TrainingComplete) {
                        popUpTo(NavRoutes.TrainingIntro) { inclusive = false }
                    }
                } else {
                    host.navController.navigate("${NavRoutes.TrainingRound}/${roundIndex + 1}") {
                        popUpTo("${NavRoutes.TrainingRound}/$roundIndex") { inclusive = true }
                    }
                }
            },
        )
    }

    composable(NavRoutes.TrainingComplete) {
        TrainingV1CompleteScreen(
            onContinue = {
                host.navController.navigate(NavRoutes.Chapters) {
                    popUpTo(NavRoutes.TrainingIntro) { inclusive = true }
                }
            },
        )
    }

    composable(NavRoutes.Settings) {
        val context = LocalContext.current
        val audioPrefs = remember(context) { AudioPrefs(context.applicationContext) }
        val season2Progress = remember(context) { Season2ProgressPrefs(context.applicationContext) }
        val backgroundMusicEnabled by audioPrefs.backgroundMusicEnabledFlow.collectAsState(initial = true)
        SettingsScreen(
            backgroundMusicEnabled = backgroundMusicEnabled,
            onBackgroundMusicEnabledChange = { enabled ->
                host.scope.launch {
                    audioPrefs.setBackgroundMusicEnabled(enabled)
                }
            },
            onResetAll = {
                host.scope.launch {
                    host.progress.resetAll()
                    host.prefs.setCharacter(DinoCharacter.Dino)
                    host.navController.navigate(NavRoutes.Chapters) {
                        popUpTo(NavRoutes.Settings) { inclusive = true }
                    }
                }
            },
            onResetChapters = { chapterIds ->
                host.scope.launch {
                    host.progress.resetChapters(chapterIds)
                    host.navController.navigate(NavRoutes.Chapters) {
                        popUpTo(NavRoutes.Settings) { inclusive = true }
                    }
                }
            },
            onResetSeason2 = {
                host.scope.launch {
                    season2Progress.resetSeason2()
                }
            },
            onBack = { host.navController.popBackStack() },
        )
    }
}
