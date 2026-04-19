package com.tal.hebrewdino.ui.domain

import kotlin.random.Random

object FinaleSlotGenerator {
    fun generate(
        rnd: Random,
        group: List<String>,
        letter1: String,
        letter2: String,
    ): Question.FinaleSlotQuestion {
        require(letter1 in group && letter2 in group && letter1 != letter2)
        val (e1, e2) = LessonWordCatalog.pickTwoDistinctForLetters(rnd, letter1, letter2)
        val w1 = e1.word
        val w2 = e2.word
        val needed = (w1.map { it.toString() } + w2.map { it.toString() })
        val poolBase = needed.toMutableList()
        val extras = group.filter { it !in poolBase }.shuffled(rnd).take(3)
        for (x in extras) {
            if (poolBase.size >= needed.size + 5) break
            poolBase.add(x)
        }
        val letterPool = poolBase.shuffled(rnd)
        val hints =
            listOf(
                PictureLetterPair(
                    imageRes = e1.tileRes,
                    tintArgb = e1.tintArgb,
                    caption = e1.word,
                    letter = letter1,
                ),
                PictureLetterPair(
                    imageRes = e2.tileRes,
                    tintArgb = e2.tintArgb,
                    caption = e2.word,
                    letter = letter2,
                ),
            ).shuffled(rnd)
        return Question.FinaleSlotQuestion(
            words = listOf(w1, w2),
            letterPool = letterPool,
            pairHints = hints,
        )
    }
}
