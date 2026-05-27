package com.tal.hebrewdino.ui.domain.cosmetics

/**
 * Surprise accessory unlocks tied to syllabus station completion (no shop / currency).
 */
object AccessoryProgression {
    fun accessoryIdForStationCompleted(
        chapterIndex: Int,
        stationId: Int,
    ): String? =
        when (chapterIndex) {
            0 ->
                when (stationId) {
                    1 -> AccessoryCatalog.hat.id
                    2 -> AccessoryCatalog.sunglasses.id
                    3 -> AccessoryCatalog.bowtie.id
                    else -> null
                }
            else -> null
        }
}
