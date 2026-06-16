package com.tal.hebrewdino.ui.game

import com.tal.hebrewdino.test.ProjectSource

import com.tal.hebrewdino.ui.domain.AnswerResult
import com.tal.hebrewdino.ui.domain.Chapter1LetterPoolSpec
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.StationQuizMode
import com.tal.hebrewdino.ui.domain.StationQuizPlan
import com.tal.hebrewdino.ui.gestures.DropTargetRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DragMissingLetterInteractionTest {
    private val wordIds = listOf("w_ד_1", "w_ב_1", "w_ל_2", "w_ש_1")

    private fun session(): LevelSession {
        val plan =
            StationQuizPlan(
                mode = StationQuizMode.DragMissingLetter,
                questionCount = 1,
                initialGroupIndex = 0,
                season2WordCatalogIds = wordIds,
                dragMissingLetterIndex = 0,
                season2AdvancedDistractorLetters = listOf("ד", "ב", "מ", "ל", "ש", "ס"),
                optionCount = 3,
            )
        return LevelSession(plan = plan, letterPoolSpec = Chapter1LetterPoolSpec)
    }

    @Test
    fun dropPolicy_acceptsCenterAndCornersOfVisibleSlot() {
        val registry = DropTargetRegistry()
        registry.update(
            id = DragMissingLetterDropPolicy.SLOT_TARGET_ID,
            rect = androidx.compose.ui.geometry.Rect(100f, 200f, 176f, 276f),
        )
        val paddingPx = 36f
        assertTrue(
            DragMissingLetterDropPolicy.isDropOnSlot(
                registry,
                androidx.compose.ui.geometry.Offset(138f, 238f),
                paddingPx,
            ),
        )
        assertTrue(
            DragMissingLetterDropPolicy.isDropOnSlot(
                registry,
                androidx.compose.ui.geometry.Offset(100f, 200f),
                paddingPx,
            ),
        )
        assertTrue(
            DragMissingLetterDropPolicy.isDropOnSlot(
                registry,
                androidx.compose.ui.geometry.Offset(176f, 276f),
                paddingPx,
            ),
        )
    }

    @Test
    fun dropPolicy_acceptsNearMissWithinPadding() {
        val registry = DropTargetRegistry()
        registry.update(
            id = DragMissingLetterDropPolicy.SLOT_TARGET_ID,
            rect = androidx.compose.ui.geometry.Rect(100f, 200f, 176f, 276f),
        )
        assertTrue(
            DragMissingLetterDropPolicy.isDropOnSlot(
                registry,
                androidx.compose.ui.geometry.Offset(70f, 238f),
                paddingPx = 36f,
            ),
        )
    }

    @Test
    fun dropPolicy_rejectsFarMiss() {
        val registry = DropTargetRegistry()
        registry.update(
            id = DragMissingLetterDropPolicy.SLOT_TARGET_ID,
            rect = androidx.compose.ui.geometry.Rect(100f, 200f, 176f, 276f),
        )
        assertFalse(
            DragMissingLetterDropPolicy.isDropOnSlot(
                registry,
                androidx.compose.ui.geometry.Offset(20f, 20f),
                paddingPx = 36f,
            ),
        )
    }

    @Test
    fun correctLetter_submittedOnce_advancesRoundOnce() {
        val session = session()
        val q = session.currentQuestion as Question.DragMissingLetterQuestion
        assertEquals(AnswerResult.Correct, session.submitDragMissingLetter(q.correctLetter))
        assertEquals(1, session.correctCount)
        assertEquals(AnswerResult.Correct, session.submitDragMissingLetter(q.correctLetter))
        assertEquals(1, session.correctCount)
    }

    @Test
    fun wrongLetter_rejectedAndCanRetry() {
        val session = session()
        val q = session.currentQuestion as Question.DragMissingLetterQuestion
        val wrong = q.optionLetters.first { it != q.correctLetter }
        assertEquals(AnswerResult.Wrong, session.submitDragMissingLetter(wrong))
        assertEquals(0, session.correctCount)
        assertEquals(AnswerResult.Correct, session.submitDragMissingLetter(q.correctLetter))
        assertEquals(1, session.correctCount)
    }

    @Test
    fun validate_supportsTapFallbackSemantics() {
        val session = session()
        val q = session.currentQuestion as Question.DragMissingLetterQuestion
        assertTrue(session.validateDragMissingLetter(q.correctLetter))
        val wrong = q.optionLetters.first { it != q.correctLetter }
        assertFalse(session.validateDragMissingLetter(wrong))
    }

    @Test
    fun game_keepsDragGestureEnabledWhileDragging() {
        val source = ProjectSource.read("app/src/main/java/com/tal/hebrewdino/ui/game/DragMissingLetterGame.kt")
        assertTrue(source.contains("enabled = interactionEnabled"))
        assertFalse(source.contains("enabled = chipEnabled"))
        assertFalse(source.contains(".hebrewDraggable(\n                                        enabled = tapEnabled"))
    }

    @Test
    fun game_wiresTapFallbackOnSlotAndOptions() {
        val source = ProjectSource.read("app/src/main/java/com/tal/hebrewdino/ui/game/DragMissingLetterGame.kt")
        assertTrue(source.contains("selectedLetter?.let { onTryPlaceLetter(it) }"))
        assertTrue(source.contains(".clickable(enabled = tapEnabled)"))
        assertTrue(source.contains(".registerDropTarget("))
        assertTrue(source.contains("DragMissingLetterDropPolicy.SLOT_TARGET_ID"))
    }

    @Test
    fun game_usesFullSlotBoundsForDropTarget() {
        val source = ProjectSource.read("app/src/main/java/com/tal/hebrewdino/ui/game/DragMissingLetterGame.kt")
        assertTrue(source.contains(".size(slotSize)"))
        assertTrue(source.contains("DragMissingLetterDropPolicy.DROP_HIT_PADDING_DP"))
    }

    @Test
    fun game_doesNotReplayLetterOnTapOrDragStart() {
        val source = ProjectSource.read("app/src/main/java/com/tal/hebrewdino/ui/game/DragMissingLetterGame.kt")
        val hostSource = ProjectSource.read("app/src/main/java/com/tal/hebrewdino/ui/screens/GameQuestionHost.kt")
        assertTrue(source.contains("onLetterSelected?.invoke(letter)"))
        assertTrue(hostSource.contains("onLetterSelected = null"))
    }
}
