package com.tal.hebrewdino.ui.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

internal val Context.dataStore by preferencesDataStore(name = "prefs")

