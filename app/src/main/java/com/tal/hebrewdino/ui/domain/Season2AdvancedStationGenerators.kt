package com.tal.hebrewdino.ui.domain

import kotlin.random.Random

/** Pure question generators for Season 2 advanced station modes. */
object Season2AdvancedStationGenerators {
    private fun LessonWordEntry.toChoice(): LessonChoice =
        LessonChoice(
            id = id,
            letter = letter,
            word = word,
            tintArgb = tintArgb,
            tileDrawable = tileRes,
        )

    fun pictureToWord(
        rnd: Random,
        wordCatalogIds: List<String>,
        excludeCorrectIds: Set<String> = emptySet(),
        choiceCount: Int = 3,
    ): Question.ImageMatchQuestion {
        val validated =
            wordCatalogIds
                .distinct()
                .map { Season2StationContentValidator.requireValidatedWord(it) }
        require(validated.size >= choiceCount) {
            "picture-to-word needs at least $choiceCount validated words; got ${validated.size}"
        }
        val pool = validated.filter { it.id !in excludeCorrectIds }
        val correct = (if (pool.isNotEmpty()) pool else validated).random(rnd)
        val distractorPool =
            validated.filter { it.id != correct.id && it.word != correct.word }
        require(distractorPool.size >= choiceCount - 1) {
            "picture-to-word needs ${choiceCount - 1} distractors for ${correct.word}"
        }
        val wrong = distractorPool.shuffled(rnd).take(choiceCount - 1)
        val choices = (wrong + correct).map { it.toChoice() }.shuffled(rnd)
        return Question.ImageMatchQuestion(
            targetWord = correct.word,
            targetLetter = correct.letter,
            choices = choices,
            correctChoiceId = correct.id,
        )
    }

    fun missingFirstLetter(
        rnd: Random,
        catalogId: String,
        distractorLetters: List<String>,
        optionCount: Int = 3,
    ): Question.MissingFirstLetterQuestion {
        val entry = Season2StationContentValidator.requireValidatedWord(catalogId)
        require(entry.word.length >= 3) {
            "missing-first-letter word must have length>=3: ${entry.word}"
        }
        val firstLetter = entry.word.first().toString()
        val remainder = entry.word.drop(1)
        val partialWord = "_$remainder"
        val pool = distractorLetters.distinct().filter { it.length == 1 }
        require(firstLetter in pool || pool.isNotEmpty()) {
            "missing-first-letter needs distractor letters"
        }
        val desired = optionCount.coerceIn(2, 6)
        val distractors =
            pool
                .filter { it != firstLetter }
                .shuffled(rnd)
                .take((desired - 1).coerceAtLeast(1))
        val options = (listOf(firstLetter) + distractors).distinct().take(desired).shuffled(rnd)
        require(firstLetter in options) { "correct letter must appear in options" }
        return Question.MissingFirstLetterQuestion(
            word = entry.word,
            catalogEntryId = entry.id,
            tileDrawable = entry.tileRes,
            tintArgb = entry.tintArgb,
            partialWord = partialWord,
            correctLetter = firstLetter,
            optionLetters = options,
        )
    }

    fun wordPartsChooseCorrectSplit(
        rnd: Random,
        spec: Season2WordPartsEntry,
        distractorSpecs: List<Season2WordPartsEntry>,
        presentationMode: Season2WordPartsPresentationMode = Season2WordPartsPresentationMode.GuidedWordParts,
    ): Question.WordPartsQuestion {
        Season2StationContentValidator.requireValidatedWord(spec.catalogId)
        val entry = LessonWordCatalog.entries.first { it.id == spec.catalogId }
        val correct = Question.WordPartsSplitOption(spec.firstPart, spec.secondPart)
        val distractors =
            distractorSpecs
                .filter { it.catalogId != spec.catalogId }
                .distinctBy { "${it.firstPart}|${it.secondPart}" }
                .shuffled(rnd)
                .take(2)
                .map { Question.WordPartsSplitOption(it.firstPart, it.secondPart) }
        require(distractors.size == 2) {
            "word-parts choose-split needs 2 distractor splits for ${spec.catalogId}"
        }
        val options = (listOf(correct) + distractors).shuffled(rnd)
        return Question.WordPartsQuestion(
            word = entry.word,
            catalogEntryId = entry.id,
            tileDrawable = entry.tileRes,
            tintArgb = entry.tintArgb,
            firstPart = spec.firstPart,
            correctPart = spec.secondPart,
            splitOptions = options,
            presentationMode = presentationMode,
        )
    }

