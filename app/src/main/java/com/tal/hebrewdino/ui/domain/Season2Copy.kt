package com.tal.hebrewdino.ui.domain

import androidx.annotation.RawRes
import com.tal.hebrewdino.ui.audio.Season2RawAudio
import com.tal.hebrewdino.ui.data.PlayerAddress

/** Kid-facing Hebrew copy for Season 2 — "מגלים דינוזאורים". */
object Season2Copy {
    const val SeasonTitle: String = "מגלים דינוזאורים"
    const val SeasonSubtitle: String = "מפה מסתורית — גלו דינוזאורים חבויים"
    const val ChapterSelectTitle: String = "מגלים דינוזאורים"
    const val MysteryMapTitle: String = "מפה מסתורית"
    const val WhoHidesHere: String = "מי מסתתר כאן?"
    const val FutureDinosaur: String = "דינוזאור מסתורי"
    const val ComingSoon: String = "בקרוב"
    const val NextChapterComingSoon: String = "הבא — בקרוב"
    const val ChapterRevealedBadge: String = "התגלה!"
    const val ReplayTileInstruction: String = "לחצו על ריבוע כדי לשחק שוב"
    const val ChapterLabelPrefix: String = "פרק"

    fun chapterSelectLabel(chapterIndex: Int, completed: Boolean): String =
        if (completed) {
            revealedDinosaurName(chapterIndex) ?: rtl("$ChapterLabelPrefix $chapterIndex")
        } else if (Season2ChapterRegistry.isPlayable(chapterIndex)) {
            rtl(WhoHidesHere)
        } else {
            rtl(FutureDinosaur)
        }

    fun puzzleMapTitle(chapterId: Int, chapterCompleted: Boolean): String =
        if (chapterCompleted) {
            val name = revealedDinosaurName(chapterId)
            if (name != null) rtl("$ChapterLabelPrefix $chapterId · $name") else rtl("$ChapterLabelPrefix $chapterId")
        } else {
            rtl("$MysteryMapTitle · $WhoHidesHere")
        }

    fun completionHeadline(chapterId: Int): String {
        val name = revealedDinosaurName(chapterId) ?: "הדינוזאור"
        return rtl("גיליתם את ה$name!")
    }

    fun completionSubline(): String = rtl("כל החלקים נחשפו — הדינוזאור שלם!")

    fun completionContinueLabel(): String = rtl("המשך")

    /** Season-level intro before chapter select (short adventure story). */
    fun seasonIntroStoryLines(): List<String> =
        listOf(
            rtl("מצאנו מפה עתיקה!"),
            rtl("בכל חלק של המפה מסתתר דינוזאור אחר."),
            rtl("בכל פעם שתפתרו משימה, עוד חלק מהתמונה יתגלה."),
            rtl("בואו נגלה מי מסתתר שם!"),
        )

    /** Chapter map intro lines — visible text matches recorded chapter intro narration (no niqqud). */
    fun ch1MapIntroStoryLines(): List<String> = chapterMapIntroStoryLines(1)

    fun ch2MapIntroStoryLines(): List<String> = chapterMapIntroStoryLines(2)

    fun ch3MapIntroStoryLines(): List<String> = chapterMapIntroStoryLines(3)

    fun ch4MapIntroStoryLines(): List<String> = chapterMapIntroStoryLines(4)

    fun ch5MapIntroStoryLines(): List<String> = chapterMapIntroStoryLines(5)

    fun ch6MapIntroStoryLines(): List<String> = chapterMapIntroStoryLines(6)

    fun ch7MapIntroStoryLines(): List<String> = chapterMapIntroStoryLines(7)

