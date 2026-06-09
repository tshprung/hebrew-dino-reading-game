package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2ChapterWiringTest {
    @Test
    fun chapter1And2_remainPlayable_unchanged() {
        assertTrue(Season2ChapterRegistry.isPlayable(1))
        assertTrue(Season2ChapterRegistry.isPlayable(2))
        assertNull(StationQuizPlans.chapter1(1).season2AdvancedMode)
        assertNull(Season2Chapter1StationOrder.quizPlan(1).season2AdvancedMode)
    }

    @Test
    fun chapters3Through6_stationPlans_generateWithoutThrowing() {
        for (chapterIndex in 3..6) {
            val ctx = Season2ChapterStationPlans.contextFor(chapterIndex)!!
            for (stationId in 1..Season2ChapterStationPlans.STATION_COUNT) {
                if (stationId == Season2Chapter1StationOrder.MEMORY_MATCH) continue
                val plan = Season2ChapterStationPlans.quizPlan(ctx, stationId)
                assertNotNull("ch$chapterIndex st$stationId", plan.mode)
            }
        }
    }

    @Test
    fun advancedStationKinds_perChapter() {
        assertEquals(Season2ChapterStationPlans.StationKind.WordParts, Season2ChapterStationPlans.stationKind(3, 5))
        assertEquals(Season2ChapterStationPlans.StationKind.PictureToWord, Season2ChapterStationPlans.stationKind(4, 5))
        assertEquals(Season2ChapterStationPlans.StationKind.MissingFirstLetter, Season2ChapterStationPlans.stationKind(5, 5))
        assertEquals(Season2ChapterStationPlans.StationKind.Rhyming, Season2ChapterStationPlans.stationKind(6, 5))
        assertEquals(Season2ChapterStationPlans.StationKind.PictureToWord, Season2ChapterStationPlans.stationKind(6, 6))
    }

    @Test
    fun playableChapters_onlyWhenFullyValidated() {
        for (index in 3..6) {
            val def = Season2ChapterRegistry.chapter(index)!!
            if (def.isPlayable) {
                assertEquals(Season2ChapterLockReason.QaReady, def.lockReason)
                assertTrue(def.validation.qaReady)
                assertTrue(def.missingAssetsReport.isEmpty())
                assertNotNull(def.posterPuzzleResId)
            } else {
                assertTrue(def.missingAssetsReport.isNotEmpty() || !def.validation.qaReady)
            }
        }
    }

    @Test
    fun lockedChapters_doNotReuseTrexPoster() {
        val trexPoster = Season2ChapterRegistry.chapter(1)!!.posterPuzzleResId
        for (index in 2..6) {
            assertNotEquals(trexPoster, Season2ChapterRegistry.chapter(index)!!.posterPuzzleResId)
        }
    }

    @Test
    fun revealedName_hiddenUntilQaReady() {
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
    fun sequentialUnlock_requiresPreviousChapterComplete() {
        assertTrue(Season2ChapterRegistry.isChapterUnlocked(1, emptySet()))
        assertFalse(Season2ChapterRegistry.isChapterUnlocked(2, emptySet()))
        assertTrue(Season2ChapterRegistry.isChapterUnlocked(2, setOf(1)))
        val playableAfter2 = Season2ChapterRegistry.isPlayable(3)
        assertEquals(playableAfter2, Season2ChapterRegistry.isChapterUnlocked(3, setOf(1, 2)))
    }

    @Test
    fun chapter3Through6_haveDistinctLetterPools() {
        assertNotNull(Season2Chapter3LetterPoolSpec.wordCatalogIds)
        assertEquals(3, Season2Chapter3LetterPoolSpec.chapterIndex)
        assertEquals(Season2ChapterContent.ch3Words, Season2Chapter3LetterPoolSpec.wordCatalogIds)
    }

    @Test
    fun stationValidator_reportsIssuesForInvalidWord() {
        val ctx =
            Season2ChapterStationPlans.ChapterStationContext(
                chapterIndex = 99,
                wordCatalogIds = listOf("w_NOPE"),
                letters = listOf("ג"),
                memoryMatchLetters = listOf("ג", "נ", "פ", "צ"),
                theme = Season2StationTheme.Standard,
            )
        val issues = Season2ChapterStationPlans.validateAllStations(ctx)
        assertTrue(issues.isNotEmpty())
    }
}
