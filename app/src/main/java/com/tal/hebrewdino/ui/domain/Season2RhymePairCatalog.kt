package com.tal.hebrewdino.ui.domain

/**
 * Curated rhyme pairs with validated image + word audio on both sides.
 * Only pairs passing [Season2StationContentValidator.validateRhymePair] are used in gameplay.
 */
data class Season2RhymePair(
    val targetCatalogId: String,
    val rhymeCatalogId: String,
    /** When set, used instead of random chapter distractors (must be in chapter scope and ≠ target/rhyme). */
    val distractorCatalogIds: List<String> = emptyList(),
)

object Season2RhymePairCatalog {
    private val rhymeOnlyWordIds: List<String> =
        listOf("w_ד_5", "w_ז_5", "w_ג_5", "w_מ_6")

    /** Ch5: -ון cluster review words from chapter 4. */
    private val ch5RhymeReviewIds: List<String> = listOf("w_ב_2", "w_ו_3")

    /** Ch6: review words + rhyme-only additions. */
    private val ch6RhymeReviewIds: List<String> =
        listOf("w_ת_4", "w_ח_2", "w_ד_5", "w_ז_5", "w_ג_5")

    /** Ch7: finale rhyme words not yet in the season union. */
    private val ch7RhymeExtraIds: List<String> =
        listOf("w_ת_4", "w_ד_5", "w_ז_5", "w_ג_5", "w_מ_6")

    private val ch5StationPairs: List<Season2RhymePair> =
        listOf(
            Season2RhymePair("w_ש_4", "w_ח_3", distractorCatalogIds = listOf("w_ק_1", "w_פ_2")),
            Season2RhymePair("w_ב_2", "w_ו_3", distractorCatalogIds = listOf("w_ש_1", "w_ס_4")),
            Season2RhymePair("w_ח_3", "w_ב_2", distractorCatalogIds = listOf("w_ז_1", "w_נ_1")),
            Season2RhymePair("w_ו_3", "w_ש_4", distractorCatalogIds = listOf("w_ק_2", "w_פ_1")),
        )

    private val ch6StationPairs: List<Season2RhymePair> =
        listOf(
            Season2RhymePair("w_ק_1", "w_ת_4", distractorCatalogIds = listOf("w_ס_1", "w_ד_1")),
            Season2RhymePair("w_ר_3", "w_ד_5", distractorCatalogIds = listOf("w_צ_2", "w_כ_3")),
            Season2RhymePair("w_ח_2", "w_ז_5", distractorCatalogIds = listOf("w_פ_2", "w_צ_1")),
            Season2RhymePair("w_ד_1", "w_ג_5", distractorCatalogIds = listOf("w_ב_1", "w_ס_4")),
        )

    private val ch7StationPairs: List<Season2RhymePair> =
        listOf(
            Season2RhymePair("w_ש_4", "w_ח_3", distractorCatalogIds = listOf("w_ק_1", "w_ת_4")),
            Season2RhymePair("w_ב_2", "w_ו_3", distractorCatalogIds = listOf("w_ז_1", "w_ס_4")),
            Season2RhymePair("w_ק_1", "w_ת_4", distractorCatalogIds = listOf("w_ר_3", "w_ד_1")),
            Season2RhymePair("w_ר_3", "w_ד_5", distractorCatalogIds = listOf("w_פ_2", "w_ח_2")),
            Season2RhymePair("w_ח_2", "w_ז_5", distractorCatalogIds = listOf("w_ג_1", "w_נ_2")),
            Season2RhymePair("w_פ_2", "w_מ_6", distractorCatalogIds = listOf("w_ש_1", "w_ר_1")),
        )

    fun wordCatalogIdsForRhymingStation(
        chapterIndex: Int,
        chapterWordCatalogIds: List<String>,
    ): List<String> =
        when (chapterIndex) {
            5 -> (chapterWordCatalogIds + ch5RhymeReviewIds).distinct()
            6 -> (chapterWordCatalogIds + ch6RhymeReviewIds).distinct()
            7 -> (chapterWordCatalogIds + ch7RhymeExtraIds).distinct()
            else -> chapterWordCatalogIds
        }

    /** @deprecated Use [pairsForStation] for Season 2 rhyme stations. */
    fun wordCatalogIdsForChapter5Rhyming(chapterWordCatalogIds: List<String>): List<String> =
        wordCatalogIdsForRhymingStation(5, chapterWordCatalogIds)

    fun pairsForStation(chapterIndex: Int, stationId: Int): List<Season2RhymePair>? =
        when (chapterIndex to stationId) {
            5 to 5 -> ch5StationPairs
            6 to 4 -> ch6StationPairs
            7 to 4 -> ch7StationPairs
            else -> null
        }

    fun pairsForWordIds(wordCatalogIds: List<String>): List<Season2RhymePair> {
        val allowed = wordCatalogIds.toSet()
        return allStationPairs().filter { pair ->
            pair.targetCatalogId in allowed &&
                pair.rhymeCatalogId in allowed &&
                Season2StationContentValidator.validateRhymePair(
                    pair.targetCatalogId,
                    pair.rhymeCatalogId,
                ).isEmpty()
        }
    }

    fun validatedPairs(): List<Season2RhymePair> =
        allStationPairs().filter {
            Season2StationContentValidator.validateRhymePair(it.targetCatalogId, it.rhymeCatalogId).isEmpty()
        }

    fun rhymeOnlyCatalogIds(): List<String> = rhymeOnlyWordIds

    private fun allStationPairs(): List<Season2RhymePair> =
        ch5StationPairs + ch6StationPairs + ch7StationPairs

    fun rhymeSiblingCatalogIds(targetCatalogId: String, correctRhymeId: String): Set<String> {
        val asTarget =
            allStationPairs()
                .filter { it.targetCatalogId == targetCatalogId && it.rhymeCatalogId != correctRhymeId }
                .map { it.rhymeCatalogId }
        val asRhyme =
            allStationPairs()
                .filter { it.rhymeCatalogId == targetCatalogId && it.targetCatalogId != correctRhymeId }
                .map { it.targetCatalogId }
        return (asTarget + asRhyme).toSet()
    }
}
