package com.tal.hebrewdino.ui.domain

/** Pure rules for Season 2 Ch1–3 early-station QA fixes (unit-testable). */
object Season2EarlyStationQaPolicy {
    fun shouldShowPictureStartsWithHint(wrongTapsThisQuestion: Int): Boolean =
        wrongTapsThisQuestion >= 2

    fun shouldPlayBalloonPraiseOnCorrectPop(season2QuizBalloons: Boolean): Boolean =
        season2QuizBalloons

    fun shouldReplayWordForPictureStartsWithCoach(season2UxStationId: Int): Boolean =
        season2UxStationId == Season2Chapter1StationOrder.PICTURE_STARTS_WITH
}
