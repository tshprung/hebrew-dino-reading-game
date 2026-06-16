package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.test.ProjectSource

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2Ch1PictureStartsWithLayoutPilotTest {
    @Test
    fun layout_pilot_disabled_for_ch1_parity() {
        assertFalse(
            Season2Ch1QaPolicy.isPictureStartsWithLayoutPilot(
            ),
        )
        assertFalse(
            Season2Ch1QaPolicy.isPictureStartsWithLayoutPilot(
            ),
        )
        assertFalse(
            Season2Ch1QaPolicy.isPictureStartsWithLayoutPilot(
            ),
        )
    }

    @Test
    fun ch1_picture_starts_with_matches_source_ui_spec() {
        val source =
            StationBehaviorRegistry.getStationUiSpec(1, Chapter1StationOrder.PICTURE_PICK_ONE)
        val s2 =
            StationBehaviorRegistry.getStationUiSpec(
                Season2ChapterIds.Chapter1Tyrannosaurus,
                Season2Chapter1StationOrder.PICTURE_STARTS_WITH,
            )
        assertTrue(source.pictureStartsWithReadablePanel)
        assertTrue(s2.pictureStartsWithReadablePanel)
        assertEquals(source.pictureStartsWithInstructionPanelStyle, s2.pictureStartsWithInstructionPanelStyle)
        assertEquals(source.pictureStartsWithSagaStation, s2.pictureStartsWithSagaStation)
        assertEquals(source.pictureStartsWithCompactLandscapeRtlWrapInstruction, s2.pictureStartsWithCompactLandscapeRtlWrapInstruction)
        assertEquals(source.pictureStartsWithVerticalNudgeDp, s2.pictureStartsWithVerticalNudgeDp, 0.01f)
    }

    @Test
    fun ch1_picture_starts_with_gets_address_aware_intro_audio() {
        val chapterId = Season2ChapterIds.Chapter1Tyrannosaurus
        val stationId = Season2Chapter1StationOrder.PICTURE_STARTS_WITH
        assertTrue(SixStationArcQaPolicy.isSagaPictureStartsWithStation(chapterId, stationId))
        assertTrue(
            Season2StationAudio.usesPictureStartsWithAddressAwareIntro(
                chapterId = chapterId,
                isSagaEpisodeParam = false,
            ),
        )
    }

    @Test
    fun readable_panel_not_suppressed_in_renderer() {
        val renderer =
            ProjectSource.read("app/src/main/java/com/tal/hebrewdino/ui/screens/PictureStartsWithQuestionRenderer.kt")
        assertTrue(renderer.contains("instructionReadablePanel && !layoutPilot"))
        assertFalse(
            Season2Ch1QaPolicy.isPictureStartsWithLayoutPilot(
            ),
        )
    }
}
