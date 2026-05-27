package com.tal.hebrewdino.ui.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class StoryNarrationPrefs(private val context: Context) : StoryNarrationGate {
    private val part2SpokenKey = booleanPreferencesKey("story_part2_baby_hatch_spoken")
    private val part3SpokenKey = booleanPreferencesKey("story_part3_first_accessory_spoken")

    override suspend fun isPart2Spoken(): Boolean =
        context.dataStore.data.map { it[part2SpokenKey] ?: false }.first()

    override suspend fun isPart3Spoken(): Boolean =
        context.dataStore.data.map { it[part3SpokenKey] ?: false }.first()

    override suspend fun setPart2Spoken() {
        context.dataStore.edit { it[part2SpokenKey] = true }
    }

    override suspend fun setPart3Spoken() {
        context.dataStore.edit { it[part3SpokenKey] = true }
    }

    override suspend fun reset() {
        context.dataStore.edit {
            it.remove(part2SpokenKey)
            it.remove(part3SpokenKey)
        }
    }
}
