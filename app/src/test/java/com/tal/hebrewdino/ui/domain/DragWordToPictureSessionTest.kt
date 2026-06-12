package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DragWordToPictureSessionTest {
    private val wordIds = listOf("w_ד_1", "w_ב_1", "w_ל_2")

    private fun session(pairCount: Int): LevelSession {
        val plan =
            StationQuizPlan(
                mode = StationQuizMode.DragWordToPicture,
                questionCount = 1,
                initialGroupIndex = 0,
                season2WordCatalogIds = wordIds,
                dragWordToPicturePairCount = pairCount,
            )
        return LevelSession(plan = plan, letterPoolSpec = Chapter1LetterPoolSpec)
    }

    @Test
    fun generatesTwoPairQuestion() {
        val q = session(pairCount = 2).currentQuestion as Question.DragWordToPictureQuestion
        assertEquals(2, q.pairs.size)
        assertEquals(2, q.wordBank.size)
    }

    @Test
    fun generatesThreePairQuestion() {
        val q = session(pairCount = 3).currentQuestion as Question.DragWordToPictureQuestion
        assertEquals(3, q.pairs.size)
        assertEquals(3, q.wordBank.size)
    }

    @Test
    fun validatePlacement_acceptsMatchingIds() {
        val session = session(pairCount = 2)
        val q = session.currentQuestion as Question.DragWordToPictureQuestion
        val pair = q.pairs.first()
        assertTrue(
            session.validateDragWordToPicturePlacement(
                wordCatalogId = pair.catalogEntryId,
                pictureCatalogId = pair.catalogEntryId,
            ),
        )
    }

    @Test
    fun validatePlacement_rejectsMismatchedIds() {
        val session = session(pairCount = 2)
        val q = session.currentQuestion as Question.DragWordToPictureQuestion
        require(q.pairs.size >= 2)
        val first = q.pairs[0]
        val second = q.pairs[1]
        assertFalse(
            session.validateDragWordToPicturePlacement(
                wordCatalogId = first.catalogEntryId,
                pictureCatalogId = second.catalogEntryId,
            ),
        )
    }

    @Test
    fun completeRound_onlyScoresOncePerQuestion() {
        val session = session(pairCount = 2)
        assertEquals(0, session.correctCount)
        assertEquals(AnswerResult.Correct, session.completeDragWordToPictureRound())
        assertEquals(1, session.correctCount)
        assertEquals(AnswerResult.Correct, session.completeDragWordToPictureRound())
        assertEquals(1, session.correctCount)
    }

    @Test
    fun submitFullPlacements_matchesCompleteRound() {
        val session = session(pairCount = 2)
        val q = session.currentQuestion as Question.DragWordToPictureQuestion
        val placements = q.pairs.associate { it.catalogEntryId to it.catalogEntryId }
        assertEquals(AnswerResult.Correct, session.submitDragWordToPicture(placements))
        assertEquals(1, session.correctCount)
    }

    @Test
    fun season2_chapter1_station4_usesDragWordToPicture() {
        assertEquals(
            StationQuizMode.DragWordToPicture,
            Season2Chapter1StationOrder.quizPlan(1, 4).mode,
        )
        assertFalse(
            StationQuizPlans.chapter1(1).mode == StationQuizMode.DragWordToPicture,
        )
    }
}
