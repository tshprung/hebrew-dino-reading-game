package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.ui.data.PlayerAddress

/** Kid-facing Hebrew hints for Season 2 Chapter 1 anti-guessing coach. */
object Season2GuessingHintCopy {
    fun openers(playerAddress: PlayerAddress): String =
        when (playerAddress) {
            PlayerAddress.Boy -> rtl("רגע, בוא נקשיב שוב.")
            PlayerAddress.Girl -> rtl("רגע, בואי נקשיב שוב.")
        }

    /** One short line for the companion speech bubble (phone-first). */
    fun coachBubbleText(
        season2StationId: Int,
        playerAddress: PlayerAddress,
    ): String {
        val pace =
            when (playerAddress) {
                PlayerAddress.Boy -> "נסה לאט."
                PlayerAddress.Girl -> "נסי לאט."
            }
        val specific =
            when (season2StationId) {
                Season2Chapter1StationOrder.POP_BALLOONS -> "נחפש את האות ששמענו."
                Season2Chapter1StationOrder.PICK_LETTER -> "איזו אות שמענו?"
                Season2Chapter1StationOrder.PICTURE_STARTS_WITH,
                Season2Chapter1StationOrder.WHICH_WORD_STARTS_WITH,
                -> "איזו מילה מתחילה באות?"
                Season2Chapter1StationOrder.MATCH_LETTER_TO_WORD -> "נחפש מילה שמתחילה באות."
                Season2Chapter1StationOrder.MEMORY_MATCH -> "ננסה לזכור איפה האותיות."
                5 -> "נקשיב שוב למילה."
                6 -> "נחפש את המילה המתאימה."
                // Advanced stations (Ch3–6 st5/st6) reuse station ids; hints stay short and mode-neutral.
                else -> pace
            }
        return rtl("רגע, נקשיב שוב. $specific")
    }

    fun hintForStation(
        season2StationId: Int,
        playerAddress: PlayerAddress,
    ): String = coachBubbleText(season2StationId, playerAddress)

    fun processPraise(playerAddress: PlayerAddress): String {
        val options =
            when (playerAddress) {
                PlayerAddress.Boy ->
                    listOf(
                        "יפה! הקשבת ומצאת!",
                        "מעולה, חשבת לאט ומצאת!",
                        "כל הכבוד, זה היה ריכוז טוב!",
                    )
                PlayerAddress.Girl ->
                    listOf(
                        "יפה! הקשבת ומצאת!",
                        "מעולה, חשבת לאט ומצאת!",
                        "כל הכבוד, זה היה ריכוז טוב!",
                    )
            }
        return rtl(options.random())
    }

    private fun rtl(text: String): String = "\u200F$text"
}
