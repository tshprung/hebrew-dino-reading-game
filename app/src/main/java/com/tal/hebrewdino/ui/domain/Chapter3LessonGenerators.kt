package com.tal.hebrewdino.ui.domain

import kotlin.random.Random

/** Episode 3: picture / image stations prefer the six episode words when possible. */
internal object Chapter3LessonGenerators {
    private val vehicleSynonyms = setOf("אוטו", "רכב", "מכונית")

    private fun pickRandomChoiceAvoidingWords(
        rnd: Random,
        letter: String,
        bannedWords: Set<String>,
    ): LessonChoice {
        val pool =
            LessonWordCatalog.entries
                .asSequence()
                .filter { it.letter == letter }
                .filter { it.word !in bannedWords }
                .toList()
        val picked = (pool.ifEmpty { LessonWordCatalog.entries.filter { it.letter == letter } }).random(rnd)
        return picked.toChoice()
    }

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
        // Station 6: prevent "אוטו/רכב/מכונית" from appearing together (not even as a pair).
        // Apply to all episode-3 image-match rounds to keep vocabulary non-redundant.
        val bannedVehicles =
            if (correct.word in vehicleSynonyms) {
                vehicleSynonyms
            } else {
                emptySet()
            }
        var vehicleAlreadyUsed = correct.word in vehicleSynonyms
        val wrongChoices =
            wrongPicks.map { letter ->
                val banned =
                    when {
                        bannedVehicles.isNotEmpty() -> bannedVehicles
                        vehicleAlreadyUsed -> vehicleSynonyms
                        else -> emptySet()
                    }
                val c = pickRandomChoiceAvoidingWords(rnd, letter, banned)
                if (c.word in vehicleSynonyms) vehicleAlreadyUsed = true
                c
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
