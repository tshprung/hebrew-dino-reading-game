package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2ChapterValidatorTest {
    @Test
    fun evaluate_allRegistryChapters_areQaReady() {
        for (chapterIndex in 1..Season2ChapterRegistry.CHAPTER_COUNT) {
            val def = Season2ChapterRegistry.chapter(chapterIndex)!!
            val readiness =
                Season2ChapterStationValidator.evaluate(
                    chapterIndex = chapterIndex,
                    posterResId = def.posterPuzzleResId,
                    letters = def.letters,
                    wordCatalogIds = def.wordCatalogIds,
                    memoryMatchLetters = def.memoryMatchLetters,
                    letterPoolSpec = def.letterPoolSpec,
                    stationContext = def.stationContext,
                )
            assertTrue(
                "ch$chapterIndex readiness issues: ${readiness.issues}",
                readiness.qaReady,
            )
            assertEquals(Season2ChapterLockReason.QaReady, readiness.lockReason)
            assertTrue(readiness.issues.isEmpty())
        }
    }

    @Test
    fun assetValidator_rejectsNullPoster() {
        val ch3 = Season2ChapterRegistry.chapter(3)!!
        val result =
            Season2ChapterAssetValidator.validate(
                posterResId = null,
                letters = ch3.letters,
                wordCatalogIds = ch3.wordCatalogIds,
                forbidTrexPoster = true,
            )
        assertFalse(result.qaReady)
        assertTrue(result.missingAssets.any { it.contains("poster") })
    }

    @Test
    fun assetValidator_rejectsTrexPosterWhenForbidden() {
        val ch3 = Season2ChapterRegistry.chapter(3)!!
        val result =
            Season2ChapterAssetValidator.validate(
                posterResId = Season2ChapterAssetValidator.TREX_POSTER_RES,
                letters = ch3.letters,
                wordCatalogIds = ch3.wordCatalogIds,
                forbidTrexPoster = true,
            )
        assertFalse(result.qaReady)
        assertTrue(result.missingAssets.any { it.contains("unique poster") })
    }

    @Test
    fun assetValidator_rejectsMissingCatalogEntry() {
        val ch3 = Season2ChapterRegistry.chapter(3)!!
        val result =
            Season2ChapterAssetValidator.validate(
                posterResId = ch3.posterPuzzleResId,
                letters = ch3.letters,
                wordCatalogIds = ch3.wordCatalogIds + "w_NOPE",
                forbidTrexPoster = true,
            )
        assertFalse(result.qaReady)
        assertTrue(result.missingAssets.any { it.contains("w_NOPE") })
    }

    @Test
    fun assetValidator_rejectsLetterWithFewerThanTwoWords() {
        val ch3 = Season2ChapterRegistry.chapter(3)!!
        val singleWordForGimel =
            ch3.wordCatalogIds.filter { id ->
                LessonWordCatalog.entries.find { it.id == id }?.letter == "ג"
            }.take(1)
        val result =
            Season2ChapterAssetValidator.validate(
                posterResId = ch3.posterPuzzleResId,
                letters = listOf("ג"),
                wordCatalogIds = singleWordForGimel,
                forbidTrexPoster = true,
            )
        assertFalse(result.qaReady)
        assertTrue(
            result.missingAssets.any { it.contains("at least 2 catalog words") && it.contains("ג") },
        )
    }

    @Test
    fun stationValidator_rejectsWrongLetterPoolChapterIndex() {
        val ch3 = Season2ChapterRegistry.chapter(3)!!
        val readiness =
            Season2ChapterStationValidator.evaluate(
                chapterIndex = 3,
                posterResId = ch3.posterPuzzleResId,
                letters = ch3.letters,
                wordCatalogIds = ch3.wordCatalogIds,
                memoryMatchLetters = ch3.memoryMatchLetters,
                letterPoolSpec = Season2Chapter4LetterPoolSpec,
                stationContext = ch3.stationContext,
            )
        assertFalse(readiness.qaReady)
        assertTrue(readiness.issues.any { it.contains("letter pool chapter index mismatch") })
    }

    @Test
    fun stationValidator_rejectsEmptyMemoryMatchLetters() {
        val ch3 = Season2ChapterRegistry.chapter(3)!!
        val readiness =
            Season2ChapterStationValidator.evaluate(
                chapterIndex = 3,
                posterResId = ch3.posterPuzzleResId,
                letters = ch3.letters,
                wordCatalogIds = ch3.wordCatalogIds,
                memoryMatchLetters = emptyList(),
                letterPoolSpec = ch3.letterPoolSpec,
                stationContext = ch3.stationContext,
            )
        assertFalse(readiness.qaReady)
        assertTrue(readiness.issues.any { it.contains("memory match letters empty") })
    }

    @Test
    fun stationValidator_chapter1_allowsTrexPoster() {
        val ch1 = Season2ChapterRegistry.chapter(1)!!
        val readiness =
            Season2ChapterStationValidator.evaluate(
                chapterIndex = 1,
                posterResId = ch1.posterPuzzleResId,
                letters = ch1.letters,
                wordCatalogIds = ch1.wordCatalogIds,
                memoryMatchLetters = ch1.memoryMatchLetters,
                letterPoolSpec = ch1.letterPoolSpec,
                stationContext = null,
            )
        assertTrue(readiness.qaReady)
        assertEquals(R.drawable.season2_trex_puzzle_full, ch1.posterPuzzleResId)
    }
}
