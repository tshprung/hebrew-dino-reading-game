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
        listOf(
            SpellRound("כלב", "w_כ_2", 0, "כ"),
            SpellRound("כלב", "w_כ_2", 2, "ב"),
            SpellRound("חלב", "w_ח_2", 0, "ח"),
            SpellRound("חלב", "w_ח_2", 2, "ב"),
            SpellRound("טוסט", "w_ט_1", 0, "ט"),
            SpellRound("טוסט", "w_ט_1", 3, "ט"),
            SpellRound("תיק", "w_ת_2", 0, "ת"),
            SpellRound("תיק", "w_ת_2", 2, "ק"),
        )

    fun pickSpellRound(questionIndex: Int): SpellRound =
        spellRounds[questionIndex.coerceIn(0, spellRounds.lastIndex)]

    fun spellCompletesWordAfterCorrectRound(questionIndex: Int): Boolean {
        val r = pickSpellRound(questionIndex)
        return r.slotIndex == r.word.lastIndex
    }
}

