package com.tal.hebrewdino.ui.domain

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PopBalloonsGeneratorTest {
    private val gen = PopBalloonsGenerator(LetterPoolSpec.Default)

    @Test
    fun generatedQuestion_containsCorrectAnswer_andExpectedOptionCount() {
        val rnd = Random(42)
        val group = listOf("א", "ב", "ד")
        val q = gen.generate(rnd, group, correctAnswer = "ב", optionCount = 9)
        assertTrue("ב" in q.options)
        assertEquals(9, q.options.size)
        assertEquals("ב", q.correctAnswer)
    }

    @Test
    fun generatedQuestion_includesAllDistinctLettersFromGroup_whenOptionCountAllows() {
        val rnd = Random(7)
        val group = listOf("א", "ב", "ד")
        val q = gen.generate(rnd, group, correctAnswer = "א", optionCount = 9)
        for (letter in group.distinct()) {
            assertTrue("$letter missing from ${q.options}", letter in q.options)
        }
    }
}
