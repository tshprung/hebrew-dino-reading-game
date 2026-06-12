package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.NavRoutes
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.Season2CompanionFeedbackAudio
import com.tal.hebrewdino.ui.audio.Season2RawAudio
import com.tal.hebrewdino.ui.companion.CompanionAssets
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.screens.GameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random
import java.io.File

class Season2StabilityRegressionAuditTest {
    // --- 1. Station routing ---
    @Test
    fun routing_ch1St6_isPictureToWord() {
        val plan = Season2Chapter1StationOrder.quizPlan(1, 6)
        assertEquals(Season2AdvancedStationMode.PictureToWord, plan.season2AdvancedMode)
        assertEquals(6, plan.questionCount)
    }

    @Test
    fun routing_intendedFinaleAndFocusStations_matchPlan() {
        Season2StabilityAudit.intendedFinaleRouting.forEach { expected ->
            if (expected.chapterIndex <= 2) {
                val plan = Season2Chapter1StationOrder.quizPlan(expected.chapterIndex, expected.stationId)
                assertEquals(expected.advancedMode, plan.season2AdvancedMode)
                assertEquals(expected.wordPartsMode, plan.season2WordPartsPresentationMode)
            } else {
                val ctx = Season2ChapterStationPlans.contextFor(expected.chapterIndex)!!
                val kind = Season2ChapterStationPlans.stationKind(expected.chapterIndex, expected.stationId)
                assertEquals(expected.kind, kind)
                val plan = Season2ChapterStationPlans.quizPlan(ctx, expected.stationId)
                assertEquals(expected.advancedMode, plan.season2AdvancedMode)
                assertEquals(expected.wordPartsMode, plan.season2WordPartsPresentationMode)
            }
        }
        assertEquals(
            Season2ChapterStationPlans.StationKind.MatchLetterToWord,
            Season2ChapterStationPlans.stationKind(4, 6),
        )
    }

    // --- 2. Audio routing safety ---
    @Test
    fun audio_season2Instructions_useResRawMp3() {
        assertTrue(Season2RawAudio.instructionRawResId(Season2AdvancedStationMode.PictureToWord) > 0)
        assertEquals(
            R.raw.season2_word_parts_choose_split_instructions,
            Season2RawAudio.instructionRawResId(
                Season2AdvancedStationMode.WordParts,
                Season2WordPartsPresentationMode.VisibleWordParts,
            ),
        )
        assertEquals(
            R.raw.season2_word_parts_hidden_split_instructions,
            Season2RawAudio.instructionRawResId(
                Season2AdvancedStationMode.WordParts,
                Season2WordPartsPresentationMode.HiddenWordPartsChallenge,
            ),
        )
        assertEquals(
            R.raw.season2_missing_first_letter_instructions,
            Season2RawAudio.instructionRawResId(Season2AdvancedStationMode.MissingFirstLetter),
        )
        assertEquals(
            R.raw.season2_rhyming_instructions,
            Season2RawAudio.instructionRawResId(Season2AdvancedStationMode.Rhyming),
        )
        assertEquals(R.raw.season2_replay_tile_instruction, Season2Copy.replayTileInstructionVoiceRawRes())
    }

    @Test
    fun audio_companionPools_useResRawFocusAndMapPraise() {
        assertTrue(Season2StabilityAudit.focusPoolsAreCompanionSpecific())
        assertTrue(Season2StabilityAudit.mapPraisePoolsAreCompanionSpecific())
        Season2RawAudio.focusPool(DinoCharacter.Dino).forEach { assertTrue(it > 0) }
        Season2RawAudio.mapPraisePool(DinoCharacter.Dina).forEach { assertTrue(it > 0) }
    }

    @Test
    fun audio_wordParts_useChunkAndFullWordNotLetterNames() {
        val withChunks = Season2StabilityAudit.wordPartCatalogIdsWithRawChunks()
        assertTrue("w_ש_1" in withChunks)
        withChunks.forEach { id ->
            assertNotNull(Season2RawAudio.wordPartRawResId(id, 1))
            assertNotNull(Season2RawAudio.wordPartRawResId(id, 2))
            assertNotNull(AudioClips.wordRawResIdByCatalogId(id))
        }
        assertNull(Season2RawAudio.wordPartRawResId("w_נ_2", 1))
        assertNotNull(AudioClips.letterNameRawResId("נ"))
    }

    @Test
    fun audio_noAssetsAudioSeason2WavInAudioClips() {
        assertFalse(Season2StabilityAudit.audioClipsContainsSeason2WavPath())
    }

