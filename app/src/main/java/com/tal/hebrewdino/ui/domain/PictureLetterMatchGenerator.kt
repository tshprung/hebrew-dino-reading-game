package com.tal.hebrewdino.ui.domain

import kotlin.random.Random

class PictureLetterMatchGenerator(
    private val pool: LetterPoolSpec = LetterPoolSpec.Default,
) {
    fun group(groupIndex: Int): List<String> = pool.groups.getOrNull(groupIndex).orEmpty()

    fun generate(
        rnd: Random,
        group: List<String>,
        letter1: String,
        letter2: String,
    ): Question.PictureLetterMatchQuestion {
        require(group.isNotEmpty())
        require(letter1 in group && letter2 in group)
        require(letter1 != letter2)

        val (e1, e2) = LessonWordCatalog.pickTwoDistinctForLetters(rnd, letter1, letter2)
        val pairs =
            listOf(
                PictureLetterPair(
                    imageRes = e1.tileDrawable,
                    tintArgb = e1.tintArgb,
                    caption = e1.word,
                    letter = letter1,
                ),
                PictureLetterPair(
                    imageRes = e2.tileDrawable,
                    tintArgb = e2.tintArgb,
                    caption = e2.word,
                    letter = letter2,
                ),
            ).shuffled(rnd)
        return Question.PictureLetterMatchQuestion(pairs = pairs)
    }
}
