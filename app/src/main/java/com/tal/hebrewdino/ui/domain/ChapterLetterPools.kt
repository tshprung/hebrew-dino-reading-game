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

object Chapter6LetterPoolSpec : LetterPoolSpec {
    override val groups: List<List<String>> = listOf(Chapter6Config.letters)
}

object TrainingV1LetterPoolSpec : LetterPoolSpec {
    override val groups: List<List<String>> = listOf(TrainingV1Config.letters)
}

/** Season 2 Chapter 1 (Tyrannosaurus) letters: ז י ס ע + reinforcement מ. */
object Season2Chapter1LetterPoolSpec : LetterPoolSpec {
    override val groups: List<List<String>> = listOf(listOf("ז", "י", "ס", "ע", "מ"))
}
