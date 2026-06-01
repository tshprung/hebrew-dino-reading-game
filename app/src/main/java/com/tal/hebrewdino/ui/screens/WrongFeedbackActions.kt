package com.tal.hebrewdino.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.companion.playAddressAwareTryAgainBlocking
import com.tal.hebrewdino.ui.companion.playLetterThenAddressAwareTryAgain
import com.tal.hebrewdino.ui.data.PlayerAddress
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.feedback.GameFeedback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

internal object WrongFeedbackActions {
    private suspend fun playLetterThenTryAgainOnSoundPool(
        sfx: SoundPoolPlayer,
        letterClip: String,
        letterMs: Long,
        followLeadFrac: Float,
        chapterId: Int,
        stationId: Int,
        playerAddress: PlayerAddress?,
        rawVoice: RawVoicePlayer?,
        voice: VoicePlayer,
    ) {
        playLetterThenAddressAwareTryAgain(
            sfx = sfx,
            letterClip = letterClip,
            letterMs = letterMs,
            followLeadFrac = followLeadFrac,
            chapterId = chapterId,
            stationId = stationId,
            playerAddress = playerAddress,
            rawVoice = rawVoice,
            voice = voice,
            context = "WrongFeedbackActions.playLetterThenTryAgainOnSoundPool(addressAware)",
        )
    }

    private suspend fun playTryAgainFallback(
        chapterId: Int,
        stationId: Int,
        playerAddress: PlayerAddress?,
        rawVoice: RawVoicePlayer?,
        voice: VoicePlayer,
    ) {
        playAddressAwareTryAgainBlocking(
            chapterId = chapterId,
            stationId = stationId,
            playerAddress = playerAddress,
            rawVoice = rawVoice,
            voice = voice,
            context = "WrongFeedbackActions.playTryAgainFallback",
        )
    }

