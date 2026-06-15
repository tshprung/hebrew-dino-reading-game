package com.tal.hebrewdino.ui.domain

/**
 * Dedicated drag-missing-letter word pools per Season 2 chapter.
 *
 * Mirrors the S1 Ch5/Ch6 split: adjacent drag-missing stations draw from different letter
 * batches so players do not repeat the same words across nearby chapters (especially Ch5↔Ch6
 * and Ch1↔Ch7).
 */
object Season2DragMissingLetterWordPools {
    const val MIN_WORD_COUNT: Int = 6

    private const val MISSING_INDEX: Int = 0

    /** Drag-missing word catalog ids for the given S2 chapter index (1–7). */
    fun wordCatalogIds(chapterIndex: Int): List<String> =
        when (chapterIndex) {
            1 -> fromChapterWords(Season2ChapterContent.ch1Words)
            2 -> fromLetters(listOf("ח", "ר", "ק", "ש"))
            3 -> fromLetters(listOf("ג", "נ", "פ", "צ"))
            4 -> fromLetters(Season2ChapterContent.ch4Letters)
            5 -> fromLetters(listOf("ס", "ז", "נ"))
            6 -> fromLetters(listOf("ח", "ר", "ק", "ש", "פ", "צ"))
            7 -> seasonFinalePool()
            else -> error("Unsupported Season 2 chapterIndex=$chapterIndex")
        }

    private fun fromChapterWords(wordIds: List<String>): List<String> =
        wordIds
            .distinct()
            .filter { isEligible(it) }

    private fun fromLetters(letters: List<String>): List<String> {
        val letterSet = letters.toSet()
        return LessonWordCatalog.entries
            .asSequence()
            .filter { it.letter in letterSet }
            .map { it.id }
            .filter { isEligible(it) }
            .distinct()
            .toList()
    }

    /**
     * Ch7 finale review — union words minus Ch1 and Ch6 drag pools so the season opener
     * and late consolidation stations do not repeat in the finale drag station.
     */
    private fun seasonFinalePool(): List<String> {
        val exclude = wordCatalogIds(1).toSet() + wordCatalogIds(6).toSet()
        return Season2ChapterContent.season2UnionWordCatalogIds
            .filter { it !in exclude }
            .filter { isEligible(it) }
            .distinct()
    }

    private fun isEligible(catalogId: String): Boolean =
        DragStationGenerators.isValidForDragMissingLetter(catalogId, missingIndex = MISSING_INDEX) &&
            Season2StationContentValidator.wordAssetCheck(catalogId)?.isValid == true
}
