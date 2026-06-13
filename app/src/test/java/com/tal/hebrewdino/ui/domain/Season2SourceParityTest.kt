package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.BALLOON_POP
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.PICTURE_PICK_ALL
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.PICTURE_PICK_ONE
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.TAP_LETTER
import com.tal.hebrewdino.ui.domain.Season2ChapterStationPlans.StationKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2SourceParityTest {
    private val parityStations: List<Triple<Int, Int, Pair<Int, Int>>> =
        listOf(
            // Ch1 — 6 stations
            Triple(Season2ChapterIds.Chapter1Tyrannosaurus, 1, 1 to BALLOON_POP),
            Triple(Season2ChapterIds.Chapter1Tyrannosaurus, 2, 1 to TAP_LETTER),
            Triple(Season2ChapterIds.Chapter1Tyrannosaurus, 3, 1 to PICTURE_PICK_ONE),
            Triple(Season2ChapterIds.Chapter1Tyrannosaurus, 4, 3 to 3),
            Triple(Season2ChapterIds.Chapter1Tyrannosaurus, 5, 1 to PICTURE_PICK_ALL),
            Triple(Season2ChapterIds.Chapter1Tyrannosaurus, 6, 5 to 2),
            // Ch2 — 4 parity (skip memory + word parts)
            Triple(Season2ChapterIds.Chapter2Triceratops, 1, 1 to TAP_LETTER),
            Triple(Season2ChapterIds.Chapter2Triceratops, 2, 3 to 3),
            Triple(Season2ChapterIds.Chapter2Triceratops, 3, 1 to PICTURE_PICK_ONE),
            Triple(Season2ChapterIds.Chapter2Triceratops, 5, 5 to 2),
            // Ch3 — 4 parity (skip word parts)
            Triple(Season2ChapterIds.Chapter3Stegosaurus, 1, 1 to TAP_LETTER),
            Triple(Season2ChapterIds.Chapter3Stegosaurus, 2, 1 to PICTURE_PICK_ONE),
            Triple(Season2ChapterIds.Chapter3Stegosaurus, 3, 5 to 2),
            Triple(Season2ChapterIds.Chapter3Stegosaurus, 4, 3 to 3),
            // Ch4 — 6
            Triple(Season2ChapterIds.Chapter4Brachiosaurus, 1, 1 to BALLOON_POP),
            Triple(Season2ChapterIds.Chapter4Brachiosaurus, 2, 1 to TAP_LETTER),
            Triple(Season2ChapterIds.Chapter4Brachiosaurus, 3, 3 to 3),
            Triple(Season2ChapterIds.Chapter4Brachiosaurus, 4, 5 to 2),
            Triple(Season2ChapterIds.Chapter4Brachiosaurus, 5, 3 to 6),
            Triple(Season2ChapterIds.Chapter4Brachiosaurus, 6, 1 to FINALE_PICTURE_LETTER_MATCH),
            // Ch5 — 4 (skip rhyming + word parts)
            Triple(Season2ChapterIds.Chapter5Ankylosaurus, 1, 1 to TAP_LETTER),
            Triple(Season2ChapterIds.Chapter5Ankylosaurus, 2, 3 to 3),
            Triple(Season2ChapterIds.Chapter5Ankylosaurus, 3, 5 to 2),
            Triple(Season2ChapterIds.Chapter5Ankylosaurus, 4, 1 to PICTURE_PICK_ALL),
            // Ch6 — 5 (skip rhyming)
            Triple(Season2ChapterIds.Chapter6Mosasaurus, 1, 1 to TAP_LETTER),
            Triple(Season2ChapterIds.Chapter6Mosasaurus, 2, 3 to 3),
            Triple(Season2ChapterIds.Chapter6Mosasaurus, 3, 5 to 2),
            Triple(Season2ChapterIds.Chapter6Mosasaurus, 5, 3 to 6),
            Triple(Season2ChapterIds.Chapter6Mosasaurus, 6, 1 to FINALE_PICTURE_LETTER_MATCH),
            // Ch7 — 3 (skip memory, rhyming, word parts)
            Triple(Season2ChapterIds.Chapter7Pteranodon, 1, 3 to 3),
            Triple(Season2ChapterIds.Chapter7Pteranodon, 2, 5 to 2),
            Triple(Season2ChapterIds.Chapter7Pteranodon, 6, 1 to FINALE_PICTURE_LETTER_MATCH),
        )

    private val uniqueStations: List<Pair<Int, Int>> =
        listOf(
            Season2ChapterIds.Chapter2Triceratops to 4,
            Season2ChapterIds.Chapter2Triceratops to 6,
            Season2ChapterIds.Chapter3Stegosaurus to 5,
            Season2ChapterIds.Chapter3Stegosaurus to 6,
            Season2ChapterIds.Chapter5Ankylosaurus to 5,
            Season2ChapterIds.Chapter5Ankylosaurus to 6,
            Season2ChapterIds.Chapter6Mosasaurus to 4,
            Season2ChapterIds.Chapter7Pteranodon to 3,
            Season2ChapterIds.Chapter7Pteranodon to 4,
            Season2ChapterIds.Chapter7Pteranodon to 5,
        )

    @Test
    fun canonicalSource_mapsAll32ParityStations() {
        assertEquals(32, parityStations.size)
        parityStations.forEach { (gameplayChapterId, stationId, expected) ->
            assertEquals(
                "ch=$gameplayChapterId st=$stationId",
                expected,
                Season2SourceStation.canonicalSource(gameplayChapterId, stationId),
            )
        }
    }

    @Test
    fun uniqueStations_haveNoCanonicalSource() {
        uniqueStations.forEach { (gameplayChapterId, stationId) ->
            assertNull(
                "expected no source ch=$gameplayChapterId st=$stationId",
                Season2SourceStation.canonicalSource(gameplayChapterId, stationId),
            )
        }
    }

    @Test
    fun uiSpec_matchesSourceExceptChapterAndStationIds() {
        parityStations.forEach { (gameplayChapterId, stationId, source) ->
            val (sourceChapterId, sourceStationId) = source
            val s2Spec = StationBehaviorRegistry.getStationUiSpec(gameplayChapterId, stationId)
            val sourceSpec = StationBehaviorRegistry.getStationUiSpec(sourceChapterId, sourceStationId)
            assertEquals(
                "template ch=$gameplayChapterId st=$stationId",
                sourceSpec.templateId,
                s2Spec.templateId,
            )
            assertEquals(
                "instruction ch=$gameplayChapterId st=$stationId",
                instructionKey(sourceSpec),
                instructionKey(s2Spec),
            )
            assertEquals(
                "help ch=$gameplayChapterId st=$stationId",
                sourceSpec.helpControlsEnabled,
                s2Spec.helpControlsEnabled,
            )
            assertEquals(
                "replay ch=$gameplayChapterId st=$stationId",
                sourceSpec.replayMode,
                s2Spec.replayMode,
            )
            assertEquals(
                "hint ch=$gameplayChapterId st=$stationId",
                sourceSpec.hintMode,
                s2Spec.hintMode,
            )
            assertEquals(gameplayChapterId, s2Spec.chapterId)
            assertEquals(stationId, s2Spec.stationId)
            assertTrue(s2Spec.showBetweenRoundIntroPulse == false)
        }
    }

    @Test
    fun quizPlan_matchesSourceShape() {
        parityStations.forEach { (gameplayChapterId, stationId, source) ->
            val (sourceChapterId, sourceStationId) = source
            val chapterIndex = gameplayChapterId - 100
            val s2Plan =
                if (chapterIndex <= 2) {
                    Season2Chapter1StationOrder.quizPlan(chapterIndex, stationId)
                } else {
                    val ctx = Season2ChapterStationPlans.contextFor(chapterIndex)!!
                    Season2ChapterStationPlans.quizPlan(ctx, stationId)
                }
            val sourcePlan = Season2SourceStation.sourceQuizPlan(sourceChapterId, sourceStationId)
            assertEquals(
                "questionCount ch=$gameplayChapterId st=$stationId",
                sourcePlan.questionCount,
                s2Plan.questionCount,
            )
            assertEquals(
                "initialGroupIndex ch=$gameplayChapterId st=$stationId",
                sourcePlan.initialGroupIndex,
                s2Plan.initialGroupIndex,
            )
            assertEquals(
                "optionCount ch=$gameplayChapterId st=$stationId",
                sourcePlan.optionCount,
                s2Plan.optionCount,
            )
            assertEquals(
                "imageMatchAlwaysThreeChoices ch=$gameplayChapterId st=$stationId",
                sourcePlan.imageMatchAlwaysThreeChoices,
                s2Plan.imageMatchAlwaysThreeChoices,
            )
            assertEquals(
                "dragWordPairCount ch=$gameplayChapterId st=$stationId",
                sourcePlan.dragWordToPicturePairCount,
                s2Plan.dragWordToPicturePairCount,
            )
        }
    }

    @Test
    fun whichWord_earlyArcUxStationId_resolvesForS2Gameplay() {
        assertEquals(
            Season2Chapter1StationOrder.WHICH_WORD_STARTS_WITH,
            SixStationArcQaPolicy.earlyArcUxStationId(
                Season2ChapterIds.Chapter1Tyrannosaurus,
                Season2Chapter1StationOrder.WHICH_WORD_STARTS_WITH,
            ),
        )
        assertEquals(
            Season2Chapter1StationOrder.WHICH_WORD_STARTS_WITH,
            SixStationArcQaPolicy.earlyArcUxStationId(
                Season2ChapterIds.Chapter5Ankylosaurus,
                4,
            ),
        )
    }

    @Test
    fun stationKind_coversAllGameplayChapters() {
        (Season2ChapterIds.Chapter1Tyrannosaurus..Season2ChapterIds.Chapter7Pteranodon).forEach { chapterId ->
            (1..Season2Chapter1StationOrder.STATION_COUNT).forEach { stationId ->
                if (uniqueStations.contains(chapterId to stationId)) return@forEach
                assertNotNull(
                    "kind ch=$chapterId st=$stationId",
                    Season2SourceStation.stationKind(chapterId, stationId),
                )
            }
        }
    }

    private fun instructionKey(spec: StationUiSpec): String =
        listOf(
            spec.pickLetterInstructionOverride,
            spec.imageMatchHeaderInstructionOverride,
            spec.imageToWordInstructionText,
            spec.findGridInlineInstructionOverride,
            spec.balloonInstructionOverride,
            spec.matchLetterInstructionText,
        ).joinToString("|")
}
