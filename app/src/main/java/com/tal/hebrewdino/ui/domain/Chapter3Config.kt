package com.tal.hebrewdino.ui.domain

object Chapter3Config {
    const val STATION_COUNT: Int = 6
    /** All journey stations are playable; keep in sync with [STATION_COUNT]. */
    const val MAX_PLAYABLE_STATION: Int = STATION_COUNT
    val letters: List<String> = listOf("ק", "ט", "נ", "ה", "ר")
}
