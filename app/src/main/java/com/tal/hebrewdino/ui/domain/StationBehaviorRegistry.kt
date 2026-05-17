package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.BALLOON_POP
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.PICTURE_PICK_ALL
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.PICTURE_PICK_ONE
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.REVEAL_THEN_CHOOSE
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.TAP_LETTER

/**
 * Central lookup for low-risk per-station UI flags. Must stay aligned with [StationQuizPlans].
 */
object StationBehaviorRegistry {

    private fun variantsFor(
        listenOnly: Boolean,
        vararg extras: StationVariant,
    ): Set<StationVariant> =
        buildSet {
            add(StationVariant.Standard)
            if (listenOnly) add(StationVariant.ListenFirst)
            addAll(extras)
        }

    fun getStationUiSpec(chapterId: Int, stationId: Int): StationUiSpec {
        val sid = stationId.coerceIn(1, Chapter1Config.STATION_COUNT)
        val plan =
            when (chapterId) {
                1 -> StationQuizPlans.chapter1(sid)
                2 -> StationQuizPlans.chapter2(sid)
                3 -> StationQuizPlans.chapter3(sid)
                4 -> StationQuizPlans.chapter4(sid)
                5 -> StationQuizPlans.chapter5(sid)
                6 -> StationQuizPlans.chapter6(stationId.coerceIn(1, Chapter6Config.STATION_COUNT))
                TrainingV1Config.CHAPTER_ID -> StationQuizPlans.trainingV1(sid)
                else -> error("Unsupported chapterId=$chapterId")
            }
        return when (chapterId) {
            1 -> sixStationUiSpec(chapterId = 1, sid, plan)
            2 -> sixStationUiSpec(chapterId = 2, sid, plan)
            3 -> chapter3UiSpec(sid, plan)
            4 -> episode4UiSpec(sid, plan)
            5 -> sixStationUiSpec(chapterId = 5, sid, plan)
            6 -> chapter6UiSpec(stationId.coerceIn(1, Chapter6Config.STATION_COUNT), plan)
            TrainingV1Config.CHAPTER_ID -> trainingV1UiSpec(sid, plan)
            else -> error("Unsupported chapterId=$chapterId")
        }
    }

    private fun pickLetterSpec(
        chapterId: Int,
        stationId: Int,
        plan: StationQuizPlan,
        extraVariants: Array<StationVariant> = emptyArray()
    ): StationUiSpec {
        val listenOnly = plan.listenOnlyTargetPrompt
        return StationUiSpec(
            chapterId = chapterId,
            stationId = stationId,
            templateId = StationTemplateId.PickLetter,
            variants = variantsFor(listenOnly, *extraVariants),
            quizMode = plan.mode,
            findGridMaxTargetCount = plan.findLetterGridMaxTargetCount,
            helpControlsEnabled = listenOnly,
            replayMode = if (listenOnly) StationReplayMode.TargetLetterOnly else StationReplayMode.None,
            hintMode = if (listenOnly) StationHintMode.TemporaryTargetLetter else StationHintMode.None,
            hintDurationMs = if (listenOnly) 2100L else null,
            pickLetterInstructionOverride = StationInstructionCopy.PickLetterSagaStation1Preamble,
            pickLetterSagaStation1CompactPreamble = null,
            pickLetterAllowPinnedCorrectShortcut = true,
            pickLetterListenOnlyHebrewPanel = false,
            // Ch6 st1 needs slightly more inset
            contentTopInsetDp = if (chapterId == 6 && stationId == 1) 72f else null
        )
    }

    private fun matchLetterToWordSpec(
        chapterId: Int,
        stationId: Int,
        plan: StationQuizPlan,
        extraVariants: Array<StationVariant> = emptyArray(),
    ): StationUiSpec {
        val listenOnly = plan.listenOnlyTargetPrompt
        return StationUiSpec(
            chapterId = chapterId,
            stationId = stationId,
            templateId = StationTemplateId.MatchLetterToWord,
            variants = variantsFor(listenOnly, *extraVariants),
            quizMode = plan.mode,
            findGridMaxTargetCount = plan.findLetterGridMaxTargetCount,
            helpControlsEnabled = false,
            replayMode = StationReplayMode.None,
            hintMode = StationHintMode.None,
            matchLetterInstructionReadablePanel = true,
            matchLetterInstructionText = StationInstructionCopy.MatchLetterFinale,
        )
    }

