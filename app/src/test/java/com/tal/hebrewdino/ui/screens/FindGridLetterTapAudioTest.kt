package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.StationQuizMode
import com.tal.hebrewdino.ui.domain.StationQuizPlans
import com.tal.hebrewdino.ui.domain.TrainingV1Config
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FindGridLetterTapAudioTest {
    @Test
    fun findGridLetterTap_playsLetterNameForEveryChapterWithFindGridMode() {
        val findGridStations =
            listOf(
                1 to Chapter1StationOrder.REVEAL_THEN_CHOOSE,
                2 to 2,
                4 to Chapter1StationOrder.REVEAL_THEN_CHOOSE,
                5 to 3,
                TrainingV1Config.CHAPTER_ID to TrainingV1Config.STATION_FIND_HEARD_LETTER_IN_GRID,
            )
        for ((chapterId, stationId) in findGridStations) {
            val plan =
                when (chapterId) {
                    1 -> StationQuizPlans.chapter1(stationId)
                    2 -> StationQuizPlans.chapter2(stationId)
                    4 -> StationQuizPlans.chapter4(stationId)
                    5 -> StationQuizPlans.chapter5(stationId)
                    TrainingV1Config.CHAPTER_ID -> StationQuizPlans.trainingV1(stationId)
                    else -> error("unexpected chapterId=$chapterId")
                }
            assertEquals(StationQuizMode.FindLetterGrid, plan.mode)
        }
    }

    @Test
    fun findGridActions_speaksLetterOnEveryTap_withoutChapterGuard() {
        val source = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/FindGridActions.kt")
        assertTrue(source.contains("rawVoice.playRawBlocking(resId)"))
        assertFalse(source.contains("chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5"))
    }

    @Test
    fun gameQuestionHost_wiresLetterTapAudioWheneverAudioEnabled() {
        val host = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/GameQuestionHost.kt")
        assertTrue(host.contains("if (ui.audioEnabled)"))
        assertTrue(host.contains("handleFindGridSagaGridLetterTapped"))
        assertFalse(host.contains("handleNonStagedCorrectTap"))
    }

    private fun readProjectSource(relativePath: String): String {
        val candidates =
            listOf(
                File(relativePath),
                File("../$relativePath"),
            )
        return candidates.first { it.isFile }.readText()
    }
}
