package com.tal.hebrewdino.ui.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Season 2 UX-only progress.
 *
 * We only need to know which dinosaur chapters (1..6) are fully completed so the chapter-select
 * screen can highlight the next available dinosaur.
 */
class Season2ProgressPrefs(private val context: Context) {
    private val completedChaptersKey: Preferences.Key<String> =
        stringPreferencesKey("season2_completed_chapters")

    private fun completedStationsKeyForChapter(chapterId: Int): Preferences.Key<String> =
        stringPreferencesKey("season2_ch${chapterId}_completed_stations")

    private fun introDismissedKeyForChapter(chapterId: Int): Preferences.Key<String> =
        stringPreferencesKey("season2_ch${chapterId}_intro_dismissed")

    private fun puzzleMapExplainHeardKeyForChapter(chapterId: Int): Preferences.Key<String> =
        stringPreferencesKey("season2_ch${chapterId}_puzzle_map_explain_heard")

    private val seasonIntroDismissedKey: Preferences.Key<String> =
        stringPreferencesKey("season2_season_intro_dismissed")

    val completedChaptersFlow: Flow<Set<Int>> =
        context.dataStore.data.map { prefs ->
            val raw = prefs[completedChaptersKey].orEmpty()
            if (raw.isBlank()) return@map emptySet()
            raw.split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .filter { it in 1..6 }
                .toSet()
        }

    fun completedStationsFlow(chapterId: Int): Flow<Set<Int>> =
        context.dataStore.data.map { prefs ->
            if (chapterId !in 1..6) return@map emptySet()
            val raw = prefs[completedStationsKeyForChapter(chapterId)].orEmpty()
            if (raw.isBlank()) return@map emptySet()
            raw.split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .filter { it in 1..6 }
                .toSet()
        }

    suspend fun markStationCompleted(chapterId: Int, stationId: Int) {
        if (chapterId !in 1..6) return
        if (stationId !in 1..6) return
        val key = completedStationsKeyForChapter(chapterId)
        context.dataStore.edit { prefs ->
            val raw = prefs[key].orEmpty()
            val set =
                raw.split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .filter { it in 1..6 }
                    .toMutableSet()
            set.add(stationId)
            prefs[key] = set.toList().sorted().joinToString(",")
        }
    }

    val seasonIntroDismissedFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[seasonIntroDismissedKey] == "1"
        }

    suspend fun markSeasonIntroDismissed() {
        context.dataStore.edit { prefs ->
            prefs[seasonIntroDismissedKey] = "1"
        }
    }

    fun introDismissedFlow(chapterId: Int): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            if (chapterId !in 1..6) return@map false
            prefs[introDismissedKeyForChapter(chapterId)] == "1"
        }

    suspend fun markIntroDismissed(chapterId: Int) {
        if (chapterId !in 1..6) return
        context.dataStore.edit { prefs ->
            prefs[introDismissedKeyForChapter(chapterId)] = "1"
        }
    }

    fun puzzleMapExplainHeardFlow(chapterId: Int): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            if (chapterId !in 1..6) return@map false
            prefs[puzzleMapExplainHeardKeyForChapter(chapterId)] == "1"
        }

    suspend fun markPuzzleMapExplainHeard(chapterId: Int) {
        if (chapterId !in 1..6) return
        context.dataStore.edit { prefs ->
            prefs[puzzleMapExplainHeardKeyForChapter(chapterId)] = "1"
        }
    }

    suspend fun markChapterCompleted(chapterId: Int) {
        if (chapterId !in 1..6) return
        context.dataStore.edit { prefs ->
            val raw = prefs[completedChaptersKey].orEmpty()
            val set =
                raw.split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .filter { it in 1..6 }
                    .toMutableSet()
            set.add(chapterId)
            prefs[completedChaptersKey] = set.toList().sorted().joinToString(",")
        }
    }

    suspend fun resetSeason2() {
        context.dataStore.edit { prefs ->
            prefs[completedChaptersKey] = ""
            prefs.remove(seasonIntroDismissedKey)
            for (ch in 1..6) {
                prefs[completedStationsKeyForChapter(ch)] = ""
                prefs.remove(introDismissedKeyForChapter(ch))
                prefs.remove(puzzleMapExplainHeardKeyForChapter(ch))
            }
        }
    }
}

