package com.tal.hebrewdino.ui.screens

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tal.hebrewdino.ui.audio.IntroMediaClips
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.data.CharacterRepository
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.data.StoryNarrationGate
import com.tal.hebrewdino.ui.data.StoryNarrationPrefs
import com.tal.hebrewdino.ui.domain.economy.TamagotchiRules
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class IntroHatchPhase {
    IDLE,
    CRACKING,
    DONE,
}

class IntroInstructionViewModel(
    private val repo: CharacterRepository,
    private val storyPrefs: StoryNarrationGate,
    rawVoicePlayer: RawVoicePlayer,
) : ViewModel() {
    private val rawVoice = rawVoicePlayer

    val character: StateFlow<DinoCharacter> =
        repo.characterFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DinoCharacter.DINO_GREEN,
        )

    val eggTapCount: StateFlow<Int> =
        repo.eggTapCountFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = 0,
        )

    private val _skipToHome = MutableStateFlow(false)
    val skipToHome: StateFlow<Boolean> = _skipToHome.asStateFlow()

    var eggTapEpoch: Int by mutableIntStateOf(0)
        private set

    var hatchPhase: IntroHatchPhase by mutableStateOf(IntroHatchPhase.IDLE)
        private set

    var isReady: Boolean by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            repo.ensureTamagotchiInitialized()
            if (repo.eggHatchedFlow.first()) {
                isReady = true
                _skipToHome.value = true
                return@launch
            }
            isReady = true
            playIntroNarration()
        }
    }

    private suspend fun playIntroNarration() {
        rawVoice.playSequenceBlocking(
            IntroMediaClips.storyResId,
            IntroMediaClips.instructionsResId,
        )
        storyPrefs.setEggKnockPromptSpoken()
    }

    fun onEggTapped() {
        if (hatchPhase != IntroHatchPhase.IDLE) return
        viewModelScope.launch {
            eggTapEpoch += 1
            val taps = repo.recordEggTap()
            if (taps >= TamagotchiRules.EGG_TAPS_TO_HATCH) {
                hatchPhase = IntroHatchPhase.CRACKING
            }
        }
    }

    suspend fun finalizeHatch() {
        if (hatchPhase == IntroHatchPhase.DONE) return
        repo.hatchEggFromTaps()
        hatchPhase = IntroHatchPhase.DONE
    }

    override fun onCleared() {
        rawVoice.release()
        super.onCleared()
    }

    class Factory(
        private val repo: CharacterRepository,
        private val storyPrefs: StoryNarrationGate,
        private val rawVoicePlayer: RawVoicePlayer,
    ) : ViewModelProvider.Factory {
        constructor(context: Context) : this(
            CharacterRepository(context.applicationContext),
            StoryNarrationPrefs(context.applicationContext),
            RawVoicePlayer(context.applicationContext),
        )

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(IntroInstructionViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return IntroInstructionViewModel(repo, storyPrefs, rawVoicePlayer) as T
            }
            error("Unknown ViewModel class: $modelClass")
        }
    }
}
