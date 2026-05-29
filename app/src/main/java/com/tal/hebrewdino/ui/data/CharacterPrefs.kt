package com.tal.hebrewdino.ui.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Selected journey companion (Season 1 Ch.1 companion art). */
enum class DinoCharacter {
    Dino,
    Dina,
}

/** How the game addresses the player in Hebrew copy and intro voice-over. */
enum class PlayerAddress {
    Boy,
    Girl,
}

class CharacterPrefs(private val context: Context) {
    private val characterKey: Preferences.Key<String> = stringPreferencesKey("character")
    private val playerAddressKey: Preferences.Key<String> = stringPreferencesKey("player_address")

    val characterFlow: Flow<DinoCharacter?> =
        context.dataStore.data.map { prefs ->
            when (prefs[characterKey]) {
                DinoCharacter.Dino.name -> DinoCharacter.Dino
                DinoCharacter.Dina.name -> DinoCharacter.Dina
                else -> null
            }
        }

    val playerAddressFlow: Flow<PlayerAddress?> =
        context.dataStore.data.map { prefs ->
            when (prefs[playerAddressKey]) {
                PlayerAddress.Boy.name -> PlayerAddress.Boy
                PlayerAddress.Girl.name -> PlayerAddress.Girl
                else -> null
            }
        }

    val onboardingCompleteFlow: Flow<Boolean> =
        kotlinx.coroutines.flow.combine(characterFlow, playerAddressFlow) { character, address ->
            character != null && address != null
        }

    suspend fun setCharacter(character: DinoCharacter) {
        context.dataStore.edit { it[characterKey] = character.name }
    }

    suspend fun setPlayerAddress(address: PlayerAddress) {
        context.dataStore.edit { it[playerAddressKey] = address.name }
    }

    suspend fun setOnboardingDefaults() {
        context.dataStore.edit {
            it[characterKey] = DinoCharacter.Dino.name
            it[playerAddressKey] = PlayerAddress.Boy.name
        }
    }

    suspend fun clearOnboarding() {
        context.dataStore.edit {
            it.remove(characterKey)
            it.remove(playerAddressKey)
        }
    }
}