    fun chapterMapIntroStoryLines(chapterIndex: Int): List<String> =
        when (chapterIndex) {
            1 ->
                listOf(
                    rtl("במפה הזאת מסתתר דינוזאור גדול עם שיניים חדות ושאגה חזקה."),
                    rtl("בואו נגלה מי זה!"),
                )
            2 ->
                listOf(
                    rtl("במפה הזאת מסתתר דינוזאור עם שלוש קרניים חזקות."),
                    rtl("בואו נגלה מי זה!"),
                )
            3 ->
                listOf(
                    rtl("במפה הזאת מסתתר דינוזאור עם לוחות גדולים על הגב."),
                    rtl("בואו נגלה מי זה!"),
                )
            4 ->
                listOf(
                    rtl("במפה הזאת מסתתר דינוזאור עם צוואר ארוך מאוד, שמגיע עד העלים הגבוהים."),
                    rtl("בואו נגלה מי זה!"),
                )
            5 ->
                listOf(
                    rtl("במפה הזאת מסתתר דינוזאור עם שריון חזק וזנב כבד."),
                    rtl("בואו נגלה מי זה!"),
                )
            6 ->
                listOf(
                    rtl("במפה הזאת מסתתר יצור ימי קדום וענקי, שחי מתחת לגלים."),
                    rtl("בואו נגלה מי זה!"),
                )
            7 ->
                listOf(
                    rtl("במפה הזאת מסתתר יצור פרהיסטורי ענק שעף בשמיים."),
                    rtl("בואו נגלה מי זה!"),
                )
            else -> error("Unknown chapter intro: $chapterIndex")
        }

    fun firstRevealMapCaption(): String = rtl("חלק ראשון נחשף! ממשיכים לגלות…")

    fun seasonIntroContinueLabel(): String = rtl("בואו נגלה!")

    /** Shown only after chapter completion — identity reveal. */
    fun revealedDinosaurName(chapterId: Int): String? = Season2ChapterRegistry.revealedName(chapterId)

    const val MemoryMatchInstruction: String = "מצאו זוגות של אותיות זהות"

    /** Compact phone-first chapter map intro (story, no name reveal). */
    fun mapIntroStoryLines(playerAddress: PlayerAddress): List<String> =
        listOf(
            rtl("מצאנו את המפה הראשונה."),
            rtl("משהו גדול מסתתר מאחורי הערפל…"),
            rtl("עוד אי אפשר לדעת מי זה."),
            rtl(mapIntroDiscoverAskLine(playerAddress)),
        )

    fun mapIntroLinesCompact(playerAddress: PlayerAddress): List<String> = mapIntroStoryLines(playerAddress)

    fun isChapterComplete(
        chapterIndex: Int,
        completedChapters: Set<Int>,
        completedStations: Set<Int>,
    ): Boolean =
        Season2ChapterRegistry.isChapterComplete(
            chapterIndex = chapterIndex,
            completedChapters = completedChapters,
            completedStations = completedStations,
        )

    fun replayTileInstruction(): String = rtl(ReplayTileInstruction)

    /** Brief caption after returning from a station (not the full intro). */
    fun returnCaptionAfterStation(completedStationCount: Int): String? =
        when (completedStationCount) {
            in 1..4 -> rtl("עוד חלק מהמפה התגלה!")
            5 -> rtl("אנחנו מתקרבים!")
            else -> null
        }

    /** @deprecated Legacy single clip; map return uses [com.tal.hebrewdino.ui.audio.Season2CompanionFeedbackAudio]. */
    @RawRes
    fun returnCaptionVoiceRawRes(completedStationCount: Int): Int? =
        when (completedStationCount) {
            in 1..5 -> Season2RawAudio.MapPartRevealed
            else -> null
        }

    @RawRes
    fun replayTileInstructionVoiceRawRes(): Int = Season2RawAudio.ReplayTileInstruction

    private fun mapIntroDiscoverAskLine(playerAddress: PlayerAddress): String =
        when (playerAddress) {
            PlayerAddress.Boy -> "תעזור לי לגלות מי מסתתר כאן?"
            PlayerAddress.Girl -> "תעזרי לי לגלות מי מסתתר כאן?"
        }

    private fun rtl(text: String): String = "\u200F$text"
}