    private fun findLetterGridSpec(
        chapterId: Int,
        stationId: Int,
        plan: StationQuizPlan
    ): StationUiSpec {
        val listenOnly = plan.listenOnlyTargetPrompt
        val findGridSagaRevealStation =
            (chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5) &&
                stationId == REVEAL_THEN_CHOOSE
        return StationUiSpec(
            chapterId = chapterId,
            stationId = stationId,
            templateId = StationTemplateId.FindLetterGrid,
            variants = variantsFor(listenOnly),
            quizMode = plan.mode,
            findGridMaxTargetCount = plan.findLetterGridMaxTargetCount,
            helpControlsEnabled = listenOnly,
            replayMode = if (listenOnly) StationReplayMode.TargetLetterOnly else StationReplayMode.None,
            hintMode = if (listenOnly) StationHintMode.TemporaryTargetLetter else StationHintMode.None,
            hintDurationMs = if (listenOnly) 2100L else null,
            findGridInlineInstructionOverride = "מצא את האות:",
            findGridInlineInstructionPanelStyle = InstructionPanelStyle.WhiteRounded,
            findGridSuppressHeaderTargetLetter = false,
            findGridHideListenOnlyHeaderTargetLetter = listenOnly,
            findGridSagaRevealStation = findGridSagaRevealStation,
        )
    }

    private fun pictureStartsWithSpec(
        chapterId: Int,
        stationId: Int,
        plan: StationQuizPlan
    ): StationUiSpec {
        val listenOnly = plan.listenOnlyTargetPrompt
        val pictureStartsWithCompactLandscapeRtlWrapInstruction =
            (chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5) &&
                stationId == PICTURE_PICK_ONE
        val pictureStartsWithVerticalNudgeDp =
            if ((chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5) &&
                stationId == PICTURE_PICK_ONE
            ) {
                19f
            } else {
                0f
            }
        return StationUiSpec(
            chapterId = chapterId,
            stationId = stationId,
            templateId = StationTemplateId.PictureStartsWith,
            variants = variantsFor(listenOnly),
            quizMode = plan.mode,
            findGridMaxTargetCount = plan.findLetterGridMaxTargetCount,
            helpControlsEnabled = listenOnly,
            replayMode = if (listenOnly) StationReplayMode.TargetWordOnly else StationReplayMode.None,
            hintMode = if (listenOnly) StationHintMode.TemporaryStartingLetter else StationHintMode.None,
            hintDurationMs = if (listenOnly) 2100L else null,
            pictureStartsWithInstructionOverride = "באיזו אות מתחילה המילה:",
            pictureStartsWithCompactLandscapeRtlWrapInstruction = pictureStartsWithCompactLandscapeRtlWrapInstruction,
            pictureStartsWithReadablePanel = true,
            pictureStartsWithInstructionPanelStyle = InstructionPanelStyle.WhiteRounded,
            hidePictureWordCaptionWhenListenOnlySaga = listenOnly,
            pictureStartsWithVerticalNudgeDp = pictureStartsWithVerticalNudgeDp,
        )
    }

