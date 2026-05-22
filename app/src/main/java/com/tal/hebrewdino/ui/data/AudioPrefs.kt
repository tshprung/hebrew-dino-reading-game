package com.tal.hebrewdino.ui.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AudioPrefs(private val context: Context) {
    private val backgroundMusicEnabledKey: Preferences.Key<Boolean> =
        booleanPreferencesKey("background_music_enabled")

    val backgroundMusicEnabledFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[backgroundMusicEnabledKey] ?: true
        }

    suspend fun setBackgroundMusicEnabled(enabled: Boolean) {
        context.dataStore.edit { it[backgroundMusicEnabledKey] = enabled }
    }
}

