package com.tal.hebrewdino.ui.components.learning

import com.tal.hebrewdino.R

data class StartsWithPrompt(
    val imageRes: Int,
    val caption: String,
    val startingLetter: String,
)

/**
 * Placeholder words mapped to their first Hebrew print letter.
 * Filter by the active chapter letter list before use.
 */
object ChapterStartsWithPrompts {
    val all: List<StartsWithPrompt> =
        listOf(
            StartsWithPrompt(R.drawable.egg_found, "ביצה", "ב"),
            StartsWithPrompt(R.drawable.finish_marker_egg, "מסע", "מ"),
            StartsWithPrompt(R.drawable.stop_marker, "לב", "ל"),
            StartsWithPrompt(R.drawable.egg_found, "קוף", "ק"),
            StartsWithPrompt(R.drawable.stop_marker, "טוסט", "ט"),
            StartsWithPrompt(R.drawable.finish_marker_egg, "נמלה", "נ"),
            StartsWithPrompt(R.drawable.egg_found, "הר", "ה"),
            StartsWithPrompt(R.drawable.stop_marker, "רכב", "ר"),
        )
}
