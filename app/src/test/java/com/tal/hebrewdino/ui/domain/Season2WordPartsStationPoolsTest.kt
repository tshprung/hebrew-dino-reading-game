package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2WordPartsStationPoolsTest {
    @Test
    fun ch3_guidedPool_usesChapterNativeWords_disjointFromHidden() {
        val scope = Season2WordPartsCatalog.wordCatalogIdsForChapter3WordParts(Season2ChapterContent.ch3Words)
        val guided =
            Season2WordPartsCatalog.entriesForPresentationMode(
                scope,
                Season2WordPartsPresentationMode.GuidedWordParts,
                stationChapterIndex = 3,
                stationId = 5,
            ).map { it.catalogId }
        val hidden =
            Season2WordPartsCatalog.entriesForPresentationMode(
                scope,
                Season2WordPartsPresentationMode.HiddenWordPartsChallenge,
                stationChapterIndex = 3,
                stationId = 6,
            ).map { it.catalogId }
        assertEquals(setOf("w_ג_1", "w_ג_3", "w_נ_2", "w_צ_2", "w_פ_2", "w_ש_1"), guided.toSet())
        assertTrue("w_פ_2" in guided)
        assertFalse("w_ח_2" in guided)
        assertTrue(guided.intersect(hidden.toSet()).isEmpty())
    }

    @Test
    fun ch2_and_ch3_guided_shareFewerWordsThanBefore() {
        val ch2Visible =
            Season2WordPartsCatalog.entriesForPresentationMode(
                Season2ChapterContent.ch2Words,
                Season2WordPartsPresentationMode.VisibleWordParts,
                stationChapterIndex = 2,
                stationId = 6,
            ).map { it.catalogId }
            .toSet()
        val ch3Guided =
            Season2WordPartsCatalog.entriesForPresentationMode(
                Season2WordPartsCatalog.wordCatalogIdsForChapter3WordParts(Season2ChapterContent.ch3Words),
                Season2WordPartsPresentationMode.GuidedWordParts,
                stationChapterIndex = 3,
                stationId = 5,
            ).map { it.catalogId }
            .toSet()
        assertEquals(6, ch2Visible.size)
        assertEquals(6, ch3Guided.size)
        assertEquals(setOf("w_פ_2", "w_ש_1"), ch2Visible.intersect(ch3Guided))
    }

    @Test
    fun eachWordPartsStation_hasSixRounds() {
        val stations =
            listOf(
                Triple(2, 6, Season2WordPartsPresentationMode.VisibleWordParts to Season2ChapterContent.ch2Words),
                Triple(
                    3,
                    5,
                    Season2WordPartsPresentationMode.GuidedWordParts to
                        Season2WordPartsCatalog.wordCatalogIdsForChapter3WordParts(Season2ChapterContent.ch3Words),
                ),
                Triple(
                    3,
                    6,
                    Season2WordPartsPresentationMode.HiddenWordPartsChallenge to
                        Season2WordPartsCatalog.wordCatalogIdsForChapter3WordParts(Season2ChapterContent.ch3Words),
                ),
                Triple(
                    5,
                    6,
                    Season2WordPartsPresentationMode.GuidedWordParts to
                        Season2WordPartsCatalog.wordCatalogIdsForChapter5WordParts(Season2ChapterContent.ch5Words),
                ),
                Triple(7, 5, Season2WordPartsPresentationMode.HiddenWordPartsChallenge to Season2ChapterContent.ch7Words),
            )
        stations.forEach { (chapter, station, modeAndWords) ->
            val (mode, words) = modeAndWords
            val pool =
                Season2WordPartsCatalog.entriesForPresentationMode(
                    words,
                    mode,
                    stationChapterIndex = chapter,
                    stationId = station,
                )
            assertTrue(
                "Ch$chapter st$station needs ≥${Season2WordPartsCatalog.MIN_ROUNDS_PER_STATION} splits; got ${pool.size}",
                pool.size >= Season2WordPartsCatalog.MIN_ROUNDS_PER_STATION,
            )
        }
    }

    @Test
    fun ch7_hiddenPool_resolvesFromUnionCatalog() {
        val ch7 =
            Season2WordPartsCatalog.entriesForPresentationMode(
                Season2ChapterContent.ch7Words,
                Season2WordPartsPresentationMode.HiddenWordPartsChallenge,
                stationChapterIndex = 7,
                stationId = 5,
            ).map { it.catalogId }
        assertEquals(
            "ch7 hidden pool",
            setOf("w_ב_2", "w_ס_4", "w_ז_3", "w_ג_1", "w_פ_2", "w_ח_3"),
            ch7.toSet(),
        )
    }

    @Test
    fun ch5_and_ch7_useDedicatedGuidedAndHiddenPools() {
        val ch5Scope = Season2WordPartsCatalog.wordCatalogIdsForChapter5WordParts(Season2ChapterContent.ch5Words)
        val ch5 =
            Season2WordPartsCatalog.entriesForPresentationMode(
                ch5Scope,
                Season2WordPartsPresentationMode.GuidedWordParts,
                stationChapterIndex = 5,
                stationId = 6,
            ).map { it.catalogId }
        assertEquals(setOf("w_ז_3", "w_ס_4", "w_נ_2", "w_צ_2", "w_ש_1", "w_ר_3"), ch5.toSet())

        val ch7 =
            Season2WordPartsCatalog.entriesForPresentationMode(
                Season2ChapterContent.ch7Words,
                Season2WordPartsPresentationMode.HiddenWordPartsChallenge,
                stationChapterIndex = 7,
                stationId = 5,
            ).map { it.catalogId }
        assertEquals(setOf("w_ב_2", "w_ס_4", "w_ז_3", "w_ג_1", "w_פ_2", "w_ח_3"), ch7.toSet())
    }
}
