package com.tal.hebrewdino.ui.domain

/**
 * Maps Season 1 six-station arc gameplay ids to early-arc UX station ids so shared
 * [Season2StationQaPolicy] inter-round timing applies (e.g. Ch1 st5 WhichWord).
 */
object SixStationArcQaPolicy {
    private val sagaChapterRange = 1..5

    private val whichWordReferencePlan =
        Chapter1StationOrder.quizPlan(Chapter1StationOrder.PICTURE_PICK_ALL)

    private fun planForSagaChapter(chapterId: Int, stationId: Int): StationQuizPlan? =
        when (chapterId) {
            1 -> StationQuizPlans.chapter1(stationId)
            2 -> StationQuizPlans.chapter2(stationId)
            4 -> StationQuizPlans.chapter4(stationId)
            5 -> StationQuizPlans.chapter5(stationId)
            else -> null
        }

    /** Saga picture-starts-with slot (Ch1 st4 equivalent), regardless of physical station index. */
    fun isSagaPictureStartsWithStation(chapterId: Int, stationId: Int): Boolean {
        val plan = planForSagaChapter(chapterId, stationId) ?: return false
        return plan.mode == StationQuizMode.PictureStartsWith && !plan.listenOnlyTargetPrompt
    }

    /** Saga pop-balloons slot (Ch1 st2 equivalent), regardless of physical station index. */
    fun isSagaPopBalloonsStation(chapterId: Int, stationId: Int): Boolean {
        val plan = planForSagaChapter(chapterId, stationId) ?: return false
        return plan.mode == StationQuizMode.PopBalloons && !plan.listenOnlyTargetPrompt
    }

    /** Saga which-word slot (Ch1 st5 equivalent), regardless of physical station index. */
    fun isSagaWhichWordStartsWithStation(chapterId: Int, stationId: Int): Boolean {
        val plan = planForSagaChapter(chapterId, stationId) ?: return false
        return plan.mode == StationQuizMode.ImageMatch &&
            plan.imageMatchAlwaysThreeChoices &&
            plan.initialGroupIndex == whichWordReferencePlan.initialGroupIndex &&
            !plan.listenOnlyTargetPrompt
    }

    fun earlyArcUxStationId(chapterId: Int, stationId: Int): Int? {
        if (chapterId !in sagaChapterRange) return null
        if (isSagaPopBalloonsStation(chapterId, stationId)) {
            return Season2Chapter1StationOrder.POP_BALLOONS
        }
        if (isSagaWhichWordStartsWithStation(chapterId, stationId)) {
            return Season2Chapter1StationOrder.WHICH_WORD_STARTS_WITH
        }
        return null
    }

    fun resolveUxStationIdForQa(
        chapterId: Int,
        stationId: Int,
        season2UxStationId: Int?,
    ): Int? = season2UxStationId ?: earlyArcUxStationId(chapterId, stationId)
}
