package com.tal.hebrewdino.ui.domain

/**
 * How a Season 2 word-parts station presents the target word to the child.
 * Wired via [StationQuizPlan.season2WordPartsPresentationMode].
 */
enum class Season2WordPartsPresentationMode {
    /** Ch2 finale: full word + split always visible (tutorial). */
    VisibleWordParts,
    /** Ch3 st5: guided bridge — full word visible, supportive layout. */
    GuidedWordParts,
    /** Ch3 st6: challenge — word hidden until hint or success. */
    HiddenWordPartsChallenge,
}
