package com.tal.hebrewdino.ui

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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.tal.hebrewdino.ui.audio.BackgroundMusicPlayer
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.data.AudioPrefs
import com.tal.hebrewdino.ui.data.DinoCharacter
import kotlinx.coroutines.launch

private val BackgroundMusicEligibleRoutes: Set<String> =
    setOf(
        NavRoutes.Opening,
        NavRoutes.Seasons,
        NavRoutes.Chapters,
        NavRoutes.Journey,
        NavRoutes.Ch2Journey,
        NavRoutes.Ch3Journey,
        NavRoutes.Ch4Journey,
        NavRoutes.Ch5Journey,
        NavRoutes.Ch6Journey,
    )

private const val BgmMenuAssetPath: String = "audio/bgm_menu.mp3"
private const val BgmSeason1AssetPath: String = "audio/bgm_season1.mp3"
private const val BgmSeason2AssetPath: String = "audio/bgm_season2.mp3"

private fun bgmAssetPathForRoute(route: String?): String =
    when (route) {
        NavRoutes.Opening, NavRoutes.Seasons -> BgmMenuAssetPath
        else -> if (route?.startsWith("s2_") == true) BgmSeason2AssetPath else BgmSeason1AssetPath
    }

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainViewModel: MainViewModel =
        viewModel(
            factory = remember(context) { MainViewModel.Factory(context) },
        )
    val prefs = mainViewModel.prefs
    val progress = mainViewModel.progress
    val scope = rememberCoroutineScope()
    val uiState by mainViewModel.uiState.collectAsState()
    val audioPrefs = remember(context) { AudioPrefs(context.applicationContext) }
    val backgroundMusicEnabled by audioPrefs.backgroundMusicEnabledFlow.collectAsState(initial = true)
    val bgMusic = remember { BackgroundMusicPlayer(context.applicationContext) }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val bgMusicEligible = currentRoute != null && currentRoute in BackgroundMusicEligibleRoutes

    LaunchedEffect(currentRoute, backgroundMusicEnabled) {
        if (!backgroundMusicEnabled) {
            bgMusic.stop()
            return@LaunchedEffect
        }
        bgMusic.playLoopFromAssets(assetPath = bgmAssetPathForRoute(currentRoute))
        bgMusic.setMuted(!bgMusicEligible)
    }

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                    bgMusic.stop()
                    VoicePlayer.stopAllNow()
                    SoundPoolPlayer.stopAllNow()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            bgMusic.release()
        }
    }

    val host =
        remember(
            uiState,
            navController,
            scope,
            context,
            progress,
            prefs,
        ) {
            AppNavHostState.from(
                uiState = uiState,
                navController = navController,
                scope = scope,
                context = context,
                progress = progress,
                prefs = prefs,
            )
        }

    LaunchedEffect(Unit) {
        progress.repairChapter2ProgressIfNeeded()
        progress.repairChapter3ProgressIfNeeded()
        progress.repairChapter4ProgressIfNeeded()
        progress.repairChapter5ProgressIfNeeded()
        progress.repairChapter6ProgressIfNeeded()
        prefs.setCharacter(DinoCharacter.Dino)
    }

    NavHost(navController = navController, startDestination = NavRoutes.Opening) {
        systemAndTrainingGraph(host)
        chapterOneToThreeGraph(host)
        chapterFourToSixGraph(host)
    }
}
