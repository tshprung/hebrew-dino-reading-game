package com.tal.hebrewdino.ui.domain

/**
 * Season 2 Chapter 1 (T-Rex) — manual poster-tile reveal order.
 *
 * Poster grid (3×2, LTR, top-left = tile 1):
 * ```
 *  1 | 2 | 3
 *  4 | 5 | 6
 * ```
 * Tile 1 (top-left) contains the face/head and must reveal last (station 6).
 */
/** @see Season2StandardRevealOrder */
object Season2Chapter1RevealOrder {
    const val STATION_COUNT: Int = Season2StandardRevealOrder.STATION_COUNT
    const val HEAD_POSTER_TILE: Int = Season2StandardRevealOrder.HEAD_POSTER_TILE

    fun posterTileForStation(stationId: Int): Int = Season2StandardRevealOrder.posterTileForStation(stationId)

    fun stationForPosterTile(posterTile: Int): Int = Season2StandardRevealOrder.stationForPosterTile(posterTile)

    fun revealedPosterTiles(completedStationIds: Set<Int>): Set<Int> =
        Season2StandardRevealOrder.revealedPosterTiles(completedStationIds)

    fun nextStation(completedStationIds: Set<Int>): Int? = Season2StandardRevealOrder.nextStation(completedStationIds)

    fun nextPlayablePosterTile(completedStationIds: Set<Int>): Int? =
        nextStation(completedStationIds)?.let { posterTileForStation(it) }
}
