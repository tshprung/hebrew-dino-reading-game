package com.tal.hebrewdino.ui.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProgressPrefs(private val context: Context) {
    private val unlockedLevelKey: Preferences.Key<Int> = intPreferencesKey("unlocked_level")
    private val completedLevelsKey: Preferences.Key<String> = androidx.datastore.preferences.core.stringPreferencesKey("completed_levels")
    private val beachIntroSeenKey: Preferences.Key<Boolean> = booleanPreferencesKey("beach_intro_seen")
    private val beachOutroSeenKey: Preferences.Key<Boolean> = booleanPreferencesKey("beach_outro_seen")

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

    val beachIntroSeenFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[beachIntroSeenKey] ?: false }

    val beachOutroSeenFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[beachOutroSeenKey] ?: false }

    suspend fun markBeachIntroSeen() {
        context.dataStore.edit { prefs -> prefs[beachIntroSeenKey] = true }
    }

    suspend fun markBeachOutroSeen() {
        context.dataStore.edit { prefs -> prefs[beachOutroSeenKey] = true }
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

    suspend fun resetAll() {
        context.dataStore.edit { prefs ->
            prefs[unlockedLevelKey] = 1
            prefs[completedLevelsKey] = ""
            prefs[beachIntroSeenKey] = false
            prefs[beachOutroSeenKey] = false
        }
    }
}

