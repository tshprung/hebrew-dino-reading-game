package com.tal.hebrewdino.ui.domain

import kotlin.random.Random

internal object Chapter1LessonGenerators {
    fun imageMatch(
        rnd: Random,
        group: List<String>,
        targetLetter: String,
    ): Question.ImageMatchQuestion {
        require(targetLetter in group)
        val correct = LessonWordCatalog.pickRandom(rnd, targetLetter)
        val otherLetters = group.filter { it != targetLetter }.shuffled(rnd)
        val cardCount = if (rnd.nextBoolean()) 2 else 3
        val wrongPicks =
            when (cardCount) {
                2 -> otherLetters.take(1)
                else -> otherLetters.take(2)
            }
        val wrongChoices =
            wrongPicks.map { letter ->
                LessonWordCatalog.pickRandom(rnd, letter).toChoice()
            }
        val choices = (wrongChoices + correct.toChoice()).shuffled(rnd)
        return Question.ImageMatchQuestion(
            targetWord = correct.word,
            targetLetter = targetLetter,
            choices = choices,
            correctChoiceId = correct.id,
        )
    }

    private fun LessonWordEntry.toChoice(): LessonChoice =
        LessonChoice(
            id = id,
            letter = letter,
            word = word,
            tintArgb = tintArgb,
            tileDrawable = tileRes,
        )
}