    private fun popBalloonsSpec(
        chapterId: Int,
        stationId: Int,
        plan: StationQuizPlan,
        isPopAllLetters: Boolean = false
    ): StationUiSpec {
        val listenOnly = plan.listenOnlyTargetPrompt
        val popBalloonsCompactLandscapePhoneTuning =
            (chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5) && stationId == BALLOON_POP ||
                ((chapterId == 3 || chapterId == 6) && stationId == 3) ||
                (chapterId == TrainingV1Config.CHAPTER_ID && stationId == TrainingV1Config.STATION_WORD_BALLOONS)
        return StationUiSpec(
            chapterId = chapterId,
            stationId = stationId,
            templateId = StationTemplateId.PopBalloons,
            variants = if (isPopAllLetters) {
                variantsFor(listenOnly, StationVariant.PopAllLettersInWord)
            } else {
                variantsFor(listenOnly)
            },
            quizMode = plan.mode,
            findGridMaxTargetCount = plan.findLetterGridMaxTargetCount,
            helpControlsEnabled = listenOnly,
            replayMode = if (listenOnly) StationReplayMode.TargetLetterOnly else StationReplayMode.None,
            hintMode = if (listenOnly) StationHintMode.TemporaryTargetLetter else StationHintMode.None,
            hintDurationMs = if (listenOnly) 2100L else null,
            balloonInstructionOverride = if (isPopAllLetters) {
                null // Handled by banner in PopBalloonsStationContent
            } else {
                "פוצץ את הבלונים עם האות:"
            },
            popBalloonsSkipInstructionHeaderBlock = isPopAllLetters,
            popBalloonsPopAllLettersBannerInstruction = if (isPopAllLetters) {
                "פוצץ את כל הבלונים עם אותיות שמופיעות במילה:"
            } else {
                null
            },
            useEpisode4BalloonInstructionPanel = listenOnly,
            balloonPlayAreaStartInsetDp = if (listenOnly) 96f else 0f,
            excludeFullScreenBalloonHintOverlay = listenOnly,
            popBalloonsCompactLandscapePhoneTuning = popBalloonsCompactLandscapePhoneTuning,
            popBalloonsShowSagaStation2InstructionLine = !isPopAllLetters
        )
    }

    /** Chapters 1, 2, 5 share [Chapter1StationOrder]; Ch5 is listen-only saga. */
    private fun sixStationUiSpec(chapterId: Int, stationId: Int, plan: StationQuizPlan): StationUiSpec {
        val listenOnly = plan.listenOnlyTargetPrompt
        return when (stationId) {
            TAP_LETTER -> pickLetterSpec(
                chapterId,
                stationId,
                plan
            )
            BALLOON_POP -> popBalloonsSpec(chapterId, stationId, plan)
            REVEAL_THEN_CHOOSE -> findLetterGridSpec(chapterId, stationId, plan)
            PICTURE_PICK_ONE -> pictureStartsWithSpec(chapterId, stationId, plan)
            PICTURE_PICK_ALL ->
                StationUiSpec(
                    chapterId = chapterId,
                    stationId = stationId,
                    templateId = StationTemplateId.ImageMatch,
                    variants = variantsFor(listenOnly),
                    quizMode = plan.mode,
                    findGridMaxTargetCount = plan.findLetterGridMaxTargetCount,
                    helpControlsEnabled = listenOnly,
                    replayMode =
                        if (listenOnly) {
                            StationReplayMode.TargetLetterOnly
                        } else {
                            StationReplayMode.None
                        },
                    hintMode =
                        if (listenOnly) {
                            StationHintMode.TemporaryTargetLetter
                        } else {
                            StationHintMode.None
                        },
                    hintDurationMs =
                        if (listenOnly) {
                            // Product: Chapter 5 station 5 hint should be ~30% shorter (if it was listen-only).
                            1470L
                        } else {
                            null
                        },
                    imageMatchShowTargetLetterChip = !listenOnly,
                    imageMatchHeaderInstructionOverride =
                        StationInstructionCopy.ImageMatchFindWordStartingWithLetter,
                    imageMatchHeaderReadablePanel = true,
                    riskNotes = "Image match three cards; Learning mode (Ch1, 2, 5).",
                )
            FINALE_PICTURE_LETTER_MATCH ->
                matchLetterToWordSpec(
                    chapterId = chapterId,
                    stationId = stationId,
                    plan = plan,
                    extraVariants = arrayOf(StationVariant.Finale),
                ).copy(riskNotes = "Finale match UI; unified MatchLetterToWord template (Ch1, 2, 5).")
            else -> error("Unexpected stationId=$stationId")
        }
    }

