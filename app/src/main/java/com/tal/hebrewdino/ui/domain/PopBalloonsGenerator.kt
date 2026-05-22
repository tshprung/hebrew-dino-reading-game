package com.tal.hebrewdino.ui.domain

import kotlin.random.Random

class PopBalloonsGenerator(
    private val pool: LetterPoolSpec = LetterPoolSpec.Default,
) {
    fun group(groupIndex: Int): List<String> = pool.groups.getOrNull(groupIndex).orEmpty()

    fun generateWithRepeatedCorrect(
        rnd: Random,
        group: List<String>,
        correctAnswer: String,
        optionCount: Int,
        correctBalloonCountRange: IntRange,
    ): Question.PopBalloonsQuestion {
        require(group.isNotEmpty()) { "Letter group must not be empty" }
        require(correctAnswer in group) { "correctAnswer must be in group" }
        require(optionCount >= 1)
        val base = group.distinct()
        val repeatsCorrect =
            correctBalloonCountRange
                .random(rnd)
                .coerceIn(1, optionCount)
        val fillerPool = base.filter { it != correctAnswer }.ifEmpty { base }
        val options = ArrayList<String>(optionCount)
        repeat(repeatsCorrect) { options.add(correctAnswer) }
        while (options.size < optionCount) {
            options.add(fillerPool.random(rnd))
        }
        options.shuffle(rnd)
        options.shuffle(rnd)
        require(correctAnswer in options)
        return Question.PopBalloonsQuestion(correctAnswer = correctAnswer, options = options)
    }

    fun generate(
        rnd: Random,
        group: List<String>,
        correctAnswer: String,
        optionCount: Int = 9,
        correctBalloonCountRange: IntRange? = null,
    ): Question.PopBalloonsQuestion {
        require(group.isNotEmpty()) { "Letter group must not be empty" }
        require(correctAnswer in group) { "correctAnswer must be in group" }

        val base = group.distinct()
        // Ensure at least one balloon for *each* letter in the chapter group.
        // Duplicates are allowed (including the correct letter); if correct repeats, user must pop all of them.
        val defaultRepeatsCorrect = if (optionCount >= base.size + 2) 2 else 1
        val desiredRepeatsCorrect = correctBalloonCountRange?.random(rnd)?.coerceAtLeast(1)
        val repeatsCorrect =
            if (desiredRepeatsCorrect != null && optionCount >= base.size + (desiredRepeatsCorrect - 1)) {
                desiredRepeatsCorrect
            } else {
                defaultRepeatsCorrect
            }
        val options =
            buildList {
                addAll(base)
                repeat(repeatsCorrect - 1) { add(correctAnswer) } // base already contains it
                if (correctBalloonCountRange != null) {
                    val fillerPool = base.filter { it != correctAnswer }.ifEmpty { base }
                    while (size < optionCount) add(fillerPool.random(rnd))
                } else {
                    while (size < optionCount) add(base.random(rnd))
                }
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

