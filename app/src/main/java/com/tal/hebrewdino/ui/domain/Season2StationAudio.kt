package com.tal.hebrewdino.ui.domain

import android.util.Log
import androidx.annotation.RawRes
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.Season2RawAudio
import com.tal.hebrewdino.ui.audio.Season2WordPartsAudio
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.domain.Question.MissingFirstLetterQuestion
import com.tal.hebrewdino.ui.domain.Question.RhymingQuestion
import com.tal.hebrewdino.ui.domain.Question.WordPartsQuestion
import kotlinx.coroutines.delay

/** Season 2 gameplay audio routing (chapter IDs 101–106). */
object Season2StationAudio {
    private const val MISSING_TAG = "MissingContent"

    fun isSeason2GameplayChapter(chapterId: Int): Boolean =
        chapterId in Season2ChapterIds.Chapter1Tyrannosaurus..Season2ChapterIds.Chapter6Mosasaurus

    /** Chapters 3–6 gameplay IDs — six-station arc with address-aware warmup audio. */
    fun isSeason2WarmupChapter(chapterId: Int): Boolean =
        chapterId in Season2ChapterIds.Chapter3Stegosaurus..Season2ChapterIds.Chapter6Mosasaurus

    fun usesChapter1StyleAddressAwareIntro(chapterId: Int): Boolean =
        chapterId in listOf(1, 2, 4, 5) || isSeason2GameplayChapter(chapterId)

    fun usesPictureStartsWithAddressAwareIntro(
        chapterId: Int,
        isSagaEpisodeParam: Boolean,
    ): Boolean = isSagaEpisodeParam || isSeason2WarmupChapter(chapterId)

    fun isPictureToWordStation(
        chapterId: Int,
        stationId: Int,
    ): Boolean {
        if ((chapterId == 3 || chapterId == 6) && stationId == 6) return true
        if (chapterId == Season2ChapterIds.Chapter1Tyrannosaurus && stationId == Season2Chapter1StationOrder.FINALE_STATION) {
            return true
        }
        if (chapterId == Season2ChapterIds.Chapter4Brachiosaurus && stationId == 5) return true
        if (chapterId == Season2ChapterIds.Chapter6Mosasaurus && stationId == 6) return true
        return false
    }

    fun isSeason2ImageToWordLayout(chapterId: Int, stationId: Int): Boolean =
        (chapterId == Season2ChapterIds.Chapter1Tyrannosaurus && stationId == Season2Chapter1StationOrder.FINALE_STATION) ||
            (chapterId == Season2ChapterIds.Chapter4Brachiosaurus && stationId == 5) ||
            (chapterId == Season2ChapterIds.Chapter6Mosasaurus && stationId == 6)

    fun usesImageToWordRawWordClips(chapterId: Int): Boolean =
        chapterId == 3 ||
            chapterId == 6 ||
            chapterId == Season2ChapterIds.Chapter1Tyrannosaurus ||
            isSeason2ImageToWordLayout(chapterId, stationId = 5) ||
            isSeason2ImageToWordLayout(chapterId, stationId = 6) ||
            chapterId == TrainingV1Config.CHAPTER_ID

    fun advancedModeForQuestion(q: Question): Season2AdvancedStationMode? =
        when (q) {
            is MissingFirstLetterQuestion -> Season2AdvancedStationMode.MissingFirstLetter
            is WordPartsQuestion -> Season2AdvancedStationMode.WordParts
            is RhymingQuestion -> Season2AdvancedStationMode.Rhyming
            is Question.ImageMatchQuestion -> null
            else -> null
        }

    @RawRes
    fun instructionRawResId(
        mode: Season2AdvancedStationMode,
        wordPartsPresentationMode: Season2WordPartsPresentationMode? = null,
    ): Int = Season2RawAudio.instructionRawResId(mode, wordPartsPresentationMode)

    suspend fun speakPictureToWordRoundPrompt(
        chapterId: Int,
        stationId: Int,
        catalogId: String,
        rawVoice: RawVoicePlayer?,
        voice: VoicePlayer,
    ) {
        if (!isPictureToWordStation(chapterId, stationId)) return
        if (rawVoice == null) {
            Log.e(
                MISSING_TAG,
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId " +
                    "context=Season2StationAudio.speakPictureToWordRoundPrompt stage=rawVoice=null " +
                    "expectedInstructionRawRes=${R.raw.instruction_image_to_word}",
            )
            voice.playRequiredBlocking(
                assetPath = "",
                context = "Season2StationAudio.speakPictureToWordRoundPrompt(rawVoice=null)",
                chapterId = chapterId,
                stationId = stationId,
            )
            return
        }
        rawVoice.playRawBlocking(R.raw.instruction_image_to_word)
        playWordRawOrLog(
            catalogId = catalogId,
            rawVoice = rawVoice,
            chapterId = chapterId,
            stationId = stationId,
            context = "Season2StationAudio.speakPictureToWordRoundPrompt",
        )
    }

    suspend fun replayPictureToWordTargetWordOnly(
        chapterId: Int,
        stationId: Int,
        catalogId: String,
        rawVoice: RawVoicePlayer?,
        voice: VoicePlayer,
    ) {
        if (!isPictureToWordStation(chapterId, stationId)) return
        if (rawVoice == null) {
            Log.e(
                MISSING_TAG,
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId " +
                    "context=Season2StationAudio.replayPictureToWordTargetWordOnly stage=rawVoice=null",
            )
            voice.playRequiredBlocking(
                assetPath = "",
                context = "Season2StationAudio.replayPictureToWordTargetWordOnly(rawVoice=null)",
                chapterId = chapterId,
                stationId = stationId,
            )
            return
        }
        playWordRawOrLog(
            catalogId = catalogId,
            rawVoice = rawVoice,
            chapterId = chapterId,
            stationId = stationId,
            context = "Season2StationAudio.replayPictureToWordTargetWordOnly",
        )
    }

