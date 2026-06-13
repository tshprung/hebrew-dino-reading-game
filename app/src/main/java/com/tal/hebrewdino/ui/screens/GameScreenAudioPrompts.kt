package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.companion.Chapter1AddressAwareAudio
import com.tal.hebrewdino.ui.data.PlayerAddress
import com.tal.hebrewdino.ui.domain.Season2ChapterIds
import com.tal.hebrewdino.ui.domain.Season2StationAudio
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.SixStationArcQaPolicy
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.TrainingV1Config
import kotlinx.coroutines.delay
import kotlin.random.Random

/** Matches [SixStationArcChapterRange] in GameScreen for saga prompt branching only. */
private val SagaChapterRangeForAudioPrompts = 1..5

private fun isSagaEpisodeForPrompt(chapterId: Int): Boolean =
    chapterId in SagaChapterRangeForAudioPrompts || chapterId == Season2ChapterIds.Chapter1Tyrannosaurus

/** Station 3 find-letter intro: stretch the intro→letter delay by this factor (e.g. 1.10 = 10% more space before the letter). */
private const val Station3IntroToLetterLeadStretchDefault = 1.10f
/** Episode 4 station 3 feedback: add ~20% more space before the letter (vs the default saga overlap). */
private const val Station3IntroToLetterLeadStretchEpisode4 = 1.20f
/** Station 3 intro on SoundPool: if [SoundPoolPlayer.durationMs] is 0, wait this long so the line is audible. */
private const val Station3InstructionFallbackDurationMs = 1300L
/**
 * Episode 1 station 5: start the letter name this far into `find_word_starts_with_letter` (overlap).
 * Same **25% shorter tail** target as the station 4 intro word lead fraction (was sequential full intro before letter).
 */
private const val Station5WhichWordIntroLetterLeadFraction = 0.775f
/**
 * Halves the SoundPool wait before the letter after [WhichWordStartsWithLetter] (shorter gap before letter name).
 */
private const val Station5WhichWordIntroToLetterLeadScale = 0.5f
/** Adds this fraction of `(1 - baseLead)` before the letter, where `baseLead` = [Station5WhichWordIntroLetterLeadFraction] × [Station5WhichWordIntroToLetterLeadScale]. */
private const val Station5WhichWordIntroToLetterGapBoost = 0.50f
/** Fixed extra pause after that lead before the letter name clip (ms). */
private const val Station5WhichWordIntroToLetterExtraPauseMs = 500L

/** Chapter 1 station 3 / Episode 3 station 1: find-grid intro (letter name on SoundPool overlap). */
internal suspend fun playSagaFindGridIntroSoundPool(
    sfx: SoundPoolPlayer,
    voice: VoicePlayer,
    q: Question.FindLetterGridQuestion,
    chapterId: Int,
    stationId: Int,
    introLetterLeadFraction: Float,
    playerAddress: PlayerAddress? = null,
    rawVoice: RawVoicePlayer? = null,
) {
    sfx.stopAllStreams()
    val requiredPrompt =
        chapterId == 1 ||
            chapterId == 2 ||
            chapterId == 3 ||
            chapterId == 4 ||
            chapterId == 5 ||
            chapterId == 6 ||
            chapterId == TrainingV1Config.CHAPTER_ID
    if (!requiredPrompt) return
    val letterResId = AudioClips.letterNameRawResId(q.targetLetter)
    if (letterResId == null) {
        android.util.Log.e(
            "MissingContent",
            "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=GameScreenAudioPrompts.playSagaFindGridIntroSoundPool stage=missing raw letter-name mapping targetLetter='${q.targetLetter}'",
        )
        rawVoice?.playRawBlocking(0)
        return
    }
    if (rawVoice == null) {
        android.util.Log.e(
            "MissingContent",
            "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=GameScreenAudioPrompts.playSagaFindGridIntroSoundPool stage=rawVoice=null expectedInstructionRawRes!=null expectedLetterRawResId=$letterResId",
        )
        voice.playRequiredBlocking(
            assetPath = "",
            context = "GameScreenAudioPrompts.playSagaFindGridIntroSoundPool(rawVoice=null)",
            chapterId = chapterId,
            stationId = stationId,
        )
        return
    }
    if (playerAddress == null) {
        android.util.Log.e(
            "MissingContent",
            "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=GameScreenAudioPrompts.playSagaFindGridIntroSoundPool stage=playerAddress=null expectedInstructionKind=FindLetter",
        )
        rawVoice.playRawBlocking(0)
        return
    }
    rawVoice.playRawBlocking(
        Chapter1AddressAwareAudio.instructionRawRes(
            kind = Chapter1AddressAwareAudio.InstructionKind.FindLetter,
            address = playerAddress,
        ),
    )
    rawVoice.playRawBlocking(letterResId)
}

