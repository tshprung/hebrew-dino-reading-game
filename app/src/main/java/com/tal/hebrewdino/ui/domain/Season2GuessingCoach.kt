package com.tal.hebrewdino.ui.domain



import android.util.Log

import com.tal.hebrewdino.ui.audio.AudioClips

import com.tal.hebrewdino.ui.audio.RawVoicePlayer

import com.tal.hebrewdino.ui.companion.Chapter1AddressAwareAudio

import com.tal.hebrewdino.ui.data.PlayerAddress

import kotlinx.coroutines.delay



/** Replays the current learning target after a companion coach intervention. */

object Season2GuessingCoach {

    suspend fun replayTargetAudio(
        uxStationId: Int,
        session: LevelSession,
        playerAddress: PlayerAddress?,
        rawVoice: RawVoicePlayer,
        gameplayChapterId: Int? = null,
        chapterId: Int = 0,
        stationUiSpec: StationUiSpec? = null,
    ) {
        val question = session.currentQuestion ?: return
        val arcKind =
            gameplayChapterId
                ?.takeIf { Season2StationAudio.isSeason2GameplayChapter(it) }
                ?.let { Season2StationUx.stationKindForGameplayChapter(it, uxStationId) }

        when {
            arcKind != null ->
                replayForArcStationKind(
                    kind = arcKind,
                    season2StationId = uxStationId,
                    question = question,
                    playerAddress = playerAddress,
                    rawVoice = rawVoice,
                    gameplayChapterId = gameplayChapterId ?: 0,
                )
            CompanionCoachPolicy.isSeason1Chapter(chapterId) && stationUiSpec != null ->
                replayForSeason1Template(
                    uxStationId = uxStationId,
                    stationUiSpec = stationUiSpec,
                    question = question,
                    playerAddress = playerAddress,
                    rawVoice = rawVoice,
                )
            else ->
                replayByLegacyStationOrder(
                    season2StationId = uxStationId,
                    question = question,
                    playerAddress = playerAddress,
                    rawVoice = rawVoice,
                    gameplayChapterId = gameplayChapterId,
                )
        }

        delay(280)
    }

    /** Pop-balloons coach: target letter only (no narrator instruction replay). */
    suspend fun replayPopBalloonsTargetLetterOnly(
        session: LevelSession,
        rawVoice: RawVoicePlayer,
        uxStationId: Int = 0,
    ) {
        val question = session.currentQuestion ?: return
        playTargetLetter(question, rawVoice, uxStationId)
        delay(280)
    }

    private suspend fun replayForSeason1Template(
        uxStationId: Int,
        stationUiSpec: StationUiSpec,
        question: Question,
        playerAddress: PlayerAddress?,
        rawVoice: RawVoicePlayer,
    ) {
        when (stationUiSpec.templateId) {
            StationTemplateId.DragWordToPicture -> {
                if (
                    Season1StationAudio.isSeason1DragWordToPictureStation(
                        stationUiSpec.chapterId,
                        stationUiSpec.stationId,
                    )
                ) {
                    Season1StationAudio.playDragWordToPictureInstruction(
                        rawVoice = rawVoice,
                        chapterId = stationUiSpec.chapterId,
                        stationId = stationUiSpec.stationId,
                        context = "Season2GuessingCoach.replayForSeason1Template(DragWordToPicture)",
                    )
                    return
                }
                val q = question as? Question.DragWordToPictureQuestion ?: return
                val catalogId = q.pairs.firstOrNull()?.catalogEntryId ?: return
                playWordRaw(catalogId, rawVoice, uxStationId)
            }
            StationTemplateId.DragMissingLetter -> {
                if (
                    Season1StationAudio.isSeason1DragMissingLetterStation(
                        stationUiSpec.chapterId,
                        stationUiSpec.stationId,
                    )
                ) {
                    Season1StationAudio.playDragMissingLetterInstruction(
                        rawVoice = rawVoice,
                        chapterId = stationUiSpec.chapterId,
                        stationId = stationUiSpec.stationId,
                        context = "Season2GuessingCoach.replayForSeason1Template(DragMissingLetter)",
                    )
                    return
                }
                val q = question as? Question.DragMissingLetterQuestion ?: return
                playWordRaw(q.catalogEntryId, rawVoice, uxStationId)
            }
            StationTemplateId.ImageToWord -> {
                val q = question as? Question.ImageMatchQuestion ?: return
                rawVoice.playRawBlocking(com.tal.hebrewdino.R.raw.instruction_image_to_word)
                playWordRaw(q.correctChoiceId, rawVoice, uxStationId)
            }
            else -> {
                val kind =
                    Chapter1AddressAwareAudio.instructionKindFor(
                        stationId = uxStationId,
                        stationTemplateId = stationUiSpec.templateId,
                        q = question,
                    )
                when (kind) {
                    Chapter1AddressAwareAudio.InstructionKind.PictureStartsWith ->
                        playInstructionThenWord(
                            question = question,
                            playerAddress = playerAddress,
                            rawVoice = rawVoice,
                            kind = kind,
                            postInstructionGapMs = Season2Ch1QaPolicy.CoachInstructionToWordGapMs,
                        )
                    Chapter1AddressAwareAudio.InstructionKind.WhichWordStartsWith,
                    Chapter1AddressAwareAudio.InstructionKind.MatchLetterToWord,
                    Chapter1AddressAwareAudio.InstructionKind.FindLetter,
                    Chapter1AddressAwareAudio.InstructionKind.PickLetter,
                    Chapter1AddressAwareAudio.InstructionKind.PopBalloons,
                    ->
                        playInstructionThenLetter(
                            season2StationId = uxStationId,
                            question = question,
                            playerAddress = playerAddress,
                            rawVoice = rawVoice,
                            kind = kind,
                        )
                    Chapter1AddressAwareAudio.InstructionKind.FindWordStartsWith -> {
                        playInstructionRaw(playerAddress, rawVoice, kind)
                        val imageQ = question as? Question.ImageMatchQuestion
                        if (imageQ != null) {
                            playWordRaw(imageQ.correctChoiceId, rawVoice, uxStationId)
                        } else {
                            playTargetLetter(question, rawVoice, uxStationId)
                        }
                    }
                    else -> playTargetLetter(question, rawVoice, uxStationId)
                }
            }
        }
    }

