package com.tal.hebrewdino.ui

import com.tal.hebrewdino.ui.data.CharacterRepository
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.domain.economy.FoodInventoryCodec
import com.tal.hebrewdino.ui.domain.economy.PendingRewardEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeCharacterStore(
    character: DinoCharacter = DinoCharacter.DINO_GREEN,
    foodCount: Int = 0,
    totalFoodEarned: Int = 0,
    growthStage: String = "EGG",
    fullUntilAtMs: Long = 0L,
    private var initialized: Boolean = true,
    initialFoodInventory: Map<String, Int> =
        if (foodCount > 0) {
            mapOf(FoodInventoryCodec.DEFAULT_FOOD_EMOJI to foodCount)
        } else {
            emptyMap()
        },
) : CharacterRepository.CharacterStore {
    override val characterFlow: MutableStateFlow<DinoCharacter> = MutableStateFlow(character)
    val foodInventoryState = MutableStateFlow(initialFoodInventory)
    override val foodInventoryFlow: Flow<Map<String, Int>> = foodInventoryState
    val eggHatchedState = MutableStateFlow(growthStage != "EGG")
    val eggTapCountState = MutableStateFlow(0)
    override val eggHatchedFlow: Flow<Boolean> = eggHatchedState
    override val eggTapCountFlow: Flow<Int> = eggTapCountState
    override val foodCountFlow: Flow<Int> =
        foodInventoryFlow.map { FoodInventoryCodec.totalCount(it) }
    override val totalFoodEarnedFlow: MutableStateFlow<Int> = MutableStateFlow(totalFoodEarned)
    override val growthStageFlow: MutableStateFlow<String> = MutableStateFlow(growthStage)
    override val fullUntilAtMsFlow: MutableStateFlow<Long> = MutableStateFlow(fullUntilAtMs.coerceAtLeast(0L))
    override val chapter1MaxCompletedStationFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    override val activeChapterIndexFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    override val highestUnlockedChapterIndexFlow: MutableStateFlow<Int> = MutableStateFlow(0)

    private var pendingEvent: PendingRewardEvent? = null
    private val chapterStations: MutableMap<Int, MutableStateFlow<Int>> =
        (0 until 4).associateWithTo(mutableMapOf()) { MutableStateFlow(0) }

    override fun chapterMaxCompletedStationFlow(chapterIndex: Int): Flow<Int> =
        chapterStations.getOrPut(chapterIndex.coerceIn(0, 3)) { MutableStateFlow(0) }

    override suspend fun setCharacter(character: DinoCharacter) {
        characterFlow.value = character
    }

    override suspend fun setFoodCount(foodCount: Int) {
        val count = foodCount.coerceAtLeast(0)
        foodInventoryState.value =
            if (count == 0) {
                emptyMap()
            } else {
                mapOf(FoodInventoryCodec.DEFAULT_FOOD_EMOJI to count)
            }
    }

    override suspend fun addFood(
        delta: Int,
        foodEmoji: String,
    ) {
        if (delta == 0) return
        val inv = foodInventoryState.value.toMutableMap()
        val emoji = foodEmoji.ifBlank { FoodInventoryCodec.DEFAULT_FOOD_EMOJI }
        inv[emoji] = ((inv[emoji] ?: 0) + delta).coerceAtLeast(0)
        foodInventoryState.value = inv
        if (delta > 0) {
            totalFoodEarnedFlow.value = (totalFoodEarnedFlow.value + delta).coerceAtLeast(0)
        }
    }

    override suspend fun consumeOneFood(foodEmoji: String?): Boolean {
        val inv = foodInventoryState.value.toMutableMap()
        val emoji =
            foodEmoji?.takeIf { (inv[it] ?: 0) > 0 }
                ?: inv.entries.firstOrNull { it.value > 0 }?.key
                ?: return false
        val next = (inv[emoji] ?: 0) - 1
        if (next > 0) {
            inv[emoji] = next
        } else {
            inv.remove(emoji)
        }
        foodInventoryState.value = inv
        return true
    }

    override suspend fun recordEggTap(): Int {
        if (eggHatchedState.value) return eggTapCountState.value
        val next =
            (eggTapCountState.value + 1).coerceAtMost(
                com.tal.hebrewdino.ui.domain.economy.TamagotchiRules.EGG_TAPS_TO_HATCH,
            )
        eggTapCountState.value = next
        return next
    }

    override suspend fun setEggHatched() {
        eggHatchedState.value = true
        eggTapCountState.value = com.tal.hebrewdino.ui.domain.economy.TamagotchiRules.EGG_TAPS_TO_HATCH
        growthStageFlow.value = "BABY"
    }

    override suspend fun setGrowthStage(stageName: String) {
        growthStageFlow.value = stageName
        if (stageName != "EGG") {
            eggHatchedState.value = true
        }
    }

    override suspend fun savePendingRewardEvent(event: PendingRewardEvent) {
        pendingEvent = event
    }

    override suspend fun readPendingRewardEvent(): PendingRewardEvent? = pendingEvent

    override suspend fun clearPendingRewardEvent() {
        pendingEvent = null
    }

    override suspend fun setFullUntilAtMs(fullUntilAtMs: Long) {
        fullUntilAtMsFlow.value = fullUntilAtMs.coerceAtLeast(0L)
    }

    override suspend fun markChapter1StationCompleted(stationId: Int) {
        markChapterStationCompleted(chapterIndex = 0, stationId = stationId)
    }

    override suspend fun setActiveChapterIndex(chapterIndex: Int) {
        activeChapterIndexFlow.value = chapterIndex.coerceIn(0, 3)
    }

    override suspend fun markChapterStationCompleted(
        chapterIndex: Int,
        stationId: Int,
    ) {
        val ch = chapterIndex.coerceIn(0, 3)
        val clamped = stationId.coerceIn(0, 3)
        chapterStations.getOrPut(ch) { MutableStateFlow(0) }.value =
            maxOf(chapterStations[ch]!!.value, clamped)
        if (ch == 0) {
            chapter1MaxCompletedStationFlow.value =
                maxOf(chapter1MaxCompletedStationFlow.value, clamped)
        }
        if (clamped >= 3) {
            highestUnlockedChapterIndexFlow.value =
                maxOf(highestUnlockedChapterIndexFlow.value, (ch + 1).coerceAtMost(3))
        }
    }

    override suspend fun ensureTamagotchiInitialized() {
        if (initialized) return
        if (foodInventoryState.value.isEmpty()) {
            foodInventoryState.value = emptyMap()
        }
        if (growthStageFlow.value.isBlank()) growthStageFlow.value = "EGG"
        fullUntilAtMsFlow.value = fullUntilAtMsFlow.value.coerceAtLeast(0L)
        chapter1MaxCompletedStationFlow.value = chapter1MaxCompletedStationFlow.value.coerceIn(0, 3)
        initialized = true
    }

    override suspend fun resetForNewGame() {
        foodInventoryState.value = emptyMap()
        totalFoodEarnedFlow.value = 0
        growthStageFlow.value = "EGG"
        eggHatchedState.value = false
        eggTapCountState.value = 0
        pendingEvent = null
        fullUntilAtMsFlow.value = 0L
        chapter1MaxCompletedStationFlow.value = 0
        activeChapterIndexFlow.value = 0
        highestUnlockedChapterIndexFlow.value = 0
        chapterStations.values.forEach { it.value = 0 }
        initialized = true
    }
}

suspend fun withFakeCharacterStore(
    store: FakeCharacterStore = FakeCharacterStore(),
    block: suspend (CharacterRepository, FakeCharacterStore) -> Unit,
) {
    val previous = CharacterRepository.storeFactory
    val previousInventory = com.tal.hebrewdino.ui.data.InventoryStore.factory
    CharacterRepository.storeFactory = { store }
    com.tal.hebrewdino.ui.data.InventoryStore.factory = { _ ->
        com.tal.hebrewdino.ui.data.InMemoryInventoryStore()
    }
    try {
        block(CharacterRepository(TestAppContext()), store)
    } finally {
        CharacterRepository.storeFactory = previous
        com.tal.hebrewdino.ui.data.InventoryStore.factory = previousInventory
    }
}
