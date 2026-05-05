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

    fun getStationUiSpec(chapterId: Int, stationId: Int): StationUiSpec {
        val sid = stationId.coerceIn(1, Chapter1Config.STATION_COUNT)
        val plan =
            when (chapterId) {
                1 -> StationQuizPlans.chapter1(sid)
                2 -> StationQuizPlans.chapter2(sid)
                3 -> StationQuizPlans.chapter3(sid)
                4 -> StationQuizPlans.chapter4(sid)
                5 -> StationQuizPlans.chapter5(sid)
                else -> error("Unsupported chapterId=$chapterId")
            }
        return when (chapterId) {
            1 -> sixStationUiSpec(chapterId = 1, sid, plan)
            2 -> sixStationUiSpec(chapterId = 2, sid, plan)
            3 -> chapter3UiSpec(sid, plan)
            4 -> episode4UiSpec(sid, plan)
            5 -> sixStationUiSpec(chapterId = 5, sid, plan)
            else -> error("Unsupported chapterId=$chapterId")
        }
    }

    /** Chapters 1, 2, 5 share [Chapter1StationOrder]; Ch5 is listen-only saga. */
    private fun sixStationUiSpec(chapterId: Int, stationId: Int, plan: StationQuizPlan): StationUiSpec {
        val listenOnly = plan.listenOnlyTargetPrompt
        return when (stationId) {
            TAP_LETTER ->
                StationUiSpec(
                    chapterId = chapterId,
                    stationId = stationId,
                    quizMode = plan.mode,
                    findGridMaxTargetCount = plan.findLetterGridMaxTargetCount,
                    helpControlsEnabled = chapterId == 5,
                    replayMode =
                        if (chapterId == 5) {
                            StationReplayMode.TargetLetterOnly
                        } else {
                            StationReplayMode.ExistingStationSpecific
                        },
                    hintMode =
                        if (chapterId == 5) {
                            StationHintMode.TemporaryTargetLetter
                        } else {
                            StationHintMode.ExistingStationSpecific
                        },
                    hintDurationMs = if (chapterId == 5) 2100L else null,
                    pickLetterInstructionOverride = if (chapterId == 5) "בחר את האות:" else null,
                    pickLetterListenOnlyHebrewPanel = listenOnly && chapterId != 4 && chapterId != 5,
                    pickLetterAllowPinnedCorrectShortcut = !listenOnly,
                    riskNotes = "Saga pick letter; Ch5 station 1 uses Ep4-style help + 'בחר את האות' copy.",
                )
            BALLOON_POP ->
                StationUiSpec(
                    chapterId = chapterId,
                    stationId = stationId,
                    quizMode = plan.mode,
                    findGridMaxTargetCount = plan.findLetterGridMaxTargetCount,
                    helpControlsEnabled = chapterId == 5,
                    replayMode =
                        if (chapterId == 5) {
                            StationReplayMode.TargetLetterOnly
                        } else {
                            StationReplayMode.ExistingStationSpecific
                        },
                    hintMode =
                        if (chapterId == 5) {
                            StationHintMode.TemporaryTargetLetter
                        } else {
                            StationHintMode.ExistingStationSpecific
                        },
                    hintDurationMs = if (chapterId == 5) 2100L else null,
                    balloonInstructionOverride =
                        if (listenOnly) {
                            "פוצץ את הבלונים של האות שנשמעה:"
                        } else {
                            "פוצץ את הבלונים עם האות:"
                        },
                    riskNotes = "Saga balloon header text; Ch5 listen-only uses שמע string (same as prior listenOnly branch).",
                )
            REVEAL_THEN_CHOOSE ->
                StationUiSpec(
                    chapterId = chapterId,
                    stationId = stationId,
                    quizMode = plan.mode,
                    findGridMaxTargetCount = plan.findLetterGridMaxTargetCount,
                    helpControlsEnabled = chapterId == 5,
                    replayMode =
                        if (chapterId == 5) {
                            StationReplayMode.TargetLetterOnly
                        } else {
                            StationReplayMode.ExistingStationSpecific
                        },
                    hintMode =
                        if (chapterId == 5) {
                            StationHintMode.TemporaryTargetLetter
                        } else {
                            StationHintMode.ExistingStationSpecific
                        },
                    hintDurationMs = if (chapterId == 5) 2100L else null,
                    findGridSuppressHeaderTargetLetter = chapterId == 3 || listenOnly,
                    findGridHideListenOnlyHeaderTargetLetter = chapterId == 5 && listenOnly,
                    findGridInlineReadablePanel = chapterId == 5 && listenOnly,
                    riskNotes = "Find grid; inline text/panel via listenOnly vs Ep4 override.",
                )
            PICTURE_PICK_ONE ->
                StationUiSpec(
                    chapterId = chapterId,
                    stationId = stationId,
                    quizMode = plan.mode,
                    findGridMaxTargetCount = plan.findLetterGridMaxTargetCount,
                    helpControlsEnabled = chapterId == 5,
                    replayMode =
                        if (chapterId == 5) {
                            StationReplayMode.TargetWordOnly
                        } else {
                            StationReplayMode.ExistingStationSpecific
                        },
                    hintMode =
                        if (chapterId == 5) {
                            StationHintMode.TemporaryStartingLetter
                        } else {
                            StationHintMode.ExistingStationSpecific
                        },
                    hintDurationMs = if (chapterId == 5) 2100L else null,
                    pictureStartsWithReadablePanel = false,
                    hidePictureWordCaptionWhenListenOnlySaga = listenOnly,
                    riskNotes = "Picture first letter; Ep4/Ch5 caption visibility differs via hidePicture…",
                )
            PICTURE_PICK_ALL ->
                StationUiSpec(
                    chapterId = chapterId,
                    stationId = stationId,
                    quizMode = plan.mode,
                    findGridMaxTargetCount = plan.findLetterGridMaxTargetCount,
                    helpControlsEnabled = chapterId == 5,
                    replayMode =
                        if (chapterId == 5) {
                            StationReplayMode.TargetLetterOnly
                        } else {
                            StationReplayMode.ExistingStationSpecific
                        },
                    hintMode =
                        if (chapterId == 5) {
                            StationHintMode.TemporaryTargetLetter
                        } else {
                            StationHintMode.ExistingStationSpecific
                        },
                    hintDurationMs = if (chapterId == 5) 2100L else null,
                    imageMatchShowTargetLetterChip = !listenOnly,
                    imageMatchHeaderInstructionOverride =
                        if (chapterId == 5) {
                            "מצא את המילה המתחילה באות:"
                        } else {
                            null
                        },
                    imageMatchHeaderReadablePanel = chapterId == 5,
                    riskNotes = "Image match three cards; listen-only hides target letter chip (Ch5).",
                )
            FINALE_PICTURE_LETTER_MATCH ->
                StationUiSpec(
                    chapterId = chapterId,
                    stationId = stationId,
                    quizMode = plan.mode,
                    findGridMaxTargetCount = plan.findLetterGridMaxTargetCount,
                    helpControlsEnabled = false,
                    replayMode = StationReplayMode.ExistingStationSpecific,
                    hintMode = StationHintMode.ExistingStationSpecific,
                    matchLetterInstructionReadablePanel = chapterId == 5,
                    riskNotes = "Finale match UI; Ch4 Ep6 uses readable panel (see episode4UiSpec).",
                )
            else -> error("Unexpected stationId=$stationId")
        }
    }

    private fun chapter3UiSpec(stationId: Int, plan: StationQuizPlan): StationUiSpec {
        return when (stationId) {
            1 ->
                StationUiSpec(
                    chapterId = 3,
                    stationId = stationId,
                    quizMode = plan.mode,
                    findGridMaxTargetCount = plan.findLetterGridMaxTargetCount,
                    helpControlsEnabled = false,
                    replayMode = StationReplayMode.ExistingStationSpecific,
                    hintMode = StationHintMode.ExistingStationSpecific,
                    pictureStartsWithReadablePanel = true,
                    riskNotes = "Ch3 st1 picture; [PictureStartsWithGame] also forces panel when chapterId==3.",
                )
            2 ->
                StationUiSpec(
                    chapterId = 3,
                    stationId = stationId,
                    quizMode = plan.mode,
                    findGridMaxTargetCount = plan.findLetterGridMaxTargetCount,
                    helpControlsEnabled = false,
                    replayMode = StationReplayMode.ExistingStationSpecific,
                    hintMode = StationHintMode.ExistingStationSpecific,
                    matchLetterInstructionReadablePanel = true,
                    imageMatchShowTargetLetterChip = true,
                    riskNotes = "Ch3 st2 match letter + word.",
                )
            3 ->
                StationUiSpec(
                    chapterId = 3,
                    stationId = stationId,
                    quizMode = plan.mode,
                    findGridMaxTargetCount = plan.findLetterGridMaxTargetCount,
                    helpControlsEnabled = false,
                    replayMode = StationReplayMode.ExistingStationSpecific,
                    hintMode = StationHintMode.ExistingStationSpecific,
                    riskNotes = "Ch3 st3 pop-all balloons — header copy in GameScreen (spell word).",
                )
            4 ->
                StationUiSpec(
                    chapterId = 3,
                    stationId = stationId,
                    quizMode = plan.mode,
                    findGridMaxTargetCount = plan.findLetterGridMaxTargetCount,
                    helpControlsEnabled = false,
                    replayMode = StationReplayMode.ExistingStationSpecific,
                    hintMode = StationHintMode.ExistingStationSpecific,
                    pickLetterAllowPinnedCorrectShortcut = true,
                    riskNotes = "Ch3 st4 highlighted letter in word.",
                )
            5 ->
                StationUiSpec(
                    chapterId = 3,
                    stationId = stationId,
                    quizMode = plan.mode,
                    findGridMaxTargetCount = plan.findLetterGridMaxTargetCount,
                    helpControlsEnabled = false,
                    replayMode = StationReplayMode.ExistingStationSpecific,
                    hintMode = StationHintMode.ExistingStationSpecific,
                    pickLetterListenOnlyHebrewPanel = true,
                    riskNotes = "Ch3 st5 audio letter recognition — shared listen-first panel pattern.",
                )
            6 ->
                StationUiSpec(
                    chapterId = 3,
                    stationId = stationId,
                    quizMode = plan.mode,
                    findGridMaxTargetCount = plan.findLetterGridMaxTargetCount,
                    helpControlsEnabled = false,
                    replayMode = StationReplayMode.ExistingStationSpecific,
                    hintMode = StationHintMode.ExistingStationSpecific,
                    riskNotes = "Ch3 st6 uses ImageToWordGame (instruction hardcoded there, not spec).",
                )
            else -> error("Unexpected Ch3 stationId=$stationId")
        }
    }

    private fun episode4UiSpec(stationId: Int, plan: StationQuizPlan): StationUiSpec {
        return when (stationId) {
            TAP_LETTER ->
                StationUiSpec(
                    chapterId = 4,
                    stationId = stationId,
                    quizMode = plan.mode,
                    findGridMaxTargetCount = plan.findLetterGridMaxTargetCount,
                    helpControlsEnabled = true,
                    hintDurationMs = 2100L,
                    replayMode = StationReplayMode.TargetLetterOnly,
                    hintMode = StationHintMode.TemporaryTargetLetter,
                    pickLetterInstructionOverride = "בחר את האות:",
                    pickLetterListenOnlyHebrewPanel = false,
                    pickLetterAllowPinnedCorrectShortcut = false,
                    riskNotes = "Listen-first pick letter; Ep4 help column.",
                )
            BALLOON_POP ->
                StationUiSpec(
                    chapterId = 4,
                    stationId = stationId,
                    quizMode = plan.mode,
                    findGridMaxTargetCount = plan.findLetterGridMaxTargetCount,
                    helpControlsEnabled = true,
                    hintDurationMs = 2100L,
                    replayMode = StationReplayMode.TargetLetterOnly,
                    hintMode = StationHintMode.TemporaryTargetLetter,
                    balloonInstructionOverride = "פוצץ את הבלונים עם האות:",
                    useEpisode4BalloonInstructionPanel = true,
                    balloonPlayAreaStartInsetDp = 96f,
                    excludeFullScreenBalloonHintOverlay = true,
                    riskNotes = "Inline hint under instruction; no full-screen balloon overlay.",
                )
            REVEAL_THEN_CHOOSE ->
                StationUiSpec(
                    chapterId = 4,
                    stationId = stationId,
                    quizMode = plan.mode,
                    findGridMaxTargetCount = plan.findLetterGridMaxTargetCount,
                    helpControlsEnabled = true,
                    hintDurationMs = 2100L,
                    replayMode = StationReplayMode.TargetLetterOnly,
                    hintMode = StationHintMode.TemporaryTargetLetter,
                    findGridInlineInstructionOverride = "מצא את האות:",
                    findGridInlineReadablePanel = true,
                    findGridHideListenOnlyHeaderTargetLetter = true,
                    findGridSuppressHeaderTargetLetter = true,
                    riskNotes = "Grid max targets capped via plan (4).",
                )
            PICTURE_PICK_ONE ->
                StationUiSpec(
                    chapterId = 4,
                    stationId = stationId,
                    quizMode = plan.mode,
                    findGridMaxTargetCount = plan.findLetterGridMaxTargetCount,
                    helpControlsEnabled = true,
                    hintDurationMs = 2100L,
                    replayMode = StationReplayMode.TargetWordOnly,
                    hintMode = StationHintMode.TemporaryStartingLetter,
                    pictureStartsWithInstructionOverride = "באיזו אות מתחילה המילה:",
                    pictureStartsWithReadablePanel = true,
                    hidePictureWordCaptionWhenListenOnlySaga = true,
                    riskNotes = "Listen-first picture station; caption hidden when listen-only saga.",
                )
            PICTURE_PICK_ALL ->
                StationUiSpec(
                    chapterId = 4,
                    stationId = stationId,
                    quizMode = plan.mode,
                    findGridMaxTargetCount = plan.findLetterGridMaxTargetCount,
                    helpControlsEnabled = true,
                    hintDurationMs = 2100L,
                    replayMode = StationReplayMode.TargetLetterOnly,
                    hintMode = StationHintMode.TemporaryTargetLetter,
                    imageMatchHeaderInstructionOverride = "מצא את המילה המתחילה באות:",
                    imageMatchHeaderReadablePanel = true,
                    imageMatchShowTargetLetterChip = false,
                    riskNotes = "Image match st5 arc.",
                )
            FINALE_PICTURE_LETTER_MATCH ->
                StationUiSpec(
                    chapterId = 4,
                    stationId = stationId,
                    quizMode = plan.mode,
                    findGridMaxTargetCount = plan.findLetterGridMaxTargetCount,
                    helpControlsEnabled = false,
                    replayMode = StationReplayMode.None,
                    hintMode = StationHintMode.ExistingStationSpecific,
                    hintDurationMs = null,
                    matchLetterInstructionReadablePanel = true,
                    riskNotes = "No right-side help column; letter not shown in header (match task).",
                )
            else -> error("Unexpected Ep4 stationId=$stationId")
        }
    }
}
