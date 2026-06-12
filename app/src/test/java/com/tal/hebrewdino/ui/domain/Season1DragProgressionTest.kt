package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.ui.data.ProgressPrefs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season1DragProgressionTest {
    @Test
    fun season1_popBalloons_exactlyThree_spacedApart() {
        val popStations =
            (1..6).flatMap { chapter ->
                (1..6).mapNotNull { station ->
                    val mode = season1PlanMode(chapter, station)
                    if (mode == StationQuizMode.PopBalloons) chapter to station else null
                }
            }
        assertEquals(listOf(1 to 2, 2 to 5, 4 to 2), popStations)
        assertEquals(3, popStations.size)
    }

    @Test
    fun season1_chapter3_hasDragWordAtStation3() {
        val plan = StationQuizPlans.chapter3(3)
        assertEquals(StationQuizMode.DragWordToPicture, plan.mode)
        assertEquals(2, plan.dragWordToPicturePairCount)
    }

    @Test
    fun season1_chapter5_hasDragMissingAtStation2() {
        val plan = StationQuizPlans.chapter5(2)
        assertEquals(StationQuizMode.DragMissingLetter, plan.mode)
        assertEquals(0, plan.dragMissingLetterIndex)
    }

    @Test
    fun season1_chapter6_hasBothDragStations() {
        assertEquals(StationQuizMode.DragWordToPicture, StationQuizPlans.chapter6(3).mode)
        assertEquals(StationQuizMode.DragMissingLetter, StationQuizPlans.chapter6(4).mode)
        assertEquals(2, StationQuizPlans.chapter6(3).dragWordToPicturePairCount)
        assertEquals(0, StationQuizPlans.chapter6(4).dragMissingLetterIndex)
    }

    @Test
    fun season1_allChapters_haveSixStations() {
        for (chapter in 1..6) {
            for (station in 1..6) {
                assertTrue(
                    "Ch$chapter st$station",
                    season1PlanMode(chapter, station) != null,
                )
            }
        }
    }

    @Test
    fun season1_dragStations_useConservativeSettings() {
        val dragWordPlans =
            listOf(
                StationQuizPlans.chapter3(3),
                StationQuizPlans.chapter6(3),
            )
        for (plan in dragWordPlans) {
            assertEquals(StationQuizMode.DragWordToPicture, plan.mode)
            assertEquals(2, plan.dragWordToPicturePairCount)
        }
        val dragMissingPlans =
            listOf(
                StationQuizPlans.chapter5(2),
                StationQuizPlans.chapter6(4),
            )
        for (plan in dragMissingPlans) {
            assertEquals(StationQuizMode.DragMissingLetter, plan.mode)
            assertEquals(0, plan.dragMissingLetterIndex)
        }
    }

    @Test
    fun season1_chapter1_unchangedOrder() {
        assertEquals(StationQuizMode.PickLetter, StationQuizPlans.chapter1(1).mode)
        assertEquals(StationQuizMode.PopBalloons, StationQuizPlans.chapter1(2).mode)
        assertEquals(StationQuizMode.FindLetterGrid, StationQuizPlans.chapter1(3).mode)
        assertEquals(StationQuizMode.PictureStartsWith, StationQuizPlans.chapter1(4).mode)
        assertEquals(StationQuizMode.ImageMatch, StationQuizPlans.chapter1(5).mode)
        assertEquals(StationQuizMode.ImageMatch, StationQuizPlans.chapter1(6).mode)
    }

    @Test
    fun season1_chapter2_reordersPopBalloonsToStation5() {
        assertEquals(StationQuizMode.PickLetter, StationQuizPlans.chapter2(1).mode)
        assertEquals(StationQuizMode.FindLetterGrid, StationQuizPlans.chapter2(2).mode)
        assertEquals(StationQuizMode.PictureStartsWith, StationQuizPlans.chapter2(3).mode)
        assertEquals(StationQuizMode.ImageMatch, StationQuizPlans.chapter2(4).mode)
        assertEquals(StationQuizMode.PopBalloons, StationQuizPlans.chapter2(5).mode)
        assertEquals(StationQuizMode.ImageMatch, StationQuizPlans.chapter2(6).mode)
    }

    @Test
    fun season1_stationPlanVersion_isSet() {
        assertEquals(1, ProgressPrefs.STATION_PLAN_VERSION)
    }

    @Test
    fun season2_stationOrder_unchanged_afterSeason1DragBatch() {
        assertEquals(7, Season2ChapterRegistry.CHAPTER_COUNT)
        assertEquals(
            listOf(2 to 4, 7 to 3),
            (1..Season2ChapterRegistry.CHAPTER_COUNT).flatMap { chapter ->
                (1..6).mapNotNull { station ->
                    val kind =
                        when (chapter) {
                            in 1..2 ->
                                Season2StationUx.stationKindForGameplayChapter(
                                    gameplayChapterId = 100 + chapter,
                                    stationId = station,
                                )
                            in 3..7 -> Season2ChapterStationPlans.stationKind(chapter, station)
                            else -> null
                        }
                    if (kind == Season2ChapterStationPlans.StationKind.MemoryMatch) {
                        chapter to station
                    } else {
                        null
                    }
                }
            },
        )
    }

    @Test
    fun season1_chapters1And2_doNotUseDragModes() {
        for (chapter in 1..2) {
            for (station in 1..6) {
                val mode = season1PlanMode(chapter, station)!!
                assertFalse(mode == StationQuizMode.DragWordToPicture)
                assertFalse(mode == StationQuizMode.DragMissingLetter)
            }
        }
    }

    private fun season1PlanMode(chapter: Int, station: Int): StationQuizMode? =
        when (chapter) {
            1 -> StationQuizPlans.chapter1(station).mode
            2 -> StationQuizPlans.chapter2(station).mode
            3 -> StationQuizPlans.chapter3(station).mode
            4 -> StationQuizPlans.chapter4(station).mode
            5 -> StationQuizPlans.chapter5(station).mode
            6 -> StationQuizPlans.chapter6(station).mode
            else -> null
        }
}
