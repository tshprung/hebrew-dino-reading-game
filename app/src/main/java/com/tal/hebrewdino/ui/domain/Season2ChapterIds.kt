package com.tal.hebrewdino.ui.domain

/**
 * Season 2 chapter indexes (1..6) are used in UX (chapter select, puzzle posters, progress prefs).
 *
 * For station gameplay we use a **separate chapterId range** so Season 2 does not collide with Season 1
 * station behavior registry.
 */
object Season2ChapterIds {
    const val Chapter1Tyrannosaurus: Int = 101
}

