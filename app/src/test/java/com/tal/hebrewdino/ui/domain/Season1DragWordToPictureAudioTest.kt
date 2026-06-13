package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season1DragWordToPictureAudioTest {
    @Test
    fun dragWordToPictureInstruction_mapsToSeason1RawAsset() {
        assertEquals(
            R.raw.s1_drag_word_to_picture_instruction,
            Season1StationAudio.dragWordToPictureInstructionRawResId(),
        )
    }

    @Test
    fun dragWordToPictureInstruction_appliesOnlyToSeason1Chapter3And6Station3() {
        assertTrue(Season1StationAudio.isSeason1DragWordToPictureStation(chapterId = 3, stationId = 3))
        assertTrue(Season1StationAudio.isSeason1DragWordToPictureStation(chapterId = 6, stationId = 3))
        assertFalse(Season1StationAudio.isSeason1DragWordToPictureStation(chapterId = 3, stationId = 2))
        assertFalse(Season1StationAudio.isSeason1DragWordToPictureStation(chapterId = 6, stationId = 4))
        assertFalse(Season1StationAudio.isSeason1DragWordToPictureStation(chapterId = 101, stationId = 3))
    }

    @Test
    fun dragWordToPictureWordAndReplay_helpersExist() {
        val source =
            listOf(
                java.io.File("app/src/main/java/com/tal/hebrewdino/ui/domain/Season1StationAudio.kt"),
                java.io.File("../app/src/main/java/com/tal/hebrewdino/ui/domain/Season1StationAudio.kt"),
            ).first { it.exists() }
                .readText()
        assertTrue(source.contains("fun playDragWordToPictureWord"))
    }

    @Test
    fun dragWordToPicture_replaysWordOnlyOnCorrectMatchAndLockedPictureTap() {
        val gameSource =
            listOf(
                java.io.File("app/src/main/java/com/tal/hebrewdino/ui/game/DragWordToPictureGame.kt"),
                java.io.File("../app/src/main/java/com/tal/hebrewdino/ui/game/DragWordToPictureGame.kt"),
            ).first { it.exists() }
                .readText()
        assertFalse(gameSource.contains("onWordSelected"))
        assertTrue(gameSource.contains("enabled = enabled && onPictureTapReplayWord != null"))
        assertTrue(gameSource.contains("onPictureTapReplayWord?.invoke(pair.catalogEntryId)"))

        val actionsSource =
            listOf(
                java.io.File("app/src/main/java/com/tal/hebrewdino/ui/screens/DragWordToPictureActions.kt"),
                java.io.File("../app/src/main/java/com/tal/hebrewdino/ui/screens/DragWordToPictureActions.kt"),
            ).first { it.exists() }
                .readText()
        assertTrue(actionsSource.contains("playDragWordToPictureWord"))
        assertTrue(actionsSource.contains("handleDropAttempt(correct)"))
        assertTrue(actionsSource.contains("launchFeedbackVoiceNoCancel"))
        assertTrue(actionsSource.contains("awaitFeedbackVoice"))
    }
}
