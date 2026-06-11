package com.tal.hebrewdino.ui.domain

/** Pure rules for Season 2 Ch1–3 early-station QA fixes (unit-testable). */
object Season2EarlyStationQaPolicy {
    fun shouldShowPictureStartsWithHint(wrongTapsThisQuestion: Int): Boolean =
        wrongTapsThisQuestion >= 2

    fun shouldPlayBalloonPraiseOnCorrectPop(season2QuizBalloons: Boolean): Boolean =
        season2QuizBalloons

    fun shouldPlayBalloonPraiseOnCorrectPop(
        season2QuizBalloons: Boolean,
        finalCorrectBalloon: Boolean,
    ): Boolean =
        shouldPlayBalloonPraiseOnCorrectPop(season2QuizBalloons) && !finalCorrectBalloon

    fun shouldReplayWordForPictureStartsWithCoach(season2UxStationId: Int): Boolean =
        season2UxStationId == Season2Chapter1StationOrder.PICTURE_STARTS_WITH

    fun shouldUseSeason2PictureStartsWithWrongAudio(
        isSeason2QuizChapter: Boolean,
        sagaEpisode: Boolean,
        stationId: Int,
    ): Boolean =
        isSeason2QuizChapter ||
            (sagaEpisode && stationId == com.tal.hebrewdino.ui.domain.Chapter1StationOrder.PICTURE_PICK_ONE)

    fun shouldSkipInStationCorrectPraiseAfterCoach(season2HadCoachIntervention: Boolean): Boolean =
        season2HadCoachIntervention

    fun shouldShowMapReturnPraiseCaption(): Boolean = false
}
