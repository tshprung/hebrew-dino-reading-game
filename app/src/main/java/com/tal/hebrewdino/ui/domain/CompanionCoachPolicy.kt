package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.data.PlayerAddress

/** When the companion coach flow (focus → replay → post-focus praise) is active. */
object CompanionCoachPolicy {
    fun isSeason1Chapter(chapterId: Int): Boolean = chapterId in 1..6

    fun isEnabled(
        chapterId: Int,
        companion: DinoCharacter?,
        playerAddress: PlayerAddress?,
    ): Boolean =
        companion != null &&
            playerAddress != null &&
            (isSeason1Chapter(chapterId) || Season2StationAudio.isSeason2GameplayChapter(chapterId))

    fun uxStationId(season2UxStationId: Int?, stationId: Int): Int =
        season2UxStationId ?: stationId
}
