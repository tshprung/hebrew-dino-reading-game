package com.tal.hebrewdino.ui.domain

/**
 * Shared fractional positions for chapter nodes on the journey / chapters map
 * so the dinosaur and station chips sit on the same path.
 */
object JourneyMapLayout {
    val stationFractions: List<Pair<Float, Float>> =
        listOf(
            0.92f to 0.58f,
            0.76f to 0.42f,
            0.60f to 0.60f,
            0.44f to 0.44f,
            0.28f to 0.62f,
            0.16f to 0.46f,
        )
}
