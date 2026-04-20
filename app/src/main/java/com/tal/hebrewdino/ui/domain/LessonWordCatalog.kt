package com.tal.hebrewdino.ui.domain

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.tal.hebrewdino.R
import kotlin.random.Random

/**
 * Authoritative picture↔word↔letter rows for picture-first stations.
 * [letter] is the pedagogical first visible print letter taught for [word] (stored explicitly).
 *
 * [tintArgb] + [tileRes] drive card art; default [R.drawable.lesson_pic_placeholder] is replaced in UI by a word illustration.
 */
data class LessonWordEntry(
    val id: String,
    val letter: String,
    val word: String,
    @ColorInt val tintArgb: Int,
    @DrawableRes val tileRes: Int = R.drawable.lesson_pic_placeholder,
)

/**
 * Frozen catalog: validated once at class load (fail fast in debug if corrupted).
 */
object LessonWordCatalog {
    val entries: List<LessonWordEntry> =
        listOf(
            LessonWordEntry("w_א_1", "א", "אבא", 0xFFB3E5FC.toInt()),
            LessonWordEntry("w_א_2", "א", "אריה", 0xFFFFF9C4.toInt()),
            LessonWordEntry("w_א_3", "א", "אבטיח", 0xFFC8E6C9.toInt()),
            LessonWordEntry("w_ב_1", "ב", "בית", 0xFFE1BEE7.toInt(), tileRes = R.drawable.lesson_pic_bait),
            LessonWordEntry("w_ב_2", "ב", "בלון", 0xFFFFCDD2.toInt()),
            LessonWordEntry("w_ב_3", "ב", "ברווז", 0xFFB2EBF2.toInt()),
            LessonWordEntry("w_מ_1", "מ", "מכונית", 0xFFFFE0B2.toInt()),
            LessonWordEntry("w_מ_2", "מ", "מחבת", 0xFFDCEDC8.toInt()),
            LessonWordEntry("w_מ_3", "מ", "מדוזה", 0xFFC5CAE9.toInt(), tileRes = R.drawable.lesson_pic_medusa),
            LessonWordEntry("w_ל_1", "ל", "לחם", 0xFFFFE082.toInt()),
            LessonWordEntry("w_ל_2", "ל", "לב", 0xFFF8BBD0.toInt()),
            LessonWordEntry("w_ל_3", "ל", "למידה", 0xFFB2DFDB.toInt()),
            LessonWordEntry("w_ד_1", "ד", "דג", 0xFF81D4FA.toInt()),
            LessonWordEntry("w_ד_2", "ד", "דלת", 0xFFFFF59D.toInt()),
            LessonWordEntry("w_ד_3", "ד", "דחליל", 0xFFD1C4E9.toInt()),
            LessonWordEntry("w_נ_1", "נ", "נמל", 0xFF9FA8DA.toInt()),
            LessonWordEntry("w_נ_2", "נ", "נר", 0xFFFFCC80.toInt()),
            LessonWordEntry("w_נ_3", "נ", "נעליים", 0xFF80DEEA.toInt()),
            LessonWordEntry("w_ר_1", "ר", "ראש", 0xFFF48FB1.toInt()),
            LessonWordEntry("w_ר_2", "ר", "רכב", 0xFFA5D6A7.toInt()),
            LessonWordEntry("w_ר_3", "ר", "רעש", 0xFFFFC400.toInt()),
            LessonWordEntry("w_ש_1", "ש", "שמש", 0xFF90CAF9.toInt()),
            LessonWordEntry("w_ש_2", "ש", "שולחן", 0xFFCE93D8.toInt()),
            LessonWordEntry("w_ש_3", "ש", "שוקולד", 0xFFFFAB91.toInt()),
            LessonWordEntry("w_ת_1", "ת", "תפוח", 0xFF80CBC4.toInt()),
            LessonWordEntry("w_ת_2", "ת", "תיק", 0xFFFFE57E.toInt()),
            LessonWordEntry("w_ת_3", "ת", "תינוק", 0xFFB39DDB.toInt()),
            LessonWordEntry("w_י_1", "י", "יום", 0xFFFFF9A7.toInt()),
            LessonWordEntry("w_י_2", "י", "ילד", 0xFF64B5F6.toInt()),
            LessonWordEntry("w_כ_1", "כ", "כיסא", 0xFFFFD54F.toInt()),
            LessonWordEntry("w_כ_2", "כ", "כלב", 0xFFC5E1A5.toInt()),
            LessonWordEntry("w_כ_3", "כ", "כדור", 0xFFAED581.toInt()),
            LessonWordEntry("w_ק_1", "ק", "קוף", 0xFFFFAB40.toInt()),
            LessonWordEntry("w_ק_2", "ק", "קוביה", 0xFF4FC3F7.toInt()),
            LessonWordEntry("w_ק_3", "ק", "קנדי", 0xFFF06292.toInt()),
            LessonWordEntry("w_ט_1", "ט", "טוסט", 0xFF4DD0E1.toInt()),
            LessonWordEntry("w_ט_2", "ט", "טבע", 0xFF9575CD.toInt()),
            LessonWordEntry("w_ט_3", "ט", "טיגר", 0xFFFFB74D.toInt()),
            LessonWordEntry("w_ה_1", "ה", "הר", 0xFFA1887F.toInt()),
            LessonWordEntry("w_ה_2", "ה", "הפתעה", 0xFFFFF176.toInt()),
            LessonWordEntry("w_ה_3", "ה", "הליכון", 0xFFBCAAA4.toInt()),
        )

