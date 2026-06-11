package com.tal.hebrewdino.ui.domain

/**
 * Curated rhyme pairs with validated image + word audio on both sides.
 * Only pairs passing [Season2StationContentValidator.validateRhymePair] are used in gameplay.
 */
data class Season2RhymePair(
    val targetCatalogId: String,
    val rhymeCatalogId: String,
)

object Season2RhymePairCatalog {
    val curatedPairs: List<Season2RhymePair> =
        listOf(
            Season2RhymePair("w_ב_2", "w_ח_3"), // בלון — חלון
            Season2RhymePair("w_ב_2", "w_ו_3"), // בלון — וילון
            Season2RhymePair("w_ח_3", "w_ו_3"), // חלון — וילון
            Season2RhymePair("w_ש_4", "w_ב_2"), // שעון — בלון
            Season2RhymePair("w_ש_4", "w_ח_3"), // שעון — חלון
            Season2RhymePair("w_ש_4", "w_ו_3"), // שעון — וילון
        )

    fun pairsForWordIds(wordCatalogIds: List<String>): List<Season2RhymePair> {
        val allowed = wordCatalogIds.toSet()
        return curatedPairs.filter { pair ->
            pair.targetCatalogId in allowed &&
                pair.rhymeCatalogId in allowed &&
                Season2StationContentValidator.validateRhymePair(
                    pair.targetCatalogId,
                    pair.rhymeCatalogId,
                ).isEmpty()
        }
    }

    fun validatedPairs(): List<Season2RhymePair> =
        curatedPairs.filter {
            Season2StationContentValidator.validateRhymePair(it.targetCatalogId, it.rhymeCatalogId).isEmpty()
        }
}
