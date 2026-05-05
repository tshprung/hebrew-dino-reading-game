package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.Chapter3EpisodeContent
import com.tal.hebrewdino.ui.domain.Question
import kotlinx.coroutines.delay
import kotlin.random.Random

/** Matches [SixStationArcChapterRange] in GameScreen for saga prompt branching only. */
private val SagaChapterRangeForAudioPrompts = 1..5

private fun isSagaEpisodeForPrompt(chapterId: Int): Boolean = chapterId in SagaChapterRangeForAudioPrompts

/** Station 3 find-letter intro: stretch the intro→letter delay by this factor (e.g. 1.10 = 10% more space before the letter). */
private const val Station3IntroToLetterLeadStretch = 1.10f
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
    introLetterLeadFraction: Float,
) {
    sfx.stopAllStreams()
    val combined = AudioClips.chooseLetterClip(q.targetLetter)
    val letter = AudioClips.letterNameClip(q.targetLetter)
    val findIntro = AudioClips.VoFindLetter
    val chooseIntro = AudioClips.VoChooseLetter
    val findMs = sfx.durationMs(findIntro) ?: 0L
    val chooseMs = sfx.durationMs(chooseIntro) ?: 0L
    val introPair: Pair<String, Long>? =
        when {
            findMs > 0L -> findIntro to findMs
            chooseMs > 0L -> chooseIntro to chooseMs
            else -> null
        }
    if (letter != null && introPair != null) {
        val (intro, introMs) = introPair
        sfx.playReturningStreamId(intro, volume = 1f)
        val lead =
            (introMs * introLetterLeadFraction * Station3IntroToLetterLeadStretch)
                .toLong()
                .coerceIn(16L, introMs)
        delay(lead)
        sfx.playReturningStreamId(letter, volume = 1f)
    } else {
        val bundledPath =
            sfx.playFirstAvailableReturningPath(
                *(listOfNotNull(combined, letter).toTypedArray()),
                volume = 1f,
            )
        if (bundledPath != null) {
            val parsed = sfx.durationMs(bundledPath) ?: 0L
            val waitMs =
                if (parsed > 0L) {
                    parsed.coerceAtLeast(80L)
                } else {
                    Station3InstructionFallbackDurationMs
                }
            delay(waitMs.coerceAtMost(6000L))
        } else {
            when {
                voice.hasAsset(findIntro) ->
                    voice.playSequenceBlocking(findIntro, letter ?: "")
                voice.hasAsset(chooseIntro) ->
                    voice.playSequenceBlocking(chooseIntro, letter ?: "")
                else -> speakLetterPrompt(voice, q.targetLetter)
            }
        }
    }
}

/** Episode 4 station 3 replay ("שוב"): target letter name only (no find-grid intro). */
internal suspend fun playEpisode4FindGridReplayLetterOnly(
    sfx: SoundPoolPlayer,
    voice: VoicePlayer,
    q: Question.FindLetterGridQuestion,
) {
    sfx.stopAllStreams()
    val letter = AudioClips.letterNameClip(q.targetLetter)
    if (letter != null) {
        val id = sfx.playReturningStreamId(letter, volume = 1f)
        if (id != null) return
        if (voice.hasAsset(letter)) {
            voice.playBlocking(letter)
            return
        }
    }
    speakLetterPrompt(voice, q.targetLetter)
}

/** Episode 3 station 2: sentence prompt + the current word (no letter-name voice). */
// Kept for backwards compatibility; Episode 3 station 2 prompt is now handled inline.
internal suspend fun playChapter3FindAllLettersInWordPrompt(
    voice: VoicePlayer,
    round: Chapter3EpisodeContent.SpellRound,
) {
    // Legacy helper kept; Episode 3 flow now uses dedicated station clips.
    val wordPath = AudioClips.wordClipByCatalogId(round.catalogId)
    if (voice.hasAsset(wordPath)) voice.playBlocking(wordPath)
}

