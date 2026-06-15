package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.ui.data.Season2ProgressPrefs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2DragWiringTest {
    @Test
    fun season1_chapter1_stationModes_unchanged() {
        assertEquals(StationQuizMode.PickLetter, Chapter1StationOrder.quizPlan(1).mode)
        assertEquals(StationQuizMode.PopBalloons, Chapter1StationOrder.quizPlan(2).mode)
        assertEquals(StationQuizMode.FindLetterGrid, Chapter1StationOrder.quizPlan(3).mode)
        assertEquals(StationQuizMode.PictureStartsWith, Chapter1StationOrder.quizPlan(4).mode)
        assertEquals(StationQuizMode.ImageMatch, Chapter1StationOrder.quizPlan(5).mode)
        assertEquals(StationQuizMode.ImageMatch, Chapter1StationOrder.quizPlan(6).mode)
    }

    @Test
    fun popBalloons_onlyInChapter1Station1AndChapter4Station1() {
        val popStations =
            (1..Season2ChapterRegistry.CHAPTER_COUNT).flatMap { chapter ->
                (1..6).mapNotNull { station ->
                    val mode =
                        planMode(chapter, station) ?: return@mapNotNull null
                    if (mode == StationQuizMode.PopBalloons) chapter to station else null
                }
            }
        assertEquals(listOf(1 to 1, 4 to 1), popStations)
    }

    @Test
    fun memoryMatch_inChapter2Station4AndChapter7Station3() {
        val memoryStations =
            (1..Season2ChapterRegistry.CHAPTER_COUNT).flatMap { chapter ->
                (1..6).mapNotNull { station ->
                    val kind = stationKind(chapter, station)
                    if (kind == Season2ChapterStationPlans.StationKind.MemoryMatch) {
                        chapter to station
                    } else {
                        null
                    }
                }
            }
        assertEquals(listOf(2 to 4, 7 to 3), memoryStations)
    }

    @Test
    fun memoryMatch_chapter2And7_validateWithoutQuizPlan() {
        for ((chapter, station) in listOf(2 to 4, 7 to 3)) {
            val issues =
                if (chapter <= 2) {
                    val ch2 = Season2ChapterRegistry.chapter(2)!!
                    Season2StationContentValidator.validateLetters(ch2.memoryMatchLetters)
                } else {
                    Season2ChapterStationPlans.validateStation(
                        Season2ChapterStationPlans.contextFor(chapter)!!,
                        station,
                    )
                }
            assertTrue("Ch$chapter st$station: $issues", issues.isEmpty())
        }
        val ch2 = Season2ChapterRegistry.chapter(2)!!
        assertEquals(listOf("ח", "ר", "ק", "ש", "מ"), ch2.memoryMatchLetters)
    }

    @Test
    fun dragWordToPicture_inAllSeason2Chapters() {
        for (chapter in 1..Season2ChapterRegistry.CHAPTER_COUNT) {
            val stations =
                (1..6).filter { station ->
                    planMode(chapter, station) == StationQuizMode.DragWordToPicture
                }
            assertTrue("Ch$chapter missing DragWordToPicture", stations.isNotEmpty())
        }
    }

    @Test
    fun dragMissingLetter_inAllSeason2Chapters() {
        for (chapter in 1..Season2ChapterRegistry.CHAPTER_COUNT) {
            val stations =
                (1..6).filter { station ->
                    planMode(chapter, station) == StationQuizMode.DragMissingLetter
                }
            assertTrue("Ch$chapter missing DragMissingLetter", stations.isNotEmpty())
        }
    }

    @Test
    fun allChapters_haveSixStations() {
        for (chapter in 1..Season2ChapterRegistry.CHAPTER_COUNT) {
            for (station in 1..6) {
                assertTrue(
                    "Ch$chapter st$station should resolve",
                    planMode(chapter, station) != null ||
                        stationKind(chapter, station) == Season2ChapterStationPlans.StationKind.MemoryMatch,
                )
            }
        }
    }

    @Test
    fun allSeason2StationPlans_validateForChapters3Through7() {
        for (chapter in 3..7) {
            val ctx = Season2ChapterStationPlans.contextFor(chapter)!!
            val issues = Season2ChapterStationPlans.validateAllStations(ctx)
            assertTrue("Ch$chapter validation issues: $issues", issues.isEmpty())
        }
    }

    @Test
    fun chapter1_dragPlans_matchSeason1SourceShape() {
        val dragWord = Season2Chapter1StationOrder.quizPlan(1, 4)
        assertEquals(StationQuizMode.DragWordToPicture, dragWord.mode)
        assertEquals(3, dragWord.dragWordToPicturePairCount)
        assertEquals(5, dragWord.questionCount)
        val dragMissing = Season2Chapter1StationOrder.quizPlan(1, 6)
        assertEquals(StationQuizMode.DragMissingLetter, dragMissing.mode)
        assertEquals(0, dragMissing.dragMissingLetterIndex)
        assertEquals(6, dragMissing.questionCount)
    }

    @Test
    fun season2_chapterCount_isSeven() {
        assertEquals(7, Season2ChapterRegistry.CHAPTER_COUNT)
    }

    @Test
    fun stationPlanVersion_bumpedForDragWiring() {
        assertEquals(2, Season2ProgressPrefs.STATION_PLAN_VERSION)
    }

    @Test
    fun season1_chapter1_doesNotUseDragModes() {
        for (stationId in 1..Chapter1Config.STATION_COUNT) {
            val mode = StationQuizPlans.chapter1(stationId).mode
            assertFalse(mode == StationQuizMode.DragWordToPicture)
            assertFalse(mode == StationQuizMode.DragMissingLetter)
        }
    }

    private fun planMode(chapter: Int, station: Int): StationQuizMode? =
        when {
            stationKind(chapter, station) == Season2ChapterStationPlans.StationKind.MemoryMatch -> null
            chapter <= 2 -> Season2Chapter1StationOrder.quizPlan(chapter, station).mode
            else ->
                Season2ChapterStationPlans
                    .quizPlan(Season2ChapterStationPlans.contextFor(chapter)!!, station)
                    .mode
        }

    private fun stationKind(chapter: Int, station: Int): Season2ChapterStationPlans.StationKind? =
        when (chapter) {
            in 1..2 ->
                Season2StationUx.stationKindForGameplayChapter(
                    gameplayChapterId = 100 + chapter,
                    stationId = station,
                )
            in 3..7 -> Season2ChapterStationPlans.stationKind(chapter, station)
            else -> null
        }
}
