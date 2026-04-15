package com.tal.hebrewdino.ui.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class DinoCharacter {
    Dino,
    Dina,
}

class CharacterPrefs(private val context: Context) {
    private val key: Preferences.Key<String> = stringPreferencesKey("character")

    val characterFlow: Flow<DinoCharacter?> =
        context.dataStore.data.map { prefs ->
            when (prefs[key]) {
                DinoCharacter.Dino.name -> DinoCharacter.Dino
                DinoCharacter.Dina.name -> DinoCharacter.Dina
                else -> null
            }
        }

    suspend fun setCharacter(character: DinoCharacter) {
        context.dataStore.edit { it[key] = character.name }
    }
}

