package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.ui.data.ProgressPrefs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season1DragProgressionTest {
    @Test
    fun season1_popBalloons_exactlyThree_spacedApart() {
        val popStations =
            (1..6).flatMap { chapter ->
                (1..6).mapNotNull { station ->
                    val mode = season1PlanMode(chapter, station)
                    if (mode == StationQuizMode.PopBalloons) chapter to station else null
                }
            }
        assertEquals(listOf(1 to 2, 2 to 5, 4 to 2), popStations)
        assertEquals(3, popStations.size)
    }

    @Test
    fun season1_chapter5_hasDragMissingAtStation2() {
        val plan = StationQuizPlans.chapter5(2)
        assertEquals(StationQuizMode.DragMissingLetter, plan.mode)
        assertEquals(0, plan.dragMissingLetterIndex)
        assertEquals(6, plan.questionCount)
    }

    @Test
    fun season1_chapter6_hasBothDragStations() {
        assertEquals(StationQuizMode.DragWordToPicture, StationQuizPlans.chapter6(3).mode)
        assertEquals(StationQuizMode.DragMissingLetter, StationQuizPlans.chapter6(4).mode)
        assertEquals(5, StationQuizPlans.chapter6(3).questionCount)
        assertEquals(3, StationQuizPlans.chapter6(3).dragWordToPicturePairCount)
        assertEquals(0, StationQuizPlans.chapter6(4).dragMissingLetterIndex)
        assertEquals(6, StationQuizPlans.chapter6(4).questionCount)
    }

    @Test
    fun season1_allChapters_haveSixStations() {
        for (chapter in 1..6) {
            for (station in 1..6) {
                assertTrue(
                    "Ch$chapter st$station",
                    season1PlanMode(chapter, station) != null,
                )
            }
        }
    }

    @Test
    fun season1_dragStations_useConservativeSettings() {
        val ch6DragWord = StationQuizPlans.chapter6(3)
        assertEquals(StationQuizMode.DragWordToPicture, ch6DragWord.mode)
        assertEquals(5, ch6DragWord.questionCount)
        assertEquals(3, ch6DragWord.dragWordToPicturePairCount)
        val ch3DragWord = StationQuizPlans.chapter3(3)
        assertEquals(StationQuizMode.DragWordToPicture, ch3DragWord.mode)
        assertEquals(3, ch3DragWord.dragWordToPicturePairCount)
        assertEquals(5, ch3DragWord.questionCount)
        val dragMissingPlans =
            listOf(
                StationQuizPlans.chapter5(2),
                StationQuizPlans.chapter6(4),
            )
        for (plan in dragMissingPlans) {
            assertEquals(StationQuizMode.DragMissingLetter, plan.mode)
            assertEquals(0, plan.dragMissingLetterIndex)
            assertEquals(6, plan.questionCount)
        }
    }

    @Test
    fun season1_chapter1And2_stationModes() {
        val ch1 =
            listOf(
                StationQuizMode.PickLetter,
                StationQuizMode.PopBalloons,
                StationQuizMode.FindLetterGrid,
                StationQuizMode.PictureStartsWith,
                StationQuizMode.ImageMatch,
                StationQuizMode.ImageMatch,
            )
        val ch2 =
            listOf(
                StationQuizMode.PickLetter,
                StationQuizMode.FindLetterGrid,
                StationQuizMode.PictureStartsWith,
                StationQuizMode.ImageMatch,
                StationQuizMode.PopBalloons,
                StationQuizMode.ImageMatch,
            )
        ch1.forEachIndexed { index, mode -> assertEquals(mode, StationQuizPlans.chapter1(index + 1).mode) }
        ch2.forEachIndexed { index, mode -> assertEquals(mode, StationQuizPlans.chapter2(index + 1).mode) }
    }

    @Test
    fun chapter3_station3_dragWord_fiveRoundsThreePairs_withLayoutFlags() {
        val plan = StationQuizPlans.chapter3(3)
        assertEquals(5, plan.questionCount)
        assertEquals(3, plan.dragWordToPicturePairCount)
        val spec = StationBehaviorRegistry.getStationUiSpec(3, 3)
        assertTrue(spec.dragWordInstructionReadablePanel)
        assertEquals(3f, spec.dragWordPictureGapMultiplier, 0f)
        assertTrue(spec.dragWordEmphasizeDropZone)

        val eligible =
            LessonWordCatalog.entries.count { entry ->
                entry.letter in Chapter3Config.letters.toSet() &&
                    Season2StationContentValidator.wordAssetCheck(entry.id)?.isValid == true
            }
        assertTrue("Need ≥15 image+audio words for Ch3 st3 (5 rounds × 3 pairs)", eligible >= 15)

        val session = LevelSession(plan = plan, letterPoolSpec = Chapter3LetterPoolSpec)
        repeat(5) {
            val q = session.currentQuestion as Question.DragWordToPictureQuestion
            assertEquals(3, q.pairs.size)
            assertEquals(3, q.wordBank.size)
            val placements = q.pairs.associate { it.catalogEntryId to it.catalogEntryId }
            assertEquals(AnswerResult.Correct, session.submitDragWordToPicture(placements))
            session.nextQuestion()
        }
    }

    @Test
    fun season1_stationPlanVersion_isSet() {
        assertEquals(1, ProgressPrefs.STATION_PLAN_VERSION)
    }

    @Test
    fun season2_stationOrder_unchanged_afterSeason1DragBatch() {
        assertEquals(7, Season2ChapterRegistry.CHAPTER_COUNT)
        assertEquals(
            listOf(2 to 4, 7 to 3),
            (1..Season2ChapterRegistry.CHAPTER_COUNT).flatMap { chapter ->
                (1..6).mapNotNull { station ->
                    val kind =
                        when (chapter) {
                            in 1..2 ->
                                Season2StationUx.stationKindForGameplayChapter(
                                    gameplayChapterId = 100 + chapter,
                                    stationId = station,
                                )
                            in 3..7 -> Season2ChapterStationPlans.stationKind(chapter, station)
                            else -> null
                        }
                    if (kind == Season2ChapterStationPlans.StationKind.MemoryMatch) {
                        chapter to station
                    } else {
                        null
                    }
                }
            },
        )
    }

    @Test
    fun season1_chapters1And2_doNotUseDragModes() {
        for (chapter in 1..2) {
            for (station in 1..6) {
                val mode = season1PlanMode(chapter, station)!!
                assertFalse(mode == StationQuizMode.DragWordToPicture)
                assertFalse(mode == StationQuizMode.DragMissingLetter)
            }
        }
    }

    private fun season1PlanMode(chapter: Int, station: Int): StationQuizMode? =
        when (chapter) {
            1 -> StationQuizPlans.chapter1(station).mode
            2 -> StationQuizPlans.chapter2(station).mode
            3 -> StationQuizPlans.chapter3(station).mode
            4 -> StationQuizPlans.chapter4(station).mode
            5 -> StationQuizPlans.chapter5(station).mode
            6 -> StationQuizPlans.chapter6(station).mode
            else -> null
        }
}
