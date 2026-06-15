package com.tal.hebrewdino.ui.screens



import com.tal.hebrewdino.ui.audio.AudioClips

import com.tal.hebrewdino.ui.audio.InStationPraiseAudio

import com.tal.hebrewdino.ui.audio.RawVoicePlayer

import com.tal.hebrewdino.ui.audio.SoundPoolPlayer

import com.tal.hebrewdino.ui.domain.AnswerResult

import com.tal.hebrewdino.ui.domain.LevelSession

import com.tal.hebrewdino.ui.domain.Season1StationAudio

import kotlinx.coroutines.CoroutineScope

import kotlinx.coroutines.Job

import kotlinx.coroutines.delay

import kotlinx.coroutines.launch

import kotlin.time.Duration.Companion.milliseconds



internal object DragWordToPictureActions {

    /**

     * @return true when [wordCatalogId] locks onto [pictureCatalogId]; false on wrong target (try-again only).

     */

    fun handleDropAttempt(

        wordCatalogId: String,

        pictureCatalogId: String,

        gameViewModel: GameViewModel,

        cancelFeedbackVoice: () -> Unit,

        audioEnabled: Boolean,

        sfx: SoundPoolPlayer,

        session: LevelSession,

        scope: CoroutineScope,

        chapterId: Int,

        stationId: Int,

        rawVoice: RawVoicePlayer?,

        audioRuntime: GameAudioRuntimeState,

        onWrongFeedback: () -> Job?,

    ): Boolean {

        if (gameViewModel.dragWordRoundCompleting) return false

        if (!gameViewModel.consumeTapCooldown(minIntervalMs = 90L)) return false

        cancelFeedbackVoice()

        if (!session.validateDragWordToPicturePlacement(wordCatalogId, pictureCatalogId)) {

            scope.launch {

                if (audioEnabled) {

                    sfx.playFirstAvailable(AudioClips.SfxWrong, volume = 0.55f)

                }

                val feedbackJob = onWrongFeedback()

                GameAudioActions.joinSilently(feedbackJob)

                delay(280.milliseconds)

            }

            return false

        }

        if (audioEnabled) {

            scope.launch {

                GameAudioActions.launchFeedbackVoiceNoCancel(

                    audioEnabled = true,

                    scope = scope,

                    audioRuntime = audioRuntime,

                ) {

                    Season1StationAudio.playDragWordToPictureWord(

                        rawVoice = rawVoice,

                        catalogEntryId = wordCatalogId,

                        chapterId = chapterId,

                        stationId = stationId,

                        context = "DragWordToPictureActions.handleDropAttempt",

                    )

                }

            }

        }

        return true

    }



    fun handleRoundComplete(

        gameViewModel: GameViewModel,

        audioEnabled: Boolean,

        session: LevelSession,

        scope: CoroutineScope,

        chapterId: Int,

        stationId: Int,

        rawVoice: RawVoicePlayer?,

        sfx: SoundPoolPlayer,

        audioRuntime: GameAudioRuntimeState,

        advanceAfterRound: suspend (Boolean) -> Unit,

    ) {

        if (gameViewModel.dragWordRoundCompleting) return

        gameViewModel.dragWordRoundCompleting = true

        gameViewModel.inputLocked = true

        scope.launch {

            if (audioEnabled) {

                if (Season1StationAudio.isDragWordToPictureBehaviorStation(chapterId, stationId)) {

                    GameAudioActions.awaitFeedbackVoice(audioRuntime, 10_000L)

                    GameAudioActions.launchFeedbackVoiceNoCancel(

                        audioEnabled = true,

                        scope = scope,

                        audioRuntime = audioRuntime,

                    ) {

                        val avoidPraise =

                            InStationPraiseAudio.rawResIdFromTrackingKey(audioRuntime.lastPraiseAssetPath)

                        val praiseRes =

                            Season1StationAudio.playDragWordToPictureRoundCompleteFeedback(

                                rawVoice = rawVoice,

                                avoidPraiseRawResId = avoidPraise,

                            )

                        if (praiseRes != null) {

                            audioRuntime.lastPraiseAssetPath = InStationPraiseAudio.trackingKey(praiseRes)

                        } else {

                            sfx.playFirstAvailable(AudioClips.SfxCorrect, volume = 0.58f)

                        }

                    }

                    GameAudioActions.awaitFeedbackVoice(audioRuntime, 10_000L)

                } else {

                    sfx.playFirstAvailable(AudioClips.SfxCorrect, volume = 0.58f)

                    delay(280.milliseconds)

                }

            }

            when (session.completeDragWordToPictureRound()) {

                AnswerResult.Correct -> {

                    val isLast = session.currentIndex >= session.totalQuestions - 1

                    advanceAfterRound(isLast)

                }

                else -> {

                    gameViewModel.dragWordRoundCompleting = false

                    gameViewModel.inputLocked = false

                }

            }

        }

    }

}


