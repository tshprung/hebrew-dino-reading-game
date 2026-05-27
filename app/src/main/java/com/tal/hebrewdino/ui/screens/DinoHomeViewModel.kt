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
import com.tal.hebrewdino.ui.domain.economy.GrowthStage
import com.tal.hebrewdino.ui.domain.economy.PlayerWallet
import com.tal.hebrewdino.ui.domain.economy.TamagotchiRules
import com.tal.hebrewdino.ui.domain.hungryDinoPromptSpokenForTts
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private enum class PendingStoryNarration {
    PART2_BABY_HATCH,
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

    val equippedAccessory: StateFlow<String?> =
        repo.equippedAccessoryFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
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

    private var pendingStory: PendingStoryNarration? = null

    init {
        viewModelScope.launch {
            repo.ensureTamagotchiInitialized()
            var previousStage: GrowthStage? = null
            repo.playerWalletFlow.collect { playerWallet ->
                val stage = playerWallet.growthStage
                val prior = previousStage
                if (prior != null && prior != stage) {
                    if (prior == GrowthStage.EGG && stage == GrowthStage.BABY) {
                        hatchEpoch += 1
                        queueBabyHatchStoryIfNeeded()
                    }
                    if (prior == GrowthStage.BABY && stage == GrowthStage.ADULT) {
                        growEpoch += 1
                    }
                    if (stage.ordinal > prior.ordinal) {
                        repo.setGrowthStage(stage.name)
                    }
                }
                previousStage = stage
                if (!isDataReady) isDataReady = true
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

    private fun queueBabyHatchStoryIfNeeded() {
        viewModelScope.launch {
            if (!storyPrefs.isPart2Spoken()) {
                pendingStory = PendingStoryNarration.PART2_BABY_HATCH
                homeSpeechEpoch += 1
            }
        }
    }

    private fun queueFirstAccessoryStoryIfNeeded() {
        viewModelScope.launch {
            if (!storyPrefs.isPart3Spoken()) {
                pendingStory = PendingStoryNarration.PART3_FIRST_ACCESSORY
                homeSpeechEpoch += 1
            }
        }
    }

    fun feedOnce() {
        viewModelScope.launch {
            repo.feedOneApple()
        }
    }

    fun onEggTapped() {
        eggTapEpoch += 1
    }

    fun homePromptText(wallet: PlayerWallet): String {
        if (wallet.isHungry) {
            return "דינו רעב! תאכילו אותו בתפוחים."
        }
        return when (wallet.growthStage) {
            GrowthStage.EGG -> {
                val remaining = (TamagotchiRules.APPLES_TO_HATCH - wallet.fedTotal).coerceAtLeast(0)
                if (remaining > 0) {
                    "עוד ${appleCountMasculinePhrase(remaining)} והביצה תבקע!"
                } else {
                    "דינו מלא ושמח!"
                }
            }
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

    fun homePromptSpokenForTts(wallet: PlayerWallet): String {
        when (pendingStory) {
            PendingStoryNarration.PART2_BABY_HATCH -> return DinoStoryScripts.part2BabyHatchSpokenForTts()
            PendingStoryNarration.PART3_FIRST_ACCESSORY -> return DinoStoryScripts.part3FirstAccessorySpokenForTts()
            null -> Unit
        }
        if (wallet.isHungry) return hungryDinoPromptSpokenForTts()
        return applyChildFriendlyTtsWorkarounds(homePromptText(wallet))
    }

    fun onHomeSpeechPlayed() {
        val played = pendingStory ?: return
        pendingStory = null
        viewModelScope.launch {
            when (played) {
                PendingStoryNarration.PART2_BABY_HATCH -> storyPrefs.setPart2Spoken()
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
        else -> "$count תפוחים"
    }
