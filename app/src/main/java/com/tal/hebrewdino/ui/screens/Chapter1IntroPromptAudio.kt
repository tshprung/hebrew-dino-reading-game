package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.companion.Chapter1AddressAwareAudio
import com.tal.hebrewdino.ui.data.PlayerAddress
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.StationTemplateId
import kotlinx.coroutines.delay

private const val Chapter1InstructionToTargetGapMs: Long = 170L

/** Plays Ch.1 address-aware station intro audio when applicable. Returns true if handled. */
internal suspend fun playChapter1AddressAwareIntro(
    chapterId: Int,
    stationId: Int,
    stationTemplateId: StationTemplateId,
    playerAddress: PlayerAddress?,
    sagaUsesPickLetterAudioStaging: Boolean,
    sagaUsesFindGridAudioStaging: Boolean,
    sagaUsesPopBalloonsAudioStaging: Boolean,
    isSagaEpisode: Boolean,
    q: Question,
    rawVoice: RawVoicePlayer,
    voice: VoicePlayer,
    sfx: SoundPoolPlayer,
    station1IntroLetterLeadFraction: Float,
    station1IntroToLetterLeadScale: Float,
    station2BalloonIntroLetterLeadFraction: Float,
    station2IntroToLetterLeadScale: Float,
    station2BalloonIntroToLetterGapBoost: Float,
    station2BalloonIntroToLetterExtraPauseMs: Long,
    station4IntroWordLeadFraction: Float,
    station4IntroToWordLeadScale: Float,
    station4IntroToWordGapBoost: Float,
    station4IntroToWordExtraPauseMs: Long,
): Boolean {
    if ((chapterId != 1 && chapterId != 2 && chapterId != 4 && chapterId != 5) || playerAddress == null) return false
    val kind =
        Chapter1AddressAwareAudio.instructionKindFor(
            stationId = stationId,
            stationTemplateId = stationTemplateId,
            q = q,
        ) ?: return false
    val instructionRaw = Chapter1AddressAwareAudio.instructionRawRes(kind, playerAddress)

    if (sagaUsesPickLetterAudioStaging) {
        val target =
            when (q) {
                is Question.PopBalloonsQuestion -> q.correctAnswer
                is Question.FindLetterGridQuestion -> q.targetLetter
                is Question.PictureStartsWithQuestion -> q.correctLetter
                is Question.ImageMatchQuestion -> q.targetLetter
                is Question.FinaleSlotQuestion -> null
            } ?: return false
        sfx.stopAllStreams()
        rawVoice.playRawBlocking(instructionRaw)
        delay(Chapter1InstructionToTargetGapMs)
        val resId = AudioClips.letterNameRawResId(target)
        if (resId == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playChapter1AddressAwareIntro(sagaUsesPickLetterAudioStaging) stage=missing raw letter-name mapping targetLetter='$target'",
            )
            rawVoice.playRawBlocking(0)
        } else {
            rawVoice.playRawBlocking(resId)
        }
        return true
    }

    if (sagaUsesFindGridAudioStaging && q is Question.FindLetterGridQuestion) {
        sfx.stopAllStreams()
        rawVoice.playRawBlocking(instructionRaw)
        delay(Chapter1InstructionToTargetGapMs)
        val resId = AudioClips.letterNameRawResId(q.targetLetter)
        if (resId == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playChapter1AddressAwareIntro(sagaUsesFindGridAudioStaging) stage=missing raw letter-name mapping targetLetter='${q.targetLetter}'",
            )
            rawVoice.playRawBlocking(0)
        } else {
            rawVoice.playRawBlocking(resId)
        }
        return true
    }

    if (stationTemplateId == StationTemplateId.ImageMatch &&
        stationId == Chapter1StationOrder.PICTURE_PICK_ALL &&
        q is Question.ImageMatchQuestion
    ) {
        sfx.stopAllStreams()
        rawVoice.playRawBlocking(instructionRaw)
        delay(Chapter1InstructionToTargetGapMs)
        val resId = AudioClips.letterNameRawResId(q.targetLetter)
        if (resId == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playChapter1AddressAwareIntro(ImageMatchStation5) stage=missing raw letter-name mapping targetLetter='${q.targetLetter}'",
            )
            rawVoice.playRawBlocking(0)
        } else {
            rawVoice.playRawBlocking(resId)
        }
        return true
    }

    if (stationTemplateId == StationTemplateId.MatchLetterToWord && q is Question.ImageMatchQuestion) {
        sfx.stopAllStreams()
        rawVoice.playRawBlocking(instructionRaw)
        return true
    }

    if (isSagaEpisode && stationId == Chapter1StationOrder.PICTURE_PICK_ONE && q is Question.PictureStartsWithQuestion) {
        sfx.stopAllStreams()
        rawVoice.playRawBlocking(instructionRaw)
        delay(Chapter1InstructionToTargetGapMs)
        val wordPath = AudioClips.wordClipByCatalogId(q.catalogEntryId)
        voice.playRequiredBlocking(
            assetPath = wordPath,
            context = "playChapter1AddressAwareIntro(Station4Word)",
            chapterId = chapterId,
            stationId = stationId,
        )
        return true
    }

    if (sagaUsesPopBalloonsAudioStaging && q is Question.PopBalloonsQuestion) {
        sfx.stopAllStreams()
        rawVoice.playRawBlocking(instructionRaw)
        delay(Chapter1InstructionToTargetGapMs)
        val resId = AudioClips.letterNameRawResId(q.correctAnswer)
        if (resId == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playChapter1AddressAwareIntro(PopBalloons) stage=missing raw letter-name mapping targetLetter='${q.correctAnswer}'",
            )
            rawVoice.playRawBlocking(0)
            return true
        }
        rawVoice.playRawBlocking(resId)
        return true
    }

    return false
}
