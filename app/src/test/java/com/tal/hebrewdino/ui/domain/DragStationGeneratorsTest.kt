package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class DragStationGeneratorsTest {
    private val rnd = Random(7)

    @Test
    fun dragWordToPicture_generatesUniquePairsAndMatchingWordBank() {
        val ids = listOf("w_ד_1", "w_ב_1", "w_ל_2")
        val q =
            DragStationGenerators.dragWordToPicture(
                rnd = rnd,
                wordCatalogIds = ids,
                pairCount = 2,
            )
        assertEquals(2, q.pairs.size)
        assertEquals(2, q.wordBank.size)
        val pairIds = q.pairs.map { it.catalogEntryId }.toSet()
        assertEquals(2, pairIds.size)
        assertEquals(pairIds, q.wordBank.map { it.catalogEntryId }.toSet())
        q.pairs.forEach { pair ->
            val card = q.wordBank.first { it.catalogEntryId == pair.catalogEntryId }
            assertEquals(pair.word, card.word)
            assertTrue(pair.word.isNotEmpty())
        }
    }

    @Test
    fun dragWordToPicture_preservesHebrewWords() {
        val q =
            DragStationGenerators.dragWordToPicture(
                rnd = Random(1),
                wordCatalogIds = listOf("w_ד_1", "w_ח_3"),
                pairCount = 2,
            )
        val words = q.pairs.map { it.word }.toSet()
        assertTrue("דג" in words || words.any { it.contains('ד') })
        assertTrue(q.wordBank.all { it.word.any { ch -> ch.code > 0x590 } })
    }

    @Test
    fun dragWordToPicture_threePairsWhenEnoughCatalogEntries() {
        val ids = listOf("w_ד_1", "w_ב_1", "w_ל_2")
        val q =
            DragStationGenerators.dragWordToPicture(
                rnd = rnd,
                wordCatalogIds = ids,
                pairCount = 3,
            )
        assertEquals(3, q.pairs.size)
        assertEquals(3, q.wordBank.size)
    }

    @Test
    fun dragMissingLetter_firstLetter_partialWordAndOptions() {
        val q =
            DragStationGenerators.dragMissingLetter(
                rnd = rnd,
                catalogId = "w_ד_1",
                distractorLetters = listOf("ד", "ב", "מ", "ל"),
            )
        assertEquals("דג", q.word)
        assertEquals("_ג", q.partialWord)
        assertEquals("ד", q.correctLetter)
        assertEquals(0, q.missingIndex)
        assertTrue(q.correctLetter in q.optionLetters)
        assertTrue(q.optionLetters.distinct().size == q.optionLetters.size)
        q.optionLetters.filter { it != q.correctLetter }.forEach { distractor ->
            assertNotEquals(q.correctLetter, distractor)
        }
    }

    @Test
    fun dragMissingLetter_preservesHebrewPartialWord() {
        val q =
            DragStationGenerators.dragMissingLetter(
                rnd = Random(2),
                catalogId = "w_ש_1",
                distractorLetters = listOf("ש", "ס", "מ"),
            )
        assertEquals("שמש", q.word)
        assertEquals("_מש", q.partialWord)
        assertEquals("ש", q.correctLetter)
    }

    @Test
    fun dragMissingLetter_distractorsNeverEqualCorrect() {
        repeat(20) { seed ->
            val q =
                DragStationGenerators.dragMissingLetter(
                    rnd = Random(seed),
                    catalogId = "w_ח_3",
                    distractorLetters = listOf("ח", "ל", "ו", "ן"),
                    optionCount = 4,
                )
            assertEquals("ח", q.correctLetter)
            assertEquals(1, q.optionLetters.count { it == q.correctLetter })
            q.optionLetters.filter { it != q.correctLetter }.forEach { assertNotEquals("ח", it) }
        }
    }

    @Test
    fun dragMissingLetter_lastLetter_partialWord() {
        val q =
            DragStationGenerators.dragMissingLetter(
                rnd = rnd,
                catalogId = "w_ד_1",
                distractorLetters = listOf("ד", "ב", "מ", "ג"),
                missingIndex = 1,
            )
        assertEquals("דג", q.word)
        assertEquals(1, q.missingIndex)
        assertEquals("ד_", q.partialWord)
        assertEquals("ג", q.correctLetter)
    }

    @Test
    fun dragMissingLetter_middleLetter_partialWord() {
        val q =
            DragStationGenerators.dragMissingLetter(
                rnd = rnd,
                catalogId = "w_ש_1",
                distractorLetters = listOf("ש", "ס", "מ", "ר"),
                missingIndex = 1,
            )
        assertEquals("שמש", q.word)
        assertEquals(1, q.missingIndex)
        assertEquals("ש_ש", q.partialWord)
        assertEquals("מ", q.correctLetter)
    }

    @Test
    fun dragMissingLetter_questionInit_rejectsDuplicateOptions() {
        val letters = listOf("ד", "ב", "מ", "ל")
        repeat(10) { seed ->
            val q =
                DragStationGenerators.dragMissingLetter(
                    rnd = Random(seed),
                    catalogId = "w_ד_1",
                    distractorLetters = letters,
                    optionCount = 3,
                )
            assertEquals(3, q.optionLetters.size)
            assertEquals(q.optionLetters.toSet().size, q.optionLetters.size)
        }
    }
}
