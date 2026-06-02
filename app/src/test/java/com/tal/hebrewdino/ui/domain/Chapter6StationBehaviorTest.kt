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
    fun chapter6_station3_balloons_has_10_options_and_is_single_target_letter() {
        val plan = StationQuizPlans.chapter6(3)
        val session = LevelSession(plan = plan, letterPoolSpec = Chapter6LetterPoolSpec)
        val q = session.currentQuestion
        assertNotNull(q)
        assertTrue(q is Question.PopBalloonsQuestion)
        val pq = q as Question.PopBalloonsQuestion
        assertEquals(10, pq.options.size)
        assertTrue(pq.correctAnswer.isNotBlank())
        assertTrue(pq.options.contains(pq.correctAnswer))
        assertTrue(session.chapter3PopAllLettersCurrentWord() == null)
    }

    @Test
    fun chapter6_station4_highlightedInWord_correctLetter_matches_question() {
        val plan = StationQuizPlans.chapter6(4)
        val session = LevelSession(plan = plan, letterPoolSpec = Chapter6LetterPoolSpec)
        val q = session.currentQuestion
        assertNotNull(q)
        assertTrue(q is Question.PopBalloonsQuestion)
        val pq = q as Question.PopBalloonsQuestion
        val round = session.highlightedLetterInWordRound(0)
        assertNotNull(round)
        assertEquals(round!!.correctLetter, pq.correctAnswer)
        assertTrue(pq.options.contains(round.correctLetter))
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

