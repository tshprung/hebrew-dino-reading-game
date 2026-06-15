package com.tal.hebrewdino.ui.domain

/** Which seasons are open in the player-facing season picker (and parent tools). */
object SeasonAvailabilityPolicy {
    fun isSeasonEnabled(seasonId: Int): Boolean =
        when (seasonId) {
            1, 2 -> true
            else -> false
        }

    fun isSeason2Enabled(): Boolean = isSeasonEnabled(seasonId = 2)
}
