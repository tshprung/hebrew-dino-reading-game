package com.tal.hebrewdino.ui.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ParentInfoPrefs internal constructor(private val dataStore: androidx.datastore.core.DataStore<Preferences>) {
    constructor(context: Context) : this(context.applicationContext.dataStore)

    private val lastSeenVersionCodeKey: Preferences.Key<Int> =
        intPreferencesKey("last_seen_parent_info_version_code")

    val lastSeenVersionCodeFlow: Flow<Int?> =
        dataStore.data.map { prefs -> prefs[lastSeenVersionCodeKey] }

    suspend fun markSeenForVersion(versionCode: Int) {
        dataStore.edit { prefs ->
            prefs[lastSeenVersionCodeKey] = versionCode
        }
    }
}
