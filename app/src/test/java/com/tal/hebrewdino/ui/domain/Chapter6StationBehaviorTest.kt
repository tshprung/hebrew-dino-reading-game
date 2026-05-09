package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.ui.NavRoutes
import com.tal.hebrewdino.ui.audio.AudioClips
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
    fun chapter6_station3_popAllLetters_options_include_all_letters_in_word() {
        val plan = StationQuizPlans.chapter6(3)
        val session = LevelSession(plan = plan, letterPoolSpec = Chapter6LetterPoolSpec)
        val q = session.currentQuestion
        assertNotNull(q)
        assertTrue(q is Question.PopBalloonsQuestion)
        val pq = q as Question.PopBalloonsQuestion
        assertEquals(10, pq.options.size)
        val word = session.chapter3PopAllLettersCurrentWord()?.first.orEmpty()
        assertTrue(word.isNotEmpty())
        val lettersInWord = word.toCharArray().map { it.toString() }.distinct()
        for (l in lettersInWord) {
            assertTrue("Expected $l to appear in options for word=$word", pq.options.contains(l))
        }
        assertEquals(word.first().toString(), pq.correctAnswer)
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

