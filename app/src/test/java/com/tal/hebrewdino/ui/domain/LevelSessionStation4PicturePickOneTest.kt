package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertTrue
import org.junit.Test

class LevelSessionStation4PicturePickOneTest {
    @Test
    fun firstQuestionLoadsManyTimesWithoutThrowing() {
        val plan = Chapter1StationOrder.quizPlan(Chapter1StationOrder.PICTURE_PICK_ONE)
        assertTrue(plan.mode == StationQuizMode.PicturePickOne)
        repeat(20_000) {
            val session =
                LevelSession(
                    questionCount = plan.questionCount,
                    initialGroupIndex = plan.initialGroupIndex,
                    quizMode = plan.mode,
                    letterPoolSpec = LetterPoolSpec.Default,
                )
            val q = session.currentQuestion
            check(q is Question.PicturePickOneQuestion) { "expected PicturePickOne, got ${q?.javaClass}" }
        }
    }
}
