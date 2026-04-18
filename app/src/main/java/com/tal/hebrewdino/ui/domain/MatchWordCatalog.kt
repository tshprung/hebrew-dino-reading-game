package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.R
import kotlin.random.Random

/**
 * Placeholder art + Hebrew word whose first print letter matches [letter].
 * Image is secondary until real art exists; the word is the primary teaching cue.
 */
data class MatchWordEntry(
    val imageRes: Int,
    val word: String,
    val letter: String,
)

object MatchWordCatalog {
    private val entries: List<MatchWordEntry> =
        listOf(
            MatchWordEntry(R.drawable.egg_found, "ביצה", "ב"),
            MatchWordEntry(R.drawable.stop_marker, "בית", "ב"),
            MatchWordEntry(R.drawable.finish_marker_egg, "במבה", "ב"),
            MatchWordEntry(R.drawable.egg_found, "דג", "ד"),
            MatchWordEntry(R.drawable.stop_marker, "דלת", "ד"),
            MatchWordEntry(R.drawable.finish_marker_egg, "דחליל", "ד"),
            MatchWordEntry(R.drawable.egg_found, "מדוזה", "מ"),
            MatchWordEntry(R.drawable.stop_marker, "מחבת", "מ"),
            MatchWordEntry(R.drawable.finish_marker_egg, "מכנסיים", "מ"),
            MatchWordEntry(R.drawable.egg_found, "מכונית", "מ"),
            MatchWordEntry(R.drawable.stop_marker, "אבטיח", "א"),
            MatchWordEntry(R.drawable.finish_marker_egg, "אריה", "א"),
            MatchWordEntry(R.drawable.egg_found, "למידה", "ל"),
            MatchWordEntry(R.drawable.stop_marker, "לב", "ל"),
            MatchWordEntry(R.drawable.finish_marker_egg, "לחם", "ל"),
            MatchWordEntry(R.drawable.egg_found, "קוף", "ק"),
            MatchWordEntry(R.drawable.stop_marker, "קניה", "ק"),
            MatchWordEntry(R.drawable.finish_marker_egg, "קוביה", "ק"),
            MatchWordEntry(R.drawable.egg_found, "טבע", "ט"),
            MatchWordEntry(R.drawable.stop_marker, "טוסט", "ט"),
            MatchWordEntry(R.drawable.finish_marker_egg, "טיגר", "ט"),
            MatchWordEntry(R.drawable.egg_found, "נמל", "נ"),
            MatchWordEntry(R.drawable.stop_marker, "נר", "נ"),
            MatchWordEntry(R.drawable.finish_marker_egg, "נעליים", "נ"),
            MatchWordEntry(R.drawable.egg_found, "הר", "ה"),
            MatchWordEntry(R.drawable.stop_marker, "הליכון", "ה"),
            MatchWordEntry(R.drawable.finish_marker_egg, "הפתעה", "ה"),
            MatchWordEntry(R.drawable.egg_found, "רכב", "ר"),
            MatchWordEntry(R.drawable.stop_marker, "ראש", "ר"),
            MatchWordEntry(R.drawable.finish_marker_egg, "רעש", "ר"),
            MatchWordEntry(R.drawable.egg_found, "שמש", "ש"),
            MatchWordEntry(R.drawable.stop_marker, "שולחן", "ש"),
            MatchWordEntry(R.drawable.finish_marker_egg, "שוקולד", "ש"),
            MatchWordEntry(R.drawable.egg_found, "תפוח", "ת"),
            MatchWordEntry(R.drawable.stop_marker, "תינוק", "ת"),
            MatchWordEntry(R.drawable.finish_marker_egg, "תיק", "ת"),
            MatchWordEntry(R.drawable.egg_found, "יום", "י"),
            MatchWordEntry(R.drawable.stop_marker, "ילד", "י"),
            MatchWordEntry(R.drawable.finish_marker_egg, "כיסא", "כ"),
            MatchWordEntry(R.drawable.egg_found, "כלב", "כ"),
        )

    fun pickForLetter(rnd: Random, letter: String, excludeImageRes: Int?): MatchWordEntry {
        val pool = entries.filter { it.letter == letter && (excludeImageRes == null || it.imageRes != excludeImageRes) }
        val use =
            if (pool.isNotEmpty()) {
                pool.random(rnd)
            } else {
                entries.filter { it.letter == letter }.randomOrNull(rnd)
                    ?: MatchWordEntry(R.drawable.egg_found, letter, letter)
            }
        return use
    }
}
