package com.tal.hebrewdino.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tal.hebrewdino.ui.data.CharacterPrefs
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.data.ProgressPrefs
import com.tal.hebrewdino.ui.screens.BeachIntroScreen
import com.tal.hebrewdino.ui.screens.BeachOutroScreen
import com.tal.hebrewdino.ui.screens.CharacterSelectScreen
import com.tal.hebrewdino.ui.screens.LevelScreen
import com.tal.hebrewdino.ui.screens.MapScreen
import com.tal.hebrewdino.ui.screens.RewardScreen
import com.tal.hebrewdino.ui.screens.SettingsScreen
import kotlinx.coroutines.launch

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val prefs = CharacterPrefs(context)
    val progress = ProgressPrefs(context)
    val scope = rememberCoroutineScope()
    val character by prefs.characterFlow.collectAsState(initial = null)
    val beachIntroSeen by progress.beachIntroSeenFlow.collectAsState(initial = false)
    val beachOutroSeen by progress.beachOutroSeenFlow.collectAsState(initial = false)
    val unlockedLevel by progress.unlockedLevelFlow.collectAsState(initial = 1)
    val completedLevels by progress.completedLevelsFlow.collectAsState(initial = emptySet())

    val startDestination =
        when {
            character == null -> Routes.CharacterSelect
            !beachIntroSeen -> Routes.StoryIntro
            else -> Routes.Map
        }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.CharacterSelect) {
            CharacterSelectScreen(
                onPick = { picked ->
                    scope.launch {
                        prefs.setCharacter(picked)
                        navController.navigate(if (beachIntroSeen) Routes.Map else Routes.StoryIntro) {
                            popUpTo(Routes.CharacterSelect) { inclusive = true }
                        }
                    }
                },
            )
        }

        composable(Routes.StoryIntro) {
            val c = character ?: DinoCharacter.Dino
            BeachIntroScreen(
                character = c,
                onContinue = {
                    scope.launch { progress.markBeachIntroSeen() }
                    navController.navigate(Routes.Map) {
                        popUpTo(Routes.CharacterSelect) { inclusive = true }
                    }
                },
                onSkip = {
                    scope.launch { progress.markBeachIntroSeen() }
                    navController.navigate(Routes.Map) {
                        popUpTo(Routes.CharacterSelect) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.Map) {
            MapScreen(
                unlockedLevel = unlockedLevel,
                completedLevels = completedLevels,
                onPlayLevel = { levelId ->
                    navController.navigate("${Routes.Level}/$levelId")
                },
                onOpenSettings = { navController.navigate(Routes.Settings) },
            )
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
                        progress.unlockAtLeast(completedLevelId + 1)
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
                    val isChapterEnd = levelId >= 10
                    if (isChapterEnd && !beachOutroSeen) {
                        navController.navigate(Routes.StoryOutro) {
                            popUpTo(Routes.Map) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Routes.Map) {
                            popUpTo(Routes.Map) { inclusive = true }
                        }
                    }
                },
            )
        }

        composable(Routes.StoryOutro) {
            val c = character ?: DinoCharacter.Dino
            BeachOutroScreen(
                character = c,
                onContinue = {
                    scope.launch { progress.markBeachOutroSeen() }
                    navController.navigate(Routes.Map) {
                        popUpTo(Routes.Map) { inclusive = true }
                    }
                },
                onSkip = {
                    scope.launch { progress.markBeachOutroSeen() }
                    navController.navigate(Routes.Map) {
                        popUpTo(Routes.Map) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.Settings) {
            SettingsScreen(
                selectedCharacter = character,
                onPick = { picked: DinoCharacter ->
                    scope.launch { prefs.setCharacter(picked) }
                },
                onResetAll = {
                    scope.launch {
                        progress.resetAll()
                        prefs.clearCharacter()
                        navController.navigate(Routes.CharacterSelect) {
                            popUpTo(Routes.Map) { inclusive = true }
                        }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
    }
}

private object Routes {
    const val CharacterSelect = "character_select"
    const val StoryIntro = "story_intro"
    const val Map = "map"
    const val Level = "level"
    const val Reward = "reward"
    const val StoryOutro = "story_outro"
    const val Settings = "settings"
}

