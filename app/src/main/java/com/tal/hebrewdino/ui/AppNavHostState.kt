package com.tal.hebrewdino.ui

import android.content.Context
import androidx.navigation.NavHostController
import com.tal.hebrewdino.ui.data.CharacterPrefs
import com.tal.hebrewdino.ui.data.ProgressPrefs
import com.tal.hebrewdino.ui.domain.Chapter1Config
import com.tal.hebrewdino.ui.domain.Chapter2Config
import com.tal.hebrewdino.ui.domain.Chapter3Config
import com.tal.hebrewdino.ui.domain.Chapter4Config
import com.tal.hebrewdino.ui.domain.Chapter5Config
import com.tal.hebrewdino.ui.domain.Chapter6Config
import com.tal.hebrewdino.ui.domain.CollectedEggs
import com.tal.hebrewdino.ui.screens.ChaptersProgress
import kotlinx.coroutines.CoroutineScope

internal data class AppNavHostState(
    val navController: NavHostController,
    val scope: CoroutineScope,
    val context: Context,
    val progress: ProgressPrefs,
    val prefs: CharacterPrefs,
    val beachOutroSeen: Boolean,
    val chapter1MidBoostSeen: Boolean,
    val chapter2MidBoostSeen: Boolean,
    val chapter2UnlockedStation: Int,
    val chapter2CompletedStations: Set<Int>,
    val chapter2Completed: Boolean,
    val chapter3MidBoostSeen: Boolean,
    val chapter3UnlockedStation: Int,
    val chapter3CompletedStations: Set<Int>,
    val chapter3Completed: Boolean,
    val chapter4MidBoostSeen: Boolean,
    val chapter4UnlockedStation: Int,
    val chapter4CompletedStations: Set<Int>,
    val chapter4Completed: Boolean,
    val chapter5MidBoostSeen: Boolean,
    val chapter5UnlockedStation: Int,
    val chapter5CompletedStations: Set<Int>,
    val chapter5Completed: Boolean,
    val chapter6MidBoostSeen: Boolean,
    val chapter6UnlockedStation: Int,
    val chapter6CompletedStations: Set<Int>,
    val chapter6Completed: Boolean,
    val unlockedLevel: Int,
    val completedLevels: Set<Int>,
    val chapter1AllStationsComplete: Boolean,
    val chapter2AllStationsComplete: Boolean,
    val chapter3AllStationsComplete: Boolean,
    val chapter4AllStationsComplete: Boolean,
    val chapter5AllStationsComplete: Boolean,
    val chapter6AllStationsComplete: Boolean,
    val chapter1ProgressForStrip: Boolean,
    val collectedEggStripCount: Int,
    val unlockedChapter: Int,
    val chapter4ComingSoon: Boolean,
    val chapter5ComingSoon: Boolean,
    val chapter6ComingSoon: Boolean,
    val maxSelectableChapterId: Int,
) {
    val chaptersProgress: ChaptersProgress
        get() =
            ChaptersProgress(
                chapter1Completed = chapter1ProgressForStrip,
                chapter2Completed = chapter2Completed,
                chapter3Completed = chapter3Completed,
                chapter4Completed = chapter4Completed,
                chapter5Completed = chapter5Completed,
                chapter6Completed = chapter6Completed,
            )

    companion object {
        internal fun deriveChapterFlags(uiState: MainUiState): AppNavChapterFlags {
            val beachOutroSeen = uiState.beachOutroSeen
            val chapter1AllStationsComplete =
                (1..Chapter1Config.STATION_COUNT).all { station -> uiState.completedLevels.contains(station) }
            val chapter2AllStationsComplete =
                (1..Chapter2Config.STATION_COUNT).all { station -> uiState.chapter2CompletedStations.contains(station) }
            val chapter3AllStationsComplete =
                (1..Chapter3Config.STATION_COUNT).all { station -> uiState.chapter3CompletedStations.contains(station) }
            val chapter4AllStationsComplete =
                (1..Chapter4Config.STATION_COUNT).all { station -> uiState.chapter4CompletedStations.contains(station) }
            val chapter5AllStationsComplete =
                (1..Chapter5Config.STATION_COUNT).all { station -> uiState.chapter5CompletedStations.contains(station) }
            val chapter6AllStationsComplete =
                (1..Chapter6Config.STATION_COUNT).all { station -> uiState.chapter6CompletedStations.contains(station) }
            val chapter1ProgressForStrip = beachOutroSeen || chapter1AllStationsComplete
            val collectedEggStripCount =
                CollectedEggs.stripCount(
                    beachOutroSeen = chapter1ProgressForStrip,
                    chapter3Completed = uiState.chapter3Completed,
                    chapter5Completed = uiState.chapter5Completed,
                )
            val unlockedChapter =
                when {
                    uiState.chapter6Completed -> 6
                    uiState.chapter5Completed -> 6
                    uiState.chapter4Completed -> 5
                    uiState.chapter3Completed -> 4
                    uiState.chapter2Completed -> 3
                    beachOutroSeen || chapter1AllStationsComplete -> 2
                    else -> 1
                }
            return AppNavChapterFlags(
                beachOutroSeen = beachOutroSeen,
                chapter1AllStationsComplete = chapter1AllStationsComplete,
                chapter2AllStationsComplete = chapter2AllStationsComplete,
                chapter3AllStationsComplete = chapter3AllStationsComplete,
                chapter4AllStationsComplete = chapter4AllStationsComplete,
                chapter5AllStationsComplete = chapter5AllStationsComplete,
                chapter6AllStationsComplete = chapter6AllStationsComplete,
                chapter1ProgressForStrip = chapter1ProgressForStrip,
                collectedEggStripCount = collectedEggStripCount,
                unlockedChapter = unlockedChapter,
                chapter4ComingSoon = !uiState.chapter3Completed,
                chapter5ComingSoon = !uiState.chapter4Completed,
                chapter6ComingSoon = !uiState.chapter5Completed,
                maxSelectableChapterId =
                    when {
                        uiState.chapter5Completed -> 6
                        uiState.chapter4Completed -> 5
                        uiState.chapter3Completed -> 4
                        else -> 3
                    },
            )
        }

        fun from(
            uiState: MainUiState,
            navController: NavHostController,
            scope: CoroutineScope,
            context: Context,
            progress: ProgressPrefs,
            prefs: CharacterPrefs,
        ): AppNavHostState {
            val flags = deriveChapterFlags(uiState)
            return AppNavHostState(
                navController = navController,
                scope = scope,
                context = context,
                progress = progress,
                prefs = prefs,
                beachOutroSeen = flags.beachOutroSeen,
                chapter1MidBoostSeen = uiState.chapter1MidBoostSeen,
                chapter2MidBoostSeen = uiState.chapter2MidBoostSeen,
                chapter2UnlockedStation = uiState.chapter2UnlockedStation,
                chapter2CompletedStations = uiState.chapter2CompletedStations,
                chapter2Completed = uiState.chapter2Completed,
                chapter3MidBoostSeen = uiState.chapter3MidBoostSeen,
                chapter3UnlockedStation = uiState.chapter3UnlockedStation,
                chapter3CompletedStations = uiState.chapter3CompletedStations,
                chapter3Completed = uiState.chapter3Completed,
                chapter4MidBoostSeen = uiState.chapter4MidBoostSeen,
                chapter4UnlockedStation = uiState.chapter4UnlockedStation,
                chapter4CompletedStations = uiState.chapter4CompletedStations,
                chapter4Completed = uiState.chapter4Completed,
                chapter5MidBoostSeen = uiState.chapter5MidBoostSeen,
                chapter5UnlockedStation = uiState.chapter5UnlockedStation,
                chapter5CompletedStations = uiState.chapter5CompletedStations,
                chapter5Completed = uiState.chapter5Completed,
                chapter6MidBoostSeen = uiState.chapter6MidBoostSeen,
                chapter6UnlockedStation = uiState.chapter6UnlockedStation,
                chapter6CompletedStations = uiState.chapter6CompletedStations,
                chapter6Completed = uiState.chapter6Completed,
                unlockedLevel = uiState.unlockedLevel,
                completedLevels = uiState.completedLevels,
                chapter1AllStationsComplete = flags.chapter1AllStationsComplete,
                chapter2AllStationsComplete = flags.chapter2AllStationsComplete,
                chapter3AllStationsComplete = flags.chapter3AllStationsComplete,
                chapter4AllStationsComplete = flags.chapter4AllStationsComplete,
                chapter5AllStationsComplete = flags.chapter5AllStationsComplete,
                chapter6AllStationsComplete = flags.chapter6AllStationsComplete,
                chapter1ProgressForStrip = flags.chapter1ProgressForStrip,
                collectedEggStripCount = flags.collectedEggStripCount,
                unlockedChapter = flags.unlockedChapter,
                chapter4ComingSoon = flags.chapter4ComingSoon,
                chapter5ComingSoon = flags.chapter5ComingSoon,
                chapter6ComingSoon = flags.chapter6ComingSoon,
                maxSelectableChapterId = flags.maxSelectableChapterId,
            )
        }
    }
}

internal data class AppNavChapterFlags(
    val beachOutroSeen: Boolean,
    val chapter1AllStationsComplete: Boolean,
    val chapter2AllStationsComplete: Boolean,
    val chapter3AllStationsComplete: Boolean,
    val chapter4AllStationsComplete: Boolean,
    val chapter5AllStationsComplete: Boolean,
    val chapter6AllStationsComplete: Boolean,
    val chapter1ProgressForStrip: Boolean,
    val collectedEggStripCount: Int,
    val unlockedChapter: Int,
    val chapter4ComingSoon: Boolean,
    val chapter5ComingSoon: Boolean,
    val chapter6ComingSoon: Boolean,
    val maxSelectableChapterId: Int,
)
