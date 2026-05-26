package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class FallingLettersViewModelTest {
    @get:Rule
    val mainDispatcherRule: MainDispatcherRule = MainDispatcherRule()

    @Test
    fun wrong_click_does_not_advance_caught_and_triggers_negative_feedback() = runTest {
        val vm = FallingLettersViewModel(rng = Random(0))
        try {
            vm.setViewport(heightPx = 800f, chipHeightPx = 60f)
            advanceTimeBy(1500L)
            runCurrent()

            val before = vm.uiState.value
            val wrong = before.letters.first { it.text != before.targetLetter }

            vm.onLetterClicked(wrong.id)
            val after = vm.uiState.value

            assertEquals(before.caughtInRound, after.caughtInRound)
            assertTrue(after.shakeToken > before.shakeToken)
        } finally {
            vm.stopTicker()
        }
    }

    @Test
    fun completing_three_rounds_emits_finish_and_calls_reward_once() = runTest {
        var rewardCalls = 0
        val events = mutableListOf<Unit>()
        val vm =
            FallingLettersViewModel(
                rng = Random(1),
                rewardHandler = FallingLettersViewModel.RewardHandler { rewardCalls += 1 },
                targetsPerRound = 1,
            )

        vm.setViewport(heightPx = 800f, chipHeightPx = 60f)
        val finishJob = launch { vm.finishEvents.collect { events += Unit } }
        val roundBreakJob =
            launch {
                vm.roundBreakEvents.collect { token ->
                    vm.onRoundBreakFinished(token)
                }
            }

        try {
            repeat(vm.uiState.value.roundsTotal) {
                var s = vm.uiState.value
                var tries = 0
                while (s.letters.none { it.text == s.targetLetter } && tries < 20) {
                    advanceTimeBy(300L)
                    runCurrent()
                    s = vm.uiState.value
                    tries += 1
                }
                val correctId = s.letters.first { it.text == s.targetLetter }.id
                vm.onLetterClicked(correctId)
                advanceTimeBy(300L)
                runCurrent()
            }

            assertTrue(vm.uiState.value.isComplete)
            assertEquals(1, rewardCalls)
            assertEquals(1, events.size)
        } finally {
            roundBreakJob.cancel()
            finishJob.cancel()
            vm.stopTicker()
        }
    }
}
