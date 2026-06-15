package com.tal.hebrewdino.ui.domain

/**
 * Fixed Episode 3 copy tied to [Chapter3StationOrder] question counts (stations 1–3).
 * Keeps [LevelSession] index-aligned without new [Question] fields.
 *
 * Picture / match / image-to-word stations each prefer a **distinct** word subset so learners
 * do not see the same six words recycled across every station in the chapter.
 */
object Chapter3EpisodeContent {
    const val STATION_4_ROUND_COUNT: Int = 9

    /** St1 — באיזו אות מתחילה המילה? */
    val pictureStartsWithCatalogIds: Set<String> =
        setOf(
            "w_ג_3", // גדר
            "w_ד_2", // דלת
            "w_ל_1", // לחם
            "w_ה_1", // הר
            "w_ו_1", // ורד
            "w_ב_1", // בית
            "w_מ_2", // מחבת
            "w_ר_1", // ראש
        )

    /** St2 — התאימו אות למילה. */
    val matchLetterCatalogIds: Set<String> =
        setOf(
            "w_ד_1", // דג
            "w_ל_2", // לב
            "w_ג_1", // גמל
            "w_ד_4", // דבש
            "w_ש_1", // שמש
            "w_ר_3", // רגל
            "w_ה_2", // הפתעה
            "w_ו_3", // וילון
        )

    /** St6 — איזו מילה מתאימה לתמונה? */
    val imageToWordCatalogIds: Set<String> =
        setOf(
            "w_א_5", // ארנב
            "w_ב_3", // ברווז
            "w_ג_2", // גלידה
            "w_ד_3", // דחליל
            "w_ל_3", // לימון
            "w_מ_3", // מדוזה
            "w_ר_4", // רכבת
            "w_ש_2", // שולחן
        )

    /** Union of station-specific pools (legacy callers / diagnostics). */
    val episodeCatalogIds: Set<String> =
        pictureStartsWithCatalogIds + matchLetterCatalogIds + imageToWordCatalogIds

    /** Station X: (hint word shown in UI, target letter that appears in that word and in the grid). */
    private val gridHintAndTarget: List<Pair<String, String>> =
        listOf(
            "גדר" to "ג",
            "דלת" to "ד",
            "ורד" to "ו",
            "לחם" to "ל",
            "בית" to "ב",
            "ראש" to "ר",
            "גלידה" to "ג",
        )

    private data class BalloonRound(
        val word: String,
        val catalogId: String,
    )

    /** Station 3: word shown in UI + catalog id; letters are popped sequentially by occurrences in the word. */
    private val balloonRounds: List<BalloonRound> =
        listOf(
            BalloonRound("גדר", "w_ג_3"),
            BalloonRound("דלת", "w_ד_2"),
            BalloonRound("ורד", "w_ו_1"),
            BalloonRound("לחם", "w_ל_1"),
            BalloonRound("בית", "w_ב_1"),
            BalloonRound("ראש", "w_ר_1"),
        )

    /** Station 3: available words for the balloons station (word to catalogId). */
    fun balloonWordCatalogPairs(): List<Pair<String, String>> =
        balloonRounds.map { it.word to it.catalogId }

    data class SpellRound(
        val word: String,
        val catalogId: String,
        /** 0-based index of the slot the learner is filling this round. */
        val slotIndex: Int,
        val correctLetter: String,
    )

    /** St4 — highlighted letter in word: three fresh words (not the st1/st2 core set). */
    private val spellRounds: List<SpellRound> =
        listOf(
            SpellRound("ורד", "w_ו_1", 0, "ו"),
            SpellRound("ורד", "w_ו_1", 1, "ר"),
            SpellRound("ורד", "w_ו_1", 2, "ד"),
            SpellRound("בית", "w_ב_1", 0, "ב"),
            SpellRound("בית", "w_ב_1", 1, "י"),
            SpellRound("בית", "w_ב_1", 2, "ת"),
            SpellRound("ראש", "w_ר_1", 0, "ר"),
            SpellRound("ראש", "w_ר_1", 1, "א"),
            SpellRound("ראש", "w_ר_1", 2, "ש"),
        )

    fun gridHintWord(questionIndex: Int): String = gridHintAndTarget[questionIndex.coerceIn(0, gridHintAndTarget.lastIndex)].first

    fun gridTargetLetter(questionIndex: Int): String = gridHintAndTarget[questionIndex.coerceIn(0, gridHintAndTarget.lastIndex)].second

    fun balloonWord(questionIndex: Int): String = balloonRounds[questionIndex.coerceIn(0, balloonRounds.lastIndex)].word

    fun balloonFirstLetter(questionIndex: Int): String = balloonWord(questionIndex).first().toString()

    fun pickSpellRound(questionIndex: Int): SpellRound = spellRounds[questionIndex.coerceIn(0, spellRounds.lastIndex)]

    private data class FindAnyInWordRound(
        val word: String,
        val catalogId: String,
    )

    /** Station 3: one round per word; any letter from the word is accepted as correct (handled in UI). */
    private val findAnyInWordRounds: List<FindAnyInWordRound> =
        listOf(
            FindAnyInWordRound("גלידה", "w_ג_2"),
            FindAnyInWordRound("ארנב", "w_א_5"),
            FindAnyInWordRound("ברווז", "w_ב_3"),
        )

    fun findAnyInWord(questionIndex: Int): Pair<String, String> {
        val r = findAnyInWordRounds[questionIndex.coerceIn(0, findAnyInWordRounds.lastIndex)]
        return r.word to r.catalogId
    }
}