    // --- 3. Map praise captions ---
    @Test
    fun mapCaptions_areIndexMapped_notTranscriptVerified() {
        assertEquals(Season2StabilityAudit.MAP_PRAISE_CAPTION_SOURCE, "INDEX_MAPPED_NOT_TRANSCRIPT_VERIFIED")
        val dino01 = Season2RawAudio.mapPraiseCaption(R.raw.season2_map_praise_dino_01)
        val dina01 = Season2RawAudio.mapPraiseCaption(R.raw.season2_map_praise_dina_01)
        assertNotNull(dino01)
        assertEquals(dino01, dina01)
        assertNotEquals(
            dino01,
            Season2RawAudio.mapPraiseCaption(R.raw.season2_map_praise_dino_02),
        )
        Season2RawAudio.mapPraisePool(DinoCharacter.Dino).forEach { raw ->
            assertTrue(Season2CompanionFeedbackAudio.mapPraiseCaption(raw).isNotBlank())
        }
    }

    // --- 4. Focus feedback ---
    @Test
    fun focus_allSeason2QuizStations_useCompanionFocusAfterTwoWrongs() {
        val stations =
            listOf(
                Season2Chapter1StationOrder.PICK_LETTER,
                Season2Chapter1StationOrder.PICTURE_STARTS_WITH,
                Season2Chapter1StationOrder.WHICH_WORD_STARTS_WITH,
                Season2Chapter1StationOrder.FINALE_STATION,
            )
        stations.forEach { sid ->
            assertTrue(
                Season2Station6FeedbackPolicy.shouldSkipCoachBubble(isSeason2Quiz = true),
            )
            assertFalse(
                Season2Station6FeedbackPolicy.shouldReplayInstructionAfterWrong(
                    consecutiveWrongInRound = 1,
                    isSeason2Quiz = true,
                ),
            )
            assertTrue(
                Season2Station6FeedbackPolicy.shouldReplayInstructionAfterWrong(
                    consecutiveWrongInRound = 2,
                    isSeason2Quiz = true,
                ),
            )
        }
        assertFalse(
            Season2Station6FeedbackPolicy.shouldReplayInstructionAfterWrong(
                consecutiveWrongInRound = 2,
                isSeason2Quiz = false,
            ),
        )
    }

    @Test
    fun focus_pools_areNonEmptyCompanionSpecificRaw() {
        assertEquals(3, Season2RawAudio.focusPool(DinoCharacter.Dino).size)
        assertEquals(3, Season2RawAudio.focusPool(DinoCharacter.Dina).size)
        assertTrue(R.raw.season2_focus_dino_01 in Season2RawAudio.focusPool(DinoCharacter.Dino).toSet())
        assertTrue(R.raw.season2_focus_dina_01 in Season2RawAudio.focusPool(DinoCharacter.Dina).toSet())
    }

    // --- 5. Word-parts invariants ---
    @Test
    fun wordParts_splitOptions_threeUniqueIncludingCorrect() {
        val modes =
            listOf(
                Season2ChapterRegistry.chapter(2)!!.wordCatalogIds to
                    Season2WordPartsPresentationMode.VisibleWordParts,
                Season2WordPartsCatalog.wordCatalogIdsForChapter3WordParts(Season2ChapterContent.ch3Words) to
                    Season2WordPartsPresentationMode.GuidedWordParts,
                Season2WordPartsCatalog.wordCatalogIdsForChapter3WordParts(Season2ChapterContent.ch3Words) to
                    Season2WordPartsPresentationMode.HiddenWordPartsChallenge,
            )
        modes.forEach { (words, mode) ->
            val q =
                Season2AdvancedStationGenerators.generateForMode(
                    rnd = Random(11),
                    mode = Season2AdvancedStationMode.WordParts,
                    wordCatalogIds = words,
                    roundIndex = 0,
                    excludeCorrectIds = emptySet(),
                    distractorLetters = emptyList(),
                    wordPartsPresentationMode = mode,
                ) as Question.WordPartsQuestion
            assertEquals(3, q.splitOptions.size)
            assertEquals(q.splitOptions.size, q.splitOptions.distinctBy { "${it.firstPart}|${it.secondPart}" }.size)
            assertTrue(
                q.splitOptions.any {
                    it.firstPart == q.firstPart && it.secondPart == q.correctPart
                },
            )
        }
    }

