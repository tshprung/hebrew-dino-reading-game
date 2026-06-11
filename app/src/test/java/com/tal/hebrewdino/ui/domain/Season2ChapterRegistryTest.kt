package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2ChapterRegistryTest {
    @Test
    fun registry_hasSixChapters() {
        assertEquals(6, Season2ChapterRegistry.CHAPTER_COUNT)
        assertEquals(6, Season2ChapterRegistry.chapters.size)
    }

    @Test
    fun chapter1_andChapter2_arePlayable() {
        assertTrue(Season2ChapterRegistry.isPlayable(1))
        assertTrue(Season2ChapterRegistry.isPlayable(2))
    }

    @Test
    fun chapters3Through6_unlockOnlyWhenFullyValidated() {
        for (index in 3..6) {
            val def = Season2ChapterRegistry.chapter(index)!!
            if (def.isPlayable) {
                assertEquals(Season2ChapterLockReason.QaReady, def.lockReason)
                assertTrue(def.missingAssetsReport.isEmpty())
                assertTrue(def.validation.qaReady)
            } else {
                assertTrue(def.missingAssetsReport.isNotEmpty())
            }
        }
        assertTrue(Season2ChapterRegistry.isPlayable(3))
    }

    @Test
    fun allSixChapters_playableWhenCatalogAndStationsValidate() {
        assertEquals((1..6).toList(), Season2ChapterRegistry.playableChapterIndices())
        for (index in 3..6) {
            assertTrue(Season2ChapterRegistry.isPlayable(index))
        }
    }

    @Test
    fun chapters3Through6_haveUniquePosterAssets() {
        val trexPoster = Season2ChapterRegistry.chapter(1)!!.posterPuzzleResId
        val posters =
            (3..6).map { index ->
                val def = Season2ChapterRegistry.chapter(index)!!
                assertNotNull(def.posterPuzzleResId)
                assertNotEquals(trexPoster, def.posterPuzzleResId)
                def.posterPuzzleResId!!
            }.toSet()
        assertEquals(4, posters.size)
    }

    @Test
    fun lockedChapters_doNotReuseTrexPoster() {
        val trexPoster = Season2ChapterRegistry.chapter(1)!!.posterPuzzleResId
        for (index in 2..6) {
            val poster = Season2ChapterRegistry.chapter(index)?.posterPuzzleResId
            assertNotNull("Chapter $index should have its own poster", poster)
            assertNotEquals(
                "Chapter $index must not use T-Rex poster",
                trexPoster,
                poster,
            )
        }
    }

    @Test
    fun chapter2_passesAssetValidation() {
        val ch2 = Season2ChapterRegistry.chapter(2)!!
        assertTrue(ch2.validation.qaReady)
        assertTrue(ch2.isQaReady)
        assertTrue(ch2.missingAssetsReport.isEmpty())
    }

    @Test
    fun revealedName_onlyForQaReadyChapters() {
        assertEquals("טירנוזאורוס", Season2ChapterRegistry.revealedName(1))
        assertEquals("טריצרטופס", Season2ChapterRegistry.revealedName(2))
        for (index in 3..6) {
            val def = Season2ChapterRegistry.chapter(index)!!
            if (def.isPlayable) {
                assertEquals(def.dinosaurNameHebrew, Season2ChapterRegistry.revealedName(index))
            } else {
                assertNull(Season2ChapterRegistry.revealedName(index))
            }
        }
    }

    @Test
    fun chapterUnlock_requiresPreviousChapterComplete() {
        assertTrue(Season2ChapterRegistry.isChapterUnlocked(1, emptySet()))
        assertFalse(Season2ChapterRegistry.isChapterUnlocked(2, emptySet()))
        assertTrue(Season2ChapterRegistry.isChapterUnlocked(2, setOf(1)))
        assertFalse(Season2ChapterRegistry.isChapterUnlocked(3, setOf(1)))
        assertTrue(Season2ChapterRegistry.isChapterUnlocked(3, setOf(1, 2)))
    }

    @Test
    fun isChapterComplete_requiresAllStationsForPlayableChapter() {
        val allStations = (1..Season2StandardRevealOrder.STATION_COUNT).toSet()
        assertTrue(
            Season2ChapterRegistry.isChapterComplete(
                chapterIndex = 1,
                completedChapters = emptySet(),
                completedStations = allStations,
            ),
        )
        assertFalse(
            Season2ChapterRegistry.isChapterComplete(
                chapterIndex = 2,
                completedChapters = emptySet(),
                completedStations = setOf(1, 2, 3),
            ),
        )
        assertTrue(
            Season2ChapterRegistry.isChapterComplete(
                chapterIndex = 3,
                completedChapters = emptySet(),
                completedStations = allStations,
            ),
        )
    }

    @Test
    fun headTile_revealsLast_onStation6() {
        assertEquals(1, Season2StandardRevealOrder.HEAD_POSTER_TILE)
        assertEquals(1, Season2StandardRevealOrder.posterTileForStation(6))
        assertEquals(6, Season2StandardRevealOrder.stationForPosterTile(1))
    }

    @Test
    fun chapter2_hasFootprintsTheme_andDistinctLetters() {
        val ch2 = Season2ChapterRegistry.chapter(2)!!
        assertEquals(Season2StationTheme.Footprints, ch2.stationTheme)
        assertEquals(listOf("ח", "ר", "ק", "ש", "מ"), ch2.letters)
        assertEquals(5, ch2.letters.distinct().size)
        assertNotNull(ch2.posterPuzzleResId)
    }

    @Test
    fun chapter6_marineCreatureLabel() {
        val ch6 = Season2ChapterRegistry.chapter(6)!!
        assertEquals("יצור ימי קדום", ch6.mysteryCreatureLabel)
        assertEquals("מוזאזאורוס", ch6.dinosaurNameHebrew)
    }
}