    suspend fun replayPictureToWordCoachInstructionAndWord(
        chapterId: Int,
        stationId: Int,
        catalogId: String,
        rawVoice: RawVoicePlayer,
    ) {
        if (!isPictureToWordStation(chapterId, stationId)) return
        rawVoice.playRawBlocking(R.raw.instruction_image_to_word)
        delay(Season2Ch1QaPolicy.CoachInstructionToWordGapMs)
        playWordRawOrLog(
            catalogId = catalogId,
            rawVoice = rawVoice,
            chapterId = chapterId,
            stationId = stationId,
            context = "Season2StationAudio.replayPictureToWordCoachInstructionAndWord",
        )
    }

    suspend fun speakAdvancedModeInstruction(
        mode: Season2AdvancedStationMode,
        chapterId: Int,
        stationId: Int,
        rawVoice: RawVoicePlayer?,
        wordPartsPresentationMode: Season2WordPartsPresentationMode? = null,
    ) {
        val instructionRes = instructionRawResId(mode, wordPartsPresentationMode)
        if (rawVoice == null) {
            Log.e(
                MISSING_TAG,
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId " +
                    "context=Season2StationAudio.speakAdvancedModeInstruction($mode) " +
                    "stage=rawVoice=null expectedInstructionRawRes=$instructionRes",
            )
            return
        }
        rawVoice.playRawBlocking(instructionRes)
    }

    suspend fun speakAdvancedRoundPrompt(
        chapterId: Int,
        stationId: Int,
        q: Question,
        voice: VoicePlayer,
        rawVoice: RawVoicePlayer?,
    ) {
        when (q) {
            is Question.ImageMatchQuestion -> {
                if (isPictureToWordStation(chapterId, stationId)) {
                    speakPictureToWordRoundPrompt(
                        chapterId = chapterId,
                        stationId = stationId,
                        catalogId = q.correctChoiceId,
                        rawVoice = rawVoice,
                        voice = voice,
                    )
                }
            }
            is MissingFirstLetterQuestion,
            is WordPartsQuestion,
            is RhymingQuestion,
            -> {
                val mode = advancedModeForQuestion(q) ?: return
                val wordPartsMode = (q as? WordPartsQuestion)?.presentationMode
                speakAdvancedModeInstruction(
                    mode = mode,
                    chapterId = chapterId,
                    stationId = stationId,
                    rawVoice = rawVoice,
                    wordPartsPresentationMode = wordPartsMode,
                )
                if (rawVoice == null) return
                val catalogId =
                    when (q) {
                        is MissingFirstLetterQuestion -> q.catalogEntryId
                        is WordPartsQuestion -> q.catalogEntryId
                        is RhymingQuestion -> q.targetCatalogEntryId
                    }
                playWordRawOrLog(
                    catalogId = catalogId,
                    rawVoice = rawVoice,
                    chapterId = chapterId,
                    stationId = stationId,
                    context = "Season2StationAudio.speakAdvancedRoundPrompt",
                )
                if (q is WordPartsQuestion) {
                    Season2WordPartsAudio.playPartsSequence(
                        catalogId = catalogId,
                        rawVoice = rawVoice,
                        chapterId = chapterId,
                        stationId = stationId,
                    )
                }
            }
            else -> Unit
        }
    }

    suspend fun replayAdvancedInstructionAndWord(
        q: Question,
        chapterId: Int,
        stationId: Int,
        rawVoice: RawVoicePlayer?,
    ) {
        val mode = advancedModeForQuestion(q) ?: return
        val wordPartsMode = (q as? WordPartsQuestion)?.presentationMode
        speakAdvancedModeInstruction(
            mode = mode,
            chapterId = chapterId,
            stationId = stationId,
            rawVoice = rawVoice,
            wordPartsPresentationMode = wordPartsMode,
        )
        if (rawVoice == null) return
        val catalogId =
            when (q) {
                is MissingFirstLetterQuestion -> q.catalogEntryId
                is WordPartsQuestion -> q.catalogEntryId
                is RhymingQuestion -> q.targetCatalogEntryId
                is Question.ImageMatchQuestion -> q.correctChoiceId
                else -> null
            } ?: return
        playWordRawOrLog(
            catalogId = catalogId,
            rawVoice = rawVoice,
            chapterId = chapterId,
            stationId = stationId,
            context = "Season2StationAudio.replayAdvancedInstructionAndWord",
        )
        if (q is WordPartsQuestion) {
            Season2WordPartsAudio.playPartsSequence(
                catalogId = catalogId,
                rawVoice = rawVoice,
                chapterId = chapterId,
                stationId = stationId,
            )
        }
    }

    private suspend fun playWordRawOrLog(
        catalogId: String,
        rawVoice: RawVoicePlayer,
        chapterId: Int,
        stationId: Int,
        context: String,
    ) {
        val wordResId = AudioClips.wordRawResIdByCatalogId(catalogId)
        if (wordResId == null) {
            Log.e(
                MISSING_TAG,
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId " +
                    "context=$context stage=missing raw word mapping catalogId='$catalogId'",
            )
            rawVoice.playRawBlocking(0)
            return
        }
        rawVoice.playRawBlocking(wordResId)
    }
}
