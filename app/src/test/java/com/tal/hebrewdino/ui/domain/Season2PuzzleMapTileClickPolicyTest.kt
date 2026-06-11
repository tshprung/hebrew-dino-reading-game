package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2PuzzleMapTileClickPolicyTest {
    @Test
    fun inProgress_onlyNextUnresolvedTileIsClickable() {
        val completed = setOf(1, 2)
        val revealed = Season2Chapter1RevealOrder.revealedPosterTiles(completed)
        val nextTile = Season2Chapter1RevealOrder.nextPlayablePosterTile(completed)!!

        for (tile in 1..6) {
            val clickable =
                Season2PuzzleMapTileClickPolicy.isTileClickable(
                    posterTile = tile,
                    revealedTiles = revealed,
                    nextPlayablePosterTile = nextTile,
                    chapterFullyRevealed = false,
                    isRevealing = false,
                )
            if (tile == nextTile) {
                assertTrue("tile $tile should be next playable", clickable)
            } else {
                assertFalse("tile $tile should be locked in progress", clickable)
            }
        }
        assertEquals(3, Season2Chapter1RevealOrder.nextStation(completed))
        assertEquals(nextTile, Season2Chapter1RevealOrder.posterTileForStation(3))
    }

    @Test
    fun inProgress_solvedTilesStayDisabledUntilChapterComplete() {
        val completed = setOf(1)
        val revealed = Season2Chapter1RevealOrder.revealedPosterTiles(completed)
        val solvedTile = Season2Chapter1RevealOrder.posterTileForStation(1)
        val nextTile = Season2Chapter1RevealOrder.nextPlayablePosterTile(completed)!!

        assertTrue(solvedTile in revealed)
        assertFalse(
            Season2PuzzleMapTileClickPolicy.isTileClickable(
                posterTile = solvedTile,
                revealedTiles = revealed,
                nextPlayablePosterTile = nextTile,
                chapterFullyRevealed = false,
                isRevealing = false,
            ),
        )
    }

    @Test
    fun inProgress_futureTilesDisabled() {
        val completed = emptySet<Int>()
        val nextTile = Season2Chapter1RevealOrder.nextPlayablePosterTile(completed)!!

        for (tile in 1..6) {
            val clickable =
                Season2PuzzleMapTileClickPolicy.isTileClickable(
                    posterTile = tile,
                    revealedTiles = emptySet(),
                    nextPlayablePosterTile = nextTile,
                    chapterFullyRevealed = false,
                    isRevealing = false,
                )
            assertEquals(tile == nextTile, clickable)
        }
    }

    @Test
    fun completedChapter_allRevealedTilesReplayable() {
        val completed = (1..6).toSet()
        val revealed = Season2Chapter1RevealOrder.revealedPosterTiles(completed)

        for (tile in 1..6) {
            assertTrue(
                Season2PuzzleMapTileClickPolicy.isTileClickable(
                    posterTile = tile,
                    revealedTiles = revealed,
                    nextPlayablePosterTile = null,
                    chapterFullyRevealed = true,
                    isRevealing = false,
                ),
            )
        }
    }

    @Test
    fun nextHighlightedTile_matchesStationProgression() {
        val completed = setOf(1, 2, 3)
        val nextTile = Season2Chapter1RevealOrder.nextPlayablePosterTile(completed)!!
        assertEquals(
            Season2Chapter1RevealOrder.posterTileForStation(4),
            nextTile,
        )
        assertTrue(
            Season2PuzzleMapTileClickPolicy.isNextTileHighlighted(
                posterTile = nextTile,
                nextPlayablePosterTile = nextTile,
                chapterFullyRevealed = false,
                isClickable = true,
            ),
        )
        assertFalse(
            Season2PuzzleMapTileClickPolicy.isNextTileHighlighted(
                posterTile = 1,
                nextPlayablePosterTile = nextTile,
                chapterFullyRevealed = false,
                isClickable = false,
            ),
        )
    }

    @Test
    fun revealingBlocksAllClicks() {
        assertFalse(
            Season2PuzzleMapTileClickPolicy.isTileClickable(
                posterTile = 6,
                revealedTiles = emptySet(),
                nextPlayablePosterTile = 6,
                chapterFullyRevealed = false,
                isRevealing = true,
            ),
        )
    }

    @Test
    fun puzzleMapScreen_usesClickPolicy() {
        val source = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/Season2PuzzleMapPrototypeScreen.kt")
        assertTrue(source.contains("Season2PuzzleMapTileClickPolicy.isTileClickable"))
        assertFalse(source.contains("isRevealed ||\n                            isNext"))
    }

    @Test
    fun season1_unchanged() {
        val source = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/Season2PuzzleMapPrototypeScreen.kt")
        assertFalse(source.contains("Chapter1StationOrder"))
        assertFalse(source.contains("GameScreen"))
    }

    private fun readProjectSource(relativePath: String): String {
        val candidates =
            listOf(
                java.io.File(relativePath),
                java.io.File("../$relativePath"),
                java.io.File("../../$relativePath"),
            )
        val file =
            candidates.firstOrNull { it.exists() }
                ?: error("Could not locate source file: $relativePath")
        return file.readText()
    }
}
