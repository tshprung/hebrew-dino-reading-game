package com.tal.hebrewdino.ui.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class DinoCharacter {
    DINO_GREEN,
    DINA_PINK,
}

class CharacterPrefs(private val context: Context) {
    private val key: Preferences.Key<String> = stringPreferencesKey("character")
    private val selectedKey: Preferences.Key<Boolean> = booleanPreferencesKey("character_selected_once")

    val characterFlow: Flow<DinoCharacter> =
        context.dataStore.data.map { prefs ->
            when (prefs[key]) {
                DinoCharacter.DINA_PINK.name -> DinoCharacter.DINA_PINK
                DinoCharacter.DINO_GREEN.name -> DinoCharacter.DINO_GREEN
                else -> DinoCharacter.DINO_GREEN
            }
        }

    val characterSelectedOnceFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[selectedKey] ?: false }

    suspend fun setCharacter(character: DinoCharacter) {
        context.dataStore.edit {
            it[key] = character.name
            it[selectedKey] = true
        }
    }

    suspend fun resetCharacterSelection() {
        context.dataStore.edit {
            it[key] = DinoCharacter.DINO_GREEN.name
            it[selectedKey] = false
        }
    }
}

