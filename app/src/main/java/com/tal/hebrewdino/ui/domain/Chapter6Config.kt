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

    data class SpellRound(
        val word: String,
        val catalogId: String,
        val slotIndex: Int,
        val correctLetter: String,
    )

    private val spellRounds: List<SpellRound> =
        buildList {
            val words =
                listOf(
                    "כלב" to "w_כ_2",
                    "חלב" to "w_ח_2",
                    "טוסט" to "w_ט_1",
                    "תיק" to "w_ת_2",
                    "נחש" to "w_נ_4",
                )
            for ((word, catalogId) in words) {
                val letters = word.toCharArray().map { it.toString() }
                for (slotIndex in letters.indices) {
                    add(
                        SpellRound(
                            word = word,
                            catalogId = catalogId,
                            slotIndex = slotIndex,
                            correctLetter = letters[slotIndex],
                        ),
                    )
                }
            }
        }

    fun pickSpellRound(questionIndex: Int): SpellRound =
        spellRounds[questionIndex.coerceIn(0, spellRounds.lastIndex)]

    fun balloonWordCatalogPairs(): List<Pair<String, String>> =
        LessonWordCatalog.entries
            .asSequence()
            .filter { it.letter in letters }
            .map { it.word to it.id }
            .distinctBy { it.first }
            .toList()
}

