package com.tal.hebrewdino.ui.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import java.io.File
import java.nio.file.Files
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressPrefsFullGameResetTest {
    @Test
    fun fullGameReset_clearsSeason1Season2AndOnboarding_butPreservesAudioPrefs() = runBlocking {
        val tmpDir = Files.createTempDirectory("hebrew-dino-datastore-test").toFile()
        val prefsFile = File(tmpDir, "prefs.preferences_pb")
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val dataStore =
            PreferenceDataStoreFactory.create(
                scope = scope,
                produceFile = { prefsFile },
            )

        val progress = ProgressPrefs(dataStore)

        val backgroundMusicEnabledKey = booleanPreferencesKey("background_music_enabled")
        dataStore.edit { prefs ->
            prefs[backgroundMusicEnabledKey] = false

            prefs[intPreferencesKey("unlocked_level")] = 6
            prefs[stringPreferencesKey("completed_levels")] = "1,2,3,4,5,6"
            prefs[booleanPreferencesKey("beach_intro_seen")] = true
            prefs[booleanPreferencesKey("beach_outro_seen")] = true
            prefs[booleanPreferencesKey("chapter1_letters_intro_seen")] = true
            prefs[booleanPreferencesKey("chapter1_mid_boost_seen")] = true

            prefs[booleanPreferencesKey("chapter2_intro_seen")] = true
            prefs[booleanPreferencesKey("chapter2_letters_intro_seen")] = true
            prefs[booleanPreferencesKey("chapter2_mid_boost_seen")] = true
            prefs[intPreferencesKey("chapter2_unlocked_station")] = 4
            prefs[stringPreferencesKey("chapter2_completed_stations")] = "1,2,3,4"
            prefs[booleanPreferencesKey("chapter2_completed")] = true

            prefs[booleanPreferencesKey("chapter3_intro_seen")] = true
            prefs[booleanPreferencesKey("chapter3_letters_intro_seen")] = true
            prefs[booleanPreferencesKey("chapter3_mid_boost_seen")] = true
            prefs[intPreferencesKey("chapter3_unlocked_station")] = 5
            prefs[stringPreferencesKey("chapter3_completed_stations")] = "1,2,3,4,5"
            prefs[booleanPreferencesKey("chapter3_completed")] = true

            prefs[booleanPreferencesKey("chapter4_intro_seen")] = true
            prefs[booleanPreferencesKey("chapter4_letters_intro_seen")] = true
            prefs[booleanPreferencesKey("chapter4_mid_boost_seen")] = true
            prefs[intPreferencesKey("chapter4_unlocked_station")] = 5
            prefs[stringPreferencesKey("chapter4_completed_stations")] = "1,2,3,4,5"
            prefs[booleanPreferencesKey("chapter4_completed")] = true

            prefs[booleanPreferencesKey("chapter5_intro_seen")] = true
            prefs[booleanPreferencesKey("chapter5_letters_intro_seen")] = true
            prefs[booleanPreferencesKey("chapter5_mid_boost_seen")] = true
            prefs[intPreferencesKey("chapter5_unlocked_station")] = 5
            prefs[stringPreferencesKey("chapter5_completed_stations")] = "1,2,3,4,5"
            prefs[booleanPreferencesKey("chapter5_completed")] = true

            prefs[booleanPreferencesKey("chapter6_intro_seen")] = true
            prefs[booleanPreferencesKey("chapter6_letters_intro_seen")] = true
            prefs[booleanPreferencesKey("chapter6_mid_boost_seen")] = true
            prefs[intPreferencesKey("chapter6_unlocked_station")] = 5
            prefs[stringPreferencesKey("chapter6_completed_stations")] = "1,2,3,4,5"
            prefs[booleanPreferencesKey("chapter6_completed")] = true

            prefs[stringPreferencesKey("character")] = DinoCharacter.Dina.name
            prefs[stringPreferencesKey("player_address")] = PlayerAddress.Girl.name

            prefs[stringPreferencesKey("season2_completed_chapters")] = "1,2"
            prefs[stringPreferencesKey("season2_ch1_completed_stations")] = "1,2,3"
            prefs[stringPreferencesKey("season2_ch2_completed_stations")] = "1"
        }

        progress.fullGameReset()

        assertEquals(1, progress.unlockedLevelFlow.first())
        assertTrue(progress.completedLevelsFlow.first().isEmpty())
        assertFalse(progress.beachOutroSeenFlow.first())
        assertFalse(progress.chapter1MidBoostSeenFlow.first())

        assertEquals(1, progress.chapter2UnlockedStationFlow.first())
        assertTrue(progress.chapter2CompletedStationsFlow.first().isEmpty())
        assertFalse(progress.chapter2CompletedFlow.first())
        assertFalse(progress.chapter2MidBoostSeenFlow.first())

        assertEquals(1, progress.chapter3UnlockedStationFlow.first())
        assertTrue(progress.chapter3CompletedStationsFlow.first().isEmpty())
        assertFalse(progress.chapter3CompletedFlow.first())
        assertFalse(progress.chapter3MidBoostSeenFlow.first())

        assertEquals(1, progress.chapter4UnlockedStationFlow.first())
        assertTrue(progress.chapter4CompletedStationsFlow.first().isEmpty())
        assertFalse(progress.chapter4CompletedFlow.first())
        assertFalse(progress.chapter4MidBoostSeenFlow.first())

        assertEquals(1, progress.chapter5UnlockedStationFlow.first())
        assertTrue(progress.chapter5CompletedStationsFlow.first().isEmpty())
        assertFalse(progress.chapter5CompletedFlow.first())
        assertFalse(progress.chapter5MidBoostSeenFlow.first())

        assertEquals(1, progress.chapter6UnlockedStationFlow.first())
        assertTrue(progress.chapter6CompletedStationsFlow.first().isEmpty())
        assertFalse(progress.chapter6CompletedFlow.first())
        assertFalse(progress.chapter6MidBoostSeenFlow.first())

        val prefs = dataStore.data.first()
        assertFalse(prefs[backgroundMusicEnabledKey] ?: true)

        assertNull(prefs[stringPreferencesKey("character")])
        assertNull(prefs[stringPreferencesKey("player_address")])

        assertNull(prefs[stringPreferencesKey("season2_completed_chapters")])
        assertNull(prefs[stringPreferencesKey("season2_ch1_completed_stations")])
        assertNull(prefs[stringPreferencesKey("season2_ch2_completed_stations")])

        scope.cancel()
    }
}

