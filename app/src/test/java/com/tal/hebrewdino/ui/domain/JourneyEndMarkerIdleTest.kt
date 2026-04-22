package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JourneyEndMarkerIdleTest {
    @Test
    fun afterOutroSeen_dinoIdlesAtEndMarker() {
        val base = 5f
        val max = 6f
        val idle =
            JourneyEndMarkerIdle.idleProgressAfterAllPlayableStationsComplete(
                canWalkToEndMarker = true,
                endMarkerReached = true,
                baseMaxDinoF = base,
                maxDinoF = max,
            )
        assertEquals(max, idle, 0f)
    }

    @Test
    fun beforeOutroSeen_dinoIdlesAtLastStation() {
        val base = 5f
        val max = 6f
        val idle =
            JourneyEndMarkerIdle.idleProgressAfterAllPlayableStationsComplete(
                canWalkToEndMarker = true,
                endMarkerReached = false,
                baseMaxDinoF = base,
                maxDinoF = max,
            )
        assertEquals(base, idle, 0f)
    }

    @Test
    fun suppressRepeatAutoWalk_onlyWhenChapterEndAndAlreadyReachedMarker() {
        assertTrue(JourneyEndMarkerIdle.suppressRepeatEndMarkerAutoWalk(endMarkerReached = true, canWalkToEndMarker = true))
        assertFalse(JourneyEndMarkerIdle.suppressRepeatEndMarkerAutoWalk(endMarkerReached = false, canWalkToEndMarker = true))
        assertFalse(JourneyEndMarkerIdle.suppressRepeatEndMarkerAutoWalk(endMarkerReached = true, canWalkToEndMarker = false))
    }
}
