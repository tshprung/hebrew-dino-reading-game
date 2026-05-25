package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.MainDispatcherRule
import com.tal.hebrewdino.ui.domain.ChallengeType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WordChallengeViewModelTest {
    @get:Rule
    val mainDispatcherRule: MainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialization_loads_five_challenges_for_requested_type() = runTest {
        val odd = WordChallengeViewModel(challengeType = ChallengeType.ODD_ONE_OUT)
        val oddState = odd.uiState.value
        assertEquals(ChallengeType.ODD_ONE_OUT, oddState.challengeType)
        assertEquals(5, oddState.challenges.size)
        assertTrue(oddState.challenges.all { it.challengeType == ChallengeType.ODD_ONE_OUT })
        assertTrue(oddState.challenges.all { it.options.size == 4 && it.options.contains(it.correctOption) })

        val rhyme = WordChallengeViewModel(challengeType = ChallengeType.RHYME)
        val rhymeState = rhyme.uiState.value
        assertEquals(ChallengeType.RHYME, rhymeState.challengeType)
        assertEquals(5, rhymeState.challenges.size)
        assertTrue(rhymeState.challenges.all { it.challengeType == ChallengeType.RHYME })
        assertTrue(rhymeState.challenges.all { it.options.size == 4 && it.options.contains(it.correctOption) })
    }

    @Test
    fun correct_selection_sets_success_then_advances_after_delay() = runTest {
        val vm = WordChallengeViewModel(challengeType = ChallengeType.ODD_ONE_OUT)
        val first = vm.uiState.value.current
        assertNotNull(first)
        val correct = first!!.correctOption

        vm.onOptionSelected(correct)
        assertEquals(correct, vm.uiState.value.selectedCorrectOption)
        assertEquals(0, vm.uiState.value.index)

        advanceTimeBy(1000L)
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.index)
        assertNull(vm.uiState.value.selectedCorrectOption)
        assertFalse(vm.uiState.value.isRoundComplete)
    }

    @Test
    fun incorrect_selection_does_not_advance_and_allows_retry() = runTest {
        val vm = WordChallengeViewModel(challengeType = ChallengeType.ODD_ONE_OUT)
        val first = vm.uiState.value.current!!
        val wrong = first.options.first { it != first.correctOption }

        val tokenBefore = vm.uiState.value.wrongAttemptToken
        vm.onOptionSelected(wrong)

        assertEquals(0, vm.uiState.value.index)
        assertNull(vm.uiState.value.selectedCorrectOption)
        assertEquals(tokenBefore + 1, vm.uiState.value.wrongAttemptToken)
        assertEquals(wrong, vm.uiState.value.lastWrongOption)
    }

    @Test
    fun completion_grants_reward_once_and_emits_finish_event() = runTest {
        var rewardCalls = 0
        val finishEvents = mutableListOf<Unit>()

        val vm =
            WordChallengeViewModel(
                challengeType = ChallengeType.RHYME,
                rewardHandler =
                    WordChallengeViewModel.RewardHandler {
                        rewardCalls += 1
                    },
            )

        val collectJob =
            launch {
                vm.finishEvents.collect { finishEvents += Unit }
            }

        repeat(5) {
            val current = vm.uiState.value.current ?: return@repeat
            vm.onOptionSelected(current.correctOption)
            advanceTimeBy(1000L)
            advanceUntilIdle()
        }

        assertTrue(vm.uiState.value.isRoundComplete)
        assertEquals(1, rewardCalls)
        assertEquals(1, finishEvents.size)

        collectJob.cancel()
    }
}

