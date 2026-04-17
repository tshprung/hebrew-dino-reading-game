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
    private val chapter1LettersIntroSeenKey: Preferences.Key<Boolean> = booleanPreferencesKey("chapter1_letters_intro_seen")
    private val chapter2IntroSeenKey: Preferences.Key<Boolean> = booleanPreferencesKey("chapter2_intro_seen")
    private val chapter2LettersIntroSeenKey: Preferences.Key<Boolean> = booleanPreferencesKey("chapter2_letters_intro_seen")
    private val chapter2UnlockedStationKey: Preferences.Key<Int> = intPreferencesKey("chapter2_unlocked_station")
    private val chapter2CompletedStationsKey: Preferences.Key<String> = androidx.datastore.preferences.core.stringPreferencesKey("chapter2_completed_stations")
    private val chapter2CompletedKey: Preferences.Key<Boolean> = booleanPreferencesKey("chapter2_completed")
    private val chapter3IntroSeenKey: Preferences.Key<Boolean> = booleanPreferencesKey("chapter3_intro_seen")
    private val chapter3LettersIntroSeenKey: Preferences.Key<Boolean> = booleanPreferencesKey("chapter3_letters_intro_seen")
    private val chapter3UnlockedStationKey: Preferences.Key<Int> = intPreferencesKey("chapter3_unlocked_station")
    private val chapter3CompletedStationsKey: Preferences.Key<String> = androidx.datastore.preferences.core.stringPreferencesKey("chapter3_completed_stations")
    private val chapter3CompletedKey: Preferences.Key<Boolean> = booleanPreferencesKey("chapter3_completed")

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

    val chapter1LettersIntroSeenFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[chapter1LettersIntroSeenKey] ?: false }

    val chapter2IntroSeenFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[chapter2IntroSeenKey] ?: false }

    val chapter2LettersIntroSeenFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[chapter2LettersIntroSeenKey] ?: false }

    val chapter2UnlockedStationFlow: Flow<Int> =
        context.dataStore.data.map { prefs -> prefs[chapter2UnlockedStationKey] ?: 1 }

    val chapter2CompletedStationsFlow: Flow<Set<Int>> =
        context.dataStore.data.map { prefs ->
            val raw = prefs[chapter2CompletedStationsKey].orEmpty()
            if (raw.isBlank()) return@map emptySet()
            raw.split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .toSet()
        }

    val chapter2CompletedFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[chapter2CompletedKey] ?: false }

    val chapter3IntroSeenFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[chapter3IntroSeenKey] ?: false }

    val chapter3LettersIntroSeenFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[chapter3LettersIntroSeenKey] ?: false }

    val chapter3UnlockedStationFlow: Flow<Int> =
        context.dataStore.data.map { prefs -> prefs[chapter3UnlockedStationKey] ?: 1 }

    val chapter3CompletedStationsFlow: Flow<Set<Int>> =
        context.dataStore.data.map { prefs ->
            val raw = prefs[chapter3CompletedStationsKey].orEmpty()
            if (raw.isBlank()) return@map emptySet()
            raw.split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .toSet()
        }

    val chapter3CompletedFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[chapter3CompletedKey] ?: false }

    suspend fun markBeachIntroSeen() {
        context.dataStore.edit { prefs -> prefs[beachIntroSeenKey] = true }
    }

    suspend fun markBeachOutroSeen() {
        context.dataStore.edit { prefs -> prefs[beachOutroSeenKey] = true }
    }

    suspend fun markChapter1LettersIntroSeen() {
        context.dataStore.edit { prefs -> prefs[chapter1LettersIntroSeenKey] = true }
    }

    suspend fun markChapter2IntroSeen() {
        context.dataStore.edit { prefs -> prefs[chapter2IntroSeenKey] = true }
    }

    suspend fun markChapter2LettersIntroSeen() {
        context.dataStore.edit { prefs -> prefs[chapter2LettersIntroSeenKey] = true }
    }

    suspend fun unlockChapter2AtLeast(stationId: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[chapter2UnlockedStationKey] ?: 1
            if (stationId > current) prefs[chapter2UnlockedStationKey] = stationId
        }
    }

    suspend fun markChapter2CompletedStation(stationId: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[chapter2CompletedStationsKey].orEmpty()
            val set =
                current.split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .toMutableSet()
            set.add(stationId)
            prefs[chapter2CompletedStationsKey] = set.toList().sorted().joinToString(",")
        }
    }

    suspend fun markChapter2Completed() {
        context.dataStore.edit { prefs -> prefs[chapter2CompletedKey] = true }
    }

    suspend fun markChapter3IntroSeen() {
        context.dataStore.edit { prefs -> prefs[chapter3IntroSeenKey] = true }
    }

    suspend fun markChapter3LettersIntroSeen() {
        context.dataStore.edit { prefs -> prefs[chapter3LettersIntroSeenKey] = true }
    }

    suspend fun unlockChapter3AtLeast(stationId: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[chapter3UnlockedStationKey] ?: 1
            if (stationId > current) prefs[chapter3UnlockedStationKey] = stationId
        }
    }

    suspend fun markChapter3CompletedStation(stationId: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[chapter3CompletedStationsKey].orEmpty()
            val set =
                current.split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .toMutableSet()
            set.add(stationId)
            prefs[chapter3CompletedStationsKey] = set.toList().sorted().joinToString(",")
        }
    }

    suspend fun markChapter3Completed() {
        context.dataStore.edit { prefs -> prefs[chapter3CompletedKey] = true }
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
            prefs[chapter1LettersIntroSeenKey] = false
            prefs[chapter2IntroSeenKey] = false
            prefs[chapter2LettersIntroSeenKey] = false
            prefs[chapter2UnlockedStationKey] = 1
            prefs[chapter2CompletedStationsKey] = ""
            prefs[chapter2CompletedKey] = false
            prefs[chapter3IntroSeenKey] = false
            prefs[chapter3LettersIntroSeenKey] = false
            prefs[chapter3UnlockedStationKey] = 1
            prefs[chapter3CompletedStationsKey] = ""
            prefs[chapter3CompletedKey] = false
        }
    }
}

