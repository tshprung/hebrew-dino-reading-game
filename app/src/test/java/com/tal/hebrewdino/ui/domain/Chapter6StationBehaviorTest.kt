package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.ui.NavRoutes
import com.tal.hebrewdino.ui.audio.AudioClips
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Chapter6StationBehaviorTest {
    @Test
    fun chapter6_station1_findGrid_has_expected_targetCount() {
        val plan = StationQuizPlans.chapter6(1)
        val session = LevelSession(plan = plan, letterPoolSpec = Chapter6LetterPoolSpec)
        val q = session.currentQuestion
        assertNotNull(q)
        assertTrue(q is Question.FindLetterGridQuestion)
        val gq = q as Question.FindLetterGridQuestion
        val expected = if (gq.columns == 3) 3 else 4
        val actual = gq.cells.count { it == gq.targetLetter }
        assertEquals(expected, actual)
    }

    @Test
    fun chapter6_station2_balloonCount_is10() {
        val plan = StationQuizPlans.chapter6(2)
        val session = LevelSession(plan = plan, letterPoolSpec = Chapter6LetterPoolSpec)
        val q = session.currentQuestion
        assertNotNull(q)
        assertTrue(q is Question.PopBalloonsQuestion)
        val pq = q as Question.PopBalloonsQuestion
        assertEquals(10, pq.options.size)
    }

    @Test
    fun chapter6_station3_pickLetter_optionsCount_is5() {
        val plan = StationQuizPlans.chapter6(3)
        val session = LevelSession(plan = plan, letterPoolSpec = Chapter6LetterPoolSpec)
        val q = session.currentQuestion
        assertNotNull(q)
        assertTrue(q is Question.PopBalloonsQuestion)
        val pq = q as Question.PopBalloonsQuestion
        assertEquals(5, pq.options.size)
        assertTrue(pq.options.contains(pq.correctAnswer))
    }

    @Test
    fun chapter6_storyAudioClipPaths_areDefined() {
        assertEquals("audio/story_ch6_intro.wav", AudioClips.StoryCh6Intro)
        assertEquals("audio/story_ch6_mid_boost.wav", AudioClips.StoryCh6MidBoost)
        assertEquals("audio/story_ch6_outro.wav", AudioClips.StoryCh6Outro)
    }

    @Test
    fun chapter6_midBoostRoute_isStable() {
        assertEquals("ch6_mid_boost", NavRoutes.Ch6MidBoost)
    }
}

