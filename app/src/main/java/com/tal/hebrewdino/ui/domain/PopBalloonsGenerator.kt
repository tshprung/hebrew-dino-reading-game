package com.tal.hebrewdino.ui.domain

import kotlin.random.Random

class PopBalloonsGenerator(
    private val pool: LetterPoolSpec = LetterPoolSpec.Default,
) {
    fun group(groupIndex: Int): List<String> = pool.groups.getOrNull(groupIndex).orEmpty()

    fun generate(
        rnd: Random,
        group: List<String>,
        correctAnswer: String,
        optionCount: Int = 9,
    ): Question.PopBalloonsQuestion {
        require(group.isNotEmpty()) { "Letter group must not be empty" }
        require(correctAnswer in group) { "correctAnswer must be in group" }

        val base = group.distinct()
        // Ensure at least one balloon for *each* letter in the chapter group.
        // Duplicates are allowed (including the correct letter); if correct repeats, user must pop all of them.
        val repeatsCorrect = if (optionCount >= base.size + 2) 2 else 1
        val options =
            buildList {
                addAll(base)
                repeat(repeatsCorrect - 1) { add(correctAnswer) } // base already contains it
                while (size < optionCount) add(base.random(rnd))
            }.shuffled(rnd)
        require(correctAnswer in options)

        return Question.PopBalloonsQuestion(correctAnswer = correctAnswer, options = options)
    }
}

