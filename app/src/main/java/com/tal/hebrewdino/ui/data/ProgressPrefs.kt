package com.tal.hebrewdino.ui.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.tal.hebrewdino.ui.domain.Chapter1Config
import com.tal.hebrewdino.ui.domain.Chapter2Config
import com.tal.hebrewdino.ui.domain.Chapter3Config
import com.tal.hebrewdino.ui.domain.Chapter4Config
import com.tal.hebrewdino.ui.domain.Chapter5Config
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProgressPrefs(private val context: Context) {
    private val unlockedLevelKey: Preferences.Key<Int> = intPreferencesKey("unlocked_level")
    private val completedLevelsKey: Preferences.Key<String> = androidx.datastore.preferences.core.stringPreferencesKey("completed_levels")
    private val beachIntroSeenKey: Preferences.Key<Boolean> = booleanPreferencesKey("beach_intro_seen")
    private val beachOutroSeenKey: Preferences.Key<Boolean> = booleanPreferencesKey("beach_outro_seen")
    private val chapter1LettersIntroSeenKey: Preferences.Key<Boolean> = booleanPreferencesKey("chapter1_letters_intro_seen")
    private val chapter1MidBoostSeenKey: Preferences.Key<Boolean> = booleanPreferencesKey("chapter1_mid_boost_seen")
    private val chapter2IntroSeenKey: Preferences.Key<Boolean> = booleanPreferencesKey("chapter2_intro_seen")
    private val chapter2LettersIntroSeenKey: Preferences.Key<Boolean> = booleanPreferencesKey("chapter2_letters_intro_seen")
    private val chapter2MidBoostSeenKey: Preferences.Key<Boolean> = booleanPreferencesKey("chapter2_mid_boost_seen")
    private val chapter2UnlockedStationKey: Preferences.Key<Int> = intPreferencesKey("chapter2_unlocked_station")
    private val chapter2CompletedStationsKey: Preferences.Key<String> = androidx.datastore.preferences.core.stringPreferencesKey("chapter2_completed_stations")
    private val chapter2CompletedKey: Preferences.Key<Boolean> = booleanPreferencesKey("chapter2_completed")
    private val chapter3IntroSeenKey: Preferences.Key<Boolean> = booleanPreferencesKey("chapter3_intro_seen")
    private val chapter3LettersIntroSeenKey: Preferences.Key<Boolean> = booleanPreferencesKey("chapter3_letters_intro_seen")
    private val chapter3MidBoostSeenKey: Preferences.Key<Boolean> = booleanPreferencesKey("chapter3_mid_boost_seen")
    private val chapter3UnlockedStationKey: Preferences.Key<Int> = intPreferencesKey("chapter3_unlocked_station")
    private val chapter3CompletedStationsKey: Preferences.Key<String> = androidx.datastore.preferences.core.stringPreferencesKey("chapter3_completed_stations")
    private val chapter3CompletedKey: Preferences.Key<Boolean> = booleanPreferencesKey("chapter3_completed")
    private val chapter4IntroSeenKey: Preferences.Key<Boolean> = booleanPreferencesKey("chapter4_intro_seen")
    private val chapter4LettersIntroSeenKey: Preferences.Key<Boolean> = booleanPreferencesKey("chapter4_letters_intro_seen")
    private val chapter4MidBoostSeenKey: Preferences.Key<Boolean> = booleanPreferencesKey("chapter4_mid_boost_seen")
    private val chapter4UnlockedStationKey: Preferences.Key<Int> = intPreferencesKey("chapter4_unlocked_station")
    private val chapter4CompletedStationsKey: Preferences.Key<String> =
        androidx.datastore.preferences.core.stringPreferencesKey("chapter4_completed_stations")
    private val chapter4CompletedKey: Preferences.Key<Boolean> = booleanPreferencesKey("chapter4_completed")
    private val chapter5IntroSeenKey: Preferences.Key<Boolean> = booleanPreferencesKey("chapter5_intro_seen")
    private val chapter5LettersIntroSeenKey: Preferences.Key<Boolean> = booleanPreferencesKey("chapter5_letters_intro_seen")
    private val chapter5MidBoostSeenKey: Preferences.Key<Boolean> = booleanPreferencesKey("chapter5_mid_boost_seen")
    private val chapter5UnlockedStationKey: Preferences.Key<Int> = intPreferencesKey("chapter5_unlocked_station")
    private val chapter5CompletedStationsKey: Preferences.Key<String> =
        androidx.datastore.preferences.core.stringPreferencesKey("chapter5_completed_stations")
    private val chapter5CompletedKey: Preferences.Key<Boolean> = booleanPreferencesKey("chapter5_completed")

    val unlockedLevelFlow: Flow<Int> =
        context.dataStore.data.map { prefs ->
            (prefs[unlockedLevelKey] ?: 1).coerceIn(1, Chapter1Config.STATION_COUNT)
        }

    val completedLevelsFlow: Flow<Set<Int>> =
        context.dataStore.data.map { prefs ->
            val raw = prefs[completedLevelsKey].orEmpty()
            if (raw.isBlank()) return@map emptySet()
            raw.split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .filter { it in 1..Chapter1Config.STATION_COUNT }
                .toSet()
        }

    val beachIntroSeenFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[beachIntroSeenKey] ?: false }

    val beachOutroSeenFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[beachOutroSeenKey] ?: false }

    val chapter1LettersIntroSeenFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[chapter1LettersIntroSeenKey] ?: false }

    val chapter1MidBoostSeenFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[chapter1MidBoostSeenKey] ?: false }

    val chapter2IntroSeenFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[chapter2IntroSeenKey] ?: false }

    val chapter2LettersIntroSeenFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[chapter2LettersIntroSeenKey] ?: false }

    val chapter2MidBoostSeenFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[chapter2MidBoostSeenKey] ?: false }

    val chapter2UnlockedStationFlow: Flow<Int> =
        context.dataStore.data.map { prefs ->
            (prefs[chapter2UnlockedStationKey] ?: 1).coerceIn(1, Chapter2Config.STATION_COUNT)
        }

    val chapter2CompletedStationsFlow: Flow<Set<Int>> =
        context.dataStore.data.map { prefs ->
            val raw = prefs[chapter2CompletedStationsKey].orEmpty()
            if (raw.isBlank()) return@map emptySet()
            raw.split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .filter { it in 1..Chapter2Config.STATION_COUNT }
                .toSet()
        }

    val chapter2CompletedFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[chapter2CompletedKey] ?: false }

    val chapter3IntroSeenFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[chapter3IntroSeenKey] ?: false }

    val chapter3LettersIntroSeenFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[chapter3LettersIntroSeenKey] ?: false }

    val chapter3MidBoostSeenFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[chapter3MidBoostSeenKey] ?: false }

    val chapter3UnlockedStationFlow: Flow<Int> =
        context.dataStore.data.map { prefs ->
            (prefs[chapter3UnlockedStationKey] ?: 1).coerceIn(1, Chapter3Config.STATION_COUNT)
        }

    val chapter3CompletedStationsFlow: Flow<Set<Int>> =
        context.dataStore.data.map { prefs ->
            val raw = prefs[chapter3CompletedStationsKey].orEmpty()
            if (raw.isBlank()) return@map emptySet()
            raw.split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .filter { it in 1..Chapter3Config.STATION_COUNT }
                .toSet()
        }

    val chapter3CompletedFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[chapter3CompletedKey] ?: false }

    val chapter4IntroSeenFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[chapter4IntroSeenKey] ?: false }

    val chapter4LettersIntroSeenFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[chapter4LettersIntroSeenKey] ?: false }

    val chapter4MidBoostSeenFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[chapter4MidBoostSeenKey] ?: false }

    val chapter4UnlockedStationFlow: Flow<Int> =
        context.dataStore.data.map { prefs ->
            (prefs[chapter4UnlockedStationKey] ?: 1).coerceIn(1, Chapter4Config.STATION_COUNT)
        }

    val chapter4CompletedStationsFlow: Flow<Set<Int>> =
        context.dataStore.data.map { prefs ->
            val raw = prefs[chapter4CompletedStationsKey].orEmpty()
            if (raw.isBlank()) return@map emptySet()
            raw.split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .filter { it in 1..Chapter4Config.STATION_COUNT }
                .toSet()
        }

    val chapter4CompletedFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[chapter4CompletedKey] ?: false }

    val chapter5IntroSeenFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[chapter5IntroSeenKey] ?: false }

    val chapter5LettersIntroSeenFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[chapter5LettersIntroSeenKey] ?: false }

    val chapter5MidBoostSeenFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[chapter5MidBoostSeenKey] ?: false }

    val chapter5UnlockedStationFlow: Flow<Int> =
        context.dataStore.data.map { prefs ->
            (prefs[chapter5UnlockedStationKey] ?: 1).coerceIn(1, Chapter5Config.STATION_COUNT)
        }

    val chapter5CompletedStationsFlow: Flow<Set<Int>> =
        context.dataStore.data.map { prefs ->
            val raw = prefs[chapter5CompletedStationsKey].orEmpty()
            if (raw.isBlank()) return@map emptySet()
            raw.split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .filter { it in 1..Chapter5Config.STATION_COUNT }
                .toSet()
        }

    val chapter5CompletedFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[chapter5CompletedKey] ?: false }

    suspend fun markBeachIntroSeen() {
        context.dataStore.edit { prefs -> prefs[beachIntroSeenKey] = true }
    }

    suspend fun markBeachOutroSeen() {
        context.dataStore.edit { prefs -> prefs[beachOutroSeenKey] = true }
    }

    suspend fun markChapter1LettersIntroSeen() {
        context.dataStore.edit { prefs -> prefs[chapter1LettersIntroSeenKey] = true }
    }

    suspend fun markChapter1MidBoostSeen() {
        context.dataStore.edit { prefs -> prefs[chapter1MidBoostSeenKey] = true }
    }

    suspend fun markChapter2IntroSeen() {
        context.dataStore.edit { prefs -> prefs[chapter2IntroSeenKey] = true }
    }

    suspend fun markChapter2LettersIntroSeen() {
        context.dataStore.edit { prefs -> prefs[chapter2LettersIntroSeenKey] = true }
    }

    suspend fun markChapter2MidBoostSeen() {
        context.dataStore.edit { prefs -> prefs[chapter2MidBoostSeenKey] = true }
    }

    suspend fun markChapter3MidBoostSeen() {
        context.dataStore.edit { prefs -> prefs[chapter3MidBoostSeenKey] = true }
    }

    suspend fun markChapter4MidBoostSeen() {
        context.dataStore.edit { prefs -> prefs[chapter4MidBoostSeenKey] = true }
    }

    suspend fun markChapter5MidBoostSeen() {
        context.dataStore.edit { prefs -> prefs[chapter5MidBoostSeenKey] = true }
    }

    suspend fun unlockChapter2AtLeast(stationId: Int) {
        val capped = stationId.coerceIn(1, Chapter2Config.STATION_COUNT)
        context.dataStore.edit { prefs ->
            val current = (prefs[chapter2UnlockedStationKey] ?: 1).coerceIn(1, Chapter2Config.STATION_COUNT)
            if (capped > current) prefs[chapter2UnlockedStationKey] = capped
        }
    }

    suspend fun markChapter2CompletedStation(stationId: Int) {
        if (stationId !in 1..Chapter2Config.STATION_COUNT) return
        context.dataStore.edit { prefs ->
            val current = prefs[chapter2CompletedStationsKey].orEmpty()
            val set =
                current.split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .filter { it in 1..Chapter2Config.STATION_COUNT }
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
        val capped = stationId.coerceIn(1, Chapter3Config.STATION_COUNT)
        context.dataStore.edit { prefs ->
            val current = (prefs[chapter3UnlockedStationKey] ?: 1).coerceIn(1, Chapter3Config.STATION_COUNT)
            if (capped > current) prefs[chapter3UnlockedStationKey] = capped
        }
    }

    suspend fun markChapter3CompletedStation(stationId: Int) {
        if (stationId !in 1..Chapter3Config.STATION_COUNT) return
        context.dataStore.edit { prefs ->
            val current = prefs[chapter3CompletedStationsKey].orEmpty()
            val set =
                current.split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .filter { it in 1..Chapter3Config.STATION_COUNT }
                    .toMutableSet()
            set.add(stationId)
            prefs[chapter3CompletedStationsKey] = set.toList().sorted().joinToString(",")
        }
    }

    /** One-shot repair if debug / older builds wrote chapter 2 unlock beyond [Chapter2Config.STATION_COUNT]. */
    suspend fun repairChapter2ProgressIfNeeded() {
        context.dataStore.edit { prefs ->
            val u = prefs[chapter2UnlockedStationKey] ?: 1
            if (u > Chapter2Config.STATION_COUNT) {
                prefs[chapter2UnlockedStationKey] = Chapter2Config.STATION_COUNT
            }
            val raw = prefs[chapter2CompletedStationsKey].orEmpty()
            if (raw.isNotBlank()) {
                val set =
                    raw.split(",")
                        .mapNotNull { it.trim().toIntOrNull() }
                        .filter { it in 1..Chapter2Config.STATION_COUNT }
                        .toMutableSet()
                prefs[chapter2CompletedStationsKey] = set.toList().sorted().joinToString(",")
            }
        }
    }

    /**
     * Dev helper: advance chapter 1 from current DataStore state (avoids stale Compose snapshot
     * when tapping "בדיקה" quickly).
     */
    suspend fun debugUnlockNextChapter1Station(): Int {
        var completedStationId = 1
        context.dataStore.edit { prefs ->
            val last = Chapter1Config.STATION_COUNT
            val completed =
                prefs[completedLevelsKey].orEmpty()
                    .split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .filter { it in 1..last }
                    .toMutableSet()
            val next = (1..last).firstOrNull { it !in completed } ?: last
            completedStationId = next
            completed.add(next)
            prefs[completedLevelsKey] = completed.toList().sorted().joinToString(",")
            val unlockTo = (next + 1).coerceAtMost(last)
            val currentUnlock = (prefs[unlockedLevelKey] ?: 1).coerceIn(1, last)
            if (unlockTo > currentUnlock) prefs[unlockedLevelKey] = unlockTo
        }
        return completedStationId
    }

    /**
     * Dev helper: advance chapter 2 from current DataStore state (avoids stale snapshot; caps unlock).
     */
    suspend fun debugUnlockNextChapter4Station(): Int {
        var completedStationId = 1
        context.dataStore.edit { prefs ->
            val last = Chapter4Config.STATION_COUNT
            val completed =
                prefs[chapter4CompletedStationsKey].orEmpty()
                    .split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .filter { it in 1..last }
                    .toMutableSet()
            val next = (1..last).firstOrNull { it !in completed } ?: last
            completedStationId = next
            completed.add(next)
            prefs[chapter4CompletedStationsKey] = completed.toList().sorted().joinToString(",")
            val unlockTo = (next + 1).coerceAtMost(last)
            val currentUnlock = (prefs[chapter4UnlockedStationKey] ?: 1).coerceIn(1, last)
            if (unlockTo > currentUnlock) prefs[chapter4UnlockedStationKey] = unlockTo
            if (next >= last) prefs[chapter4CompletedKey] = true
        }
        return completedStationId
    }

    suspend fun debugUnlockNextChapter5Station(): Int {
        var completedStationId = 1
        context.dataStore.edit { prefs ->
            val last = Chapter5Config.STATION_COUNT
            val completed =
                prefs[chapter5CompletedStationsKey].orEmpty()
                    .split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .filter { it in 1..last }
                    .toMutableSet()
            val next = (1..last).firstOrNull { it !in completed } ?: last
            completedStationId = next
            completed.add(next)
            prefs[chapter5CompletedStationsKey] = completed.toList().sorted().joinToString(",")
            val unlockTo = (next + 1).coerceAtMost(last)
            val currentUnlock = (prefs[chapter5UnlockedStationKey] ?: 1).coerceIn(1, last)
            if (unlockTo > currentUnlock) prefs[chapter5UnlockedStationKey] = unlockTo
            if (next >= last) prefs[chapter5CompletedKey] = true
        }
        return completedStationId
    }

    suspend fun debugUnlockNextChapter2Station(): Int {
        var completedStationId = 1
        context.dataStore.edit { prefs ->
            val last = Chapter2Config.STATION_COUNT
            val completed =
                prefs[chapter2CompletedStationsKey].orEmpty()
                    .split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .filter { it in 1..last }
                    .toMutableSet()
            val next = (1..last).firstOrNull { it !in completed } ?: last
            completedStationId = next
            completed.add(next)
            prefs[chapter2CompletedStationsKey] = completed.toList().sorted().joinToString(",")
            val unlockTo = (next + 1).coerceAtMost(last)
            val currentUnlock = (prefs[chapter2UnlockedStationKey] ?: 1).coerceIn(1, last)
            if (unlockTo > currentUnlock) prefs[chapter2UnlockedStationKey] = unlockTo
            if (next >= last) prefs[chapter2CompletedKey] = true
        }
        return completedStationId
    }

    suspend fun debugUnlockNextChapter3Station(): Int {
        var completedStationId = 1
        context.dataStore.edit { prefs ->
            val last = Chapter3Config.STATION_COUNT
            val completed =
                prefs[chapter3CompletedStationsKey].orEmpty()
                    .split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .filter { it in 1..last }
                    .toMutableSet()
            val next = (1..last).firstOrNull { it !in completed } ?: last
            completedStationId = next
            completed.add(next)
            prefs[chapter3CompletedStationsKey] = completed.toList().sorted().joinToString(",")
            val unlockTo = (next + 1).coerceAtMost(last)
            val currentUnlock = (prefs[chapter3UnlockedStationKey] ?: 1).coerceIn(1, last)
            if (unlockTo > currentUnlock) prefs[chapter3UnlockedStationKey] = unlockTo
            if (next >= last) prefs[chapter3CompletedKey] = true
        }
        return completedStationId
    }

    /** One-shot repair if older builds wrote chapter 3 progress beyond released stations. */
    suspend fun repairChapter3ProgressIfNeeded() {
        context.dataStore.edit { prefs ->
            val u = prefs[chapter3UnlockedStationKey] ?: 1
            if (u > Chapter3Config.STATION_COUNT) {
                prefs[chapter3UnlockedStationKey] = Chapter3Config.STATION_COUNT
            }
            val raw = prefs[chapter3CompletedStationsKey].orEmpty()
            if (raw.isNotBlank()) {
                val set =
                    raw.split(",")
                        .mapNotNull { it.trim().toIntOrNull() }
                        .filter { it in 1..Chapter3Config.STATION_COUNT }
                        .toMutableSet()
                prefs[chapter3CompletedStationsKey] = set.toList().sorted().joinToString(",")
            }
        }
    }

    suspend fun markChapter3Completed() {
        context.dataStore.edit { prefs -> prefs[chapter3CompletedKey] = true }
    }

    suspend fun markChapter4IntroSeen() {
        context.dataStore.edit { prefs -> prefs[chapter4IntroSeenKey] = true }
    }

    suspend fun markChapter4LettersIntroSeen() {
        context.dataStore.edit { prefs -> prefs[chapter4LettersIntroSeenKey] = true }
    }

    suspend fun unlockChapter4AtLeast(stationId: Int) {
        val capped = stationId.coerceIn(1, Chapter4Config.STATION_COUNT)
        context.dataStore.edit { prefs ->
            val current = (prefs[chapter4UnlockedStationKey] ?: 1).coerceIn(1, Chapter4Config.STATION_COUNT)
            if (capped > current) prefs[chapter4UnlockedStationKey] = capped
        }
    }

    suspend fun markChapter4CompletedStation(stationId: Int) {
        if (stationId !in 1..Chapter4Config.STATION_COUNT) return
        context.dataStore.edit { prefs ->
            val current = prefs[chapter4CompletedStationsKey].orEmpty()
            val set =
                current.split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .filter { it in 1..Chapter4Config.STATION_COUNT }
                    .toMutableSet()
            set.add(stationId)
            prefs[chapter4CompletedStationsKey] = set.toList().sorted().joinToString(",")
        }
    }

    suspend fun markChapter4Completed() {
        context.dataStore.edit { prefs -> prefs[chapter4CompletedKey] = true }
    }

    suspend fun repairChapter4ProgressIfNeeded() {
        context.dataStore.edit { prefs ->
            val u = prefs[chapter4UnlockedStationKey] ?: 1
            if (u > Chapter4Config.STATION_COUNT) {
                prefs[chapter4UnlockedStationKey] = Chapter4Config.STATION_COUNT
            }
            val raw = prefs[chapter4CompletedStationsKey].orEmpty()
            if (raw.isNotBlank()) {
                val set =
                    raw.split(",")
                        .mapNotNull { it.trim().toIntOrNull() }
                        .filter { it in 1..Chapter4Config.STATION_COUNT }
                        .toMutableSet()
                prefs[chapter4CompletedStationsKey] = set.toList().sorted().joinToString(",")
            }
        }
    }

    suspend fun markChapter5IntroSeen() {
        context.dataStore.edit { prefs -> prefs[chapter5IntroSeenKey] = true }
    }

    suspend fun markChapter5LettersIntroSeen() {
        context.dataStore.edit { prefs -> prefs[chapter5LettersIntroSeenKey] = true }
    }

    suspend fun unlockChapter5AtLeast(stationId: Int) {
        val capped = stationId.coerceIn(1, Chapter5Config.STATION_COUNT)
        context.dataStore.edit { prefs ->
            val current = (prefs[chapter5UnlockedStationKey] ?: 1).coerceIn(1, Chapter5Config.STATION_COUNT)
            if (capped > current) prefs[chapter5UnlockedStationKey] = capped
        }
    }

    suspend fun markChapter5CompletedStation(stationId: Int) {
        if (stationId !in 1..Chapter5Config.STATION_COUNT) return
        context.dataStore.edit { prefs ->
            val current = prefs[chapter5CompletedStationsKey].orEmpty()
            val set =
                current.split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .filter { it in 1..Chapter5Config.STATION_COUNT }
                    .toMutableSet()
            set.add(stationId)
            prefs[chapter5CompletedStationsKey] = set.toList().sorted().joinToString(",")
        }
    }

    suspend fun markChapter5Completed() {
        context.dataStore.edit { prefs -> prefs[chapter5CompletedKey] = true }
    }

    suspend fun repairChapter5ProgressIfNeeded() {
        context.dataStore.edit { prefs ->
            val u = prefs[chapter5UnlockedStationKey] ?: 1
            if (u > Chapter5Config.STATION_COUNT) {
                prefs[chapter5UnlockedStationKey] = Chapter5Config.STATION_COUNT
            }
            val raw = prefs[chapter5CompletedStationsKey].orEmpty()
            if (raw.isNotBlank()) {
                val set =
                    raw.split(",")
                        .mapNotNull { it.trim().toIntOrNull() }
                        .filter { it in 1..Chapter5Config.STATION_COUNT }
                        .toMutableSet()
                prefs[chapter5CompletedStationsKey] = set.toList().sorted().joinToString(",")
            }
        }
    }

    suspend fun unlockAtLeast(levelId: Int) {
        val capped = levelId.coerceIn(1, Chapter1Config.STATION_COUNT)
        context.dataStore.edit { prefs ->
            val current = (prefs[unlockedLevelKey] ?: 1).coerceIn(1, Chapter1Config.STATION_COUNT)
            if (capped > current) {
                prefs[unlockedLevelKey] = capped
            }
        }
    }

    suspend fun markCompleted(levelId: Int) {
        if (levelId !in 1..Chapter1Config.STATION_COUNT) return
        context.dataStore.edit { prefs ->
            val current = prefs[completedLevelsKey].orEmpty()
            val set =
                current.split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .filter { it in 1..Chapter1Config.STATION_COUNT }
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
            prefs[chapter1MidBoostSeenKey] = false
            prefs[chapter2IntroSeenKey] = false
            prefs[chapter2LettersIntroSeenKey] = false
            prefs[chapter2MidBoostSeenKey] = false
            prefs[chapter2UnlockedStationKey] = 1
            prefs[chapter2CompletedStationsKey] = ""
            prefs[chapter2CompletedKey] = false
            prefs[chapter3IntroSeenKey] = false
            prefs[chapter3LettersIntroSeenKey] = false
            prefs[chapter3MidBoostSeenKey] = false
            prefs[chapter3UnlockedStationKey] = 1
            prefs[chapter3CompletedStationsKey] = ""
            prefs[chapter3CompletedKey] = false
            prefs[chapter4IntroSeenKey] = false
            prefs[chapter4LettersIntroSeenKey] = false
            prefs[chapter4MidBoostSeenKey] = false
            prefs[chapter4UnlockedStationKey] = 1
            prefs[chapter4CompletedStationsKey] = ""
            prefs[chapter4CompletedKey] = false
            prefs[chapter5IntroSeenKey] = false
            prefs[chapter5LettersIntroSeenKey] = false
            prefs[chapter5MidBoostSeenKey] = false
            prefs[chapter5UnlockedStationKey] = 1
            prefs[chapter5CompletedStationsKey] = ""
            prefs[chapter5CompletedKey] = false
        }
    }

    /** Clears saved progress for the given chapter numbers only (1–5). */
    suspend fun resetChapters(chapterIds: Set<Int>) {
        if (chapterIds.isEmpty()) return
        context.dataStore.edit { prefs ->
            if (1 in chapterIds) {
                prefs[unlockedLevelKey] = 1
                prefs[completedLevelsKey] = ""
                prefs[beachIntroSeenKey] = false
                prefs[beachOutroSeenKey] = false
                prefs[chapter1LettersIntroSeenKey] = false
                prefs[chapter1MidBoostSeenKey] = false
            }
            if (2 in chapterIds) {
                prefs[chapter2IntroSeenKey] = false
                prefs[chapter2LettersIntroSeenKey] = false
                prefs[chapter2MidBoostSeenKey] = false
                prefs[chapter2UnlockedStationKey] = 1
                prefs[chapter2CompletedStationsKey] = ""
                prefs[chapter2CompletedKey] = false
            }
            if (3 in chapterIds) {
                prefs[chapter3IntroSeenKey] = false
                prefs[chapter3LettersIntroSeenKey] = false
                prefs[chapter3MidBoostSeenKey] = false
                prefs[chapter3UnlockedStationKey] = 1
                prefs[chapter3CompletedStationsKey] = ""
                prefs[chapter3CompletedKey] = false
            }
            if (4 in chapterIds) {
                prefs[chapter4IntroSeenKey] = false
                prefs[chapter4LettersIntroSeenKey] = false
                prefs[chapter4MidBoostSeenKey] = false
                prefs[chapter4UnlockedStationKey] = 1
                prefs[chapter4CompletedStationsKey] = ""
                prefs[chapter4CompletedKey] = false
            }
            if (5 in chapterIds) {
                prefs[chapter5IntroSeenKey] = false
                prefs[chapter5LettersIntroSeenKey] = false
                prefs[chapter5MidBoostSeenKey] = false
                prefs[chapter5UnlockedStationKey] = 1
                prefs[chapter5CompletedStationsKey] = ""
                prefs[chapter5CompletedKey] = false
            }
        }
    }
}

