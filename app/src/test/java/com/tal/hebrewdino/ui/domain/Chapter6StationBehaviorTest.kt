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
        assertEquals(5, plan.questionCount)
        assertEquals(3, plan.dragWordToPicturePairCount)
        val session = LevelSession(plan = plan, letterPoolSpec = Chapter6LetterPoolSpec)
        val q = session.currentQuestion
        assertNotNull(q)
        assertTrue(q is Question.DragWordToPictureQuestion)
        assertEquals(3, (q as Question.DragWordToPictureQuestion).pairs.size)
    }

    @Test
    fun chapter6_station4_dragMissingLetter_generatesQuestion() {
        val plan = StationQuizPlans.chapter6(4)
        assertEquals(StationQuizMode.DragMissingLetter, plan.mode)
        assertEquals(0, plan.dragMissingLetterIndex)
        assertEquals(6, plan.questionCount)
        assertNotNull(plan.season2WordCatalogIds)
        val session = LevelSession(plan = plan, letterPoolSpec = Chapter6LetterPoolSpec)
        val q = session.currentQuestion
        assertNotNull(q)
        assertTrue(q is Question.DragMissingLetterQuestion)
    }

    @Test
    fun chapter6_station4_dragMissingLetter_usesDistinctWordsFromChapter5Station2() {
        val ch6WordIds = Chapter6Config.dragMissingLetterWordCatalogIds()
        assertTrue("Ch6 st4 needs ≥6 drag-missing words", ch6WordIds.size >= 6)
        val ch5Letters = Chapter5Config.letters.toSet()
        val ch5WordIds =
            LessonWordCatalog.entries
                .asSequence()
                .filter { it.letter in ch5Letters }
                .map { it.id }
                .filter { catalogId ->
                    DragStationGenerators.isValidForDragMissingLetter(catalogId, missingIndex = 0) &&
                        Season2StationContentValidator.wordAssetCheck(catalogId)?.isValid == true
                }
                .toSet()
        assertTrue(ch6WordIds.none { it in ch5WordIds })
        assertEquals(
            Chapter4Config.letters.toSet(),
            ch6WordIds
                .mapNotNull { id -> LessonWordCatalog.entries.find { it.id == id }?.letter }
                .toSet(),
        )
        assertEquals(ch6WordIds, StationQuizPlans.chapter6(4).season2WordCatalogIds)
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
