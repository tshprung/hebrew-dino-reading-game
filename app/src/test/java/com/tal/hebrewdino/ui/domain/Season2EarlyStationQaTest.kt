package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.ui.audio.InStationPraiseAudio
import com.tal.hebrewdino.ui.screens.HintPulseActions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Behavioral QA policies for early S2 / saga stations (no source-file audits). */
class Season2EarlyStationQaTest {
    @Test
    fun balloonPraise_and_hintThresholds() {
        assertTrue(Season2EarlyStationQaPolicy.shouldPlayBalloonPraiseOnCorrectPop(season2QuizBalloons = true))
        assertFalse(Season2EarlyStationQaPolicy.shouldPlayBalloonPraiseOnCorrectPop(season2QuizBalloons = false))
        assertFalse(
            Season2EarlyStationQaPolicy.shouldPlayBalloonPraiseOnCorrectPop(
                season2QuizBalloons = true,
                finalCorrectBalloon = true,
            ),
        )
        assertFalse(Season2EarlyStationQaPolicy.shouldShowPictureStartsWithHint(wrongTapsThisQuestion = 1))
        assertTrue(Season2EarlyStationQaPolicy.shouldShowPictureStartsWithHint(wrongTapsThisQuestion = 2))
        val first = HintPulseActions.registerWrongTapForHintPulse(wrongTapsThisQuestion = 0, hintPulseEpoch = 0)
        assertEquals(1, first.first)
        assertEquals(0, first.second)
    }

    @Test
    fun coachIntervention_and_replayPolicies() {
        assertTrue(Season2EarlyStationQaPolicy.shouldSkipInStationCorrectPraiseAfterCoach(season2HadCoachIntervention = true))
        assertFalse(Season2EarlyStationQaPolicy.shouldSkipInStationCorrectPraiseAfterCoach(season2HadCoachIntervention = false))
        assertTrue(InStationPraiseAudio.usesRawPraisePool(Season2ChapterIds.Chapter1Tyrannosaurus))
        assertTrue(
            Season2EarlyStationQaPolicy.shouldReplayWordForPictureStartsWithCoach(
                Season2Chapter1StationOrder.PICTURE_STARTS_WITH,
            ),
        )
        assertFalse(
            Season2EarlyStationQaPolicy.shouldReplayWordForPictureStartsWithCoach(
                Season2Chapter1StationOrder.PICK_LETTER,
            ),
        )
        assertTrue(
            Season2EarlyStationQaPolicy.shouldUseSeason2PictureStartsWithWrongAudio(
                isSeason2QuizChapter = true,
                sagaEpisode = false,
                chapterId = 101,
                stationId = 1,
            ),
        )
    }
}
