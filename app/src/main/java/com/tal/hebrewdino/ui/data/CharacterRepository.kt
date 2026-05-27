package com.tal.hebrewdino.ui.data

import android.content.Context
import com.tal.hebrewdino.ui.domain.HebrewSyllabus
import com.tal.hebrewdino.ui.domain.economy.GrowthStage
import com.tal.hebrewdino.ui.domain.economy.ParticleCue
import com.tal.hebrewdino.ui.domain.economy.PendingRewardEvent
import com.tal.hebrewdino.ui.domain.economy.PlayerWallet
import com.tal.hebrewdino.ui.domain.cosmetics.AccessoryCatalog
import com.tal.hebrewdino.ui.domain.cosmetics.AccessoryProgression
import com.tal.hebrewdino.ui.domain.economy.FoodInventoryCodec
import com.tal.hebrewdino.ui.domain.economy.TamagotchiRules
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class CharacterRepository(context: Context) {
    private val appContext = context.applicationContext
    private val store: CharacterStore = storeFactory(appContext)
    private val inventoryStore: InventoryStoreOperations = InventoryStore.factory(appContext)

    val characterFlow: Flow<DinoCharacter> = store.characterFlow
    val foodCountFlow: Flow<Int> = store.foodCountFlow
    val foodInventoryFlow: Flow<Map<String, Int>> = store.foodInventoryFlow
    val totalFoodEarnedFlow: Flow<Int> = store.totalFoodEarnedFlow
    val growthStageFlow: Flow<String> = store.growthStageFlow
    val fullUntilAtMsFlow: Flow<Long> = store.fullUntilAtMsFlow

    val eggTapCountFlow: Flow<Int> = store.eggTapCountFlow

    val eggHatchedFlow: Flow<Boolean> = store.eggHatchedFlow

    val playerWalletFlow: Flow<PlayerWallet> =
        combine(
            combine(
                store.foodCountFlow,
                store.totalFoodEarnedFlow,
                store.growthStageFlow,
                store.fullUntilAtMsFlow,
                store.eggHatchedFlow,
            ) { food, totalEarned, stageName, fullUntil, eggHatched ->
                WalletSnapshot(
                    food = food,
                    totalEarned = totalEarned,
                    stageName = stageName,
                    fullUntil = fullUntil,
                    eggHatched = eggHatched,
                )
            },
            store.eggTapCountFlow,
        ) { snapshot, eggTapCount ->
            TamagotchiRules.buildWallet(
                applesCount = snapshot.food,
                totalEarned = snapshot.totalEarned,
                storedStageName = snapshot.stageName,
                fullUntilAtMs = snapshot.fullUntil,
                eggHatched = snapshot.eggHatched,
                eggTapCount = eggTapCount,
            )
        }
    val chapter1MaxCompletedStationFlow: Flow<Int> = store.chapter1MaxCompletedStationFlow

    val activeChapterIndexFlow: Flow<Int> = store.activeChapterIndexFlow

    val highestUnlockedChapterIndexFlow: Flow<Int> = store.highestUnlockedChapterIndexFlow

    val ownedAccessoriesFlow: Flow<Set<String>> = inventoryStore.ownedAccessoriesFlow

    val equippedAccessoryFlow: Flow<String?> = inventoryStore.equippedAccessoryFlow

    val pendingAccessoryEquipFlow: Flow<String?> = inventoryStore.pendingAccessoryEquipFlow

    fun chapterMaxCompletedStationFlow(chapterIndex: Int): Flow<Int> =
        store.chapterMaxCompletedStationFlow(chapterIndex)

    suspend fun setCharacter(character: DinoCharacter) {
        store.setCharacter(character)
    }

    suspend fun setFoodCount(foodCount: Int) {
        store.setFoodCount(foodCount.coerceAtLeast(0))
    }

    suspend fun addFood(
        delta: Int,
        foodEmoji: String = FoodInventoryCodec.DEFAULT_FOOD_EMOJI,
    ) {
        if (delta == 0) return
        store.addFood(delta, foodEmoji)
    }

    suspend fun setGrowthStage(stageName: String) {
        store.setGrowthStage(stageName)
    }

    suspend fun setFullUntilAtMs(fullUntilAtMs: Long) {
        store.setFullUntilAtMs(fullUntilAtMs.coerceAtLeast(0L))
    }

    suspend fun savePendingRewardEvent(event: PendingRewardEvent) {
        store.savePendingRewardEvent(event)
    }

    suspend fun readPendingRewardEvent(): PendingRewardEvent? = store.readPendingRewardEvent()

    suspend fun clearPendingRewardEvent() {
        store.clearPendingRewardEvent()
    }

    suspend fun feedOneFood(foodEmoji: String? = null) {
        if (!store.consumeOneFood(foodEmoji)) return
        store.setFullUntilAtMs(Long.MAX_VALUE)
        syncGrowthStageFromWallet()
    }

    suspend fun recordEggTap(): Int = store.recordEggTap()

    suspend fun hatchEggFromTaps() {
        store.setEggHatched()
        store.setGrowthStage(GrowthStage.BABY.name)
    }

    /** Grants a milestone accessory (owned, not auto-equipped); returns true if newly unlocked. */
    suspend fun grantProgressionAccessory(itemId: String): Boolean {
        if (AccessoryCatalog.find(itemId) == null) return false
        val owned = inventoryStore.ownedAccessoriesFlow.first()
        if (itemId in owned) return false
        inventoryStore.addOwned(itemId)
        inventoryStore.setPendingAccessoryEquip(itemId)
        return true
    }

    suspend fun grantAccessoryForStationCompleted(
        chapterIndex: Int,
        stationId: Int,
    ): String? {
        val itemId =
            AccessoryProgression.accessoryIdForStationCompleted(
                chapterIndex = chapterIndex,
                stationId = stationId,
            ) ?: return null
        return if (grantProgressionAccessory(itemId)) itemId else null
    }

    /** Equips a pending gift after the child taps it on Dino Home. */
    suspend fun completePendingAccessoryEquip() {
        val pending = inventoryStore.pendingAccessoryEquipFlow.first() ?: return
        val owned = inventoryStore.ownedAccessoriesFlow.first()
        if (pending !in owned) return
        inventoryStore.setEquipped(pending)
        inventoryStore.setPendingAccessoryEquip(null)
    }

    suspend fun equipItem(itemId: String?) {
        if (itemId == null) {
            inventoryStore.setEquipped(null)
            return
        }
        val owned = inventoryStore.ownedAccessoriesFlow.first()
        if (itemId !in owned) return
        inventoryStore.setEquipped(itemId)
    }

    suspend fun syncGrowthStageFromWallet() {
        val wallet = playerWalletFlow.first()
        store.setGrowthStage(wallet.growthStage.name)
    }

    suspend fun markChapter1StationCompleted(stationId: Int) {
        store.markChapter1StationCompleted(stationId.coerceIn(0, 3))
    }

    suspend fun setActiveChapterIndex(chapterIndex: Int) {
        store.setActiveChapterIndex(chapterIndex)
    }

    suspend fun markChapterStationCompleted(
        chapterIndex: Int,
        stationId: Int,
    ) {
        store.markChapterStationCompleted(chapterIndex, stationId.coerceIn(0, 3))
    }

    suspend fun ensureTamagotchiInitialized() {
        store.ensureTamagotchiInitialized()
    }

    suspend fun resetForNewGame() {
        store.resetForNewGame()
        inventoryStore.clearAll()
    }

    internal interface CharacterStore {
        val characterFlow: Flow<DinoCharacter>
        val foodCountFlow: Flow<Int>
        val foodInventoryFlow: Flow<Map<String, Int>>
        val eggHatchedFlow: Flow<Boolean>
        val eggTapCountFlow: Flow<Int>
        val totalFoodEarnedFlow: Flow<Int>
        val growthStageFlow: Flow<String>
        val fullUntilAtMsFlow: Flow<Long>
        val chapter1MaxCompletedStationFlow: Flow<Int>
        val activeChapterIndexFlow: Flow<Int>
        val highestUnlockedChapterIndexFlow: Flow<Int>

        fun chapterMaxCompletedStationFlow(chapterIndex: Int): Flow<Int>

        suspend fun setCharacter(character: DinoCharacter)
        suspend fun setFoodCount(foodCount: Int)
        suspend fun addFood(
            delta: Int,
            foodEmoji: String,
        )

        /** @return false if nothing to consume */
        suspend fun consumeOneFood(foodEmoji: String?): Boolean

        suspend fun recordEggTap(): Int

        suspend fun setEggHatched()

        suspend fun setGrowthStage(stageName: String)
        suspend fun savePendingRewardEvent(event: PendingRewardEvent)
        suspend fun readPendingRewardEvent(): PendingRewardEvent?
        suspend fun clearPendingRewardEvent()
        suspend fun setFullUntilAtMs(fullUntilAtMs: Long)
        suspend fun markChapter1StationCompleted(stationId: Int)
        suspend fun setActiveChapterIndex(chapterIndex: Int)
        suspend fun markChapterStationCompleted(chapterIndex: Int, stationId: Int)
        suspend fun ensureTamagotchiInitialized()
        suspend fun resetForNewGame()
    }

    private class PrefsCharacterStore(private val appContext: Context) : CharacterStore {
        private val characterPrefs: CharacterPrefs = CharacterPrefs(appContext)
        private val foodCountKey = intPreferencesKey("tama_food_count")
        private val foodInventoryKey = stringPreferencesKey("tama_food_inventory_v1")
        private val eggHatchedKey = booleanPreferencesKey("tama_egg_hatched")
        private val eggTapCountKey = intPreferencesKey("tama_egg_tap_count")
        private val totalFoodEarnedKey = intPreferencesKey("tama_total_food_earned")
        private val growthStageKey = stringPreferencesKey("tama_growth_stage")
        private val pendingRewardEventIdKey = stringPreferencesKey("tama_pending_reward_event_id")
        private val pendingRewardApplesKey = intPreferencesKey("tama_pending_reward_apples")
        private val pendingRewardFanfareKey = stringPreferencesKey("tama_pending_reward_fanfare")
        private val pendingRewardVisualCueKey = stringPreferencesKey("tama_pending_reward_visual_cue")
        private val pendingRewardAccessoryKey = stringPreferencesKey("tama_pending_reward_accessory_id")
        private val pendingRewardFoodEmojiKey = stringPreferencesKey("tama_pending_reward_food_emoji")
        private val pendingRewardFoodSingularKey = stringPreferencesKey("tama_pending_reward_food_singular")
        private val pendingRewardFoodPluralKey = stringPreferencesKey("tama_pending_reward_food_plural")
        private val initializedKey = booleanPreferencesKey("tama_initialized")
        private val fullUntilAtMsKey = longPreferencesKey("tama_full_until_at_ms")
        private val chapter1MaxCompletedStationKey = intPreferencesKey("chapter1_max_completed_station")
        private val activeChapterIndexKey = intPreferencesKey("syllabus_active_chapter_index")
        private val highestUnlockedChapterIndexKey = intPreferencesKey("syllabus_highest_unlocked_chapter_index")

        private fun chapterStationKey(chapterIndex: Int) =
            intPreferencesKey("syllabus_chapter_${chapterIndex.coerceIn(0, HebrewSyllabus.chapterCount - 1)}_max_station")

        override val characterFlow: Flow<DinoCharacter> = characterPrefs.characterFlow

        override val foodInventoryFlow: Flow<Map<String, Int>> =
            appContext.dataStore.data.map { prefs ->
                readFoodInventory(prefs)
            }

        override val eggHatchedFlow: Flow<Boolean> =
            appContext.dataStore.data.map { prefs ->
                prefs[eggHatchedKey] == true ||
                    TamagotchiRules.parseGrowthStage(prefs[growthStageKey] ?: GrowthStage.EGG.name) != GrowthStage.EGG
            }

        override val eggTapCountFlow: Flow<Int> =
            appContext.dataStore.data.map { prefs ->
                (prefs[eggTapCountKey] ?: 0).coerceIn(0, TamagotchiRules.EGG_TAPS_TO_HATCH)
            }

        override val foodCountFlow: Flow<Int> =
            foodInventoryFlow.map { FoodInventoryCodec.totalCount(it) }

        override val totalFoodEarnedFlow: Flow<Int> =
            appContext.dataStore.data.map { prefs ->
                (prefs[totalFoodEarnedKey] ?: 0).coerceAtLeast(0)
            }

        override val growthStageFlow: Flow<String> =
            appContext.dataStore.data.map { prefs ->
                prefs[growthStageKey] ?: "EGG"
            }

        override val fullUntilAtMsFlow: Flow<Long> =
            appContext.dataStore.data.map { prefs ->
                (prefs[fullUntilAtMsKey] ?: 0L).coerceAtLeast(0L)
            }

        override val chapter1MaxCompletedStationFlow: Flow<Int> =
            chapterMaxCompletedStationFlow(0)

        override val activeChapterIndexFlow: Flow<Int> =
            appContext.dataStore.data.map { prefs ->
                (prefs[activeChapterIndexKey] ?: 0).coerceIn(0, HebrewSyllabus.chapterCount - 1)
            }

        override val highestUnlockedChapterIndexFlow: Flow<Int> =
            appContext.dataStore.data.map { prefs ->
                val legacy = (prefs[chapter1MaxCompletedStationKey] ?: 0).coerceIn(0, 3)
                val stored = (prefs[highestUnlockedChapterIndexKey] ?: 0).coerceIn(0, HebrewSyllabus.chapterCount - 1)
                val migrated = if (legacy >= 3) maxOf(stored, 1) else stored
                migrated.coerceIn(0, HebrewSyllabus.chapterCount - 1)
            }

        override fun chapterMaxCompletedStationFlow(chapterIndex: Int): Flow<Int> {
            val key = chapterStationKey(chapterIndex)
            return appContext.dataStore.data.map { prefs ->
                val direct = (prefs[key] ?: 0).coerceIn(0, 3)
                if (chapterIndex == 0) {
                    val legacy = (prefs[chapter1MaxCompletedStationKey] ?: 0).coerceIn(0, 3)
                    maxOf(direct, legacy)
                } else {
                    direct
                }
            }
        }

        override suspend fun setCharacter(character: DinoCharacter) {
            characterPrefs.setCharacter(character)
        }

        override suspend fun setFoodCount(foodCount: Int) {
            val count = foodCount.coerceAtLeast(0)
            appContext.dataStore.edit { prefs ->
                if (count == 0) {
                    prefs.remove(foodInventoryKey)
                    prefs[foodCountKey] = 0
                } else {
                    prefs.writeFoodInventory(mapOf(FoodInventoryCodec.DEFAULT_FOOD_EMOJI to count))
                }
            }
        }

        override suspend fun addFood(
            delta: Int,
            foodEmoji: String,
        ) {
            if (delta == 0) return
            appContext.dataStore.edit { prefs ->
                prefs.migrateLegacyFoodCountIfNeeded()
                val inv = readFoodInventory(prefs).toMutableMap()
                val emoji = foodEmoji.ifBlank { FoodInventoryCodec.DEFAULT_FOOD_EMOJI }
                inv[emoji] = ((inv[emoji] ?: 0) + delta).coerceAtLeast(0)
                prefs.writeFoodInventory(inv)
                if (delta > 0) {
                    val earned = (prefs[totalFoodEarnedKey] ?: 0).coerceAtLeast(0)
                    prefs[totalFoodEarnedKey] = (earned + delta).coerceAtLeast(0)
                }
            }
            syncGrowthStageInStore()
        }

        override suspend fun recordEggTap(): Int {
            var count = 0
            appContext.dataStore.edit { prefs ->
                if (prefs[eggHatchedKey] == true) return@edit
                count = ((prefs[eggTapCountKey] ?: 0) + 1).coerceAtMost(TamagotchiRules.EGG_TAPS_TO_HATCH)
                prefs[eggTapCountKey] = count
            }
            return count
        }

        override suspend fun setEggHatched() {
            appContext.dataStore.edit { prefs ->
                prefs[eggHatchedKey] = true
                prefs[eggTapCountKey] = TamagotchiRules.EGG_TAPS_TO_HATCH
            }
        }

        override suspend fun consumeOneFood(foodEmoji: String?): Boolean {
            var consumed = false
            appContext.dataStore.edit { prefs ->
                prefs.migrateLegacyFoodCountIfNeeded()
                val inv = readFoodInventory(prefs).toMutableMap()
                val emoji =
                    foodEmoji?.takeIf { (inv[it] ?: 0) > 0 }
                        ?: inv.entries.firstOrNull { it.value > 0 }?.key
                if (emoji == null) return@edit
                val next = (inv[emoji] ?: 0) - 1
                if (next > 0) {
                    inv[emoji] = next
                } else {
                    inv.remove(emoji)
                }
                prefs.writeFoodInventory(inv)
                consumed = true
            }
            return consumed
        }

        private fun readFoodInventory(prefs: androidx.datastore.preferences.core.Preferences): Map<String, Int> {
            val decoded = FoodInventoryCodec.decode(prefs[foodInventoryKey])
            if (decoded.isNotEmpty()) return decoded
            val legacy = (prefs[foodCountKey] ?: 0).coerceAtLeast(0)
            return if (legacy > 0) mapOf(FoodInventoryCodec.DEFAULT_FOOD_EMOJI to legacy) else emptyMap()
        }

        private fun androidx.datastore.preferences.core.MutablePreferences.writeFoodInventory(
            inv: Map<String, Int>,
        ) {
            val total = FoodInventoryCodec.totalCount(inv)
            this[foodInventoryKey] = FoodInventoryCodec.encode(inv)
            this[foodCountKey] = total
        }

        private fun androidx.datastore.preferences.core.MutablePreferences.migrateLegacyFoodCountIfNeeded() {
            if (!this[foodInventoryKey].isNullOrBlank()) return
            val legacy = (this[foodCountKey] ?: 0).coerceAtLeast(0)
            if (legacy <= 0) return
            writeFoodInventory(mapOf(FoodInventoryCodec.DEFAULT_FOOD_EMOJI to legacy))
        }

        private suspend fun syncGrowthStageInStore() {
            val prefsSnapshot = appContext.dataStore.data.first()
            val food = FoodInventoryCodec.totalCount(readFoodInventory(prefsSnapshot))
            val earned = (prefsSnapshot[totalFoodEarnedKey] ?: 0).coerceAtLeast(0)
            val stageName = prefsSnapshot[growthStageKey] ?: GrowthStage.EGG.name
            val wallet =
                TamagotchiRules.buildWallet(
                    applesCount = food,
                    totalEarned = earned,
                    storedStageName = stageName,
                    fullUntilAtMs = (prefsSnapshot[fullUntilAtMsKey] ?: 0L).coerceAtLeast(0L),
                    eggHatched = prefsSnapshot[eggHatchedKey] == true ||
                        TamagotchiRules.parseGrowthStage(stageName) != GrowthStage.EGG,
                    eggTapCount = (prefsSnapshot[eggTapCountKey] ?: 0).coerceAtLeast(0),
                )
            appContext.dataStore.edit { it[growthStageKey] = wallet.growthStage.name }
        }

        override suspend fun setGrowthStage(stageName: String) {
            appContext.dataStore.edit { it[growthStageKey] = stageName }
        }

        override suspend fun savePendingRewardEvent(event: PendingRewardEvent) {
            appContext.dataStore.edit { prefs ->
                prefs[pendingRewardEventIdKey] = event.eventId
                prefs[pendingRewardApplesKey] = event.applesCount.coerceAtLeast(0)
                prefs[pendingRewardFanfareKey] = event.fanfareText
                if (event.visualCue != null) {
                    prefs[pendingRewardVisualCueKey] = event.visualCue.name
                } else {
                    prefs.remove(pendingRewardVisualCueKey)
                }
                if (!event.accessoryUnlockId.isNullOrBlank()) {
                    prefs[pendingRewardAccessoryKey] = event.accessoryUnlockId
                } else {
                    prefs.remove(pendingRewardAccessoryKey)
                }
                prefs[pendingRewardFoodEmojiKey] = event.foodEmoji
                prefs[pendingRewardFoodSingularKey] = event.foodNameSingularHe
                prefs[pendingRewardFoodPluralKey] = event.foodNamePluralHe
            }
        }

        override suspend fun readPendingRewardEvent(): PendingRewardEvent? {
            val prefs = appContext.dataStore.data.first()
            val legacyDelta = prefs[intPreferencesKey("tama_pending_reward_food_delta")] ?: 0
            if (legacyDelta > 0 && prefs[pendingRewardEventIdKey] == null) {
                val migrated =
                    PendingRewardEvent(
                        eventId = "legacy_${System.currentTimeMillis()}",
                        applesCount = legacyDelta.coerceAtLeast(0),
                        fanfareText = com.tal.hebrewdino.ui.domain.economy.fanfareTextForApples(legacyDelta),
                        visualCue = null,
                    )
                savePendingRewardEvent(migrated)
                return migrated
            }
            val id = prefs[pendingRewardEventIdKey] ?: return null
            val apples = (prefs[pendingRewardApplesKey] ?: 0).coerceAtLeast(0)
            if (apples <= 0) return null
            val fanfare = prefs[pendingRewardFanfareKey] ?: return null
            val cueName = prefs[pendingRewardVisualCueKey]
            val cue =
                cueName?.let { name ->
                    try {
                        ParticleCue.valueOf(name)
                    } catch (_: Throwable) {
                        null
                    }
                }
            return PendingRewardEvent(
                eventId = id,
                applesCount = apples,
                fanfareText = fanfare,
                visualCue = cue,
                accessoryUnlockId = prefs[pendingRewardAccessoryKey]?.takeIf { it.isNotBlank() },
                foodEmoji = prefs[pendingRewardFoodEmojiKey] ?: "🍎",
                foodNameSingularHe = prefs[pendingRewardFoodSingularKey] ?: "תפוח",
                foodNamePluralHe = prefs[pendingRewardFoodPluralKey] ?: "תפוחים",
            )
        }

        override suspend fun clearPendingRewardEvent() {
            appContext.dataStore.edit { prefs ->
                prefs.remove(pendingRewardEventIdKey)
                prefs.remove(pendingRewardApplesKey)
                prefs.remove(pendingRewardFanfareKey)
                prefs.remove(pendingRewardVisualCueKey)
                prefs.remove(pendingRewardAccessoryKey)
                prefs.remove(pendingRewardFoodEmojiKey)
                prefs.remove(pendingRewardFoodSingularKey)
                prefs.remove(pendingRewardFoodPluralKey)
            }
        }

        override suspend fun setFullUntilAtMs(fullUntilAtMs: Long) {
            appContext.dataStore.edit { it[fullUntilAtMsKey] = fullUntilAtMs.coerceAtLeast(0L) }
        }

        override suspend fun markChapter1StationCompleted(stationId: Int) {
            markChapterStationCompleted(chapterIndex = 0, stationId = stationId)
        }

        override suspend fun setActiveChapterIndex(chapterIndex: Int) {
            val clamped = chapterIndex.coerceIn(0, HebrewSyllabus.chapterCount - 1)
            appContext.dataStore.edit { prefs ->
                prefs[activeChapterIndexKey] = clamped
            }
        }

        override suspend fun markChapterStationCompleted(
            chapterIndex: Int,
            stationId: Int,
        ) {
            val chapter = chapterIndex.coerceIn(0, HebrewSyllabus.chapterCount - 1)
            val clamped = stationId.coerceIn(0, 3)
            if (clamped <= 0) return
            appContext.dataStore.edit { prefs ->
                val key = chapterStationKey(chapter)
                val current = (prefs[key] ?: 0).coerceIn(0, 3)
                prefs[key] = maxOf(current, clamped)
                if (chapter == 0) {
                    val legacy = (prefs[chapter1MaxCompletedStationKey] ?: 0).coerceIn(0, 3)
                    prefs[chapter1MaxCompletedStationKey] = maxOf(legacy, clamped)
                }
                if (clamped >= 3) {
                    val unlocked = (prefs[highestUnlockedChapterIndexKey] ?: 0).coerceIn(0, HebrewSyllabus.chapterCount - 1)
                    val next = (chapter + 1).coerceAtMost(HebrewSyllabus.chapterCount - 1)
                    prefs[highestUnlockedChapterIndexKey] = maxOf(unlocked, next)
                    if (next > chapter) {
                        prefs[activeChapterIndexKey] = next
                    }
                }
            }
        }

        override suspend fun ensureTamagotchiInitialized() {
            appContext.dataStore.edit { prefs ->
                val initialized = prefs[initializedKey] ?: false
                if (initialized) return@edit
                val current = (prefs[foodCountKey] ?: 0).coerceAtLeast(0)
                prefs[foodCountKey] = maxOf(current, 0)
                if (prefs[growthStageKey].isNullOrBlank()) {
                    prefs[growthStageKey] = "EGG"
                }
                val earned = (prefs[totalFoodEarnedKey] ?: 0).coerceAtLeast(0)
                prefs[totalFoodEarnedKey] = maxOf(earned, 0)
                prefs[fullUntilAtMsKey] = (prefs[fullUntilAtMsKey] ?: 0L).coerceAtLeast(0L)
                prefs[chapter1MaxCompletedStationKey] = (prefs[chapter1MaxCompletedStationKey] ?: 0).coerceIn(0, 3)
                prefs[activeChapterIndexKey] = (prefs[activeChapterIndexKey] ?: 0).coerceIn(0, HebrewSyllabus.chapterCount - 1)
                prefs[highestUnlockedChapterIndexKey] =
                    (prefs[highestUnlockedChapterIndexKey] ?: 0).coerceIn(0, HebrewSyllabus.chapterCount - 1)
                HebrewSyllabus.chapters.indices.forEach { ch ->
                    val key = chapterStationKey(ch)
                    prefs[key] = (prefs[key] ?: 0).coerceIn(0, 3)
                }
                prefs[initializedKey] = true
            }
        }

        override suspend fun resetForNewGame() {
            appContext.dataStore.edit { prefs ->
                prefs[foodCountKey] = 0
                prefs.remove(foodInventoryKey)
                prefs[eggHatchedKey] = false
                prefs[eggTapCountKey] = 0
                prefs[totalFoodEarnedKey] = 0
                prefs[growthStageKey] = "EGG"
                prefs.remove(pendingRewardEventIdKey)
                prefs.remove(pendingRewardApplesKey)
                prefs.remove(pendingRewardFanfareKey)
                prefs.remove(pendingRewardVisualCueKey)
                prefs.remove(pendingRewardAccessoryKey)
                prefs.remove(pendingRewardFoodEmojiKey)
                prefs.remove(pendingRewardFoodSingularKey)
                prefs.remove(pendingRewardFoodPluralKey)
                prefs[fullUntilAtMsKey] = 0L
                prefs[chapter1MaxCompletedStationKey] = 0
                prefs[activeChapterIndexKey] = 0
                prefs[highestUnlockedChapterIndexKey] = 0
                HebrewSyllabus.chapters.indices.forEach { ch ->
                    prefs[chapterStationKey(ch)] = 0
                }
                prefs[initializedKey] = true
            }
        }
    }

    companion object {
        @Volatile
        internal var storeFactory: (Context) -> CharacterStore =
            { appContext -> PrefsCharacterStore(appContext) }
    }
}

private data class WalletSnapshot(
    val food: Int,
    val totalEarned: Int,
    val stageName: String,
    val fullUntil: Long,
    val eggHatched: Boolean,
)
