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
import com.tal.hebrewdino.ui.data.ProgressPrefs
import com.tal.hebrewdino.ui.screens.CharacterSelectScreen
import com.tal.hebrewdino.ui.screens.LevelScreen
import com.tal.hebrewdino.ui.screens.MapScreen
import com.tal.hebrewdino.ui.screens.RewardScreen
import kotlinx.coroutines.launch

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val prefs = CharacterPrefs(context)
    val progress = ProgressPrefs(context)
    val scope = rememberCoroutineScope()
    val character by prefs.characterFlow.collectAsState(initial = null)
    val unlockedLevel by progress.unlockedLevelFlow.collectAsState(initial = 1)

    val startDestination = if (character == null) Routes.CharacterSelect else Routes.Map

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.CharacterSelect) {
            CharacterSelectScreen(
                onPick = { picked ->
                    scope.launch {
                        prefs.setCharacter(picked)
                        navController.navigate(Routes.Map) {
                            popUpTo(Routes.CharacterSelect) { inclusive = true }
                        }
                    }
                },
            )
        }

        composable(Routes.Map) {
            MapScreen(
                unlockedLevel = unlockedLevel,
                onPlayLevel = { levelId ->
                    navController.navigate("${Routes.Level}/$levelId")
                },
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
                    navController.navigate(Routes.Map) {
                        popUpTo(Routes.Map) { inclusive = true }
                    }
                },
            )
        }
    }
}

private object Routes {
    const val CharacterSelect = "character_select"
    const val Map = "map"
    const val Level = "level"
    const val Reward = "reward"
}

