package com.tal.hebrewdino.ui.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Helpers for layouts that must work in **portrait and landscape** (phones and tablets).
 *
 * Rules used across game UI:
 * - Prefer a parent `Column`/`Row` with **`weight(1f)`** so children get a **bounded** max size.
 * - Avoid `fillMaxHeight()` under `Arrangement.Center` without `weight` — measurement often becomes unbounded
 *   and wide screens pick huge sizes from width alone.
 * - When a `BoxWithConstraints` might still see unbounded height, combine [shortSideDp] with [effectiveMaxHeight].
 */
object ScreenFit {
    @Composable
    fun shortSideDp(): Dp {
        val c = LocalConfiguration.current
        return minOf(c.screenWidthDp, c.screenHeightDp).dp
    }

    @Composable
    fun longSideDp(): Dp {
        val c = LocalConfiguration.current
        return maxOf(c.screenWidthDp, c.screenHeightDp).dp
    }

    /**
     * [boxMaxHeight] is [androidx.compose.foundation.layout.BoxWithConstraintsScope.maxHeight].
     */
    fun effectiveMaxHeight(
        hasBoundedHeight: Boolean,
        boxMaxHeight: Dp,
        shortSideDp: Dp,
    ): Dp =
        if (hasBoundedHeight) {
            boxMaxHeight
        } else {
            (shortSideDp * 0.55f).coerceAtLeast(140.dp)
        }

    /** Caps oversized grid cells on very wide landscape layouts. */
    fun gridCellCapDp(shortSideDp: Dp): Dp = (shortSideDp * 0.19f).coerceIn(38.dp, 56.dp)

    /** Max height for balloon / free-form tap areas so they stay on-screen in landscape. */
    @Composable
    fun popBalloonsAreaHeightDp(default: Dp = 440.dp): Dp =
        minOf(default, shortSideDp() * 0.52f).coerceAtLeast(220.dp)

    /**
     * Equal width for [count] siblings in a row with [gap] between them (e.g. image-match cards).
     * Clamps so very narrow phones still fit without horizontal clip.
     */
    fun rowChildWidthDp(
        rowInnerWidth: Dp,
        count: Int,
        gap: Dp,
        minEach: Dp = 88.dp,
        maxEach: Dp = 172.dp,
    ): Dp {
        val n = count.coerceAtLeast(1)
        val raw = (rowInnerWidth - gap * (n - 1).coerceAtLeast(0)) / n
        val lower = minOf(minEach, raw)
        return raw.coerceIn(lower, maxEach)
    }
}