    init {
        validateCatalog(entries)
    }

    fun entriesForLetter(letter: String): List<LessonWordEntry> = entries.filter { it.letter == letter }

    fun pickRandom(rnd: Random, letter: String, excludeIds: Set<String> = emptySet()): LessonWordEntry {
        val pool = entries.filter { it.letter == letter && it.id !in excludeIds }
        require(pool.isNotEmpty()) { "No catalog entry for letter=$letter" }
        return pool.random(rnd)
    }

    /** Image-match: cycle through different example words for the same letter before repeating. */
    fun pickRandomForImageMatch(rnd: Random, letter: String, excludeIds: Set<String>): LessonWordEntry {
        val pool = entries.filter { it.letter == letter && it.id !in excludeIds }
        if (pool.isNotEmpty()) return pool.random(rnd)
        return pickRandom(rnd, letter)
    }

    fun pickOtherLetterWord(rnd: Random, forbiddenLetter: String, excludeIds: Set<String> = emptySet()): LessonWordEntry {
        val pool = entries.filter { it.letter != forbiddenLetter && it.id !in excludeIds }
        require(pool.isNotEmpty()) { "No distractor entries" }
        return pool.random(rnd)
    }

    fun pickTwoDistinctForLetters(rnd: Random, letter1: String, letter2: String): Pair<LessonWordEntry, LessonWordEntry> {
        require(letter1 != letter2)
        val e1 = pickRandom(rnd, letter1)
        var e2 = pickRandom(rnd, letter2)
        var guard = 0
        while (e1.tintArgb == e2.tintArgb && guard++ < 8) {
            e2 = pickRandom(rnd, letter2, excludeIds = setOf(e2.id))
        }
        return e1 to e2
    }

    /** Package-private for tests; throws if invalid. */
    internal fun validateCatalog(list: List<LessonWordEntry>) {
        val ids = mutableSetOf<String>()
        val tints = mutableSetOf<Int>()
        for (e in list) {
            check(ids.add(e.id)) { "Duplicate lesson id: ${e.id}" }
            check(tints.add(e.tintArgb)) { "Duplicate tint (visual collision): ${e.id} tint=${e.tintArgb}" }
            check(e.letter.isNotEmpty() && e.letter.length == 1) { "Bad letter for ${e.id}" }
            check(e.word.isNotEmpty()) { "Empty word for ${e.id}" }
            val first = e.word.first().toString()
            check(first == e.letter) { "Word first char mismatch: ${e.id} word=${e.word} letter=${e.letter} first=$first" }
        }
    }
}
