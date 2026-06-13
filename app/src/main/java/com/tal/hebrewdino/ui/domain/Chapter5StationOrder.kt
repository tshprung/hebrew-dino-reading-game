package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.PICTURE_PICK_ALL
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.PICTURE_PICK_ONE
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.REVEAL_THEN_CHOOSE
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.TAP_LETTER

/**
 * Chapter 5 — learning arc with a gentle [DragMissingLetter] intro at station 2.
 */
object Chapter5StationOrder {
    fun quizPlan(stationId: Int): StationQuizPlan =
        when (stationId.coerceIn(1, Chapter5Config.STATION_COUNT)) {
            1 -> Chapter1StationOrder.quizPlan(TAP_LETTER)
            2 -> Season1DragStationQuizPlans.dragMissingLetter(questionCount = 6)
            3 -> Chapter1StationOrder.quizPlan(REVEAL_THEN_CHOOSE)
            4 -> Chapter1StationOrder.quizPlan(PICTURE_PICK_ONE)
            5 -> Chapter1StationOrder.quizPlan(PICTURE_PICK_ALL)
            6 -> Chapter1StationOrder.quizPlan(FINALE_PICTURE_LETTER_MATCH)
            else ->
                error(
                    "Chapter 5 station out of range 1..${Chapter5Config.STATION_COUNT} after coerce (raw=$stationId)",
                )
        }
}
