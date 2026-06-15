package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.Season2RawAudio
import com.tal.hebrewdino.ui.companion.CompanionAssets
import com.tal.hebrewdino.ui.data.DinoCharacter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class Season2QaFixBatchTest {
    private val ch1 = Season2ChapterIds.Chapter1Tyrannosaurus
    private val ch2 = Season2ChapterIds.Chapter2Triceratops
    private val ch3 = Season2ChapterIds.Chapter3Stegosaurus

    @Test
    fun ch1_st6_allPictureToWordChoices_haveRawWordAudio() {
        val words = Season2ChapterRegistry.chapter(1)!!.wordCatalogIds
        repeat(12) { round ->
            val q =
                Season2AdvancedStationGenerators.pictureToWord(
                    rnd = Random(round),
                    wordCatalogIds = words,
                )
            q.choices.forEach { choice ->
                assertNotNull(
                    "missing raw word audio for ${choice.id}",
                    AudioClips.wordRawResIdByCatalogId(choice.id),
                )
            }
        }
    }

    @Test
    fun ch1_st6_pictureToWordUsesInstructionPlusWordReplayPath() {
        assertTrue(Season2StationAudio.isPictureToWordStation(ch1, 6))
        assertEquals(R.raw.instruction_image_to_word, Season2RawAudio.instructionRawResId(Season2AdvancedStationMode.PictureToWord))
        assertEquals(
            "\u200Fאיזו מילה מתאימה לתמונה?",
            Season2StationThemeCopy.pictureToWordInstruction(Season2StationTheme.Standard),
        )
    }

    @Test
    fun wordParts_catalogIdForSplit_resolvesDistractorAndCorrect() {
        assertEquals("w_ג_1", Season2WordPartsCatalog.catalogIdForSplit("ג", "מל"))
        assertEquals("w_פ_2", Season2WordPartsCatalog.catalogIdForSplit("פי", "ל"))
        assertEquals("w_ש_1", Season2WordPartsCatalog.catalogIdForSplit("ש", "מש"))
    }

    @Test
    fun wordParts_correctConfirmationUsesFullWordRawRes() {
        val catalogId = "w_ג_1"
        assertNotNull(Season2RawAudio.wordPartRawResId(catalogId, 1))
        assertNotNull(Season2RawAudio.wordPartRawResId(catalogId, 2))
        assertNotNull(AudioClips.wordRawResIdByCatalogId(catalogId))
    }

    @Test
    fun ch2_st6_and_ch3_st5_generateThreeSplitOptionsIncludingCorrect() {
        val ch3Scope = Season2WordPartsCatalog.wordCatalogIdsForChapter3WordParts(Season2ChapterContent.ch3Words)
        listOf(
            Season2ChapterRegistry.chapter(2)!!.wordCatalogIds to Season2WordPartsPresentationMode.VisibleWordParts,
            ch3Scope to Season2WordPartsPresentationMode.GuidedWordParts,
        ).forEach { (words, mode) ->
            val q = generateWordParts(words, mode)
            assertEquals(3, q.splitOptions.size)
            assertTrue(
                q.splitOptions.any {
                    it.firstPart == q.firstPart && it.secondPart == q.correctPart
                },
            )
        }
    }

    @Test
    fun ch3_st6_hiddenModeDiffersFromSt5Pool() {
        val ch3Scope = Season2WordPartsCatalog.wordCatalogIdsForChapter3WordParts(Season2ChapterContent.ch3Words)
        val guidedPool =
            Season2WordPartsCatalog.entriesForPresentationMode(
                ch3Scope,
                Season2WordPartsPresentationMode.GuidedWordParts,
                stationChapterIndex = 3,
                stationId = 5,
            ).map { it.catalogId }.toSet()
        val hiddenPool =
            Season2WordPartsCatalog.entriesForPresentationMode(
                ch3Scope,
                Season2WordPartsPresentationMode.HiddenWordPartsChallenge,
                stationChapterIndex = 3,
                stationId = 6,
            ).map { it.catalogId }.toSet()
        assertEquals(setOf("w_ג_1", "w_ג_3", "w_נ_2", "w_צ_2", "w_פ_2", "w_ש_1"), guidedPool)
        assertEquals(setOf("w_ב_2", "w_ח_2", "w_ח_3", "w_ר_1", "w_ר_3", "w_ז_3"), hiddenPool)
        assertTrue("guided and hidden pools should not overlap", guidedPool.intersect(hiddenPool).isEmpty())
        assertFalse("w_ח_2" in guidedPool)
        assertTrue("w_ב_2" in hiddenPool)

        val q =
            generateWordParts(
                ch3Scope,
                Season2WordPartsPresentationMode.HiddenWordPartsChallenge,
            )
        assertEquals(Season2WordPartsPresentationMode.HiddenWordPartsChallenge, q.presentationMode)
        assertEquals(3, q.splitOptions.size)
    }

    @Test
    fun wordLevelStationRoundCounts_matchParityOrWordPartsPool() {
        val ch1Plan = Season2Chapter1StationOrder.quizPlan(chapterIndex = 1, stationId = 6)
        assertEquals(6, ch1Plan.questionCount)

        val ch2Plan = Season2Chapter1StationOrder.quizPlan(chapterIndex = 2, stationId = 6)
        assertEquals(6, ch2Plan.questionCount)

        val ch3Ctx = Season2ChapterStationPlans.contextFor(3)!!
        val ch3St5 = Season2ChapterStationPlans.quizPlan(ch3Ctx, 5)
        val ch3St6 = Season2ChapterStationPlans.quizPlan(ch3Ctx, 6)
        assertEquals(6, ch3St5.questionCount)
        assertEquals(6, ch3St6.questionCount)
    }

    @Test
    fun wordParts_splitTapHasFullWordAudioForValidatedSplits() {
        val catalogId = "w_ש_1"
        assertNotNull(Season2RawAudio.wordPartRawResId(catalogId, 1))
        assertNotNull(Season2RawAudio.wordPartRawResId(catalogId, 2))
        assertNotNull(AudioClips.wordRawResIdByCatalogId(catalogId))
        assertTrue(Season2WordPartsCatalog.hasCompleteWordPartsAudio(catalogId))
    }

    @Test
    fun mapReturnCaption_visibleTextCoversStationsOneThroughFive() {
        assertNotNull(Season2Copy.returnCaptionAfterStation(1))
        assertNotNull(Season2Copy.returnCaptionAfterStation(4))
        assertNotNull(Season2Copy.returnCaptionAfterStation(5))
        assertNotEquals(
            Season2Copy.returnCaptionAfterStation(1),
            Season2Copy.returnCaptionAfterStation(5),
        )
    }

    @Test
    fun mapReturnCaptionNavKeys_exist() {
        assertEquals("s2_map_return_caption_event", Season2NavKeys.MAP_RETURN_CAPTION_EVENT)
        assertEquals("s2_map_return_caption_count", Season2NavKeys.MAP_RETURN_CAPTION_COUNT)
    }

    @Test
    fun pictureToWord_wrongTryAgain_playsInlineOnRawVoice() {
        val imageMatch =
            readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/ImageMatchActions.kt")
        assertTrue(imageMatch.contains("runImageToWordWrongFeedback"))
        assertTrue(imageMatch.contains("playAddressAwareTryAgainBlocking"))
        assertTrue(imageMatch.contains("runImageToWordWrongFeedback(tryAgain)"))
        assertTrue(imageMatch.contains("onWrongFeedback(choiceId, true, playInlineTryAgain)"))
        val wrong =
            readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/WrongFeedbackActions.kt")
        assertTrue(wrong.contains("GameAudioActions.joinSilently(voiceJob)"))
    }

    @Test
    fun whichWord_wrongTryAgain_playsInlineWithoutPreCancel() {
        val imageMatch =
            readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/ImageMatchActions.kt")
        assertTrue(imageMatch.contains("runWhichWordWrongAttempt"))
        assertTrue(imageMatch.contains("runImageToWordWrongFeedback(tryAgain)"))
        assertTrue(imageMatch.contains("physicalStationId = stationId"))
        val policy =
            readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/domain/Season2StationQaPolicy.kt")
        assertTrue(policy.contains("physicalStationId: Int? = null"))
        val gameScreen = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/GameScreen.kt")
        assertTrue(gameScreen.contains("return WrongFeedbackActions.trigger("))
    }

    @Test
    fun pickLetter_wrongTryAgain_playsBlockingAndJoinsFeedback() {
        val pick =
            readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/PickLetterActions.kt")
        assertTrue(pick.contains("playPickLetterWrongTryAgainIfNeeded"))
        assertTrue(pick.contains("playAddressAwareTryAgainBlocking"))
        assertTrue(pick.contains("PickLetterActions.handlePick(wrong,tryAgain)"))
        assertTrue(pick.contains("onWrongFeedback(picked, true, skipTryAgain)"))
        assertTrue(pick.contains("GameAudioActions.joinSilently(feedbackJob)"))
        val gameScreen = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/GameScreen.kt")
        assertTrue(gameScreen.contains("willPlayCoachFocusAfterWrong = willPlayCoachFocusAfterWrong"))
        assertTrue(gameScreen.contains("skipTryAgainAudio = skipTryAgainAudio"))
        assertTrue(gameScreen.contains("GameScreen.onWrongFeedback(coachTryAgain)"))
        assertFalse(
            gameScreen.contains(
                "launchFeedbackVoiceNoCancel(\n" +
                    "                                    audioEnabled = true,\n" +
                    "                                    scope = this,\n" +
                    "                                    audioRuntime = audioRuntime,\n" +
                    "                                ) {\n" +
                    "                                    playAddressAwareTryAgainBlocking(\n" +
                    "                                        chapterId = chapterId,\n" +
                    "                                        stationId = stationId,\n" +
                    "                                        playerAddress = chapter1PlayerAddress,\n" +
                    "                                        rawVoice = rawVoice,\n" +
                    "                                        voice = voice,\n" +
                    "                                        context = \"GameScreen.onWrongFeedback(coachTryAgain)\",\n" +
                    "                                    )\n" +
                    "                                }",
            ),
        )
        val wrong =
            readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/WrongFeedbackActions.kt")
        assertTrue(wrong.contains("skipTryAgainAudio: Boolean = false"))
        assertTrue(wrong.contains("launchFeedbackVoiceNoCancel"))
        assertTrue(wrong.contains("GameAudioActions.joinSilently(voiceJob)"))
        val feedback =
            readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/companion/Chapter1FeedbackAudio.kt")
        assertTrue(feedback.contains("Season2StationAudio.isSeason2GameplayChapter(chapterId)"))
    }

    @Test
    fun wordParts_wrongTryAgain_playsInlineOnRawVoice() {
        val actions =
            readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/Season2AdvancedStationActions.kt")
        assertTrue(actions.contains("playAddressAwareTryAgainBlocking"))
        assertTrue(actions.contains("handleWordPartsPick(tryAgain)"))
        assertTrue(actions.contains("onWrongFeedback(true)"))
    }

    @Test
    fun wordParts_coachReplay_includesWordAndParts() {
        val coach =
            readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/domain/Season2GuessingCoach.kt")
        assertTrue(coach.contains("playPictureTapSequence"))
        val audio =
            readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/audio/Season2WordPartsAudio.kt")
        assertTrue(audio.contains("playPictureTapSequence"))
        assertTrue(audio.contains("playPartsSequence"))
    }

    @Test
    fun season1_unchanged() {
        val plan = StationQuizPlans.chapter1(Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH)
        assertNotEquals(Season2AdvancedStationMode.PictureToWord, plan.season2AdvancedMode)
    }

    @Test
    fun noLegacyDinoAssets() {
        val dino = CompanionAssets.forCharacter(DinoCharacter.Dino)
        assertEquals(R.drawable.companion_dino_idle, dino.poseIdle)
    }

    private fun generateWordParts(
        wordIds: List<String>,
        mode: Season2WordPartsPresentationMode,
    ): Question.WordPartsQuestion {
        val specs = Season2WordPartsCatalog.entriesForPresentationMode(wordIds, mode)
        require(specs.size >= 3)
        return Season2AdvancedStationGenerators.generateForMode(
            rnd = Random(7),
            mode = Season2AdvancedStationMode.WordParts,
            wordCatalogIds = wordIds,
            roundIndex = 0,
            excludeCorrectIds = emptySet(),
            distractorLetters = emptyList(),
            wordPartsPresentationMode = mode,
        ) as Question.WordPartsQuestion
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