    fun rhyming(
        rnd: Random,
        pair: Season2RhymePair,
        wordCatalogIds: List<String>,
    ): Question.RhymingQuestion {
        require(
            Season2StationContentValidator.validateRhymePair(pair.targetCatalogId, pair.rhymeCatalogId).isEmpty(),
        ) {
            "invalid rhyme pair ${pair.targetCatalogId} / ${pair.rhymeCatalogId}"
        }
        val target = Season2StationContentValidator.requireValidatedWord(pair.targetCatalogId)
        val rhyme = Season2StationContentValidator.requireValidatedWord(pair.rhymeCatalogId)
        val distractorPool =
            wordCatalogIds
                .map { Season2StationContentValidator.requireValidatedWord(it) }
                .filter { it.id != rhyme.id && it.id != target.id }
        require(distractorPool.size >= 2) {
            "rhyming needs at least 2 distractor words in catalog scope"
        }
        val distractors = distractorPool.shuffled(rnd).take(2)
        val choices = (distractors + rhyme).map { it.toChoice() }.shuffled(rnd)
        return Question.RhymingQuestion(
            targetWord = target.word,
            targetCatalogEntryId = target.id,
            targetTileDrawable = target.tileRes,
            targetTintArgb = target.tintArgb,
            choices = choices,
            correctChoiceId = rhyme.id,
        )
    }

    fun generateForMode(
        rnd: Random,
        mode: Season2AdvancedStationMode,
        wordCatalogIds: List<String>,
        roundIndex: Int,
        excludeCorrectIds: Set<String>,
        distractorLetters: List<String>,
        wordPartsPresentationMode: Season2WordPartsPresentationMode? = null,
    ): Question {
        when (mode) {
            Season2AdvancedStationMode.PictureToWord ->
                return pictureToWord(
                    rnd = rnd,
                    wordCatalogIds = wordCatalogIds,
                    excludeCorrectIds = excludeCorrectIds,
                )
            Season2AdvancedStationMode.MissingFirstLetter -> {
                val eligible =
                    wordCatalogIds.filter { Season2StationContentValidator.isValidForMissingFirstLetter(it) }
                require(eligible.isNotEmpty()) { "no eligible missing-first-letter words" }
                val catalogId = eligible[roundIndex % eligible.size]
                return missingFirstLetter(
                    rnd = rnd,
                    catalogId = catalogId,
                    distractorLetters = distractorLetters,
                )
            }
            Season2AdvancedStationMode.WordParts -> {
                val presentation =
                    wordPartsPresentationMode ?: Season2WordPartsPresentationMode.GuidedWordParts
                val specs =
                    Season2WordPartsCatalog.entriesForPresentationMode(wordCatalogIds, presentation)
                require(specs.isNotEmpty()) { "no validated word-parts entries" }
                val spec = specs[roundIndex % specs.size]
                require(specs.size >= 3) {
                    "word-parts choose-split needs at least 3 validated splits in scope"
                }
                return wordPartsChooseCorrectSplit(
                    rnd = rnd,
                    spec = spec,
                    distractorSpecs = specs.filter { it.catalogId != spec.catalogId },
                    presentationMode = presentation,
                )
            }
            Season2AdvancedStationMode.Rhyming -> {
                val pairs = Season2RhymePairCatalog.pairsForWordIds(wordCatalogIds)
                require(pairs.isNotEmpty()) { "no validated rhyme pairs" }
                val pair = pairs[roundIndex % pairs.size]
                return rhyming(rnd = rnd, pair = pair, wordCatalogIds = wordCatalogIds)
            }
        }
    }
}
