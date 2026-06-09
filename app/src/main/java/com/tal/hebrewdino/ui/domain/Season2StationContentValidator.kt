package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips

/**
 * Reusable validation for Season 2 advanced station plans.
 * Fails loudly — never silently skips missing image/audio.
 */
object Season2StationContentValidator {
    data class WordAssetCheck(
        val catalogId: String,
        val word: String,
        val hasImage: Boolean,
        val hasWordAudio: Boolean,
    ) {
        val isValid: Boolean = hasImage && hasWordAudio
    }

    fun wordAssetCheck(catalogId: String): WordAssetCheck? {
        val entry = LessonWordCatalog.entries.find { it.id == catalogId } ?: return null
        return WordAssetCheck(
            catalogId = catalogId,
            word = entry.word,
            hasImage = entry.tileRes != R.drawable.lesson_pic_placeholder,
            hasWordAudio = AudioClips.wordRawResIdByCatalogId(catalogId) != null,
        )
    }

    fun requireValidatedWord(catalogId: String): LessonWordEntry {
        val entry =
            LessonWordCatalog.entries.find { it.id == catalogId }
                ?: error("Missing LessonWordCatalog entry: $catalogId")
        val check = wordAssetCheck(catalogId)!!
        require(check.hasImage) { "Missing image asset for $catalogId (${entry.word})" }
        require(check.hasWordAudio) { "Missing word audio for $catalogId (${entry.word})" }
        return entry
    }

    fun validateWords(catalogIds: List<String>): List<String> {
        val missing = mutableListOf<String>()
        for (id in catalogIds.distinct()) {
            val check = wordAssetCheck(id)
            when {
                check == null -> missing.add("LessonWordCatalog entry $id")
                !check.hasImage -> missing.add("lesson_pic image for $id (${check.word})")
                !check.hasWordAudio -> missing.add("word audio for $id (${check.word})")
            }
        }
        return missing
    }

    fun validateLetterNameAudio(letter: String): String? =
        if (AudioClips.letterNameRawResId(letter) == null) {
            "letter_name audio for '$letter'"
        } else {
            null
        }

    fun validateLetters(letters: List<String>): List<String> =
        letters.distinct().mapNotNull { validateLetterNameAudio(it) }

    fun validateRhymePair(targetCatalogId: String, rhymeCatalogId: String): List<String> {
        val missing = mutableListOf<String>()
        missing.addAll(validateWords(listOf(targetCatalogId, rhymeCatalogId)))
        if (targetCatalogId == rhymeCatalogId) {
            missing.add("rhyme pair must use two distinct catalog ids")
        }
        return missing
    }

    fun validateWordPartsEntry(entry: Season2WordPartsEntry): List<String> {
        val missing = mutableListOf<String>()
        missing.addAll(validateWords(listOf(entry.catalogId)))
        val wordEntry = LessonWordCatalog.entries.find { it.id == entry.catalogId }
        if (wordEntry != null) {
            val expected = entry.firstPart + entry.secondPart
            if (wordEntry.word != expected) {
                missing.add(
                    "word-parts split mismatch for ${entry.catalogId}: " +
                        "expected '$expected' got '${wordEntry.word}'",
                )
            }
            if (entry.firstPart.length == 1) {
                missing.addAll(validateLetters(listOf(entry.firstPart)))
            }
        }
        return missing
    }

    fun isValidForMissingFirstLetter(catalogId: String): Boolean {
        val entry = LessonWordCatalog.entries.find { it.id == catalogId } ?: return false
        val check = wordAssetCheck(catalogId) ?: return false
        return check.isValid && entry.word.length >= 3
    }

    fun validateAdvancedPlan(plan: Season2AdvancedStationPlan): List<String> {
        val missing = mutableListOf<String>()
        missing.addAll(validateWords(plan.wordCatalogIds))
        when (plan.mode) {
            Season2AdvancedStationMode.PictureToWord -> {
                if (plan.wordCatalogIds.distinct().size < 3) {
                    missing.add("picture-to-word needs at least 3 validated words")
                }
            }
            Season2AdvancedStationMode.MissingFirstLetter -> {
                val eligible =
                    plan.wordCatalogIds.filter { isValidForMissingFirstLetter(it) }
                if (eligible.isEmpty()) {
                    missing.add("missing-first-letter needs at least one word with length>=3 and full assets")
                }
                missing.addAll(validateLetters(plan.distractorLetters))
            }
            Season2AdvancedStationMode.WordParts -> {
                val specs =
                    if (plan.wordPartsPresentationMode != null) {
                        Season2WordPartsCatalog.entriesForPresentationMode(
                            plan.wordCatalogIds,
                            plan.wordPartsPresentationMode,
                        )
                    } else {
                        Season2WordPartsCatalog.entriesForWordIds(plan.wordCatalogIds)
                    }
                if (specs.isEmpty()) {
                    missing.add("word-parts needs at least one validated word-parts entry")
                }
                specs.forEach { missing.addAll(validateWordPartsEntry(it)) }
            }
            Season2AdvancedStationMode.Rhyming -> {
                val pairs = Season2RhymePairCatalog.pairsForWordIds(plan.wordCatalogIds)
                if (pairs.isEmpty()) {
                    missing.add("rhyming needs at least one validated rhyme pair")
                }
                pairs.forEach { pair ->
                    missing.addAll(validateRhymePair(pair.targetCatalogId, pair.rhymeCatalogId))
                }
            }
        }
        return missing.distinct()
    }
}
