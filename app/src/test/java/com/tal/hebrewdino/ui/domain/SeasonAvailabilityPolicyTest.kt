package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SeasonAvailabilityPolicyTest {
    @Test
    fun season1_and_season2_enabled() {
        assertTrue(SeasonAvailabilityPolicy.isSeasonEnabled(seasonId = 1))
        assertTrue(SeasonAvailabilityPolicy.isSeason2Enabled())
    }

    @Test
    fun future_seasons_disabled() {
        assertFalse(SeasonAvailabilityPolicy.isSeasonEnabled(seasonId = 3))
        assertFalse(SeasonAvailabilityPolicy.isSeasonEnabled(seasonId = 4))
    }
}
