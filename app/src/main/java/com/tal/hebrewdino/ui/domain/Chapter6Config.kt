package com.tal.hebrewdino.ui.domain

/**
 * Chapter 6: consolidation/review. No new letters.
 *
 * Letter pool is a union of letters taught in Chapters 4–5, in stable order.
 */
object Chapter6Config {
    const val STATION_COUNT: Int = 6

    val letters: List<String> =
        buildList {
            fun addAllUnique(xs: List<String>) {
                for (x in xs) if (x !in this) add(x)
            }
            addAllUnique(Chapter4Config.letters)
            addAllUnique(Chapter5Config.letters)
        }
}