    private fun chapter3UiSpec(stationId: Int, plan: StationQuizPlan): StationUiSpec {
        return when (stationId) {
            1 -> pictureStartsWithSpec(chapterId = 3, stationId = stationId, plan = plan)
            2 ->
                matchLetterToWordSpec(
                    chapterId = 3,
                    stationId = stationId,
                    plan = plan,
                ).copy(
                    contentTopInsetDp = 56f,
                    riskNotes = "Ch3 st2 match letter + word; unified MatchLetterToWord template.",
                )
            3 -> popBalloonsSpec(chapterId = 3, stationId = stationId, plan = plan, isPopAllLetters = false)
            4 ->
                pickLetterSpec(
                    chapterId = 3,
                    stationId = stationId,
                    plan = plan,
                    extraVariants = arrayOf(StationVariant.HighlightedLetterInWord, StationVariant.Episode4Help),
                ).copy(
                    variants = variantsFor(listenOnly = false, StationVariant.HighlightedLetterInWord, StationVariant.Episode4Help),
                    helpControlsEnabled = true,
                    replayMode = StationReplayMode.ExistingStationSpecific,
                    hintMode = StationHintMode.None,
                    hintDurationMs = null,
                    pickLetterInstructionOverride = null,
                    pickLetterHighlightedInWordInstruction = StationInstructionCopy.PickLetterHighlightedInWord,
                    contentTopInsetDp = 56f,
                    riskNotes = "Ch3 st4 highlighted letter in word.",
                )
            5 ->
                pickLetterSpec(
                    chapterId = 3,
                    stationId = stationId,
                    plan = plan,
                    extraVariants = arrayOf(StationVariant.Chapter3AudioLetterRecognition, StationVariant.Episode4Help)
                ).copy(
                    contentTopInsetDp = 56f,
                    riskNotes = "Ch3 st5 audio recognition; uses unified pickLetterSpec.",
                )
            6 ->
                StationUiSpec(
                    chapterId = 3,
                    stationId = stationId,
                    templateId = StationTemplateId.ImageToWord,
                    variants = variantsFor(listenOnly = false, StationVariant.Chapter3ImageToWord),
                    quizMode = plan.mode,
                    findGridMaxTargetCount = plan.findLetterGridMaxTargetCount,
                    helpControlsEnabled = false,
                    replayMode = StationReplayMode.ExistingStationSpecific,
                    hintMode = StationHintMode.ExistingStationSpecific,
                    imageToWordInstructionText = StationInstructionCopy.Chapter3ImageToWord,
                    contentTopInsetDp = 56f,
                    riskNotes = "Ch3 st6 uses ImageToWordGame (instruction from spec).",
                )
            else -> error("Unexpected Ch3 stationId=$stationId")
        }
    }

