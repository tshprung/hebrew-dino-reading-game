package com.tal.hebrewdino.ui.domain

import kotlin.random.Random

/** Episode 3: picture / image stations prefer the six episode words when possible. */
internal object Chapter3LessonGenerators {
    fun pictureStartsWith(
        rnd: Random,
        group: List<String>,
        targetLetter: String,
        excludeCorrectWordIds: Set<String>,
        optionLetters: List<String>,
    ): Question.PictureStartsWithQuestion {
        require(targetLetter in group)
        val candidates =
            LessonWordCatalog.entries.filter {
                it.id in Chapter3EpisodeContent.episodeCatalogIds &&
                    it.letter == targetLetter &&
                    it.id !in excludeCorrectWordIds
            }
        val correct =
            if (candidates.isNotEmpty()) {
                candidates.random(rnd)
            } else {
                LessonWordCatalog.pickRandomForImageMatch(rnd, targetLetter, excludeCorrectWordIds)
            }
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
        excludeCorrectWordIds: Set<String>,
        alwaysThreeChoices: Boolean,
        /** When set (e.g. station 6), exactly this many cards (including the correct one). */
        totalChoiceCount: Int? = null,
    ): Question.ImageMatchQuestion {
        require(targetLetter in group)
        val candidates =
            LessonWordCatalog.entries.filter {
                it.id in Chapter3EpisodeContent.episodeCatalogIds &&
                    it.letter == targetLetter &&
                    it.id !in excludeCorrectWordIds
            }
        val correct =
            if (candidates.isNotEmpty()) {
                candidates.random(rnd)
            } else {
                LessonWordCatalog.pickRandomForImageMatch(rnd, targetLetter, excludeCorrectWordIds)
            }
        val otherLetters = group.filter { it != targetLetter }.shuffled(rnd)
        val cardCount =
            when {
                totalChoiceCount != null -> totalChoiceCount.coerceIn(2, 6)
                alwaysThreeChoices -> 3
                rnd.nextBoolean() -> 2
                else -> 3
            }
        val wrongCount = (cardCount - 1).coerceAtLeast(1)
        val wrongPicks = otherLetters.take(wrongCount)
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
