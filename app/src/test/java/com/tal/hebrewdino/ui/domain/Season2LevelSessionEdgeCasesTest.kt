package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Wrong-answer, dedup, and repeat-run edge cases for Season 2 [LevelSession]. */
class Season2LevelSessionEdgeCasesTest {
    /** One station per distinct interaction/scoring path in Season 2. */
    private val representativeStations =
        listOf(
            1 to 1, // PopBalloons
            2 to 1, // PickLetter
            1 to 3, // PictureStartsWith
            1 to 5, // ImageMatch (which word)
            1 to 4, // DragWordToPicture
            1 to 6, // DragMissingLetter
            2 to 6, // WordParts (advanced)
            4 to 5, // PictureToWord (advanced)
            5 to 5, // Rhyming (advanced)
            3 to 3, // DragMissingLetter via chapter 3+ planner
        )

    @Test
    fun wrongAnswer_doesNotScore_beforeCorrect_onRepresentativeStations() {
        for ((chapter, station) in representativeStations) {
            val plan = Season2LevelSessionTestSupport.quizPlan(chapter, station)
            val label = Season2LevelSessionTestSupport.stationLabel(chapter, station, plan)
            val session = Season2LevelSessionTestSupport.sessionFor(chapter, station)
            val q =
                session.currentQuestion
                    ?: error("$label: expected first question")

            assertEquals("$label: initial correctCount", 0, session.correctCount)
            assertEquals(
                "$label: wrong submission",
                AnswerResult.Wrong,
                Season2LevelSessionTestSupport.submitWrongAnswer(session, q),
            )
            assertEquals("$label: correctCount after wrong", 0, session.correctCount)
            assertEquals(
                "$label: correct submission",
                AnswerResult.Correct,
                Season2LevelSessionTestSupport.submitCorrectAnswer(session, q),
            )
            assertEquals("$label: correctCount after one correct", 1, session.correctCount)
        }
    }

    @Test
    fun dragWordToPicture_pairCatalogIdsUniqueWithinEachRound() {
        val stations =
            Season2LevelSessionTestSupport.playableStations().filter { (chapter, station) ->
                Season2LevelSessionTestSupport.quizPlan(chapter, station).mode ==
                    StationQuizMode.DragWordToPicture
            }
        assertTrue("expected drag-word stations across Season 2", stations.size >= 7)

        for ((chapter, station) in stations) {
            val plan = Season2LevelSessionTestSupport.quizPlan(chapter, station)
            val label = Season2LevelSessionTestSupport.stationLabel(chapter, station, plan)
            val session = Season2LevelSessionTestSupport.sessionFor(chapter, station)

            repeat(plan.questionCount) { round ->
                val q =
                    session.currentQuestion as? Question.DragWordToPictureQuestion
                        ?: error("$label round ${round + 1}: expected DragWordToPictureQuestion")
                assertEquals(
                    "$label round ${round + 1}: pair ids must be unique within the round",
                    q.pairs.size,
                    q.pairs.map { it.catalogEntryId }.distinct().size,
                )
                Season2LevelSessionTestSupport.submitCorrectAnswer(session, q)
                session.nextQuestion()
            }
        }
    }

    @Test
    fun rhymingStations_useEachConfiguredPairExactlyOnce() {
        val rhymingStations =
            Season2LevelSessionTestSupport.playableStations().filter { (chapter, station) ->
                Season2LevelSessionTestSupport.quizPlan(chapter, station).season2AdvancedMode ==
                    Season2AdvancedStationMode.Rhyming
            }
        assertEquals(
            listOf(5 to 5, 6 to 4, 7 to 4),
            rhymingStations.sortedWith(compareBy({ it.first }, { it.second })),
        )

        for ((chapter, station) in rhymingStations) {
            val plan = Season2LevelSessionTestSupport.quizPlan(chapter, station)
            val label = Season2LevelSessionTestSupport.stationLabel(chapter, station, plan)
            val session = Season2LevelSessionTestSupport.sessionFor(chapter, station)
            val targetIds = mutableListOf<String>()

            repeat(plan.questionCount) { round ->
                val q =
                    session.currentQuestion as? Question.RhymingQuestion
                        ?: error("$label round ${round + 1}: expected RhymingQuestion")
                targetIds.add(q.targetCatalogEntryId)
                Season2LevelSessionTestSupport.submitCorrectAnswer(session, q)
                session.nextQuestion()
            }

            assertEquals(
                "$label: distinct rhyme targets",
                plan.questionCount,
                targetIds.distinct().size,
            )
        }
    }

