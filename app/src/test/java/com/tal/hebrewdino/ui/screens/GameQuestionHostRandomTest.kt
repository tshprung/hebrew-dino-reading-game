package com.tal.hebrewdino.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.random.Random

class GameQuestionHostRandomTest {
    @Test
    fun pickRandomAvoiding_empty_isNull() {
        assertNull(pickRandomAvoiding(values = emptyArray(), avoid = null, random = Random(0)))
    }

    @Test
    fun pickRandomAvoiding_single_alwaysReturnsThatValue() {
        val values = arrayOf("A")
        assertEquals("A", pickRandomAvoiding(values = values, avoid = null, random = Random(0)))
        assertEquals("A", pickRandomAvoiding(values = values, avoid = "A", random = Random(0)))
    }

    @Test
    fun pickRandomAvoiding_avoids_whenPresent_and_sizeGreaterThan1() {
        val values = arrayOf("A", "B", "C", "D")
        val avoid = "C"
        repeat(50) { seed ->
            val picked = pickRandomAvoiding(values = values, avoid = avoid, random = Random(seed))
            assertNotEquals(avoid, picked)
        }
    }

    @Test
    fun pickRandomAvoiding_doesNotForceAvoid_whenNotPresent() {
        val values = arrayOf("A", "B", "D")
        val avoid = "C"
        repeat(10) { seed ->
            val picked = pickRandomAvoiding(values = values, avoid = avoid, random = Random(seed))
            val ok = values.contains(picked)
            assertEquals(true, ok)
        }
    }
}

