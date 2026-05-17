package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.StationTemplateId
import com.tal.hebrewdino.ui.domain.TrainingV1Config
import kotlinx.coroutines.delay

internal suspend fun playIntroPrompt(
    audioEnabled: Boolean,
    chapterId: Int,
    stationId: Int,
    listenOnlyTargetPrompt: Boolean,
    stationTemplateId: StationTemplateId,
    planPopAllLettersInWord: Boolean,
    isSagaEpisode: Boolean,
    sagaUsesPickLetterAudioStaging: Boolean,
    sagaUsesPopBalloonsAudioStaging: Boolean,
    sagaUsesFindGridAudioStaging: Boolean,
    isChapter3HighlightedLetterInWordStation: Boolean,
    isChapter3AudioLetterRecognitionStation: Boolean,
    session: LevelSession,
    q: Question,
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
) {
    if (!audioEnabled) return

    if (isChapter3HighlightedLetterInWordStation && q is Question.PopBalloonsQuestion) {
        val round = session.highlightedLetterInWordRound() ?: return
        sfx.stopAllStreams()
        val intro = AudioClips.Ch3St4FindHighlightedLetterInWordInstruction
        val parts =
            buildList {
                if (round.slotIndex == 0 && voice.hasAsset(intro)) add(intro)
                add(AudioClips.wordClipByCatalogId(round.catalogId))
                AudioClips.letterNameClip(round.correctLetter)?.let { add(it) }
            }.filter { voice.hasAsset(it) }
        if (parts.isNotEmpty()) voice.playSequenceBlocking(*parts.toTypedArray())
        return
    }

    if (isChapter3AudioLetterRecognitionStation && q is Question.PopBalloonsQuestion) {
        sfx.stopAllStreams()
        val instruction = AudioClips.VoChooseLetter
        val letterClip = AudioClips.letterNameClip(q.correctAnswer)
        val parts =
            buildList {
                if (voice.hasAsset(instruction)) add(instruction)
                if (letterClip != null) add(letterClip)
            }.filter { voice.hasAsset(it) }
        if (parts.isNotEmpty()) voice.playSequenceBlocking(*parts.toTypedArray())
        return
    }

    if (stationTemplateId == StationTemplateId.ImageMatch &&
        stationId == Chapter1StationOrder.PICTURE_PICK_ALL &&
        q is Question.ImageMatchQuestion
    ) {
        sfx.stopAllStreams()
        val intro =
            listOf(AudioClips.ChoosePictureStartsWithLetter, AudioClips.WhichWordStartsWithLetter)
                .firstOrNull { voice.hasAsset(it) }
                ?: AudioClips.WhichWordStartsWithLetter
        val letterName = AudioClips.letterNameClip(q.targetLetter)
        val parts =
            buildList {
                if (voice.hasAsset(intro)) add(intro)
                if (letterName != null) add(letterName)
            }.filter { voice.hasAsset(it) }
        if (parts.isNotEmpty()) {
            voice.playSequenceBlocking(*parts.toTypedArray())
        } else {
            speakLetterPrompt(voice, q.targetLetter)
        }
        return
    }

    if (stationTemplateId == StationTemplateId.MatchLetterToWord && q is Question.ImageMatchQuestion) {
        sfx.stopAllStreams()
        if (voice.hasAsset(AudioClips.MatchLetterToWordInstructions)) {
            voice.playBlocking(AudioClips.MatchLetterToWordInstructions)
        }
        return
    }

    if (stationTemplateId == StationTemplateId.ImageToWord && q is Question.ImageMatchQuestion) {
        sfx.stopAllStreams()
        val intro =
            if (voice.hasAsset(AudioClips.Ch3ImageToWordInstructions)) {
                AudioClips.Ch3ImageToWordInstructions
            } else {
                AudioClips.ImageToWordInstructions
            }
        if (voice.hasAsset(intro)) voice.playBlocking(intro)
        val wordPath =
            if (chapterId == 3 || chapterId == 6) {
                val ch3Word = "audio/ch3_word_${q.correctChoiceId}.wav"
                if (voice.hasAsset(ch3Word)) ch3Word else AudioClips.wordClipByCatalogId(q.correctChoiceId)
            } else {
                AudioClips.wordClipByCatalogId(q.correctChoiceId)
            }
        if (voice.hasAsset(wordPath)) voice.playBlocking(wordPath)
        return
    }

    if (sagaUsesPickLetterAudioStaging) {
        val target =
            when (q) {
                is Question.PopBalloonsQuestion -> q.correctAnswer
                is Question.FindLetterGridQuestion -> q.targetLetter
                is Question.PictureStartsWithQuestion -> q.correctLetter
                is Question.ImageMatchQuestion -> q.targetLetter
                is Question.FinaleSlotQuestion -> null
            }
        if (target != null) {
            val letterClip = AudioClips.letterNameClip(target)
            val intro =
                if ((chapterId == 4 || chapterId == 5) && stationId == Chapter1StationOrder.TAP_LETTER) {
                    if (voice.hasAsset(AudioClips.VoBachorEtHaot)) {
                        AudioClips.VoBachorEtHaot
                    } else {
                        AudioClips.VoChooseLetter
                    }
                } else {
                    AudioClips.VoChooseLetter
                }
            val introMs = sfx.durationMs(intro) ?: 0L
            if (introMs > 0L && letterClip != null) {
                sfx.stopAllStreams()
                sfx.playReturningStreamId(intro, volume = 1f)
                val lead =
                    (introMs * station1IntroLetterLeadFraction * station1IntroToLetterLeadScale)
                        .toLong()
                        .coerceIn(16L, introMs)
                delay(lead)
                sfx.playReturningStreamId(letterClip, volume = 1f)
            } else {
                voice.playSequenceBlocking(
                    intro,
                    letterClip ?: "",
                )
            }
        }
        return
    }

    if (sagaUsesFindGridAudioStaging && q is Question.FindLetterGridQuestion) {
        playSagaFindGridIntroSoundPool(sfx, voice, q, chapterId, 0.94f)
        return
    }

    if (chapterId == 6 && stationId == 4 && q is Question.PictureStartsWithQuestion) {
        sfx.stopAllStreams()
        val clip = AudioClips.WhichLetterDoesWordStart
        val wordPath = AudioClips.wordClipByCatalogId(q.catalogEntryId)
        if (voice.hasAsset(clip)) voice.playBlocking(clip)
        if (voice.hasAsset(wordPath)) voice.playBlocking(wordPath)
        return
    }

    if ((chapterId == 3 || chapterId == 6) && stationId == 1 && q is Question.PictureStartsWithQuestion) {
        sfx.stopAllStreams()
        val clip = AudioClips.WhichLetterDoesWordStart
        val wordPath = AudioClips.wordClipByCatalogId(q.catalogEntryId)
        if (voice.hasAsset(clip)) voice.playBlocking(clip)
        if (voice.hasAsset(wordPath)) voice.playBlocking(wordPath)
        return
    }

    if ((chapterId == 3 || chapterId == 6) && stationId == 2 && q is Question.ImageMatchQuestion) {
        sfx.stopAllStreams()
        voice.playBlocking(AudioClips.MatchLetterToWordInstructions)
        return
    }

    if (chapterId == TrainingV1Config.CHAPTER_ID &&
        stationId == TrainingV1Config.STATION_HEAR_LETTER_CHOOSE &&
        q is Question.PopBalloonsQuestion
    ) {
        sfx.stopAllStreams()
        val intro = AudioClips.VoChooseLetter
        val letterClip = AudioClips.letterNameClip(q.correctAnswer)
        val parts =
            buildList {
                if (voice.hasAsset(intro)) add(intro)
                if (letterClip != null && voice.hasAsset(letterClip)) add(letterClip)
            }
        if (parts.isNotEmpty()) {
            voice.playSequenceBlocking(*parts.toTypedArray())
        } else {
            speakLetterPrompt(voice, q.correctAnswer)
        }
        return
    }

    if (chapterId == TrainingV1Config.CHAPTER_ID &&
        stationId == TrainingV1Config.STATION_WHICH_WORD_STARTS_WITH_LETTER &&
        q is Question.ImageMatchQuestion
    ) {
        sfx.stopAllStreams()
        val intro =
            if (voice.hasAsset(AudioClips.WhichWordStartsWithLetter)) {
                AudioClips.WhichWordStartsWithLetter
            } else {
                AudioClips.ChoosePictureStartsWithLetter
            }
        val letterClip = AudioClips.letterNameClip(q.targetLetter)
        val parts =
            buildList {
                if (voice.hasAsset(intro)) add(intro)
                if (letterClip != null && voice.hasAsset(letterClip)) add(letterClip)
            }
        if (parts.isNotEmpty()) {
            voice.playSequenceBlocking(*parts.toTypedArray())
        } else {
            speakLetterPrompt(voice, q.targetLetter)
        }
        return
    }

    if (chapterId == TrainingV1Config.CHAPTER_ID &&
        stationId == TrainingV1Config.STATION_MATCH_LETTER_TO_WORD &&
        q is Question.ImageMatchQuestion
    ) {
        sfx.stopAllStreams()
        if (voice.hasAsset(AudioClips.MatchLetterToWordInstructions)) {
            voice.playBlocking(AudioClips.MatchLetterToWordInstructions)
        }
        return
    }

    if (isSagaEpisode && stationId == Chapter1StationOrder.PICTURE_PICK_ONE && q is Question.PictureStartsWithQuestion) {
        val intro = AudioClips.WhichLetterDoesWordStart
        val wordPath = AudioClips.wordClipByCatalogId(q.catalogEntryId)
        val introMs = sfx.durationMs(intro) ?: 0L
        if (introMs > 0 && voice.hasAsset(wordPath)) {
            sfx.stopAllStreams()
            sfx.playReturningStreamId(intro, volume = 1f)
            val baseIntroWordLeadFrac = station4IntroWordLeadFraction * station4IntroToWordLeadScale
            val introWordLeadFrac =
                baseIntroWordLeadFrac + station4IntroToWordGapBoost * (1f - baseIntroWordLeadFrac)
            val lead =
                (introMs * introWordLeadFrac)
                    .toLong()
                    .coerceIn(16L, introMs)
            delay(lead + station4IntroToWordExtraPauseMs)
            sfx.stopAllStreams()
            voice.playBlocking(wordPath)
        } else {
            voice.playSequenceBlocking(intro, wordPath)
        }
        return
    }

    if ((sagaUsesPopBalloonsAudioStaging ||
            planPopAllLettersInWord ||
            (chapterId == 6 && stationId == 3) ||
            (chapterId == TrainingV1Config.CHAPTER_ID && stationId == TrainingV1Config.STATION_WORD_BALLOONS)) &&
        q is Question.PopBalloonsQuestion
    ) {
        if (planPopAllLettersInWord) {
            sfx.stopAllStreams()
            val clip = AudioClips.Ch3St3PopAllLettersInWordInstruction
            val (_, catalogId) =
                session.chapter3PopAllLettersCurrentWord()
                    ?: error("Missing pop-all letters word for index ${session.currentIndex}")
            val wordPath = AudioClips.wordClipByCatalogId(catalogId)
            if (voice.hasAsset(clip)) voice.playBlocking(clip)
            if (voice.hasAsset(wordPath)) voice.playBlocking(wordPath)
        } else {
            val intro =
                if (chapterId == TrainingV1Config.CHAPTER_ID &&
                    stationId == TrainingV1Config.STATION_WORD_BALLOONS &&
                    voice.hasAsset(AudioClips.PopAllBalloonsWithLetter)
                ) {
                    AudioClips.PopAllBalloonsWithLetter
                } else {
                    AudioClips.PopBalloonsWithLetter
                }
            val letterClip = AudioClips.letterNameClip(q.correctAnswer)
            val introMs = sfx.durationMs(intro) ?: 0L
            if (introMs > 0 && letterClip != null) {
                sfx.stopAllStreams()
                sfx.playReturningStreamId(intro, volume = 1f)
                val baseIntroLeadFrac = station2BalloonIntroLetterLeadFraction * station2IntroToLetterLeadScale
                val introLeadFrac =
                    baseIntroLeadFrac + station2BalloonIntroToLetterGapBoost * (1f - baseIntroLeadFrac)
                val lead =
                    (introMs * introLeadFrac)
                        .toLong()
                        .coerceIn(16L, introMs)
                delay(lead + station2BalloonIntroToLetterExtraPauseMs)
                sfx.playReturningStreamId(letterClip, volume = 1f)
            } else {
                voice.playSequenceBlocking(
                    intro,
                    letterClip ?: "",
                )
            }
        }
        return
    }

    speakPromptForQuestion(
        voice,
        sfx,
        stationId = stationId,
        chapterId = chapterId,
        listenOnlyTargetPrompt = listenOnlyTargetPrompt,
        q = q,
    )
}
