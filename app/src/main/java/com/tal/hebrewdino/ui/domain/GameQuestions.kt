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
}
