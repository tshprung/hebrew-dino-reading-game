package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2DragMissingLetterWordPoolsTest {
    @Test
    fun eachChapter_hasAtLeastSixEligibleWords() {
        for (chapter in 1..Season2ChapterRegistry.CHAPTER_COUNT) {
            val pool = Season2DragMissingLetterWordPools.wordCatalogIds(chapter)
            assertTrue(
                "Ch$chapter drag-missing pool needs ≥${Season2DragMissingLetterWordPools.MIN_WORD_COUNT} words; got ${pool.size}: $pool",
                pool.size >= Season2DragMissingLetterWordPools.MIN_WORD_COUNT,
            )
        }
    }

    @Test
    fun ch5_and_ch6_pools_doNotOverlap() {
        val ch5 = Season2DragMissingLetterWordPools.wordCatalogIds(5).toSet()
        val ch6 = Season2DragMissingLetterWordPools.wordCatalogIds(6).toSet()
        assertTrue(ch5.isNotEmpty())
        assertTrue(ch6.isNotEmpty())
        assertTrue("Ch5∩Ch6 should be empty; overlap=${ch5.intersect(ch6)}", ch5.intersect(ch6).isEmpty())
    }

    @Test
    fun ch1_and_ch7_pools_doNotOverlap() {
        val ch1 = Season2DragMissingLetterWordPools.wordCatalogIds(1).toSet()
        val ch7 = Season2DragMissingLetterWordPools.wordCatalogIds(7).toSet()
        assertTrue(ch1.isNotEmpty())
        assertTrue(ch7.isNotEmpty())
        assertTrue("Ch1∩Ch7 should be empty; overlap=${ch1.intersect(ch7)}", ch1.intersect(ch7).isEmpty())
    }

    @Test
    fun ch4_and_ch6_pools_doNotOverlap() {
        val ch4 = Season2DragMissingLetterWordPools.wordCatalogIds(4).toSet()
        val ch6 = Season2DragMissingLetterWordPools.wordCatalogIds(6).toSet()
        assertTrue(ch4.isNotEmpty())
        assertTrue(ch6.isNotEmpty())
        assertTrue("Ch4∩Ch6 should be empty; overlap=${ch4.intersect(ch6)}", ch4.intersect(ch6).isEmpty())
    }

    @Test
    fun ch6_usesChapter6ReviewLetterBatch_notChapter4Repeat() {
        val ch6Pool = Season2DragMissingLetterWordPools.wordCatalogIds(6)
        val ch6Letters = setOf("ח", "ר", "ק", "ש", "פ", "צ")
        assertEquals(
            ch6Letters,
            ch6Pool.mapNotNull { id -> LessonWordCatalog.entries.find { it.id == id }?.letter }.toSet(),
        )
        val ch4Pool = Season2DragMissingLetterWordPools.wordCatalogIds(4).toSet()
        assertTrue("Ch6 must not reuse Ch4 drag-missing words", ch6Pool.intersect(ch4Pool).isEmpty())
    }

    @Test
    fun stationPlans_wireDedicatedPools() {
        val ch1Drag = Season2Chapter1StationOrder.quizPlan(1, Season2Chapter1StationOrder.FINALE_STATION)
        assertEquals(
            Season2DragMissingLetterWordPools.wordCatalogIds(1),
            ch1Drag.season2WordCatalogIds,
        )

        val ch2Drag = Season2Chapter1StationOrder.quizPlan(2, 5)
        assertEquals(
            Season2DragMissingLetterWordPools.wordCatalogIds(2),
            ch2Drag.season2WordCatalogIds,
        )

        for (chapter in 3..7) {
            val ctx = Season2ChapterStationPlans.contextFor(chapter)!!
            val stationId =
                when (chapter) {
                    3 -> 3
                    4 -> 4
                    5 -> 3
                    6 -> 3
                    7 -> 2
                    else -> error("unexpected")
                }
            val plan = Season2ChapterStationPlans.quizPlan(ctx, stationId)
            assertEquals(StationQuizMode.DragMissingLetter, plan.mode)
            assertEquals(
                Season2DragMissingLetterWordPools.wordCatalogIds(chapter),
                plan.season2WordCatalogIds,
            )
        }
    }
}
