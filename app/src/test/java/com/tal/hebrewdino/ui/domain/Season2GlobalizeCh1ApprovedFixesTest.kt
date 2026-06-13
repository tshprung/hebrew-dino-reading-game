package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.ui.data.PlayerAddress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2GlobalizeCh1ApprovedFixesTest {
    private val expectedIntroBodies =
        mapOf(
            1 to "במפה הזאת מסתתר דינוזאור גדול עם שיניים חדות ושאגה חזקה. בואו נגלה מי זה!",
            2 to "במפה הזאת מסתתר דינוזאור עם שלוש קרניים חזקות. בואו נגלה מי זה!",
            3 to "במפה הזאת מסתתר דינוזאור עם לוחות גדולים על הגב. בואו נגלה מי זה!",
            4 to "במפה הזאת מסתתר דינוזאור עם צוואר ארוך מאוד, שמגיע עד העלים הגבוהים. בואו נגלה מי זה!",
            5 to "במפה הזאת מסתתר דינוזאור עם שריון חזק וזנב כבד. בואו נגלה מי זה!",
            6 to "במפה הזאת מסתתר יצור ימי קדום וענקי, שחי מתחת לגלים. בואו נגלה מי זה!",
            7 to "במפה הזאת מסתתר יצור פרהיסטורי ענק שעף בשמיים. בואו נגלה מי זה!",
        )

    @Test
    fun S2_all_chapter_intro_texts_match_expected() {
        (1..7).forEach { chapterIndex ->
            val lines = Season2Copy.chapterMapIntroStoryLines(chapterIndex)
            assertEquals(2, lines.size)
            val combined = lines.joinToString(" ") { it.replace("\u200F", "") }
            assertEquals(expectedIntroBodies[chapterIndex], combined)
            assertFalse(lines.any { line -> line.any { c -> c.code in 0x0591..0x05C7 } })
        }
        (1..7).forEach { chapterIndex ->
            val registryLines =
                Season2ChapterRegistry.chapter(chapterIndex)!!.mapIntroStoryLines(PlayerAddress.Boy)
            assertEquals(Season2Copy.chapterMapIntroStoryLines(chapterIndex), registryLines)
            assertEquals(registryLines, Season2ChapterRegistry.chapter(chapterIndex)!!.mapIntroStoryLines(PlayerAddress.Girl))
        }
    }

    @Test
    fun Ch2_St2_correct_praise_not_clipped_policy() {
        val gameplayChapterId = 1
        val uxStationId = Season2Chapter1StationOrder.PICK_LETTER
        assertEquals(
            Season2ChapterStationPlans.StationKind.PickLetter,
            Season2StationQaPolicy.stationKind(gameplayChapterId, uxStationId),
        )
        assertTrue(
            Season2StationQaPolicy.shouldSkipAdvanceRoundInterRoundFeedback(
                gameplayChapterId = gameplayChapterId,
                season2UxStationId = uxStationId,
                isLast = false,
            ),
        )
        assertTrue(
            Season2StationQaPolicy.shouldSkipAdvanceRoundInterRoundFeedback(
                gameplayChapterId = gameplayChapterId,
                season2UxStationId = uxStationId,
                isLast = true,
            ),
        )
        assertTrue(Season2StationQaPolicy.shouldSuppressSagaEpisodeAdvancePraise(isSeason2QuizChapter = true))
        assertFalse(Season2StationQaPolicy.shouldSuppressSagaEpisodeAdvancePraise(isSeason2QuizChapter = false))
        val pick = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/PickLetterActions.kt")
        assertTrue(pick.contains("launchFeedbackVoiceNoCancel"))
        assertTrue(pick.contains("joinSilently(job)"))
        val advance = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/AdvanceAfterRoundActions.kt")
        assertTrue(advance.contains("shouldSuppressSagaEpisodeAdvancePraise"))
        assertTrue(advance.contains("isSeason2QuizChapter && sagaUsesPickLetterAudioStaging"))
        val gameScreen = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/GameScreen.kt")
        assertTrue(gameScreen.contains("isSeason2QuizChapter = isSeason2QuizChapter"))
    }

    @Test
    fun Ch2_post_focus_correct_plays_success_audio() {
        assertTrue(
            Season2PostFocusCorrectPolicy.shouldPlayCompanionPraiseOnCorrect(
                season2HadCoachIntervention = true,
            ),
        )
        val gameScreen = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/GameScreen.kt")
        assertTrue(gameScreen.contains("Season2PostFocusCorrectAudio.playBlocking"))
        assertFalse(gameScreen.contains("processPraise(chapter1PlayerAddress)"))
    }

    @Test
    fun S2_all_relevant_station_types_post_focus_correct_success() {
        val focusKinds =
            listOf(
                1 to Season2Chapter1StationOrder.POP_BALLOONS,
                1 to Season2Chapter1StationOrder.PICK_LETTER,
                1 to Season2Chapter1StationOrder.PICTURE_STARTS_WITH,
                1 to Season2Chapter1StationOrder.WHICH_WORD_STARTS_WITH,
                Season2ChapterIds.Chapter4Brachiosaurus to 5,
                Season2ChapterIds.Chapter5Ankylosaurus to 5,
                Season2ChapterIds.Chapter6Mosasaurus to 6,
            )
        focusKinds.forEach { (gameplayChapterId, uxStationId) ->
            assertTrue(
                "kind for gameplay=$gameplayChapterId ux=$uxStationId",
                Season2StationQaPolicy.stationKind(gameplayChapterId, uxStationId) != null,
            )
        }
        val sources =
            listOf(
                "app/src/main/java/com/tal/hebrewdino/ui/screens/PickLetterActions.kt",
                "app/src/main/java/com/tal/hebrewdino/ui/screens/PictureStartsWithActions.kt",
                "app/src/main/java/com/tal/hebrewdino/ui/screens/ImageMatchActions.kt",
                "app/src/main/java/com/tal/hebrewdino/ui/screens/Season2AdvancedStationActions.kt",
                "app/src/main/java/com/tal/hebrewdino/ui/screens/PopBalloonsActions.kt",
            )
        sources.forEach { path ->
            val source = readProjectSource(path)
            assertTrue("$path handles post-focus skip or praise", source.contains("shouldSkipInStationCorrectPraiseAfterCoach") || source.contains("afterCoachIntervention"))
        }
    }

    @Test
    fun no_double_praise() {
        assertTrue(
            Season2StationQaPolicy.shouldSkipAdvanceRoundPraiseBecausePlayedInStation(
                Season2ChapterIds.Chapter1Tyrannosaurus,
                Season2Chapter1StationOrder.PICK_LETTER,
            ),
        )
        assertTrue(Season2StationQaPolicy.shouldSuppressSagaEpisodeAdvancePraise(isSeason2QuizChapter = true))
        val advance = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/AdvanceAfterRoundActions.kt")
        assertTrue(advance.contains("!suppressSagaAdvancePraise"))
    }

    @Test
    fun Ch1_behavior_unchanged() {
        assertEquals(Season2Copy.ch1MapIntroStoryLines(), Season2Copy.chapterMapIntroStoryLines(1))
        assertTrue(
            Season2ChapterFlowPolicy.shouldRequestFirstTimeChapterReward(stationId = Season2Chapter1StationOrder.FINALE_STATION,
                wasStationAlreadyDone = false,
                chapterWasCompleteBefore = false,
            ),
        )
        assertTrue(
            Season2StationQaPolicy.shouldSkipAdvanceRoundPraiseBecausePlayedInStation(
                Season2ChapterIds.Chapter1Tyrannosaurus,
                Season2Chapter1StationOrder.PICK_LETTER,
            ),
        )
    }

    @Test
    fun S1_unchanged() {
        assertEquals(
            com.tal.hebrewdino.ui.domain.StationQuizMode.PickLetter,
            com.tal.hebrewdino.ui.domain.StationQuizPlans.chapter1(
                com.tal.hebrewdino.ui.domain.Chapter1StationOrder.TAP_LETTER,
            ).mode,
        )
        assertFalse(Season2StationQaPolicy.shouldSuppressSagaEpisodeAdvancePraise(isSeason2QuizChapter = false))
    }

    @Test
    fun content_progression_unchanged() {
        assertEquals(7, Season2ChapterRegistry.CHAPTER_COUNT)
        assertEquals(6, Season2StandardRevealOrder.STATION_COUNT)
        assertNotNull(Season2ChapterStationPlans.contextFor(3))
        assertNotNull(Season2ChapterStationPlans.contextFor(6))
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
