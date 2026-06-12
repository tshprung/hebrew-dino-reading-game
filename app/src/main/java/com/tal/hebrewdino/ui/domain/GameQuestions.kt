package com.tal.hebrewdino.ui.domain

import androidx.annotation.ColorInt
import com.tal.hebrewdino.R

/** Picture target for [Question.DragWordToPictureQuestion]. */
data class DragWordPicturePair(
    val catalogEntryId: String,
    val word: String,
    val tileDrawable: Int,
    @param:ColorInt val tintArgb: Int,
)

/** Draggable written word card for [Question.DragWordToPictureQuestion]. */
data class WordCard(
    val catalogEntryId: String,
    val word: String,
)

sealed class Question {
    data class PopBalloonsQuestion(
        val correctAnswer: String,
        val options: List<String>,
    ) : Question()

    /** Tap every cell that shows [targetLetter] on a square grid (3×3 or 4×4). */
    data class FindLetterGridQuestion(
        val targetLetter: String,
        val columns: Int,
        val rows: Int,
        val cells: List<String>,
    ) : Question() {
        init {
            require(columns in 3..4 && rows in 3..4)
            require(cells.size == columns * rows)
            require(cells.count { it == targetLetter } >= 3)
        }
    }

    /** Pick the card whose word matches [targetWord] (2–3 large image cards). */
    data class ImageMatchQuestion(
        val targetWord: String,
        val targetLetter: String,
        val choices: List<LessonChoice>,
        val correctChoiceId: String,
    ) : Question() {
        init {
            require(choices.size in 2..6)
            require(choices.any { it.id == correctChoiceId })
        }
    }

    /** Picture + word; tap the letter the word starts with (episode letters as options). */
    data class PictureStartsWithQuestion(
        val word: String,
        val correctLetter: String,
        val catalogEntryId: String,
        val tileDrawable: Int,
        @param:ColorInt val tintArgb: Int,
        val optionLetters: List<String>,
    ) : Question() {
        init {
            require(word.isNotEmpty() && correctLetter.length == 1)
            require(correctLetter in optionLetters)
        }
    }

    /** Fill word slots by dragging / placing letters from the pool (two words). */
    data class FinaleSlotQuestion(
        val words: List<String>,
        val letterPool: List<String>,
        val pairHints: List<PictureLetterPair>,
    ) : Question() {
        init {
            require(words.size == 2)
        }
    }

    /** Picture + partial word with missing first letter; tap the correct first letter. */
    data class MissingFirstLetterQuestion(
        val word: String,
        val catalogEntryId: String,
        val tileDrawable: Int,
        @param:ColorInt val tintArgb: Int,
        /** RTL display, e.g. "_מש" for שמש. */
        val partialWord: String,
        val correctLetter: String,
        val optionLetters: List<String>,
    ) : Question() {
        init {
            require(word.isNotEmpty() && correctLetter.length == 1)
            require(correctLetter in optionLetters)
            require(partialWord.startsWith("_"))
        }
    }

    enum class WordPartsPromptKind {
        ChooseCorrectSplit,
    }

    data class WordPartsSplitOption(
        val firstPart: String,
        val secondPart: String,
    ) {
        val key: String get() = "$firstPart|$secondPart"
    }

    /** Picture + word split recognition (Season 2 word-parts stations). */
    data class WordPartsQuestion(
        val word: String,
        val catalogEntryId: String,
        val tileDrawable: Int,
        @param:ColorInt val tintArgb: Int,
        val firstPart: String,
        val correctPart: String,
        val splitOptions: List<WordPartsSplitOption>,
        val promptKind: WordPartsPromptKind = WordPartsPromptKind.ChooseCorrectSplit,
        val presentationMode: Season2WordPartsPresentationMode = Season2WordPartsPresentationMode.GuidedWordParts,
    ) : Question() {
        init {
            require(firstPart.isNotEmpty() && correctPart.isNotEmpty())
            require(word == firstPart + correctPart)
            require(splitOptions.size == 3)
            require(
                splitOptions.any { it.firstPart == firstPart && it.secondPart == correctPart },
            ) { "correct split must appear in splitOptions for $word" }
        }
    }

    /**
     * Several picture cards and the same words as draggable cards; match each word to its picture.
     * Not player-facing until GameQuestionHost drag UI is wired.
     */
    data class DragWordToPictureQuestion(
        val pairs: List<DragWordPicturePair>,
        val wordBank: List<WordCard>,
    ) : Question() {
        init {
            require(pairs.size in 2..3) { "drag-word-to-picture needs 2..3 pairs" }
            require(pairs.map { it.catalogEntryId }.distinct().size == pairs.size) {
                "drag-word-to-picture pairs must have unique catalog ids"
            }
            val pairIds = pairs.map { it.catalogEntryId }.toSet()
            val bankIds = wordBank.map { it.catalogEntryId }.toSet()
            require(bankIds == pairIds) {
                "wordBank must contain exactly the pair catalog ids"
            }
            require(wordBank.map { it.word }.toSet().size == wordBank.size) {
                "wordBank words must be unique"
            }
        }
    }

    /**
     * Picture + partial word with one missing letter; drag the correct letter into the slot.
     * Not player-facing until GameQuestionHost drag UI is wired.
     */
    data class DragMissingLetterQuestion(
        val word: String,
        val catalogEntryId: String,
        val tileDrawable: Int,
        @param:ColorInt val tintArgb: Int,
        /** 0-based index into [word] graphemes (0 = first visible Hebrew letter). */
        val missingIndex: Int,
        /** RTL display, e.g. "_ג" for דג with missing first letter. */
        val partialWord: String,
        val correctLetter: String,
        val optionLetters: List<String>,
    ) : Question() {
        init {
            require(word.isNotEmpty() && correctLetter.length == 1)
            require(missingIndex in word.indices)
            require(word[missingIndex].toString() == correctLetter)
            require(correctLetter in optionLetters)
            require(optionLetters.distinct().size == optionLetters.size)
            require(partialWord.contains("_"))
        }
    }

    /** Target picture/word + picture choices; pick the rhyming match. */
    data class RhymingQuestion(
        val targetWord: String,
        val targetCatalogEntryId: String,
        val targetTileDrawable: Int,
        @param:ColorInt val targetTintArgb: Int,
        val choices: List<LessonChoice>,
        val correctChoiceId: String,
    ) : Question() {
        init {
            require(choices.size in 2..6)
            require(choices.any { it.id == correctChoiceId })
        }
    }
}

data class LessonChoice(
    val id: String,
    val letter: String,
    val word: String,
    @param:ColorInt val tintArgb: Int,
    val tileDrawable: Int = R.drawable.lesson_word_tile,
)

data class PictureLetterPair(
    val imageRes: Int,
    @param:ColorInt val tintArgb: Int? = null,
    /** Optional Hebrew label under the picture (gameplay secondary). */
    val caption: String?,
    val letter: String,
)

