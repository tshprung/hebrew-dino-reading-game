package com.tal.hebrewdino.ui.domain

import androidx.annotation.DrawableRes
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.data.PlayerAddress

enum class Season2StationTheme {
    Standard,
    Footprints,
    StegosaurusPlates,
    HighLeaves,
    LetterArmor,
    UnderwaterBubbles,
}

enum class Season2ChapterLockReason {
    QaReady,
    MissingAssets,
    NotImplemented,
}

data class Season2ChapterDefinition(
    val chapterIndex: Int,
    val dinosaurNameHebrew: String,
    val mysteryCreatureLabel: String = "דינוזאור מסתורי",
    @param:DrawableRes val posterPuzzleResId: Int?,
    val letters: List<String>,
    val wordCatalogIds: List<String>,
    val memoryMatchLetters: List<String>,
    val letterPoolSpec: LetterPoolSpec,
    val stationTheme: Season2StationTheme,
    val gameplayChapterId: Int,
    val mapIntroStoryLines: (PlayerAddress) -> List<String>,
    val learningFocus: String,
    val missingAssetsReport: List<String>,
    val lockReason: Season2ChapterLockReason,
) {
    val validation: Season2ChapterAssetValidator.ValidationResult =
        Season2ChapterAssetValidator.validate(
            posterResId = posterPuzzleResId,
            letters = letters,
            wordCatalogIds = wordCatalogIds,
            forbidTrexPoster = chapterIndex != 1,
        )

    val isQaReady: Boolean =
        lockReason == Season2ChapterLockReason.QaReady && validation.qaReady

    val isPlayable: Boolean = isQaReady

    fun posterResForUi(): Int? = if (isQaReady) posterPuzzleResId else null

    val stationContext: Season2ChapterStationPlans.ChapterStationContext? =
        Season2ChapterStationPlans.contextFor(chapterIndex)
}

/** Authoritative Season 2 chapter content registry. */
object Season2ChapterRegistry {
    const val CHAPTER_COUNT: Int = 6

    val chapters: List<Season2ChapterDefinition> by lazy { buildChapters() }

    fun chapter(chapterIndex: Int): Season2ChapterDefinition? =
        chapters.find { it.chapterIndex == chapterIndex }

    fun playableChapterIndices(): List<Int> = chapters.filter { it.isPlayable }.map { it.chapterIndex }

    fun isPlayable(chapterIndex: Int): Boolean = chapter(chapterIndex)?.isPlayable == true

    fun revealedName(chapterIndex: Int): String? =
        chapter(chapterIndex)?.takeIf { it.isQaReady }?.dinosaurNameHebrew

    fun posterResId(chapterIndex: Int): Int? = chapter(chapterIndex)?.posterResForUi()

    fun missingAssetsForChapter(chapterIndex: Int): List<String> {
        val def = chapter(chapterIndex) ?: return listOf("unknown chapter $chapterIndex")
        return if (def.isQaReady) def.validation.missingAssets else def.missingAssetsReport
    }

    fun isChapterComplete(
        chapterIndex: Int,
        completedChapters: Set<Int>,
        completedStations: Set<Int>,
    ): Boolean {
        if (chapterIndex in completedChapters) return true
        if (!isPlayable(chapterIndex)) return false
        return completedStations.size >= Season2StandardRevealOrder.STATION_COUNT
    }

    fun isChapterUnlocked(chapterIndex: Int, completedChapters: Set<Int>): Boolean {
        if (!isPlayable(chapterIndex)) return false
        if (chapterIndex == 1) return true
        return (chapterIndex - 1) in completedChapters
    }

