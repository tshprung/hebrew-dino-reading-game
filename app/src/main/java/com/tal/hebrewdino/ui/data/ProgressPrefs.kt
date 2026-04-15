package com.tal.hebrewdino.ui.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProgressPrefs(private val context: Context) {
    private val unlockedLevelKey: Preferences.Key<Int> = intPreferencesKey("unlocked_level")

    val unlockedLevelFlow: Flow<Int> =
        context.dataStore.data.map { prefs ->
            prefs[unlockedLevelKey] ?: 1
        }

    suspend fun unlockAtLeast(levelId: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[unlockedLevelKey] ?: 1
            if (levelId > current) {
                prefs[unlockedLevelKey] = levelId
            }
        }
    }
}

