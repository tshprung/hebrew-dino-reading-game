package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2GuessingDetectorTest {
    @Test
    fun singleWrongAttempt_doesNotTrigger() {
        val detector = Season2GuessingDetector()
        assertFalse(detector.recordWrongAttempt(nowMs = 1_000L))
    }

    @Test
    fun twoWrongAttemptsSameRound_triggers() {
        val detector = Season2GuessingDetector()
        assertFalse(detector.recordWrongAttempt(nowMs = 1_000L))
        assertTrue(detector.recordWrongAttempt(nowMs = 1_500L))
    }

    @Test
    fun threeWrongAttemptsWithinWindow_triggers() {
        val detector = Season2GuessingDetector()
        assertFalse(detector.recordWrongAttempt(nowMs = 1_000L))
        detector.onNewRound()
        assertFalse(detector.recordWrongAttempt(nowMs = 2_000L))
        detector.onNewRound()
        assertTrue(detector.recordWrongAttempt(nowMs = 3_500L))
    }

    @Test
    fun rapidTapsWithinWindow_triggers() {
        val detector = Season2GuessingDetector()
        assertFalse(detector.recordWrongAttempt(nowMs = 1_000L))
        detector.onNewRound()
        assertFalse(detector.recordWrongAttempt(nowMs = 1_200L))
        detector.onNewRound()
        assertTrue(detector.recordWrongAttempt(nowMs = 1_700L))
    }

    @Test
    fun interventionAcknowledged_resetsCounters() {
        val detector = Season2GuessingDetector()
        detector.recordWrongAttempt(nowMs = 1_000L)
        detector.recordWrongAttempt(nowMs = 1_500L)
        detector.onInterventionAcknowledged()
        assertFalse(detector.recordWrongAttempt(nowMs = 2_000L))
    }

    @Test
    fun memoryMatchMode_ignoresWrongAttempts() {
        val detector = Season2GuessingDetector(memoryMatchMode = true)
        assertFalse(detector.recordWrongAttempt(nowMs = 1_000L))
        assertFalse(detector.recordWrongAttempt(nowMs = 1_100L))
        assertFalse(detector.recordWrongAttempt(nowMs = 1_200L))
    }

    @Test
    fun memoryMatchMode_triggersOnRapidTapsOnly() {
        val detector = Season2GuessingDetector(memoryMatchMode = true)
        assertFalse(detector.recordTap(nowMs = 1_000L))
        assertFalse(detector.recordTap(nowMs = 1_300L))
        assertTrue(detector.recordTap(nowMs = 1_700L))
    }

    @Test
    fun memoryMatchMode_twoTapsDoNotTrigger() {
        val detector = Season2GuessingDetector(memoryMatchMode = true)
        assertFalse(detector.recordTap(nowMs = 1_000L))
        assertFalse(detector.recordTap(nowMs = 1_400L))
    }
}
