package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.ui.companion.CompanionVisualPolicy

/** Season 2 gameplay presentation helpers (gated; chapters 3–6 warmup arc). */
object Season2StationUx {
    fun expectsSelectedCompanion(chapterId: Int): Boolean =
        CompanionVisualPolicy.expectsSelectedCompanion(chapterId)

    fun isWarmupPictureStartsWith(chapterId: Int, stationId: Int): Boolean =
        Season2StationAudio.isSeason2WarmupChapter(chapterId) &&
            stationId == Season2Chapter1StationOrder.PICTURE_STARTS_WITH &&
            runCatching {
                val chapterIndex = chapterId - 100
                Season2ChapterStationPlans.stationKind(chapterIndex, stationId) ==
                    Season2ChapterStationPlans.StationKind.PictureStartsWith
            }.getOrDefault(false)

    fun stationKindForGameplayChapter(
        gameplayChapterId: Int,
        stationId: Int,
    ): Season2ChapterStationPlans.StationKind? {
        if (!Season2StationAudio.isSeason2GameplayChapter(gameplayChapterId)) return null
        val chapterIndex = gameplayChapterId - 100
        if (chapterIndex in 3..6) {
            return Season2ChapterStationPlans.stationKind(chapterIndex, stationId)
        }
        if (chapterIndex in 1..2) {
            return when (stationId) {
                Season2Chapter1StationOrder.POP_BALLOONS -> Season2ChapterStationPlans.StationKind.PopBalloons
                Season2Chapter1StationOrder.PICK_LETTER -> Season2ChapterStationPlans.StationKind.PickLetter
                Season2Chapter1StationOrder.PICTURE_STARTS_WITH ->
                    Season2ChapterStationPlans.StationKind.PictureStartsWith
                Season2Chapter1StationOrder.WHICH_WORD_STARTS_WITH ->
                    Season2ChapterStationPlans.StationKind.WhichWordStartsWith
                Season2Chapter1StationOrder.FINALE_STATION ->
                    when (chapterIndex) {
                        1 -> Season2ChapterStationPlans.StationKind.PictureToWord
                        2 -> Season2ChapterStationPlans.StationKind.WordParts
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

    fun isWordPartsStation(gameplayChapterId: Int, stationId: Int): Boolean =
        stationKindForGameplayChapter(gameplayChapterId, stationId) ==
            Season2ChapterStationPlans.StationKind.WordParts
}
