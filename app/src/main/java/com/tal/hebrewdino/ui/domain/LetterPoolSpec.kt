package com.tal.hebrewdino.ui.domain

/** Letter groups used by [LevelSession] difficulty / rotation. */
interface LetterPoolSpec {
    val groups: List<List<String>>

    companion object {
        val Default: LetterPoolSpec =
            object : LetterPoolSpec {
                override val groups: List<List<String>> = LetterPool.groups
            }
    }
}
