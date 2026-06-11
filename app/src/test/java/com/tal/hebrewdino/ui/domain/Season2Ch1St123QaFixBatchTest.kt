package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.ui.audio.InStationPraiseAudio
import com.tal.hebrewdino.ui.screens.HintPulseActions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2Ch1St123QaFixBatchTest {
    @Test
    fun balloonPraise_alwaysOnSeason2QuizPopBalloons() {
        assertTrue(Season2EarlyStationQaPolicy.shouldPlayBalloonPraiseOnCorrectPop(season2QuizBalloons = true))
        assertFalse(Season2EarlyStationQaPolicy.shouldPlayBalloonPraiseOnCorrectPop(season2QuizBalloons = false))
    }

    @Test
    fun inStationPraise_usesRawPoolForSeason2GameplayChapter() {
        assertTrue(InStationPraiseAudio.usesRawPraisePool(Season2ChapterIds.Chapter1Tyrannosaurus))
    }

    @Test
    fun pictureStartsWith_hintOnlyAfterTwoWrongTaps() {
        assertFalse(Season2EarlyStationQaPolicy.shouldShowPictureStartsWithHint(wrongTapsThisQuestion = 1))
        assertTrue(Season2EarlyStationQaPolicy.shouldShowPictureStartsWithHint(wrongTapsThisQuestion = 2))
    }

    @Test
    fun firstWrongTap_doesNotTriggerHintPulse() {
        val (taps, epoch) = HintPulseActions.registerWrongTapForHintPulse(wrongTapsThisQuestion = 0, hintPulseEpoch = 0)
        assertEquals(1, taps)
        assertEquals(0, epoch)
    }

    @Test
    fun doubleWrongTapRegistration_wasHintBug_nowSingleTapStaysBelowThreshold() {
        val first = HintPulseActions.registerWrongTapForHintPulse(wrongTapsThisQuestion = 0, hintPulseEpoch = 0)
        assertEquals(1, first.first)
        assertEquals(0, first.second)
    }

    @Test
    fun ch1_st3_coachReplayUsesWordNotLetterOnly() {
        assertTrue(
            Season2EarlyStationQaPolicy.shouldReplayWordForPictureStartsWithCoach(
                Season2Chapter1StationOrder.PICTURE_STARTS_WITH,
            ),
        )
        assertFalse(
            Season2EarlyStationQaPolicy.shouldReplayWordForPictureStartsWithCoach(
                Season2Chapter1StationOrder.PICK_LETTER,
            ),
        )
    }

    @Test
    fun popBalloonsActions_wiresSeason2QuizPraiseParams() {
        val source = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/PopBalloonsActions.kt")
        assertTrue(source.contains("season2QuizBalloons"))
        assertTrue(source.contains("InStationPraiseAudio.pick"))
    }

    @Test
    fun gameScreen_unlocksInputAfterSeason2FirstWrong() {
        val source = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/GameScreen.kt")
        assertTrue(source.contains("gameViewModel.inputLocked = false"))
        assertFalse(source.contains("HintPulseActions.registerWrongTapForHintPulse(gameViewModel)"))
    }

    @Test
    fun pictureStartsWith_passesSeason2QuizFlag() {
        val source = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/PictureStartsWithActions.kt")
        assertTrue(source.contains("isSeason2QuizChapter"))
    }

    @Test
    fun season1_pickLetterPlan_unchanged() {
        val plan = StationQuizPlans.chapter1(Chapter1StationOrder.TAP_LETTER)
        assertEquals(StationQuizMode.PickLetter, plan.mode)
        assertEquals(6, plan.questionCount)
    }

    private fun readProjectSource(relativePath: String): String {
        val candidates =
            listOf(
                java.io.File(relativePath),
                java.io.File("../$relativePath"),
                java.io.File("../../$relativePath"),
            )
        val file =
            candidates.firstOrNull { it.exists() }
                ?: error("Could not locate source file: $relativePath")
        return file.readText()
    }
}
