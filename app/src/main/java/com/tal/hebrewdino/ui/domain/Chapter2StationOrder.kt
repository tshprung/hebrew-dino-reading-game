package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.BALLOON_POP
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.PICTURE_PICK_ALL
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.PICTURE_PICK_ONE
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.REVEAL_THEN_CHOOSE
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.TAP_LETTER

/**
 * Chapter 2 — same station types as chapter 1, reordered so PopBalloons is spaced later (st5).
 */
object Chapter2StationOrder {
    fun quizPlan(stationId: Int): StationQuizPlan =
        when (stationId.coerceIn(1, Chapter2Config.STATION_COUNT)) {
            1 -> Chapter1StationOrder.quizPlan(TAP_LETTER)
            2 -> Chapter1StationOrder.quizPlan(REVEAL_THEN_CHOOSE)
            3 -> Chapter1StationOrder.quizPlan(PICTURE_PICK_ONE)
            4 -> Chapter1StationOrder.quizPlan(PICTURE_PICK_ALL)
            5 -> Chapter1StationOrder.quizPlan(BALLOON_POP)
            6 -> Chapter1StationOrder.quizPlan(FINALE_PICTURE_LETTER_MATCH)
            else ->
                error(
                    "Chapter 2 station out of range 1..${Chapter2Config.STATION_COUNT} after coerce (raw=$stationId)",
                )
        }
}
