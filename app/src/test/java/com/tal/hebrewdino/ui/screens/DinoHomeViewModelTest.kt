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
    fun feeding_decrements_food_and_keeps_growth_stage_when_not_crossing_threshold() = runTest {
        val oldFactory = CharacterRepository.storeFactory
        val store =
            FakeStore(
                character = DinoCharacter.DINO_GREEN,
                foodCount = 6,
                totalFoodEarned = 10,
                growthStage = "BABY",
                initialized = true,
            )
        CharacterRepository.storeFactory =
            {
                store
            }

        try {
            val vm = DinoHomeViewModel(TestContext())
            advanceUntilIdle()

            vm.feedOnce()
            advanceUntilIdle()
            assertEquals(5, vm.foodCount)
            assertEquals(GrowthStage.BABY, vm.growthStage)
            assertEquals(5, store.foodCountFlow.value)
            assertEquals("BABY", store.growthStageFlow.value)
        } finally {
            CharacterRepository.storeFactory = oldFactory
        }
    }

    @Test
    fun egg_stage_can_hatch_by_feeding_to_three_fed_apples() = runTest {
        val oldFactory = CharacterRepository.storeFactory
        val store =
            FakeStore(
                character = DinoCharacter.DINO_GREEN,
                foodCount = 1,
                totalFoodEarned = 3,
                growthStage = "EGG",
                initialized = true,
            )
        CharacterRepository.storeFactory =
            {
                store
            }

        try {
            val vm = DinoHomeViewModel(TestContext())
            advanceUntilIdle()

            vm.feedOnce()
            advanceUntilIdle()

            assertEquals(0, vm.foodCount)
            assertEquals(GrowthStage.BABY, vm.growthStage)
            assertEquals(0, store.foodCountFlow.value)
            assertEquals("BABY", store.growthStageFlow.value)
        } finally {
            CharacterRepository.storeFactory = oldFactory
        }
    }

    @Test
    fun growth_stage_updates_from_total_food_earned_thresholds() = runTest {
        val oldFactory = CharacterRepository.storeFactory
        val store =
            FakeStore(
                character = DinoCharacter.DINO_GREEN,
                foodCount = 0,
                totalFoodEarned = 2,
                growthStage = "EGG",
                initialized = true,
            )
        CharacterRepository.storeFactory =
            {
                store
            }

        try {
            val vm = DinoHomeViewModel(TestContext())
            advanceUntilIdle()
            assertEquals(GrowthStage.EGG, vm.growthStage)

            store.totalFoodEarnedFlow.value = 3
            advanceUntilIdle()
            assertEquals(GrowthStage.BABY, vm.growthStage)
            assertEquals("BABY", store.growthStageFlow.value)

            store.totalFoodEarnedFlow.value = 11
            advanceUntilIdle()
            assertEquals(GrowthStage.ADULT, vm.growthStage)
            assertEquals("ADULT", store.growthStageFlow.value)
        } finally {
            CharacterRepository.storeFactory = oldFactory
        }
    }

    @Test
    fun character_flow_emits_dina_and_dino_from_repository() = runTest {
        val oldFactory = CharacterRepository.storeFactory
        val store =
            FakeStore(
                character = DinoCharacter.DINA_PINK,
                foodCount = 0,
                totalFoodEarned = 0,
                growthStage = "EGG",
                initialized = true,
            )
        CharacterRepository.storeFactory =
            {
                store
            }

        try {
            val vm = DinoHomeViewModel(TestContext())
            val first = vm.character.first { it == DinoCharacter.DINA_PINK }
            assertEquals(DinoCharacter.DINA_PINK, first)

            store.characterFlow.value = DinoCharacter.DINO_GREEN
            val second = vm.character.first { it == DinoCharacter.DINO_GREEN }
            assertEquals(DinoCharacter.DINO_GREEN, second)
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
        totalFoodEarned: Int,
        growthStage: String,
        fullUntilAtMs: Long = 0L,
        private var initialized: Boolean,
    ) : CharacterRepository.CharacterStore {
        override val characterFlow: MutableStateFlow<DinoCharacter> = MutableStateFlow(character)
        override val foodCountFlow: MutableStateFlow<Int> = MutableStateFlow(foodCount)
        override val totalFoodEarnedFlow: MutableStateFlow<Int> = MutableStateFlow(totalFoodEarned)
        override val growthStageFlow: MutableStateFlow<String> = MutableStateFlow(growthStage)
        override val pendingRewardFoodDeltaFlow: MutableStateFlow<Int> = MutableStateFlow(0)
        override val fullUntilAtMsFlow: MutableStateFlow<Long> = MutableStateFlow(fullUntilAtMs.coerceAtLeast(0L))
        override val chapter1MaxCompletedStationFlow: MutableStateFlow<Int> = MutableStateFlow(0)

        override suspend fun setCharacter(character: DinoCharacter) {
            characterFlow.value = character
        }

        override suspend fun setFoodCount(foodCount: Int) {
            foodCountFlow.value = foodCount
        }

        override suspend fun addFood(delta: Int) {
            foodCountFlow.value = (foodCountFlow.value + delta).coerceAtLeast(0)
            if (delta > 0) {
                totalFoodEarnedFlow.value = (totalFoodEarnedFlow.value + delta).coerceAtLeast(0)
            }
        }

        override suspend fun setGrowthStage(stageName: String) {
            growthStageFlow.value = stageName
        }

        override suspend fun setPendingRewardFoodDelta(delta: Int) {
            pendingRewardFoodDeltaFlow.value = delta
        }

        override suspend fun setFullUntilAtMs(fullUntilAtMs: Long) {
            fullUntilAtMsFlow.value = fullUntilAtMs.coerceAtLeast(0L)
        }

        override suspend fun markChapter1StationCompleted(stationId: Int) {
            chapter1MaxCompletedStationFlow.value =
                maxOf(
                    chapter1MaxCompletedStationFlow.value.coerceIn(0, 3),
                    stationId.coerceIn(0, 3),
                )
        }

        override suspend fun ensureTamagotchiInitialized() {
            if (initialized) return
            val current = foodCountFlow.value.coerceAtLeast(0)
            foodCountFlow.value = maxOf(current, 0)
            if (growthStageFlow.value.isBlank()) growthStageFlow.value = "EGG"
            fullUntilAtMsFlow.value = fullUntilAtMsFlow.value.coerceAtLeast(0L)
            chapter1MaxCompletedStationFlow.value = chapter1MaxCompletedStationFlow.value.coerceIn(0, 3)
            initialized = true
        }

        override suspend fun resetForNewGame() {
            foodCountFlow.value = 0
            totalFoodEarnedFlow.value = 0
            growthStageFlow.value = "EGG"
            pendingRewardFoodDeltaFlow.value = 0
            fullUntilAtMsFlow.value = 0L
            chapter1MaxCompletedStationFlow.value = 0
            initialized = true
        }
    }
}
