package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.ui.NavRoutes
import com.tal.hebrewdino.ui.audio.AudioClips
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Chapter6StationBehaviorTest {
    @Test
    fun chapter6_station1_pickLetter_optionsCount_is6() {
        val plan = StationQuizPlans.chapter6(1)
        val session = LevelSession(plan = plan, letterPoolSpec = Chapter6LetterPoolSpec)
        val q = session.currentQuestion
        assertNotNull(q)
        assertTrue(q is Question.PopBalloonsQuestion)
        val pq = q as Question.PopBalloonsQuestion
        assertEquals(6, pq.options.size)
        assertTrue(pq.options.contains(pq.correctAnswer))
    }

    @Test
    fun chapter6_station3_popAllLetters_balloonCount_is10() {
        val plan = StationQuizPlans.chapter6(3)
        val session = LevelSession(plan = plan, letterPoolSpec = Chapter6LetterPoolSpec)
        val q = session.currentQuestion
        assertNotNull(q)
        assertTrue(q is Question.PopBalloonsQuestion)
        val pq = q as Question.PopBalloonsQuestion
        assertEquals(10, pq.options.size)
    }

    @Test
    fun chapter6_station4_pictureStartsWith_optionCount_isCappedAt6() {
        val plan = StationQuizPlans.chapter6(4)
        val session = LevelSession(plan = plan, letterPoolSpec = Chapter6LetterPoolSpec)
        val q = session.currentQuestion
        assertNotNull(q)
        assertTrue(q is Question.PictureStartsWithQuestion)
        val pq = q as Question.PictureStartsWithQuestion
        assertTrue(pq.optionLetters.size <= 6)
        assertTrue(pq.correctLetter in pq.optionLetters)
    }

    @Test
    fun chapter6_audioClipConstants_exist_and_match_expected_paths() {
        assertEquals("audio/story_ch6_intro.wav", AudioClips.StoryCh6Intro)
        assertEquals("audio/story_ch6_mid_boost.wav", AudioClips.StoryCh6MidBoost)
        assertEquals("audio/story_ch6_outro.wav", AudioClips.StoryCh6Outro)
        assertEquals("audio/this_is.wav", AudioClips.ThisIsPrefix)
        assertEquals("audio/which_word_starts_with_letter.wav", AudioClips.WhichWordStartsWithLetter)
    }

    @Test
    fun chapter6_has_midBoost_route_constant() {
        assertEquals("ch6_mid_boost", NavRoutes.Ch6MidBoost)
    }
}

