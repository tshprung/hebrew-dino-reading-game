package com.tal.hebrewdino.ui.domain

/** Clickability rules for Season 2 puzzle-map poster tiles. */
object Season2PuzzleMapTileClickPolicy {
  fun isTileClickable(
      posterTile: Int,
      revealedTiles: Set<Int>,
      nextPlayablePosterTile: Int?,
      chapterFullyRevealed: Boolean,
      isRevealing: Boolean,
  ): Boolean {
      if (isRevealing) return false
      if (chapterFullyRevealed) return posterTile in revealedTiles
      return posterTile == nextPlayablePosterTile
  }

  fun isNextTileHighlighted(
      posterTile: Int,
      nextPlayablePosterTile: Int?,
      chapterFullyRevealed: Boolean,
      isClickable: Boolean,
  ): Boolean =
      isClickable && !chapterFullyRevealed && posterTile == nextPlayablePosterTile
}
