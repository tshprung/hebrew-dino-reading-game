package com.tal.hebrewdino.ui.domain

/**
 * Season 2 chapter indexes (1..6) are used in UX (chapter select, puzzle posters, progress prefs).
 *
 * For station gameplay we use a **separate chapterId range** so Season 2 does not collide with Season 1
 * station behavior registry.
 */
object Season2ChapterIds {
    const val Chapter1Tyrannosaurus: Int = 101
    const val Chapter2Triceratops: Int = 102
    const val Chapter3Stegosaurus: Int = 103
    const val Chapter4Brachiosaurus: Int = 104
    const val Chapter5Ankylosaurus: Int = 105
    const val Chapter6Mosasaurus: Int = 106

    fun chapterGameplayId(chapterIndex: Int): Int =
        when (chapterIndex) {
            1 -> Chapter1Tyrannosaurus
            2 -> Chapter2Triceratops
            3 -> Chapter3Stegosaurus
            4 -> Chapter4Brachiosaurus
            5 -> Chapter5Ankylosaurus
            6 -> Chapter6Mosasaurus
            else -> 100 + chapterIndex
        }
}

