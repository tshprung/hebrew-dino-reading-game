package com.tal.hebrewdino.ui.domain

object Chapter3Config {
    const val STATION_COUNT: Int = 6
    /** Stations above this are not released yet (locked on map + clamped in prefs). */
    const val MAX_PLAYABLE_STATION: Int = 2
    val letters: List<String> = listOf("ק", "ט", "נ", "ה", "ר")
}