    private fun buildChapters(): List<Season2ChapterDefinition> =
        listOf(
            qaReadyChapter(
                chapterIndex = 1,
                dinosaurNameHebrew = "טירנוזאורוס",
                posterPuzzleResId = R.drawable.season2_trex_puzzle_full,
                letters = listOf("ז", "י", "ס", "ע", "מ", "ל"),
                wordCatalogIds =
                    listOf(
                        "w_ז_1", "w_ז_2", "w_ז_3",
                        "w_י_3", "w_י_4",
                        "w_ס_1", "w_ס_4",
                        "w_ע_1", "w_ע_7",
                        "w_מ_2", "w_מ_3",
                        "w_ל_1", "w_ל_2",
                    ),
                memoryMatchLetters = listOf("ז", "י", "ס", "ע", "מ", "ל"),
                letterPoolSpec = Season2Chapter1LetterPoolSpec,
                stationTheme = Season2StationTheme.Standard,
                gameplayChapterId = Season2ChapterIds.Chapter1Tyrannosaurus,
                mapIntroStoryLines = { address -> Season2Copy.mapIntroStoryLines(address) },
                learningFocus = "אותיות ז י ס ע + מילים מוכרות",
            ),
            qaReadyChapter(
                chapterIndex = 2,
                dinosaurNameHebrew = "טריצרטופס",
                posterPuzzleResId = R.drawable.season2_triceratops_puzzle_full,
                letters = listOf("ח", "ר", "ק", "ש", "מ"),
                wordCatalogIds =
                    listOf(
                        "w_ח_1", "w_ח_2", "w_ח_3",
                        "w_ר_1", "w_ר_3", "w_ר_4",
                        "w_ק_1", "w_ק_2", "w_ק_3",
                        "w_ש_1", "w_ש_2", "w_ש_4",
                        "w_מ_2", "w_מ_3",
                        "w_פ_2",
                    ),
                memoryMatchLetters = listOf("ח", "ר", "ק", "ש", "מ"),
                letterPoolSpec = Season2Chapter2LetterPoolSpec,
                stationTheme = Season2StationTheme.Footprints,
                gameplayChapterId = Season2ChapterIds.Chapter2Triceratops,
                mapIntroStoryLines = { address -> ch2MapIntroLines(address) },
                learningFocus = "אותיות ח ר ק ש + מילים מוחשיות",
            ),
            wiredChapter(
                index = 3,
                name = "סטגוזאורוס",
                poster = R.drawable.season2_stegosaurus_puzzle_full,
                letters = Season2ChapterContent.ch3Letters,
                words = Season2ChapterContent.ch3Words,
                letterPoolSpec = Season2Chapter3LetterPoolSpec,
                theme = Season2StationTheme.StegosaurusPlates,
                focus = "חלקי מילה + אותיות ג נ פ צ",
                mapIntro = { address -> ch3MapIntroLines(address) },
            ),
            wiredChapter(
                index = 4,
                name = "ברכיוזאורוס",
                poster = R.drawable.season2_brachiosaurus_puzzle_full,
                letters = Season2ChapterContent.ch4Letters,
                words = Season2ChapterContent.ch4Words,
                letterPoolSpec = Season2Chapter4LetterPoolSpec,
                theme = Season2StationTheme.HighLeaves,
                focus = "תמונה→מילה, אותיות ב ד ת כ",
                mapIntro = { address -> ch4MapIntroLines(address) },
            ),
            wiredChapter(
                index = 5,
                name = "אנקילוזאורוס",
                poster = R.drawable.season2_ankylosaurus_puzzle_full,
                letters = Season2ChapterContent.ch5Letters,
                words = Season2ChapterContent.ch5Words,
                letterPoolSpec = Season2Chapter5LetterPoolSpec,
                theme = Season2StationTheme.LetterArmor,
                focus = "אות ראשונה חסרה — חזרה על מילים",
                mapIntro = { address -> ch5MapIntroLines(address) },
            ),
            wiredChapter(
                index = 6,
                name = "מוזאזאורוס",
                mysteryLabel = "יצור ימי קדום",
                poster = R.drawable.season2_mosasaurus_puzzle_full,
                letters = Season2ChapterContent.ch6Letters,
                words = Season2ChapterContent.ch6Words,
                letterPoolSpec = Season2Chapter6LetterPoolSpec,
                theme = Season2StationTheme.UnderwaterBubbles,
                focus = "חזרה מעורבבת + חרוזים",
                mapIntro = { address -> ch6MapIntroLines(address) },
            ),
        )

