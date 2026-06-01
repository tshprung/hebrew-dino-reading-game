package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.data.PlayerAddress
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
    chapter1PlayerAddress: PlayerAddress? = null,
    rawVoice: RawVoicePlayer? = null,
) {
    if (!audioEnabled) return
    val usesRawLetterNames = chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5

    if ((chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5) && chapter1PlayerAddress != null && rawVoice != null) {
        if (
            playChapter1AddressAwareIntro(
                chapterId = chapterId,
                stationId = stationId,
                stationTemplateId = stationTemplateId,
                playerAddress = chapter1PlayerAddress,
                sagaUsesPickLetterAudioStaging = sagaUsesPickLetterAudioStaging,
                sagaUsesFindGridAudioStaging = sagaUsesFindGridAudioStaging,
                sagaUsesPopBalloonsAudioStaging = sagaUsesPopBalloonsAudioStaging,
                isSagaEpisode = isSagaEpisode,
                q = q,
                rawVoice = rawVoice,
                voice = voice,
                sfx = sfx,
                station1IntroLetterLeadFraction = station1IntroLetterLeadFraction,
                station1IntroToLetterLeadScale = station1IntroToLetterLeadScale,
                station2BalloonIntroLetterLeadFraction = station2BalloonIntroLetterLeadFraction,
                station2IntroToLetterLeadScale = station2IntroToLetterLeadScale,
                station2BalloonIntroToLetterGapBoost = station2BalloonIntroToLetterGapBoost,
                station2BalloonIntroToLetterExtraPauseMs = station2BalloonIntroToLetterExtraPauseMs,
                station4IntroWordLeadFraction = station4IntroWordLeadFraction,
                station4IntroToWordLeadScale = station4IntroToWordLeadScale,
                station4IntroToWordGapBoost = station4IntroToWordGapBoost,
                station4IntroToWordExtraPauseMs = station4IntroToWordExtraPauseMs,
            )
        ) {
            return
        }
    }

    if (isChapter3HighlightedLetterInWordStation && q is Question.PopBalloonsQuestion) {
        val round = session.highlightedLetterInWordRound() ?: return
        sfx.stopAllStreams()
        val intro = AudioClips.Ch3St4FindHighlightedLetterInWordInstruction
        if (round.slotIndex == 0 && voice.hasAsset(intro)) {
            voice.playBlocking(intro)
        }
        val wordResId = AudioClips.wordRawResIdByCatalogId(round.catalogId)
        if (wordResId == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(HighlightedLetterInWord) stage=missing raw word mapping catalogId='${round.catalogId}'",
            )
            if (rawVoice != null) {
                rawVoice.playRawBlocking(0)
            } else {
                voice.playRequiredBlocking(
                    assetPath = "",
                    context = "playIntroPrompt(HighlightedLetterInWord,missingWordMapping,rawVoice=null)",
                    chapterId = chapterId,
                    stationId = stationId,
                )
            }
            return
        }
        val letterResId = AudioClips.letterNameRawResId(round.correctLetter)
        if (letterResId == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(HighlightedLetterInWord) stage=missing raw letter-name mapping letter='${round.correctLetter}'",
            )
            if (rawVoice != null) {
                rawVoice.playRawBlocking(0)
            } else {
                voice.playRequiredBlocking(
                    assetPath = "",
                    context = "playIntroPrompt(HighlightedLetterInWord,missingLetterNameMapping,rawVoice=null)",
                    chapterId = chapterId,
                    stationId = stationId,
                )
            }
            return
        }
        if (rawVoice == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(HighlightedLetterInWord) stage=rawVoice=null expectedWordRawResId=$wordResId expectedLetterRawResId=$letterResId",
            )
            voice.playRequiredBlocking(
                assetPath = "",
                context = "playIntroPrompt(HighlightedLetterInWord,rawVoice=null)",
                chapterId = chapterId,
                stationId = stationId,
            )
            return
        }
        rawVoice.playRawBlocking(wordResId)
        rawVoice.playRawBlocking(letterResId)
        return
    }

    if (isChapter3AudioLetterRecognitionStation && q is Question.PopBalloonsQuestion) {
        sfx.stopAllStreams()
        val instruction = AudioClips.VoChooseLetter
        if (voice.hasAsset(instruction)) {
            voice.playBlocking(instruction)
        }
        val resId = AudioClips.letterNameRawResId(q.correctAnswer)
        if (resId == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(AudioLetterRecognition) stage=missing raw letter-name mapping targetLetter='${q.correctAnswer}'",
            )
            if (rawVoice != null) {
                rawVoice.playRawBlocking(0)
            } else {
                voice.playRequiredBlocking(
                    assetPath = "",
                    context = "playIntroPrompt(AudioLetterRecognition,missingLetterNameMapping,rawVoice=null)",
                    chapterId = chapterId,
                    stationId = stationId,
                )
            }
            return
        }
        if (rawVoice == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(AudioLetterRecognition) stage=rawVoice=null expectedRawResId=$resId",
            )
            voice.playRequiredBlocking(
                assetPath = "",
                context = "playIntroPrompt(AudioLetterRecognition,rawVoice=null)",
                chapterId = chapterId,
                stationId = stationId,
            )
            return
        }
        rawVoice.playRawBlocking(resId)
        return
    }

    if (stationTemplateId == StationTemplateId.ImageMatch &&
        stationId == Chapter1StationOrder.PICTURE_PICK_ALL &&
        q is Question.ImageMatchQuestion
    ) {
        sfx.stopAllStreams()
        val isRequiredChapter = chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5
        if (isRequiredChapter) {
            voice.playRequiredBlocking(
                assetPath = AudioClips.WhichWordStartsWithLetter,
                context = "playIntroPrompt(ImageMatchStation5Intro)",
                chapterId = chapterId,
                stationId = stationId,
            )
            val letterResId = AudioClips.letterNameRawResId(q.targetLetter)
            if (letterResId == null) {
                android.util.Log.e(
                    "MissingContent",
                    "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(ImageMatchStation5) stage=missing raw letter-name mapping targetLetter='${q.targetLetter}'",
                )
                rawVoice?.playRawBlocking(0)
                return
            }
            if (rawVoice == null) {
                android.util.Log.e(
                    "MissingContent",
                    "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(ImageMatchStation5) stage=rawVoice=null expectedRawResId=$letterResId",
                )
                voice.playRequiredBlocking(
                    assetPath = "",
                    context = "playIntroPrompt(ImageMatchStation5,rawVoice=null)",
                    chapterId = chapterId,
                    stationId = stationId,
                )
                return
            }
            rawVoice.playRawBlocking(letterResId)
        } else {
            val intro =
                listOf(AudioClips.ChoosePictureStartsWithLetter, AudioClips.WhichWordStartsWithLetter)
                    .firstOrNull { voice.hasAsset(it) }
                    ?: AudioClips.WhichWordStartsWithLetter
            if (!voice.hasAsset(intro)) {
                android.util.Log.e(
                    "MissingContent",
                    "Missing required station prompt intro asset. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(ImageMatchStation5) expectedAnyOf=[${AudioClips.ChoosePictureStartsWithLetter}, ${AudioClips.WhichWordStartsWithLetter}]",
                )
            }
            val letterName = AudioClips.letterNameClip(q.targetLetter)
            val parts =
                buildList {
                    if (voice.hasAsset(intro)) add(intro)
                    if (letterName != null) add(letterName)
                }.filter { voice.hasAsset(it) }
            if (parts.isNotEmpty()) {
                voice.playSequenceBlocking(parts)
            } else {
                speakLetterPrompt(
                    voice = voice,
                    letter = q.targetLetter,
                    chapterId = chapterId,
                    stationId = stationId,
                    context = "playIntroPrompt(ImageMatchStation5Fallback)",
                    rawVoice = rawVoice,
                )
            }
        }
        return
    }

    if (stationTemplateId == StationTemplateId.MatchLetterToWord && q is Question.ImageMatchQuestion) {
        sfx.stopAllStreams()
        voice.playRequiredBlocking(
            assetPath = AudioClips.MatchLetterToWordInstructions,
            context = "playIntroPrompt(MatchLetterToWord)",
            chapterId = chapterId,
            stationId = stationId,
        )
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
        voice.playRequiredBlocking(
            assetPath = intro,
            context = "playIntroPrompt(ImageToWord)",
            chapterId = chapterId,
            stationId = stationId,
        )
        if (chapterId == 3 || chapterId == 6) {
            val wordResId = AudioClips.wordRawResIdByCatalogId(q.correctChoiceId)
            if (wordResId == null) {
                android.util.Log.e(
                    "MissingContent",
                    "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(ImageToWordWord) stage=missing raw word mapping catalogId='${q.correctChoiceId}'",
                )
                if (rawVoice != null) {
                    rawVoice.playRawBlocking(0)
                } else {
                    voice.playRequiredBlocking(
                        assetPath = "",
                        context = "playIntroPrompt(ImageToWordWord,missingWordMapping,rawVoice=null)",
                        chapterId = chapterId,
                        stationId = stationId,
                    )
                }
                return
            }
            if (rawVoice == null) {
                android.util.Log.e(
                    "MissingContent",
                    "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(ImageToWordWord) stage=rawVoice=null expectedRawResId=$wordResId",
                )
                voice.playRequiredBlocking(
                    assetPath = "",
                    context = "playIntroPrompt(ImageToWordWord,rawVoice=null)",
                    chapterId = chapterId,
                    stationId = stationId,
                )
                return
            }
            rawVoice.playRawBlocking(wordResId)
            return
        }
        val wordPath =
            AudioClips.imageToWordClipByCatalogId(
                catalogEntryId = q.correctChoiceId,
                chapterId = chapterId,
                voiceHasAsset = { path -> voice.hasAsset(path) },
            )
        voice.playRequiredBlocking(
            assetPath = wordPath,
            context = "playIntroPrompt(ImageToWordWord)",
            chapterId = chapterId,
            stationId = stationId,
        )
        return
    }

    if (sagaUsesPickLetterAudioStaging) {
        val requiredSoundPool =
            chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5
        val target =
            when (q) {
                is Question.PopBalloonsQuestion -> q.correctAnswer
                is Question.FindLetterGridQuestion -> q.targetLetter
                is Question.PictureStartsWithQuestion -> q.correctLetter
                is Question.ImageMatchQuestion -> q.targetLetter
                is Question.FinaleSlotQuestion -> null
            }
        if (target != null) {
            val intro =
                if ((chapterId == 4 || chapterId == 5) && stationId == Chapter1StationOrder.TAP_LETTER) {
                    AudioClips.VoBachorEtHaot
                } else {
                    AudioClips.VoChooseLetter
                }
            val introMs =
                if (requiredSoundPool) {
                    sfx.durationMsRequiredOrNull(
                        assetPath = intro,
                        context = "GameIntroPromptPlayer.playIntroPrompt(station1,SoundPoolIntro)",
                        chapterId = chapterId,
                        stationId = stationId,
                    ) ?: 0L
                } else {
                    sfx.durationMs(intro) ?: 0L
                }
            if (requiredSoundPool) {
                val letterResId = AudioClips.letterNameRawResId(target)
                if (letterResId == null) {
                    android.util.Log.e(
                        "MissingContent",
                        "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(station1) stage=missing raw letter-name mapping targetLetter='$target'",
                    )
                    rawVoice?.playRawBlocking(0)
                    return
                }
                if (rawVoice == null) {
                    android.util.Log.e(
                        "MissingContent",
                        "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(station1) stage=rawVoice=null expectedRawResId=$letterResId",
                    )
                    voice.playRequiredBlocking(
                        assetPath = "",
                        context = "playIntroPrompt(station1,rawVoice=null)",
                        chapterId = chapterId,
                        stationId = stationId,
                    )
                    return
                }
                sfx.stopAllStreams()
                sfx.playRequiredReturningStreamId(
                    assetPath = intro,
                    volume = 1f,
                    context = "GameIntroPromptPlayer.playIntroPrompt(station1,SoundPoolIntro)",
                    chapterId = chapterId,
                    stationId = stationId,
                )
                val lead =
                    (introMs * (station1IntroLetterLeadFraction * station1IntroToLetterLeadScale))
                        .toLong()
                        .coerceIn(16L, introMs.coerceAtLeast(16L))
                delay(lead)
                rawVoice.playRawBlocking(letterResId)
            } else {
                if (!voice.hasAsset(intro)) {
                    android.util.Log.e(
                        "MissingContent",
                        "Missing required station prompt intro asset. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(station1,VoiceSequenceFallback) expected=$intro",
                    )
                }
                voice.playSequenceBlocking(
                    intro,
                    AudioClips.letterNameClip(target) ?: "",
                )
            }
        }
        return
    }

    if (sagaUsesFindGridAudioStaging && q is Question.FindLetterGridQuestion) {
        playSagaFindGridIntroSoundPool(
            sfx = sfx,
            voice = voice,
            q = q,
            chapterId = chapterId,
            stationId = stationId,
            introLetterLeadFraction = 0.94f,
            rawVoice = rawVoice,
        )
        return
    }

    if ((chapterId == 3 || chapterId == 6) && stationId == 1 && q is Question.PictureStartsWithQuestion) {
        sfx.stopAllStreams()
        val clip = AudioClips.WhichLetterDoesWordStart
        if (voice.hasAsset(clip)) {
            voice.playBlocking(clip)
        }
        val wordResId = AudioClips.wordRawResIdByCatalogId(q.catalogEntryId)
        if (wordResId == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(Ch3Ch6Station1Word) stage=missing raw word mapping catalogId='${q.catalogEntryId}'",
            )
            if (rawVoice != null) {
                rawVoice.playRawBlocking(0)
            } else {
                voice.playRequiredBlocking(
                    assetPath = "",
                    context = "playIntroPrompt(Ch3Ch6Station1Word,missingWordMapping,rawVoice=null)",
                    chapterId = chapterId,
                    stationId = stationId,
                )
            }
            return
        }
        if (rawVoice == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(Ch3Ch6Station1Word) stage=rawVoice=null expectedRawResId=$wordResId",
            )
            voice.playRequiredBlocking(
                assetPath = "",
                context = "playIntroPrompt(Ch3Ch6Station1Word,rawVoice=null)",
                chapterId = chapterId,
                stationId = stationId,
            )
            return
        }
        rawVoice.playRawBlocking(wordResId)
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
        voice.playSequenceBlocking(parts)
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
        voice.playSequenceBlocking(parts)
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
        val requiredSoundPool =
            chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5
        val introMs =
            if (requiredSoundPool) {
                sfx.durationMsRequiredOrNull(
                    assetPath = intro,
                    context = "GameIntroPromptPlayer.playIntroPrompt(station4,SoundPoolIntro)",
                    chapterId = chapterId,
                    stationId = stationId,
                ) ?: 0L
            } else {
                sfx.durationMs(intro) ?: 0L
            }
        if (introMs > 0 && requiredSoundPool) {
            sfx.stopAllStreams()
            sfx.playRequiredReturningStreamId(
                assetPath = intro,
                volume = 1f,
                context = "GameIntroPromptPlayer.playIntroPrompt(station4,SoundPoolIntro)",
                chapterId = chapterId,
                stationId = stationId,
            )
            val baseIntroWordLeadFrac = station4IntroWordLeadFraction * station4IntroToWordLeadScale
            val introWordLeadFrac =
                baseIntroWordLeadFrac + station4IntroToWordGapBoost * (1f - baseIntroWordLeadFrac)
            val lead =
                (introMs * introWordLeadFrac)
                    .toLong()
                    .coerceIn(16L, introMs)
            delay(lead + station4IntroToWordExtraPauseMs)
            sfx.stopAllStreams()
            voice.playRequiredBlocking(
                assetPath = wordPath,
                context = "playIntroPrompt(Station4Word)",
                chapterId = chapterId,
                stationId = stationId,
            )
        } else if (introMs > 0 && voice.hasAsset(wordPath)) {
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
            voice.playSequenceRequiredBlocking(
                assetPaths = listOf(intro, wordPath),
                context = "playIntroPrompt(Station4VoiceFallbackSequence)",
                chapterId = chapterId,
                stationId = stationId,
            )
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
            val intro = AudioClips.PopBalloonsWithLetter
            val requiredSoundPool =
                chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5
            val letterResId =
                if (requiredSoundPool) {
                    AudioClips.letterNameRawResId(q.correctAnswer)
                } else {
                    null
                }
            val letterClip =
                if (requiredSoundPool) {
                    null
                } else {
                    AudioClips.letterNameClip(q.correctAnswer)
                }
            val introMs =
                if (requiredSoundPool) {
                    sfx.durationMsRequiredOrNull(
                        assetPath = intro,
                        context = "GameIntroPromptPlayer.playIntroPrompt(station2,SoundPoolIntro)",
                        chapterId = chapterId,
                        stationId = stationId,
                    ) ?: 0L
                } else {
                    sfx.durationMs(intro) ?: 0L
                }
            if (introMs > 0) {
                sfx.stopAllStreams()
                if (requiredSoundPool) {
                    sfx.playRequiredReturningStreamId(
                        assetPath = intro,
                        volume = 1f,
                        context = "GameIntroPromptPlayer.playIntroPrompt(station2,SoundPoolIntro)",
                        chapterId = chapterId,
                        stationId = stationId,
                    )
                } else {
                    sfx.playReturningStreamId(intro, volume = 1f)
                }
                val baseIntroLeadFrac = station2BalloonIntroLetterLeadFraction * station2IntroToLetterLeadScale
                val introLeadFrac =
                    baseIntroLeadFrac + station2BalloonIntroToLetterGapBoost * (1f - baseIntroLeadFrac)
                val lead =
                    (introMs * introLeadFrac)
                        .toLong()
                        .coerceIn(16L, introMs)
                delay(lead + station2BalloonIntroToLetterExtraPauseMs)
                if (requiredSoundPool) {
                    if (letterResId == null) {
                        android.util.Log.e(
                            "MissingContent",
                            "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(station2) stage=missing raw letter-name mapping targetLetter='${q.correctAnswer}'",
                        )
                        rawVoice?.playRawBlocking(0)
                        return
                    }
                    if (rawVoice == null) {
                        android.util.Log.e(
                            "MissingContent",
                            "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(station2) stage=rawVoice=null expectedRawResId=$letterResId",
                        )
                        voice.playRequiredBlocking(
                            assetPath = "",
                            context = "playIntroPrompt(station2,rawVoice=null)",
                            chapterId = chapterId,
                            stationId = stationId,
                        )
                        return
                    }
                    rawVoice.playRawBlocking(letterResId)
                    return
                }
                if (letterClip != null) {
                    val id =
                        if (requiredSoundPool) {
                            sfx.playRequiredReturningStreamId(
                                assetPath = letterClip,
                                volume = 1f,
                                context = "GameIntroPromptPlayer.playIntroPrompt(station2,SoundPoolLetter)",
                                chapterId = chapterId,
                                stationId = stationId,
                            )
                        } else {
                            sfx.playReturningStreamId(letterClip, volume = 1f)
                        }
                    if (id == null) {
                        if (requiredSoundPool) return
                        if (voice.hasAsset(letterClip)) {
                            voice.playBlocking(letterClip)
                        } else {
                            speakLetterPrompt(voice, q.correctAnswer)
                        }
                    }
                } else {
                    if (requiredSoundPool) {
                        return
                    }
                    speakLetterPrompt(voice, q.correctAnswer)
                }
            } else {
                if (requiredSoundPool) return
                voice.playBlocking(intro)
                if (letterClip != null && voice.hasAsset(letterClip)) {
                    voice.playBlocking(letterClip)
                } else {
                    speakLetterPrompt(voice, q.correctAnswer)
                }
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
        rawVoice = rawVoice,
    )
}
