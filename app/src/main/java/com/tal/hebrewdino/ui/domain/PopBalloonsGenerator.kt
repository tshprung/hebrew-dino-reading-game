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
            }
                // Shuffle twice to reduce repeated ordering in small option sets (e.g. PickLetter with 3 options),
                // so letter placement “feels” different between rounds.
                .shuffled(rnd)
                .shuffled(rnd)
        require(correctAnswer in options)

        return Question.PopBalloonsQuestion(correctAnswer = correctAnswer, options = options)
    }

    /**
     * Pick-letter station with a larger option set: [correctAnswer] plus distractors from [group],
     * without forcing every pool letter onto the board (unlike [generate] for 7 balloons).
     */
    fun generatePickLetterOptions(
        rnd: Random,
        group: List<String>,
        correctAnswer: String,
        optionCount: Int,
    ): Question.PopBalloonsQuestion {
        require(group.isNotEmpty())
        require(correctAnswer in group)
        require(optionCount in 3..9)
        val base = group.distinct()
        val others = base.filter { it != correctAnswer }.shuffled(rnd)
        val distractorCount = (optionCount - 1).coerceAtLeast(2)
        val picks = others.take(distractorCount.coerceAtMost(others.size))
        val options =
            buildList {
                add(correctAnswer)
                addAll(picks)
                while (size < optionCount) {
                    add(base.random(rnd))
                }
            }
                .shuffled(rnd)
                .shuffled(rnd)
        require(correctAnswer in options)
        return Question.PopBalloonsQuestion(correctAnswer = correctAnswer, options = options)
    }
}