    private fun trainingV1UiSpec(stationId: Int, plan: StationQuizPlan): StationUiSpec {
        return when (stationId) {
            TrainingV1Config.STATION_HEAR_LETTER_CHOOSE ->
                pickLetterSpec(
                    chapterId = TrainingV1Config.CHAPTER_ID,
                    stationId = stationId,
                    plan = plan,
                    extraVariants = arrayOf(StationVariant.Episode4Help),
                ).copy(
                    helpControlsEnabled = true,
                    replayMode = StationReplayMode.TargetLetterOnly,
                    hintMode = StationHintMode.TemporaryTargetLetter,
                    hintDurationMs = 2100L,
                    pickLetterInstructionOverride = StationInstructionCopy.TrainingHearLetterChoose,
                    contentTopInsetDp = 72f,
                )
            TrainingV1Config.STATION_WHICH_WORD_STARTS_WITH_LETTER ->
                StationUiSpec(
                    chapterId = TrainingV1Config.CHAPTER_ID,
                    stationId = stationId,
                    templateId = StationTemplateId.ImageMatch,
                    variants = variantsFor(listenOnly = false, StationVariant.Episode4Help),
                    quizMode = plan.mode,
                    findGridMaxTargetCount = plan.findLetterGridMaxTargetCount,
                    helpControlsEnabled = true,
                    replayMode = StationReplayMode.TargetLetterOnly,
                    hintMode = StationHintMode.TemporaryTargetLetter,
                    hintDurationMs = 2100L,
                    imageMatchShowTargetLetterChip = false,
                    imageMatchHeaderInstructionOverride = StationInstructionCopy.TrainingWhichWordStartsWithLetter,
                    imageMatchHeaderReadablePanel = true,
                    contentTopInsetDp = 72f,
                )
            TrainingV1Config.STATION_PICTURE_CHOOSE_WORD ->
                StationUiSpec(
                    chapterId = TrainingV1Config.CHAPTER_ID,
                    stationId = stationId,
                    templateId = StationTemplateId.ImageToWord,
                    variants = variantsFor(listenOnly = false),
                    quizMode = plan.mode,
                    findGridMaxTargetCount = plan.findLetterGridMaxTargetCount,
                    helpControlsEnabled = false,
                    replayMode = StationReplayMode.None,
                    hintMode = StationHintMode.None,
                    imageToWordInstructionText = StationInstructionCopy.TrainingChooseWordForPicture,
                    contentTopInsetDp = 56f,
                )
            TrainingV1Config.STATION_FIND_HEARD_LETTER_IN_GRID ->
                StationUiSpec(
                    chapterId = TrainingV1Config.CHAPTER_ID,
                    stationId = stationId,
                    templateId = StationTemplateId.FindLetterGrid,
                    variants = variantsFor(listenOnly = true, StationVariant.Episode4Help),
                    quizMode = plan.mode,
                    findGridMaxTargetCount = plan.findLetterGridMaxTargetCount,
                    helpControlsEnabled = true,
                    hintDurationMs = 2100L,
                    replayMode = StationReplayMode.TargetLetterOnly,
                    hintMode = StationHintMode.TemporaryTargetLetter,
                    findGridInlineInstructionOverride = StationInstructionCopy.TrainingFindLetterInGrid,
                    findGridInlineInstructionPanelStyle = InstructionPanelStyle.WhiteRounded,
                    findGridSuppressHeaderTargetLetter = true,
                    findGridHideListenOnlyHeaderTargetLetter = true,
                    contentTopInsetDp = 56f,
                )
            TrainingV1Config.STATION_WORD_BALLOONS ->
                popBalloonsSpec(
                    chapterId = TrainingV1Config.CHAPTER_ID,
                    stationId = stationId,
                    plan = plan,
                    isPopAllLetters = false,
                ).copy(
                    helpControlsEnabled = false,
                    replayMode = StationReplayMode.None,
                    hintMode = StationHintMode.None,
                    hintDurationMs = null,
                    balloonInstructionOverride = "פוצץ את כל הבלונים עם האות:",
                    contentTopInsetDp = 56f,
                )
            TrainingV1Config.STATION_MATCH_LETTER_TO_WORD ->
                matchLetterToWordSpec(
                    chapterId = TrainingV1Config.CHAPTER_ID,
                    stationId = stationId,
                    plan = plan,
                    extraVariants = arrayOf(StationVariant.Finale),
                )
            else -> error("Unexpected Training v1 stationId=$stationId")
        }
    }

