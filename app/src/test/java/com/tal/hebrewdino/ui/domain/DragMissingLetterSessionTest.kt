package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DragMissingLetterSessionTest {
    private val wordIds = listOf("w_ד_1", "w_ב_1", "w_ל_2", "w_ש_1")

    private fun session(missingIndex: Int = 0): LevelSession {
        val plan =
            StationQuizPlan(
                mode = StationQuizMode.DragMissingLetter,
                questionCount = 1,
                initialGroupIndex = 0,
                season2WordCatalogIds = wordIds,
                dragMissingLetterIndex = missingIndex,
                season2AdvancedDistractorLetters = listOf("ד", "ב", "מ", "ל", "ש", "ס"),
                optionCount = 3,
            )
        return LevelSession(plan = plan, letterPoolSpec = Chapter1LetterPoolSpec)
    }

    @Test
    fun generatesMissingFirstLetterQuestion() {
        val q = session(missingIndex = 0).currentQuestion as Question.DragMissingLetterQuestion
        assertEquals(0, q.missingIndex)
        assertTrue(q.partialWord.contains("_"))
        assertTrue(q.correctLetter in q.optionLetters)
    }

    @Test
    fun validate_acceptsCorrectLetter() {
        val session = session()
        val q = session.currentQuestion as Question.DragMissingLetterQuestion
        assertTrue(session.validateDragMissingLetter(q.correctLetter))
    }

    @Test
    fun validate_rejectsWrongLetter() {
        val session = session()
        val q = session.currentQuestion as Question.DragMissingLetterQuestion
        val wrong = q.optionLetters.first { it != q.correctLetter }
        assertFalse(session.validateDragMissingLetter(wrong))
    }

    @Test
    fun submitCorrect_fillsSlotSemanticsAndScoresOnce() {
        val session = session()
        val q = session.currentQuestion as Question.DragMissingLetterQuestion
        assertEquals(0, session.correctCount)
        assertEquals(AnswerResult.Correct, session.submitDragMissingLetter(q.correctLetter))
        assertEquals(1, session.correctCount)
        assertEquals(AnswerResult.Correct, session.submitDragMissingLetter(q.correctLetter))
        assertEquals(1, session.correctCount)
    }

    @Test
    fun submitWrong_doesNotScore() {
        val session = session()
        val q = session.currentQuestion as Question.DragMissingLetterQuestion
        val wrong = q.optionLetters.first { it != q.correctLetter }
        assertEquals(AnswerResult.Wrong, session.submitDragMissingLetter(wrong))
        assertEquals(0, session.correctCount)
    }

    @Test
    fun generatesLastLetterMissingWhenConfigured() {
        val plan =
            StationQuizPlan(
                mode = StationQuizMode.DragMissingLetter,
                questionCount = 1,
                initialGroupIndex = 0,
                season2WordCatalogIds = listOf("w_ד_1"),
                dragMissingLetterIndex = 1,
                season2AdvancedDistractorLetters = listOf("ד", "ב", "מ", "ג"),
                optionCount = 3,
            )
        val q =
            LevelSession(plan = plan, letterPoolSpec = Chapter1LetterPoolSpec)
                .currentQuestion as Question.DragMissingLetterQuestion
        assertEquals("דג", q.word)
        assertEquals(1, q.missingIndex)
        assertEquals("ד_", q.partialWord)
        assertEquals("ג", q.correctLetter)
    }

    @Test
    fun generatesMiddleLetterMissingWhenConfigured() {
        val plan =
            StationQuizPlan(
                mode = StationQuizMode.DragMissingLetter,
                questionCount = 1,
                initialGroupIndex = 0,
                season2WordCatalogIds = listOf("w_ש_1"),
                dragMissingLetterIndex = 1,
                season2AdvancedDistractorLetters = listOf("ש", "ס", "מ", "ר"),
                optionCount = 3,
            )
        val q =
            LevelSession(plan = plan, letterPoolSpec = Chapter1LetterPoolSpec)
                .currentQuestion as Question.DragMissingLetterQuestion
        assertEquals("שמש", q.word)
        assertEquals(1, q.missingIndex)
        assertEquals("ש_ש", q.partialWord)
        assertEquals("מ", q.correctLetter)
    }

    @Test
    fun season2_chapter1_station6_usesDragMissingLetter() {
        assertEquals(
            StationQuizMode.DragMissingLetter,
            Season2Chapter1StationOrder.quizPlan(1, 6).mode,
        )
        assertFalse(
            StationQuizPlans.chapter1(1).mode == StationQuizMode.DragMissingLetter,
        )
    }
}
