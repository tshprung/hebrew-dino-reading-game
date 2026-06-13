package com.tal.hebrewdino.ui.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tal.hebrewdino.ui.gestures.DropTargetRegistry

/** Drop-hit rules for [DragMissingLetterGame]. */
object DragMissingLetterDropPolicy {
    const val SLOT_TARGET_ID: String = "missing_letter_slot"

    /** Extra hit slop around the visible slot box (forgiving child drops). */
    const val DROP_HIT_PADDING_DP: Float = 36f

    fun slotSizeDp(compact: Boolean): Dp = if (compact) 68.dp else 76.dp

    fun isDropOnSlot(
        registry: DropTargetRegistry,
        position: Offset,
        paddingPx: Float,
    ): Boolean = registry.findTarget(position, paddingPx) == SLOT_TARGET_ID
}
