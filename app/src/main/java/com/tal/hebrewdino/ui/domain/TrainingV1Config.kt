package com.tal.hebrewdino.ui.domain

object TrainingV1Config {
    const val CHAPTER_ID: Int = 7
    const val TOTAL_ROUNDS: Int = 10

    const val STATION_HEAR_LETTER_CHOOSE: Int = 1
    const val STATION_WHICH_WORD_STARTS_WITH_LETTER: Int = 2
    const val STATION_PICTURE_CHOOSE_WORD: Int = 3
    const val STATION_FIND_HEARD_LETTER_IN_GRID: Int = 4
    const val STATION_WORD_BALLOONS: Int = 5
    const val STATION_MATCH_LETTER_TO_WORD: Int = 6

    val letters: List<String> =
        buildList {
            fun addAllUnique(src: List<String>) {
                for (l in src) if (l !in this) add(l)
            }
            addAllUnique(Chapter1Config.letters)
            addAllUnique(Chapter2Config.letters)
            addAllUnique(Chapter4Config.letters)
            addAllUnique(Chapter5Config.letters)
        }

    fun balloonWordCatalogPairs(): List<Pair<String, String>> =
        listOf(
            "שמש" to "w_ש_1",
            "דבש" to "w_ד_4",
            "גמל" to "w_ג_1",
            "לב" to "w_ל_2",
            "רגל" to "w_ר_3",
            "כלב" to "w_כ_2",
        )
}
