package com.tal.hebrewdino.ui.domain

import androidx.annotation.ColorInt
import com.tal.hebrewdino.R

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
        PickSecondPart,
    }

    /** Connect beginning letter + rest of word (Season 2 word-parts station). */
    data class WordPartsQuestion(
        val word: String,
        val catalogEntryId: String,
        val tileDrawable: Int,
        @param:ColorInt val tintArgb: Int,
        val firstPart: String,
        val correctPart: String,
        val partOptions: List<String>,
        val promptKind: WordPartsPromptKind,
        val presentationMode: Season2WordPartsPresentationMode = Season2WordPartsPresentationMode.GuidedWordParts,
    ) : Question() {
        init {
            require(firstPart.isNotEmpty() && correctPart.isNotEmpty())
            require(correctPart in partOptions)
            require(word == firstPart + correctPart || promptKind == WordPartsPromptKind.PickSecondPart)
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

