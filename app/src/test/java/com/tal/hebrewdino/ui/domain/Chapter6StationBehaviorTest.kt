package com.tal.hebrewdino.ui.domain

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
}

