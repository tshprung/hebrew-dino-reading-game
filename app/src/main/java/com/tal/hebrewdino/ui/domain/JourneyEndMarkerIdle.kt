package com.tal.hebrewdino.ui.domain

/**
 * Pure helpers for dino idle position / auto-walk when the chapter is fully complete and an end marker exists.
 * Used by [com.tal.hebrewdino.ui.screens.JourneyScreen]; covered by unit tests so finale replay bugs stay fixed.
 */
object JourneyEndMarkerIdle {
    /**
     * When there is no next incomplete station ([nextPlayableSuggested] > [playableLevels]), dino should either
     * stand at the last station (before first end-walk) or at the end marker once [endMarkerReached] is true.
     */
    fun idleProgressAfterAllPlayableStationsComplete(
        canWalkToEndMarker: Boolean,
        endMarkerReached: Boolean,
        baseMaxDinoF: Float,
        maxDinoF: Float,
    ): Float =
        if (canWalkToEndMarker && endMarkerReached) {
            maxDinoF
        } else {
            baseMaxDinoF.coerceIn(0f, maxDinoF)
        }

    /** After the end marker was reached once, do not schedule the “walk to egg” auto-walk again on re-entry. */
    fun suppressRepeatEndMarkerAutoWalk(endMarkerReached: Boolean, canWalkToEndMarker: Boolean): Boolean =
        endMarkerReached && canWalkToEndMarker
}
