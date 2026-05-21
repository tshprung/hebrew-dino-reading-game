package com.tal.hebrewdino.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.tal.hebrewdino.ui.data.DinoCharacter
import kotlinx.coroutines.launch

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val mainViewModel: MainViewModel =
        viewModel(
            factory = remember(context) { MainViewModel.Factory(context) },
        )
    val prefs = mainViewModel.prefs
    val progress = mainViewModel.progress
    val scope = rememberCoroutineScope()
    val uiState by mainViewModel.uiState.collectAsState()

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
