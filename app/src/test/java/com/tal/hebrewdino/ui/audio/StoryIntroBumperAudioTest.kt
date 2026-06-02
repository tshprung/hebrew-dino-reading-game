package com.tal.hebrewdino.ui.audio

import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.data.DinoCharacter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StoryIntroBumperAudioTest {
    @Test
    fun supportsIntroBumper_chaptersTwoThroughSixOnly() {
        assertFalse(StoryIntroBumperAudio.supportsIntroBumper(1))
        assertTrue(StoryIntroBumperAudio.supportsIntroBumper(2))
        assertTrue(StoryIntroBumperAudio.supportsIntroBumper(6))
        assertFalse(StoryIntroBumperAudio.supportsIntroBumper(7))
    }

    @Test
    fun introBumperRawRes_chapter2_mapsByCompanion() {
        assertEquals(
            R.raw.story_bumper_ch2_intro_dino,
            StoryIntroBumperAudio.introBumperRawRes(2, DinoCharacter.Dino),
        )
        assertEquals(
            R.raw.story_bumper_ch2_intro_dina,
            StoryIntroBumperAudio.introBumperRawRes(2, DinoCharacter.Dina),
        )
    }

    @Test
    fun introBumperRawRes_chaptersThreeThroughSix_mapsDistinctDinoAndDinaClips() {
        for (chapterId in 3..6) {
            val dino = StoryIntroBumperAudio.introBumperRawRes(chapterId, DinoCharacter.Dino)
            val dina = StoryIntroBumperAudio.introBumperRawRes(chapterId, DinoCharacter.Dina)
            assertTrue(dino != 0)
            assertTrue(dina != 0)
            assertTrue(dino != dina)
        }
    }

    @Test
    fun introBumperRawRes_unknownChapter_returnsZero() {
        assertEquals(0, StoryIntroBumperAudio.introBumperRawRes(99, DinoCharacter.Dino))
    }

    @Test
    fun introBumper_doesNotUseStoryNarrationRawIds() {
        val bumper = StoryIntroBumperAudio.introBumperRawRes(4, DinoCharacter.Dino)
        assertFalse(bumper == R.raw.ch4_story_intro_dino)
        assertFalse(bumper == R.raw.ch4_story_intro_dina)
    }

    @Test
    fun introBumperBodyText_chapter2_sameForDinoAndDina() {
        val expected = "איזה כיף שאתם איתי! תודה שאתם עוזרים לי לחפש את הביצה הבאה."
        assertEquals(expected, StoryIntroBumperAudio.introBumperBodyText(2, DinoCharacter.Dino))
        assertEquals(expected, StoryIntroBumperAudio.introBumperBodyText(2, DinoCharacter.Dina))
    }

    @Test
    fun introBumperBodyText_chapter3_differsByCompanionGender() {
        assertEquals(
            "אני שמח שאתם ממשיכים איתי! בואו נעקוב יחד אחרי העקבות.",
            StoryIntroBumperAudio.introBumperBodyText(3, DinoCharacter.Dino),
        )
        assertEquals(
            "אני שמחה שאתם ממשיכים איתי! בואו נעקוב יחד אחרי העקבות.",
            StoryIntroBumperAudio.introBumperBodyText(3, DinoCharacter.Dina),
        )
    }

    @Test
    fun introBumperBodyText_chaptersFourThroughSix_nonEmpty() {
        for (chapterId in 4..6) {
            assertTrue(StoryIntroBumperAudio.introBumperBodyText(chapterId, DinoCharacter.Dino).isNotBlank())
            assertTrue(StoryIntroBumperAudio.introBumperBodyText(chapterId, DinoCharacter.Dina).isNotBlank())
        }
    }

    @Test
    fun introBumperBodyText_unknownChapter_empty() {
        assertEquals("", StoryIntroBumperAudio.introBumperBodyText(99, DinoCharacter.Dino))
    }
}
