package com.tal.hebrewdino.ui.domain

object LetterPool {
    val groups: List<List<String>> =
        listOf(
            // Start with audio-covered letters first (א/ב/מ/ל/ד) then expand gradually.
            listOf("א", "ב", "מ"),
            listOf("א", "ל", "מ"),
            listOf("ב", "ד", "מ"),
            listOf("ד", "ל", "מ"),
            listOf("א", "ב", "ל"),
            listOf("א", "ד", "ל"),

            // Expand beyond recorded letter clips (still playable; voice falls back).
            listOf("נ", "מ", "ל"),
            listOf("ר", "ל", "ד"),
            listOf("ש", "מ", "ב"),
            listOf("ת", "ל", "א"),
            listOf("י", "מ", "ל"),
            listOf("כ", "ב", "מ"),
        )
}

