package com.tal.hebrewdino.ui.domain

import android.util.Log
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.domain.Question.MissingFirstLetterQuestion
import com.tal.hebrewdino.ui.domain.Question.RhymingQuestion
import com.tal.hebrewdino.ui.domain.Question.WordPartsQuestion

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

    fun instructionAssetPath(
        mode: Season2AdvancedStationMode,
        wordPartsPresentationMode: Season2WordPartsPresentationMode? = null,
    ): String =
        when (mode) {
            Season2AdvancedStationMode.PictureToWord -> AudioClips.Season2PictureToWordInstructions
            Season2AdvancedStationMode.MissingFirstLetter -> AudioClips.Season2MissingFirstLetterInstructions
            Season2AdvancedStationMode.WordParts ->
                when (wordPartsPresentationMode) {
                    Season2WordPartsPresentationMode.HiddenWordPartsChallenge ->
                        AudioClips.Season2WordPartsHiddenSplitInstructions
                    else -> AudioClips.Season2WordPartsChooseSplitInstructions
                }
            Season2AdvancedStationMode.Rhyming -> AudioClips.Season2RhymingInstructions
        }

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
        val wordResId = AudioClips.wordRawResIdByCatalogId(catalogId)
        if (wordResId == null) {
            Log.e(
                MISSING_TAG,
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId " +
                    "context=Season2StationAudio.speakPictureToWordRoundPrompt stage=missing raw word mapping " +
                    "catalogId='$catalogId'",
            )
            rawVoice.playRawBlocking(0)
            return
        }
        rawVoice.playRawBlocking(wordResId)
    }

    suspend fun speakAdvancedModeInstruction(
        mode: Season2AdvancedStationMode,
        chapterId: Int,
        stationId: Int,
        voice: VoicePlayer,
        rawVoice: RawVoicePlayer?,
        wordPartsPresentationMode: Season2WordPartsPresentationMode? = null,
    ) {
        when (mode) {
            Season2AdvancedStationMode.PictureToWord -> {
                if (rawVoice == null) {
                    Log.e(
                        MISSING_TAG,
                        "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId " +
                            "context=Season2StationAudio.speakAdvancedModeInstruction(PictureToWord) " +
                            "stage=rawVoice=null expectedInstructionRawRes=${R.raw.instruction_image_to_word}",
                    )
                    return
                }
                rawVoice.playRawBlocking(R.raw.instruction_image_to_word)
            }
            else -> {
                val path = instructionAssetPath(mode, wordPartsPresentationMode)
                if (!voice.hasAsset(path)) {
                    Log.e(
                        MISSING_TAG,
                        "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId " +
                            "context=Season2StationAudio.speakAdvancedModeInstruction($mode) " +
                            "stage=missing asset path='$path'",
                    )
                } else {
                    voice.playFirstAvailableBlocking(path)
                }
            }
        }
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
                val wordPartsMode =
                    (q as? WordPartsQuestion)?.presentationMode
                speakAdvancedModeInstruction(
                    mode = mode,
                    chapterId = chapterId,
                    stationId = stationId,
                    voice = voice,
                    rawVoice = rawVoice,
                    wordPartsPresentationMode = wordPartsMode,
                )
                if (rawVoice == null) return
                val catalogId =
                    when (q) {
                        is MissingFirstLetterQuestion -> q.catalogEntryId
                        is WordPartsQuestion -> q.catalogEntryId
                        is RhymingQuestion -> q.targetCatalogEntryId
                        else -> return
                    }
                val wordResId = AudioClips.wordRawResIdByCatalogId(catalogId)
                if (wordResId == null) {
                    Log.e(
                        MISSING_TAG,
                        "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId " +
                            "context=Season2StationAudio.speakAdvancedRoundPrompt stage=missing raw word mapping " +
                            "catalogId='$catalogId'",
                    )
                    rawVoice.playRawBlocking(0)
                    return
                }
                rawVoice.playRawBlocking(wordResId)
            }
            else -> Unit
        }
    }
}
