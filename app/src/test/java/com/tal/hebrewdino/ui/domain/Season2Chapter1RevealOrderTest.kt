package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2Chapter1RevealOrderTest {
    @Test
    fun station6_revealsHeadTileLast() {
        assertEquals(1, Season2Chapter1RevealOrder.posterTileForStation(6))
        assertEquals(6, Season2Chapter1RevealOrder.stationForPosterTile(1))
    }

    @Test
    fun station1_doesNotRevealHead() {
        assertEquals(6, Season2Chapter1RevealOrder.posterTileForStation(1))
        assertTrue(Season2Chapter1RevealOrder.posterTileForStation(1) != Season2Chapter1RevealOrder.HEAD_POSTER_TILE)
    }

    @Test
    fun revealedTiles_followCompletedStations() {
        val tiles = Season2Chapter1RevealOrder.revealedPosterTiles(setOf(1, 2, 3))
        assertEquals(setOf(6, 4, 5), tiles)
        assertTrue(Season2Chapter1RevealOrder.HEAD_POSTER_TILE !in tiles)
    }

    @Test
    fun nextStation_isFirstIncomplete() {
        assertEquals(3, Season2Chapter1RevealOrder.nextStation(setOf(1, 2)))
        assertNull(Season2Chapter1RevealOrder.nextStation(setOf(1, 2, 3, 4, 5, 6)))
    }

    @Test
    fun nextPlayablePosterTile_matchesNextStation() {
        assertEquals(5, Season2Chapter1RevealOrder.nextPlayablePosterTile(setOf(1, 2)))
    }
}
