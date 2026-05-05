package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Episode4HelpTest {

    @Test
    fun helpColumnActive_onlyChapter4WithRegistryFlag() {
        val ep4 =
            StationUiSpec(
                chapterId = 4,
                stationId = 1,
                quizMode = StationQuizMode.PickLetter,
                helpControlsEnabled = true,
            )
        assertTrue(Episode4Help.isHelpColumnActive(4, ep4))
        assertFalse(Episode4Help.isHelpColumnActive(5, ep4))
        assertFalse(Episode4Help.isHelpColumnActive(4, ep4.copy(helpControlsEnabled = false)))
    }

    @Test
    fun targetLetterForHelpHint_matchesQuestionShapes() {
        val pop =
            Question.PopBalloonsQuestion(
                correctAnswer = "מ",
                options = listOf("מ", "ב"),
            )
        assertEquals("מ", Episode4Help.targetLetterForHelpHint(pop))

        val cells =
            buildList {
                repeat(3) { add("ד") }
                repeat(6) { add("א") }
            }
        val grid =
            Question.FindLetterGridQuestion(
                targetLetter = "ד",
                columns = 3,
                rows = 3,
                cells = cells,
            )
        assertEquals("ד", Episode4Help.targetLetterForHelpHint(grid))

        val pic =
            Question.PictureStartsWithQuestion(
                correctLetter = "א",
                word = "אריה",
                catalogEntryId = "x",
                tileDrawable = R.drawable.lesson_word_tile,
                tintArgb = 0,
                optionLetters = listOf("א", "ב"),
            )
        assertEquals("א", Episode4Help.targetLetterForHelpHint(pic))

        val choice =
            LessonChoice(
                id = "c1",
                letter = "ג",
                word = "גג",
                tintArgb = 0,
                tileDrawable = R.drawable.lesson_word_tile,
            )
        val other =
            LessonChoice(
                id = "c2",
                letter = "ב",
                word = "בב",
                tintArgb = 0,
                tileDrawable = R.drawable.lesson_word_tile,
            )
        val img =
            Question.ImageMatchQuestion(
                targetWord = "גג",
                targetLetter = "ג",
                choices = listOf(choice, other),
                correctChoiceId = "c1",
            )
        assertEquals("ג", Episode4Help.targetLetterForHelpHint(img))
    }

    @Test
    fun hintRevealFallback_matchesLegacyThreeSeconds() {
        assertEquals(3000L, Episode4Help.HINT_REVEAL_FALLBACK_MS)
    }
}
