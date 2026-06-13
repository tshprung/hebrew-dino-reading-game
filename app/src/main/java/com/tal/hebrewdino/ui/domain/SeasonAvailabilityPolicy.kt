package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.BuildConfig

/** Which seasons are open in the player-facing season picker (and parent tools). */
object SeasonAvailabilityPolicy {
    fun isSeasonEnabled(seasonId: Int): Boolean =
        when (seasonId) {
            1 -> true
            2 -> BuildConfig.DEBUG
            else -> false
        }

    fun isSeason2Enabled(): Boolean = isSeasonEnabled(seasonId = 2)
}
