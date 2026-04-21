package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Contract tests for Episode 1 (Chapter 1) journey stations. Bump counts/flags only when intentionally changing the product.
 */
class Chapter1StationOrderTest {
    @Test
    fun stationCount_isSix() {
        assertEquals(6, Chapter1Config.STATION_COUNT)
    }

    @Test
    fun quizPlan_matchesEpisode1V1Contract() {
        val s1 = Chapter1StationOrder.quizPlan(1)
        assertEquals(StationQuizMode.PickLetter, s1.mode)
        assertEquals(6, s1.questionCount)
        assertEquals(0, s1.initialGroupIndex)

        val s2 = Chapter1StationOrder.quizPlan(2)
        assertEquals(StationQuizMode.PopBalloons, s2.mode)
        assertEquals(6, s2.questionCount)

        val s3 = Chapter1StationOrder.quizPlan(3)
        assertEquals(StationQuizMode.FindLetterGrid, s3.mode)
        assertEquals(7, s3.questionCount)

        val s4 = Chapter1StationOrder.quizPlan(4)
        assertEquals(StationQuizMode.PictureStartsWith, s4.mode)
        assertEquals(8, s4.questionCount)
        assertEquals(1, s4.initialGroupIndex)

        val s5 = Chapter1StationOrder.quizPlan(5)
        assertEquals(StationQuizMode.ImageMatch, s5.mode)
        assertEquals(8, s5.questionCount)
        assertEquals(2, s5.initialGroupIndex)
        assertTrue(s5.imageMatchAlwaysThreeChoices)
        assertEquals(1.5f, s5.imageMatchCaptionSizeMultiplier, 0f)
        assertEquals(1f, s5.imageMatchPictureSizeMultiplier, 0f)

        val s6 = Chapter1StationOrder.quizPlan(6)
        assertEquals(StationQuizMode.ImageMatch, s6.mode)
        assertEquals(6, s6.questionCount)
        assertEquals(3, s6.initialGroupIndex)
    }

    @Test
    fun stationQuizPlans_chapter1_delegatesToChapter1StationOrder() {
        for (id in 1..Chapter1Config.STATION_COUNT) {
            assertEquals(Chapter1StationOrder.quizPlan(id), StationQuizPlans.chapter1(id))
        }
    }

    @Test
    fun outOfRangeStationId_coercesToValidRange() {
        val low = Chapter1StationOrder.quizPlan(0)
        val high = Chapter1StationOrder.quizPlan(99)
        assertEquals(Chapter1StationOrder.quizPlan(1), low)
        assertEquals(Chapter1StationOrder.quizPlan(Chapter1Config.STATION_COUNT), high)
    }
}