    private fun qaReadyChapter(
        chapterIndex: Int,
        dinosaurNameHebrew: String,
        posterPuzzleResId: Int,
        letters: List<String>,
        wordCatalogIds: List<String>,
        memoryMatchLetters: List<String>,
        letterPoolSpec: LetterPoolSpec,
        stationTheme: Season2StationTheme,
        gameplayChapterId: Int,
        mapIntroStoryLines: (PlayerAddress) -> List<String>,
        learningFocus: String,
    ): Season2ChapterDefinition =
        Season2ChapterDefinition(
            chapterIndex = chapterIndex,
            dinosaurNameHebrew = dinosaurNameHebrew,
            posterPuzzleResId = posterPuzzleResId,
            letters = letters,
            wordCatalogIds = wordCatalogIds,
            memoryMatchLetters = memoryMatchLetters,
            letterPoolSpec = letterPoolSpec,
            stationTheme = stationTheme,
            gameplayChapterId = gameplayChapterId,
            mapIntroStoryLines = mapIntroStoryLines,
            learningFocus = learningFocus,
            missingAssetsReport = emptyList(),
            lockReason = Season2ChapterLockReason.QaReady,
        )

    private fun wiredChapter(
        index: Int,
        name: String,
        mysteryLabel: String = "דינוזאור מסתורי",
        poster: Int,
        letters: List<String>,
        words: List<String>,
        letterPoolSpec: Season2ChapterLetterPool,
        theme: Season2StationTheme,
        focus: String,
        mapIntro: (PlayerAddress) -> List<String>,
    ): Season2ChapterDefinition {
        val ctx = Season2ChapterStationPlans.contextFor(index)!!
        val readiness =
            Season2ChapterStationValidator.evaluate(
                chapterIndex = index,
                posterResId = poster,
                letters = letters,
                wordCatalogIds = words,
                memoryMatchLetters = ctx.memoryMatchLetters,
                letterPoolSpec = letterPoolSpec,
                stationContext = ctx,
            )
        return Season2ChapterDefinition(
            chapterIndex = index,
            dinosaurNameHebrew = name,
            mysteryCreatureLabel = mysteryLabel,
            posterPuzzleResId = poster,
            letters = letters,
            wordCatalogIds = words,
            memoryMatchLetters = ctx.memoryMatchLetters,
            letterPoolSpec = letterPoolSpec,
            stationTheme = theme,
            gameplayChapterId = Season2ChapterIds.chapterGameplayId(index),
            mapIntroStoryLines = mapIntro,
            learningFocus = focus,
            missingAssetsReport = readiness.issues,
            lockReason = readiness.lockReason,
        )
    }

    private fun ch2MapIntroLines(playerAddress: PlayerAddress): List<String> =
        listOf(
            "\u200Fמצאנו עקבות בחול!",
            "\u200Fהן מובילות למפה מסתורית שנייה…",
            "\u200Fמשהו עם קרניים מסתתר כאן.",
            mapIntroDiscoverAskLine(playerAddress),
        )

    private fun ch3MapIntroLines(playerAddress: PlayerAddress): List<String> =
        listOf(
            "\u200Fמצאנו מפה עם לוחות מוזרים…",
            "\u200Fאולי הם שייכים לדינוזאור עם קוצים על הגב.",
            mapIntroDiscoverAskLine(playerAddress),
        )

    private fun ch4MapIntroLines(playerAddress: PlayerAddress): List<String> =
        listOf(
            "\u200Fהמפה הבאה מובילה לעצים גבוהים…",
            "\u200Fמי מגיע עד העלים?",
            mapIntroDiscoverAskLine(playerAddress),
        )

    private fun ch5MapIntroLines(playerAddress: PlayerAddress): List<String> =
        listOf(
            "\u200Fמצאנו סימנים של שריון חזק…",
            "\u200Fמי מסתתר מאחורי המפה הזאת?",
            mapIntroDiscoverAskLine(playerAddress),
        )

    private fun ch6MapIntroLines(playerAddress: PlayerAddress): List<String> =
        listOf(
            "\u200Fהמפה האחרונה מובילה אל הים…",
            "\u200Fמשהו גדול שוחה שם מתחת למים.",
            mapIntroDiscoverAskLine(playerAddress),
        )

    private fun mapIntroDiscoverAskLine(playerAddress: PlayerAddress): String =
        when (playerAddress) {
            PlayerAddress.Boy -> "\u200Fתעזור לי לגלות מי זה?"
            PlayerAddress.Girl -> "\u200Fתעזרי לי לגלות מי זה?"
        }
}
