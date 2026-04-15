package com.tal.hebrewdino.ui.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProgressPrefs(private val context: Context) {
    private val unlockedLevelKey: Preferences.Key<Int> = intPreferencesKey("unlocked_level")
    private val completedLevelsKey: Preferences.Key<String> = androidx.datastore.preferences.core.stringPreferencesKey("completed_levels")

    val unlockedLevelFlow: Flow<Int> =
        context.dataStore.data.map { prefs ->
            prefs[unlockedLevelKey] ?: 1
        }

    val completedLevelsFlow: Flow<Set<Int>> =
        context.dataStore.data.map { prefs ->
            val raw = prefs[completedLevelsKey].orEmpty()
            if (raw.isBlank()) return@map emptySet()
            raw.split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .toSet()
        }

    suspend fun unlockAtLeast(levelId: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[unlockedLevelKey] ?: 1
            if (levelId > current) {
                prefs[unlockedLevelKey] = levelId
            }
        }
    }

    suspend fun markCompleted(levelId: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[completedLevelsKey].orEmpty()
            val set =
                current.split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .toMutableSet()
            set.add(levelId)
            prefs[completedLevelsKey] = set.toList().sorted().joinToString(",")
        }
    }
}

