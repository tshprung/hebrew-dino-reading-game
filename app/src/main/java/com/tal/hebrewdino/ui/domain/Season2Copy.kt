package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.ui.audio.AudioClips
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
    const val PuzzleHint: String = "פתרו את המשחקים וגלו את הדינוזאור!"
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
            rtl("דינו ודינה טיילו ביער, ופתאום ראו משהו נוצץ בין העלים."),
            rtl("זאת הייתה מפה עתיקה!"),
            rtl("אולי מסתתרים כאן דינוזאורים חדשים…"),
            rtl("בואו נפתור משחקים ונראה מי מסתתר!"),
        )

    fun seasonIntroLines(): List<String> = seasonIntroStoryLines()

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

    fun mapIntroNeutralFallback(): String = rtl("בואו נגלה מי מסתתר כאן.")

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

    fun mapIntroLines(playerAddress: PlayerAddress): List<String> = mapIntroLinesCompact(playerAddress)

    fun replayTileInstruction(): String = rtl(ReplayTileInstruction)

    /** Brief caption after returning from a station (not the full intro). */
    fun returnCaptionAfterStation(completedStationCount: Int): String? =
        when (completedStationCount) {
            in 1..4 -> rtl("עוד חלק מהמפה התגלה!")
            5 -> rtl("אנחנו מתקרבים!")
            else -> null
        }

    /** Approved voice asset path for [returnCaptionAfterStation], when recorded. */
    fun returnCaptionVoiceAsset(completedStationCount: Int): String? =
        when (completedStationCount) {
            in 1..4 -> AudioClips.Season2MapPartRevealed
            5 -> AudioClips.Season2MapAlmostDone
            else -> null
        }

    fun replayTileInstructionVoiceAsset(): String = AudioClips.Season2ReplayTileInstruction

    private fun mapIntroDiscoverAskLine(playerAddress: PlayerAddress): String =
        when (playerAddress) {
            PlayerAddress.Boy -> "תעזור לי לגלות מי מסתתר כאן?"
            PlayerAddress.Girl -> "תעזרי לי לגלות מי מסתתר כאן?"
        }

    private fun rtl(text: String): String = "\u200F$text"
}
