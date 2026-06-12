package com.tal.hebrewdino.ui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2WordPartsUxPolishTest {
    @Test
    fun imageTap_usesFullWordThenParts_noInstruction() {
        val audio = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/audio/Season2WordPartsAudio.kt")
        assertTrue(audio.contains("fun playPictureTapSequence"))
        assertTrue(audio.contains("playCorrectFullWord"))
        assertTrue(audio.contains("playPartsSequence"))
        val gameScreen = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/GameScreen.kt")
        assertTrue(gameScreen.contains("handleWordPartsPictureTap"))
        assertTrue(gameScreen.contains("playPictureTapSequence"))
    }

    @Test
    fun replayButton_keepsInstructionFullWordParts() {
        val help = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/Episode4HelpController.kt")
        assertTrue(help.contains("replayAdvancedInstructionAndWord"))
        val stationAudio = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/domain/Season2StationAudio.kt")
        assertTrue(stationAudio.contains("speakAdvancedModeInstruction"))
        assertTrue(stationAudio.contains("playPartsSequence"))
    }

    @Test
    fun word_visible_area_not_empty_visible_guided() {
        val ui = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/Season2WordPartsGame.kt")
        assertTrue(ui.contains("WordPartsTargetWordBox"))
        assertTrue(ui.contains("VisibleWordParts"))
        assertTrue(ui.contains("GuidedWordParts"))
        assertFalse(ui.contains("WordPartsTargetWordBox") && ui.contains("under image"))
    }

    @Test
    fun options_moved_down_if_word_area_grows() {
        val ui = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/Season2WordPartsGame.kt")
        assertTrue(ui.contains("val heroHeight = maxOf(cardH, infoColumnHeight)"))
        assertTrue(ui.contains("wordLineMinHeight + hintReserve"))
    }

    @Test
    fun hint_side_pill_one_line_wide_short() {
        val ui = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/Season2WordPartsGame.kt")
        assertTrue(ui.contains("WordPartsHintEquationPill"))
        assertTrue(ui.contains("maxLines = 1"))
        assertTrue(ui.contains("RoundedCornerShape(999.dp)"))
        assertTrue(ui.contains(".fillMaxWidth()"))
    }

    @Test
    fun normal_no_empty_side_slot() {
        val ui = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/Season2WordPartsGame.kt")
        assertTrue(ui.contains("if (showInfoColumn)"))
        assertTrue(ui.contains("showInfoColumn = showFullWord || hintEquationText != null"))
    }

    @Test
    fun correct_only_correct_option_visible() {
        val ui = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/Season2WordPartsGame.kt")
        assertTrue(ui.contains("val optionsToShow ="))
        assertTrue(ui.contains("listOf(correctSplit)"))
        assertTrue(ui.contains("enabled = enabled && !isCorrectState"))
    }

    @Test
    fun correct_state_persists_until_next_round_no_old_options_flash() {
        val actions = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/Season2AdvancedStationActions.kt")
        assertFalse(actions.contains("wordPartsCompletedEquation = null"))
        assertTrue(actions.contains("if (gameViewModel.wordPartsCompletedEquation != null) return"))
        val roundStart = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/GameRoundStartActions.kt")
        assertTrue(roundStart.contains("wordPartsCompletedEquation = null"))
    }

    @Test
    fun all_taps_cancel_previous_audio_job() {
        val actions = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/Season2AdvancedStationActions.kt")
        assertTrue(actions.contains("fun interruptWordPartsVoice"))
        assertTrue(actions.contains("rawVoice?.stopNow()"))
        val gameScreen = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/GameScreen.kt")
        assertTrue(gameScreen.contains("interruptWordPartsVoice"))
    }

    @Test
    fun correct_locks_answer_taps_until_next_round() {
        val actions = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/Season2AdvancedStationActions.kt")
        assertTrue(actions.contains("if (gameViewModel.wordPartsCompletedEquation != null) return"))
        val ui = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/Season2WordPartsGame.kt")
        assertTrue(ui.contains("enabled = enabled && !isCorrectState"))
    }

    @Test
    fun Ch3St6_hidden_preserved() {
        val ui = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/Season2WordPartsGame.kt")
        assertTrue(ui.contains("HiddenWordPartsChallenge"))
        assertTrue(ui.contains("showInfoColumn = showFullWord || hintEquationText != null"))
        assertTrue(ui.contains("hintReserve =\n                if (!isHidden)"))
    }

    @Test
    fun instruction_bg_wrap_content() {
        val ui = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/Season2WordPartsGame.kt")
        assertTrue(ui.contains("text = instructionText"))
        assertTrue(ui.contains("wrapContentWidth(Alignment.CenterHorizontally)"))
        val instructionBlock = ui.substringAfter("text = instructionText").substringBefore("Row(")
        assertFalse(instructionBlock.contains(".fillMaxWidth()"))
    }

    @Test
    fun target_word_bg_wrap_content() {
        val ui = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/Season2WordPartsGame.kt")
        assertTrue(ui.contains("wrapContentWidth(Alignment.Start)"))
        assertTrue(ui.contains("WordPartsTargetWordBox"))
    }

    @Test
    fun image_down_left_nudge() {
        val ui = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/Season2WordPartsGame.kt")
        assertTrue(ui.contains("Season2Ch2St6WordPartsPolicy.ImagePhysicalLeftDp"))
        assertTrue(ui.contains("if (isCh2St6)"))
    }

    @Test
    fun options_down_nudge() {
        val ui = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/Season2WordPartsGame.kt")
        assertTrue(ui.contains("Season2Ch2St6WordPartsPolicy.OptionsDownDp"))
    }

    @Test
    fun wordparts_states_unchanged() {
        val ui = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/Season2WordPartsGame.kt")
        assertTrue(ui.contains("showInfoColumn = showFullWord || hintEquationText != null"))
        assertTrue(ui.contains("val optionsToShow ="))
        assertTrue(ui.contains("listOf(correctSplit)"))
    }

    @Test
    fun audio_unchanged() {
        val audio = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/audio/Season2WordPartsAudio.kt")
        assertTrue(audio.contains("playPictureTapSequence"))
        assertTrue(audio.contains("playSplitTapSequence"))
    }

    @Test
    fun wordPartsLayout_rtlAndOptionScale() {
        val ui = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/game/Season2WordPartsGame.kt")
        assertTrue(ui.contains("LocalLayoutDirection provides LayoutDirection.Rtl"))
        assertTrue(ui.contains("OptionScale = 1.5f"))
    }

    @Test
    fun mapNumbers_visibleFromStart_clickPolicyUnchanged() {
        val map = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/Season2PuzzleMapPrototypeScreen.kt")
        assertTrue(map.contains("replayStationNumber = stationNumber"))
        assertTrue(map.contains("Season2PuzzleMapTileClickPolicy.isTileClickable"))
    }

    @Test
    fun S1_unchanged() {
        assertEquals(
            com.tal.hebrewdino.ui.domain.StationQuizMode.PickLetter,
            com.tal.hebrewdino.ui.domain.StationQuizPlans.chapter1(
                com.tal.hebrewdino.ui.domain.Chapter1StationOrder.TAP_LETTER,
            ).mode,
        )
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
