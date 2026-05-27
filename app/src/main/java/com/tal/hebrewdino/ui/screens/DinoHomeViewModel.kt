package com.tal.hebrewdino.ui.screens

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tal.hebrewdino.ui.data.CharacterRepository
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.data.StoryNarrationGate
import com.tal.hebrewdino.ui.data.StoryNarrationPrefs
import com.tal.hebrewdino.ui.domain.DinoStoryScripts
import com.tal.hebrewdino.ui.domain.applyChildFriendlyTtsWorkarounds
import com.tal.hebrewdino.ui.domain.economy.FoodInventoryCodec
import com.tal.hebrewdino.ui.domain.economy.FoodInventoryEntry
import com.tal.hebrewdino.ui.domain.economy.GrowthStage
import com.tal.hebrewdino.ui.domain.economy.PlayerWallet
import com.tal.hebrewdino.ui.domain.economy.TamagotchiRules
import com.tal.hebrewdino.ui.domain.hungryDinoPromptSpokenForTts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private enum class PendingStoryNarration {
    EGG_KNOCK_PROMPT,
    POST_HATCH_INTRO,
    PART3_FIRST_ACCESSORY,
}

class DinoHomeViewModel(
    private val repo: CharacterRepository,
    private val storyPrefs: StoryNarrationGate,
) : ViewModel() {
    val character: StateFlow<DinoCharacter> =
        repo.characterFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DinoCharacter.DINO_GREEN,
        )

    val wallet: StateFlow<PlayerWallet?> =
        repo.playerWalletFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = null,
            )

    val foodInventory: StateFlow<List<FoodInventoryEntry>> =
        repo.foodInventoryFlow
            .map { FoodInventoryCodec.entriesForDisplay(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList(),
            )

    val equippedAccessory: StateFlow<String?> =
        repo.equippedAccessoryFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
        )

    val pendingAccessoryEquip: StateFlow<String?> =
        repo.pendingAccessoryEquipFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
        )

    val ownedAccessories: StateFlow<Set<String>> =
        repo.ownedAccessoriesFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet(),
        )

    private val missionPulseAfterHatch = MutableStateFlow(false)

    val eggTapCount: StateFlow<Int> =
        repo.eggTapCountFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = 0,
        )

    /** High-contrast pulse on "צא למשימה" after hatch narration or when no side gift. */
    val highlightMissionButton: StateFlow<Boolean> =
        combine(
            pendingAccessoryEquip,
            missionPulseAfterHatch,
        ) { pending, pulseAfterHatch ->
            pulseAfterHatch || pending.isNullOrBlank()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true,
        )

    var isDataReady: Boolean by mutableStateOf(false)
        private set

    var hatchEpoch: Int by mutableIntStateOf(0)
        private set

    var growEpoch: Int by mutableIntStateOf(0)
        private set

    var eggTapEpoch: Int by mutableIntStateOf(0)
        private set

    var confettiTrigger: Int by mutableIntStateOf(0)
        private set

    /** Bumps when a queued story line should be spoken on Dino Home. */
    var homeSpeechEpoch: Int by mutableIntStateOf(0)
        private set

    /** Bumps when periodic home reminders should restart (e.g. after story narration). */
    var homeReminderEpoch: Int by mutableIntStateOf(0)
        private set

    var suppressPeriodicReminders: Boolean by mutableStateOf(false)
        private set

    private var pendingStory: PendingStoryNarration? = null

    init {
        viewModelScope.launch {
            repo.ensureTamagotchiInitialized()
            var previousStage: GrowthStage? = null
            repo.playerWalletFlow.collect { playerWallet ->
                val stage = playerWallet.growthStage
                val prior = previousStage
                if (prior != null && prior != stage) {
                    if (prior == GrowthStage.BABY && stage == GrowthStage.ADULT) {
                        growEpoch += 1
                    }
                    if (stage.ordinal > prior.ordinal && prior != GrowthStage.EGG) {
                        repo.setGrowthStage(stage.name)
                    }
                }
                previousStage = stage
                if (!isDataReady) {
                    isDataReady = true
                    queueEggKnockPromptIfNeeded(stage)
                }
            }
        }
        viewModelScope.launch {
            var readyForUnlockCelebration = false
            var lastOwnedCount = 0
            repo.ownedAccessoriesFlow.collect { owned ->
                if (!readyForUnlockCelebration) {
                    lastOwnedCount = owned.size
                    if (isDataReady) readyForUnlockCelebration = true
                    return@collect
                }
                if (owned.size > lastOwnedCount) {
                    confettiTrigger += 1
                    queueFirstAccessoryStoryIfNeeded()
                }
                lastOwnedCount = owned.size
            }
        }
    }

    private fun queueEggKnockPromptIfNeeded(stage: GrowthStage) {
        if (stage != GrowthStage.EGG) return
        viewModelScope.launch {
            if (!storyPrefs.isEggKnockPromptSpoken()) {
                pendingStory = PendingStoryNarration.EGG_KNOCK_PROMPT
                suppressPeriodicReminders = true
                homeSpeechEpoch += 1
            }
        }
    }

    private fun queuePostHatchIntroIfNeeded() {
        viewModelScope.launch {
            if (!storyPrefs.isPart2Spoken()) {
                pendingStory = PendingStoryNarration.POST_HATCH_INTRO
                suppressPeriodicReminders = true
                homeSpeechEpoch += 1
            }
        }
    }

    private fun queueFirstAccessoryStoryIfNeeded() {
        viewModelScope.launch {
            if (repo.pendingAccessoryEquipFlow.first() != null) return@launch
            if (!storyPrefs.isPart3Spoken()) {
                pendingStory = PendingStoryNarration.PART3_FIRST_ACCESSORY
                suppressPeriodicReminders = true
                homeSpeechEpoch += 1
            }
        }
    }

    fun completePendingAccessoryEquip() {
        viewModelScope.launch {
            repo.completePendingAccessoryEquip()
        }
    }

    fun equipOwnedAccessory(itemId: String) {
        viewModelScope.launch {
            if (repo.pendingAccessoryEquipFlow.first() == itemId) return@launch
            repo.equipItem(itemId)
        }
    }

    fun feedOnce(foodEmoji: String) {
        viewModelScope.launch {
            repo.feedOneFood(foodEmoji)
        }
    }

    fun onEggTapped() {
        val wallet = wallet.value ?: return
        if (wallet.growthStage != GrowthStage.EGG) return
        eggTapEpoch += 1
        viewModelScope.launch {
            val taps = repo.recordEggTap()
            if (taps >= TamagotchiRules.EGG_TAPS_TO_HATCH) {
                repo.hatchEggFromTaps()
                hatchEpoch += 1
                queuePostHatchIntroIfNeeded()
            }
        }
    }

    fun shouldPlayBabyChirpBeforeEntrySpeech(): Boolean = pendingStory == PendingStoryNarration.POST_HATCH_INTRO

    fun homePromptText(wallet: PlayerWallet): String {
        if (wallet.isHungry) {
            return "דינו רעב! תאכילו אותו בתפוחים."
        }
        return when (wallet.growthStage) {
            GrowthStage.EGG -> "הקישו על הביצה — טוק, טוק, טוק!"
            GrowthStage.BABY -> {
                val remaining = (TamagotchiRules.APPLES_TO_ADULT - wallet.fedTotal).coerceAtLeast(0)
                if (remaining > 0) {
                    "עוד ${appleCountMasculinePhrase(remaining)} ודינו יגדל!"
                } else {
                    "דינו מלא ושמח!"
                }
            }
            GrowthStage.ADULT ->
                "דינו בוגר! כל הכבוד — המשיכו להאכיל ולצאת למשימות!"
        }
    }

    fun entryHomeSpokenForTts(wallet: PlayerWallet): String {
        when (pendingStory) {
            PendingStoryNarration.EGG_KNOCK_PROMPT -> return DinoStoryScripts.eggKnockPromptSpokenForTts()
            PendingStoryNarration.POST_HATCH_INTRO -> return DinoStoryScripts.postHatchIntroSpokenForTts()
            PendingStoryNarration.PART3_FIRST_ACCESSORY -> return DinoStoryScripts.part3FirstAccessorySpokenForTts()
            null -> Unit
        }
        return periodicHomeReminderSpokenForTts(wallet)
    }

    fun periodicHomeReminderText(wallet: PlayerWallet): String =
        if (wallet.applesCount > 0) {
            "לחצו על האוכל כדי להאכיל את דינו!"
        } else {
            "צאו למשימה כדי לקבל אוכל בשביל דינו!"
        }

    fun periodicHomeReminderSpokenForTts(wallet: PlayerWallet): String =
        applyChildFriendlyTtsWorkarounds(periodicHomeReminderText(wallet))

    fun homePromptSpokenForTts(wallet: PlayerWallet): String {
        if (wallet.isHungry) return hungryDinoPromptSpokenForTts()
        return applyChildFriendlyTtsWorkarounds(homePromptText(wallet))
    }

    companion object {
        const val HOME_REMINDER_INTERVAL_MS: Long = 10_000L
    }

    fun onHomeSpeechPlayed() {
        val played = pendingStory ?: return
        pendingStory = null
        suppressPeriodicReminders = false
        homeReminderEpoch += 1
        if (played == PendingStoryNarration.POST_HATCH_INTRO) {
            missionPulseAfterHatch.value = true
        }
        viewModelScope.launch {
            when (played) {
                PendingStoryNarration.EGG_KNOCK_PROMPT -> storyPrefs.setEggKnockPromptSpoken()
                PendingStoryNarration.POST_HATCH_INTRO -> storyPrefs.setPart2Spoken()
                PendingStoryNarration.PART3_FIRST_ACCESSORY -> storyPrefs.setPart3Spoken()
            }
        }
    }

    class Factory(
        private val repo: CharacterRepository,
        private val storyPrefs: StoryNarrationGate,
    ) : ViewModelProvider.Factory {
        constructor(context: Context) : this(
            CharacterRepository(context.applicationContext),
            StoryNarrationPrefs(context.applicationContext),
        )

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DinoHomeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DinoHomeViewModel(repo, storyPrefs) as T
            }
            error("Unknown ViewModel class: $modelClass")
        }
    }
}

internal fun appleCountMasculinePhrase(count: Int): String =
    when (count) {
        1 -> "תפוח אחד"
        2 -> "שני תפוחים"
        3 -> "שלושה תפוחים"
        4 -> "ארבעה תפוחים"
        5 -> "חמישה תפוחים"
        6 -> "שישה תפוחים"
        7 -> "שבעה תפוחים"
        8 -> "שמונה תפוחים"
        9 -> "תשעה תפוחים"
        10 -> "עשרה תפוחים"
        else -> "$count תפוחים"
    }
