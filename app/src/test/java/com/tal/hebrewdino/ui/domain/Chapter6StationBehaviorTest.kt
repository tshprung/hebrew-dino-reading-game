package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.ui.NavRoutes
import com.tal.hebrewdino.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Chapter6StationBehaviorTest {
    @Test
    fun chapter6_station1_pictureStartsWith_has_5_optionLetters() {
        val plan = StationQuizPlans.chapter6(1)
        val session = LevelSession(plan = plan, letterPoolSpec = Chapter6LetterPoolSpec)
        val q = session.currentQuestion
        assertNotNull(q)
        assertTrue(q is Question.PictureStartsWithQuestion)
        val pq = q as Question.PictureStartsWithQuestion
        assertEquals(5, pq.optionLetters.size)
    }

    @Test
    fun chapter6_station3_dragWordToPicture_generatesQuestion() {
        val plan = StationQuizPlans.chapter6(3)
        assertEquals(StationQuizMode.DragWordToPicture, plan.mode)
        assertEquals(2, plan.dragWordToPicturePairCount)
        val session = LevelSession(plan = plan, letterPoolSpec = Chapter6LetterPoolSpec)
        val q = session.currentQuestion
        assertNotNull(q)
        assertTrue(q is Question.DragWordToPictureQuestion)
    }

    @Test
    fun chapter6_station4_dragMissingLetter_generatesQuestion() {
        val plan = StationQuizPlans.chapter6(4)
        assertEquals(StationQuizMode.DragMissingLetter, plan.mode)
        assertEquals(0, plan.dragMissingLetterIndex)
        val session = LevelSession(plan = plan, letterPoolSpec = Chapter6LetterPoolSpec)
        val q = session.currentQuestion
        assertNotNull(q)
        assertTrue(q is Question.DragMissingLetterQuestion)
    }

    @Test
    fun chapter6_storyNarrationRawResIds_areDefined() {
        assertTrue(R.raw.ch6_story_intro_dino != 0)
        assertTrue(R.raw.ch6_story_intro_dina != 0)
        assertTrue(R.raw.ch6_story_mid_dino != 0)
        assertTrue(R.raw.ch6_story_mid_dina != 0)
        assertTrue(R.raw.ch6_story_outro_dino != 0)
        assertTrue(R.raw.ch6_story_outro_dina != 0)
    }

    @Test
    fun chapter6_midBoostRoute_isStable() {
        assertEquals("ch6_mid_boost", NavRoutes.Ch6MidBoost)
    }
}
