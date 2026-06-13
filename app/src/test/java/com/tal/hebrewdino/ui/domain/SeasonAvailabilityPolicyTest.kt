package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.BuildConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SeasonAvailabilityPolicyTest {
    @Test
    fun season1_always_enabled() {
        assertTrue(SeasonAvailabilityPolicy.isSeasonEnabled(seasonId = 1))
    }

    @Test
    fun season2_enabled_only_in_debug_builds() {
        assertEquals(BuildConfig.DEBUG, SeasonAvailabilityPolicy.isSeason2Enabled())
        assertEquals(BuildConfig.DEBUG, SeasonAvailabilityPolicy.isSeasonEnabled(seasonId = 2))
    }

    @Test
    fun future_seasons_disabled() {
        assertFalse(SeasonAvailabilityPolicy.isSeasonEnabled(seasonId = 3))
        assertFalse(SeasonAvailabilityPolicy.isSeasonEnabled(seasonId = 4))
    }
}