/** Episode 4 station 3 replay ("שוב"): target letter name only (no find-grid intro). */
internal suspend fun playEpisode4FindGridReplayLetterOnly(
    sfx: SoundPoolPlayer,
    voice: VoicePlayer,
    q: Question.FindLetterGridQuestion,
    chapterId: Int,
    stationId: Int,
    rawVoice: RawVoicePlayer? = null,
) {
    sfx.stopAllStreams()
    if (chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5) {
        val letterResId = AudioClips.letterNameRawResId(q.targetLetter)
        if (letterResId == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required replay letter-name audio. chapterId=$chapterId stationId=$stationId context=GameScreenAudioPrompts.playEpisode4FindGridReplayLetterOnly stage=missing raw letter-name mapping targetLetter='${q.targetLetter}'",
            )
            rawVoice?.playRawBlocking(0)
            return
        }
        if (rawVoice == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required replay letter-name audio. chapterId=$chapterId stationId=$stationId context=GameScreenAudioPrompts.playEpisode4FindGridReplayLetterOnly stage=rawVoice=null expectedRawResId=$letterResId",
            )
            voice.playRequiredBlocking(
                assetPath = "",
                context = "GameScreenAudioPrompts.playEpisode4FindGridReplayLetterOnly(rawVoice=null)",
                chapterId = chapterId,
                stationId = stationId,
            )
            return
        }
        rawVoice.playRawBlocking(letterResId)
        return
    }
    val letter = AudioClips.letterNameClip(q.targetLetter)
    if (letter != null) {
        val id =
            sfx.playRequiredReturningStreamId(
                assetPath = letter,
                volume = 1f,
                context = "GameScreenAudioPrompts.playEpisode4FindGridReplayLetterOnly",
                chapterId = chapterId,
                stationId = stationId,
            )
        if (id != null) return
        if (voice.hasAsset(letter)) {
            voice.playBlocking(letter)
            return
        }
    }
    speakLetterPrompt(voice, q.targetLetter)
}

