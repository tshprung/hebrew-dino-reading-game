package com.tal.hebrewdino.ui.domain

/**
 * Fixed Episode 3 copy tied to [Chapter3StationOrder] question counts (stations 1–3).
 * Keeps [LevelSession] index-aligned without new [Question] fields.
 */
object Chapter3EpisodeContent {
    /** Catalog ids for picture / image-match bias (subset of [LessonWordCatalog]). */
    val episodeCatalogIds: Set<String> =
        setOf(
            "w_ד_1", // דג
            "w_ל_2", // לב
            "w_ג_1", // גמל
            "w_ש_1", // שמש
            "w_ד_4", // דבש
            "w_ר_3", // רגל
        )

    /** Station X: (hint word shown in UI, target letter that appears in that word and in the grid). */
    private val gridHintAndTarget: List<Pair<String, String>> =
        listOf(
            "גמל" to "מ",
            "דג" to "ג",
            "שמש" to "ש",
            "לב" to "ב",
            "גמל" to "ל",
            "רגל" to "ג",
            "דבש" to "ש",
        )

    private data class BalloonRound(
        val word: String,
        val catalogId: String,
    )

    /** Station 3: word shown in UI + catalog id; letters are popped sequentially by occurrences in the word. */
    private val balloonRounds: List<BalloonRound> =
        listOf(
            BalloonRound("גמל", "w_ג_1"),
            BalloonRound("דג", "w_ד_1"),
            BalloonRound("שמש", "w_ש_1"),
            BalloonRound("דבש", "w_ד_4"),
            BalloonRound("לב", "w_ל_2"),
            BalloonRound("רגל", "w_ר_3"),
        )

    data class SpellRound(
        val word: String,
        val catalogId: String,
        /** 0-based index of the slot the learner is filling this round. */
        val slotIndex: Int,
        val correctLetter: String,
    )

    /** Station 2: sequential rounds per word (one highlighted letter at a time). */
    private val spellRounds: List<SpellRound> =
        listOf(
            SpellRound("גמל", "w_ג_1", 0, "ג"),
            SpellRound("גמל", "w_ג_1", 1, "מ"),
            SpellRound("גמל", "w_ג_1", 2, "ל"),
            SpellRound("דבש", "w_ד_4", 0, "ד"),
            SpellRound("דבש", "w_ד_4", 1, "ב"),
            SpellRound("דבש", "w_ד_4", 2, "ש"),
            SpellRound("שמש", "w_ש_1", 0, "ש"),
            SpellRound("שמש", "w_ש_1", 1, "מ"),
            SpellRound("שמש", "w_ש_1", 2, "ש"),
        )

    fun gridHintWord(questionIndex: Int): String = gridHintAndTarget[questionIndex.coerceIn(0, gridHintAndTarget.lastIndex)].first

    fun gridTargetLetter(questionIndex: Int): String = gridHintAndTarget[questionIndex.coerceIn(0, gridHintAndTarget.lastIndex)].second

    fun balloonWord(questionIndex: Int): String = balloonRounds[questionIndex.coerceIn(0, balloonRounds.lastIndex)].word

    fun balloonCatalogId(questionIndex: Int): String = balloonRounds[questionIndex.coerceIn(0, balloonRounds.lastIndex)].catalogId

    fun balloonFirstLetter(questionIndex: Int): String = balloonWord(questionIndex).first().toString()

    data class PopAllLettersRound(
        val word: String,
        val catalogId: String,
        val correctLetter: String,
    )

    /** Station 3: 3 words; each word contributes one round per letter occurrence. */
    private val popAllLettersRounds: List<PopAllLettersRound> =
        listOf(
            // גמל
            PopAllLettersRound("גמל", "w_ג_1", "ג"),
            PopAllLettersRound("גמל", "w_ג_1", "מ"),
            PopAllLettersRound("גמל", "w_ג_1", "ל"),
            // דבש
            PopAllLettersRound("דבש", "w_ד_4", "ד"),
            PopAllLettersRound("דבש", "w_ד_4", "ב"),
            PopAllLettersRound("דבש", "w_ד_4", "ש"),
            // שמש (ש appears twice → two separate rounds)
            PopAllLettersRound("שמש", "w_ש_1", "ש"),
            PopAllLettersRound("שמש", "w_ש_1", "מ"),
            PopAllLettersRound("שמש", "w_ש_1", "ש"),
        )

    fun popAllLettersRound(questionIndex: Int): PopAllLettersRound =
        popAllLettersRounds[questionIndex.coerceIn(0, popAllLettersRounds.lastIndex)]

    fun pickSpellRound(questionIndex: Int): SpellRound = spellRounds[questionIndex.coerceIn(0, spellRounds.lastIndex)]

    private data class FindAnyInWordRound(
        val word: String,
        val catalogId: String,
    )

    /** Station 3: one round per word; any letter from the word is accepted as correct (handled in UI). */
    private val findAnyInWordRounds: List<FindAnyInWordRound> =
        listOf(
            FindAnyInWordRound("גמל", "w_ג_1"),
            FindAnyInWordRound("דבש", "w_ד_4"),
            FindAnyInWordRound("שמש", "w_ש_1"),
        )

    fun findAnyInWord(questionIndex: Int): Pair<String, String> {
        val r = findAnyInWordRounds[questionIndex.coerceIn(0, findAnyInWordRounds.lastIndex)]
        return r.word to r.catalogId
    }

    /** True when this round is the last letter of its word (praise plays after a correct tap). */
    fun spellCompletesWordAfterCorrectRound(questionIndex: Int): Boolean {
        val r = pickSpellRound(questionIndex)
        return r.slotIndex == r.word.lastIndex
    }

    /** UI: revealed letters before the current slot (same [word] as this round). */
    fun spellProgressLabel(round: SpellRound): String =
        buildString {
            round.word.forEachIndexed { i, ch ->
                when {
                    i < round.slotIndex -> append(ch)
                    else -> append('━')
                }
            }
        }
}
