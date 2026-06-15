package com.tal.hebrewdino.ui.game

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DragWordToPictureInteractionTest {
    @Test
    fun game_doesNotReplayWordOnTapOrDragStart() {
        val source = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/DragWordToPictureGame.kt")
        assertFalse(source.contains("onWordSelected"))
    }

    @Test
    fun game_allowsPictureReplayAfterLocked() {
        val source = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/DragWordToPictureGame.kt")
        assertTrue(source.contains("enabled = enabled && onPictureTapReplayWord != null"))
        assertTrue(source.contains("onPictureTapReplayWord?.invoke(pair.catalogEntryId)"))
    }

    @Test
    fun game_wordChipMatchesDropSlotDimensions() {
        val source = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/DragWordToPictureGame.kt")
        assertTrue(source.contains("width = pictureCardWidth"))
        assertTrue(source.contains("height = wordChipHeight"))
        assertTrue(source.contains("LockedWordChip("))
        assertTrue(source.contains("WordBankChip("))
    }

    private fun readProjectSource(relativePath: String): String {
        val candidates =
            listOf(
                java.io.File(relativePath),
                java.io.File("../$relativePath"),
                java.io.File("../../$relativePath"),
            )
        return candidates.first { it.exists() }.readText()
    }
}
