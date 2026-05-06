package com.tal.hebrewdino.ui.domain

/**
 * Declarative UI-oriented station behavior for saga gameplay ([com.tal.hebrewdino.ui.screens.GameScreen]).
 *
 * **Audio timing** / SoundPool overlap / VoicePlayer sequencing live in GameScreen — not here.
 *
 * **Wiring policy:** Every property is either used when rendering GameScreen low-risk UI, or tagged
 * `[DOCS]` in its comment (mirrors plan/product notes only; do not read for UI).
 */
enum class StationReplayMode {
    /** No Episode 4-style minimal replay / help column not applicable. */
    None,
    /** Help replay plays target letter name only (Ep4 st1–3, st5). */
    TargetLetterOnly,
    /** Help replay plays target word audio only (Ep4 st4). */
    TargetWordOnly,
    /** Keep existing chapter-specific replay paths (non-Ep4 saga / fallbacks). */
    ExistingStationSpecific,
}

enum class StationHintMode {
    None,
    TemporaryTargetLetter,
    TemporaryStartingLetter,
    ExistingStationSpecific,
}

/** White readability panel behind a header instruction line (non-balloon stations). */
enum class InstructionPanelStyle {
    None,
    WhiteRounded,
}

/**
 * @param findGridMaxTargetCount mirrors [StationQuizPlan.findLetterGridMaxTargetCount] when set.
 * @param balloonPlayAreaStartInsetDp layout start inset for balloon area (RTL: toward help column).
 */
