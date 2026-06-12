package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.Season2CompanionFeedbackAudio
import com.tal.hebrewdino.ui.audio.Season2RawAudio
import com.tal.hebrewdino.ui.companion.CompanionAssets
import com.tal.hebrewdino.ui.data.DinoCharacter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class Season2QaFeedbackBatchTest {
    @Test
    fun focusPolicy_appliesToAllSeason2QuizStations() {
        assertTrue(
            Season2Station6FeedbackPolicy.shouldReplayInstructionAfterWrong(
                consecutiveWrongInRound = 2,
                isSeason2Quiz = true,
            ),
        )
        assertFalse(
            Season2Station6FeedbackPolicy.shouldReplayInstructionAfterWrong(
                consecutiveWrongInRound = 1,
                isSeason2Quiz = true,
            ),
        )
    }

    @Test
    fun focusPools_useCompanionSpecificRawClips() {
        val dino = Season2RawAudio.focusPool(DinoCharacter.Dino).toSet()
        val dina = Season2RawAudio.focusPool(DinoCharacter.Dina).toSet()
        assertEquals(3, dino.size)
        assertEquals(3, dina.size)
        assertTrue(R.raw.season2_focus_dino_01 in dino)
        assertTrue(R.raw.season2_focus_dina_01 in dina)
        assertTrue(dino.none { it in dina })
    }

    @Test
    fun mapPraiseCaption_matchesEveryPoolClip() {
        for (companion in listOf(DinoCharacter.Dino, DinoCharacter.Dina)) {
            Season2RawAudio.mapPraisePool(companion).forEach { rawRes ->
                val caption = Season2CompanionFeedbackAudio.mapPraiseCaption(rawRes)
                assertTrue(caption.isNotBlank())
            }
        }
        val praise =
            Season2CompanionFeedbackAudio.pickMapReturnPraise(
                DinoCharacter.Dino,
                avoidRawResId = 0,
                random = Random(3),
            )
        assertEquals(
            Season2CompanionFeedbackAudio.mapPraiseCaption(praise),
            Season2RawAudio.mapPraiseCaption(praise),
        )
        assertNotEquals(
            Season2Copy.returnCaptionAfterStation(1),
            Season2CompanionFeedbackAudio.mapPraiseCaption(R.raw.season2_map_praise_dino_02),
        )
    }

    @Test
    fun ch2_letterPool_hasFiveUniqueValidatedLetters() {
        val ch2 = Season2ChapterRegistry.chapter(2)!!
        assertEquals(listOf("ח", "ר", "ק", "ש", "מ"), ch2.letters)
        assertEquals(5, ch2.letters.distinct().size)
        assertTrue(ch2.validation.qaReady)
        assertTrue(ch2.wordCatalogIds.count { id ->
            LessonWordCatalog.entries.find { it.id == id }?.letter == "מ"
        } >= 2)
    }

    @Test
    fun ch3_station2_pictureStartsWith_hasFiveOptions() {
        val ctx = Season2ChapterStationPlans.contextFor(3)!!
        val plan = Season2ChapterStationPlans.quizPlan(ctx, 2)
        assertEquals(5, plan.optionCount)
        assertEquals(5, Season2ChapterContent.ch3Letters.distinct().size)
    }

    @Test
    fun wordParts_splitTapAudioAssets_completeForValidatedSplit() {
        val catalogId = "w_ש_1"
        assertNotNull(Season2RawAudio.wordPartRawResId(catalogId, 1))
        assertNotNull(Season2RawAudio.wordPartRawResId(catalogId, 2))
        assertTrue(Season2WordPartsCatalog.hasCompleteWordPartsAudio(catalogId))
    }

    @Test
    fun season1_unchanged() {
        val plan = StationQuizPlans.chapter1(Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH)
        assertNull(plan.season2AdvancedMode)
    }

    @Test
    fun noLegacyDinoAssets() {
        val dino = CompanionAssets.forCharacter(DinoCharacter.Dino)
        assertEquals(R.drawable.companion_dino_idle, dino.poseIdle)
    }
}
