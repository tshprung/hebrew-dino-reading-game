package com.tal.hebrewdino.ui.domain

/**
 * One journey station runs exactly one quiz interaction pattern (possibly 2–3 rounds of it).
 * Never mix patterns within the same station session.
 */
enum class StationQuizMode {
    TapChoice,
    PopBalloons,
    RevealTiles,
    PictureLetterMatch,
}
