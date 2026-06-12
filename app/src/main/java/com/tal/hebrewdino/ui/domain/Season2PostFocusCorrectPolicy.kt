package com.tal.hebrewdino.ui.domain

/** Season 2 only — praise after two-mistake companion focus, before round advance. */
object Season2PostFocusCorrectPolicy {
    fun shouldPlayCompanionPraiseOnCorrect(
        isSeason2QuizChapter: Boolean,
        season2HadCoachIntervention: Boolean,
    ): Boolean = isSeason2QuizChapter && season2HadCoachIntervention

    /** After companion post-focus praise, narrator must not praise again on round advance. */
    fun shouldSuppressAdvanceRoundNarratorPraise(playedPostFocusCompanionPraise: Boolean): Boolean =
        playedPostFocusCompanionPraise
}
