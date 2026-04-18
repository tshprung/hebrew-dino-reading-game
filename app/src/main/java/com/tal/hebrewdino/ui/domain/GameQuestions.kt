package com.tal.hebrewdino.ui.domain

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
}

data class PictureLetterPair(
    val imageRes: Int,
    /** Optional Hebrew label under the picture (gameplay secondary). */
    val caption: String?,
    val letter: String,
)

