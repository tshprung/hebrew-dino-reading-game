package com.tal.hebrewdino.ui.domain

/**
 * Tracks wrong attempts and rapid taps to detect brute-force guessing in Season 2 Chapter 1.
 * Pure logic — no Android dependencies (unit-testable).
 */
class Season2GuessingDetector(
    /** When true, only rapid-tap detection applies (memory match). */
    private val memoryMatchMode: Boolean = false,
    private val wrongThresholdSameRound: Int = 2,
    private val wrongThresholdWindow: Int = 3,
    private val wrongWindowMs: Long = 4_000L,
    private val rapidTapWindowMs: Long = 900L,
    private val rapidTapMinCount: Int = 3,
) {
    private var wrongThisRound: Int = 0
    private val wrongTimestamps = ArrayDeque<Long>()
    private val tapTimestamps = ArrayDeque<Long>()

    fun onNewRound() {
        wrongThisRound = 0
    }

    fun onCorrect() {
        wrongThisRound = 0
        wrongTimestamps.clear()
        tapTimestamps.clear()
    }

    fun onInterventionAcknowledged() {
        wrongThisRound = 0
        wrongTimestamps.clear()
        tapTimestamps.clear()
    }

    /**
     * Records a wrong answer attempt. Returns true when a gentle coach intervention should run.
     * A single mistake does not trigger intervention.
     */
    fun recordWrongAttempt(nowMs: Long = System.currentTimeMillis()): Boolean {
        if (memoryMatchMode) return false
        recordTapInternal(nowMs)
        wrongThisRound++
        wrongTimestamps.addLast(nowMs)
        pruneDeque(wrongTimestamps, nowMs - wrongWindowMs)
        return shouldIntervene(nowMs)
    }

    /**
     * Records any answer tap (memory match). Returns true when rapid random tapping is detected.
     */
    fun recordTap(nowMs: Long = System.currentTimeMillis()): Boolean {
        recordTapInternal(nowMs)
        if (!memoryMatchMode) return false
        return tapTimestamps.size >= rapidTapMinCount
    }

    private fun recordTapInternal(nowMs: Long) {
        tapTimestamps.addLast(nowMs)
        pruneDeque(tapTimestamps, nowMs - rapidTapWindowMs)
    }

    private fun shouldIntervene(nowMs: Long): Boolean {
        if (wrongThisRound >= wrongThresholdSameRound) return true
        pruneDeque(wrongTimestamps, nowMs - wrongWindowMs)
        if (wrongTimestamps.size >= wrongThresholdWindow) return true
        if (tapTimestamps.size >= rapidTapMinCount) return true
        return false
    }

    private fun pruneDeque(
        deque: ArrayDeque<Long>,
        minAllowed: Long,
    ) {
        while (deque.isNotEmpty() && deque.first() < minAllowed) {
            deque.removeFirst()
        }
    }
}
