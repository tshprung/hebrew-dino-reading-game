package com.tal.hebrewdino.ui

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import com.tal.hebrewdino.ui.domain.Season2ChapterRegistry
import com.tal.hebrewdino.ui.domain.Season2NavKeys
import com.tal.hebrewdino.ui.screens.ChaptersScreen
import com.tal.hebrewdino.ui.screens.OnboardingCompanionScreen
import com.tal.hebrewdino.ui.screens.OnboardingPlayerAddressScreen
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
import kotlin.random.Random

internal fun NavGraphBuilder.systemAndTrainingGraph(host: AppNavHostState) {
    composable(NavRoutes.OnboardingCompanion) {
        OnboardingCompanionScreen(
            onNext = { character ->
                host.scope.launch {
                    host.prefs.setCharacter(character)
                    host.navController.navigate(NavRoutes.OnboardingPlayerAddress) {
                        popUpTo(NavRoutes.OnboardingCompanion) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            },
        )
    }

    composable(NavRoutes.OnboardingPlayerAddress) {
        OnboardingPlayerAddressScreen(
            onStart = { address ->
                host.scope.launch {
                    host.prefs.setPlayerAddress(address)
                    host.navController.navigate(NavRoutes.Seasons) {
                        popUpTo(NavRoutes.Opening) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            },
        )
    }

    composable(NavRoutes.Opening) {
        val context = LocalContext.current
        var showParentsGate by remember { mutableStateOf(false) }
        var parentsGateNonce by remember { mutableStateOf(0) }
        val challenge = remember(parentsGateNonce) { generateParentsGateChallenge() }
        OpeningScreen(
            onPlay = {
                when {
                    host.onboardingComplete -> {
                        host.navController.navigate(NavRoutes.Seasons) {
                            launchSingleTop = true
                        }
                    }
                    host.hasChosenCompanion -> {
                        host.navController.navigate(NavRoutes.OnboardingPlayerAddress) {
                            launchSingleTop = true
                        }
                    }
                    else -> {
                        host.navController.navigate(NavRoutes.OnboardingCompanion) {
                            launchSingleTop = true
                        }
                    }
                }
            },
            onOpenSettings = {
                parentsGateNonce++
                showParentsGate = true
            },
            onExit = { (context as? android.app.Activity)?.finish() },
        )
        if (showParentsGate) {
            ParentsGateDialog(
                challenge = challenge,
                onCancel = { showParentsGate = false },
                onSuccess = {
                    showParentsGate = false
                    host.navController.navigate(NavRoutes.Settings)
                },
            )
        }
    }

    composable(NavRoutes.Seasons) {
        SeasonsScreen(
            companionCharacter = host.companionCharacter,
            onOpenSeason1 = {
                host.navController.navigate(NavRoutes.Chapters) { launchSingleTop = true }
            },
            onOpenSeason2 =
                if (BuildConfig.DEBUG) {
                    {
                        host.navController.navigate(NavRoutes.Season2ChapterSelect) {
                            popUpTo(NavRoutes.Season2ChapterSelect) { inclusive = true }
                            launchSingleTop = true
                        }
                        host.navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set(Season2NavKeys.SHOW_SEASON_INTRO, true)
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

    composable(NavRoutes.Season2ChapterSelect) { backStackEntry ->
        val requestSeasonIntro by
            backStackEntry.savedStateHandle
                .getStateFlow(Season2NavKeys.SHOW_SEASON_INTRO, false)
                .collectAsState()
        Season2ChapterSelectScreen(
            companionCharacter = host.companionCharacter,
            requestSeasonIntro = requestSeasonIntro,
            onSeasonIntroConsumed = {
                backStackEntry.savedStateHandle[Season2NavKeys.SHOW_SEASON_INTRO] = false
            },
            onBack = { host.navController.popBackStack() },
            onOpenChapter = { chapterId ->
                if (!Season2ChapterRegistry.isPlayable(chapterId)) return@Season2ChapterSelectScreen
                host.navController.navigate(NavRoutes.season2PuzzleMapPrototype(chapterId)) {
                    launchSingleTop = true
                }
                host.navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.set(Season2NavKeys.SHOW_CHAPTER_INTRO, true)
            },
        )
    }

    composable(
        route = NavRoutes.Season2PuzzleMapPrototype,
        arguments = listOf(navArgument("chapterId") { type = NavType.IntType }),
    ) { backStackEntry ->
        val chapterId = backStackEntry.arguments?.getInt("chapterId") ?: 1
        if (!Season2ChapterRegistry.isPlayable(chapterId)) {
            host.navController.popBackStack()
            return@composable
        }
        val celebrateRequest by
            backStackEntry.savedStateHandle
                .getStateFlow(Season2NavKeys.REQUEST_CHAPTER_CELEBRATION, false)
                .collectAsState()
        val requestChapterIntro by
            backStackEntry.savedStateHandle
                .getStateFlow(Season2NavKeys.SHOW_CHAPTER_INTRO, false)
                .collectAsState()
        Season2PuzzleMapPrototypeScreen(
            chapterId = chapterId,
            companionCharacter = host.companionCharacter,
            playerAddress = host.playerAddress,
            onBack = { host.navController.popBackStack() },
            onOpenStation = { stationId ->
                host.navController.navigate(NavRoutes.season2ChapterStation(chapterId, stationId)) {
                    launchSingleTop = true
                }
            },
            requestChapterIntro = requestChapterIntro,
            onChapterIntroConsumed = {
                backStackEntry.savedStateHandle[Season2NavKeys.SHOW_CHAPTER_INTRO] = false
            },
            requestChapterCelebration = celebrateRequest,
            onChapterCelebrationConsumed = {
                backStackEntry.savedStateHandle[Season2NavKeys.REQUEST_CHAPTER_CELEBRATION] = false
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
        if (!Season2ChapterRegistry.isPlayable(chapterId)) {
            host.navController.popBackStack()
            return@composable
        }
        val stationId = backStackEntry.arguments?.getInt("stationId") ?: Season2Chapter1StationOrder.POP_BALLOONS
        Season2ChapterStationScreen(
            chapterId = chapterId,
            stationId = stationId,
            companionCharacter = host.companionCharacter,
            playerAddress = host.playerAddress,
            onBack = { host.navController.popBackStack() },
            onComplete = { requestChapterCelebration ->
                if (requestChapterCelebration) {
                    host.navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(Season2NavKeys.REQUEST_CHAPTER_CELEBRATION, true)
                }
                host.navController.popBackStack()
            },
        )
    }

    composable(NavRoutes.Chapters) {
        var showParentsGate by remember { mutableStateOf(false) }
        var parentsGateNonce by remember { mutableStateOf(0) }
        val challenge = remember(parentsGateNonce) { generateParentsGateChallenge() }
        ChaptersScreen(
            unlockedChapter = host.unlockedChapter,
            chapter4ComingSoon = host.chapter4ComingSoon,
            chapter5ComingSoon = host.chapter5ComingSoon,
            chapter6ComingSoon = host.chapter6ComingSoon,
            maxSelectableChapterId = host.maxSelectableChapterId,
            chaptersProgress = host.chaptersProgress,
            companionCharacter = host.companionCharacter,
            onBackToSeasons = {
                host.navController.navigate(NavRoutes.Seasons) {
                    popUpTo(NavRoutes.Seasons) { inclusive = false }
                    launchSingleTop = true
                }
            },
            onOpenSettings = {
                parentsGateNonce++
                showParentsGate = true
            },
            onOpenChapter = { chapterId ->
                when (chapterId) {
                    1 -> {
                        // Season 1 Ch.1: story/narrator intro, then companion intro, then chapter flow.
                        host.navController.navigate(NavRoutes.StoryIntro)
                    }
                    2 -> {
                        val canEnterChapter2 = host.beachOutroSeen || host.chapter1AllStationsComplete
                        if (canEnterChapter2) host.navController.navigate(NavRoutes.Ch2Intro)
                    }
                    3 -> {
                        if (host.chapter2Completed) {
                            host.navController.navigate(NavRoutes.Ch3Intro)
                        }
                    }
                    4 -> {
                        if (host.chapter3Completed) {
                            host.navController.navigate(NavRoutes.Ch4Intro)
                        }
                    }
                    5 -> {
                        if (host.chapter4Completed) {
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
        if (showParentsGate) {
            ParentsGateDialog(
                challenge = challenge,
                onCancel = { showParentsGate = false },
                onSuccess = {
                    showParentsGate = false
                    host.navController.navigate(NavRoutes.Settings)
                },
            )
        }
    }

    composable(NavRoutes.TrainingIntro) {
        TrainingV1IntroScreen(
            companionCharacter = host.companionCharacter,
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
            companionCharacter = host.companionCharacter,
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
            playerAddress = host.playerAddress,
        )
    }

    composable(NavRoutes.TrainingComplete) {
        TrainingV1CompleteScreen(
            companionCharacter = host.companionCharacter,
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
                    host.progress.fullGameReset()
                    host.navController.navigate(NavRoutes.Opening) {
                        popUpTo(host.navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            },
            companionCharacter = host.companionCharacter,
            playerAddress = host.playerAddress,
            onCompanionCharacterChange = { character ->
                host.scope.launch { host.prefs.setCharacter(character) }
            },
            onPlayerAddressChange = { address ->
                host.scope.launch { host.prefs.setPlayerAddress(address) }
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

private enum class ParentsGateOp {
    Plus,
    Minus,
}

private data class ParentsGateChallenge(
    val a: Int,
    val b: Int,
    val op: ParentsGateOp,
) {
    val answer: Int =
        when (op) {
            ParentsGateOp.Plus -> a + b
            ParentsGateOp.Minus -> a - b
        }

    val display: String =
        when (op) {
            ParentsGateOp.Plus -> "\u200E$a + $b = ?\u200E"
            ParentsGateOp.Minus -> "\u200E$a - $b = ?\u200E"
        }
}

private fun generateParentsGateChallenge(): ParentsGateChallenge {
    return if (Random.nextBoolean()) {
        ParentsGateChallenge(
            a = Random.nextInt(2, 10),
            b = Random.nextInt(2, 10),
            op = ParentsGateOp.Plus,
        )
    } else {
        val a = Random.nextInt(4, 10)
        val b = Random.nextInt(2, a)
        ParentsGateChallenge(a = a, b = b, op = ParentsGateOp.Minus)
    }
}

@androidx.compose.runtime.Composable
private fun ParentsGateDialog(
    challenge: ParentsGateChallenge,
    onCancel: () -> Unit,
    onSuccess: () -> Unit,
) {
    var input by remember(challenge) { mutableStateOf("") }
    var showError by remember(challenge) { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(text = "אזור הורים") },
        text = {
            Column {
                Text(text = "כדי להמשיך, פתרו תרגיל קצר:")
                Text(
                    text = challenge.display,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 10.dp),
                )
                OutlinedTextField(
                    value = input,
                    onValueChange = { v ->
                        input = v.filter { it.isDigit() }.take(3)
                        if (showError) showError = false
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("תשובה") },
                    modifier = Modifier.padding(top = 10.dp),
                )
                if (showError) {
                    Text(
                        text = "נסו שוב",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val parsed = input.toIntOrNull()
                    if (parsed == challenge.answer) {
                        onSuccess()
                    } else {
                        showError = true
                    }
                },
            ) {
                Text("כניסה")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("ביטול")
            }
        },
    )
}
