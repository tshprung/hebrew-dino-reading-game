package com.tal.hebrewdino.ui.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CharacterRepository(context: Context) {
    private val store: CharacterStore = storeFactory(context.applicationContext)

    val characterFlow: Flow<DinoCharacter?> = store.characterFlow
    val foodCountFlow: Flow<Int> = store.foodCountFlow
    val growthStageFlow: Flow<String> = store.growthStageFlow
    val pendingRewardFoodDeltaFlow: Flow<Int> = store.pendingRewardFoodDeltaFlow

    suspend fun setCharacter(character: DinoCharacter) {
        store.setCharacter(character)
    }

    suspend fun setFoodCount(foodCount: Int) {
        store.setFoodCount(foodCount.coerceAtLeast(0))
    }

    suspend fun addFood(delta: Int) {
        if (delta == 0) return
        store.addFood(delta)
    }

    suspend fun setGrowthStage(stageName: String) {
        store.setGrowthStage(stageName)
    }

    suspend fun setPendingRewardFoodDelta(delta: Int) {
        store.setPendingRewardFoodDelta(delta.coerceAtLeast(0))
    }

    suspend fun clearPendingRewardFoodDelta() {
        store.setPendingRewardFoodDelta(0)
    }

    suspend fun ensureTamagotchiInitialized() {
        store.ensureTamagotchiInitialized()
    }

    internal interface CharacterStore {
        val characterFlow: Flow<DinoCharacter?>
        val foodCountFlow: Flow<Int>
        val growthStageFlow: Flow<String>
        val pendingRewardFoodDeltaFlow: Flow<Int>

        suspend fun setCharacter(character: DinoCharacter)
        suspend fun setFoodCount(foodCount: Int)
        suspend fun addFood(delta: Int)
        suspend fun setGrowthStage(stageName: String)
        suspend fun setPendingRewardFoodDelta(delta: Int)
        suspend fun ensureTamagotchiInitialized()
    }

    private class PrefsCharacterStore(private val appContext: Context) : CharacterStore {
        private val characterPrefs: CharacterPrefs = CharacterPrefs(appContext)
        private val foodCountKey = intPreferencesKey("tama_food_count")
        private val growthStageKey = stringPreferencesKey("tama_growth_stage")
        private val pendingRewardFoodDeltaKey = intPreferencesKey("tama_pending_reward_food_delta")
        private val initializedKey = booleanPreferencesKey("tama_initialized")

        override val characterFlow: Flow<DinoCharacter?> = characterPrefs.characterFlow

        override val foodCountFlow: Flow<Int> =
            appContext.dataStore.data.map { prefs ->
                (prefs[foodCountKey] ?: 3).coerceAtLeast(0)
            }

        override val growthStageFlow: Flow<String> =
            appContext.dataStore.data.map { prefs ->
                prefs[growthStageKey] ?: "EGG"
            }

        override val pendingRewardFoodDeltaFlow: Flow<Int> =
            appContext.dataStore.data.map { prefs ->
                (prefs[pendingRewardFoodDeltaKey] ?: 0).coerceAtLeast(0)
            }

        override suspend fun setCharacter(character: DinoCharacter) {
            characterPrefs.setCharacter(character)
        }

        override suspend fun setFoodCount(foodCount: Int) {
            appContext.dataStore.edit { it[foodCountKey] = foodCount.coerceAtLeast(0) }
        }

        override suspend fun addFood(delta: Int) {
            appContext.dataStore.edit { prefs ->
                val current = (prefs[foodCountKey] ?: 3).coerceAtLeast(0)
                prefs[foodCountKey] = (current + delta).coerceAtLeast(0)
            }
        }

        override suspend fun setGrowthStage(stageName: String) {
            appContext.dataStore.edit { it[growthStageKey] = stageName }
        }

        override suspend fun setPendingRewardFoodDelta(delta: Int) {
            appContext.dataStore.edit { it[pendingRewardFoodDeltaKey] = delta.coerceAtLeast(0) }
        }

        override suspend fun ensureTamagotchiInitialized() {
            appContext.dataStore.edit { prefs ->
                val initialized = prefs[initializedKey] ?: false
                if (initialized) return@edit
                val current = (prefs[foodCountKey] ?: 0).coerceAtLeast(0)
                prefs[foodCountKey] = maxOf(current, 3)
                if (prefs[growthStageKey].isNullOrBlank()) {
                    prefs[growthStageKey] = "EGG"
                }
                prefs[initializedKey] = true
            }
        }
    }

    companion object {
        @Volatile
        internal var storeFactory: (Context) -> CharacterStore =
            { appContext -> PrefsCharacterStore(appContext) }
    }
}
