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
    stationId: Int? = null,
    playerAddress: PlayerAddress?,
    rawVoice: RawVoicePlayer?,
    voice: VoicePlayer,
    context: String = "playAddressAwareTryAgainBlocking",
) {
    val requiredChapter = chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5
    if (requiredChapter) {
        if (rawVoice == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required try-again feedback audio. chapterId=$chapterId stationId=$stationId context=$context stage=rawVoice=null expectedRawRes=${if (playerAddress != null) "Chapter1AddressAwareAudio.tryAgainRawRes($playerAddress)" else "R.raw.vo_try_again_neutral"}",
            )
            voice.playRequiredBlocking(
                assetPath = "",
                context = "$context(rawVoice=null)",
                chapterId = chapterId,
                stationId = stationId,
            )
            return
        }
        if (playerAddress != null) {
            rawVoice.playRawBlocking(Chapter1AddressAwareAudio.tryAgainRawRes(playerAddress))
        } else {
            rawVoice.playRawBlocking(R.raw.vo_try_again_neutral)
        }
        return
    }
    voice.playFirstAvailableBlocking(AudioClips.VoTryAgain2, AudioClips.VoTryAgain1)
}

internal suspend fun playLetterThenAddressAwareTryAgain(
    sfx: SoundPoolPlayer,
    letterClip: String,
    letterMs: Long,
    followLeadFrac: Float,
    chapterId: Int,
    stationId: Int? = null,
    playerAddress: PlayerAddress?,
    rawVoice: RawVoicePlayer?,
    voice: VoicePlayer,
    context: String = "playLetterThenAddressAwareTryAgain",
) {
    sfx.stopAllStreams()
    if (chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5) {
        sfx.playRequiredReturningStreamId(
            assetPath = letterClip,
            volume = 1f,
            context = "$context(letter)",
            chapterId = chapterId,
            stationId = stationId,
        )
    } else {
        sfx.playReturningStreamId(letterClip, volume = 1f)
    }
    val lead =
        (letterMs * followLeadFrac)
            .toLong()
            .coerceIn(16L, letterMs)
    delay(lead)
    playAddressAwareTryAgainBlocking(
        chapterId = chapterId,
        stationId = stationId,
        playerAddress = playerAddress,
        rawVoice = rawVoice,
        voice = voice,
        context = "$context(tryAgain)",
    )
}
