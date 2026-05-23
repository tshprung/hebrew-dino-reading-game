package com.tal.hebrewdino.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.ui.graphics.Color
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.domain.AnswerResult
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.game.ChildGameAudioHooks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

internal object PopBalloonsActions {
    fun handlePopSfx(
        audioEnabled: Boolean,
        sagaUsesPopBalloonsAudioStaging: Boolean,
        letter: String,
        isCorrect: Boolean,
        finalCorrectBalloon: Boolean,
        balloonIndex: Int,
        scope: CoroutineScope,
        voice: VoicePlayer,
        sfx: SoundPoolPlayer,
        cancelFeedbackVoice: () -> Unit,
        audioRuntime: GameAudioRuntimeState,
        nextStation2CorrectPopVariant: () -> Int,
        station2PopTailPaddingMs: Long,
        station2PopFallbackDurationMs: Long,
    ) {
        if (!audioEnabled) return
        cancelFeedbackVoice()
        if (sagaUsesPopBalloonsAudioStaging) {
            GameAudioActions.launchFeedbackVoiceNoCancel(
                audioEnabled = true,
                scope = scope,
                audioRuntime = audioRuntime,
            ) {
                    sfx.stopAllStreams()
                    if (isCorrect) {
                        val variant = nextStation2CorrectPopVariant()
                        val rate =
                            if (variant % 2 == 0) {
                                0.98f
                            } else {
                                1.02f
                            }
                        val pops =
                            AudioClips.station2CorrectPopPlaylist(
                                variant,
                                finalCorrectBalloon,
                            )
                        val popPlayed =
                            sfx.playFirstAvailableReturningPathAndStreamId(
                                *pops,
                                volume = if (finalCorrectBalloon) 0.72f else 0.64f,
                                rate = rate,
                            )
                        val popPath = popPlayed?.first
                        val popMs = popPath?.let { sfx.durationMs(it) } ?: 0L
                        val r = rate.coerceIn(0.8f, 1.25f)
                        val popWaitMs =
                            when {
                                popPath == null -> 0L
                                popMs > 0 ->
                                    (popMs / r).toLong().coerceAtLeast(16L) +
                                        station2PopTailPaddingMs
                                else ->
                                    station2PopFallbackDurationMs +
                                        station2PopTailPaddingMs
                            }.coerceAtMost(5000L)
                        if (popWaitMs > 0) delay(popWaitMs)
                        sfx.stopAllStreams()
                        val speakLetter = finalCorrectBalloon || Random.nextFloat() < 0.35f
                        if (speakLetter) {
                            val letterClip = AudioClips.letterNameClip(letter)
                            if (letterClip != null) {
                                sfx.playReturningStreamId(letterClip, volume = 1f)
                                val d = sfx.durationMs(letterClip) ?: 0L
                                if (d > 0) delay(d)
                            }
                        }
                    } else {
                        val wrongPops =
                            AudioClips.station2WrongPopPlaylist(balloonIndex)
                        val wrongPopPlayed =
                            sfx.playFirstAvailableReturningPathAndStreamId(
                                *wrongPops,
                                volume = 0.56f,
                                rate = 1f,
                            )
                        val wrongPopPath = wrongPopPlayed?.first
                        val wrongPopMs = wrongPopPath?.let { sfx.durationMs(it) } ?: 0L
                        val wrongWaitMs =
                            when {
                                wrongPopPath == null -> 0L
                                wrongPopMs > 0 ->
                                    wrongPopMs.coerceAtLeast(16L) +
                                        station2PopTailPaddingMs
                                else ->
                                    station2PopFallbackDurationMs +
                                        station2PopTailPaddingMs
                            }.coerceAtMost(5000L)
                        if (wrongWaitMs > 0) delay(wrongWaitMs)
                        sfx.stopAllStreams()
                        val letterClip = AudioClips.letterNameClip(letter)
                        if (letterClip != null && voice.hasAsset(letterClip)) {
                            voice.playBlocking(letterClip)
                        }
                        voice.playFirstAvailableBlocking(
                            AudioClips.VoTryAgain2,
                            AudioClips.VoTryAgain1,
                        )
                    }
                }
            return
        }
        scope.launch {
            sfx.playFirstAvailable(
                if (isCorrect) AudioClips.SfxBalloonPopSoft else AudioClips.SfxBalloonPopWrongFunny,
                AudioClips.SfxBalloonPopSoft,
                AudioClips.SfxBalloonPop,
                volume = if (isCorrect) 0.88f else 0.32f,
            )
        }
        val clip = AudioClips.letterNameClip(letter)
        if (clip != null) {
            GameAudioActions.launchFeedbackVoiceNoCancel(
                audioEnabled = true,
                scope = scope,
                audioRuntime = audioRuntime,
            ) {
                delay(90)
                voice.playSequenceBlocking(clip)
            }
        }
    }