    private fun episode4UiSpec(stationId: Int, plan: StationQuizPlan): StationUiSpec {
        val listenOnly = plan.listenOnlyTargetPrompt
        return when (stationId) {
            TAP_LETTER ->
                pickLetterSpec(
                    chapterId = 4,
                    stationId = stationId,
                    plan = plan
                ).copy(
                    riskNotes = "Ep4 st1 Learning pick letter; uses unified pickLetterSpec.",
                )
            BALLOON_POP ->
                popBalloonsSpec(chapterId = 4, stationId = stationId, plan = plan)
                    .copy(riskNotes = "Ep4 st2 balloons; Learning mode.")
            REVEAL_THEN_CHOOSE ->
                findLetterGridSpec(chapterId = 4, stationId = stationId, plan = plan)
                    .copy(riskNotes = "Ep4 st3 grid; Learning mode.")
            PICTURE_PICK_ONE ->
                pictureStartsWithSpec(chapterId = 4, stationId = stationId, plan = plan)
                    .copy(riskNotes = "Ep4 st4 picture; Learning mode.")
            PICTURE_PICK_ALL ->
                StationUiSpec(
                    chapterId = 4,
                    stationId = stationId,
                    templateId = StationTemplateId.ImageMatch,
                    variants = variantsFor(listenOnly),
                    quizMode = plan.mode,
                    findGridMaxTargetCount = plan.findLetterGridMaxTargetCount,
                    helpControlsEnabled = listenOnly,
                    hintDurationMs = if (listenOnly) 2100L else null,
                    replayMode = if (listenOnly) StationReplayMode.TargetLetterOnly else StationReplayMode.None,
                    hintMode = if (listenOnly) StationHintMode.TemporaryTargetLetter else StationHintMode.None,
                    imageMatchHeaderInstructionOverride =
                        StationInstructionCopy.ImageMatchFindWordStartingWithLetter,
                    imageMatchHeaderReadablePanel = true,
                    imageMatchShowTargetLetterChip = !listenOnly,
                    riskNotes = "Image match st5 Learning mode.",
                )
            FINALE_PICTURE_LETTER_MATCH ->
                matchLetterToWordSpec(
                    chapterId = 4,
                    stationId = stationId,
                    plan = plan,
                    extraVariants = arrayOf(StationVariant.Finale),
                ).copy(riskNotes = "Finale match UI; unified MatchLetterToWord template (Ch4).")
            else -> error("Unexpected Ep4 stationId=$stationId")
        }
    }

    private fun chapter6UiSpec(stationId: Int, plan: StationQuizPlan): StationUiSpec {
        return when (stationId) {
            1 ->
                pictureStartsWithSpec(chapterId = 6, stationId = stationId, plan = plan)
                    .copy(
                        riskNotes = "Ch6 st1 picture; aligned with Chapter 3 station 1.",
                    )
            2 ->
                matchLetterToWordSpec(
                    chapterId = 6,
                    stationId = stationId,
                    plan = plan,
                ).copy(
                    contentTopInsetDp = 56f,
                    riskNotes = "Ch6 st2 match letter + word; aligned with Chapter 3 station 2.",
                )
            3 ->
                popBalloonsSpec(chapterId = 6, stationId = stationId, plan = plan, isPopAllLetters = false)
                    .copy(
                        riskNotes = "Ch6 st3 pop-all; aligned with Chapter 3 station 3.",
                    )
            4 ->
                pickLetterSpec(
                    chapterId = 6,
                    stationId = stationId,
                    plan = plan,
                    extraVariants = arrayOf(StationVariant.HighlightedLetterInWord, StationVariant.Episode4Help),
                ).copy(
                    variants = variantsFor(listenOnly = false, StationVariant.HighlightedLetterInWord, StationVariant.Episode4Help),
                    helpControlsEnabled = true,
                    replayMode = StationReplayMode.ExistingStationSpecific,
                    hintMode = StationHintMode.None,
                    hintDurationMs = null,
                    pickLetterInstructionOverride = null,
                    pickLetterHighlightedInWordInstruction = StationInstructionCopy.PickLetterHighlightedInWord,
                    contentTopInsetDp = 56f,
                    riskNotes = "Ch6 st4 highlighted letter in word; aligned with Chapter 3 station 4.",
                )
            5 ->
                pickLetterSpec(
                    chapterId = 6,
                    stationId = stationId,
                    plan = plan,
                    extraVariants = arrayOf(StationVariant.Chapter3AudioLetterRecognition, StationVariant.Episode4Help),
                ).copy(
                    contentTopInsetDp = 56f,
                    riskNotes = "Ch6 st5 audio recognition; aligned with Chapter 3 station 5.",
                )
            6 ->
                StationUiSpec(
                    chapterId = 6,
                    stationId = stationId,
                    templateId = StationTemplateId.ImageToWord,
                    variants = variantsFor(listenOnly = false, StationVariant.Chapter3ImageToWord),
                    quizMode = plan.mode,
                    findGridMaxTargetCount = plan.findLetterGridMaxTargetCount,
                    helpControlsEnabled = false,
                    replayMode = StationReplayMode.ExistingStationSpecific,
                    hintMode = StationHintMode.ExistingStationSpecific,
                    imageToWordInstructionText = StationInstructionCopy.Chapter3ImageToWord,
                    contentTopInsetDp = 56f,
                    riskNotes = "Ch6 st6 uses ImageToWordGame; aligned with Chapter 3 station 6.",
                )
            else -> error("Unexpected Ch6 stationId=$stationId")
        }
    }