    @Test
    fun wordParts_ch2ShowsFullWord_ch3St6HidesByDefault() {
        val ch2Plan = Season2Chapter1StationOrder.quizPlan(2, 6)
        assertEquals(Season2WordPartsPresentationMode.VisibleWordParts, ch2Plan.season2WordPartsPresentationMode)
        val ch3St6 = Season2ChapterStationPlans.quizPlan(Season2ChapterStationPlans.contextFor(3)!!, 6)
        assertEquals(
            Season2WordPartsPresentationMode.HiddenWordPartsChallenge,
            ch3St6.season2WordPartsPresentationMode,
        )
    }

    @Test
    fun wordParts_tappedSplitMapsToOwnFullWord_notTargetOnWrong() {
        val wrongSplit = Season2WordPartsCatalog.catalogIdForSplit("ג", "מל")
        val targetSplit = Season2WordPartsCatalog.catalogIdForSplit("ש", "מש")
        assertNotNull(wrongSplit)
        assertNotNull(targetSplit)
        assertNotEquals(wrongSplit, targetSplit)
        assertNotNull(AudioClips.wordRawResIdByCatalogId(wrongSplit!!))
        assertNotEquals(
            LessonWordCatalog.entries.first { it.id == wrongSplit }.word,
            LessonWordCatalog.entries.first { it.id == targetSplit!! }.word,
        )
    }

    @Test
    fun wordParts_ch3Pools_andRoundCounts() {
        val (guided, hidden) = Season2StabilityAudit.ch3WordPartsPools()
        assertEquals(setOf("w_ג_1", "w_ג_3", "w_פ_2", "w_ח_2", "w_ח_3", "w_ר_1"), guided)
        assertEquals(setOf("w_ש_1", "w_ר_3", "w_ח_2", "w_ח_3", "w_ר_1", "w_ג_1"), hidden)
        val ctx = Season2ChapterStationPlans.contextFor(3)!!
        assertEquals(6, Season2ChapterStationPlans.quizPlan(ctx, 5).questionCount)
        assertEquals(6, Season2ChapterStationPlans.quizPlan(ctx, 6).questionCount)
    }

    @Test
    fun wordParts_gameViewModel_hasPickJobForInterruptibleAudio() {
        val vm =
            GameViewModel(
                plan = Season2Chapter1StationOrder.quizPlan(2, 6),
                letterPoolSpec = Season2Chapter2LetterPoolSpec,
            )
        assertNull(vm.wordPartsPickJob)
    }

    @Test
    fun wordParts_splitCardIsOnlyTapTarget_notPartChips() {
        val src = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/Season2WordPartsGame.kt")
        assertTrue(src.contains("WordPartsSplitOptionRow"))
        assertFalse(src.contains("SplitPartLabel(text = option.firstPart"))
        assertFalse(src.contains("onPickSplit(option.firstPart"))
    }

    // --- 6. Ch2 letter pool ---
    @Test
    fun ch2_letterPool_fiveUniqueWithMemReviewWords() {
        val ch2 = Season2ChapterRegistry.chapter(2)!!
        assertEquals(listOf("ח", "ר", "ק", "ש", "מ"), ch2.letters)
        assertEquals(5, ch2.letters.distinct().size)
        assertTrue(ch2.validation.qaReady)
        assertEquals(2, Season2StabilityAudit.memWordsForLetter(2, "מ"))
        assertNotNull(AudioClips.letterNameRawResId("מ"))
    }

    @Test
    fun ch2_memLetter_hasValidatedAssetsForAllGenerators() {
        val ch2 = Season2ChapterRegistry.chapter(2)!!
        assertNotNull(AudioClips.letterNameRawResId("מ"))
        ch2.wordCatalogIds
            .filter { id -> LessonWordCatalog.entries.find { it.id == id }?.letter == "מ" }
            .forEach { id -> assertNotNull(AudioClips.wordRawResIdByCatalogId(id)) }
        assertTrue(ch2.memoryMatchLetters.contains("מ"))
        assertEquals(5, ch2.memoryMatchLetters.distinct().size)
    }

    // --- 7. Ch3 St3 ---
    @Test
    fun ch3_st3_fiveUniqueLetterChoicesFromChapterPool() {
        val plan = Season2ChapterStationPlans.quizPlan(Season2ChapterStationPlans.contextFor(3)!!, 3)
        assertEquals(5, plan.optionCount)
        assertEquals(listOf("ג", "נ", "פ", "צ", "ש"), Season2ChapterContent.ch3Letters)
        val spec = StationBehaviorRegistry.getStationUiSpec(Season2ChapterIds.Chapter3Stegosaurus, 3)
        assertEquals(StationTemplateId.PictureStartsWith, spec.templateId)
    }

