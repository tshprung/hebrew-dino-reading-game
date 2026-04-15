package com.tal.hebrewdino.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tal.hebrewdino.ui.data.CharacterPrefs
import com.tal.hebrewdino.ui.screens.CharacterSelectScreen
import com.tal.hebrewdino.ui.screens.LevelScreen
import com.tal.hebrewdino.ui.screens.MapScreen

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val prefs = CharacterPrefs(context)
    val character by prefs.characterFlow.collectAsState(initial = null)

    val startDestination = if (character == null) Routes.CharacterSelect else Routes.Map

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.CharacterSelect) {
            CharacterSelectScreen(
                onPick = { picked ->
                    prefs.setCharacter(picked)
                    navController.navigate(Routes.Map) {
                        popUpTo(Routes.CharacterSelect) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.Map) {
            MapScreen(
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
            )
        }
    }
}

private object Routes {
    const val CharacterSelect = "character_select"
    const val Map = "map"
    const val Level = "level"
}

