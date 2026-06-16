package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.domain.LetterPoolSpec
import com.tal.hebrewdino.ui.domain.TrainingV1Config
import kotlinx.coroutines.Job

internal class GameAudioRuntimeState {
    var feedbackVoiceJob: Job? = null
    var promptVoiceJob: Job? = null
    var lastPraiseAssetPath: String? = null
}

private object GameAudioPreloader {
    suspend fun preloadStation1(
        audioEnabled: Boolean,
        sagaUsesPickLetterAudioStaging: Boolean,
        chapterId: Int,
        letterPoolSpec: LetterPoolSpec,
        voice: VoicePlayer,
        sfx: SoundPoolPlayer,
    ) {
        if (!(audioEnabled && sagaUsesPickLetterAudioStaging)) return
        val usesRawLetterNames =
            chapterId == 1 ||
                chapterId == 2 ||
                chapterId == 3 ||
                chapterId == 4 ||
                chapterId == 5 ||
                chapterId == 6 ||
                chapterId == TrainingV1Config.CHAPTER_ID
        val poolLetters = letterPoolSpec.groups.flatten().distinct()
        if (chapterId == 4) {
            voice.warmUp(AudioClips.VoBachorEtHaot)
        }
        val perLetterPaths =
            poolLetters.flatMap { letter ->
                listOfNotNull(
                    AudioClips.station1WrongCombined(letter),
                    if (usesRawLetterNames) null else AudioClips.letterNameClip(letter),
                )
            }
        val station1IntroExtras =
            if (chapterId == 4) {
                arrayOf(AudioClips.VoBachorEtHaot)
            } else {
                emptyArray()
            }
        sfx.preload(
            *station1IntroExtras,
            *perLetterPaths.toTypedArray(),
        )
    }

    suspend fun preloadPopBalloons(
        audioEnabled: Boolean,
        usesPopBalloonsSoundPoolPrompt: Boolean,
        chapterId: Int,
        letterPoolSpec: LetterPoolSpec,
        sfx: SoundPoolPlayer,
    ) {
        if (!(audioEnabled && usesPopBalloonsSoundPoolPrompt)) return
        val usesRawLetterNames =
            chapterId == 1 ||
                chapterId == 2 ||
                chapterId == 3 ||
                chapterId == 4 ||
                chapterId == 5 ||
                chapterId == 6 ||
                chapterId == TrainingV1Config.CHAPTER_ID
        val letters = letterPoolSpec.groups.flatten().distinct()
        val paths = ArrayList<String>()
        paths.add(AudioClips.PopAllBalloonsWithLetter)
        paths.add(AudioClips.SfxBalloonPopSoft)
        paths.add(AudioClips.SfxBalloonPopWrongFunny)
        paths.add(AudioClips.SfxBalloonPop)
        paths.add(AudioClips.SfxStation2PopSoft1)
        paths.add(AudioClips.SfxStation2PopSoft2)
        paths.add(AudioClips.SfxStation2PopPlop)
        paths.add(AudioClips.SfxStation2PopFinale)
        for (letter in letters) {
            if (!usesRawLetterNames) {
                AudioClips.letterNameClip(letter)?.let(paths::add)
            }
            AudioClips.wrongSentenceClip(letter)?.let(paths::add)
            AudioClips.station1WrongCombined(letter)?.let(paths::add)
        }
        sfx.preload(*paths.distinct().toTypedArray())
    }

    suspend fun preloadFindGrid(
        audioEnabled: Boolean,
        sagaUsesFindGridAudioStaging: Boolean,
        sfx: SoundPoolPlayer,
    ) {
        if (!(audioEnabled && sagaUsesFindGridAudioStaging)) return
        val paths = ArrayList<String>()
        paths.add(AudioClips.SfxWrong)
        paths.add(AudioClips.SfxCorrect)
        sfx.preload(*paths.distinct().toTypedArray())
    }
}

@Composable
internal fun GameAudioLifecycleEffects(
    lifecycleOwner: LifecycleOwner,
    stationId: Int,
    cancelFeedbackVoice: () -> Unit,
    releaseAudio: () -> Unit,
) {
    val cancelFeedbackVoiceLatest by rememberUpdatedState(cancelFeedbackVoice)
    val releaseAudioLatest by rememberUpdatedState(releaseAudio)

    DisposableEffect(lifecycleOwner, stationId) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                    cancelFeedbackVoiceLatest()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            cancelFeedbackVoiceLatest()
            releaseAudioLatest()
        }
    }
}

@Composable
internal fun GameAudioPreloadEffects(
    stationId: Int,
    chapterId: Int,
    letterPoolSpec: LetterPoolSpec,
    audioEnabled: Boolean,
    sagaUsesPickLetterAudioStaging: Boolean,
    usesPopBalloonsSoundPoolPrompt: Boolean,
    sagaUsesFindGridAudioStaging: Boolean,
    voice: VoicePlayer,
    sfx: SoundPoolPlayer,
) {
    LaunchedEffect(stationId, chapterId, letterPoolSpec) {
        GameAudioPreloader.preloadStation1(
            audioEnabled = audioEnabled,
            sagaUsesPickLetterAudioStaging = sagaUsesPickLetterAudioStaging,
            chapterId = chapterId,
            letterPoolSpec = letterPoolSpec,
            voice = voice,
            sfx = sfx,
        )
    }

    LaunchedEffect(stationId, chapterId, letterPoolSpec) {
        GameAudioPreloader.preloadPopBalloons(
            audioEnabled = audioEnabled,
            usesPopBalloonsSoundPoolPrompt = usesPopBalloonsSoundPoolPrompt,
            chapterId = chapterId,
            letterPoolSpec = letterPoolSpec,
            sfx = sfx,
        )
    }

    LaunchedEffect(stationId, chapterId, letterPoolSpec) {
        GameAudioPreloader.preloadFindGrid(
            audioEnabled = audioEnabled,
            sagaUsesFindGridAudioStaging = sagaUsesFindGridAudioStaging,
            sfx = sfx,
        )
    }
}
