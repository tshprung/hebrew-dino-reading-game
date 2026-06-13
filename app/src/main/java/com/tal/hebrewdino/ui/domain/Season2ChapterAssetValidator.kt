package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips

/** Pure asset readiness checks for Season 2 chapters (unit-testable). */
object Season2ChapterAssetValidator {
    val TREX_POSTER_RES: Int = R.drawable.season2_trex_puzzle_full

    data class ValidationResult(
        val qaReady: Boolean,
        val missingAssets: List<String>,
    )

    fun validate(
        posterResId: Int?,
        letters: List<String>,
        wordCatalogIds: List<String>,
        forbidTrexPoster: Boolean,
    ): ValidationResult {
        val missing = mutableListOf<String>()

        if (posterResId == null) {
            missing.add("poster_puzzle_full.png (chapter-specific)")
        } else if (forbidTrexPoster && posterResId == TREX_POSTER_RES) {
            missing.add("unique poster (must not reuse season2_trex_puzzle_full)")
        }

        for (letter in letters.distinct()) {
            if (AudioClips.letterNameRawResId(letter) == null) {
                missing.add("letter_name_${letterHebrewFileStem(letter)}.mp3")
            }
        }

        for (catalogId in wordCatalogIds.distinct()) {
            val entry = LessonWordCatalog.entries.find { it.id == catalogId }
            if (entry == null) {
                missing.add("LessonWordCatalog entry $catalogId")
                continue
            }
            if (entry.tileRes == R.drawable.lesson_pic_placeholder) {
                missing.add("lesson_pic_${entry.word}.png")
            }
            if (AudioClips.wordRawResIdByCatalogId(catalogId) == null) {
                missing.add("word audio for $catalogId (${entry.word})")
            }
        }

        val minWordsPerLetter = letters.distinct().associateWith { letter ->
            wordCatalogIds.count { id ->
                LessonWordCatalog.entries.find { it.id == id }?.letter == letter
            }
        }
        for ((letter, count) in minWordsPerLetter) {
            if (count < 2) {
                missing.add("at least 2 catalog words with image+audio for letter '$letter' (found $count)")
            }
        }

        return ValidationResult(
            qaReady = missing.isEmpty() && posterResId != null,
            missingAssets = missing,
        )
    }

    private fun letterHebrewFileStem(letter: String): String =
        when (letter) {
            "ח" -> "chet"
            "ר" -> "reish"
            "ק" -> "kuf"
            "ש" -> "shin"
            "ז" -> "zayin"
            "י" -> "yod"
            "ס" -> "samech"
            "ע" -> "ayin"
            "מ" -> "mem"
            "ל" -> "lamed"
            "ג" -> "gimel"
            "נ" -> "nun"
            "פ" -> "peh"
            "צ" -> "tsadi"
            "ב" -> "bet"
            "ד" -> "dalet"
            "ת" -> "tav"
            "כ" -> "kaf"
            "ה" -> "heh"
            "ו" -> "vav"
            "ט" -> "tet"
            else -> letter
        }
}
