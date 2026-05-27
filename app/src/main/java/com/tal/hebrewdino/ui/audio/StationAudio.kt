package com.tal.hebrewdino.ui.audio

import com.tal.hebrewdino.ui.domain.ChallengeType
import kotlinx.coroutines.withTimeoutOrNull

object StationAudio {
    private const val INSTRUCTION_TIMEOUT_MS: Long = 12_000L
    private const val SUCCESS_TIMEOUT_MS: Long = 4_000L
    suspend fun playStationInstructions(
        rawVoice: RawVoicePlayer,
        challengeType: ChallengeType,
    ) {
        val resId = StationMediaClips.instructionsResId(challengeType)
        if (resId == 0 || !StationVoiceGuide.isRealRawClip(rawVoice, resId)) return
        withTimeoutOrNull(INSTRUCTION_TIMEOUT_MS) {
            rawVoice.playBlocking(resId)
        }
    }

    suspend fun playStation3Instructions(rawVoice: RawVoicePlayer) {
        if (!StationVoiceGuide.isRealRawClip(rawVoice, StationMediaClips.station3InstructionsResId)) return
        withTimeoutOrNull(INSTRUCTION_TIMEOUT_MS) {
            rawVoice.playBlocking(StationMediaClips.station3InstructionsResId)
        }
    }

    suspend fun playSuccess(
        rawVoice: RawVoicePlayer,
        sfx: SfxManager,
    ) {
        rawVoice.stopNow()
        val played =
            withTimeoutOrNull(SUCCESS_TIMEOUT_MS) {
                if (StationVoiceGuide.isRealRawClip(rawVoice, StationMediaClips.successResId)) {
                    rawVoice.playBlocking(StationMediaClips.successResId)
                    true
                } else {
                    false
                }
            } == true
        if (!played) {
            sfx.playCorrect()
        }
    }

    suspend fun playSoftError(
        rawVoice: RawVoicePlayer,
        sfx: SfxManager,
    ) {
        rawVoice.stopNow()
        if (StationVoiceGuide.isRealRawClip(rawVoice, StationMediaClips.errorResId)) {
            rawVoice.playSoftFeedback(StationMediaClips.errorResId)
        } else {
            sfx.playWrong()
        }
    }
}
