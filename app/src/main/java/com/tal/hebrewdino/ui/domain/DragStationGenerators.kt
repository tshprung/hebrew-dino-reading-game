package com.tal.hebrewdino.ui.domain

import kotlin.random.Random

/** Pure generators for drag station questions (Season 1 + Season 2). */
object DragStationGenerators {
    fun dragWordToPicture(
        rnd: Random,
        wordCatalogIds: List<String>,
        pairCount: Int = 2,
        excludeCorrectIds: Set<String> = emptySet(),
    ): Question.DragWordToPictureQuestion {
        val validated =
            wordCatalogIds
                .distinct()
                .map { Season2StationContentValidator.requireValidatedWord(it) }
        val desiredPairs = pairCount.coerceIn(2, 3)
        require(validated.size >= desiredPairs) {
            "drag-word-to-picture needs at least $desiredPairs validated words; got ${validated.size}"
        }
        val pool = validated.filter { it.id !in excludeCorrectIds }
        val selected =
            (if (pool.size >= desiredPairs) pool else validated)
                .shuffled(rnd)
                .take(desiredPairs)
        val pairs =
            selected.map { entry ->
                DragWordPicturePair(
                    catalogEntryId = entry.id,
                    word = entry.word,
                    tileDrawable = entry.tileRes,
                    tintArgb = entry.tintArgb,
                )
            }
        val wordBank =
            pairs
                .map { WordCard(catalogEntryId = it.catalogEntryId, word = it.word) }
                .shuffled(rnd)
        return Question.DragWordToPictureQuestion(
            pairs = pairs,
            wordBank = wordBank,
        )
    }

    fun dragMissingLetter(
        rnd: Random,
        catalogId: String,
        distractorLetters: List<String>,
        missingIndex: Int = 0,
        optionCount: Int = 3,
    ): Question.DragMissingLetterQuestion {
        val entry = Season2StationContentValidator.requireValidatedWord(catalogId)
        val graphemes = entry.word.map { it.toString() }
        require(graphemes.isNotEmpty()) { "drag-missing-letter word must not be empty: ${entry.word}" }
        require(missingIndex in graphemes.indices) {
            "missingIndex=$missingIndex out of range for word '${entry.word}'"
        }
        val correctLetter = graphemes[missingIndex]
        val partialWord =
            graphemes
                .mapIndexed { index, letter -> if (index == missingIndex) "_" else letter }
                .joinToString("")
        val pool = distractorLetters.distinct().filter { it.length == 1 }
        require(pool.isNotEmpty()) { "drag-missing-letter needs distractor letters" }
        val desired = optionCount.coerceIn(2, 6)
        val distractors =
            pool
                .filter { it != correctLetter }
                .shuffled(rnd)
                .take((desired - 1).coerceAtLeast(1))
        val options =
            (listOf(correctLetter) + distractors)
                .distinct()
                .take(desired)
                .shuffled(rnd)
        require(correctLetter in options) { "correct letter must appear in options" }
        return Question.DragMissingLetterQuestion(
            word = entry.word,
            catalogEntryId = entry.id,
            tileDrawable = entry.tileRes,
            tintArgb = entry.tintArgb,
            missingIndex = missingIndex,
            partialWord = partialWord,
            correctLetter = correctLetter,
            optionLetters = options,
        )
    }

    fun isValidForDragMissingLetter(catalogId: String, missingIndex: Int = 0): Boolean {
        val entry = LessonWordCatalog.entries.find { it.id == catalogId } ?: return false
        if (missingIndex !in entry.word.indices) return false
        return Season2StationContentValidator.wordAssetCheck(catalogId)?.isValid == true
    }
}
