package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2Ch1QaPolishBatchTest {
    @Test
    fun ch1_st1_wrong_single_tryagain() {
        assertTrue(Season2Ch1QaPolicy.shouldPlayTryAgainInPopBalloonsSfx(season2QuizBalloons = true))
        assertTrue(Season2Ch1QaPolicy.shouldPlayTryAgainInPopBalloonsSfx(season2QuizBalloons = false))
        val source = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/PopBalloonsActions.kt")
        assertTrue(source.contains("shouldPlayTryAgainInPopBalloonsSfx"))
    }

    @Test
    fun ch1_completed_map_entry_voice_only_from_chapter_list_not_station_return() {
        assertTrue(
            Season2Ch1QaPolicy.shouldUseCompletedReplayTilesEntryVoice(
                chapterFullyRevealed = true,
                entryFromChapterSelect = true,
            ),
        )
        assertFalse(
            Season2Ch1QaPolicy.shouldUseCompletedReplayTilesEntryVoice(
                chapterFullyRevealed = true,
                entryFromChapterSelect = false,
            ),
        )
        assertFalse(
            Season2MapEntryVoicePolicy.shouldOrchestrateMapEntryVoice(
                progressHydrated = true,
                showChapterIntroOverlay = false,
                entryFromChapterSelect = false,
                mapReturnCaptionEvent = 0L,
                mapEntryInstructionSpoken = false,
                suppressBecauseStationReturn = false,
            ),
        )
        assertEquals(
            R.raw.season2_map_entry_replay_tiles_01,
            Season2MapEntryVoicePolicy.mapEntryInstructionRawRes(
                chapterId = 1,
                chapterFullyRevealed = true,
                nextPlayablePosterTile = null,
                entryFromChapterSelect = true,
            ),
        )
    }

    @Test
    fun chapter_intro_skipped_when_fully_solved() {
        for (chapterId in 1..6) {
            assertFalse(
                Season2IntroFlow.shouldShowChapterIntro(
                    chapterId = chapterId,
                    entryFromChapterSelect = true,
                    chapterFullyRevealed = true,
                ),
            )
            assertTrue(
                Season2IntroFlow.shouldShowChapterIntro(
                    chapterId = chapterId,
                    entryFromChapterSelect = true,
                    chapterFullyRevealed = false,
                ),
            )
        }
    }

    @Test
    fun ch1_st3_single_completion_praise() {
        assertTrue(
            Season2StationQaPolicy.shouldSkipAdvanceRoundPraiseBecausePlayedInStation(Season2ChapterIds.Chapter1Tyrannosaurus, 
                Season2Chapter1StationOrder.PICTURE_STARTS_WITH,
            ),
        )
        val advance = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/AdvanceAfterRoundActions.kt")
        assertTrue(advance.contains("shouldSkipAdvanceRoundPraiseBecausePlayedInStation"))
    }

    @Test
    fun ch1_st3_replay_sequence_instruction_then_word_short_gap_policy() {
        assertEquals(60L, Season2Ch1QaPolicy.CoachInstructionToWordGapMs)
        val coach = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/domain/Season2GuessingCoach.kt")
        assertTrue(coach.contains("CoachInstructionToWordGapMs"))
        assertTrue(coach.contains("postInstructionGapMs = Season2Ch1QaPolicy.CoachInstructionToWordGapMs"))
    }

    @Test
    fun ch1_st4_background_present_readability() {
        val source = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/Season2MemoryMatchStationScreen.kt")
        assertTrue(source.contains("season2_memory_match_bg"))
        assertTrue(source.contains("ContentScale.Crop"))
    }

    @Test
    fun ch1_st5_every_tap_reads_tapped_word() {
        val host = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/GameQuestionHost.kt")
        assertTrue(host.contains("Season2StationQaPolicy.isWhichWordStartsWithStation"))
        assertTrue(host.contains("handleImageToWordWordPressed"))
    }

    @Test
    fun ch1_st5_no_long_blank_interround() {
        assertTrue(
            Season2StationQaPolicy.shouldSkipAdvanceRoundInterRoundFeedback(
                Season2ChapterIds.Chapter1Tyrannosaurus,
                Season2Chapter1StationOrder.WHICH_WORD_STARTS_WITH,
                isLast = false,
            ),
        )
        assertTrue(Season2StationQaPolicy.useTightBetweenRoundTiming(Season2ChapterIds.Chapter1Tyrannosaurus, Season2Chapter1StationOrder.WHICH_WORD_STARTS_WITH))
    }

    @Test
    fun ch1_st6_dragMissingLetter_pictureTapReplayWired() {
        val gameScreen = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/GameScreen.kt")
        assertTrue(gameScreen.contains("handleDragMissingLetterPictureTap"))
        assertFalse(
            Season2StationQaPolicy.isPictureToWordStation(
                Season2ChapterIds.Chapter1Tyrannosaurus,
                Season2Chapter1StationOrder.FINALE_STATION,
            ),
        )
    }

    @Test
    fun ch1_st6_dragMissingLetter_usesGenericFeedback() {
        val actions = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/DragMissingLetterActions.kt")
        assertTrue(actions.contains("AudioClips.SfxCorrect"))
        assertTrue(actions.contains("AudioClips.SfxWrong"))
    }

    @Test
    fun ch1_st6_single_correct_praise_guard() {
        val session = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/domain/LevelSession.kt")
        assertTrue(session.contains("dragMissingLetterRoundScored"))
    }

    @Test
    fun ch1_st6_layout_nudge_constants() {
        assertEquals(19, Season2Ch1QaPolicy.FinaleExtraDownDp.value.toInt())
        assertEquals(120, Season2Ch1QaPolicy.FinaleDinoReservedWidthDp.value.toInt())
    }

    @Test
    fun season1_unchanged() {
        assertEquals(StationQuizMode.PickLetter, StationQuizPlans.chapter1(Chapter1StationOrder.TAP_LETTER).mode)
    }

    private fun readProjectSource(relativePath: String): String {
        val candidates =
            listOf(
                java.io.File(relativePath),
                java.io.File("../$relativePath"),
                java.io.File("../../$relativePath"),
            )
        val file =
            candidates.firstOrNull { it.exists() }
                ?: error("Could not locate source file: $relativePath")
        return file.readText()
    }
}
