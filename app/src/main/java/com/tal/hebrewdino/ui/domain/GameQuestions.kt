package com.tal.hebrewdino.ui.domain

import androidx.annotation.ColorInt
import com.tal.hebrewdino.R

sealed class Question {
    data class TapChoiceQuestion(
        val correctAnswer: String,
        val options: List<String>,
    ) : Question()

    data class PopBalloonsQuestion(
        val correctAnswer: String,
        val options: List<String>,
    ) : Question()

    /** Letters sit under face-down tiles; tap a tile to reveal, then the choice is that letter. */
    data class RevealTilesQuestion(
        val correctAnswer: String,
        val options: List<String>,
    ) : Question()

    /** Two pictures and two letters; child taps a picture then the matching letter (tap–tap). */
    data class PictureLetterMatchQuestion(
        val pairs: List<PictureLetterPair>,
    ) : Question() {
        init {
            require(pairs.size == 2) { "Chapter 1 finale uses exactly two picture–letter pairs" }
        }
    }

    /** Tap the one picture whose word starts with [targetLetter] (three choices). */
    data class PicturePickOneQuestion(
        val targetLetter: String,
        val choices: List<LessonChoice>,
    ) : Question() {
        init {
            require(choices.size == 3)
            require(choices.count { it.letter == targetLetter } == 1)
        }
    }

    /** Select exactly the two pictures that start with [targetLetter]; max four choices. */
    data class PicturePickAllQuestion(
        val targetLetter: String,
        val choices: List<LessonChoice>,
        val correctIds: Set<String>,
    ) : Question() {
        init {
            require(choices.size <= 4)
            require(correctIds.size == 2)
            require(correctIds.all { id -> choices.any { it.id == id } })
            require(choices.count { it.id in correctIds && it.letter == targetLetter } == 2)
        }
    }
}

data class LessonChoice(
    val id: String,
    val letter: String,
    val word: String,
    @ColorInt val tintArgb: Int,
    val tileDrawable: Int = R.drawable.lesson_word_tile,
)

data class PictureLetterPair(
    val imageRes: Int,
    @ColorInt val tintArgb: Int? = null,
    /** Optional Hebrew label under the picture (gameplay secondary). */
    val caption: String?,
    val letter: String,
)

