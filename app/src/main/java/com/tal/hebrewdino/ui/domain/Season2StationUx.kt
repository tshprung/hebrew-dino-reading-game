package com.tal.hebrewdino.ui.domain

/** Season 2 gameplay presentation helpers (gated; chapters 3–6 warmup arc). */
object Season2StationUx {
    fun isWarmupPictureStartsWith(chapterId: Int, stationId: Int): Boolean =
        Season2StationAudio.isSeason2WarmupChapter(chapterId) &&
            stationKindForGameplayChapter(chapterId, stationId) ==
            Season2ChapterStationPlans.StationKind.PictureStartsWith

    fun stationKindForGameplayChapter(
        gameplayChapterId: Int,
        stationId: Int,
    ): Season2ChapterStationPlans.StationKind? {
        if (!Season2StationAudio.isSeason2GameplayChapter(gameplayChapterId)) return null
        val chapterIndex = gameplayChapterId - 100
        if (chapterIndex in 3..7) {
            return Season2ChapterStationPlans.stationKind(chapterIndex, stationId)
        }
        if (chapterIndex in 1..2) {
            return when (chapterIndex) {
                1 ->
                    when (stationId) {
                        Season2Chapter1StationOrder.POP_BALLOONS ->
                            Season2ChapterStationPlans.StationKind.PopBalloons
                        Season2Chapter1StationOrder.PICK_LETTER ->
                            Season2ChapterStationPlans.StationKind.PickLetter
                        Season2Chapter1StationOrder.PICTURE_STARTS_WITH ->
                            Season2ChapterStationPlans.StationKind.PictureStartsWith
                        Season2Chapter1StationOrder.MEMORY_MATCH ->
                            Season2ChapterStationPlans.StationKind.DragWordToPicture
                        Season2Chapter1StationOrder.WHICH_WORD_STARTS_WITH ->
                            Season2ChapterStationPlans.StationKind.WhichWordStartsWith
                        Season2Chapter1StationOrder.FINALE_STATION ->
                            Season2ChapterStationPlans.StationKind.DragMissingLetter
                        else -> null
                    }
                2 ->
                    when (stationId) {
                        1 -> Season2ChapterStationPlans.StationKind.PickLetter
                        2 -> Season2ChapterStationPlans.StationKind.DragWordToPicture
                        3 -> Season2ChapterStationPlans.StationKind.PictureStartsWith
                        4 -> Season2ChapterStationPlans.StationKind.MemoryMatch
                        5 -> Season2ChapterStationPlans.StationKind.DragMissingLetter
                        6 -> Season2ChapterStationPlans.StationKind.WordParts
                        else -> null
                    }
                else -> null
            }
        }
        return null
    }

    fun isMatchLetterFinale(gameplayChapterId: Int, stationId: Int): Boolean =
        stationKindForGameplayChapter(gameplayChapterId, stationId) ==
            Season2ChapterStationPlans.StationKind.MatchLetterToWord

    fun isWhichWordStartsWithStation(gameplayChapterId: Int, stationId: Int): Boolean =
        stationKindForGameplayChapter(gameplayChapterId, stationId) ==
            Season2ChapterStationPlans.StationKind.WhichWordStartsWith
}
