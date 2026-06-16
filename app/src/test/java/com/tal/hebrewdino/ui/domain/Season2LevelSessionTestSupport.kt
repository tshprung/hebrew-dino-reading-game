package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

/** Shared helpers for Season 2 [LevelSession] integration tests. */
internal object Season2LevelSessionTestSupport {
    fun playableStations(chapter: Int? = null): List<Pair<Int, Int>> =
        (1..Season2ChapterRegistry.CHAPTER_COUNT)
            .filter { chapter == null || it == chapter }
            .flatMap { chapterIndex ->
                (1..Season2Chapter1StationOrder.STATION_COUNT).mapNotNull { stationId ->
                    if (Season2Chapter1StationOrder.isMemoryMatchStation(chapterIndex, stationId)) {
                        null
                    } else {
                        chapterIndex to stationId
                    }
                }
            }

    fun quizPlan(chapterIndex: Int, stationId: Int): StationQuizPlan =
        when {
            chapterIndex <= 2 ->
                Season2Chapter1StationOrder.quizPlan(chapterIndex, stationId)
            else ->
                Season2ChapterStationPlans.quizPlan(
                    Season2ChapterStationPlans.contextFor(chapterIndex)!!,
                    stationId,
                )
        }

    fun sessionFor(chapter: Int, station: Int): LevelSession {
        val chapterDef =
            Season2ChapterRegistry.chapter(chapter)
                ?: error("Season2 chapter $chapter not in registry")
        val plan = quizPlan(chapter, station)
        return LevelSession(plan = plan, letterPoolSpec = chapterDef.letterPoolSpec)
    }

    fun stationLabel(chapter: Int, station: Int, plan: StationQuizPlan): String {
        val advanced = plan.season2AdvancedMode?.name ?: "none"
        return "ch$chapter st$station mode=${plan.mode} advanced=$advanced"
    }

    fun expectedQuestionClass(plan: StationQuizPlan): Class<out Question> {
        plan.season2AdvancedMode?.let { mode ->
            return when (mode) {
                Season2AdvancedStationMode.PictureToWord -> Question.ImageMatchQuestion::class.java
                Season2AdvancedStationMode.MissingFirstLetter ->
                    Question.MissingFirstLetterQuestion::class.java
                Season2AdvancedStationMode.WordParts -> Question.WordPartsQuestion::class.java
                Season2AdvancedStationMode.Rhyming -> Question.RhymingQuestion::class.java
            }
        }
        return when (plan.mode) {
            StationQuizMode.PopBalloons,
            StationQuizMode.PickLetter,
            -> Question.PopBalloonsQuestion::class.java
            StationQuizMode.FindLetterGrid -> Question.FindLetterGridQuestion::class.java
            StationQuizMode.PictureStartsWith -> Question.PictureStartsWithQuestion::class.java
            StationQuizMode.ImageMatch -> Question.ImageMatchQuestion::class.java
            StationQuizMode.DragWordToPicture -> Question.DragWordToPictureQuestion::class.java
            StationQuizMode.DragMissingLetter -> Question.DragMissingLetterQuestion::class.java
            StationQuizMode.FinaleSlot -> Question.FinaleSlotQuestion::class.java
        }
    }

    fun submitCorrectAnswer(session: LevelSession, q: Question): AnswerResult =
        when (q) {
            is Question.PopBalloonsQuestion -> session.submitAnswer(q.correctAnswer)
            is Question.FindLetterGridQuestion -> session.completeCurrentRound()
            is Question.ImageMatchQuestion -> session.submitImageMatch(q.correctChoiceId)
            is Question.PictureStartsWithQuestion ->
                session.submitPictureStartsWith(q.correctLetter)
            is Question.DragWordToPictureQuestion -> {
                val placements = q.pairs.associate { it.catalogEntryId to it.catalogEntryId }
                session.submitDragWordToPicture(placements)
            }
            is Question.DragMissingLetterQuestion ->
                session.submitDragMissingLetter(q.correctLetter)
            is Question.MissingFirstLetterQuestion ->
                session.submitMissingFirstLetter(q.correctLetter)
            is Question.WordPartsQuestion -> {
                val picked =
                    q.splitOptions.first {
                        it.firstPart == q.firstPart && it.secondPart == q.correctPart
                    }
                session.submitWordParts(picked)
            }
            is Question.RhymingQuestion -> session.submitRhyming(q.correctChoiceId)
            is Question.FinaleSlotQuestion -> session.submitFinaleWords(q.words)
        }

    fun submitWrongAnswer(session: LevelSession, q: Question): AnswerResult =
        when (q) {
            is Question.PopBalloonsQuestion -> {
                val wrong = q.options.first { it != q.correctAnswer }
                session.submitAnswer(wrong)
            }
            is Question.FindLetterGridQuestion -> session.wrongTap()
            is Question.ImageMatchQuestion -> {
                val wrongId = q.choices.first { it.id != q.correctChoiceId }.id
                session.submitImageMatch(wrongId)
            }
            is Question.PictureStartsWithQuestion -> {
                val wrong = q.optionLetters.first { it != q.correctLetter }
                session.submitPictureStartsWith(wrong)
            }
            is Question.DragWordToPictureQuestion -> {
                require(q.pairs.size >= 2) { "need two pairs for mismatched placement" }
                val first = q.pairs[0].catalogEntryId
                val second = q.pairs[1].catalogEntryId
                session.submitDragWordToPicture(mapOf(first to second, second to first))
            }
            is Question.DragMissingLetterQuestion -> {
                val wrong = q.optionLetters.first { it != q.correctLetter }
                session.submitDragMissingLetter(wrong)
            }
            is Question.MissingFirstLetterQuestion -> {
                val wrong = q.optionLetters.first { it != q.correctLetter }
                session.submitMissingFirstLetter(wrong)
            }
            is Question.WordPartsQuestion -> {
                val wrong =
                    q.splitOptions.first {
                        it.firstPart != q.firstPart || it.secondPart != q.correctPart
                    }
                session.submitWordParts(wrong)
            }
            is Question.RhymingQuestion -> {
                val wrongId = q.choices.first { it.id != q.correctChoiceId }.id
                session.submitRhyming(wrongId)
            }
            is Question.FinaleSlotQuestion -> {
                session.submitFinaleWords(q.words.reversed())
            }
        }

    fun runFullStation(chapter: Int, station: Int) {
        val plan = quizPlan(chapter, station)
        val label = stationLabel(chapter, station, plan)
        val session = sessionFor(chapter, station)

        val first = session.currentQuestion
        assertNotNull("$label: first question null", first)
        assertTrue(
            "$label: expected ${expectedQuestionClass(plan).simpleName}, got ${first!!.javaClass.simpleName}",
            expectedQuestionClass(plan).isInstance(first),
        )

        repeat(plan.questionCount) { round ->
            val q = session.currentQuestion
            assertNotNull("$label round ${round + 1}/${plan.questionCount}: null question", q)
            assertEquals(
                "$label round ${round + 1}/${plan.questionCount}",
                AnswerResult.Correct,
                submitCorrectAnswer(session, q!!),
            )
            session.nextQuestion()
        }

        assertNull("$label: question after completion", session.currentQuestion)
        assertEquals("$label: correctCount", plan.questionCount, session.correctCount)
    }
}
