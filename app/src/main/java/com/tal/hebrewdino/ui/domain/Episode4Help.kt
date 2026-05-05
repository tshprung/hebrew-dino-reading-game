package com.tal.hebrewdino.ui.domain

/**
 * Episode 4 stations 1–5 help column / hint replay — pure helpers only (no audio, no timing).
 *
 * GameScreen gates UI with [chapterId] and [StationUiSpec.helpControlsEnabled]; registry enables
 * help for Episode 4 stations 1–5 and Chapter 5 stations 1–5 (finale off).
 */
object Episode4Help {
    /** Fallback when [StationUiSpec.hintDurationMs] is null (matches legacy GameScreen constant). */
    const val HINT_REVEAL_FALLBACK_MS = 3000L

    /**
     * True when the Episode 4-style right-side help column should be shown (chapter + spec).
     * Chapters 4–5 use the same help column when [StationUiSpec.helpControlsEnabled] is on.
     */
    fun isHelpColumnActive(chapterId: Int, stationUiSpec: StationUiSpec): Boolean =
        (chapterId == 4 || chapterId == 5) && stationUiSpec.helpControlsEnabled

    /**
     * Temporary hint letter shown during רמז for stations 1–3 and 5 (same `when` as legacy hint dispatch).
     * Station 4 picture uses [Question.PictureStartsWithQuestion.correctLetter] (starting letter).
     */
    fun targetLetterForHelpHint(question: Question): String? =
        when (question) {
            is Question.PopBalloonsQuestion -> question.correctAnswer
            is Question.FindLetterGridQuestion -> question.targetLetter
            is Question.PictureStartsWithQuestion -> question.correctLetter
            is Question.ImageMatchQuestion -> question.targetLetter
            else -> null
        }
}
