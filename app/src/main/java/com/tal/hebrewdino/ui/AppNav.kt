package com.tal.hebrewdino.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.firebase.analytics.FirebaseAnalytics
import com.tal.hebrewdino.ui.audio.BackgroundMusicPlayer
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.data.AudioPrefs
import com.tal.hebrewdino.ui.data.CharacterRepository
import com.tal.hebrewdino.ui.data.StoryNarrationPrefs
import com.tal.hebrewdino.ui.economy.RewardEngine
import com.tal.hebrewdino.ui.data.CharacterPrefs
import com.tal.hebrewdino.ui.domain.ChallengeType
import com.tal.hebrewdino.ui.domain.HebrewSyllabus
import com.tal.hebrewdino.ui.screens.CharacterSelectionScreen
import com.tal.hebrewdino.ui.screens.ChallengeSummaryScreen
import com.tal.hebrewdino.ui.screens.DinoHomeScreen
import com.tal.hebrewdino.ui.screens.DinoHomeViewModel
import com.tal.hebrewdino.ui.screens.FallingLettersScreen
import com.tal.hebrewdino.ui.screens.IntroInstructionScreen
import com.tal.hebrewdino.ui.screens.OpeningScreen
import com.tal.hebrewdino.ui.screens.ParentalGateScreen
import com.tal.hebrewdino.ui.screens.StationSelectScreen
import com.tal.hebrewdino.ui.screens.WordChallengeScreen
import com.tal.hebrewdino.ui.audio.TextToSpeechManager
import kotlinx.coroutines.launch

internal object AppAnalytics {
    private var analytics: FirebaseAnalytics? = null

    fun init(context: Context) {
        if (analytics != null) return
        val appContext = context.applicationContext
        analytics = FirebaseAnalytics.getInstance(appContext).apply { setUserId(null) }
    }

    private fun levelName(
        chapterId: Int,
        stationId: Int,
    ): String = "ch${chapterId}_st${stationId}"

    fun logLevelStart(
        chapterId: Int,
        stationId: Int,
    ) {
        val a = analytics ?: return
        val b = Bundle(1)
        b.putString("level_name", levelName(chapterId, stationId))
        a.logEvent("level_start", b)
    }

    fun logLevelComplete(
        chapterId: Int,
        stationId: Int,
        timeTakenSeconds: Long,
    ) {
        val a = analytics ?: return
        val b = Bundle(2)
        b.putString("level_name", levelName(chapterId, stationId))
        b.putLong("time_taken_seconds", timeTakenSeconds.coerceAtLeast(0L))
        a.logEvent("level_complete", b)
    }

    fun logLevelRetry(
        chapterId: Int,
        stationId: Int,
        mistakeType: String,
    ) {
        val a = analytics ?: return
        val b = Bundle(2)
        b.putString("level_name", levelName(chapterId, stationId))
        b.putString("mistake_type", mistakeType)
        a.logEvent("level_retry", b)
    }
}

private val BackgroundMusicEligibleRoutes: Set<String> =
    setOf(
        NavRoutes.Opening,
        NavRoutes.IntroInstruction,
        NavRoutes.Chapters,
        NavRoutes.ChapterSelect,
    )

private const val BgmMenuAssetPath: String = "audio/bgm_menu.mp3"
private const val BgmSeason1AssetPath: String = "audio/bgm_season1.mp3"

