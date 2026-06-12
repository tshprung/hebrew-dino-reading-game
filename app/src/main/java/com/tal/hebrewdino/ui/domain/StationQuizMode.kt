package com.tal.hebrewdino.ui.domain

/**
 * One journey station runs exactly one quiz interaction pattern (possibly 2–3 rounds of it).
 * Never mix patterns within the same station session.
 */
enum class StationQuizMode {
    FindLetterGrid,
    PickLetter,
    PopBalloons,
    /** Picture + word; pick the first letter (chapter 1 station 4). */
    PictureStartsWith,
    ImageMatch,
    FinaleSlot,
    /** Drag each written word card onto its matching picture (not player-facing yet). */
    DragWordToPicture,
    /** Drag the missing letter into a partial word slot (not player-facing yet). */
    DragMissingLetter,
}
