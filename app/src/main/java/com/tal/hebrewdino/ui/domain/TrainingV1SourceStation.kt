package com.tal.hebrewdino.ui.domain

/**
 * Maps Training chapter (7) stations to the first equivalent station in Season 1 chapters 1–6.
 * Used for UI spec, audio staging, and gameplay parity (content still comes from training pools).
 */
object TrainingV1SourceStation {
    fun sourceChapterAndStation(trainingStationId: Int): Pair<Int, Int> =
        when (trainingStationId) {
            TrainingV1Config.STATION_HEAR_LETTER_CHOOSE ->
                1 to Chapter1StationOrder.TAP_LETTER
            TrainingV1Config.STATION_WHICH_WORD_STARTS_WITH_LETTER ->
                1 to Chapter1StationOrder.PICTURE_PICK_ALL
            TrainingV1Config.STATION_PICTURE_CHOOSE_WORD ->
                3 to 6
            TrainingV1Config.STATION_FIND_HEARD_LETTER_IN_GRID ->
                1 to Chapter1StationOrder.REVEAL_THEN_CHOOSE
            TrainingV1Config.STATION_WORD_BALLOONS ->
                1 to Chapter1StationOrder.BALLOON_POP
            TrainingV1Config.STATION_MATCH_LETTER_TO_WORD ->
                1 to Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH
            else -> error("Unknown Training v1 stationId=$trainingStationId")
        }

    /** Resolves training ids to source ids; identity for all other chapters. */
    fun resolve(chapterId: Int, stationId: Int): Pair<Int, Int> =
        if (chapterId == TrainingV1Config.CHAPTER_ID) {
            sourceChapterAndStation(stationId)
        } else {
            chapterId to stationId
        }

    fun sourceQuizPlan(trainingStationId: Int): StationQuizPlan {
        val (sourceChapter, sourceStation) = sourceChapterAndStation(trainingStationId)
        val plan =
            when (sourceChapter) {
                1 -> StationQuizPlans.chapter1(sourceStation)
                3 -> StationQuizPlans.chapter3(sourceStation)
                else -> error("Unsupported training source chapterId=$sourceChapter")
            }
        return plan.copy(questionCount = 1)
    }
}
