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
        placeholders: List<Pair<Int, String?>>,
    ): Question.PictureLetterMatchQuestion {
        require(group.isNotEmpty())
        require(letter1 in group && letter2 in group)
        require(letter1 != letter2)
        require(placeholders.size >= 2)

        val shuffledPlaceholders = placeholders.shuffled(rnd)
        val pairs =
            listOf(
                PictureLetterPair(
                    imageRes = shuffledPlaceholders[0].first,
                    caption = shuffledPlaceholders[0].second,
                    letter = letter1,
                ),
                PictureLetterPair(
                    imageRes = shuffledPlaceholders[1].first,
                    caption = shuffledPlaceholders[1].second,
                    letter = letter2,
                ),
            ).shuffled(rnd)
        return Question.PictureLetterMatchQuestion(pairs = pairs)
    }
}
