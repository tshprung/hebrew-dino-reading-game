package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2Ch1QaFixBatchTest {
    @Test
    fun parentInfo_autoShowOnlyAfterPrefsLoaded() {
        val source = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/AppNavSystemGraph.kt")
        assertTrue(source.contains("lastSeenVersionCodeFlow.first()"))
        assertFalse(source.contains("autoParentInfoChecked"))
        assertTrue(source.contains("markSeenForVersion(BuildConfig.VERSION_CODE)"))
        assertTrue(source.indexOf("markSeenForVersion") < source.indexOf("showParentInfo = false"))
    }

    @Test
    fun st1_finalPop_skipsDuplicateBalloonPraise() {
        assertFalse(
            Season2EarlyStationQaPolicy.shouldPlayBalloonPraiseOnCorrectPop(
                season2QuizBalloons = true,
                finalCorrectBalloon = true,
            ),
        )
        val source = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/PopBalloonsActions.kt")
        assertTrue(source.contains("finalCorrectBalloon = finalCorrectBalloon"))
    }

    @Test
    fun st2_st3_postFocusCorrect_skipsDuplicateInStationPraise() {
        assertTrue(Season2EarlyStationQaPolicy.shouldSkipInStationCorrectPraiseAfterCoach(season2HadCoachIntervention = true))
        assertFalse(Season2EarlyStationQaPolicy.shouldSkipInStationCorrectPraiseAfterCoach(season2HadCoachIntervention = false))
        val gameScreen = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/GameScreen.kt")
        assertTrue(gameScreen.contains("playAddressAwareTryAgainBlocking"))
        assertTrue(gameScreen.contains("season2HadCoachIntervention = true"))
    }

    @Test
    fun st3_wrongUsesSeason2QuizLetterAudioPath() {
        assertTrue(
            Season2EarlyStationQaPolicy.shouldUseSeason2PictureStartsWithWrongAudio(
                isSeason2QuizChapter = true,
                sagaEpisode = false,
                stationId = 1,
            ),
        )
    }

    @Test
    fun mapReturn_audioOnlyNoCaptionOverlay() {
        val source = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/Season2PuzzleMapPrototypeScreen.kt")
        assertTrue(source.contains("mapReturnVoiceResId"))
        assertFalse(source.contains("Season2MapReturnCaptionOverlay"))
    }

    @Test
    fun st5_wrong_playsTappedWordBeforeFeedback() {
        val source = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/ImageMatchActions.kt")
        assertTrue(source.contains("playImageToWordTappedOptionAudio"))
        assertTrue(source.contains("PICTURE_PICK_ALL"))
    }

    @Test
    fun st6_hintAndSingleCorrectPraise() {
        val gameScreen = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/GameScreen.kt")
        assertTrue(gameScreen.contains("Season2AdvancedStationMode.PictureToWord"))
        assertTrue(gameScreen.contains("performSideHelpReplay()"))
        val advance = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/AdvanceAfterRoundActions.kt")
        assertTrue(advance.contains("isPictureToWordStation"))
    }

    @Test
    fun mapEntryVoice_usesDedicatedMapEntryClips() {
        val source = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/Season2PuzzleMapPrototypeScreen.kt")
        assertTrue(source.contains("Season2MapEntryVoicePolicy.mapEntryInstructionRawRes"))
        assertFalse(source.contains("replayTileInstructionVoiceRawRes()"))
        assertFalse(source.contains("puzzleExplainStarted"))
    }

    @Test
    fun season1_unchanged() {
        val pickLetter = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/PickLetterActions.kt")
        assertFalse(pickLetter.contains("Season2Chapter1StationOrder"))
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
