package com.tal.hebrewdino.ui.domain

/**
 * Locked interaction order for Chapter 1 (one mechanic per station; several rounds allowed).
 * Implementation wiring: [StationQuizPlans.chapter1].
 */
object Chapter1StationOrder {
    const val TAP_LETTER: Int = 1
    const val BALLOON_POP: Int = 2
    const val REVEAL_THEN_CHOOSE: Int = 3
    const val PICTURE_PICK_ONE: Int = 4
    const val PICTURE_PICK_ALL: Int = 5
    const val FINALE_PICTURE_LETTER_MATCH: Int = 6
}
