package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.companion.Chapter1AddressAwareAudio
import com.tal.hebrewdino.ui.data.PlayerAddress
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.Season1StationAudio
import com.tal.hebrewdino.ui.domain.Season2StationAudio
import com.tal.hebrewdino.ui.domain.SixStationArcQaPolicy
import com.tal.hebrewdino.ui.domain.StationTemplateId
import com.tal.hebrewdino.ui.domain.TrainingV1Config

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
    chapter1PlayerAddress: PlayerAddress? = null,
    rawVoice: RawVoicePlayer? = null,
) {
    if (!audioEnabled) return

    if (Season2StationAudio.usesChapter1StyleAddressAwareIntro(chapterId) && chapter1PlayerAddress != null && rawVoice != null) {
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
                sfx = sfx,
            )
        ) {
            return
        }
    }

    if (
        stationTemplateId == StationTemplateId.DragWordToPicture &&
            q is Question.DragWordToPictureQuestion &&
            Season1StationAudio.isSeason1DragWordToPictureStation(chapterId, stationId)
    ) {
        if (session.currentIndex == 0) {
            sfx.stopAllStreams()
            if (rawVoice == null) {
                android.util.Log.e(
                    "MissingContent",
                    "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(DragWordToPicture) stage=rawVoice=null expectedInstructionRawRes=${Season1StationAudio.dragWordToPictureInstructionRawResId()}",
                )
                voice.playRequiredBlocking(
                    assetPath = "",
                    context = "playIntroPrompt(DragWordToPicture,rawVoice=null)",
                    chapterId = chapterId,
                    stationId = stationId,
                )
                return
            }
            rawVoice.playRawBlocking(Season1StationAudio.dragWordToPictureInstructionRawResId())
        }
        return
    }

    if (
        stationTemplateId == StationTemplateId.DragMissingLetter &&
            q is Question.DragMissingLetterQuestion &&
            Season1StationAudio.isSeason1DragMissingLetterStation(chapterId, stationId)
    ) {
        sfx.stopAllStreams()
        if (rawVoice == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(DragMissingLetter) stage=rawVoice=null expectedInstructionRawRes=${Season1StationAudio.dragMissingLetterInstructionRawResId()}",
            )
            voice.playRequiredBlocking(
                assetPath = "",
                context = "playIntroPrompt(DragMissingLetter,rawVoice=null)",
                chapterId = chapterId,
                stationId = stationId,
            )
            return
        }
        if (session.currentIndex == 0) {
            Season1StationAudio.playDragMissingLetterRoundIntro(
                rawVoice = rawVoice,
                catalogEntryId = q.catalogEntryId,
                chapterId = chapterId,
                stationId = stationId,
                includeInstruction = true,
                context = "playIntroPrompt(DragMissingLetter)",
            )
        } else {
            Season1StationAudio.playDragMissingLetterWord(
                rawVoice = rawVoice,
                catalogEntryId = q.catalogEntryId,
                chapterId = chapterId,
                stationId = stationId,
                context = "playIntroPrompt(DragMissingLetterWord)",
            )
        }
        return
    }

    if (isChapter3HighlightedLetterInWordStation && q is Question.PopBalloonsQuestion) {
        val round = session.highlightedLetterInWordRound() ?: return
        sfx.stopAllStreams()
        if (round.slotIndex == 0) {
            if (rawVoice == null) {
                android.util.Log.e(
                    "MissingContent",
                    "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(HighlightedLetterInWord) stage=rawVoice=null expectedInstructionRawRes=${R.raw.instruction_find_highlighted_letter_in_word}",
                )
                voice.playRequiredBlocking(
                    assetPath = "",
                    context = "playIntroPrompt(HighlightedLetterInWord,rawVoice=null)",
                    chapterId = chapterId,
                    stationId = stationId,
                )
                return
            }
            rawVoice.playRawBlocking(R.raw.instruction_find_highlighted_letter_in_word)
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
        if (rawVoice == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(AudioLetterRecognition) stage=rawVoice=null expectedInstructionRawRes!=null",
            )
            voice.playRequiredBlocking(
                assetPath = "",
                context = "playIntroPrompt(AudioLetterRecognition,rawVoice=null)",
                chapterId = chapterId,
                stationId = stationId,
            )
            return
        }
        if (chapter1PlayerAddress == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(AudioLetterRecognition) stage=playerAddress=null expectedInstructionKind=PickLetter",
            )
            rawVoice.playRawBlocking(0)
            return
        }
        rawVoice.playRawBlocking(
            Chapter1AddressAwareAudio.instructionRawRes(
                kind = Chapter1AddressAwareAudio.InstructionKind.PickLetter,
                address = chapter1PlayerAddress,
            ),
        )
        val resId = AudioClips.letterNameRawResId(q.correctAnswer)
        if (resId == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(AudioLetterRecognition) stage=missing raw letter-name mapping targetLetter='${q.correctAnswer}'",
            )
            rawVoice.playRawBlocking(0)
            return
        }
        rawVoice.playRawBlocking(resId)
        return
    }

    if (stationTemplateId == StationTemplateId.ImageMatch &&
        SixStationArcQaPolicy.isSagaWhichWordStartsWithStation(chapterId, stationId) &&
        q is Question.ImageMatchQuestion
    ) {
        sfx.stopAllStreams()
        if (rawVoice == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(ImageMatchStation5) stage=rawVoice=null expectedInstructionRawRes!=null",
            )
            voice.playRequiredBlocking(
                assetPath = "",
                context = "playIntroPrompt(ImageMatchStation5,rawVoice=null)",
                chapterId = chapterId,
                stationId = stationId,
            )
            return
        }
        if (chapter1PlayerAddress == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(ImageMatchStation5) stage=playerAddress=null expectedInstructionKind=WhichWordStartsWith",
            )
            rawVoice.playRawBlocking(0)
            return
        }
        rawVoice.playRawBlocking(
            Chapter1AddressAwareAudio.instructionRawRes(
                kind = Chapter1AddressAwareAudio.InstructionKind.WhichWordStartsWith,
                address = chapter1PlayerAddress,
            ),
        )
        val letterResId = AudioClips.letterNameRawResId(q.targetLetter)
        if (letterResId == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(ImageMatchStation5) stage=missing raw letter-name mapping targetLetter='${q.targetLetter}'",
            )
            rawVoice.playRawBlocking(0)
            return
        }
        rawVoice.playRawBlocking(letterResId)
        return
    }

    if (stationTemplateId == StationTemplateId.MatchLetterToWord && q is Question.ImageMatchQuestion) {
        sfx.stopAllStreams()
        if (rawVoice == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(MatchLetterToWord) stage=rawVoice=null expectedInstructionRawRes!=null",
            )
            voice.playRequiredBlocking(
                assetPath = "",
                context = "playIntroPrompt(MatchLetterToWord,rawVoice=null)",
                chapterId = chapterId,
                stationId = stationId,
            )
            return
        }
        if (chapter1PlayerAddress == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(MatchLetterToWord) stage=playerAddress=null expectedInstructionKind=MatchLetterToWord",
            )
            rawVoice.playRawBlocking(0)
            return
        }
        rawVoice.playRawBlocking(
            Chapter1AddressAwareAudio.instructionRawRes(
                kind = Chapter1AddressAwareAudio.InstructionKind.MatchLetterToWord,
                address = chapter1PlayerAddress,
            ),
        )
        return
    }

    if (stationTemplateId == StationTemplateId.ImageToWord && q is Question.ImageMatchQuestion) {
        sfx.stopAllStreams()
        if (Season2StationAudio.isPictureToWordStation(chapterId, stationId)) {
            Season2StationAudio.speakPictureToWordRoundPrompt(
                chapterId = chapterId,
                stationId = stationId,
                catalogId = q.correctChoiceId,
                rawVoice = rawVoice,
                voice = voice,
            )
            return
        }
        voice.playRequiredBlocking(
            assetPath = AudioClips.ImageToWordInstructions,
            context = "playIntroPrompt(ImageToWord)",
            chapterId = chapterId,
            stationId = stationId,
        )
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
            }
        if (target != null) {
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
                    "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(station1) stage=rawVoice=null expectedInstructionRawRes!=null expectedLetterRawResId=$letterResId",
                )
                voice.playRequiredBlocking(
                    assetPath = "",
                    context = "playIntroPrompt(station1,rawVoice=null)",
                    chapterId = chapterId,
                    stationId = stationId,
                )
                return
            }
            if ((chapterId == 4 || chapterId == 5) &&
                stationId == Chapter1StationOrder.TAP_LETTER &&
                voice.hasAsset(AudioClips.VoBachorEtHaot)
            ) {
                sfx.stopAllStreams()
                voice.playBlocking(AudioClips.VoBachorEtHaot)
            } else {
                if (chapter1PlayerAddress == null) {
                    android.util.Log.e(
                        "MissingContent",
                        "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(station1) stage=playerAddress=null expectedInstructionKind=PickLetter",
                    )
                    rawVoice.playRawBlocking(0)
                    return
                }
                rawVoice.playRawBlocking(
                    Chapter1AddressAwareAudio.instructionRawRes(
                        kind = Chapter1AddressAwareAudio.InstructionKind.PickLetter,
                        address = chapter1PlayerAddress,
                    ),
                )
            }
            rawVoice.playRawBlocking(letterResId)
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
            playerAddress = chapter1PlayerAddress,
            rawVoice = rawVoice,
        )
        return
    }

    if (
        (chapterId == 3 || chapterId == 6 || Season2StationAudio.isSeason2WarmupChapter(chapterId)) &&
        stationId == 1 &&
        q is Question.PictureStartsWithQuestion
    ) {
        sfx.stopAllStreams()
        if (rawVoice == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(Ch3Ch6Station1Word) stage=rawVoice=null expectedInstructionRawRes!=null",
            )
            voice.playRequiredBlocking(
                assetPath = "",
                context = "playIntroPrompt(Ch3Ch6Station1Word,rawVoice=null)",
                chapterId = chapterId,
                stationId = stationId,
            )
            return
        }
        if (chapter1PlayerAddress == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(Ch3Ch6Station1Word) stage=playerAddress=null expectedInstructionKind=PictureStartsWith",
            )
            rawVoice.playRawBlocking(0)
            return
        }
        rawVoice.playRawBlocking(
            Chapter1AddressAwareAudio.instructionRawRes(
                kind = Chapter1AddressAwareAudio.InstructionKind.PictureStartsWith,
                address = chapter1PlayerAddress,
            ),
        )
        val wordResId = AudioClips.wordRawResIdByCatalogId(q.catalogEntryId)
        if (wordResId == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(Ch3Ch6Station1Word) stage=missing raw word mapping catalogId='${q.catalogEntryId}'",
            )
            rawVoice.playRawBlocking(0)
            return
        }
        rawVoice.playRawBlocking(wordResId)
        return
    }

    if (
        (chapterId == 3 || chapterId == 6 || Season2StationAudio.isSeason2WarmupChapter(chapterId)) &&
        stationId == 2 &&
        q is Question.ImageMatchQuestion
    ) {
        sfx.stopAllStreams()
        if (rawVoice == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(Ch3Ch6Station2MatchLetterToWord) stage=rawVoice=null expectedInstructionRawRes!=null",
            )
            voice.playRequiredBlocking(
                assetPath = "",
                context = "playIntroPrompt(Ch3Ch6Station2MatchLetterToWord,rawVoice=null)",
                chapterId = chapterId,
                stationId = stationId,
            )
            return
        }
        if (chapter1PlayerAddress == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(Ch3Ch6Station2MatchLetterToWord) stage=playerAddress=null expectedInstructionKind=MatchLetterToWord",
            )
            rawVoice.playRawBlocking(0)
            return
        }
        rawVoice.playRawBlocking(
            Chapter1AddressAwareAudio.instructionRawRes(
                kind = Chapter1AddressAwareAudio.InstructionKind.MatchLetterToWord,
                address = chapter1PlayerAddress,
            ),
        )
        return
    }

    if (
        Season2StationAudio.usesPictureStartsWithAddressAwareIntro(chapterId, isSagaEpisode) &&
        SixStationArcQaPolicy.isSagaPictureStartsWithStation(chapterId, stationId) &&
        q is Question.PictureStartsWithQuestion
    ) {
        if (rawVoice == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(Station4) stage=rawVoice=null expectedInstructionRawRes!=null expectedWordRawRes!=null",
            )
            voice.playRequiredBlocking(
                assetPath = "",
                context = "playIntroPrompt(Station4,rawVoice=null)",
                chapterId = chapterId,
                stationId = stationId,
            )
            return
        }
        if (chapter1PlayerAddress == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(Station4) stage=playerAddress=null expectedInstructionKind=PictureStartsWith",
            )
            rawVoice.playRawBlocking(0)
            return
        }
        val wordResId = AudioClips.wordRawResIdByCatalogId(q.catalogEntryId)
        if (wordResId == null) {
            android.util.Log.e(
                "MissingContent",
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(Station4) stage=missing raw word mapping catalogId='${q.catalogEntryId}'",
            )
            rawVoice.playRawBlocking(0)
            return
        }
        if (!listenOnlyTargetPrompt) {
            rawVoice.playRawBlocking(
                Chapter1AddressAwareAudio.instructionRawRes(
                    kind = Chapter1AddressAwareAudio.InstructionKind.PictureStartsWith,
                    address = chapter1PlayerAddress,
                ),
            )
        }
        rawVoice.playRawBlocking(wordResId)
        return
    }

    if ((sagaUsesPopBalloonsAudioStaging ||
            planPopAllLettersInWord ||
            (chapterId == 6 && stationId == 3)) &&
        q is Question.PopBalloonsQuestion
    ) {
        if (planPopAllLettersInWord) {
            sfx.stopAllStreams()
            val (_, catalogId) =
                session.chapter3PopAllLettersCurrentWord()
                    ?: error("Missing pop-all letters word for index ${session.currentIndex}")
            if (rawVoice == null) {
                android.util.Log.e(
                    "MissingContent",
                    "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(PopAllLettersInWord) stage=rawVoice=null expectedInstructionRawRes=${R.raw.instruction_pop_all_letters_in_word}",
                )
                voice.playRequiredBlocking(
                    assetPath = "",
                    context = "playIntroPrompt(PopAllLettersInWord,rawVoice=null)",
                    chapterId = chapterId,
                    stationId = stationId,
                )
                return
            }
            rawVoice.playRawBlocking(R.raw.instruction_pop_all_letters_in_word)
            if (chapterId == 3 || chapterId == 6 || chapterId == TrainingV1Config.CHAPTER_ID) {
                val resId = AudioClips.wordRawResIdByCatalogId(catalogId)
                if (resId == null) {
                    android.util.Log.e(
                        "MissingContent",
                        "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(PopAllLettersInWord) stage=missing raw word mapping catalogId='$catalogId'",
                    )
                    rawVoice.playRawBlocking(0)
                    return
                }
                rawVoice.playRawBlocking(resId)
            } else {
                val wordPath = AudioClips.wordClipByCatalogId(catalogId)
                if (voice.hasAsset(wordPath)) voice.playBlocking(wordPath)
            }
        } else {
            if (rawVoice == null) {
                android.util.Log.e(
                    "MissingContent",
                    "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(station2) stage=rawVoice=null expectedInstructionRawRes!=null expectedLetterRawRes!=null",
                )
                voice.playRequiredBlocking(
                    assetPath = "",
                    context = "playIntroPrompt(station2,rawVoice=null)",
                    chapterId = chapterId,
                    stationId = stationId,
                )
                return
            }
            if (chapter1PlayerAddress == null) {
                android.util.Log.e(
                    "MissingContent",
                    "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(station2) stage=playerAddress=null expectedInstructionKind=PopBalloons",
                )
                rawVoice.playRawBlocking(0)
                return
            }
            val letterResId = AudioClips.letterNameRawResId(q.correctAnswer)
            if (letterResId == null) {
                android.util.Log.e(
                    "MissingContent",
                    "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=playIntroPrompt(station2) stage=missing raw letter-name mapping targetLetter='${q.correctAnswer}'",
                )
                rawVoice.playRawBlocking(0)
                return
            }
            rawVoice.playRawBlocking(
                Chapter1AddressAwareAudio.instructionRawRes(
                    kind = Chapter1AddressAwareAudio.InstructionKind.PopBalloons,
                    address = chapter1PlayerAddress,
                ),
            )
            rawVoice.playRawBlocking(letterResId)
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
        playerAddress = chapter1PlayerAddress,
        rawVoice = rawVoice,
    )
}
