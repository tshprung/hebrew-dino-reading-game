package com.tal.hebrewdino.ui.domain

data class SyllabusChapter(
    val index: Int,
    val title: String,
    val subtitle: String,
    val letters: List<String>,
)

object HebrewSyllabus {
    val chapters: List<SyllabusChapter> =
        listOf(
            SyllabusChapter(
                index = 0,
                title = "פרק 1",
                subtitle = "אותיות יסוד",
                letters = listOf("א", "מ", "ל", "ס"),
            ),
            SyllabusChapter(
                index = 1,
                title = "פרק 2",
                subtitle = "ב, ו, ק, ר",
                letters = listOf("ב", "ו", "ק", "ר"),
            ),
            SyllabusChapter(
                index = 2,
                title = "פרק 3",
                subtitle = "ד, ה, נ, י",
                letters = listOf("ד", "ה", "נ", "י"),
            ),
            SyllabusChapter(
                index = 3,
                title = "פרק 4",
                subtitle = "ח, ת, צ, ש",
                letters = listOf("ח", "ת", "צ", "ש"),
            ),
        )

    fun chapterOrNull(index: Int): SyllabusChapter? = chapters.getOrNull(index.coerceIn(0, chapters.lastIndex))

    fun lettersLabel(letters: List<String>): String = letters.joinToString("  ")

    val chapterCount: Int get() = chapters.size
}
