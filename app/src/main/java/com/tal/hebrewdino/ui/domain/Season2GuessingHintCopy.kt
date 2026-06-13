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
        uxStationId: Int,
        playerAddress: PlayerAddress,
        templateId: StationTemplateId? = null,
        gameplayChapterId: Int? = null,
    ): String {
        val pace =
            when (playerAddress) {
                PlayerAddress.Boy -> "נסה לאט."
                PlayerAddress.Girl -> "נסי לאט."
            }
        val specific =
            when (templateId) {
                StationTemplateId.PopBalloons -> "נחפש את האות ששמענו."
                StationTemplateId.PickLetter -> "איזו אות שמענו?"
                StationTemplateId.FindLetterGrid -> "נמצא את האות בטבלה."
                StationTemplateId.PictureStartsWith -> "איזו מילה מתחילה באות?"
                StationTemplateId.ImageMatch -> "נבחר את התמונה הנכונה."
                StationTemplateId.MatchLetterToWord -> "נחפש מילה שמתחילה באות."
                StationTemplateId.ImageToWord ->
                    if (gameplayChapterId == 3 || gameplayChapterId == 6) {
                        "נבחר את המילה שמתאימה לתמונה."
                    } else {
                        "נקשיב שוב למילה."
                    }
                StationTemplateId.DragWordToPicture -> "נגרור את המילה לתמונה."
                StationTemplateId.DragMissingLetter -> "נשלים את האות החסרה."
                null ->
                    when (uxStationId) {
                        Season2Chapter1StationOrder.POP_BALLOONS -> "נחפש את האות ששמענו."
                        Season2Chapter1StationOrder.PICK_LETTER -> "איזו אות שמענו?"
                        Season2Chapter1StationOrder.PICTURE_STARTS_WITH,
                        Season2Chapter1StationOrder.WHICH_WORD_STARTS_WITH,
                        -> "איזו מילה מתחילה באות?"
                        Season2Chapter1StationOrder.MATCH_LETTER_TO_WORD -> "נחפש מילה שמתחילה באות."
                        Season2Chapter1StationOrder.MEMORY_MATCH -> "ננסה לזכור איפה האותיות."
                        5 -> "נקשיב שוב למילה."
                        6 -> "נחפש את המילה המתאימה."
                        else -> pace
                    }
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
