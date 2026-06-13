package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.data.PlayerAddress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season1CoachInterventionTest {
    @Test
    fun companionCoachPolicy_enabledForSeason1ChaptersWithCompanionAndAddress() {
        (1..6).forEach { chapterId ->
            assertTrue(
                CompanionCoachPolicy.isEnabled(
                    chapterId = chapterId,
                    companion = DinoCharacter.Dino,
                    playerAddress = PlayerAddress.Boy,
                ),
            )
        }
    }

    @Test
    fun companionCoachPolicy_disabledWithoutCompanionOrAddress() {
        assertFalse(
            CompanionCoachPolicy.isEnabled(
                chapterId = 1,
                companion = null,
                playerAddress = PlayerAddress.Boy,
            ),
        )
        assertFalse(
            CompanionCoachPolicy.isEnabled(
                chapterId = 1,
                companion = DinoCharacter.Dina,
                playerAddress = null,
            ),
        )
    }

    @Test
    fun uxStationId_usesStationIdWhenSeason2UxMissing() {
        assertEquals(3, CompanionCoachPolicy.uxStationId(season2UxStationId = null, stationId = 3))
        assertEquals(5, CompanionCoachPolicy.uxStationId(season2UxStationId = null, stationId = 5))
    }

    @Test
    fun hintCopy_usesTemplateIdForSeason1Stations() {
        val dragWord =
            Season2GuessingHintCopy.coachBubbleText(
                uxStationId = 3,
                playerAddress = PlayerAddress.Girl,
                templateId = StationTemplateId.DragWordToPicture,
            )
        assertTrue(dragWord.contains("נגרור"))
        val pop =
            Season2GuessingHintCopy.coachBubbleText(
                uxStationId = 2,
                playerAddress = PlayerAddress.Boy,
                templateId = StationTemplateId.PopBalloons,
            )
        assertTrue(pop.contains("אות"))
    }

    @Test
    fun focusFeedback_appliesToSeason1WhenCoachEnabled() {
        assertTrue(
            Season2Station6FeedbackPolicy.shouldReplayInstructionAfterWrong(
                consecutiveWrongInRound = 2,
                companionCoachEnabled = true,
            ),
        )
        assertFalse(
            Season2Station6FeedbackPolicy.shouldReplayInstructionAfterWrong(
                consecutiveWrongInRound = 1,
                companionCoachEnabled = true,
            ),
        )
    }

    @Test
    fun gameScreen_wiresCompanionCoachForSeason1() {
        val gameScreen = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/GameScreen.kt")
        assertTrue(gameScreen.contains("CompanionCoachPolicy.isEnabled"))
        assertTrue(gameScreen.contains("coachUxStationId"))
        assertTrue(gameScreen.contains("companionCoachEnabled"))
        assertTrue(gameScreen.contains("replayPopBalloonsTargetLetterOnly"))
        assertTrue(gameScreen.contains("replayForSeason1Template") || gameScreen.contains("stationUiSpec = stationUiSpec"))
    }

    @Test
    fun season1Chapters_allStationsUseGameScreenTemplates() {
        val gameScreenTemplates = StationTemplateId.entries.toSet()
        (1..6).forEach { chapterId ->
            (1..6).forEach { stationId ->
                val spec = StationBehaviorRegistry.getStationUiSpec(chapterId, stationId)
                assertTrue(
                    "ch$chapterId st$stationId template=${spec.templateId}",
                    spec.templateId in gameScreenTemplates,
                )
            }
        }
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
