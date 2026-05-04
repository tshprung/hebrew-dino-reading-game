package com.tal.hebrewdino.ui.domain

/** Single-group pool: all chapter letters used together for generators / matching. */
object Chapter1LetterPoolSpec : LetterPoolSpec {
    override val groups: List<List<String>> = listOf(Chapter1Config.letters)
}

object Chapter2LetterPoolSpec : LetterPoolSpec {
    override val groups: List<List<String>> = listOf(Chapter2Config.letters)
}

object Chapter3LetterPoolSpec : LetterPoolSpec {
    override val groups: List<List<String>> = listOf(Chapter3Config.letters)
}

object Chapter4LetterPoolSpec : LetterPoolSpec {
    override val groups: List<List<String>> = listOf(Chapter4Config.letters)
}

object Chapter5LetterPoolSpec : LetterPoolSpec {
    override val groups: List<List<String>> = listOf(Chapter5Config.letters)
}
