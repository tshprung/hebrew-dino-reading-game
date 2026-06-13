package com.tal.hebrewdino.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tal.hebrewdino.ui.data.CharacterPrefs
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.data.PlayerAddress
import com.tal.hebrewdino.ui.data.ProgressPrefs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class MainUiState(
    val beachOutroSeen: Boolean = false,
    val chapter1MidBoostSeen: Boolean = false,
    val chapter2MidBoostSeen: Boolean = false,
    val chapter2UnlockedStation: Int = 1,
    val chapter2CompletedStations: Set<Int> = emptySet(),
    val chapter2Completed: Boolean = false,
    val chapter3MidBoostSeen: Boolean = false,
    val chapter3UnlockedStation: Int = 1,
    val chapter3CompletedStations: Set<Int> = emptySet(),
    val chapter3Completed: Boolean = false,
    val chapter4IntroSeen: Boolean = false,
    val chapter4LettersIntroSeen: Boolean = false,
    val chapter4MidBoostSeen: Boolean = false,
    val chapter4UnlockedStation: Int = 1,
    val chapter4CompletedStations: Set<Int> = emptySet(),
    val chapter4Completed: Boolean = false,
    val chapter5IntroSeen: Boolean = false,
    val chapter5LettersIntroSeen: Boolean = false,
    val chapter5MidBoostSeen: Boolean = false,
    val chapter5UnlockedStation: Int = 1,
    val chapter5CompletedStations: Set<Int> = emptySet(),
    val chapter5Completed: Boolean = false,
    val chapter6IntroSeen: Boolean = false,
    val chapter6LettersIntroSeen: Boolean = false,
    val chapter6MidBoostSeen: Boolean = false,
    val chapter6UnlockedStation: Int = 1,
    val chapter6CompletedStations: Set<Int> = emptySet(),
    val chapter6Completed: Boolean = false,
    val unlockedLevel: Int = 1,
    val completedLevels: Set<Int> = emptySet(),
    val season1ChapterUnlockWaivers: Set<Int> = emptySet(),
    val character: DinoCharacter? = null,
    val playerAddress: PlayerAddress? = null,
)

private data class Chapter1ProgressState(
    val beachOutroSeen: Boolean,
    val chapter1MidBoostSeen: Boolean,
    val unlockedLevel: Int,
    val completedLevels: Set<Int>,
)

private data class Chapter2ProgressState(
    val chapter2MidBoostSeen: Boolean,
    val chapter2UnlockedStation: Int,
    val chapter2CompletedStations: Set<Int>,
    val chapter2Completed: Boolean,
)

private data class Chapter3ProgressState(
    val chapter3MidBoostSeen: Boolean,
    val chapter3UnlockedStation: Int,
    val chapter3CompletedStations: Set<Int>,
    val chapter3Completed: Boolean,
)

private data class Chapter4ProgressState(
    val chapter4IntroSeen: Boolean,
    val chapter4LettersIntroSeen: Boolean,
    val chapter4MidBoostSeen: Boolean,
    val chapter4UnlockedStation: Int,
    val chapter4CompletedStations: Set<Int>,
    val chapter4Completed: Boolean,
)

private data class Chapter5ProgressState(
    val chapter5IntroSeen: Boolean,
    val chapter5LettersIntroSeen: Boolean,
    val chapter5MidBoostSeen: Boolean,
    val chapter5UnlockedStation: Int,
    val chapter5CompletedStations: Set<Int>,
    val chapter5Completed: Boolean,
)

private data class Chapter6ProgressState(
    val chapter6IntroSeen: Boolean,
    val chapter6LettersIntroSeen: Boolean,
    val chapter6MidBoostSeen: Boolean,
    val chapter6UnlockedStation: Int,
    val chapter6CompletedStations: Set<Int>,
    val chapter6Completed: Boolean,
)

