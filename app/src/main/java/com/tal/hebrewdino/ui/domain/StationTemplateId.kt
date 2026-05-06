package com.tal.hebrewdino.ui.domain

/**
 * StationTemplateId describes the reusable *UI template* used to render a station.
 *
 * - **Template**: the reusable station "shape" (screen layout + interaction model).
 * - **Variant**: modifiers that change policies/presentation while keeping the same template.
 * - **Config**: concrete text/pools/visibility/panels already represented in [StationUiSpec] / [StationQuizPlan].
 *
 * Generator/pedagogical behavior stays in [StationQuizPlan] and [LevelSession].
 */
enum class StationTemplateId {
    PickLetter,
    PopBalloons,
    FindLetterGrid,
    PictureStartsWith,
    ImageMatch,
    MatchLetterToWord,
    ImageToWord,
    FinaleSlot,
    Special,
}

