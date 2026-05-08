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
                else -> error("Unsupported chapterId=$chapterId")
            }
        return when (chapterId) {
            1 -> sixStationUiSpec(chapterId = 1, sid, plan)
            2 -> sixStationUiSpec(chapterId = 2, sid, plan)
            3 -> chapter3UiSpec(sid, plan)
            4 -> episode4UiSpec(sid, plan)
            5 -> sixStationUiSpec(chapterId = 5, sid, plan)
            6 -> chapter6UiSpec(stationId.coerceIn(1, Chapter6Config.STATION_COUNT), plan)
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
            findGridHideListenOnlyHeaderTargetLetter = listenOnly
        )
    }

    private fun pictureStartsWithSpec(
        chapterId: Int,
        stationId: Int,
        plan: StationQuizPlan
    ): StationUiSpec {
        val listenOnly = plan.listenOnlyTargetPrompt
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
            pictureStartsWithReadablePanel = true,
            pictureStartsWithInstructionPanelStyle = InstructionPanelStyle.WhiteRounded,
            hidePictureWordCaptionWhenListenOnlySaga = listenOnly
        )
    }

    private fun popBalloonsSpec(
        chapterId: Int,
        stationId: Int,
        plan: StationQuizPlan,
        isPopAllLetters: Boolean = false
    ): StationUiSpec {
        val listenOnly = plan.listenOnlyTargetPrompt
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
                ).copy(riskNotes = "Ch3 st2 match letter + word; unified MatchLetterToWord template.")
            3 -> popBalloonsSpec(chapterId = 3, stationId = stationId, plan = plan, isPopAllLetters = true)
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
                    riskNotes = "Ch3 st4 highlighted letter in word.",
                )
            5 ->
                pickLetterSpec(
                    chapterId = 3,
                    stationId = stationId,
                    plan = plan,
                    extraVariants = arrayOf(StationVariant.Chapter3AudioLetterRecognition, StationVariant.Episode4Help)
                ).copy(
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
                    riskNotes = "Ch3 st6 uses ImageToWordGame (instruction from spec).",
                )
            else -> error("Unexpected Ch3 stationId=$stationId")
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
        val listenOnly = plan.listenOnlyTargetPrompt
        return when (stationId) {
            1 ->
                pickLetterSpec(
                    chapterId = 6,
                    stationId = stationId,
                    plan = plan,
                    extraVariants = arrayOf(StationVariant.Episode4Help)
                ).copy(
                    riskNotes = "Ch6 st1 listen-first pick letter; uses unified pickLetterSpec.",
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
                    riskNotes = "Ch6 st2 highlighted letter in word (reuses Ch3 st4 behavior).",
                )
            3 ->
                popBalloonsSpec(chapterId = 6, stationId = stationId, plan = plan, isPopAllLetters = true)
                    .copy(
                        contentTopInsetDp = 56f,
                        riskNotes = "Ch6 st3 pop-all; uses unified popBalloonsSpec.",
                    )
            4 ->
                pictureStartsWithSpec(chapterId = 6, stationId = stationId, plan = plan)
                    .copy(
                        contentTopInsetDp = 56f,
                        riskNotes = "Ch6 st4 picture; uses unified pictureStartsWithSpec.",
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
                    riskNotes = "Ch6 st5 image match (review).",
                )
            6 ->
                matchLetterToWordSpec(
                    chapterId = 6,
                    stationId = stationId,
                    plan = plan,
                    extraVariants = arrayOf(StationVariant.Finale),
                ).copy(
                    contentTopInsetDp = 56f,
                    // Keep existing narrative note; UI is now unified.
                    riskNotes = "Ch6 st6 finale match (homecoming narrative via story screens).",
                )
            else -> error("Unexpected Ch6 stationId=$stationId")
        }
    }
}
