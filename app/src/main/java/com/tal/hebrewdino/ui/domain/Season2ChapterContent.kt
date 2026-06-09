package com.tal.hebrewdino.ui.domain

/**
 * Shared Season 2 chapter word/letter lists.
 * Each chapter word list must satisfy [Season2ChapterAssetValidator] (≥2 words per taught letter).
 */
object Season2ChapterContent {
    val ch3Words =
        listOf(
            "w_ג_1", "w_ג_3",
            "w_נ_1", "w_נ_2", "w_נ_4",
            "w_פ_1", "w_פ_2",
            "w_צ_1", "w_צ_2",
        )

    val ch3Letters = listOf("ג", "נ", "פ", "צ")

    val ch4Words =
        listOf(
            "w_ב_1", "w_ב_2", "w_ב_3",
            "w_ד_1", "w_ד_2", "w_ד_3",
            "w_ת_1", "w_ת_2", "w_ת_3",
            "w_כ_1", "w_כ_2", "w_כ_3",
            "w_ה_1", "w_ה_2",
            "w_ו_1", "w_ו_3",
            "w_ט_1", "w_ט_2",
        )

    val ch4Letters = listOf("ב", "ד", "ת", "כ", "ה", "ו", "ט")

    val ch5Words =
        listOf(
            "w_ש_1", "w_ש_2",
            "w_ח_1", "w_ח_3",
            "w_ר_1", "w_ר_3",
            "w_ק_1", "w_ק_2",
            "w_ס_1", "w_ס_4",
            "w_ז_1", "w_ז_3",
            "w_נ_1", "w_נ_2",
            "w_פ_1", "w_פ_2",
        )

    val ch5Letters = listOf("ח", "ר", "ק", "ש", "ס", "ז", "נ", "פ")

    val ch6Words =
        listOf(
            "w_ב_1", "w_ב_2",
            "w_ח_1", "w_ח_3",
            "w_ו_3",
            "w_ר_1", "w_ר_3",
            "w_ק_1", "w_ק_2",
            "w_ש_1", "w_ש_4",
            "w_ס_1", "w_ס_4",
            "w_ד_1", "w_ד_2",
            "w_כ_2", "w_כ_3",
            "w_פ_1", "w_פ_2",
            "w_צ_1", "w_צ_2",
        )

    val ch6Letters = listOf("ב", "ח", "ר", "ק", "ש", "ס", "ד", "כ", "פ", "צ")
}