    private suspend fun playWordRaw(
        catalogId: String,
        rawVoice: RawVoicePlayer,
        uxStationId: Int,
    ) {
        val wordResId = AudioClips.wordRawResIdByCatalogId(catalogId)
        if (wordResId == null) {
            Log.e(
                "MissingContent",
                "Missing coach word audio. stationId=$uxStationId catalogId='$catalogId'",
            )
            return
        }
        rawVoice.playRawBlocking(wordResId)
    }



    private suspend fun replayForArcStationKind(

        kind: Season2ChapterStationPlans.StationKind,

        season2StationId: Int,

        question: Question,

        playerAddress: PlayerAddress?,

        rawVoice: RawVoicePlayer,

        gameplayChapterId: Int,

    ) {

        when (kind) {

            Season2ChapterStationPlans.StationKind.PopBalloons,

            Season2ChapterStationPlans.StationKind.PickLetter,

            -> playTargetLetter(question, rawVoice, season2StationId)



            Season2ChapterStationPlans.StationKind.PictureStartsWith ->
                if (Season2EarlyStationQaPolicy.shouldReplayWordForPictureStartsWithCoach(season2StationId)) {
                    playInstructionThenWord(
                        question = question,
                        playerAddress = playerAddress,
                        rawVoice = rawVoice,
                        kind = Chapter1AddressAwareAudio.InstructionKind.PictureStartsWith,
                    )
                } else {
                    playInstructionThenLetter(
                        season2StationId = season2StationId,
                        question = question,
                        playerAddress = playerAddress,
                        rawVoice = rawVoice,
                        kind = Chapter1AddressAwareAudio.InstructionKind.PictureStartsWith,
                    )
                }



            Season2ChapterStationPlans.StationKind.WhichWordStartsWith ->

                playInstructionThenLetter(

                    season2StationId = season2StationId,

                    question = question,

                    playerAddress = playerAddress,

                    rawVoice = rawVoice,

                    kind = Chapter1AddressAwareAudio.InstructionKind.WhichWordStartsWith,

                )



            Season2ChapterStationPlans.StationKind.MatchLetterToWord ->

                playInstructionThenLetter(

                    season2StationId = season2StationId,

                    question = question,

                    playerAddress = playerAddress,

                    rawVoice = rawVoice,

                    kind = Chapter1AddressAwareAudio.InstructionKind.MatchLetterToWord,

                )



            Season2ChapterStationPlans.StationKind.MemoryMatch ->

                playMemoryInstruction(playerAddress, rawVoice)



            Season2ChapterStationPlans.StationKind.PictureToWord ->
                if (
                    question is Question.ImageMatchQuestion &&
                        Season2StationQaPolicy.shouldReplayPictureToWordCoachWithInstruction(
                            gameplayChapterId = gameplayChapterId,
                            season2UxStationId = season2StationId,
                        )
                ) {
                    Season2StationAudio.replayPictureToWordCoachInstructionAndWord(
                        chapterId = gameplayChapterId,
                        stationId = season2StationId,
                        catalogId = question.correctChoiceId,
                        rawVoice = rawVoice,
                    )
                } else {
                    replayAdvancedTargetAudio(
                        question = question,
                        rawVoice = rawVoice,
                        chapterId = gameplayChapterId,
                        stationId = season2StationId,
                    )
                }

            Season2ChapterStationPlans.StationKind.WordParts,

            Season2ChapterStationPlans.StationKind.MissingFirstLetter,

            Season2ChapterStationPlans.StationKind.Rhyming,

            ->

                        replayAdvancedTargetAudio(

                            question = question,

                            rawVoice = rawVoice,

                            chapterId = gameplayChapterId,

                            stationId = season2StationId,

                        )

            Season2ChapterStationPlans.StationKind.DragWordToPicture,
            Season2ChapterStationPlans.StationKind.DragMissingLetter,
            -> Unit
        }

    }



