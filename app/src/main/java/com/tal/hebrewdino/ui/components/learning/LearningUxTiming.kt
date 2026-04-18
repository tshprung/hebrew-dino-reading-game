package com.tal.hebrewdino.ui.components.learning

/** Shared timing for letter/path feedback so chapters feel consistent. */
object LearningUxTiming {
    const val AfterCorrectHoldMs: Long = 260L
    const val AfterWrongHoldMs: Long = 520L
    const val TapCooldownMs: Long = 165L
    const val CorrectPathWalkMs: Int = 800
    const val WrongPathWalkMs: Int = 450
}