    fun handleWrongPick(
        gameViewModel: GameViewModel,
        sagaUsesPopBalloonsAudioStaging: Boolean,
        cancelFeedbackVoice: () -> Unit,
        onWrongFeedback: () -> Unit,
        session: LevelSession,
        chapterId: Int,
        stationId: Int,
        scope: CoroutineScope,
        optionsShake: Animatable<Float, AnimationVector1D>,
    ) {
        if (!gameViewModel.consumeTapCooldown()) return
        if (!sagaUsesPopBalloonsAudioStaging) {
            cancelFeedbackVoice()
        }
        session.wrongTap()
        gameViewModel.shakeEpoch += 1
        HintPulseActions.registerWrongTapForHintPulse(gameViewModel)
        if (sagaUsesPopBalloonsAudioStaging) {
            scope.launch {
                gameViewModel.inputLocked = true
                gameViewModel.dinoVisual = DinoVisual.TryAgain
                val strongerWrongShake =
                    (isSagaEpisode(chapterId) &&
                        (stationId == Chapter1StationOrder.PICTURE_PICK_ONE ||
                            stationId == Chapter1StationOrder.PICTURE_PICK_ALL))
                playShake(
                    scope,
                    optionsShake,
                    baseShakeAmplitudePx =
                        if (isSagaEpisode(chapterId)) 20f else 18f,
                    strength = if (strongerWrongShake) 1.25f else 1f,
                )
                gameViewModel.dinoVisual = DinoVisual.Idle
                gameViewModel.inputLocked = false
            }
        } else {
            onWrongFeedback()
        }
    }

    fun handleAllCorrectPopped(
        lastLetter: String,
        poppedBalloonColor: Color,
        isChapter3PopAllLettersStation: Boolean,
        gameViewModel: GameViewModel,
        sagaUsesPopBalloonsAudioStaging: Boolean,
        chapterId: Int,
        stationId: Int,
        audioEnabled: Boolean,
        cancelFeedbackVoice: () -> Unit,
        session: LevelSession,
        scope: CoroutineScope,
        audioRuntime: GameAudioRuntimeState,
        advanceAfterRound: suspend (isLast: Boolean) -> Unit,
    ) {
        val ch1St2 = sagaUsesPopBalloonsAudioStaging
        if (ch1St2 || (chapterId == 3 && stationId == 3)) {
            gameViewModel.station2PinnedBalloonLetter = lastLetter
            gameViewModel.station2PinnedBalloonColor = poppedBalloonColor
        } else if (!ch1St2) {
            cancelFeedbackVoice()
        }
        if (isChapter3PopAllLettersStation) {
            gameViewModel.inputLocked = true
            scope.launch {
                session.completeCurrentRound()
                if (audioEnabled) ChildGameAudioHooks.onCorrect()
                val isLast = session.currentIndex >= session.totalQuestions - 1
                advanceAfterRound(isLast)
            }
        } else {
            when (session.submitAnswer(lastLetter)) {
                AnswerResult.Correct ->
                    scope.launch {
                        gameViewModel.inputLocked = true
                        if (ch1St2) {
                            GameAudioActions.awaitFeedbackVoice(audioRuntime, 4000L)
                            cancelFeedbackVoice()
                        }
                        if (audioEnabled && !ch1St2) {
                            ChildGameAudioHooks.onCorrect()
                        }
                        val isLast = session.currentIndex >= session.totalQuestions - 1
                        advanceAfterRound(isLast)
                    }
                else -> {}
            }
        }
    }
}

