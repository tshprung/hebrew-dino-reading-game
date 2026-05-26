package com.tal.hebrewdino.ui.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CharacterRepository(context: Context) {
    private val store: CharacterStore = storeFactory(context.applicationContext)

    val characterFlow: Flow<DinoCharacter> = store.characterFlow
    val foodCountFlow: Flow<Int> = store.foodCountFlow
    val totalFoodEarnedFlow: Flow<Int> = store.totalFoodEarnedFlow
    val growthStageFlow: Flow<String> = store.growthStageFlow
    val pendingRewardFoodDeltaFlow: Flow<Int> = store.pendingRewardFoodDeltaFlow
    val fullUntilAtMsFlow: Flow<Long> = store.fullUntilAtMsFlow
    val chapter1MaxCompletedStationFlow: Flow<Int> = store.chapter1MaxCompletedStationFlow

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

    suspend fun setFullUntilAtMs(fullUntilAtMs: Long) {
        store.setFullUntilAtMs(fullUntilAtMs.coerceAtLeast(0L))
    }

    suspend fun clearPendingRewardFoodDelta() {
        store.setPendingRewardFoodDelta(0)
    }

    suspend fun markChapter1StationCompleted(stationId: Int) {
        store.markChapter1StationCompleted(stationId.coerceIn(0, 3))
    }

    suspend fun ensureTamagotchiInitialized() {
        store.ensureTamagotchiInitialized()
    }

    suspend fun resetForNewGame() {
        store.resetForNewGame()
    }

    internal interface CharacterStore {
        val characterFlow: Flow<DinoCharacter>
        val foodCountFlow: Flow<Int>
        val totalFoodEarnedFlow: Flow<Int>
        val growthStageFlow: Flow<String>
        val pendingRewardFoodDeltaFlow: Flow<Int>
        val fullUntilAtMsFlow: Flow<Long>
        val chapter1MaxCompletedStationFlow: Flow<Int>

        suspend fun setCharacter(character: DinoCharacter)
        suspend fun setFoodCount(foodCount: Int)
        suspend fun addFood(delta: Int)
        suspend fun setGrowthStage(stageName: String)
        suspend fun setPendingRewardFoodDelta(delta: Int)
        suspend fun setFullUntilAtMs(fullUntilAtMs: Long)
        suspend fun markChapter1StationCompleted(stationId: Int)
        suspend fun ensureTamagotchiInitialized()
        suspend fun resetForNewGame()
    }

    private class PrefsCharacterStore(private val appContext: Context) : CharacterStore {
        private val characterPrefs: CharacterPrefs = CharacterPrefs(appContext)
        private val foodCountKey = intPreferencesKey("tama_food_count")
        private val totalFoodEarnedKey = intPreferencesKey("tama_total_food_earned")
        private val growthStageKey = stringPreferencesKey("tama_growth_stage")
        private val pendingRewardFoodDeltaKey = intPreferencesKey("tama_pending_reward_food_delta")
        private val initializedKey = booleanPreferencesKey("tama_initialized")
        private val fullUntilAtMsKey = longPreferencesKey("tama_full_until_at_ms")
        private val chapter1MaxCompletedStationKey = intPreferencesKey("chapter1_max_completed_station")

        override val characterFlow: Flow<DinoCharacter> = characterPrefs.characterFlow

        override val foodCountFlow: Flow<Int> =
            appContext.dataStore.data.map { prefs ->
                (prefs[foodCountKey] ?: 0).coerceAtLeast(0)
            }

        override val totalFoodEarnedFlow: Flow<Int> =
            appContext.dataStore.data.map { prefs ->
                (prefs[totalFoodEarnedKey] ?: 0).coerceAtLeast(0)
            }

        override val growthStageFlow: Flow<String> =
            appContext.dataStore.data.map { prefs ->
                prefs[growthStageKey] ?: "EGG"
            }

        override val pendingRewardFoodDeltaFlow: Flow<Int> =
            appContext.dataStore.data.map { prefs ->
                (prefs[pendingRewardFoodDeltaKey] ?: 0).coerceAtLeast(0)
            }

        override val fullUntilAtMsFlow: Flow<Long> =
            appContext.dataStore.data.map { prefs ->
                (prefs[fullUntilAtMsKey] ?: 0L).coerceAtLeast(0L)
            }

        override val chapter1MaxCompletedStationFlow: Flow<Int> =
            appContext.dataStore.data.map { prefs ->
                (prefs[chapter1MaxCompletedStationKey] ?: 0).coerceIn(0, 3)
            }

        override suspend fun setCharacter(character: DinoCharacter) {
            characterPrefs.setCharacter(character)
        }

        override suspend fun setFoodCount(foodCount: Int) {
            appContext.dataStore.edit { it[foodCountKey] = foodCount.coerceAtLeast(0) }
        }

        override suspend fun addFood(delta: Int) {
            appContext.dataStore.edit { prefs ->
                val current = (prefs[foodCountKey] ?: 0).coerceAtLeast(0)
                prefs[foodCountKey] = (current + delta).coerceAtLeast(0)
                if (delta > 0) {
                    val earned = (prefs[totalFoodEarnedKey] ?: 0).coerceAtLeast(0)
                    prefs[totalFoodEarnedKey] = (earned + delta).coerceAtLeast(0)
                }
            }
        }

        override suspend fun setGrowthStage(stageName: String) {
            appContext.dataStore.edit { it[growthStageKey] = stageName }
        }

        override suspend fun setPendingRewardFoodDelta(delta: Int) {
            appContext.dataStore.edit { it[pendingRewardFoodDeltaKey] = delta.coerceAtLeast(0) }
        }

        override suspend fun setFullUntilAtMs(fullUntilAtMs: Long) {
            appContext.dataStore.edit { it[fullUntilAtMsKey] = fullUntilAtMs.coerceAtLeast(0L) }
        }

        override suspend fun markChapter1StationCompleted(stationId: Int) {
            val clamped = stationId.coerceIn(0, 3)
            if (clamped <= 0) return
            appContext.dataStore.edit { prefs ->
                val current = (prefs[chapter1MaxCompletedStationKey] ?: 0).coerceIn(0, 3)
                prefs[chapter1MaxCompletedStationKey] = maxOf(current, clamped)
            }
        }

        override suspend fun ensureTamagotchiInitialized() {
            appContext.dataStore.edit { prefs ->
                val initialized = prefs[initializedKey] ?: false
                if (initialized) return@edit
                val current = (prefs[foodCountKey] ?: 0).coerceAtLeast(0)
                prefs[foodCountKey] = maxOf(current, 0)
                if (prefs[growthStageKey].isNullOrBlank()) {
                    prefs[growthStageKey] = "EGG"
                }
                val earned = (prefs[totalFoodEarnedKey] ?: 0).coerceAtLeast(0)
                prefs[totalFoodEarnedKey] = maxOf(earned, 0)
                prefs[fullUntilAtMsKey] = (prefs[fullUntilAtMsKey] ?: 0L).coerceAtLeast(0L)
                prefs[chapter1MaxCompletedStationKey] = (prefs[chapter1MaxCompletedStationKey] ?: 0).coerceIn(0, 3)
                prefs[initializedKey] = true
            }
        }

        override suspend fun resetForNewGame() {
            appContext.dataStore.edit { prefs ->
                prefs[foodCountKey] = 0
                prefs[totalFoodEarnedKey] = 0
                prefs[growthStageKey] = "EGG"
                prefs[pendingRewardFoodDeltaKey] = 0
                prefs[fullUntilAtMsKey] = 0L
                prefs[chapter1MaxCompletedStationKey] = 0
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
