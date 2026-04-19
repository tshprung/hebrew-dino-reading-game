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
        optionCount: Int = 3,
    ): Question.PopBalloonsQuestion {
        require(group.isNotEmpty()) { "Letter group must not be empty" }
        require(correctAnswer in group) { "correctAnswer must be in group" }

        // Unique letters only — balloon UI must not collapse duplicate option strings.
        val unique =
            buildList {
                add(correctAnswer)
                val others = group.filter { it != correctAnswer }.shuffled(rnd)
                for (o in others) {
                    if (size >= optionCount) break
                    if (o !in this) add(o)
                }
            }.toMutableList()
        val need = minOf(optionCount, group.distinct().size)
        while (unique.size < need) {
            val extra = group.filter { it !in unique }.randomOrNull(rnd) ?: break
            unique.add(extra)
        }
        val options = unique.take(minOf(optionCount, unique.size)).shuffled(rnd)
        require(correctAnswer in options)
        require(options.distinct().size == options.size) { "PopBalloons options must be unique: $options" }

        return Question.PopBalloonsQuestion(correctAnswer = correctAnswer, options = options)
    }
}

