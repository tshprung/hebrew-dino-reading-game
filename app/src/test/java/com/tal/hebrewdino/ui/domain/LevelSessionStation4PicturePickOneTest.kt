package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertTrue
import org.junit.Test

class LevelSessionStation4PicturePickOneTest {
    @Test
    fun firstQuestionLoadsManyTimesWithoutThrowing() {
        val plan = Chapter1StationOrder.quizPlan(Chapter1StationOrder.PICTURE_PICK_ONE)
        assertTrue(plan.mode == StationQuizMode.PictureStartsWith)
        repeat(20_000) {
            val session =
                LevelSession(
                    plan = plan,
                    letterPoolSpec = LetterPoolSpec.Default,
                )
            val q = session.currentQuestion
            check(q is Question.PictureStartsWithQuestion) { "expected PictureStartsWith, got ${q?.javaClass}" }
        }
    }
}
