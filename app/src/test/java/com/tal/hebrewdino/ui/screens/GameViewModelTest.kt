package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.domain.LetterPoolSpec
import com.tal.hebrewdino.ui.domain.StationQuizPlans
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameViewModelTest {
    private fun viewModel(stationId: Int = 1) =
        GameViewModel(
            plan = StationQuizPlans.chapter1(stationId),
            letterPoolSpec = LetterPoolSpec.Default,
        )

    @Test
    fun initialPhase_isIntroAndInputLocked() {
        val vm = viewModel()
        assertEquals(GamePhase.Intro, vm.phase)
        assertTrue(vm.inputLocked)
        assertEquals(DinoVisual.Idle, vm.dinoVisual)
        assertFalse(vm.completionCallbackFired)
    }

    @Test
    fun session_isCreatedFromPlan() {
        val vm = viewModel(stationId = 2)
        assertTrue(vm.session.totalQuestions > 0)
        assertTrue(vm.session.currentQuestion != null)
    }

    @Test
    fun resetEpisode4HelpForNewQuestion_clearsLocksAndHint() {
        val vm = viewModel()
        vm.episode4HelpLocksChoices = true
        vm.episode4HelpActiveHintLetter = "א"
        vm.resetEpisode4HelpForNewQuestion()
        assertFalse(vm.episode4HelpLocksChoices)
        assertNull(vm.episode4HelpActiveHintLetter)
    }

    @Test
    fun resetBalloonHelpForNewQuestion_clearsLocksAndHint() {
        val vm = viewModel()
        vm.balloonHelpLocksChoices = true
        vm.balloonHelpHintLetter = "ב"
        vm.resetBalloonHelpForNewQuestion()
        assertFalse(vm.balloonHelpLocksChoices)
        assertNull(vm.balloonHelpHintLetter)
    }
}
