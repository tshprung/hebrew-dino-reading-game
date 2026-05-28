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

    val completedChaptersFlow: Flow<Set<Int>> =
        context.dataStore.data.map { prefs ->
            val raw = prefs[completedChaptersKey].orEmpty()
            if (raw.isBlank()) return@map emptySet()
            raw.split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .filter { it in 1..6 }
                .toSet()
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
        }
    }
}

