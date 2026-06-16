package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.test.ProjectSource

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2WarmupStationQaTest {
    @Test
    fun warmup_question_counts_matchSeason1Source() {
        val ctx = Season2ChapterStationPlans.contextFor(3)!!
        assertEquals(6, Season2ChapterStationPlans.quizPlan(ctx, 1).questionCount)
        assertEquals(8, Season2ChapterStationPlans.quizPlan(ctx, 2).questionCount)
        assertEquals(6, Season2ChapterStationPlans.quizPlan(ctx, 3).questionCount)
        val ch4 = Season2ChapterStationPlans.contextFor(4)!!
        assertEquals(6, Season2ChapterStationPlans.quizPlan(ch4, 1).questionCount)
        assertEquals(6, Season2ChapterStationPlans.quizPlan(ch4, 2).questionCount)
    }

    @Test
    fun s2_gameplay_uses_raw_letter_feedback() {
        assertTrue(
            Season2WarmupStationQaPolicy.usesRawLetterNameStationFeedback(
                Season2ChapterIds.Chapter3Stegosaurus,
            ),
        )
        assertTrue(
            Season2WarmupStationQaPolicy.usesRawLetterNameStationFeedback(
                Season2ChapterIds.Chapter4Brachiosaurus,
            ),
        )
        assertFalse(Season2WarmupStationQaPolicy.usesRawLetterNameStationFeedback(3))
    }

    @Test
    fun balloon_praise_after_coach_uses_companion_not_narrator() {
        assertTrue(
            Season2WarmupStationQaPolicy.shouldPlayBalloonCompanionPraiseAfterCoach(
                season2QuizBalloons = true,
                isCorrect = true,
                afterCoachIntervention = true,
            ),
        )
        assertFalse(
            Season2WarmupStationQaPolicy.shouldPlayBalloonProgressPraise(
                season2QuizBalloons = true,
                finalCorrectBalloon = false,
                afterCoachIntervention = true,
            ),
        )
        assertTrue(
            Season2WarmupStationQaPolicy.shouldPlayBalloonFinalRoundPraise(
                season2QuizBalloons = true,
                finalCorrectBalloon = true,
                afterCoachIntervention = false,
            ),
        )
    }

    @Test
    fun wiring_sources() {
        val pick = ProjectSource.read("app/src/main/java/com/tal/hebrewdino/ui/screens/PickLetterActions.kt")
        assertTrue(pick.contains("Season2WarmupStationQaPolicy.usesRawLetterNameStationFeedback"))
        assertTrue(pick.contains("Season2PostFocusCorrectAudio.playBlocking"))
        val pop = ProjectSource.read("app/src/main/java/com/tal/hebrewdino/ui/screens/PopBalloonsActions.kt")
        assertTrue(pop.contains("shouldPlayBalloonCompanionPraiseAfterCoach"))
        val wrong = ProjectSource.read("app/src/main/java/com/tal/hebrewdino/ui/screens/WrongFeedbackActions.kt")
        assertTrue(wrong.contains("isSeason2GameplayChapter(chapterId)"))
        val memory =
            ProjectSource.read("app/src/main/java/com/tal/hebrewdino/ui/screens/Season2MemoryMatchStationScreen.kt")
        assertTrue(memory.contains("TextAlign.Center"))
        val wordParts = ProjectSource.read("app/src/main/java/com/tal/hebrewdino/ui/game/Season2WordPartsGame.kt")
        assertFalse(wordParts.contains("HintOptionsExtraPhysicalLeftDp"))
        val picture = ProjectSource.read("app/src/main/java/com/tal/hebrewdino/ui/game/PictureStartsWithGame.kt")
        assertTrue(picture.contains("HalfCmPhysicalLeftDp"))
    }
}
