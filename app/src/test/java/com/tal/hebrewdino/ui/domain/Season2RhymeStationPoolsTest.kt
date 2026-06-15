package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2RhymeStationPoolsTest {
    @Test
    fun newRhymeWords_haveCatalogImageAndAudio() {
        Season2RhymePairCatalog.rhymeOnlyCatalogIds().forEach { id ->
            val check = Season2StationContentValidator.wordAssetCheck(id)
            assertTrue("$id should be valid", check?.isValid == true)
        }
    }

    @Test
    fun ch5_st5_hasFourOnClusterPairsWithVariedDistractors() {
        val scope =
            Season2RhymePairCatalog.wordCatalogIdsForRhymingStation(5, Season2ChapterContent.ch5Words)
        val pairs = Season2RhymePairCatalog.pairsForStation(5, 5)!!
        assertEquals(4, pairs.size)
        assertEquals(
            listOf(
                "w_ש_4" to "w_ח_3",
                "w_ב_2" to "w_ו_3",
                "w_ח_3" to "w_ב_2",
                "w_ו_3" to "w_ש_4",
            ),
            pairs.map { it.targetCatalogId to it.rhymeCatalogId },
        )
        val distractorSets = pairs.map { it.distractorCatalogIds.toSet() }.toSet()
        assertEquals(4, distractorSets.size)

        val plan = Season2ChapterStationPlans.quizPlan(Season2ChapterStationPlans.contextFor(5)!!, 5)
        assertEquals(4, plan.questionCount)
        assertTrue("w_ב_2" in (plan.season2WordCatalogIds ?: emptyList()))
        pairs.forEach { pair ->
            assertTrue(pair.targetCatalogId in scope)
            assertTrue(pair.rhymeCatalogId in scope)
            pair.distractorCatalogIds.forEach { assertTrue("$it in ch5 rhyme scope", it in scope) }
        }
    }

    @Test
    fun ch6_st4_hasFourDistinctRhymeFamilies() {
        val scope =
            Season2RhymePairCatalog.wordCatalogIdsForRhymingStation(6, Season2ChapterContent.ch6Words)
        val pairs = Season2RhymePairCatalog.pairsForStation(6, 4)!!
        assertEquals(4, pairs.size)
        assertTrue("w_ת_4" in scope)
        assertTrue("w_ד_5" in scope)
        assertTrue("w_ז_5" in scope)
        assertTrue("w_ג_5" in scope)

        val plan = Season2ChapterStationPlans.quizPlan(Season2ChapterStationPlans.contextFor(6)!!, 4)
        assertEquals(4, plan.questionCount)
        pairs.forEach { pair ->
            assertTrue(pair.targetCatalogId in scope)
            assertTrue(pair.rhymeCatalogId in scope)
        }
    }

    @Test
    fun ch7_st4_mixesOnClusterAndNewFamiliesWithoutRepeatingCh6Order() {
        val pairs = Season2RhymePairCatalog.pairsForStation(7, 4)!!
        assertEquals(6, pairs.size)
        val ch6Pairs = Season2RhymePairCatalog.pairsForStation(6, 4)!!.map { it.targetCatalogId to it.rhymeCatalogId }
        val ch7Pairs = pairs.map { it.targetCatalogId to it.rhymeCatalogId }
        assertNotEquals(ch6Pairs, ch7Pairs)

        val plan = Season2ChapterStationPlans.quizPlan(Season2ChapterStationPlans.contextFor(7)!!, 4)
        assertEquals(6, plan.questionCount)
        assertTrue("w_מ_6" in (plan.season2WordCatalogIds ?: emptyList()))
    }

    @Test
    fun rhymeStations_doNotReuseSingleDistractorPairAcrossAllRounds() {
        val allDistractors =
            (Season2RhymePairCatalog.pairsForStation(5, 5)!! +
                Season2RhymePairCatalog.pairsForStation(6, 4)!! +
                Season2RhymePairCatalog.pairsForStation(7, 4)!!)
                .flatMap { it.distractorCatalogIds }
        val counts = allDistractors.groupingBy { it }.eachCount()
        assertFalse(
            "שמש+פיל should not dominate every rhyme round",
            counts.getOrDefault("w_ש_1", 0) > 4 && counts.getOrDefault("w_פ_2", 0) > 4,
        )
    }
}