    private suspend fun replayByLegacyStationOrder(

        season2StationId: Int,

        question: Question,

        playerAddress: PlayerAddress?,

        rawVoice: RawVoicePlayer,

        gameplayChapterId: Int?,

    ) {

        when (season2StationId) {

            Season2Chapter1StationOrder.POP_BALLOONS,

            Season2Chapter1StationOrder.PICK_LETTER,

            -> playTargetLetter(question, rawVoice, season2StationId)



            Season2Chapter1StationOrder.PICTURE_STARTS_WITH -> {
                playInstructionThenWord(
                    question = question,
                    playerAddress = playerAddress,
                    rawVoice = rawVoice,
                    kind = Chapter1AddressAwareAudio.InstructionKind.PictureStartsWith,
                    postInstructionGapMs = Season2Ch1QaPolicy.CoachInstructionToWordGapMs,
                )
            }



            Season2Chapter1StationOrder.WHICH_WORD_STARTS_WITH ->

                playInstructionThenLetter(

                    season2StationId = season2StationId,

                    question = question,

                    playerAddress = playerAddress,

                    rawVoice = rawVoice,

                    kind = Chapter1AddressAwareAudio.InstructionKind.WhichWordStartsWith,

                )



            Season2Chapter1StationOrder.FINALE_STATION,
            Season2Chapter1StationOrder.MATCH_LETTER_TO_WORD,
            ->
                when (question) {
                    is Question.ImageMatchQuestion ->
                        if (
                            Season2StationQaPolicy.shouldReplayPictureToWordCoachWithInstruction(
                                gameplayChapterId = gameplayChapterId ?: Season2ChapterIds.Chapter1Tyrannosaurus,
                                season2UxStationId = season2StationId,
                            )
                        ) {
                            Season2StationAudio.replayPictureToWordCoachInstructionAndWord(
                                chapterId = gameplayChapterId ?: Season2ChapterIds.Chapter1Tyrannosaurus,
                                stationId = season2StationId,
                                catalogId = question.correctChoiceId,
                                rawVoice = rawVoice,
                            )
                        } else {
                            replayAdvancedTargetAudio(
                                question = question,
                                rawVoice = rawVoice,
                                chapterId = gameplayChapterId ?: Season2ChapterIds.Chapter1Tyrannosaurus,
                                stationId = season2StationId,
                            )
                        }
                    is Question.WordPartsQuestion,
                    ->
                        replayAdvancedTargetAudio(
                            question = question,
                            rawVoice = rawVoice,
                            chapterId = gameplayChapterId ?: Season2ChapterIds.Chapter1Tyrannosaurus,
                            stationId = season2StationId,
                        )
                    else ->
                        playInstructionThenLetter(
                            season2StationId = season2StationId,
                            question = question,
                            playerAddress = playerAddress,
                            rawVoice = rawVoice,
                            kind = Chapter1AddressAwareAudio.InstructionKind.MatchLetterToWord,
                        )
                }



            Season2Chapter1StationOrder.MEMORY_MATCH -> {

                playMemoryInstruction(playerAddress, rawVoice)

            }



            else -> {

                if (question is Question.MissingFirstLetterQuestion ||

                    question is Question.WordPartsQuestion ||

                    question is Question.RhymingQuestion ||

                    (

                        question is Question.ImageMatchQuestion &&

                            season2StationId in

                                listOf(

                                    Season2Chapter1StationOrder.WHICH_WORD_STARTS_WITH,

                                    5,

                                    6,

                                )

                    )

                ) {

                    replayAdvancedTargetAudio(

                        question = question,

                        rawVoice = rawVoice,

                        chapterId = 0,

                        stationId = season2StationId,

                    )

                } else {

                    playTargetLetter(question, rawVoice, season2StationId)

                }

            }

        }

    }



