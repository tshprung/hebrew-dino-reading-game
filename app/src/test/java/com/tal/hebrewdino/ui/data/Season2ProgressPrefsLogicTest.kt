package com.tal.hebrewdino.ui.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Documents persisted Season 2 intro flags (mirrors DataStore string values). */
class Season2ProgressPrefsLogicTest {
    @Test
    fun seasonIntroDismissed_readsStoredFlag() {
        assertTrue(parseSeasonIntroDismissed("1"))
        assertFalse(parseSeasonIntroDismissed(null))
        assertFalse(parseSeasonIntroDismissed(""))
    }

    private fun parseSeasonIntroDismissed(raw: String?): Boolean = raw == "1"
}
