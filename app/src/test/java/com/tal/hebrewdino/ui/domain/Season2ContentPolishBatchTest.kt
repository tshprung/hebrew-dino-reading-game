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
                stationChapterIndex = 2,
                stationId = 6,
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
                stationChapterIndex = 2,
                stationId = 6,
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
    fun ch6_st4_rhymePairs_useStationPoolOnly() {
        val scope =
            Season2RhymePairCatalog.wordCatalogIdsForRhymingStation(6, Season2ChapterContent.ch6Words)
        val pairs = Season2RhymePairCatalog.pairsForStation(6, 4)!!
        assertEquals(4, pairs.size)
        pairs.forEach { pair ->
            assertTrue(pair.targetCatalogId in scope)
            assertTrue(pair.rhymeCatalogId in scope)
            assertTrue(
                Season2StationContentValidator.validateRhymePair(
                    pair.targetCatalogId,
                    pair.rhymeCatalogId,
                ).isEmpty(),
            )
        }
    }

    @Test
    fun ch6_st4_questionCount_matchesStationPairPool() {
        val ctx = Season2ChapterStationPlans.contextFor(6)!!
        val plan = Season2ChapterStationPlans.quizPlan(ctx, 4)
        assertEquals(Season2AdvancedStationMode.Rhyming, plan.season2AdvancedMode)
        assertEquals(4, plan.questionCount)
    }

    @Test
    fun ch6_st4_rhymePairs_includeKofTofAndRegelDegel() {
        val pairs = Season2RhymePairCatalog.pairsForStation(6, 4)!!
        assertTrue(pairs.any { it.targetCatalogId == "w_ק_1" && it.rhymeCatalogId == "w_ת_4" })
        assertTrue(pairs.any { it.targetCatalogId == "w_ר_3" && it.rhymeCatalogId == "w_ד_5" })
        assertTrue(pairs.any { it.targetCatalogId == "w_ד_1" && it.rhymeCatalogId == "w_ג_5" })
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
