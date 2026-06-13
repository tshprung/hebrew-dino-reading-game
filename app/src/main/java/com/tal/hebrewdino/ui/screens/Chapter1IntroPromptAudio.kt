package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.companion.Chapter1AddressAwareAudio
import com.tal.hebrewdino.ui.data.PlayerAddress
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.Season2StationAudio
import com.tal.hebrewdino.ui.domain.StationTemplateId
import com.tal.hebrewdino.ui.domain.TrainingV1SourceStation
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private const val Chapter1InstructionToTargetGapMs: Long = 170L
private const val Chapter4PictureStartsWithInstructionToWordGapMs: Long = 60L

private fun instructionToTargetGapMs(
    chapterId: Int,
    stationId: Int,
    stationTemplateId: StationTemplateId,
): Long {
    if (
        chapterId == 4 &&
        stationId == Chapter1StationOrder.PICTURE_PICK_ONE &&
        stationTemplateId == StationTemplateId.PictureStartsWith
    ) {
        return Chapter4PictureStartsWithInstructionToWordGapMs
    }
    return Chapter1InstructionToTargetGapMs
}

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
    sfx: SoundPoolPlayer,
): Boolean {
    if (!Season2StationAudio.usesChapter1StyleAddressAwareIntro(chapterId) || playerAddress == null) return false
    val (promptChapterId, promptStationId) = TrainingV1SourceStation.resolve(chapterId, stationId)
    val kind =
        Chapter1AddressAwareAudio.instructionKindFor(
            stationId = promptStationId,
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
                is Question.MissingFirstLetterQuestion -> q.correctLetter
                is Question.WordPartsQuestion -> q.word.first().toString()
                is Question.RhymingQuestion -> q.targetWord.first().toString()
                is Question.DragWordToPictureQuestion -> null
                is Question.DragMissingLetterQuestion -> q.correctLetter
            } ?: return false
        sfx.stopAllStreams()
        rawVoice.playRawBlocking(instructionRaw)
        delay(instructionToTargetGapMs(promptChapterId, promptStationId, stationTemplateId).milliseconds)
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
        delay(instructionToTargetGapMs(promptChapterId, promptStationId, stationTemplateId).milliseconds)
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

    if (stationTemplateId == StationTemplateId.ImageMatch && q is Question.ImageMatchQuestion) {
        sfx.stopAllStreams()
        rawVoice.playRawBlocking(instructionRaw)
        delay(instructionToTargetGapMs(promptChapterId, promptStationId, stationTemplateId).milliseconds)
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

    if (
        Season2StationAudio.usesPictureStartsWithAddressAwareIntro(chapterId, isSagaEpisode) &&
        stationTemplateId == StationTemplateId.PictureStartsWith &&
        q is Question.PictureStartsWithQuestion
    ) {
        sfx.stopAllStreams()
        rawVoice.playRawBlocking(instructionRaw)
        delay(instructionToTargetGapMs(promptChapterId, promptStationId, stationTemplateId).milliseconds)
        val wordResId = AudioClips.wordRawResIdByCatalogId(q.catalogEntryId)
        if (wordResId == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playChapter1AddressAwareIntro(Station4Word) stage=missing raw word mapping catalogId='${q.catalogEntryId}'",
            )
            rawVoice.playRawBlocking(0)
            return true
        }
        rawVoice.playRawBlocking(wordResId)
        return true
    }

    if (sagaUsesPopBalloonsAudioStaging && q is Question.PopBalloonsQuestion) {
        sfx.stopAllStreams()
        rawVoice.playRawBlocking(instructionRaw)
        delay(instructionToTargetGapMs(promptChapterId, promptStationId, stationTemplateId).milliseconds)
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
