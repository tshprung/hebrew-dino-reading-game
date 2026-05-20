package com.tal.hebrewdino.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.feedback.GameFeedback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

internal object WrongFeedbackActions {
    fun trigger(
        scope: CoroutineScope,
        gameViewModel: GameViewModel,
        audioEnabled: Boolean,
        sagaEpisode: Boolean,
        chapterId: Int,
        stationId: Int,
        sagaUsesPickLetterAudioStaging: Boolean,
        isChapter3HighlightedLetterInWordStation: Boolean,
        isChapter3AudioLetterRecognitionStation: Boolean,
        gameFeedback: GameFeedback,
        voice: VoicePlayer,
        sfx: SoundPoolPlayer,
        cancelFeedbackVoice: () -> Unit,
        audioRuntime: GameAudioRuntimeState,
        optionsShake: Animatable<Float, AnimationVector1D>,
        dinoSlip: Animatable<Float, AnimationVector1D>,
        dinoTilt: Animatable<Float, AnimationVector1D>,
        onWrongHook: () -> Unit,
        wrongPickedLetter: String? = null,
        wrongWordCatalogId: String? = null,
        wrongPickedLetterAlreadySpoken: Boolean = false,
        wrongWordAlreadySpoken: Boolean = false,
    ) {
        scope.launch {
            gameViewModel.inputLocked = true
            gameViewModel.dinoVisual = DinoVisual.TryAgain
            val immediateCh1Ch2Station4Voice =
                audioEnabled &&
                    sagaEpisode &&
                    (chapterId == 1 || chapterId == 2) &&
                    stationId == Chapter1StationOrder.PICTURE_PICK_ONE &&
                    wrongPickedLetter != null
            if (immediateCh1Ch2Station4Voice) {
                GameAudioActions.launchFeedbackVoiceAfterCancel(
                    audioEnabled = audioEnabled,
                    scope = scope,
                    audioRuntime = audioRuntime,
                    cancelFeedbackVoice = cancelFeedbackVoice,
                ) {
                        val lc = AudioClips.letterNameClip(wrongPickedLetter)
                        val letterMs = lc?.let { sfx.durationMs(it) } ?: 0L
                        if (lc != null && letterMs > 0L) {
                            sfx.stopAllStreams()
                            sfx.playReturningStreamId(lc, volume = 1f)
                            val baseWrongFrac =
                                Station1WrongLetterToFollowLeadFraction *
                                    Station4WrongLetterToFollowLeadScale
                            val followLeadFrac =
                                baseWrongFrac +
                                    Station4WrongLetterToTryAgainGapBoost * (1f - baseWrongFrac)
                            val lead =
                                (letterMs * followLeadFrac)
                                    .toLong()
                                    .coerceIn(16L, letterMs)
                            delay(lead)
                            sfx.playFirstAvailable(
                                AudioClips.VoTryAgain2,
                                AudioClips.VoTryAgain1,
                                volume = 1f,
                            )
                        } else {
                            if (lc != null && voice.hasAsset(lc)) {
                                voice.playBlocking(lc)
                            }
                            voice.playFirstAvailableBlocking(AudioClips.VoTryAgain2, AudioClips.VoTryAgain1)
                        }
                }
            }
            if (sagaEpisode) {
                dinoSlip.snapTo(0f)
                dinoTilt.snapTo(0f)
                dinoTilt.animateTo(-7f, tween(90))
                dinoSlip.animateTo(10f, tween(90))
                dinoTilt.animateTo(6f, tween(110))
                dinoSlip.animateTo(-6f, tween(110))
                dinoTilt.animateTo(0f, tween(140))
                dinoSlip.animateTo(0f, tween(140))
            }
            val strongerWrongShake =
                (sagaEpisode && (stationId == Chapter1StationOrder.PICTURE_PICK_ONE || stationId == Chapter1StationOrder.PICTURE_PICK_ALL))
            playShake(
                scope,
                optionsShake,
                baseShakeAmplitudePx = if (sagaEpisode) 20f else 18f,
                strength = if (strongerWrongShake) 1.25f else 1f,
            )
            if (audioEnabled) {
                val allowWrongSfx =
                    (!(sagaUsesPickLetterAudioStaging) || isChapter3HighlightedLetterInWordStation || isChapter3AudioLetterRecognitionStation) &&
                        !(sagaEpisode && stationId == Chapter1StationOrder.PICTURE_PICK_ALL)
                if (allowWrongSfx) {
                    gameFeedback.playWrong()
                    onWrongHook()
                }
                if (sagaUsesPickLetterAudioStaging && wrongPickedLetter != null && chapterId != 3) {
                    GameAudioActions.launchFeedbackVoiceAfterCancel(
                        audioEnabled = audioEnabled,
                        scope = scope,
                        audioRuntime = audioRuntime,
                        cancelFeedbackVoice = cancelFeedbackVoice,
                    ) play@{
                            val letterClip = AudioClips.letterNameClip(wrongPickedLetter)
                            val letterMs = letterClip?.let { sfx.durationMs(it) } ?: 0L
                            val variant = Random.nextInt(100)
                            if (variant < 20) {
                                sfx.playFirstAvailable(
                                    AudioClips.VoTryAgain2,
                                    AudioClips.VoTryAgain1,
                                    volume = 1f,
                                )
                                return@play
                            }
                            if (variant < 55) {
                                if (letterClip != null && voice.hasAsset(letterClip)) voice.playBlocking(letterClip)
                                return@play
                            }

                            if (letterClip != null && letterMs > 0L) {
                                sfx.stopAllStreams()
                                sfx.playReturningStreamId(letterClip, volume = 1f)
                                val followLeadFrac =
                                    Station1WrongLetterToFollowLeadFraction +
                                        Station1WrongLetterToTryAgainGapBoost *
                                        (1f - Station1WrongLetterToFollowLeadFraction)
                                val lead =
                                    (letterMs * followLeadFrac)
                                        .toLong()
                                        .coerceIn(16L, letterMs)
                                delay(lead)
                                sfx.playFirstAvailable(
                                    AudioClips.VoTryAgain2,
                                    AudioClips.VoTryAgain1,
                                    volume = 1f,
                                )
                            } else {
                                if (letterClip != null && voice.hasAsset(letterClip)) {
                                    voice.playBlocking(letterClip)
                                }
                                voice.playFirstAvailableBlocking(AudioClips.VoTryAgain2, AudioClips.VoTryAgain1)
                            }
                    }
                    gameViewModel.dinoVisual = DinoVisual.Idle
                    gameViewModel.inputLocked = false
                    return@launch
                }
                if (sagaEpisode &&
                    stationId == Chapter1StationOrder.PICTURE_PICK_ONE &&
                    wrongPickedLetter != null &&
                    !immediateCh1Ch2Station4Voice
                ) {
                    GameAudioActions.launchFeedbackVoiceAfterCancel(
                        audioEnabled = audioEnabled,
                        scope = scope,
                        audioRuntime = audioRuntime,
                        cancelFeedbackVoice = cancelFeedbackVoice,
                    ) {
                            val lc = AudioClips.letterNameClip(wrongPickedLetter)
                            val letterMs = lc?.let { sfx.durationMs(it) } ?: 0L
                            if (lc != null && letterMs > 0L) {
                                sfx.stopAllStreams()
                                sfx.playReturningStreamId(lc, volume = 1f)
                                val baseWrongFrac =
                                    Station1WrongLetterToFollowLeadFraction *
                                        Station4WrongLetterToFollowLeadScale
                                val followLeadFrac =
                                    baseWrongFrac +
                                        Station4WrongLetterToTryAgainGapBoost * (1f - baseWrongFrac)
                                val lead =
                                    (letterMs * followLeadFrac)
                                        .toLong()
                                        .coerceIn(16L, letterMs)
                                delay(lead)
                                sfx.playFirstAvailable(
                                    AudioClips.VoTryAgain2,
                                    AudioClips.VoTryAgain1,
                                    volume = 1f,
                                )
                            } else {
                                if (lc != null && voice.hasAsset(lc)) {
                                    voice.playBlocking(lc)
                                }
                                voice.playFirstAvailableBlocking(AudioClips.VoTryAgain2, AudioClips.VoTryAgain1)
                            }
                    }
                    gameViewModel.dinoVisual = DinoVisual.Idle
                    gameViewModel.inputLocked = false
                    return@launch
                }
                if (!immediateCh1Ch2Station4Voice) {
                    GameAudioActions.launchFeedbackVoiceAfterCancel(
                        audioEnabled = audioEnabled,
                        scope = scope,
                        audioRuntime = audioRuntime,
                        cancelFeedbackVoice = cancelFeedbackVoice,
                    ) play@{
                            val feedbackDelayMs =
                                when {
                                    chapterId == 4 && stationId == Chapter1StationOrder.TAP_LETTER -> 0L
                                    chapterId == 4 && stationId == Chapter1StationOrder.PICTURE_PICK_ONE -> 0L
                                    chapterId == 4 && stationId == Chapter1StationOrder.PICTURE_PICK_ALL -> 0L
                                    (chapterId == 3 || chapterId == 6) && stationId == 4 -> 0L
                                    chapterId == 5 && stationId == Chapter1StationOrder.PICTURE_PICK_ONE -> 0L
                                    else -> 110L
                                }
                            delay(feedbackDelayMs)
                            if (wrongWordCatalogId != null && !wrongWordAlreadySpoken) {
                                val wordPath = AudioClips.wordClipByCatalogId(wrongWordCatalogId)
                                if (voice.hasAsset(wordPath)) voice.playBlocking(wordPath)
                                voice.playFirstAvailableBlocking(AudioClips.VoTryAgain2, AudioClips.VoTryAgain1)
                                return@play
                            }

                            if (wrongPickedLetter != null) {
                                if (chapterId == 5 && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                                    val lc = AudioClips.letterNameClip(wrongPickedLetter)
                                    if (lc != null && voice.hasAsset(lc)) {
                                        voice.playBlocking(lc)
                                    }
                                    voice.playFirstAvailableBlocking(AudioClips.VoTryAgain2, AudioClips.VoTryAgain1)
                                    return@play
                                }
                                if (!wrongPickedLetterAlreadySpoken) {
                                    val letterName = AudioClips.letterNameClip(wrongPickedLetter)
                                    if (letterName != null && voice.hasAsset(letterName)) {
                                        voice.playBlocking(letterName)
                                    }
                                }
                                voice.playFirstAvailableBlocking(AudioClips.VoTryAgain2, AudioClips.VoTryAgain1)
                                return@play
                            }

                            voice.playFirstAvailableBlocking(AudioClips.VoTryAgain2, AudioClips.VoTryAgain1)
                    }
                }
            }
            gameViewModel.dinoVisual = DinoVisual.Idle
            gameViewModel.inputLocked = false
        }
    }
}
