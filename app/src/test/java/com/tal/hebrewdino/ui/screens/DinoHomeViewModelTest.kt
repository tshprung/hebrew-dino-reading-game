package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.FakeCharacterStore
import com.tal.hebrewdino.ui.MainDispatcherRule
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.data.InMemoryStoryNarrationGate
import com.tal.hebrewdino.ui.domain.DinoStoryScripts
import com.tal.hebrewdino.ui.domain.economy.GrowthStage
import com.tal.hebrewdino.ui.withFakeCharacterStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DinoHomeViewModelTest {
    @get:Rule
    val mainDispatcherRule: MainDispatcherRule = MainDispatcherRule()

    private fun viewModel(
        repo: com.tal.hebrewdino.ui.data.CharacterRepository,
        story: InMemoryStoryNarrationGate = InMemoryStoryNarrationGate(),
    ): DinoHomeViewModel = DinoHomeViewModel(repo, story)

    @Test
    fun feeding_decrements_food_and_keeps_growth_stage_when_not_crossing_threshold() = runTest {
        val store =
            FakeCharacterStore(
                character = DinoCharacter.DINO_GREEN,
                foodCount = 6,
                totalFoodEarned = 10,
                growthStage = "BABY",
                initialized = true,
            )
        withFakeCharacterStore(store) { repo, fake ->
            val vm = viewModel(repo)
            advanceUntilIdle()

            vm.feedOnce()
            advanceUntilIdle()
            assertEquals(5, vm.wallet.value?.applesCount)
            assertEquals(GrowthStage.BABY, vm.wallet.value?.growthStage)
            assertEquals(5, fake.foodCountFlow.value)
            assertEquals("BABY", fake.growthStageFlow.value)
        }
    }

    @Test
    fun egg_stage_can_hatch_by_feeding_to_three_fed_apples() = runTest {
        val store =
            FakeCharacterStore(
                character = DinoCharacter.DINO_GREEN,
                foodCount = 1,
                totalFoodEarned = 3,
                growthStage = "EGG",
                initialized = true,
            )
        withFakeCharacterStore(store) { repo, fake ->
            val vm = viewModel(repo)
            advanceUntilIdle()

            vm.feedOnce()
            advanceUntilIdle()

            assertEquals(0, vm.wallet.value?.applesCount)
            assertEquals(GrowthStage.BABY, vm.wallet.value?.growthStage)
            assertEquals(0, fake.foodCountFlow.value)
            assertEquals("BABY", fake.growthStageFlow.value)
        }
    }

    @Test
    fun growth_stage_updates_from_total_food_earned_thresholds() = runTest {
        val store =
            FakeCharacterStore(
                character = DinoCharacter.DINO_GREEN,
                foodCount = 0,
                totalFoodEarned = 2,
                growthStage = "EGG",
                initialized = true,
            )
        withFakeCharacterStore(store) { repo, fake ->
            val vm = viewModel(repo)
            advanceUntilIdle()
            assertEquals(GrowthStage.EGG, vm.wallet.value?.growthStage)

            fake.totalFoodEarnedFlow.value = 3
            advanceUntilIdle()
            assertEquals(GrowthStage.BABY, vm.wallet.value?.growthStage)
            assertEquals("BABY", fake.growthStageFlow.value)

            fake.totalFoodEarnedFlow.value = 11
            advanceUntilIdle()
            assertEquals(GrowthStage.ADULT, vm.wallet.value?.growthStage)
            assertEquals("ADULT", fake.growthStageFlow.value)
        }
    }

    @Test
    fun hatching_queues_part2_baby_story_for_tts() = runTest {
        val store =
            FakeCharacterStore(
                character = DinoCharacter.DINO_GREEN,
                foodCount = 1,
                totalFoodEarned = 3,
                growthStage = "EGG",
                initialized = true,
            )
        withFakeCharacterStore(store) { repo, _ ->
            val vm = viewModel(repo, InMemoryStoryNarrationGate())
            advanceUntilIdle()

            vm.feedOnce()
            advanceUntilIdle()

            val wallet = vm.wallet.value!!
            assertEquals(GrowthStage.BABY, wallet.growthStage)
            assertTrue(vm.homeSpeechEpoch >= 1)
            assertEquals(DinoStoryScripts.part2BabyHatchSpokenForTts(), vm.homePromptSpokenForTts(wallet))
        }
    }

    @Test
    fun character_flow_emits_dina_and_dino_from_repository() = runTest {
        val store =
            FakeCharacterStore(
                character = DinoCharacter.DINA_PINK,
                foodCount = 0,
                totalFoodEarned = 0,
                growthStage = "EGG",
                initialized = true,
            )
        withFakeCharacterStore(store) { repo, fake ->
            val vm = viewModel(repo)
            val first = vm.character.first { it == DinoCharacter.DINA_PINK }
            assertEquals(DinoCharacter.DINA_PINK, first)

            fake.characterFlow.value = DinoCharacter.DINO_GREEN
            val second = vm.character.first { it == DinoCharacter.DINO_GREEN }
            assertEquals(DinoCharacter.DINO_GREEN, second)
        }
    }
}