    private fun chapter6TrainingCandidateUiSpec(stationId: Int, plan: StationQuizPlan): StationUiSpec {
        val listenOnly = plan.listenOnlyTargetPrompt
        return when (stationId) {
            1 ->
                pickLetterSpec(
                    chapterId = 6,
                    stationId = stationId,
                    plan = plan,
                    extraVariants = arrayOf(StationVariant.Episode4Help),
                ).copy(
                    riskNotes = "Training candidate: listen-first pick letter (from prior Ch6 st1).",
                )
            2 ->
                pickLetterSpec(
                    chapterId = 6,
                    stationId = stationId,
                    plan = plan,
                    extraVariants = arrayOf(StationVariant.HighlightedLetterInWord, StationVariant.Episode4Help),
                ).copy(
                    variants = variantsFor(listenOnly = false, StationVariant.HighlightedLetterInWord, StationVariant.Episode4Help),
                    helpControlsEnabled = true,
                    replayMode = StationReplayMode.ExistingStationSpecific,
                    hintMode = StationHintMode.None,
                    hintDurationMs = null,
                    pickLetterInstructionOverride = null,
                    pickLetterHighlightedInWordInstruction = StationInstructionCopy.PickLetterHighlightedInWord,
                    contentTopInsetDp = 56f,
                    riskNotes = "Training candidate: highlighted letter in word (from prior Ch6 st2).",
                )
            3 ->
                popBalloonsSpec(chapterId = 6, stationId = stationId, plan = plan, isPopAllLetters = true)
                    .copy(
                        contentTopInsetDp = 56f,
                        riskNotes = "Training candidate: pop-all-letters-in-word (from prior Ch6 st3).",
                    )
            4 ->
                pictureStartsWithSpec(chapterId = 6, stationId = stationId, plan = plan)
                    .copy(
                        contentTopInsetDp = 76f,
                        riskNotes = "Training candidate: harder picture-starts-with variant (from prior Ch6 st4).",
                    )
            5 ->
                StationUiSpec(
                    chapterId = 6,
                    stationId = stationId,
                    templateId = StationTemplateId.ImageMatch,
                    variants = variantsFor(listenOnly, StationVariant.Episode4Help),
                    quizMode = plan.mode,
                    findGridMaxTargetCount = plan.findLetterGridMaxTargetCount,
                    helpControlsEnabled = true,
                    replayMode = StationReplayMode.TargetLetterOnly,
                    hintMode = StationHintMode.TemporaryTargetLetter,
                    hintDurationMs = 2100L,
                    imageMatchShowTargetLetterChip = false,
                    imageMatchHeaderInstructionOverride = StationInstructionCopy.ImageMatchFindWordStartingWithLetter,
                    imageMatchHeaderReadablePanel = true,
                    contentTopInsetDp = 86f,
                    riskNotes = "Training candidate: listen-first image match (from prior Ch6 st5).",
                )
            6 ->
                matchLetterToWordSpec(
                    chapterId = 6,
                    stationId = stationId,
                    plan = plan,
                    extraVariants = arrayOf(StationVariant.Finale),
                ).copy(
                    contentTopInsetDp = 56f,
                    riskNotes = "Training candidate: advanced finale match (from prior Ch6 st6).",
                )
            else -> error("Unexpected Ch6 stationId=$stationId")
        }
    }
}