    private suspend fun playStandaloneTryAgain(
        sfx: SoundPoolPlayer,
        chapterId: Int,
        stationId: Int,
        playerAddress: PlayerAddress?,
        rawVoice: RawVoicePlayer?,
        voice: VoicePlayer,
    ) {
        playAddressAwareTryAgainBlocking(
            chapterId = chapterId,
            stationId = stationId,
            playerAddress = playerAddress,
            rawVoice = rawVoice,
            voice = voice,
            context = "WrongFeedbackActions.playStandaloneTryAgain(addressAware)",
        )
    }

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
        chapter1PlayerAddress: PlayerAddress? = null,
        rawVoice: RawVoicePlayer? = null,
    ) {
        scope.launch {
            gameViewModel.inputLocked = true
            gameViewModel.dinoVisual = DinoVisual.TryAgain
            val immediateCh1Ch2Station4Voice =
                audioEnabled &&
                    sagaEpisode &&
                    (chapterId == 1 || chapterId == 2) &&
                    stationId == Chapter1StationOrder.PICTURE_PICK_ONE &&
                    wrongPickedLetter != null &&
                    !wrongPickedLetterAlreadySpoken
            if (immediateCh1Ch2Station4Voice) {
                GameAudioActions.launchFeedbackVoice(
                    audioEnabled = audioEnabled,
                    scope = scope,
                    audioRuntime = audioRuntime,
                    cancelFeedbackVoice = cancelFeedbackVoice,
                ) {
                    val requiredChapter = chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5
                    if (requiredChapter) {
                        val resId = AudioClips.letterNameRawResId(wrongPickedLetter)
                        if (resId == null) {
                            android.util.Log.e(
                                "MissingContent",
                                "Missing required wrong-feedback letter-name audio. chapterId=$chapterId stationId=$stationId context=WrongFeedbackActions.trigger(immediateStation4) stage=missing raw letter-name mapping wrongPickedLetter='$wrongPickedLetter'",
                            )
                            rawVoice?.playRawBlocking(0)
                            return@launchFeedbackVoice
                        }
                        if (rawVoice == null) {
                            android.util.Log.e(
                                "MissingContent",
                                "Missing required wrong-feedback letter-name audio. chapterId=$chapterId stationId=$stationId context=WrongFeedbackActions.trigger(immediateStation4) stage=rawVoice=null expectedRawResId=$resId",
                            )
                            voice.playRequiredBlocking(
                                assetPath = "",
                                context = "WrongFeedbackActions.trigger(immediateStation4,rawVoice=null)",
                                chapterId = chapterId,
                                stationId = stationId,
                            )
                            return@launchFeedbackVoice
                        }
                        rawVoice.playRawBlocking(resId)
                        playTryAgainFallback(
                            chapterId = chapterId,
                            stationId = stationId,
                            playerAddress = chapter1PlayerAddress,
                            rawVoice = rawVoice,
                            voice = voice,
                        )
                        return@launchFeedbackVoice
                    }

                    val lc = AudioClips.letterNameClip(wrongPickedLetter)
                    val letterMs =
                        lc?.let {
                            if (chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5) {
                                sfx.durationMsRequiredOrNull(
                                    assetPath = it,
                                    context = "WrongFeedbackActions.trigger(immediateStation4,SoundPoolLetter)",
                                    chapterId = chapterId,
                                    stationId = stationId,
                                ) ?: 0L
                            } else {
                                sfx.durationMs(it) ?: 0L
                            }
                        } ?: 0L
                    if (lc != null && letterMs > 0L) {
                        val baseWrongFrac =
                            Station1WrongLetterToFollowLeadFraction *
                                Station4WrongLetterToFollowLeadScale
                        val followLeadFrac =
                            baseWrongFrac +
                                Station4WrongLetterToTryAgainGapBoost * (1f - baseWrongFrac)
                        playLetterThenTryAgainOnSoundPool(
                            sfx = sfx,
                            letterClip = lc,
                            letterMs = letterMs,
                            followLeadFrac = followLeadFrac,
                            chapterId = chapterId,
                            stationId = stationId,
                            playerAddress = chapter1PlayerAddress,
                            rawVoice = rawVoice,
                            voice = voice,
                        )
                    } else {
                        if (lc != null) {
                            if (chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5) {
                                voice.playRequiredBlocking(
                                    assetPath = lc,
                                    context = "WrongFeedbackActions.trigger(immediateStation4,LetterOnly)",
                                    chapterId = chapterId,
                                    stationId = stationId,
                                )
                            } else if (voice.hasAsset(lc)) {
                                voice.playBlocking(lc)
                            }
                        }
                        playTryAgainFallback(
                            chapterId = chapterId,
                            stationId = stationId,
                            playerAddress = chapter1PlayerAddress,
                            rawVoice = rawVoice,
                            voice = voice,
                        )
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
                        !(sagaEpisode && stationId == Chapter1StationOrder.PICTURE_PICK_ALL) &&
                        !(chapterId == 3 && stationId == 3)
                if (allowWrongSfx) {
                    gameFeedback.playWrong()
                    onWrongHook()
                }
                if (sagaUsesPickLetterAudioStaging && wrongPickedLetter != null && chapterId != 3) {
                    GameAudioActions.launchFeedbackVoice(
                        audioEnabled = audioEnabled,
                        scope = scope,
                        audioRuntime = audioRuntime,
                        cancelFeedbackVoice = cancelFeedbackVoice,
                    ) play@{
                        if (wrongPickedLetterAlreadySpoken) {
                            playStandaloneTryAgain(
                                sfx = sfx,
                                chapterId = chapterId,
                                stationId = stationId,
                                playerAddress = chapter1PlayerAddress,
                                rawVoice = rawVoice,
                                voice = voice,
                            )
                            return@play
                        }
                        val requiredChapter = chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5
                        if (requiredChapter) {
                            val resId = AudioClips.letterNameRawResId(wrongPickedLetter)
                            if (resId == null) {
                                android.util.Log.e(
                                    "MissingContent",
                                    "Missing required wrong-feedback letter-name audio. chapterId=$chapterId stationId=$stationId context=WrongFeedbackActions.trigger(sagaPickLetter) stage=missing raw letter-name mapping wrongPickedLetter='$wrongPickedLetter'",
                                )
                                rawVoice?.playRawBlocking(0)
                                return@play
                            }
                            if (rawVoice == null) {
                                android.util.Log.e(
                                    "MissingContent",
                                    "Missing required wrong-feedback letter-name audio. chapterId=$chapterId stationId=$stationId context=WrongFeedbackActions.trigger(sagaPickLetter) stage=rawVoice=null expectedRawResId=$resId",
                                )
                                voice.playRequiredBlocking(
                                    assetPath = "",
                                    context = "WrongFeedbackActions.trigger(sagaPickLetter,rawVoice=null)",
                                    chapterId = chapterId,
                                    stationId = stationId,
                                )
                                return@play
                            }
                            val variant = Random.nextInt(100)
                            if (variant < 20) {
                                playStandaloneTryAgain(
                                    sfx = sfx,
                                    chapterId = chapterId,
                                    stationId = stationId,
                                    playerAddress = chapter1PlayerAddress,
                                    rawVoice = rawVoice,
                                    voice = voice,
                                )
                                return@play
                            }
                            rawVoice.playRawBlocking(resId)
                            if (variant < 55) return@play
                            playTryAgainFallback(
                                chapterId = chapterId,
                                stationId = stationId,
                                playerAddress = chapter1PlayerAddress,
                                rawVoice = rawVoice,
                                voice = voice,
                            )
                            return@play
                        }
                        val letterClip = AudioClips.letterNameClip(wrongPickedLetter)
                        val letterMs =
                            letterClip?.let {
                                if (chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5) {
                                    sfx.durationMsRequiredOrNull(
                                        assetPath = it,
                                        context = "WrongFeedbackActions.trigger(sagaPickLetter,SoundPoolLetter)",
                                        chapterId = chapterId,
                                        stationId = stationId,
                                    ) ?: 0L
                                } else {
                                    sfx.durationMs(it) ?: 0L
                                }
                            } ?: 0L
                        val variant = Random.nextInt(100)
                        if (variant < 20) {
                            playStandaloneTryAgain(
                                sfx = sfx,
                                chapterId = chapterId,
                                stationId = stationId,
                                playerAddress = chapter1PlayerAddress,
                                rawVoice = rawVoice,
                                voice = voice,
                            )
                            return@play
                        }
                        if (variant < 55) {
                            if (letterClip != null) {
                                if (chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5) {
                                    voice.playRequiredBlocking(
                                        assetPath = letterClip,
                                        context = "WrongFeedbackActions.trigger(sagaPickLetter,LetterOnly)",
                                        chapterId = chapterId,
                                        stationId = stationId,
                                    )
                                } else if (voice.hasAsset(letterClip)) {
                                    voice.playBlocking(letterClip)
                                }
                            } else if (chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5) {
                                android.util.Log.e(
                                    "MissingContent",
                                    "Missing required wrong-feedback letter audio. chapterId=$chapterId stationId=$stationId context=WrongFeedbackActions.trigger(sagaPickLetter,LetterOnly) stage=missing letter-name mapping wrongPickedLetter='$wrongPickedLetter'",
                                )
                                voice.playRequiredBlocking(
                                    assetPath = "",
                                    context = "WrongFeedbackActions.trigger(sagaPickLetter,missingLetterNameMapping)",
                                    chapterId = chapterId,
                                    stationId = stationId,
                                )
                            }
                            return@play
                        }

                        if (letterClip != null && letterMs > 0L) {
                            val followLeadFrac =
                                Station1WrongLetterToFollowLeadFraction +
                                    Station1WrongLetterToTryAgainGapBoost *
                                    (1f - Station1WrongLetterToFollowLeadFraction)
                            playLetterThenTryAgainOnSoundPool(
                                sfx = sfx,
                                letterClip = letterClip,
                                letterMs = letterMs,
                                followLeadFrac = followLeadFrac,
                                chapterId = chapterId,
                                stationId = stationId,
                                playerAddress = chapter1PlayerAddress,
                                rawVoice = rawVoice,
                                voice = voice,
                            )
                        } else {
                            if (letterClip != null) {
                                if (chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5) {
                                    voice.playRequiredBlocking(
                                        assetPath = letterClip,
                                        context = "WrongFeedbackActions.trigger(sagaPickLetter,LetterOnlyFallback)",
                                        chapterId = chapterId,
                                        stationId = stationId,
                                    )
                                } else if (voice.hasAsset(letterClip)) {
                                    voice.playBlocking(letterClip)
                                }
                            } else if (chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5) {
                                android.util.Log.e(
                                    "MissingContent",
                                    "Missing required wrong-feedback letter audio. chapterId=$chapterId stationId=$stationId context=WrongFeedbackActions.trigger(sagaPickLetter,LetterOnlyFallback) stage=missing letter-name mapping wrongPickedLetter='$wrongPickedLetter'",
                                )
                                voice.playRequiredBlocking(
                                    assetPath = "",
                                    context = "WrongFeedbackActions.trigger(sagaPickLetter,missingLetterNameMapping)",
                                    chapterId = chapterId,
                                    stationId = stationId,
                                )
                            }
                            playTryAgainFallback(
                                chapterId = chapterId,
                                stationId = stationId,
                                playerAddress = chapter1PlayerAddress,
                                rawVoice = rawVoice,
                                voice = voice,
                            )
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
                    GameAudioActions.launchFeedbackVoice(
                        audioEnabled = audioEnabled,
                        scope = scope,
                        audioRuntime = audioRuntime,
                        cancelFeedbackVoice = cancelFeedbackVoice,
                    ) play@{
                        if (wrongPickedLetterAlreadySpoken) {
                            playTryAgainFallback(
                                chapterId = chapterId,
                                stationId = stationId,
                                playerAddress = chapter1PlayerAddress,
                                rawVoice = rawVoice,
                                voice = voice,
                            )
                            return@play
                        }
                        val requiredChapter = chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5
                        if (requiredChapter) {
                            val resId = AudioClips.letterNameRawResId(wrongPickedLetter)
                            if (resId == null) {
                                android.util.Log.e(
                                    "MissingContent",
                                    "Missing required wrong-feedback letter-name audio. chapterId=$chapterId stationId=$stationId context=WrongFeedbackActions.trigger(station4) stage=missing raw letter-name mapping wrongPickedLetter='$wrongPickedLetter'",
                                )
                                rawVoice?.playRawBlocking(0)
                                return@play
                            }
                            if (rawVoice == null) {
                                android.util.Log.e(
                                    "MissingContent",
                                    "Missing required wrong-feedback letter-name audio. chapterId=$chapterId stationId=$stationId context=WrongFeedbackActions.trigger(station4) stage=rawVoice=null expectedRawResId=$resId",
                                )
                                voice.playRequiredBlocking(
                                    assetPath = "",
                                    context = "WrongFeedbackActions.trigger(station4,rawVoice=null)",
                                    chapterId = chapterId,
                                    stationId = stationId,
                                )
                                return@play
                            }
                            rawVoice.playRawBlocking(resId)
                            playTryAgainFallback(
                                chapterId = chapterId,
                                stationId = stationId,
                                playerAddress = chapter1PlayerAddress,
                                rawVoice = rawVoice,
                                voice = voice,
                            )
                            return@play
                        }
                        val lc = AudioClips.letterNameClip(wrongPickedLetter)
                        val letterMs =
                            lc?.let {
                                if (chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5) {
                                    sfx.durationMsRequiredOrNull(
                                        assetPath = it,
                                        context = "WrongFeedbackActions.trigger(station4,SoundPoolLetter)",
                                        chapterId = chapterId,
                                        stationId = stationId,
                                    ) ?: 0L
                                } else {
                                    sfx.durationMs(it) ?: 0L
                                }
                            } ?: 0L
                        if (lc != null && letterMs > 0L) {
                            val baseWrongFrac =
                                Station1WrongLetterToFollowLeadFraction *
                                    Station4WrongLetterToFollowLeadScale
                            val followLeadFrac =
                                baseWrongFrac +
                                    Station4WrongLetterToTryAgainGapBoost * (1f - baseWrongFrac)
                            playLetterThenTryAgainOnSoundPool(
                                sfx = sfx,
                                letterClip = lc,
                                letterMs = letterMs,
                                followLeadFrac = followLeadFrac,
                                chapterId = chapterId,
                                stationId = stationId,
                                playerAddress = chapter1PlayerAddress,
                                rawVoice = rawVoice,
                                voice = voice,
                            )
                        } else {
                            if (lc != null) {
                                if (chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5) {
                                    voice.playRequiredBlocking(
                                        assetPath = lc,
                                        context = "WrongFeedbackActions.trigger(station4,LetterOnlyFallback)",
                                        chapterId = chapterId,
                                        stationId = stationId,
                                    )
                                } else if (voice.hasAsset(lc)) {
                                    voice.playBlocking(lc)
                                }
                            } else if (chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5) {
                                android.util.Log.e(
                                    "MissingContent",
                                    "Missing required wrong-feedback letter audio. chapterId=$chapterId stationId=$stationId context=WrongFeedbackActions.trigger(station4,LetterOnlyFallback) stage=missing letter-name mapping wrongPickedLetter='$wrongPickedLetter'",
                                )
                                voice.playRequiredBlocking(
                                    assetPath = "",
                                    context = "WrongFeedbackActions.trigger(station4,missingLetterNameMapping)",
                                    chapterId = chapterId,
                                    stationId = stationId,
                                )
                            }
                            playTryAgainFallback(
                                chapterId = chapterId,
                                stationId = stationId,
                                playerAddress = chapter1PlayerAddress,
                                rawVoice = rawVoice,
                                voice = voice,
                            )
                        }
                    }
                    gameViewModel.dinoVisual = DinoVisual.Idle
                    gameViewModel.inputLocked = false
                    return@launch
                }
                if (!immediateCh1Ch2Station4Voice) {
                    GameAudioActions.launchFeedbackVoice(
                        audioEnabled = audioEnabled,
                        scope = scope,
                        audioRuntime = audioRuntime,
                        cancelFeedbackVoice = cancelFeedbackVoice,
                    ) play@{
                        val feedbackDelayMs =
                            when {
                                (chapterId == 1 || chapterId == 2) &&
                                    stationId == Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH ->
                                    0L
                                (chapterId == 3 || chapterId == 6) && stationId == 4 -> 0L
                                else -> 110L
                            }
                        delay(feedbackDelayMs)
                        if (wrongWordCatalogId != null && !wrongWordAlreadySpoken) {
                            if (chapterId == 3 || chapterId == 6) {
                                val resId = AudioClips.wordRawResIdByCatalogId(wrongWordCatalogId)
                                if (resId == null) {
                                    android.util.Log.e(
                                        "MissingContent",
                                        "Missing required wrong-feedback word audio. chapterId=$chapterId stationId=$stationId context=WrongFeedbackActions.trigger(wrongWord,wordThenTryAgain) stage=missing raw word mapping catalogId='$wrongWordCatalogId'",
                                    )
                                    if (rawVoice != null) {
                                        rawVoice.playRawBlocking(0)
                                    } else {
                                        voice.playRequiredBlocking(
                                            assetPath = "",
                                            context = "WrongFeedbackActions.trigger(wrongWord,missingWordMapping,rawVoice=null)",
                                            chapterId = chapterId,
                                            stationId = stationId,
                                        )
                                    }
                                } else if (rawVoice == null) {
                                    android.util.Log.e(
                                        "MissingContent",
                                        "Missing required wrong-feedback word audio. chapterId=$chapterId stationId=$stationId context=WrongFeedbackActions.trigger(wrongWord,wordThenTryAgain) stage=rawVoice=null expectedRawResId=$resId",
                                    )
                                    voice.playRequiredBlocking(
                                        assetPath = "",
                                        context = "WrongFeedbackActions.trigger(wrongWord,rawVoice=null)",
                                        chapterId = chapterId,
                                        stationId = stationId,
                                    )
                                } else {
                                    rawVoice.playRawBlocking(resId)
                                }
                            } else {
                                if (chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5) {
                                    val resId = AudioClips.wordRawResIdByCatalogId(wrongWordCatalogId)
                                    if (resId == null) {
                                        android.util.Log.e(
                                            "MissingContent",
                                            "Missing required wrong-feedback word audio. chapterId=$chapterId stationId=$stationId context=WrongFeedbackActions.trigger(wrongWord,wordThenTryAgain) stage=missing raw word mapping catalogId='$wrongWordCatalogId'",
                                        )
                                        rawVoice?.playRawBlocking(0)
                                    } else if (rawVoice == null) {
                                        android.util.Log.e(
                                            "MissingContent",
                                            "Missing required wrong-feedback word audio. chapterId=$chapterId stationId=$stationId context=WrongFeedbackActions.trigger(wrongWord,wordThenTryAgain) stage=rawVoice=null expectedRawResId=$resId",
                                        )
                                        voice.playRequiredBlocking(
                                            assetPath = "",
                                            context = "WrongFeedbackActions.trigger(wrongWord,rawVoice=null)",
                                            chapterId = chapterId,
                                            stationId = stationId,
                                        )
                                    } else {
                                        rawVoice.playRawBlocking(resId)
                                    }
                                } else {
                                    val wordPath = AudioClips.wordClipByCatalogId(wrongWordCatalogId)
                                    if (voice.hasAsset(wordPath)) {
                                        voice.playBlocking(wordPath)
                                    }
                                }
                            }
                            playTryAgainFallback(
                                chapterId = chapterId,
                                stationId = stationId,
                                playerAddress = chapter1PlayerAddress,
                                rawVoice = rawVoice,
                                voice = voice,
                            )
                            return@play
                        }

                        if (wrongPickedLetter != null) {
                            if (!wrongPickedLetterAlreadySpoken) {
                                if (chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5) {
                                    val resId = AudioClips.letterNameRawResId(wrongPickedLetter)
                                    if (resId == null) {
                                        android.util.Log.e(
                                            "MissingContent",
                                            "Missing required wrong-feedback letter-name audio. chapterId=$chapterId stationId=$stationId context=WrongFeedbackActions.trigger(wrongLetter,letterThenTryAgain) stage=missing raw letter-name mapping wrongPickedLetter='$wrongPickedLetter'",
                                        )
                                        rawVoice?.playRawBlocking(0)
                                    } else if (rawVoice == null) {
                                        android.util.Log.e(
                                            "MissingContent",
                                            "Missing required wrong-feedback letter-name audio. chapterId=$chapterId stationId=$stationId context=WrongFeedbackActions.trigger(wrongLetter,letterThenTryAgain) stage=rawVoice=null expectedRawResId=$resId",
                                        )
                                        voice.playRequiredBlocking(
                                            assetPath = "",
                                            context = "WrongFeedbackActions.trigger(wrongLetter,rawVoice=null)",
                                            chapterId = chapterId,
                                            stationId = stationId,
                                        )
                                    } else {
                                        rawVoice.playRawBlocking(resId)
                                    }
                                } else if (chapterId == 3 || chapterId == 6) {
                                    val resId = AudioClips.letterNameRawResId(wrongPickedLetter)
                                    if (resId == null) {
                                        android.util.Log.e(
                                            "MissingContent",
                                            "Missing required wrong-feedback letter-name audio. chapterId=$chapterId stationId=$stationId context=WrongFeedbackActions.trigger(wrongLetter,letterThenTryAgain) stage=missing raw letter-name mapping wrongPickedLetter='$wrongPickedLetter'",
                                        )
                                        if (rawVoice != null) {
                                            rawVoice.playRawBlocking(0)
                                        } else {
                                            voice.playRequiredBlocking(
                                                assetPath = "",
                                                context = "WrongFeedbackActions.trigger(wrongLetter,missingLetterNameMapping,rawVoice=null)",
                                                chapterId = chapterId,
                                                stationId = stationId,
                                            )
                                        }
                                    } else if (rawVoice == null) {
                                        android.util.Log.e(
                                            "MissingContent",
                                            "Missing required wrong-feedback letter-name audio. chapterId=$chapterId stationId=$stationId context=WrongFeedbackActions.trigger(wrongLetter,letterThenTryAgain) stage=rawVoice=null expectedRawResId=$resId",
                                        )
                                        voice.playRequiredBlocking(
                                            assetPath = "",
                                            context = "WrongFeedbackActions.trigger(wrongLetter,rawVoice=null)",
                                            chapterId = chapterId,
                                            stationId = stationId,
                                        )
                                    } else {
                                        rawVoice.playRawBlocking(resId)
                                    }
                                } else {
                                    val letterName = AudioClips.letterNameClip(wrongPickedLetter)
                                    if (letterName != null && voice.hasAsset(letterName)) {
                                        voice.playBlocking(letterName)
                                    }
                                }
                            }
                            playTryAgainFallback(
                                chapterId = chapterId,
                                stationId = stationId,
                                playerAddress = chapter1PlayerAddress,
                                rawVoice = rawVoice,
                                voice = voice,
                            )
                            return@play
                        }

                        playTryAgainFallback(
                            chapterId = chapterId,
                            stationId = stationId,
                            playerAddress = chapter1PlayerAddress,
                            rawVoice = rawVoice,
                            voice = voice,
                        )
                    }
                }
            }
            gameViewModel.dinoVisual = DinoVisual.Idle
            gameViewModel.inputLocked = false
        }
    }
}
