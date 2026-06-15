package com.tal.hebrewdino.ui.domain

/** Nikkud captions for rhyming-station cards and prompts (catalog [word] stays plain for audio/validation). */
object Season2RhymingDisplayWords {
    private val byCatalogId: Map<String, String> =
        mapOf(
            "w_ב_2" to "בָּלוֹן",
            "w_ח_3" to "חַלּוֹן",
            "w_ו_3" to "וִילוֹן",
            "w_ש_4" to "שָׁעוֹן",
            "w_ק_1" to "קוֹף",
            "w_ת_4" to "תוֹף",
            "w_ר_3" to "רֶגֶל",
            "w_ד_5" to "דֶּגֶל",
            "w_ח_2" to "חָלָב",
            "w_ז_5" to "זָנָב",
            "w_ד_1" to "דָּג",
            "w_ג_5" to "גַּג",
            "w_פ_2" to "פִּיל",
            "w_מ_6" to "מְעִיל",
        )

    fun displayWord(catalogId: String, plainWord: String): String = byCatalogId[catalogId] ?: plainWord
}