private data class Chapters2To5State(
    val chapter2: Chapter2ProgressState,
    val chapter3: Chapter3ProgressState,
    val chapter4: Chapter4ProgressState,
    val chapter5: Chapter5ProgressState,
)

class MainViewModel(context: Context) : ViewModel() {
    private val appContext = context.applicationContext

    val prefs: CharacterPrefs = CharacterPrefs(appContext)
    val progress: ProgressPrefs = ProgressPrefs(appContext)

    private val chapter1State: StateFlow<Chapter1ProgressState> =
        combine(
            progress.beachOutroSeenFlow,
            progress.chapter1MidBoostSeenFlow,
            progress.unlockedLevelFlow,
            progress.completedLevelsFlow,
        ) { beachOutroSeen, chapter1MidBoostSeen, unlockedLevel, completedLevels ->
            Chapter1ProgressState(
                beachOutroSeen = beachOutroSeen,
                chapter1MidBoostSeen = chapter1MidBoostSeen,
                unlockedLevel = unlockedLevel,
                completedLevels = completedLevels,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue =
                Chapter1ProgressState(
                    beachOutroSeen = false,
                    chapter1MidBoostSeen = false,
                    unlockedLevel = 1,
                    completedLevels = emptySet(),
                ),
        )

    private val chapter2State: StateFlow<Chapter2ProgressState> =
        combine(
            progress.chapter2MidBoostSeenFlow,
            progress.chapter2UnlockedStationFlow,
            progress.chapter2CompletedStationsFlow,
            progress.chapter2CompletedFlow,
        ) { chapter2MidBoostSeen, chapter2UnlockedStation, chapter2CompletedStations, chapter2Completed ->
            Chapter2ProgressState(
                chapter2MidBoostSeen = chapter2MidBoostSeen,
                chapter2UnlockedStation = chapter2UnlockedStation,
                chapter2CompletedStations = chapter2CompletedStations,
                chapter2Completed = chapter2Completed,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue =
                Chapter2ProgressState(
                    chapter2MidBoostSeen = false,
                    chapter2UnlockedStation = 1,
                    chapter2CompletedStations = emptySet(),
                    chapter2Completed = false,
                ),
        )

    private val chapter3State: StateFlow<Chapter3ProgressState> =
        combine(
            progress.chapter3MidBoostSeenFlow,
            progress.chapter3UnlockedStationFlow,
            progress.chapter3CompletedStationsFlow,
            progress.chapter3CompletedFlow,
        ) { chapter3MidBoostSeen, chapter3UnlockedStation, chapter3CompletedStations, chapter3Completed ->
            Chapter3ProgressState(
                chapter3MidBoostSeen = chapter3MidBoostSeen,
                chapter3UnlockedStation = chapter3UnlockedStation,
                chapter3CompletedStations = chapter3CompletedStations,
                chapter3Completed = chapter3Completed,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue =
                Chapter3ProgressState(
                    chapter3MidBoostSeen = false,
                    chapter3UnlockedStation = 1,
                    chapter3CompletedStations = emptySet(),
                    chapter3Completed = false,
                ),
        )

    private val chapter4State: StateFlow<Chapter4ProgressState> =
        combine(
            combine(
                progress.chapter4IntroSeenFlow,
                progress.chapter4LettersIntroSeenFlow,
                progress.chapter4MidBoostSeenFlow,
            ) { chapter4IntroSeen, chapter4LettersIntroSeen, chapter4MidBoostSeen ->
                Triple(chapter4IntroSeen, chapter4LettersIntroSeen, chapter4MidBoostSeen)
            },
            combine(
                progress.chapter4UnlockedStationFlow,
                progress.chapter4CompletedStationsFlow,
                progress.chapter4CompletedFlow,
            ) { chapter4UnlockedStation, chapter4CompletedStations, chapter4Completed ->
                Triple(chapter4UnlockedStation, chapter4CompletedStations, chapter4Completed)
            },
        ) { chapter4Flags, chapter4Progress ->
            Chapter4ProgressState(
                chapter4IntroSeen = chapter4Flags.first,
                chapter4LettersIntroSeen = chapter4Flags.second,
                chapter4MidBoostSeen = chapter4Flags.third,
                chapter4UnlockedStation = chapter4Progress.first,
                chapter4CompletedStations = chapter4Progress.second,
                chapter4Completed = chapter4Progress.third,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue =
                Chapter4ProgressState(
                    chapter4IntroSeen = false,
                    chapter4LettersIntroSeen = false,
                    chapter4MidBoostSeen = false,
                    chapter4UnlockedStation = 1,
                    chapter4CompletedStations = emptySet(),
                    chapter4Completed = false,
                ),
        )

    private val chapter5State: StateFlow<Chapter5ProgressState> =
        combine(
            combine(
                progress.chapter5IntroSeenFlow,
                progress.chapter5LettersIntroSeenFlow,
                progress.chapter5MidBoostSeenFlow,
            ) { chapter5IntroSeen, chapter5LettersIntroSeen, chapter5MidBoostSeen ->
                Triple(chapter5IntroSeen, chapter5LettersIntroSeen, chapter5MidBoostSeen)
            },
            combine(
                progress.chapter5UnlockedStationFlow,
                progress.chapter5CompletedStationsFlow,
                progress.chapter5CompletedFlow,
            ) { chapter5UnlockedStation, chapter5CompletedStations, chapter5Completed ->
                Triple(chapter5UnlockedStation, chapter5CompletedStations, chapter5Completed)
            },
        ) { chapter5Flags, chapter5Progress ->
            Chapter5ProgressState(
                chapter5IntroSeen = chapter5Flags.first,
                chapter5LettersIntroSeen = chapter5Flags.second,
                chapter5MidBoostSeen = chapter5Flags.third,
                chapter5UnlockedStation = chapter5Progress.first,
                chapter5CompletedStations = chapter5Progress.second,
                chapter5Completed = chapter5Progress.third,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue =
                Chapter5ProgressState(
                    chapter5IntroSeen = false,
                    chapter5LettersIntroSeen = false,
                    chapter5MidBoostSeen = false,
                    chapter5UnlockedStation = 1,
                    chapter5CompletedStations = emptySet(),
                    chapter5Completed = false,
                ),
        )

    private val chapter6State: StateFlow<Chapter6ProgressState> =
        combine(
            combine(
                progress.chapter6IntroSeenFlow,
                progress.chapter6LettersIntroSeenFlow,
                progress.chapter6MidBoostSeenFlow,
            ) { chapter6IntroSeen, chapter6LettersIntroSeen, chapter6MidBoostSeen ->
                Triple(chapter6IntroSeen, chapter6LettersIntroSeen, chapter6MidBoostSeen)
            },
            combine(
                progress.chapter6UnlockedStationFlow,
                progress.chapter6CompletedStationsFlow,
                progress.chapter6CompletedFlow,
            ) { chapter6UnlockedStation, chapter6CompletedStations, chapter6Completed ->
                Triple(chapter6UnlockedStation, chapter6CompletedStations, chapter6Completed)
            },
        ) { chapter6Flags, chapter6Progress ->
            Chapter6ProgressState(
                chapter6IntroSeen = chapter6Flags.first,
                chapter6LettersIntroSeen = chapter6Flags.second,
                chapter6MidBoostSeen = chapter6Flags.third,
                chapter6UnlockedStation = chapter6Progress.first,
                chapter6CompletedStations = chapter6Progress.second,
                chapter6Completed = chapter6Progress.third,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue =
                Chapter6ProgressState(
                    chapter6IntroSeen = false,
                    chapter6LettersIntroSeen = false,
                    chapter6MidBoostSeen = false,
                    chapter6UnlockedStation = 1,
                    chapter6CompletedStations = emptySet(),
                    chapter6Completed = false,
                ),
        )

    private val chapters2To5State: StateFlow<Chapters2To5State> =
        combine(chapter2State, chapter3State, chapter4State, chapter5State) { chapter2, chapter3, chapter4, chapter5 ->
            Chapters2To5State(
                chapter2 = chapter2,
                chapter3 = chapter3,
                chapter4 = chapter4,
                chapter5 = chapter5,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Chapters2To5State(chapter2State.value, chapter3State.value, chapter4State.value, chapter5State.value),
        )

    private val season1WaiversState: StateFlow<Set<Int>> =
        progress.season1ChapterUnlockWaiversFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet(),
        )

    val uiState: StateFlow<MainUiState> =
        combine(
            combine(chapter1State, chapters2To5State, chapter6State) { chapter1, chapters2To5, chapter6 ->
                Triple(chapter1, chapters2To5, chapter6)
            },
            combine(season1WaiversState, prefs.characterFlow, prefs.playerAddressFlow) { season1Waivers, character, playerAddress ->
                Triple(season1Waivers, character, playerAddress)
            },
        ) { progress, identity ->
            val (chapter1, chapters2To5, chapter6) = progress
            val (season1Waivers, character, playerAddress) = identity
            MainUiState(
                beachOutroSeen = chapter1.beachOutroSeen,
                chapter1MidBoostSeen = chapter1.chapter1MidBoostSeen,
                unlockedLevel = chapter1.unlockedLevel,
                completedLevels = chapter1.completedLevels,
                chapter2MidBoostSeen = chapters2To5.chapter2.chapter2MidBoostSeen,
                chapter2UnlockedStation = chapters2To5.chapter2.chapter2UnlockedStation,
                chapter2CompletedStations = chapters2To5.chapter2.chapter2CompletedStations,
                chapter2Completed = chapters2To5.chapter2.chapter2Completed,
                chapter3MidBoostSeen = chapters2To5.chapter3.chapter3MidBoostSeen,
                chapter3UnlockedStation = chapters2To5.chapter3.chapter3UnlockedStation,
                chapter3CompletedStations = chapters2To5.chapter3.chapter3CompletedStations,
                chapter3Completed = chapters2To5.chapter3.chapter3Completed,
                chapter4IntroSeen = chapters2To5.chapter4.chapter4IntroSeen,
                chapter4LettersIntroSeen = chapters2To5.chapter4.chapter4LettersIntroSeen,
                chapter4MidBoostSeen = chapters2To5.chapter4.chapter4MidBoostSeen,
                chapter4UnlockedStation = chapters2To5.chapter4.chapter4UnlockedStation,
                chapter4CompletedStations = chapters2To5.chapter4.chapter4CompletedStations,
                chapter4Completed = chapters2To5.chapter4.chapter4Completed,
                chapter5IntroSeen = chapters2To5.chapter5.chapter5IntroSeen,
                chapter5LettersIntroSeen = chapters2To5.chapter5.chapter5LettersIntroSeen,
                chapter5MidBoostSeen = chapters2To5.chapter5.chapter5MidBoostSeen,
                chapter5UnlockedStation = chapters2To5.chapter5.chapter5UnlockedStation,
                chapter5CompletedStations = chapters2To5.chapter5.chapter5CompletedStations,
                chapter5Completed = chapters2To5.chapter5.chapter5Completed,
                chapter6IntroSeen = chapter6.chapter6IntroSeen,
                chapter6LettersIntroSeen = chapter6.chapter6LettersIntroSeen,
                chapter6MidBoostSeen = chapter6.chapter6MidBoostSeen,
                chapter6UnlockedStation = chapter6.chapter6UnlockedStation,
                chapter6CompletedStations = chapter6.chapter6CompletedStations,
                chapter6Completed = chapter6.chapter6Completed,
                season1ChapterUnlockWaivers = season1Waivers,
                character = character,
                playerAddress = playerAddress,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MainUiState(),
        )

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(context) as T
            }
            error("Unknown ViewModel class: $modelClass")
        }
    }
}
