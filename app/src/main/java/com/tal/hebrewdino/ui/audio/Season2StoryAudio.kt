package com.tal.hebrewdino.ui.audio

import androidx.annotation.RawRes
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.domain.Season2Copy

/** Shared Season 2 story narration clips in `res/raw` (neutral — same for Dino and Dina). */
object Season2StoryAudio {
    @RawRes val StoryIntro: Int = R.raw.season2_story_intro_01

    @RawRes val PuzzleMapExplain: Int = R.raw.season2_puzzle_map_explain_01

    @RawRes val FirstReveal: Int = R.raw.season2_first_reveal_01

    @RawRes val SeasonCompleteSummary: Int = R.raw.season2_complete_summary_01

    private val ChapterCompleteByIndex: IntArray =
        intArrayOf(
            R.raw.season2_ch1_complete_01,
            R.raw.season2_ch2_complete_01,
            R.raw.season2_ch3_complete_01,
            R.raw.season2_ch4_complete_01,
            R.raw.season2_ch5_complete_01,
            R.raw.season2_ch6_complete_01,
            R.raw.season2_ch7_complete_01,
        )

    /** Optional chapter clue intros — wired only when the raw file is present in the build. */
    private val OptionalChapterIntroByIndex: IntArray =
        intArrayOf(
            R.raw.season2_ch1_intro_01,
            R.raw.season2_ch2_intro_01,
            R.raw.season2_ch3_intro_01,
            R.raw.season2_ch4_intro_01,
            R.raw.season2_ch5_intro_01,
            R.raw.season2_ch6_intro_01,
            R.raw.season2_ch7_intro_01,
        )

    val requiredStoryRawResIds: List<Int> =
        listOf(
            StoryIntro,
            PuzzleMapExplain,
            FirstReveal,
        ) + ChapterCompleteByIndex.toList()

    val optionalChapterIntroRawResIds: List<Int> = OptionalChapterIntroByIndex.toList()

    fun chapterCompleteRawRes(chapterId: Int): Int {
        require(chapterId in 1..ChapterCompleteByIndex.size) {
            "Season2StoryAudio: missing required chapter complete raw for ch$chapterId"
        }
        return ChapterCompleteByIndex[chapterId - 1]
    }

    /** Returns the optional chapter intro raw id when that file is wired for this build. */
    fun optionalChapterIntroRawRes(chapterId: Int): Int? {
        if (chapterId !in 1..OptionalChapterIntroByIndex.size) return null
        return OptionalChapterIntroByIndex[chapterId - 1]
    }

    fun firstRevealCaption(): String = Season2Copy.firstRevealMapCaption()

    /**
     * Puzzle-map explanation: once per chapter on first hidden-map entry (no stations done yet),
     * not while the chapter story overlay is visible and not after station returns.
     */
    fun shouldPlayPuzzleMapExplain(
        showChapterIntroOverlay: Boolean,
        completedStationCount: Int,
        puzzleMapExplainHeard: Boolean,
    ): Boolean =
        !showChapterIntroOverlay &&
            completedStationCount == 0 &&
            !puzzleMapExplainHeard

    /**
     * Represents the voice narration and transcript for the puzzle map reveal.
     */
    sealed class MapReturnVoice {
        @get:RawRes abstract val rawResId: Int
        abstract val caption: String
        abstract val isFirstReveal: Boolean

        data class FirstReveal(
            @get:RawRes override val rawResId: Int,
            override val caption: String,
        ) : MapReturnVoice() {
            override val isFirstReveal: Boolean = true
        }

        /** Companion-specific praise for subsequent piece reveals (stations 2-5). */
        data class CompanionPraise(
            @get:RawRes override val rawResId: Int,
            override val caption: String,
        ) : MapReturnVoice() {
            override val isFirstReveal: Boolean = false
        }
    }

    /** First station return uses shared first-reveal narration; later returns use companion map praise. */
    fun mapReturnVoice(
        completedCount: Int,
        companion: DinoCharacter,
        avoidPraiseRawResId: Int,
    ): MapReturnVoice? {
        if (completedCount !in 1..5) return null
        if (completedCount == 1) {
            return MapReturnVoice.FirstReveal(
                rawResId = FirstReveal,
                caption = firstRevealCaption(),
            )
        }
        val praiseRes =
            Season2CompanionFeedbackAudio.pickMapReturnPraise(
                companion = companion,
                avoidRawResId = avoidPraiseRawResId,
            )
        return MapReturnVoice.CompanionPraise(
            rawResId = praiseRes,
            caption = Season2CompanionFeedbackAudio.mapPraiseCaption(praiseRes),
        )
    }
}
