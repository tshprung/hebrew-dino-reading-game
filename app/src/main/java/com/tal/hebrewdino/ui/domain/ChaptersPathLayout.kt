package com.tal.hebrewdino.ui.domain

/**
 * Normalized geometry for the **Chapters** map only — not shared with [JourneyMapLayout] / [JourneyScreen].
 *
 * Coordinates are fractions of container width (x) and height (y), origin top-left.
 * Parameter `t` runs along the winding path from **top** (`t≈0`) to **bottom** (`t≈1`).
 */
object ChaptersPathLayout {
    const val CHAPTER_COUNT: Int = 10

    data class NormPt(val x: Float, val y: Float)

    /** `t` in [0,1] along the same centerline as the road drawn on the chapters screen. */
    fun pointOnPath(t: Float): NormPt {
        val p0 = NormPt(0.66f, 0.06f)
        val p1 = NormPt(0.44f, 0.18f)
        val p2 = NormPt(0.70f, 0.34f)
        val p3 = NormPt(0.38f, 0.46f)
        val p4 = NormPt(0.72f, 0.62f)
        val p5 = NormPt(0.40f, 0.78f)
        val p6 = NormPt(0.64f, 0.94f)
        val tt = t.coerceIn(0f, 1f)
        return if (tt <= 0.5f) {
            chapCubic(tt * 2f, p0, p1, p2, p3)
        } else {
            chapCubic((tt - 0.5f) * 2f, p3, p4, p5, p6)
        }
    }

    /**
     * Chapter **1** is at the **bottom** of the scroll (high `t`); chapter **10** near the **top** (low `t`).
     * `chapterIndex` is 0-based (0 = chapter 1).
     */
    fun tForChapterIndex(chapterIndex: Int, total: Int = CHAPTER_COUNT): Float {
        val i = chapterIndex.coerceIn(0, total - 1)
        return 1f - (i + 0.5f) / total
    }

    private fun chapCubic(t: Float, p0: NormPt, p1: NormPt, p2: NormPt, p3: NormPt): NormPt {
        val u = 1f - t
        val uu = u * u
        val tt = t * t
        val x = u * uu * p0.x + 3f * uu * t * p1.x + 3f * u * tt * p2.x + t * tt * p3.x
        val y = u * uu * p0.y + 3f * uu * t * p1.y + 3f * u * tt * p2.y + t * tt * p3.y
        return NormPt(x, y)
    }
}