    @Test
    fun wordPartsStations_useDistinctWordsPerRun_whenWithinCatalogCapacity() {
        val wordPartsStations =
            Season2LevelSessionTestSupport.playableStations().filter { (chapter, station) ->
                Season2LevelSessionTestSupport.quizPlan(chapter, station).season2AdvancedMode ==
                    Season2AdvancedStationMode.WordParts
            }
        assertTrue("expected word-parts stations", wordPartsStations.size >= 4)

        for ((chapter, station) in wordPartsStations) {
            val plan = Season2LevelSessionTestSupport.quizPlan(chapter, station)
            val label = Season2LevelSessionTestSupport.stationLabel(chapter, station, plan)
            val session = Season2LevelSessionTestSupport.sessionFor(chapter, station)
            val catalogIds = mutableListOf<String>()

            repeat(plan.questionCount) { round ->
                val q =
                    session.currentQuestion as? Question.WordPartsQuestion
                        ?: error("$label round ${round + 1}: expected WordPartsQuestion")
                catalogIds.add(q.catalogEntryId)
                Season2LevelSessionTestSupport.submitCorrectAnswer(session, q)
                session.nextQuestion()
            }

            assertEquals(
                "$label: distinct word-parts targets",
                plan.questionCount,
                catalogIds.distinct().size,
            )
        }
    }

    @Test
    fun allPlayableStations_completeFullRun_acrossRepeatedFreshSessions() {
        val repeatCount = 8
        for ((chapter, station) in Season2LevelSessionTestSupport.playableStations()) {
            val plan = Season2LevelSessionTestSupport.quizPlan(chapter, station)
            val label = Season2LevelSessionTestSupport.stationLabel(chapter, station, plan)
            repeat(repeatCount) { run ->
                try {
                    Season2LevelSessionTestSupport.runFullStation(chapter, station)
                } catch (e: AssertionError) {
                    throw AssertionError("$label fresh session run ${run + 1}/$repeatCount: ${e.message}")
                }
            }
        }
    }

    @Test
    fun dragMissingLetter_repeatedWrongThenCorrect_scoresOncePerRound() {
        val session = Season2LevelSessionTestSupport.sessionFor(chapter = 1, station = 6)
        val q = session.currentQuestion as Question.DragMissingLetterQuestion
        val wrong = q.optionLetters.first { it != q.correctLetter }

        assertEquals(AnswerResult.Wrong, session.submitDragMissingLetter(wrong))
        assertEquals(AnswerResult.Wrong, session.submitDragMissingLetter(wrong))
        assertEquals(0, session.correctCount)

        assertEquals(AnswerResult.Correct, session.submitDragMissingLetter(q.correctLetter))
        assertEquals(1, session.correctCount)
        assertEquals(AnswerResult.Correct, session.submitDragMissingLetter(q.correctLetter))
        assertEquals(1, session.correctCount)
    }

    @Test
    fun dragWordToPicture_wrongPlacementThenCorrect_scoresOncePerRound() {
        val session = Season2LevelSessionTestSupport.sessionFor(chapter = 1, station = 4)
        val q = session.currentQuestion as Question.DragWordToPictureQuestion
        require(q.pairs.size >= 2)

        val first = q.pairs[0].catalogEntryId
        val second = q.pairs[1].catalogEntryId
        assertEquals(
            AnswerResult.Wrong,
            session.submitDragWordToPicture(mapOf(first to second, second to first)),
        )
        assertEquals(0, session.correctCount)

        val correctPlacements = q.pairs.associate { it.catalogEntryId to it.catalogEntryId }
        assertEquals(AnswerResult.Correct, session.submitDragWordToPicture(correctPlacements))
        assertEquals(1, session.correctCount)
        assertEquals(AnswerResult.Correct, session.completeDragWordToPictureRound())
        assertEquals(1, session.correctCount)
    }
}
