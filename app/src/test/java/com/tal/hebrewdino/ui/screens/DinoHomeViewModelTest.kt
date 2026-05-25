package com.tal.hebrewdino.ui.screens

import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import com.tal.hebrewdino.ui.MainDispatcherRule
import com.tal.hebrewdino.ui.data.CharacterRepository
import com.tal.hebrewdino.ui.data.DinoCharacter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DinoHomeViewModelTest {
    @get:Rule
    val mainDispatcherRule: MainDispatcherRule = MainDispatcherRule()

    @Test
    fun feeding_decrements_food_increments_progress_and_advances_growth_stage() = runTest {
        val oldFactory = CharacterRepository.storeFactory
        val store = FakeStore(character = DinoCharacter.Dino, foodCount = 10, growthStage = "EGG", initialized = true)
        CharacterRepository.storeFactory =
            {
                store
            }

        try {
            val vm = DinoHomeViewModel(TestContext())
            advanceUntilIdle()
            setFloatState(vm, "growthProgress01", 0f)
            setState(vm, "isHungry", true)

            vm.feedOnce()
            advanceUntilIdle()
            assertEquals(9, vm.foodCount)
            assertFalse(vm.isHungry)
            assertEquals(0.34f, vm.growthProgress01, 0.001f)
            assertEquals(GrowthStage.EGG, vm.growthStage)
            assertEquals(9, store.foodCountFlow.value)
            assertEquals("EGG", store.growthStageFlow.value)

            vm.feedOnce()
            advanceUntilIdle()
            assertEquals(8, vm.foodCount)
            assertEquals(0.68f, vm.growthProgress01, 0.001f)
            assertEquals(GrowthStage.EGG, vm.growthStage)
            assertEquals(8, store.foodCountFlow.value)

            vm.feedOnce()
            advanceUntilIdle()
            assertEquals(7, vm.foodCount)
            assertEquals(0f, vm.growthProgress01, 0.001f)
            assertEquals(GrowthStage.BABY, vm.growthStage)
            assertTrue(vm.isHungry)
            assertEquals(7, store.foodCountFlow.value)
            assertEquals("BABY", store.growthStageFlow.value)

            vm.feedOnce()
            vm.feedOnce()
            vm.feedOnce()
            advanceUntilIdle()
            assertEquals(4, vm.foodCount)
            assertEquals(GrowthStage.ADULT, vm.growthStage)
            assertEquals(0f, vm.growthProgress01, 0.001f)
            assertEquals(4, store.foodCountFlow.value)
            assertEquals("ADULT", store.growthStageFlow.value)

            val foodBefore = vm.foodCount
            vm.feedOnce()
            advanceUntilIdle()
            assertEquals(foodBefore - 1, vm.foodCount)
            assertEquals(GrowthStage.ADULT, vm.growthStage)
            assertEquals(0f, vm.growthProgress01, 0.001f)
            assertEquals(foodBefore - 1, store.foodCountFlow.value)
        } finally {
            CharacterRepository.storeFactory = oldFactory
        }
    }

    @Test
    fun feeding_with_zero_food_does_not_change_state_or_crash() = runTest {
        val oldFactory = CharacterRepository.storeFactory
        val store = FakeStore(character = DinoCharacter.Dino, foodCount = 0, growthStage = "BABY", initialized = true)
        CharacterRepository.storeFactory =
            {
                store
            }

        try {
            val vm = DinoHomeViewModel(TestContext())
            advanceUntilIdle()
            setFloatState(vm, "growthProgress01", 0.5f)
            setState(vm, "isHungry", true)

            vm.feedOnce()
            advanceUntilIdle()

            assertEquals(0, vm.foodCount)
            assertEquals(GrowthStage.BABY, vm.growthStage)
            assertEquals(0.5f, vm.growthProgress01, 0.001f)
            assertTrue(vm.isHungry)
            assertEquals(0, store.foodCountFlow.value)
            assertEquals("BABY", store.growthStageFlow.value)
        } finally {
            CharacterRepository.storeFactory = oldFactory
        }
    }

    @Test
    fun character_flow_emits_dina_and_dino_from_repository() = runTest {
        val oldFactory = CharacterRepository.storeFactory
        val store = FakeStore(character = DinoCharacter.Dina, foodCount = 3, growthStage = "EGG", initialized = true)
        CharacterRepository.storeFactory =
            {
                store
            }

        try {
            val vm = DinoHomeViewModel(TestContext())
            val first = vm.character.filterNotNull().first { it == DinoCharacter.Dina }
            assertEquals(DinoCharacter.Dina, first)

            store.characterFlow.value = DinoCharacter.Dino
            val second = vm.character.filterNotNull().first { it == DinoCharacter.Dino }
            assertEquals(DinoCharacter.Dino, second)
        } finally {
            CharacterRepository.storeFactory = oldFactory
        }
    }

    private class TestContext : ContextWrapper(null) {
        override fun getApplicationContext(): Context = this
    }

    private fun setFloatState(target: Any, propertyName: String, value: Float) {
        val f = target.javaClass.getDeclaredField("${propertyName}\$delegate")
        f.isAccessible = true
        val state = f.get(target) as MutableFloatState
        state.floatValue = value
    }

    private fun <T> setState(target: Any, propertyName: String, value: T) {
        val f = target.javaClass.getDeclaredField("${propertyName}\$delegate")
        f.isAccessible = true
        val state = f.get(target) as MutableState<T>
        state.value = value
    }

    private class FakeStore(
        character: DinoCharacter,
        foodCount: Int,
        growthStage: String,
        private var initialized: Boolean,
    ) : CharacterRepository.CharacterStore {
        override val characterFlow: MutableStateFlow<DinoCharacter?> = MutableStateFlow(character)
        override val foodCountFlow: MutableStateFlow<Int> = MutableStateFlow(foodCount)
        override val growthStageFlow: MutableStateFlow<String> = MutableStateFlow(growthStage)
        override val pendingRewardFoodDeltaFlow: MutableStateFlow<Int> = MutableStateFlow(0)

        override suspend fun setCharacter(character: DinoCharacter) {
            characterFlow.value = character
        }

        override suspend fun setFoodCount(foodCount: Int) {
            foodCountFlow.value = foodCount
        }

        override suspend fun addFood(delta: Int) {
            foodCountFlow.value = (foodCountFlow.value + delta).coerceAtLeast(0)
        }

        override suspend fun setGrowthStage(stageName: String) {
            growthStageFlow.value = stageName
        }

        override suspend fun setPendingRewardFoodDelta(delta: Int) {
            pendingRewardFoodDeltaFlow.value = delta
        }

        override suspend fun ensureTamagotchiInitialized() {
            if (initialized) return
            val current = foodCountFlow.value.coerceAtLeast(0)
            foodCountFlow.value = maxOf(current, 3)
            if (growthStageFlow.value.isBlank()) growthStageFlow.value = "EGG"
            initialized = true
        }
    }
}
