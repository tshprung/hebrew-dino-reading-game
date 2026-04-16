package com.tal.hebrewdino.ui.domain

import kotlin.random.Random

class DragToEggGenerator(
    private val pool: LetterPoolSpec = LetterPoolSpec.Default,
) {
    fun group(groupIndex: Int): List<String> = pool.groups.getOrNull(groupIndex).orEmpty()

    fun generate(
        rnd: Random,
        group: List<String>,
        correctAnswer: String,
        optionCount: Int = 3,
    ): Question.DragToEggQuestion {
        require(group.isNotEmpty()) { "Letter group must not be empty" }
        require(correctAnswer in group) { "correctAnswer must be in group" }

        val options =
            buildList {
                add(correctAnswer)
                val others = group.filter { it != correctAnswer }.shuffled(rnd)
                addAll(others.take((optionCount - 1).coerceAtLeast(0)))
            }.shuffled(rnd)

        return Question.DragToEggQuestion(correctAnswer = correctAnswer, options = options)
    }
}

