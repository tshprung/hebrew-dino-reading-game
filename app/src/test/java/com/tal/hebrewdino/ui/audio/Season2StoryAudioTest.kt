package com.tal.hebrewdino.ui.audio

import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.domain.Season2IntroFlow
import com.tal.hebrewdino.ui.domain.Season2NavKeys
import com.tal.hebrewdino.ui.domain.Season2StabilityAudit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2StoryAudioTest {
    @Test
    fun requiredStoryRawResIds_allResolve() {
        Season2StoryAudio.requiredStoryRawResIds.forEach { rawResId ->
            assertNotEquals("required story raw must not be 0", 0, rawResId)
        }
        assertEquals(R.raw.season2_story_intro_01, Season2StoryAudio.StoryIntro)
        assertEquals(R.raw.season2_puzzle_map_explain_01, Season2StoryAudio.PuzzleMapExplain)
        assertEquals(R.raw.season2_first_reveal_01, Season2StoryAudio.FirstReveal)
    }

    @Test
    fun chapterCompleteRawRes_mapsCh1ThroughCh6() {
        assertEquals(R.raw.season2_ch1_complete_01, Season2StoryAudio.chapterCompleteRawRes(1))
        assertEquals(R.raw.season2_ch2_complete_01, Season2StoryAudio.chapterCompleteRawRes(2))
        assertEquals(R.raw.season2_ch3_complete_01, Season2StoryAudio.chapterCompleteRawRes(3))
        assertEquals(R.raw.season2_ch4_complete_01, Season2StoryAudio.chapterCompleteRawRes(4))
        assertEquals(R.raw.season2_ch5_complete_01, Season2StoryAudio.chapterCompleteRawRes(5))
        assertEquals(R.raw.season2_ch6_complete_01, Season2StoryAudio.chapterCompleteRawRes(6))
    }

    @Test
    fun optionalChapterIntroRawRes_wiredWhenPresent() {
        for (chapterId in 1..6) {
            val rawResId = Season2StoryAudio.optionalChapterIntroRawRes(chapterId)
            assertNotNull("optional ch$chapterId intro should be wired when file exists", rawResId)
            assertNotEquals(0, rawResId!!)
        }
        assertEquals(6, Season2StoryAudio.optionalChapterIntroRawResIds.size)
    }

    @Test
    fun seasonIntroRoute_usesStoryIntroRaw() {
        assertTrue(Season2IntroFlow.shouldShowSeasonIntro(entryFromSeasons = true))
        assertEquals(R.raw.season2_story_intro_01, Season2StoryAudio.StoryIntro)
    }

    @Test
    fun puzzleMapExplain_onlyOnInitialHiddenMapEntry() {
        assertTrue(
            Season2StoryAudio.shouldPlayPuzzleMapExplain(
                showChapterIntroOverlay = false,
                completedStationCount = 0,
                puzzleMapExplainHeard = false,
            ),
        )
        assertFalse(
            Season2StoryAudio.shouldPlayPuzzleMapExplain(
                showChapterIntroOverlay = true,
                completedStationCount = 0,
                puzzleMapExplainHeard = false,
            ),
        )
        assertFalse(
            Season2StoryAudio.shouldPlayPuzzleMapExplain(
                showChapterIntroOverlay = false,
                completedStationCount = 1,
                puzzleMapExplainHeard = false,
            ),
        )
        assertFalse(
            Season2StoryAudio.shouldPlayPuzzleMapExplain(
                showChapterIntroOverlay = false,
                completedStationCount = 0,
                puzzleMapExplainHeard = true,
            ),
        )
    }

    @Test
    fun firstReveal_playsOnceOnFirstStationReturn_notCompanionPraise() {
        val first =
            Season2StoryAudio.mapReturnVoice(
                completedCount = 1,
                companion = DinoCharacter.Dino,
                avoidPraiseRawResId = 0,
            )
        assertNotNull(first)
        assertTrue(first!!.isFirstReveal)
        assertEquals(R.raw.season2_first_reveal_01, first.rawResId)

        val second =
            Season2StoryAudio.mapReturnVoice(
                completedCount = 2,
                companion = DinoCharacter.Dino,
                avoidPraiseRawResId = 0,
            )
        assertNotNull(second)
        assertFalse(second!!.isFirstReveal)
        assertNotEquals(R.raw.season2_first_reveal_01, second.rawResId)
    }

    @Test
    fun companionMapPraise_stillWorksAfterLaterStationReturns() {
        val dinaSecond =
            Season2StoryAudio.mapReturnVoice(
                completedCount = 3,
                companion = DinoCharacter.Dina,
                avoidPraiseRawResId = 0,
            )
        assertNotNull(dinaSecond)
        assertFalse(dinaSecond!!.isFirstReveal)
        assertTrue(
            Season2CompanionFeedbackAudio.mapPraiseCaption(dinaSecond.rawResId).isNotBlank(),
        )
    }

    @Test
    fun navKeys_unchangedForStoryIntroGate() {
        assertEquals("s2_show_season_intro", Season2NavKeys.SHOW_SEASON_INTRO)
        assertEquals("s2_map_return_caption_count", Season2NavKeys.MAP_RETURN_CAPTION_COUNT)
    }

    @Test
    fun storyAudio_usesResRawMp3_notAssetsWav() {
        assertFalse(Season2StabilityAudit.audioClipsContainsSeason2WavPath())
        Season2StoryAudio.requiredStoryRawResIds.forEach { assertNotEquals(0, it) }
    }

    @Test
    fun storyAudio_noLegacyDinoMomMapAssetsRestored() {
        assertEquals(2, Season2StabilityAudit.legacyUnusedMapRawResIds().size)
    }
}