internal suspend fun replayEpisode4Stations15RoundAudio(
    sfx: SoundPoolPlayer,
    voice: VoicePlayer,
    chapterId: Int,
    stationId: Int,
    q: Question,
    rawVoice: RawVoicePlayer? = null,
) {
    when (q) {
        is Question.PopBalloonsQuestion -> {
            when (stationId) {
                Chapter1StationOrder.TAP_LETTER -> {
                    sfx.stopAllStreams()
                    if (chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5) {
                        val resId = AudioClips.letterNameRawResId(q.correctAnswer)
                        if (resId == null) {
                            android.util.Log.e(
                                "MissingContent",
                                "Missing required replay letter-name audio. chapterId=$chapterId stationId=$stationId context=GameScreenAudioPrompts.replayEpisode4Stations15RoundAudio(TAP_LETTER) stage=missing raw letter-name mapping letter='${q.correctAnswer}'",
                            )
                            rawVoice?.playRawBlocking(0)
                            return
                        }
                        if (rawVoice == null) {
                            android.util.Log.e(
                                "MissingContent",
                                "Missing required replay letter-name audio. chapterId=$chapterId stationId=$stationId context=GameScreenAudioPrompts.replayEpisode4Stations15RoundAudio(TAP_LETTER) stage=rawVoice=null expectedRawResId=$resId",
                            )
                            voice.playRequiredBlocking(
                                assetPath = "",
                                context = "GameScreenAudioPrompts.replayEpisode4Stations15RoundAudio(TAP_LETTER,rawVoice=null)",
                                chapterId = chapterId,
                                stationId = stationId,
                            )
                            return
                        }
                        rawVoice.playRawBlocking(resId)
                        return
                    }
                    val letterClip = AudioClips.letterNameClip(q.correctAnswer)
                    if (letterClip != null) {
                        val id =
                            sfx.playRequiredReturningStreamId(
                                assetPath = letterClip,
                                volume = 1f,
                                context = "GameScreenAudioPrompts.replayEpisode4Stations15RoundAudio(TAP_LETTER)",
                                chapterId = chapterId,
                                stationId = stationId,
                            )
                        if (id != null) return
                        if (voice.hasAsset(letterClip)) {
                            voice.playBlocking(letterClip)
                            return
                        }
                    }
                    speakLetterPrompt(voice, q.correctAnswer)
                }
                Chapter1StationOrder.BALLOON_POP -> {
                    sfx.stopAllStreams()
                    if (chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5) {
                        val resId = AudioClips.letterNameRawResId(q.correctAnswer)
                        if (resId == null) {
                            android.util.Log.e(
                                "MissingContent",
                                "Missing required replay letter-name audio. chapterId=$chapterId stationId=$stationId context=GameScreenAudioPrompts.replayEpisode4Stations15RoundAudio(BALLOON_POP) stage=missing raw letter-name mapping letter='${q.correctAnswer}'",
                            )
                            rawVoice?.playRawBlocking(0)
                            return
                        }
                        if (rawVoice == null) {
                            android.util.Log.e(
                                "MissingContent",
                                "Missing required replay letter-name audio. chapterId=$chapterId stationId=$stationId context=GameScreenAudioPrompts.replayEpisode4Stations15RoundAudio(BALLOON_POP) stage=rawVoice=null expectedRawResId=$resId",
                            )
                            voice.playRequiredBlocking(
                                assetPath = "",
                                context = "GameScreenAudioPrompts.replayEpisode4Stations15RoundAudio(BALLOON_POP,rawVoice=null)",
                                chapterId = chapterId,
                                stationId = stationId,
                            )
                            return
                        }
                        rawVoice.playRawBlocking(resId)
                        return
                    }
                    val letterClip = AudioClips.letterNameClip(q.correctAnswer)
                    if (letterClip != null) {
                        val id =
                            sfx.playRequiredReturningStreamId(
                                assetPath = letterClip,
                                volume = 1f,
                                context = "GameScreenAudioPrompts.replayEpisode4Stations15RoundAudio(BALLOON_POP)",
                                chapterId = chapterId,
                                stationId = stationId,
                            )
                        if (id != null) return
                        if (voice.hasAsset(letterClip)) {
                            voice.playBlocking(letterClip)
                            return
                        }
                    }
                    speakLetterPrompt(voice, q.correctAnswer)
                }
                else -> {}
            }
        }
        is Question.FindLetterGridQuestion ->
            playEpisode4FindGridReplayLetterOnly(
                sfx = sfx,
                voice = voice,
                q = q,
                chapterId = chapterId,
                stationId = stationId,
                rawVoice = rawVoice,
            )
        is Question.PictureStartsWithQuestion -> {
            if (chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5) {
                val resId = AudioClips.wordRawResIdByCatalogId(q.catalogEntryId)
                if (resId == null) {
                    android.util.Log.e(
                        "MissingContent",
                        "Missing required replay word audio. chapterId=$chapterId stationId=$stationId context=GameScreenAudioPrompts.replayEpisode4Stations15RoundAudio(PictureStartsWith) stage=missing raw word mapping catalogId='${q.catalogEntryId}'",
                    )
                    rawVoice?.playRawBlocking(0)
                    return
                }
                if (rawVoice == null) {
                    android.util.Log.e(
                        "MissingContent",
                        "Missing required replay word audio. chapterId=$chapterId stationId=$stationId context=GameScreenAudioPrompts.replayEpisode4Stations15RoundAudio(PictureStartsWith) stage=rawVoice=null expectedRawResId=$resId",
                    )
                    voice.playRequiredBlocking(
                        assetPath = "",
                        context = "GameScreenAudioPrompts.replayEpisode4Stations15RoundAudio(PictureStartsWith,rawVoice=null)",
                        chapterId = chapterId,
                        stationId = stationId,
                    )
                    return
                }
                rawVoice.playRawBlocking(resId)
                return
            }
            val wordPath = AudioClips.wordClipByCatalogId(q.catalogEntryId)
            if (voice.hasAsset(wordPath)) {
                voice.playBlocking(wordPath)
            }
        }
        is Question.ImageMatchQuestion -> {
            sfx.stopAllStreams()
            if (chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5) {
                val resId = AudioClips.letterNameRawResId(q.targetLetter)
                if (resId == null) {
                    android.util.Log.e(
                        "MissingContent",
                        "Missing required replay letter-name audio. chapterId=$chapterId stationId=$stationId context=GameScreenAudioPrompts.replayEpisode4Stations15RoundAudio(ImageMatch) stage=missing raw letter-name mapping letter='${q.targetLetter}'",
                    )
                    rawVoice?.playRawBlocking(0)
                    return
                }
                if (rawVoice == null) {
                    android.util.Log.e(
                        "MissingContent",
                        "Missing required replay letter-name audio. chapterId=$chapterId stationId=$stationId context=GameScreenAudioPrompts.replayEpisode4Stations15RoundAudio(ImageMatch) stage=rawVoice=null expectedRawResId=$resId",
                    )
                    voice.playRequiredBlocking(
                        assetPath = "",
                        context = "GameScreenAudioPrompts.replayEpisode4Stations15RoundAudio(ImageMatch,rawVoice=null)",
                        chapterId = chapterId,
                        stationId = stationId,
                    )
                    return
                }
                rawVoice.playRawBlocking(resId)
                return
            }
            val letterName = AudioClips.letterNameClip(q.targetLetter)
            if (letterName != null) {
                val id =
                    sfx.playRequiredReturningStreamId(
                        assetPath = letterName,
                        volume = 1f,
                        context = "GameScreenAudioPrompts.replayEpisode4Stations15RoundAudio(ImageMatch)",
                        chapterId = chapterId,
                        stationId = stationId,
                    )
                if (id != null) return
                if (voice.hasAsset(letterName)) {
                    voice.playBlocking(letterName)
                    return
                }
            }
            speakLetterPrompt(voice, q.targetLetter)
        }
        else -> {}
    }
}

