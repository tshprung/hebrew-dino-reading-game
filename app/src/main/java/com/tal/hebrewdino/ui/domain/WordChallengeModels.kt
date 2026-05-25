package com.tal.hebrewdino.ui.domain

enum class ChallengeType {
    ODD_ONE_OUT,
    RHYME,
    WORD_MATCH,
    IMAGE_MATCH,
}

data class WordChallenge(
    val id: String,
    val questionText: String,
    val options: List<String>,
    val correctOption: String,
    val challengeType: ChallengeType,
    val category: String,
)

