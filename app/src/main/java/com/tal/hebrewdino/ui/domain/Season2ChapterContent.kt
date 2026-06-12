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
            "w_ש_1", "w_ש_4",
        )

    /** ג נ פ צ + ש (review/reinforcement from earlier chapters). */
    val ch3Letters = listOf("ג", "נ", "פ", "צ", "ש")

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
            "w_ש_1", "w_ש_2", "w_ש_4",
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

    val ch1Words =
        listOf(
            "w_ז_1", "w_ז_2", "w_ז_3",
            "w_י_3", "w_י_4",
            "w_ס_1", "w_ס_4",
            "w_ע_1", "w_ע_7",
            "w_מ_2", "w_מ_3",
            "w_ל_1", "w_ל_2",
        )

    val ch1Letters = listOf("ז", "י", "ס", "ע", "מ", "ל")

    val ch2Words =
        listOf(
            "w_ח_1", "w_ח_2", "w_ח_3",
            "w_ר_1", "w_ר_3", "w_ר_4",
            "w_ק_1", "w_ק_2", "w_ק_3",
            "w_ש_1", "w_ש_2", "w_ש_4",
            "w_מ_2", "w_מ_3",
            "w_פ_2",
        )

    val ch2Letters = listOf("ח", "ר", "ק", "ש", "מ")

    /** Deduped union of Season 2 Chapters 1–6 catalog ids (review pool for Ch7). */
    val season2UnionWordCatalogIds: List<String> =
        (ch1Words + ch2Words + ch3Words + ch4Words + ch5Words + ch6Words).distinct()

    /** Deduped union of Season 2 Chapters 1–6 taught letters (no new letters in Ch7). */
    val season2UnionLetters: List<String> =
        (ch1Letters + ch2Letters + ch3Letters + ch4Letters + ch5Letters + ch6Letters).distinct()

    val ch7Words: List<String> = season2UnionWordCatalogIds

    val ch7Letters: List<String> = season2UnionLetters
}
