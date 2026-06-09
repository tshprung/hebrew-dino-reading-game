package com.tal.hebrewdino.ui.domain

/** Readable reward overlay sizing for Season 2 chapter completion (phone landscape first). */
object Season2RewardLayout {
    /** Main reward visual — discovered dinosaur poster. */
    const val POSTER_MAX_WIDTH_COMPACT_DP: Int = 520
    const val POSTER_MAX_HEIGHT_COMPACT_DP: Int = 248
    const val POSTER_MAX_WIDTH_REGULAR_DP: Int = 560
    const val POSTER_MAX_HEIGHT_REGULAR_DP: Int = 288

    /** Secondary companion beside the poster — must stay smaller than poster focus. */
    const val COMPANION_SIZE_COMPACT_DP: Int = 108
    const val COMPANION_SIZE_REGULAR_DP: Int = 132
    const val COMPANION_MAX_POSTER_HEIGHT_RATIO: Float = 0.46f

    const val HEADLINE_SP_COMPACT: Int = 30
    const val HEADLINE_SP_REGULAR: Int = 36
    const val SUBLINE_SP_COMPACT: Int = 17
    const val SUBLINE_SP_REGULAR: Int = 20

    const val CONTINUE_MIN_WIDTH_DP: Int = 168
    const val CONTINUE_MAX_WIDTH_DP: Int = 196
    const val CONTINUE_HEIGHT_DP: Int = 44
    const val CONTINUE_FONT_SP_COMPACT: Int = 15
    const val CONTINUE_FONT_SP_REGULAR: Int = 16

    const val HERO_GAP_DP: Int = 10
    const val TEXT_TO_HERO_GAP_DP: Int = 6
    const val HERO_TO_BUTTON_GAP_DP: Int = 8
}