internal suspend fun speakLetterPrompt(
    voice: VoicePlayer,
    letter: String,
    /**
     * Episode 1 station 1: pick randomly between "מצא את האות" and "בחר את האות" when both files exist;
     * other stations use only "בחר את האות" when present.
     */
    station1IntroVariant: Boolean = false,
    chapterId: Int? = null,
    stationId: Int? = null,
    context: String = "speakLetterPrompt",
    playerAddress: PlayerAddress? = null,
    rawVoice: RawVoicePlayer? = null,
) {
    val requiredPrompt =
        chapterId == 1 ||
            chapterId == 2 ||
            chapterId == 3 ||
            chapterId == 4 ||
            chapterId == 5 ||
            chapterId == 6 ||
            chapterId == TrainingV1Config.CHAPTER_ID
    if (requiredPrompt && stationId != null) {
        if (rawVoice == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=$context stage=rawVoice=null expectedInstructionRawRes!=null",
            )
            voice.playRequiredBlocking(
                assetPath = "",
                context = "$context(rawVoice=null)",
                chapterId = chapterId,
                stationId = stationId,
            )
            return
        }
        val introKind =
            when (stationId) {
                Chapter1StationOrder.TAP_LETTER -> Chapter1AddressAwareAudio.InstructionKind.PickLetter
                Chapter1StationOrder.BALLOON_POP -> Chapter1AddressAwareAudio.InstructionKind.PopBalloons
                Chapter1StationOrder.REVEAL_THEN_CHOOSE -> Chapter1AddressAwareAudio.InstructionKind.FindLetter
                else -> Chapter1AddressAwareAudio.InstructionKind.PickLetter
            }
        if (
            station1IntroVariant &&
                (chapterId == 4 || chapterId == 5) &&
                stationId == Chapter1StationOrder.TAP_LETTER &&
                voice.hasAsset(AudioClips.VoBachorEtHaot)
        ) {
            voice.playBlocking(AudioClips.VoBachorEtHaot)
        } else {
            if (playerAddress == null) {
                android.util.Log.e(
                    "MissingContent",
                    "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=$context stage=playerAddress=null expectedInstructionKind=$introKind",
                )
                rawVoice.playRawBlocking(0)
                return
            }
            rawVoice.playRawBlocking(Chapter1AddressAwareAudio.instructionRawRes(introKind, playerAddress))
        }
        val letterResId = AudioClips.letterNameRawResId(letter)
        if (letterResId == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=$context stage=missing letter-name mapping targetLetter='$letter'",
            )
            rawVoice.playRawBlocking(0)
            return
        }
        rawVoice.playRawBlocking(letterResId)
        return
    }
    val letterName = AudioClips.letterNameClip(letter)
    if (letterName == null) {
        AudioClips.reportMissingLetterNameMapping(
            tappedLetter = letter,
            chapterId = chapterId,
            stationId = stationId,
            context = context,
        )
    } else if (!voice.hasAsset(letterName)) {
        AudioClips.reportMissingLetterNameAsset(
            tappedLetter = letter,
            mappedAssetPath = letterName,
            chapterId = chapterId,
            stationId = stationId,
            context = context,
            detail = "VoicePlayer.hasAsset=false",
        )
    }
    val parts =
        buildList {
            if (letterName != null && voice.hasAsset(letterName)) add(letterName)
        }
    when (parts.size) {
        0 -> {
            android.util.Log.e(
                "MissingContent",
                "Missing required station prompt audio (no playable letter clip). chapterId=$chapterId stationId=$stationId context=$context targetLetter='$letter' mappedLetterClip=$letterName",
            )
            return
        }
        else -> voice.playBlocking(parts[0])
    }
}

