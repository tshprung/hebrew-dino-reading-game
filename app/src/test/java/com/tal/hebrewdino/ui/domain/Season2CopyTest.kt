package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.ui.data.PlayerAddress
import com.tal.hebrewdino.ui.domain.Season2Chapter1RevealOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2CopyTest {
    @Test
    fun revealedDinosaurName_knownForQaReadyChapters() {
        assertEquals("טירנוזאורוס", Season2Copy.revealedDinosaurName(1))
        assertEquals("טריצרטופס", Season2Copy.revealedDinosaurName(2))
        assertEquals("סטגוזאורוס", Season2Copy.revealedDinosaurName(3))
        assertEquals("ברכיוזאורוס", Season2Copy.revealedDinosaurName(4))
    }

    @Test
    fun chapterSelectLabel_hidesIdentityBeforeComplete() {
        assertTrue(Season2Copy.chapterSelectLabel(1, completed = false).contains("מי מסתתר"))
        assertTrue(Season2Copy.chapterSelectLabel(1, completed = true).contains("טירנוזאורוס"))
        assertTrue(Season2Copy.chapterSelectLabel(2, completed = false).contains("מי מסתתר"))
        assertTrue(Season2Copy.chapterSelectLabel(2, completed = true).contains("טריצרטופס"))
        assertTrue(Season2Copy.chapterSelectLabel(3, completed = false).contains("מי מסתתר"))
    }

    @Test
    fun puzzleMapTitle_hidesIdentityBeforeComplete() {
        assertTrue(Season2Copy.puzzleMapTitle(1, chapterCompleted = false).contains("מפה מסתורית"))
        assertTrue(Season2Copy.puzzleMapTitle(1, chapterCompleted = true).contains("טירנוזאורוס"))
    }

    @Test
    fun mapIntroLines_usesAddressAwareAskLine() {
        val boyAsk = Season2Copy.mapIntroStoryLines(PlayerAddress.Boy)[3]
        val girlAsk = Season2Copy.mapIntroStoryLines(PlayerAddress.Girl)[3]
        assertTrue(boyAsk.contains("תעזור"))
        assertTrue(girlAsk.contains("תעזרי"))
    }

    @Test
    fun mapIntroLines_staysCompactWithoutNameReveal() {
        val lines = Season2Copy.mapIntroStoryLines(PlayerAddress.Boy)
        assertEquals(4, lines.size)
        assertTrue(lines[0].contains("המפה הראשונה"))
        assertFalse(lines.any { it.contains("טירנוזאורוס") })
        assertTrue(lines[3].contains("תעזור"))
    }

    @Test
    fun seasonIntroLines_tellsAdventureStory() {
        val lines = Season2Copy.seasonIntroStoryLines()
        assertEquals(4, lines.size)
        assertTrue(lines[0].contains("מפה עתיקה"))
        assertTrue(lines[1].contains("בכל חלק"))
        assertTrue(lines[2].contains("בכל פעם שתפתרו משימה"))
        assertTrue(lines[3].contains("מי מסתתר שם"))
    }

    @Test
    fun ch1MapIntroLines_matchNarration() {
        val lines = Season2Copy.ch1MapIntroStoryLines()
        assertEquals(2, lines.size)
        assertTrue(lines[0].contains("שיניים חדות"))
        assertTrue(lines[1].contains("בואו נגלה מי זה"))
    }

    @Test
    fun isChapterComplete_usesStationsWhenChapterFlagMissing() {
        val allStations = (1..Season2Chapter1RevealOrder.STATION_COUNT).toSet()
        assertTrue(
            Season2Copy.isChapterComplete(
                chapterIndex = 1,
                completedChapters = emptySet(),
                completedStations = allStations,
            ),
        )
        assertFalse(
            Season2Copy.isChapterComplete(
                chapterIndex = 1,
                completedChapters = emptySet(),
                completedStations = setOf(1, 2, 3),
            ),
        )
    }

    @Test
    fun chapterRevealedBadge_usesHebrewRevealedLabel() {
        assertEquals("התגלה!", Season2Copy.ChapterRevealedBadge)
    }

    @Test
    fun returnCaption_variesByProgress() {
        assertTrue(Season2Copy.returnCaptionAfterStation(2)!!.contains("עוד חלק"))
        assertTrue(Season2Copy.returnCaptionAfterStation(5)!!.contains("מתקרבים"))
        assertNull(Season2Copy.returnCaptionAfterStation(6))
    }

    @Test
    fun completionHeadline_revealsIdentity() {
        assertTrue(Season2Copy.completionHeadline(1).contains("טירנוזאורוס"))
    }

    @Test
    fun replayTileInstruction_usesSquareWording() {
        assertTrue(Season2Copy.replayTileInstruction().contains("ריבוע"))
        assertFalse(Season2Copy.replayTileInstruction().contains("כוכב"))
    }

    @Test
    fun returnCaptionVoiceRawRes_legacyStillResolvable() {
        assertNotNull(Season2Copy.returnCaptionVoiceRawRes(3))
        assertNotNull(Season2Copy.returnCaptionVoiceRawRes(5))
        assertNull(Season2Copy.returnCaptionVoiceRawRes(6))
    }
}
