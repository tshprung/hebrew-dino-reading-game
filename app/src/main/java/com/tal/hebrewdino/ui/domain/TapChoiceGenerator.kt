package com.tal.hebrewdino.ui.domain

import kotlin.random.Random

class TapChoiceGenerator(
    private val pool: LetterPoolSpec = LetterPoolSpec.Default,
) {
    fun group(groupIndex: Int): List<String> = pool.groups.getOrNull(groupIndex).orEmpty()

    fun generateTapChoiceQuestion(
        rnd: Random,
        group: List<String>,
        correctAnswer: String,
        optionCount: Int = 3,
    ): Question.TapChoiceQuestion {
        require(group.isNotEmpty()) { "Letter group must not be empty" }
        require(correctAnswer in group) { "correctAnswer must be in group" }
        val options =
            buildList {
                add(correctAnswer)
                val others = group.filter { it != correctAnswer }.shuffled(rnd)
                addAll(others.take((optionCount - 1).coerceAtLeast(0)))
            }.shuffled(rnd)

        return Question.TapChoiceQuestion(correctAnswer = correctAnswer, options = options)
    }
}

interface LetterPoolSpec {
    val groups: List<List<String>>

    data object Default : LetterPoolSpec {
        override val groups: List<List<String>> = LetterPool.groups
    }
}