data class StationUiSpec(
    /** WIRED: identity passed through GameScreen / registry tests. */
    val chapterId: Int,
    /** WIRED: identity passed through GameScreen / registry tests. */
    val stationId: Int,
    /** Canonical UI template for this station (architecture metadata). */
    val templateId: StationTemplateId,
    /** Template variants (architecture metadata). Always includes at least [StationVariant.Standard]. */
    val variants: Set<StationVariant> = setOf(StationVariant.Standard),
    /** [DOCS] Mirror of plan mode; tests/registry alignment only. */
    val quizMode: StationQuizMode,
    /** WIRED: Episode 4 right-side replay/hint column eligibility (also gated by chapterId==4 in GameScreen). */
    val helpControlsEnabled: Boolean = false,
    /** WIRED: hint lock duration when help controls apply (Ep4 st1–5). */
    val hintDurationMs: Long? = null,
    /** WIRED: Registry + [StationBehaviorRegistryTest]; Episode 4 stations 1–5 replay semantics (help replay uses legacy paths in GameScreen). */
    val replayMode: StationReplayMode = StationReplayMode.None,
    /** [DOCS] Hint interaction style; Ep4 uses Temporary* values for product clarity. */
    val hintMode: StationHintMode = StationHintMode.ExistingStationSpecific,
    /** [DOCS] Mirrors plan; generation uses [StationQuizPlan.findLetterGridMaxTargetCount]. */
    val findGridMaxTargetCount: Int? = null,
    /**
     * WIRED: When non-null, pick-letter header uses this text + large white panel (Ep4 st1).
     * [DOCS]: absent on chapters that use default saga header instead.
     */
    val pickLetterInstructionOverride: String? = null,
    /**
     * WIRED: Chapter 1/2 saga station 1 — smaller preamble above target chip (no large white panel).
     * Null when [pickLetterInstructionOverride] or listen-only panel handles the header.
     */
    val pickLetterSagaStation1CompactPreamble: String? = null,
    /** WIRED: Chapter 3 station 4 — banner above highlighted spell row. */
    val pickLetterHighlightedInWordInstruction: String? = null,
    /** WIRED: Shown in listen-only Hebrew panel (Chapter 3 station 5 + any station with [pickLetterListenOnlyHebrewPanel]). */
    val pickLetterListenOnlyInstructionText: String? = null,
    /** WIRED: Label for the replay-letter button in the listen-only panel. */
    val pickLetterRepeatLetterButtonLabel: String? = null,
    /**
     * WIRED: “מצא את האות שנאמרת” + replay letter UI for listen-only saga pick-letter (Ch5 st1)
     * and same pattern for Ch3 st5 audio recognition (combined with plan flags in GameScreen).
     */
    val pickLetterListenOnlyHebrewPanel: Boolean = false,
    /**
     * WIRED: Allow shrinking LetterOptions to the pinned correct letter after a correct tap (saga station 1).
     * False when listen-only station 1 should keep full option rows (Ep4/Ch5 st1).
     */
    val pickLetterAllowPinnedCorrectShortcut: Boolean = true,
    /** WIRED: Ep4 balloon station: white instruction panel + hint under instruction. */
    val useEpisode4BalloonInstructionPanel: Boolean = false,
    /** WIRED: Balloon instruction line when using inline/header balloon copy (Ep4 + saga defaults). */
    val balloonInstructionOverride: String? = null,
    /**
     * WIRED: When true, [PopBalloonsInstructionHeaderBlock] renders nothing (e.g. Chapter 3 pop-all-letters:
     * banner is shown separately).
     */
    val popBalloonsSkipInstructionHeaderBlock: Boolean = false,
    /**
     * WIRED: Saga station 2 — show the instruction line above the letter chip when not using Ep4 panel
     * or pinned-balloon staging.
     */
    val popBalloonsShowSagaStation2InstructionLine: Boolean = false,
    /** WIRED: Instruction line on the Chapter 3 pop-all-letters word banner. */
    val popBalloonsPopAllLettersBannerInstruction: String? = null,
    /** WIRED: RTL start inset for balloon play area (Ep4 st2). */
    val balloonPlayAreaStartInsetDp: Float = 0f,
    /** WIRED: Suppress full-screen center hint overlay for balloons (Ep4 st2 uses inline hint). */
    val excludeFullScreenBalloonHintOverlay: Boolean = false,
    /** WIRED: Find-grid inline instruction when in saga station 3 grid mode (Ep4 explicit; others use listenOnly fallback). */
    val findGridInlineInstructionOverride: String? = null,
    /** WIRED: Hide written target letter in find-grid header (Ep4 listen-first station 3). */
    val findGridHideListenOnlyHeaderTargetLetter: Boolean = false,
    /**
     * WIRED: [FindLetterGridGame] suppressHeaderTargetLetter — hide header letter chip when listen-only
     * or legacy Ch3-or-listenOnly rule (Ch3 has no find-grid today; value keeps parity with old branch).
     */
    val findGridSuppressHeaderTargetLetter: Boolean = false,
    /** WIRED: Picture station instruction override (Ep4 st4); null uses GameScreen listen-only / default strings. */
    val pictureStartsWithInstructionOverride: String? = null,
    /**
     * WIRED: When non-null and GameScreen applies listen-first saga station 4 rule, this instruction
     * replaces the default listen-only saga string.
     */
    val pictureStartsWithListenOnlySagaInstruction: String? = null,
    /** WIRED: Readable panel for picture instruction ([PictureStartsWithGame] also treats Ch3 specially). */
    val pictureStartsWithReadablePanel: Boolean = false,
    /** WIRED: Panel style for the picture instruction line (replaces chapterId==3 special-casing). */
    val pictureStartsWithInstructionPanelStyle: InstructionPanelStyle = InstructionPanelStyle.None,
    /**
     * WIRED: When true, hide picture word caption for listen-only saga station 4 (Ep4 + Ch5).
     * Used as: showCaption = !(listenOnly && saga && st4 && this).
     */
    val hidePictureWordCaptionWhenListenOnlySaga: Boolean = false,
    /** WIRED: Image-match header instruction override (Ep4 st5); null uses GameScreen fallbacks. */
    val imageMatchHeaderInstructionOverride: String? = null,
    /** WIRED: White readable header panel for image-match (Ep4 st5). */
    val imageMatchHeaderReadablePanel: Boolean = false,
    /** WIRED: Show target-letter chip in image-match header row (false for listen-only saga station 5). */
    val imageMatchShowTargetLetterChip: Boolean = true,
    /** WIRED: [MatchLetterToWordGame] instructionReadablePanelOverride / chapter 3 station 2 panel. */
    val matchLetterInstructionReadablePanel: Boolean = false,
    /** WIRED: Persistent instructions for match-letter / finale UI; null falls back to game default copy. */
    val matchLetterInstructionText: String? = null,
    /** WIRED: Chapter 3 station 6 image→word header. */
    val imageToWordInstructionText: String? = null,
    /** WIRED: Inline find-grid instruction panel style (replaces chapterId==3 branch in [FindLetterGridGame]). */
    val findGridInlineInstructionPanelStyle: InstructionPanelStyle = InstructionPanelStyle.None,
    /**
     * WIRED: Optional extra top inset for station content to avoid crowding the progress bar on stations
     * that show a large instruction row / banner (spec-driven; default keeps existing layout).
     */
    val contentTopInsetDp: Float? = null,
    /** [DOCS] Human-readable registry notes (not read by UI). */
    val riskNotes: String = "",
)

fun StationUiSpec.hasVariant(v: StationVariant): Boolean = variants.contains(v)