    private suspend fun playTargetLetter(

        question: Question,

        rawVoice: RawVoicePlayer,

        season2StationId: Int,

    ) {

        val letter = targetLetterFromQuestion(question) ?: return

        val resId = AudioClips.letterNameRawResId(letter)

        if (resId == null) {

            Log.e(

                "MissingContent",

                "Missing Season2 coach letter audio. stationId=$season2StationId letter='$letter'",

            )

            return

        }

        rawVoice.playRawBlocking(resId)

    }



    private suspend fun playInstructionThenLetter(
        season2StationId: Int,
        question: Question,
        playerAddress: PlayerAddress?,
        rawVoice: RawVoicePlayer,
        kind: Chapter1AddressAwareAudio.InstructionKind,
    ) {
        playInstructionRaw(playerAddress, rawVoice, kind)
        playTargetLetter(question, rawVoice, season2StationId)
    }

    private suspend fun playInstructionThenWord(
        question: Question,
        playerAddress: PlayerAddress?,
        rawVoice: RawVoicePlayer,
        kind: Chapter1AddressAwareAudio.InstructionKind,
        postInstructionGapMs: Long = 200L,
    ) {
        playInstructionRaw(playerAddress, rawVoice, kind, postInstructionGapMs)
        val pictureQuestion = question as? Question.PictureStartsWithQuestion
        if (pictureQuestion != null) {
            val wordResId = AudioClips.wordRawResIdByCatalogId(pictureQuestion.catalogEntryId)
            if (wordResId == null) {
                Log.e(
                    "MissingContent",
                    "Missing Season2 coach picture word audio. catalogId='${pictureQuestion.catalogEntryId}'",
                )
                return
            }
            rawVoice.playRawBlocking(wordResId)
        } else {
            playTargetLetter(question, rawVoice, season2StationId = Season2Chapter1StationOrder.PICTURE_STARTS_WITH)
        }
    }

    private suspend fun playInstructionRaw(
        playerAddress: PlayerAddress?,
        rawVoice: RawVoicePlayer,
        kind: Chapter1AddressAwareAudio.InstructionKind,
        postInstructionGapMs: Long = 200L,
    ) {
        if (playerAddress == null) return
        val instructionRes = Chapter1AddressAwareAudio.instructionRawRes(kind, playerAddress)
        if (instructionRes != 0) {
            rawVoice.playRawBlocking(instructionRes)
            if (postInstructionGapMs > 0) {
                delay(postInstructionGapMs)
            }
        }
    }




    private suspend fun playMemoryInstruction(

        playerAddress: PlayerAddress?,

        rawVoice: RawVoicePlayer,

    ) {

        if (playerAddress == null) return

        val resId =

            Chapter1AddressAwareAudio.instructionRawRes(

                Chapter1AddressAwareAudio.InstructionKind.MemoryMatch,

                playerAddress,

            )

        if (resId == 0) {

            Log.e(

                "MissingContent",

                "Missing Season2 coach memory-match instruction. expected instruction_memory_match_boy/girl.mp3",

            )

            return

        }

        rawVoice.playRawBlocking(resId)

    }



    suspend fun replayAdvancedTargetAudio(

        question: Question,

        rawVoice: RawVoicePlayer,

        chapterId: Int,

        stationId: Int,

    ) {

        val catalogId =

            when (question) {

                is Question.MissingFirstLetterQuestion -> question.catalogEntryId

                is Question.WordPartsQuestion -> question.catalogEntryId

                is Question.RhymingQuestion -> question.targetCatalogEntryId

                is Question.ImageMatchQuestion -> question.correctChoiceId

                else -> null

            }

        if (catalogId == null) {

            playTargetLetter(question, rawVoice, stationId)

            return

        }

        val resId = AudioClips.wordRawResIdByCatalogId(catalogId)

        if (resId == null) {

            Log.e(

                "MissingContent",

                "Missing Season2 coach word audio. chapterId=$chapterId stationId=$stationId catalogId='$catalogId'",

            )

            return

        }

        rawVoice.playRawBlocking(resId)
    }



    private fun targetLetterFromQuestion(question: Question): String? =

        when (question) {

            is Question.PopBalloonsQuestion -> question.correctAnswer

            is Question.FindLetterGridQuestion -> question.targetLetter

            is Question.PictureStartsWithQuestion -> question.correctLetter

            is Question.ImageMatchQuestion -> question.targetLetter

            is Question.MissingFirstLetterQuestion -> question.correctLetter

            is Question.WordPartsQuestion -> question.word.first().toString()

            is Question.RhymingQuestion ->

                question.choices.firstOrNull { it.id == question.correctChoiceId }?.letter

            else -> null

        }

}


