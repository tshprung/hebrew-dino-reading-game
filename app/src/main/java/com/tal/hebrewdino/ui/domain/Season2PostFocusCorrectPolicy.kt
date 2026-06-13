package com.tal.hebrewdino.ui.domain

/** Praise after coach intervention, before round advance. */
object Season2PostFocusCorrectPolicy {
    fun shouldPlayCompanionPraiseOnCorrect(
        season2HadCoachIntervention: Boolean,
    ): Boolean = season2HadCoachIntervention

    /** After companion post-focus praise, narrator must not praise again on round advance. */
    fun shouldSuppressAdvanceRoundNarratorPraise(playedPostFocusCompanionPraise: Boolean): Boolean =
        playedPostFocusCompanionPraise
}
