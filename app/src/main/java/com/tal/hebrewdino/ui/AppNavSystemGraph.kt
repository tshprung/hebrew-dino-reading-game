package com.tal.hebrewdino.ui

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tal.hebrewdino.ui.audio.TextToSpeechManager
import com.tal.hebrewdino.ui.data.AudioPrefs
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.domain.ChallengeType
import com.tal.hebrewdino.ui.screens.CharacterSelectionScreen
import com.tal.hebrewdino.ui.screens.ChallengeSummaryScreen
import com.tal.hebrewdino.ui.screens.DinoHomeScreen
import com.tal.hebrewdino.ui.screens.DinoHomeViewModel
import com.tal.hebrewdino.ui.screens.ChaptersScreen
import com.tal.hebrewdino.ui.screens.FallingLettersScreen
import com.tal.hebrewdino.ui.screens.IntroInstructionScreen
import com.tal.hebrewdino.ui.screens.OpeningScreen
import com.tal.hebrewdino.ui.screens.ParentalGateScreen
import com.tal.hebrewdino.ui.screens.SeasonsScreen
import com.tal.hebrewdino.ui.screens.SettingsScreen
import com.tal.hebrewdino.ui.screens.TrainingV1CompleteScreen
import com.tal.hebrewdino.ui.screens.TrainingV1IntroScreen
import com.tal.hebrewdino.ui.screens.TrainingV1RoundScreen
import com.tal.hebrewdino.ui.screens.WordChallengeScreen
import com.tal.hebrewdino.ui.domain.TrainingV1Config
import com.tal.hebrewdino.ui.data.CharacterRepository
import com.tal.hebrewdino.ui.economy.RewardEngine
import kotlinx.coroutines.launch

internal fun NavGraphBuilder.systemAndTrainingGraph(host: AppNavHostState) {
    composable(NavRoutes.CharacterSelection) {
        CharacterSelectionScreen(
            onSelect = { character ->
                host.scope.launch { host.prefs.setCharacter(character) }
                host.navController.navigate(NavRoutes.IntroInstruction) {
                    popUpTo(NavRoutes.CharacterSelection) { inclusive = true }
                    launchSingleTop = true
                }
            },
        )
    }

    composable(
        route = NavRoutes.WordChallenge,
        arguments =
            listOf(
                navArgument(NavRoutes.WordChallengeTypeArg) {
                    type = NavType.StringType
                    defaultValue = ChallengeType.ODD_ONE_OUT.name
                },
            ),
    ) { backStackEntry ->
        val rawType = backStackEntry.arguments?.getString(NavRoutes.WordChallengeTypeArg) ?: ChallengeType.ODD_ONE_OUT.name
        val challengeType =
            try {
                ChallengeType.valueOf(rawType)
            } catch (_: Throwable) {
                ChallengeType.ODD_ONE_OUT
            }
        val rewardEngine = remember(host.context) { RewardEngine.get(host.context.applicationContext) }
        WordChallengeScreen(
            onBackToStationSelect = { host.navController.backToStationSelect() },
            onRoundCompleteToSummary = {
                host.navController.navigate(NavRoutes.ChallengeSummary) {
                    popUpTo(NavRoutes.WordChallenge) { inclusive = true }
                    launchSingleTop = true
                }
            },
            challengeType = challengeType,
            rewardEngine = rewardEngine,
        )
    }

    composable(NavRoutes.ChallengeSummary) {
        val rewardEngine = remember(host.context) { RewardEngine.get(host.context.applicationContext) }
        ChallengeSummaryScreen(
            rewardEngine = rewardEngine,
            onBackToDinoHome = {
                host.navController.navigate(NavRoutes.Chapters) {
                    popUpTo(NavRoutes.Chapters) { inclusive = true }
                    launchSingleTop = true
                }
            },
        )
    }

    composable(NavRoutes.FallingLetters) {
        val rewardEngine = remember(host.context) { RewardEngine.get(host.context.applicationContext) }
        FallingLettersScreen(
            onBackToStationSelect = { host.navController.backToStationSelect() },
            onRoundCompleteToSummary = {
                host.navController.navigate(NavRoutes.ChallengeSummary) {
                    popUpTo(NavRoutes.FallingLetters) { inclusive = true }
                    launchSingleTop = true
                }
            },
            rewardEngine = rewardEngine,
        )
    }

    composable(NavRoutes.Opening) {
        val context = LocalContext.current
        OpeningScreen(
            onPlay = {
                host.navController.navigate(NavRoutes.IntroInstruction) {
                    launchSingleTop = true
                }
            },
            onOpenSettings = { host.navController.navigate(NavRoutes.Settings) },
            onExit = { (context as? android.app.Activity)?.finish() },
        )
    }

    composable(NavRoutes.IntroInstruction) {
        IntroInstructionScreen(
            onHatchComplete = {
                host.navController.navigate(NavRoutes.Chapters) {
                    popUpTo(NavRoutes.IntroInstruction) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onBack = { host.navController.popBackStack() },
        )
    }

    composable(NavRoutes.Seasons) {
        SeasonsScreen(
            onOpenSeason1 = {
                host.navController.navigate(NavRoutes.Chapters) { launchSingleTop = true }
            },
            onBackToOpening = {
                host.navController.navigate(NavRoutes.Opening) {
                    popUpTo(NavRoutes.Opening) { inclusive = false }
                    launchSingleTop = true
                }
            },
        )
    }

    composable(NavRoutes.Chapters) {
        val context = LocalContext.current
        val vm: DinoHomeViewModel =
            viewModel(
                factory = remember(context) { DinoHomeViewModel.Factory(context) },
            )
        DinoHomeScreen(
            viewModel = vm,
            onGoOnMission = {
                host.navController.navigate(NavRoutes.ChapterSelect) { launchSingleTop = true }
            },
            onBackToIntro = {
                host.navController.navigate(NavRoutes.IntroInstruction) {
                    popUpTo(NavRoutes.Chapters) { inclusive = true }
                    launchSingleTop = true
                }
            },
        )
    }

    composable(NavRoutes.ChapterSelect) {
        val context = LocalContext.current
        LaunchedEffect(Unit) {
            TextToSpeechManager.get(context.applicationContext).warmUp()
        }
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
            onOpenParents = { host.navController.navigate(NavRoutes.ParentalGate) { launchSingleTop = true } },
            onOpenWordChallengeStation = { stationId ->
                val type =
                    when (stationId) {
                        2 -> ChallengeType.RHYME
                        else -> ChallengeType.ODD_ONE_OUT
                    }
                host.navController.navigate(NavRoutes.wordChallengeRoute(type, chapterIndex = 0)) { launchSingleTop = true }
            },
            onOpenFallingLettersStation3 = {
                host.navController.navigate(NavRoutes.FallingLetters) { launchSingleTop = true }
            },
            onOpenChapter = { chapterId ->
                when (chapterId) {
                    1 -> {
                        // Always show chapter intro on entry (user request).
                        host.navController.navigate(NavRoutes.StoryIntro)
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

    composable(NavRoutes.ParentalGate) {
        val context = LocalContext.current
        val rewardEngine = remember(context) { RewardEngine.get(context.applicationContext) }
        ParentalGateScreen(
            onBack = { host.navController.popBackStack() },
            onResetProgress = {
                host.progress.resetAll()
                host.scope.launch {
                    CharacterRepository(context.applicationContext).resetForNewGame()
                    rewardEngine.resetAll()
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
                    host.prefs.setCharacter(DinoCharacter.DINO_GREEN)
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
            onBack = { host.navController.popBackStack() },
        )
    }
}
