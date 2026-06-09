package com.tal.hebrewdino.ui.domain

/** Season 2 letter pool + curated chapter word ids (for generators and allowlists). */
interface Season2ChapterLetterPool : LetterPoolSpec {
    val chapterIndex: Int
    val wordCatalogIds: List<String>
}

object Season2Chapter3LetterPoolSpec : Season2ChapterLetterPool {
    override val chapterIndex: Int = 3
    override val groups: List<List<String>> = listOf(Season2ChapterContent.ch3Letters)
    override val wordCatalogIds: List<String> = Season2ChapterContent.ch3Words
}

object Season2Chapter4LetterPoolSpec : Season2ChapterLetterPool {
    override val chapterIndex: Int = 4
    override val groups: List<List<String>> = listOf(Season2ChapterContent.ch4Letters)
    override val wordCatalogIds: List<String> = Season2ChapterContent.ch4Words
}

object Season2Chapter5LetterPoolSpec : Season2ChapterLetterPool {
    override val chapterIndex: Int = 5
    override val groups: List<List<String>> = listOf(Season2ChapterContent.ch5Letters)
    override val wordCatalogIds: List<String> = Season2ChapterContent.ch5Words
}

object Season2Chapter6LetterPoolSpec : Season2ChapterLetterPool {
    override val chapterIndex: Int = 6
    override val groups: List<List<String>> = listOf(Season2ChapterContent.ch6Letters)
    override val wordCatalogIds: List<String> = Season2ChapterContent.ch6Words
}