private fun bgmAssetPathForRoute(route: String?): String =
    when (route) {
        NavRoutes.Opening,
        NavRoutes.IntroInstruction,
        NavRoutes.Chapters,
        NavRoutes.ChapterSelect,
        -> BgmMenuAssetPath
        else -> BgmSeason1AssetPath
    }

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val characterRepo = remember(context) { CharacterRepository(context.applicationContext) }
    val rewardEngine = remember(context) { RewardEngine.get(context.applicationContext) }
    val progressPrefs = remember(context) { com.tal.hebrewdino.ui.data.ProgressPrefs(context.applicationContext) }
    val characterPrefs = remember(context) { CharacterPrefs(context.applicationContext) }
    val characterChosen by characterPrefs.characterSelectedOnceFlow.collectAsState(initial = false)
    val audioPrefs = remember(context) { AudioPrefs(context.applicationContext) }
    val backgroundMusicEnabled by audioPrefs.backgroundMusicEnabledFlow.collectAsState(initial = true)
    val bgMusic = remember { BackgroundMusicPlayer(context.applicationContext) }
    val tts = remember(context) { TextToSpeechManager.get(context.applicationContext) }
    val isTtsSpeaking by tts.isSpeaking.collectAsState(initial = false)
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val bgMusicEligible = currentRoute != null && currentRoute in BackgroundMusicEligibleRoutes

    LaunchedEffect(currentRoute, backgroundMusicEnabled, isTtsSpeaking) {
        if (!backgroundMusicEnabled) {
            bgMusic.stop()
            return@LaunchedEffect
        }
        bgMusic.playLoopFromAssets(assetPath = bgmAssetPathForRoute(currentRoute))
        bgMusic.setMuted(!bgMusicEligible)
        bgMusic.setDucked(isTtsSpeaking && bgMusicEligible)
    }

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                    bgMusic.stop()
                    VoicePlayer.stopAllNow()
                    SoundPoolPlayer.stopAllNow()
                    TextToSpeechManager.get(context.applicationContext).stop()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            bgMusic.release()
        }
    }

    NavHost(navController = navController, startDestination = NavRoutes.Opening) {
        composable(NavRoutes.Opening) {
            OpeningScreen(
                onPlay = {
                    if (characterChosen) {
                        navController.navigate(NavRoutes.IntroInstruction) { launchSingleTop = true }
                    }
                },
                onOpenSettings = { navController.navigate(NavRoutes.ParentalGate) { launchSingleTop = true } },
                onExit = { (context as? ComponentActivity)?.finish() },
            )
        }

        composable(NavRoutes.IntroInstruction) {
            IntroInstructionScreen(
                onContinue = {
                    navController.navigate(NavRoutes.Chapters) {
                        popUpTo(NavRoutes.IntroInstruction) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(NavRoutes.Chapters) {
            val vm: DinoHomeViewModel =
                androidx.lifecycle.viewmodel.compose.viewModel(
                    factory =
                        remember(characterRepo, context) {
                            DinoHomeViewModel.Factory(
                                characterRepo,
                                StoryNarrationPrefs(context.applicationContext),
                            )
                        },
                )
            DinoHomeScreen(
                viewModel = vm,
                onGoOnMission = { navController.navigate(NavRoutes.ChapterSelect) { launchSingleTop = true } },
                onBackToIntro = {
                    navController.navigate(NavRoutes.IntroInstruction) {
                        popUpTo(NavRoutes.Chapters) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(NavRoutes.ChapterSelect) {
            LaunchedEffect(Unit) {
                TextToSpeechManager.get(context.applicationContext).warmUp()
            }
            val activeChapter by characterRepo.activeChapterIndexFlow.collectAsState(initial = 0)
            StationSelectScreen(
                onBackToDinoHome = { navController.popBackStack() },
                onOpenParents = { navController.navigate(NavRoutes.ParentalGate) { launchSingleTop = true } },
                onOpenWordChallengeStation = { stationId ->
                    val type =
                        when (stationId) {
                            1 -> ChallengeType.LETTER_RECOGNITION
                            2 -> ChallengeType.PHONEMIC_ISOLATION
                            else -> ChallengeType.ODD_ONE_OUT
                        }
                    navController.navigate(NavRoutes.wordChallengeRoute(type, activeChapter)) { launchSingleTop = true }
                },
                onOpenFallingLettersStation3 = {
                    navController.navigate(NavRoutes.fallingLettersRoute(activeChapter)) { launchSingleTop = true }
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
                    navArgument(NavRoutes.ChapterIndexArg) {
                        type = NavType.IntType
                        defaultValue = 0
                    },
                ),
        ) { backStackEntry ->
            val rawType = backStackEntry.arguments?.getString(NavRoutes.WordChallengeTypeArg) ?: ChallengeType.ODD_ONE_OUT.name
            val chapterIndex = backStackEntry.arguments?.getInt(NavRoutes.ChapterIndexArg) ?: 0
            val challengeType =
                try {
                    ChallengeType.valueOf(rawType)
                } catch (_: Throwable) {
                    ChallengeType.ODD_ONE_OUT
                }
            WordChallengeScreen(
                onExitToHome = {
                    navController.navigate(NavRoutes.Chapters) {
                        popUpTo(NavRoutes.Chapters) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onRoundCompleteToSummary = {
                    navController.navigate(NavRoutes.ChallengeSummary) {
                        popUpTo(NavRoutes.WordChallenge) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                challengeType = challengeType,
                chapterIndex = chapterIndex.coerceIn(0, HebrewSyllabus.chapterCount - 1),
                rewardEngine = rewardEngine,
            )
        }

        composable(
            route = NavRoutes.FallingLetters,
            arguments =
                listOf(
                    navArgument(NavRoutes.ChapterIndexArg) {
                        type = NavType.IntType
                        defaultValue = 0
                    },
                ),
        ) { backStackEntry ->
            val chapterIndex = backStackEntry.arguments?.getInt(NavRoutes.ChapterIndexArg) ?: 0
            FallingLettersScreen(
                chapterIndex = chapterIndex.coerceIn(0, HebrewSyllabus.chapterCount - 1),
                onExitToHome = {
                    navController.navigate(NavRoutes.Chapters) {
                        popUpTo(NavRoutes.Chapters) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onRoundCompleteToSummary = {
                    navController.navigate(NavRoutes.ChallengeSummary) {
                        popUpTo(NavRoutes.FallingLetters) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                rewardEngine = rewardEngine,
            )
        }

        composable(NavRoutes.ChallengeSummary) {
            ChallengeSummaryScreen(
                rewardEngine = rewardEngine,
                onBackToDinoHome = {
                    navController.navigate(NavRoutes.Chapters) {
                        popUpTo(NavRoutes.Chapters) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(NavRoutes.ParentalGate) {
            ParentalGateScreen(
                onBack = { navController.popBackStack() },
                onResetProgress = {
                    progressPrefs.resetAll()
                    scope.launch {
                        characterRepo.resetForNewGame()
                        rewardEngine.resetAll()
                        StoryNarrationPrefs(context.applicationContext).reset()
                    }
                    characterPrefs.resetCharacterSelection()
                },
            )
        }
    }

    if (!characterChosen) {
        CharacterSelectionScreen(
            onSelect = { chosen ->
                scope.launch {
                    characterPrefs.setCharacter(chosen)
                    navController.navigate(NavRoutes.IntroInstruction) {
                        popUpTo(NavRoutes.Opening) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            },
        )
    }
}
