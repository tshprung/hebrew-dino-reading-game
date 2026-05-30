package com.tal.hebrewdino.ui.companion

import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.data.PlayerAddress
import com.tal.hebrewdino.R
import kotlinx.coroutines.delay

/** Season 1 Ch.1 address-aware wrong-answer feedback (try again). */
internal suspend fun playAddressAwareTryAgainBlocking(
    chapterId: Int,
    playerAddress: PlayerAddress?,
    rawVoice: RawVoicePlayer?,
    voice: VoicePlayer,
) {
    if (chapterId == 1 && rawVoice != null) {
        if (playerAddress != null) {
            rawVoice.playRawBlocking(Chapter1AddressAwareAudio.tryAgainRawRes(playerAddress))
        } else {
            rawVoice.playRawBlocking(R.raw.vo_try_again_neutral)
        }
    } else {
        voice.playFirstAvailableBlocking(AudioClips.VoTryAgain2, AudioClips.VoTryAgain1)
    }
}

internal suspend fun playLetterThenAddressAwareTryAgain(
    sfx: SoundPoolPlayer,
    letterClip: String,
    letterMs: Long,
    followLeadFrac: Float,
    chapterId: Int,
    playerAddress: PlayerAddress?,
    rawVoice: RawVoicePlayer?,
    voice: VoicePlayer,
) {
    sfx.stopAllStreams()
    sfx.playReturningStreamId(letterClip, volume = 1f)
    val lead =
        (letterMs * followLeadFrac)
            .toLong()
            .coerceIn(16L, letterMs)
    delay(lead)
    playAddressAwareTryAgainBlocking(
        chapterId = chapterId,
        playerAddress = playerAddress,
        rawVoice = rawVoice,
        voice = voice,
    )
}
