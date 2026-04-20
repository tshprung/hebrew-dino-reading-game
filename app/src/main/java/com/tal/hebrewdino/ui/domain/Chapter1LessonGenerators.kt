package com.tal.hebrewdino.ui.domain

import kotlin.random.Random

internal object Chapter1LessonGenerators {
    fun pictureStartsWith(
        rnd: Random,
        group: List<String>,
        targetLetter: String,
        excludeCorrectWordIds: Set<String>,
        optionLetters: List<String>,
    ): Question.PictureStartsWithQuestion {
        require(targetLetter in group)
        val correct = LessonWordCatalog.pickRandomForImageMatch(rnd, targetLetter, excludeCorrectWordIds)
        require(correct.letter in optionLetters)
        return Question.PictureStartsWithQuestion(
            word = correct.word,
            correctLetter = correct.letter,
            catalogEntryId = correct.id,
            tileDrawable = correct.tileRes,
            tintArgb = correct.tintArgb,
            optionLetters = optionLetters,
        )
    }

    fun imageMatch(
        rnd: Random,
        group: List<String>,
        targetLetter: String,
        /** Prefer words not yet shown for this letter in the current station session. */
        excludeCorrectWordIds: Set<String> = emptySet(),
        alwaysThreeChoices: Boolean = false,
    ): Question.ImageMatchQuestion {
        require(targetLetter in group)
        val correct = LessonWordCatalog.pickRandomForImageMatch(rnd, targetLetter, excludeCorrectWordIds)
        val otherLetters = group.filter { it != targetLetter }.shuffled(rnd)
        val cardCount = if (alwaysThreeChoices) 3 else if (rnd.nextBoolean()) 2 else 3
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
