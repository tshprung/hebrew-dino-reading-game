package com.tal.hebrewdino.ui.domain

/**
 * StationVariant describes *mode/policy* differences for a station template.
 *
 * Variants are **metadata** for architecture clarity and low-risk UI branching. They must not
 * encode generator semantics; use [StationQuizPlan] for learning/generation behavior.
 */
enum class StationVariant {
    Standard,

    ListenFirst,

    HelpColumn,

    HighlightedLetterInWord,
    Chapter3AudioLetterRecognition,
    PopAllLettersInWord,
    Chapter3ImageToWord,

    Finale,
}

