package com.tal.hebrewdino.ui.domain

import kotlin.random.Random

internal object Chapter1LessonGenerators {
    fun picturePickOne(
        rnd: Random,
        group: List<String>,
        targetLetter: String,
    ): Question.PicturePickOneQuestion {
        require(targetLetter in group)
        val correct = LessonWordCatalog.pickRandom(rnd, targetLetter)
        val otherLetters = group.filter { it != targetLetter }.shuffled(rnd).take(2)
        require(otherLetters.size == 2) { "Need 3 letters in group for picture-pick-one" }
        val d1 = LessonWordCatalog.pickRandom(rnd, otherLetters[0])
        val d2 = LessonWordCatalog.pickRandom(rnd, otherLetters[1])
        val choices =
            listOf(correct, d1, d2)
                .map { it.toChoice() }
                .shuffled(rnd)
        return Question.PicturePickOneQuestion(targetLetter = targetLetter, choices = choices)
    }

    fun picturePickAll(
        rnd: Random,
        group: List<String>,
        targetLetter: String,
    ): Question.PicturePickAllQuestion {
        require(targetLetter in group)
        val first = LessonWordCatalog.pickRandom(rnd, targetLetter)
        var second = LessonWordCatalog.pickRandom(rnd, targetLetter, excludeIds = setOf(first.id))
        var tries = 0
        while (second.word == first.word && tries++ < 6) {
            second = LessonWordCatalog.pickRandom(rnd, targetLetter, excludeIds = setOf(first.id, second.id))
        }
        val otherLetters = group.filter { it != targetLetter }.shuffled(rnd).take(2)
        require(otherLetters.size == 2)
        val w1 = LessonWordCatalog.pickRandom(rnd, otherLetters[0])
        val w2 = LessonWordCatalog.pickRandom(rnd, otherLetters[1])
        val choices =
            listOf(first, second, w1, w2)
                .map { it.toChoice() }
                .shuffled(rnd)
        val correctIds = setOf(first.id, second.id)
        return Question.PicturePickAllQuestion(
            targetLetter = targetLetter,
            choices = choices,
            correctIds = correctIds,
        )
    }

    private fun LessonWordEntry.toChoice(): LessonChoice =
        LessonChoice(
            id = id,
            letter = letter,
            word = word,
            tintArgb = tintArgb,
        )
}
