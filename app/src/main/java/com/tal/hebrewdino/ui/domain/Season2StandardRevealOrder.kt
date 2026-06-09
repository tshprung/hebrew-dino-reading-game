package com.tal.hebrewdino.ui.domain

/**
 * Standard Season 2 poster grid reveal order (3×2, LTR).
 * Tile 1 (top-left) = face/head — reveals last on station 6.
 */
object Season2StandardRevealOrder {
    const val STATION_COUNT: Int = 6
    const val HEAD_POSTER_TILE: Int = 1

    private val stationToPosterTile: IntArray =
        intArrayOf(
            6, // st1 → bottom-right
            4, // st2 → bottom-left
            5, // st3 → bottom-middle
            3, // st4 → top-right
            2, // st5 → top-middle
            1, // st6 → top-left (head LAST)
        )

    fun posterTileForStation(stationId: Int): Int {
        require(stationId in 1..STATION_COUNT) { "stationId=$stationId" }
        return stationToPosterTile[stationId - 1]
    }

    fun stationForPosterTile(posterTile: Int): Int {
        require(posterTile in 1..STATION_COUNT) { "posterTile=$posterTile" }
        return stationToPosterTile.indexOfFirst { it == posterTile } + 1
    }

    fun revealedPosterTiles(completedStationIds: Set<Int>): Set<Int> =
        completedStationIds
            .filter { it in 1..STATION_COUNT }
            .map { posterTileForStation(it) }
            .toSet()

    fun nextStation(completedStationIds: Set<Int>): Int? =
        (1..STATION_COUNT).firstOrNull { it !in completedStationIds }
}