internal suspend fun replayEpisode4Stations15RoundAudio(
    sfx: SoundPoolPlayer,
    voice: VoicePlayer,
    stationId: Int,
    q: Question,
) {
    when (q) {
        is Question.PopBalloonsQuestion -> {
            when (stationId) {
                Chapter1StationOrder.TAP_LETTER -> {
                    sfx.stopAllStreams()
                    val letterClip = AudioClips.letterNameClip(q.correctAnswer)
                    if (letterClip != null) {
                        val id = sfx.playReturningStreamId(letterClip, volume = 1f)
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
                    val letterClip = AudioClips.letterNameClip(q.correctAnswer)
                    if (letterClip != null) {
                        val id = sfx.playReturningStreamId(letterClip, volume = 1f)
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
        is Question.FindLetterGridQuestion -> playEpisode4FindGridReplayLetterOnly(sfx, voice, q)
        is Question.PictureStartsWithQuestion -> {
            val wordPath = AudioClips.wordClipByCatalogId(q.catalogEntryId)
            if (voice.hasAsset(wordPath)) {
                voice.playBlocking(wordPath)
            }
        }
        is Question.ImageMatchQuestion -> {
            sfx.stopAllStreams()
            val letterName = AudioClips.letterNameClip(q.targetLetter)
            if (letterName != null) {
                val id = sfx.playReturningStreamId(letterName, volume = 1f)
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
) {
    // Prefer one combined clip when the asset exists (e.g. full "בחר את האות …" per letter).
    val combined = AudioClips.chooseLetterClip(letter)
    if (combined != null && voice.hasAsset(combined)) {
        voice.playBlocking(combined)
        return
    }

    val letterName = AudioClips.letterNameClip(letter)
    val parts =
        buildList {
            if (station1IntroVariant) {
                val findOk = voice.hasAsset(AudioClips.VoFindLetter)
                val chooseOk = voice.hasAsset(AudioClips.VoChooseLetter)
                when {
                    findOk && chooseOk ->
                        add(
                            if (Random.nextBoolean()) {
                                AudioClips.VoFindLetter
                            } else {
                                AudioClips.VoChooseLetter
                            },
                        )
                    findOk -> add(AudioClips.VoFindLetter)
                    chooseOk -> add(AudioClips.VoChooseLetter)
                }
            } else {
                if (voice.hasAsset(AudioClips.VoChooseLetter)) add(AudioClips.VoChooseLetter)
            }
            if (letterName != null && voice.hasAsset(letterName)) add(letterName)
        }
    when (parts.size) {
        0 -> return
        1 -> voice.playBlocking(parts[0])
        else -> voice.playSequenceBlocking(*parts.toTypedArray())
    }
}

internal suspend fun speakPromptForQuestion(
    voice: VoicePlayer,
    sfx: SoundPoolPlayer,
    stationId: Int,
    chapterId: Int,
    listenOnlyTargetPrompt: Boolean,
    q: Question,
) {
    when (q) {
        is Question.PopBalloonsQuestion -> {
            // Episode 1 station 2: instruction is started from GameScreen LaunchedEffect (SoundPool).
            speakLetterPrompt(voice, q.correctAnswer)
        }
        is Question.FindLetterGridQuestion -> speakLetterPrompt(voice, q.targetLetter)
        is Question.PictureStartsWithQuestion -> {
            // Episode 1 station 4: instruction + spoken word (e.g. "ברווז").
            if (isSagaEpisodeForPrompt(chapterId) && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                val intro = AudioClips.WhichLetterDoesWordStart
                val wordPath = AudioClips.wordClipByCatalogId(q.catalogEntryId)
                // Exact timing/overlap handled in the prompt startup path (has access to SoundPool).
                // Keep this path as a safe fallback.
                if (voice.hasAsset(intro) && voice.hasAsset(wordPath)) {
                    voice.playSequenceBlocking(intro, wordPath)
                } else if (listenOnlyTargetPrompt && voice.hasAsset(wordPath)) {
                    voice.playBlocking(wordPath)
                } else {
                    speakLetterPrompt(voice, q.correctLetter)
                }
            } else {
                speakLetterPrompt(voice, q.correctLetter)
            }
        }
        is Question.ImageMatchQuestion -> {
            if (chapterId == 3 && stationId == 6) {
                // Episode 3 station 6: play the dedicated instruction ONLY if it exists,
                // and always speak the pictured word (no legacy fallback).
                val intro =
                    if (voice.hasAsset(AudioClips.Ch3ImageToWordInstructions)) {
                        AudioClips.Ch3ImageToWordInstructions
                    } else {
                        AudioClips.ImageToWordInstructions
                    }
                if (voice.hasAsset(intro)) {
                    voice.playBlocking(intro)
                }
                val ch3Word = "audio/ch3_word_${q.correctChoiceId}.wav"
                val wordPath =
                    if (voice.hasAsset(ch3Word)) {
                        ch3Word
                    } else {
                        AudioClips.wordClipByCatalogId(q.correctChoiceId)
                    }
                if (voice.hasAsset(wordPath)) {
                    voice.playBlocking(wordPath)
                }
                return
            }
            if (isSagaEpisodeForPrompt(chapterId) && stationId == Chapter1StationOrder.PICTURE_PICK_ALL) {
                // Episode 1 station 5: "איזו מילה מתחילה באות" + letter name (SoundPool overlap when duration parses).
                val intro = AudioClips.WhichWordStartsWithLetter
                val letterName = AudioClips.letterNameClip(q.targetLetter)
                val introMs = sfx.durationMs(intro) ?: 0L
                if (introMs > 0L && letterName != null) {
                    sfx.stopAllStreams()
                    sfx.playReturningStreamId(intro, volume = 1f)
                    val baseWhichWordLeadFrac =
                        Station5WhichWordIntroLetterLeadFraction *
                            Station5WhichWordIntroToLetterLeadScale
                    val whichWordLeadFrac =
                        baseWhichWordLeadFrac +
                            Station5WhichWordIntroToLetterGapBoost * (1f - baseWhichWordLeadFrac)
                    val lead =
                        (introMs * whichWordLeadFrac)
                            .toLong()
                            .coerceIn(16L, introMs)
                    delay(lead + Station5WhichWordIntroToLetterExtraPauseMs)
                    sfx.playReturningStreamId(letterName, volume = 1f)
                } else {
                    voice.playBlocking(intro)
                    if (letterName != null) voice.playBlocking(letterName)
                }
            } else if (isSagaEpisodeForPrompt(chapterId) && stationId == Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH) {
                // Station 6: "ליחצו על אות והמילה שמתחילה באותה האות".
                voice.playBlocking(AudioClips.MatchLetterToWordInstructions)
            } else {
                speakLetterPrompt(voice, q.targetLetter)
            }
        }
        is Question.FinaleSlotQuestion -> voice.playBlocking(AudioClips.VoChooseLetter)
    }
}