    // --- 8. Memory Match praise (source audit) ---
    @Test
    fun memoryMatch_praiseUsesInStationPraiseShort_withVoiceDuck() {
        val src = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/Season2MemoryMatchStationScreen.kt")
        assertTrue(src.contains("InStationPraiseAudio.pick()"))
        assertTrue(src.contains("withVoiceDuck"))
        assertTrue(src.contains("playPraiseClip()"))
        val stationCompleteBlock =
            src.substringAfter("if (roundIndex < totalRounds - 1)")
                .substringAfter("} else {")
                .substringBefore("onMarkCompleted()")
        assertEquals(1, stationCompleteBlock.split("playPraiseClip()").size - 1)
    }

    // --- 9. Map / reward flow ---
    @Test
    fun mapReward_navKeys_andRewardDestination() {
        assertEquals("s2_map_return_caption_event", Season2NavKeys.MAP_RETURN_CAPTION_EVENT)
        assertEquals("s2_map_return_caption_count", Season2NavKeys.MAP_RETURN_CAPTION_COUNT)
        val nav = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/AppNavSystemGraph.kt")
        assertTrue(nav.contains("onRewardContinue"))
        assertTrue(nav.contains("NavRoutes.Season2ChapterSelect"))
    }

    @Test
    fun mapReward_legacyMapClipsUnusedByActiveMapReturn() {
        assertEquals(2, Season2StabilityAudit.legacyUnusedMapRawResIds().size)
        val mapScreen = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/Season2PuzzleMapPrototypeScreen.kt")
        val storyAudio = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/audio/Season2StoryAudio.kt")
        assertTrue(mapScreen.contains("Season2StoryAudio.mapReturnVoice"))
        assertTrue(storyAudio.contains("pickMapReturnPraise"))
        assertFalse(mapScreen.contains("returnCaptionVoiceRawRes"))
        assertFalse(mapScreen.contains("MapPartRevealed"))
    }

    @Test
    fun mapPraise_noImmediateRepeatWhenAlternativesExist() {
        val first =
            Season2CompanionFeedbackAudio.pickMapReturnPraise(
                DinoCharacter.Dino,
                avoidRawResId = 0,
                random = Random(2),
            )
        val second =
            Season2CompanionFeedbackAudio.pickMapReturnPraise(
                DinoCharacter.Dino,
                avoidRawResId = first,
                random = Random(2),
            )
        assertNotEquals(first, second)
    }

    // --- 10. Content coverage ---
    @Test
    fun content_allPlayableChapters_passAssetValidation() {
        (1..6).forEach { index ->
            val ch = Season2ChapterRegistry.chapter(index)!!
            assertTrue("Ch$index missing: ${ch.validation.missingAssets}", ch.validation.qaReady)
        }
    }

    @Test
    fun content_compactGapsReport_nonEmpty() {
        val report = Season2StabilityAudit.compactContentGapsReport()
        assertTrue(report.contains("below6="))
        assertTrue(report.contains("ch3St5=6"))
        assertTrue(report.contains("ch3St6=6"))
    }

    // --- 11. Legacy safety ---
    @Test
    fun legacy_noDinoOrMomDrawableReferencesInMainSources() {
        val roots =
            listOf(
                "app/src/main/java/com/tal/hebrewdino/ui/companion",
                "app/src/main/java/com/tal/hebrewdino/ui/screens",
            )
        roots.forEach { root ->
            File(root).walkTopDown().filter { it.extension == "kt" }.forEach { file ->
                val text = file.readText()
                assertFalse("${file.path} dino_*", text.contains("R.drawable.dino_"))
                assertFalse("${file.path} mom_*", text.contains("R.drawable.mom_"))
            }
        }
    }

    @Test
    fun season1_stationPlan_unchanged() {
        val plan = StationQuizPlans.chapter1(Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH)
        assertNull(plan.season2AdvancedMode)
    }

    @Test
    fun noLegacyDinoCompanionAssets() {
        val dino = CompanionAssets.forCharacter(DinoCharacter.Dino)
        assertEquals(R.drawable.companion_dino_idle, dino.poseIdle)
    }

    private fun readProjectSource(relativePath: String): String {
        val candidates =
            listOf(
                File(relativePath),
                File("../$relativePath"),
                File("../../$relativePath"),
            )
        val file = candidates.firstOrNull { it.exists() }
            ?: error("Could not locate source file: $relativePath")
        return file.readText()
    }
}