internal suspend fun speakPromptForQuestion(
    voice: VoicePlayer,
    sfx: SoundPoolPlayer,
    stationId: Int,
    chapterId: Int,
    listenOnlyTargetPrompt: Boolean,
    q: Question,
    playerAddress: PlayerAddress? = null,
    rawVoice: RawVoicePlayer? = null,
) {
    val requiredChapters =
        chapterId == 1 ||
            chapterId == 2 ||
            chapterId == 3 ||
            chapterId == 4 ||
            chapterId == 5 ||
            chapterId == 6 ||
            chapterId == TrainingV1Config.CHAPTER_ID
    when (q) {
        is Question.PopBalloonsQuestion -> {
            // Episode 1 station 2: instruction is started from GameScreen LaunchedEffect (SoundPool).
            speakLetterPrompt(
                voice = voice,
                letter = q.correctAnswer,
                chapterId = if (requiredChapters) chapterId else null,
                stationId = if (requiredChapters) stationId else null,
                context = "speakPromptForQuestion(PopBalloons)",
                playerAddress = playerAddress,
                rawVoice = rawVoice,
            )
        }
        is Question.FindLetterGridQuestion ->
            speakLetterPrompt(
                voice = voice,
                letter = q.targetLetter,
                chapterId = if (requiredChapters) chapterId else null,
                stationId = if (requiredChapters) stationId else null,
                context = "speakPromptForQuestion(FindLetterGrid)",
                playerAddress = playerAddress,
                rawVoice = rawVoice,
            )
        is Question.PictureStartsWithQuestion -> {
            // Episode 1 station 4: instruction + spoken word (e.g. "ברווז").
            if (isSagaEpisodeForPrompt(chapterId) &&
                SixStationArcQaPolicy.isSagaPictureStartsWithStation(chapterId, stationId)
            ) {
                if (requiredChapters) {
                    val resId = AudioClips.wordRawResIdByCatalogId(q.catalogEntryId)
                    if (resId == null) {
                        android.util.Log.e(
                            "MissingContent",
                            "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=speakPromptForQuestion(Station4Word) stage=missing raw word mapping catalogId='${q.catalogEntryId}'",
                        )
                        rawVoice?.playRawBlocking(0)
                        return
                    }
                    if (rawVoice == null) {
                        android.util.Log.e(
                            "MissingContent",
                            "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=speakPromptForQuestion(Station4Word) stage=rawVoice=null expectedRawResId=$resId",
                        )
                        voice.playRequiredBlocking(
                            assetPath = "",
                            context = "speakPromptForQuestion(Station4Word,rawVoice=null)",
                            chapterId = chapterId,
                            stationId = stationId,
                        )
                        return
                    }
                    if (playerAddress == null) {
                        android.util.Log.e(
                            "MissingContent",
                            "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=speakPromptForQuestion(Station4Word) stage=playerAddress=null expectedInstructionKind=PictureStartsWith",
                        )
                        rawVoice.playRawBlocking(0)
                        return
                    }
                    rawVoice.playRawBlocking(
                        Chapter1AddressAwareAudio.instructionRawRes(
                            kind = Chapter1AddressAwareAudio.InstructionKind.PictureStartsWith,
                            address = playerAddress,
                        ),
                    )
                    rawVoice.playRawBlocking(resId)
                    return
                }
                val wordPath = AudioClips.wordClipByCatalogId(q.catalogEntryId)
                // Exact timing/overlap handled in the prompt startup path (has access to SoundPool).
                // Keep this path as a safe fallback.
                if (listenOnlyTargetPrompt && voice.hasAsset(wordPath)) {
                    voice.playBlocking(wordPath)
                } else {
                    speakLetterPrompt(
                        voice = voice,
                        letter = q.correctLetter,
                        chapterId = if (requiredChapters) chapterId else null,
                        stationId = if (requiredChapters) stationId else null,
                        context = "speakPromptForQuestion(Station4FallbackLetterPrompt)",
                        playerAddress = playerAddress,
                        rawVoice = rawVoice,
                    )
                }
            } else {
                speakLetterPrompt(
                    voice = voice,
                    letter = q.correctLetter,
                    chapterId = if (requiredChapters) chapterId else null,
                    stationId = if (requiredChapters) stationId else null,
                    context = "speakPromptForQuestion(PictureStartsWith)",
                    playerAddress = playerAddress,
                    rawVoice = rawVoice,
                )
            }
        }
        is Question.ImageMatchQuestion -> {
            if (
                Season2StationAudio.isPictureToWordStation(chapterId, stationId) ||
                    (chapterId == TrainingV1Config.CHAPTER_ID &&
                        stationId == TrainingV1Config.STATION_PICTURE_CHOOSE_WORD)
            ) {
                Season2StationAudio.speakPictureToWordRoundPrompt(
                    chapterId = chapterId,
                    stationId = stationId,
                    catalogId = q.correctChoiceId,
                    rawVoice = rawVoice,
                    voice = voice,
                )
                return
            }
            if (chapterId == TrainingV1Config.CHAPTER_ID &&
                stationId == TrainingV1Config.STATION_WHICH_WORD_STARTS_WITH_LETTER
            ) {
                if (rawVoice == null) {
                    android.util.Log.e(
                        "MissingContent",
                        "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=speakPromptForQuestion(TrainingImageMatchWhichWordStartsWith) stage=rawVoice=null expectedInstructionRawRes!=null",
                    )
                    voice.playRequiredBlocking(
                        assetPath = "",
                        context = "speakPromptForQuestion(TrainingImageMatchWhichWordStartsWith,rawVoice=null)",
                        chapterId = chapterId,
                        stationId = stationId,
                    )
                    return
                }
                if (playerAddress == null) {
                    android.util.Log.e(
                        "MissingContent",
                        "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=speakPromptForQuestion(TrainingImageMatchWhichWordStartsWith) stage=playerAddress=null expectedInstructionKind=WhichWordStartsWith",
                    )
                    rawVoice.playRawBlocking(0)
                    return
                }
                val letterResId = AudioClips.letterNameRawResId(q.targetLetter)
                if (letterResId == null) {
                    android.util.Log.e(
                        "MissingContent",
                        "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=speakPromptForQuestion(TrainingImageMatchWhichWordStartsWith) stage=missing raw letter-name mapping targetLetter='${q.targetLetter}'",
                    )
                    rawVoice.playRawBlocking(0)
                    return
                }
                rawVoice.playRawBlocking(
                    Chapter1AddressAwareAudio.instructionRawRes(
                        kind = Chapter1AddressAwareAudio.InstructionKind.WhichWordStartsWith,
                        address = playerAddress,
                    ),
                )
                rawVoice.playRawBlocking(letterResId)
                return
            }
            if (isSagaEpisodeForPrompt(chapterId) &&
                SixStationArcQaPolicy.isSagaWhichWordStartsWithStation(chapterId, stationId)
            ) {
                // Episode 1 station 5: "איזו מילה מתחילה באות" + letter name (SoundPool overlap when duration parses).
                if (!requiredChapters) return
                val letterResId = AudioClips.letterNameRawResId(q.targetLetter)
                if (letterResId == null) {
                    android.util.Log.e(
                        "MissingContent",
                        "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=GameScreenAudioPrompts.speakPromptForQuestion(station5) stage=missing raw letter-name mapping targetLetter='${q.targetLetter}'",
                    )
                    rawVoice?.playRawBlocking(0)
                    return
                }
                if (rawVoice == null) {
                    android.util.Log.e(
                        "MissingContent",
                        "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=GameScreenAudioPrompts.speakPromptForQuestion(station5) stage=rawVoice=null expectedInstructionRawRes!=null expectedLetterRawResId=$letterResId",
                    )
                    voice.playRequiredBlocking(
                        assetPath = "",
                        context = "GameScreenAudioPrompts.speakPromptForQuestion(station5,rawVoice=null)",
                        chapterId = chapterId,
                        stationId = stationId,
                    )
                    return
                }
                if (playerAddress == null) {
                    android.util.Log.e(
                        "MissingContent",
                        "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=GameScreenAudioPrompts.speakPromptForQuestion(station5) stage=playerAddress=null expectedInstructionKind=WhichWordStartsWith",
                    )
                    rawVoice.playRawBlocking(0)
                    return
                }
                rawVoice.playRawBlocking(
                    Chapter1AddressAwareAudio.instructionRawRes(
                        kind = Chapter1AddressAwareAudio.InstructionKind.WhichWordStartsWith,
                        address = playerAddress,
                    ),
                )
                rawVoice.playRawBlocking(letterResId)
            } else if (isSagaEpisodeForPrompt(chapterId) && stationId == Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH) {
                // Station 6: "ליחצו על אות והמילה שמתחילה באותה האות".
                if (!requiredChapters) return
                if (rawVoice == null) {
                    android.util.Log.e(
                        "MissingContent",
                        "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=speakPromptForQuestion(Station6MatchLetterToWord) stage=rawVoice=null expectedInstructionRawRes!=null",
                    )
                    voice.playRequiredBlocking(
                        assetPath = "",
                        context = "speakPromptForQuestion(Station6MatchLetterToWord,rawVoice=null)",
                        chapterId = chapterId,
                        stationId = stationId,
                    )
                    return
                }
                if (playerAddress == null) {
                    android.util.Log.e(
                        "MissingContent",
                        "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=speakPromptForQuestion(Station6MatchLetterToWord) stage=playerAddress=null expectedInstructionKind=MatchLetterToWord",
                    )
                    rawVoice.playRawBlocking(0)
                    return
                }
                rawVoice.playRawBlocking(
                    Chapter1AddressAwareAudio.instructionRawRes(
                        kind = Chapter1AddressAwareAudio.InstructionKind.MatchLetterToWord,
                        address = playerAddress,
                    ),
                )
            } else {
                speakLetterPrompt(
                    voice = voice,
                    letter = q.targetLetter,
                    chapterId = if (requiredChapters) chapterId else null,
                    stationId = if (requiredChapters) stationId else null,
                    context = "speakPromptForQuestion(ImageMatchFallbackLetterPrompt)",
                    playerAddress = playerAddress,
                    rawVoice = rawVoice,
                )
            }
        }
        is Question.MissingFirstLetterQuestion,
        is Question.WordPartsQuestion,
        is Question.RhymingQuestion,
        -> {
            Season2StationAudio.speakAdvancedRoundPrompt(
                chapterId = chapterId,
                stationId = stationId,
                q = q,
                voice = voice,
                rawVoice = rawVoice,
            )
        }
        is Question.FinaleSlotQuestion -> {
            if (!requiredChapters) return
            if (rawVoice == null) {
                android.util.Log.e(
                    "MissingContent",
                    "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=speakPromptForQuestion(FinaleSlotQuestion) stage=rawVoice=null expectedInstructionRawRes!=null",
                )
                voice.playRequiredBlocking(
                    assetPath = "",
                    context = "speakPromptForQuestion(FinaleSlotQuestion,rawVoice=null)",
                    chapterId = chapterId,
                    stationId = stationId,
                )
                return
            }
            if (playerAddress == null) {
                android.util.Log.e(
                    "MissingContent",
                    "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=speakPromptForQuestion(FinaleSlotQuestion) stage=playerAddress=null expectedInstructionKind=PickLetter",
                )
                rawVoice.playRawBlocking(0)
                return
            }
            rawVoice.playRawBlocking(
                Chapter1AddressAwareAudio.instructionRawRes(
                    kind = Chapter1AddressAwareAudio.InstructionKind.PickLetter,
                    address = playerAddress,
                ),
            )
        }
        is Question.DragWordToPictureQuestion,
        is Question.DragMissingLetterQuestion,
        -> Unit
    }
}
