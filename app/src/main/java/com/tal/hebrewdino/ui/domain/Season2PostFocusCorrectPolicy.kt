package com.tal.hebrewdino.ui.domain

/** Season 2 only — praise after two-mistake companion focus, before round advance. */
object Season2PostFocusCorrectPolicy {
    fun shouldPlayCompanionPraiseOnCorrect(
        isSeason2QuizChapter: Boolean,
        season2HadCoachIntervention: Boolean,
    ): Boolean = isSeason2QuizChapter && season2HadCoachIntervention

    fun shouldShowPostFocusTextBubble(): Boolean = false

    fun shouldSkipGenericInStationPraise(season2HadCoachIntervention: Boolean): Boolean =
        Season2EarlyStationQaPolicy.shouldSkipInStationCorrectPraiseAfterCoach(season2HadCoachIntervention)
}
