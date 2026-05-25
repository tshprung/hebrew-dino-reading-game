package com.tal.hebrewdino.ui.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class CharacterRepository(context: Context) {
    private val prefs: CharacterPrefs = CharacterPrefs(context.applicationContext)

    val characterFlow: Flow<DinoCharacter?> = prefs.characterFlow

    suspend fun setCharacter(character: DinoCharacter) {
        prefs.setCharacter(character)
    }
}

