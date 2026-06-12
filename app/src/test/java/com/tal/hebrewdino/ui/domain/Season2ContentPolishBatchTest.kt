package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.Season2RawAudio
import com.tal.hebrewdino.ui.companion.CompanionAssets
import com.tal.hebrewdino.ui.data.DinoCharacter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2ContentPolishBatchTest {
    @Test
    fun ch2_st6_hasSixWordPartsRoundsIncludingPil() {
        val ch2 = Season2ChapterRegistry.chapter(2)!!
        assertTrue("w_פ_2" in ch2.wordCatalogIds)
        val plan = Season2Chapter1StationOrder.quizPlan(chapterIndex = 2, stationId = 6)
        assertEquals(6, plan.questionCount)
        assertEquals(Season2WordPartsPresentationMode.VisibleWordParts, plan.season2WordPartsPresentationMode)

        val pool =
            Season2WordPartsCatalog.entriesForPresentationMode(
                ch2.wordCatalogIds,
                Season2WordPartsPresentationMode.VisibleWordParts,
            )
        assertEquals(6, pool.size)
        val pil = pool.first { it.catalogId == "w_פ_2" }
        assertEquals("פי", pil.firstPart)
        assertEquals("ל", pil.secondPart)
    }

    @Test
    fun ch2_st6_wordPartsItems_haveImageFullWordAndPartAudio() {
        val ch2 = Season2ChapterRegistry.chapter(2)!!
        val pool =
            Season2WordPartsCatalog.entriesForPresentationMode(
                ch2.wordCatalogIds,
                Season2WordPartsPresentationMode.VisibleWordParts,
            )
        pool.forEach { entry ->
            val word = LessonWordCatalog.entries.first { it.id == entry.catalogId }
            assertNotEquals(R.drawable.lesson_pic_placeholder, word.tileRes)
            assertNotNull(AudioClips.wordRawResIdByCatalogId(entry.catalogId))
            assertNotNull(Season2RawAudio.wordPartRawResId(entry.catalogId, 1))
            assertNotNull(Season2RawAudio.wordPartRawResId(entry.catalogId, 2))
            assertTrue(Season2WordPartsCatalog.hasCompleteWordPartsAudio(entry.catalogId))
        }
    }

    @Test
    fun ch6_st5_rhymePairs_useSupportedWordsOnly() {
        val ch6Words = Season2ChapterContent.ch6Words
        val pairs = Season2RhymePairCatalog.pairsForWordIds(ch6Words)
        assertEquals(6, pairs.size)
        pairs.forEach { pair ->
            assertTrue(pair.targetCatalogId in ch6Words)
            assertTrue(pair.rhymeCatalogId in ch6Words)
            assertTrue(
                Season2StationContentValidator.validateRhymePair(
                    pair.targetCatalogId,
                    pair.rhymeCatalogId,
                ).isEmpty(),
            )
        }
    }

    @Test
    fun ch6_st4_questionCount_matchesValidPairPool() {
        val ctx = Season2ChapterStationPlans.contextFor(6)!!
        val plan = Season2ChapterStationPlans.quizPlan(ctx, 4)
        assertEquals(Season2AdvancedStationMode.Rhyming, plan.season2AdvancedMode)
        assertTrue(plan.questionCount >= 3)
    }

    @Test
    fun ch6_st5_finalRhymePairs_includeShaonOnCluster() {
        val pairs = Season2RhymePairCatalog.pairsForWordIds(Season2ChapterContent.ch6Words)
        assertTrue(pairs.any { it.targetCatalogId == "w_ש_4" && it.rhymeCatalogId == "w_ב_2" })
        assertTrue(pairs.any { it.targetCatalogId == "w_ש_4" && it.rhymeCatalogId == "w_ח_3" })
        assertTrue(pairs.any { it.targetCatalogId == "w_ב_2" && it.rhymeCatalogId == "w_ח_3" })
    }

    @Test
    fun season1_unchanged() {
        val plan = StationQuizPlans.chapter1(Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH)
        assertNull(plan.season2AdvancedMode)
    }

    @Test
    fun noLegacyDinoAssets() {
        val dino = CompanionAssets.forCharacter(DinoCharacter.Dino)
        assertEquals(R.drawable.companion_dino_idle, dino.poseIdle)
    }

    @Test
    fun noSeason2WavInAudioClips() {
        assertFalse(Season2StabilityAudit.audioClipsContainsSeason2WavPath())
    }
}
