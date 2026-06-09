package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.ui.data.PlayerAddress
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2GuessingHintCopyTest {
    @Test
    fun openers_useAddressAwareWording() {
        assertTrue(Season2GuessingHintCopy.openers(PlayerAddress.Boy).contains("בוא נקשיב"))
        assertTrue(Season2GuessingHintCopy.openers(PlayerAddress.Girl).contains("בואי נקשיב"))
    }

    @Test
    fun coachBubbleText_staysShort() {
        val hint =
            Season2GuessingHintCopy.coachBubbleText(
                Season2Chapter1StationOrder.PICK_LETTER,
                PlayerAddress.Boy,
            )
        assertTrue(hint.contains("נקשיב שוב"))
        assertTrue(hint.contains("איזו אות שמענו"))
        assertTrue(hint.length < 80)
    }

    @Test
    fun hintForStation_popBalloons_mentionsHeardLetter() {
        val hint =
            Season2GuessingHintCopy.hintForStation(
                Season2Chapter1StationOrder.POP_BALLOONS,
                PlayerAddress.Boy,
            )
        assertTrue(hint.contains("נחפש את האות ששמענו"))
    }

    @Test
    fun hintForStation_pickLetter_asksWhichLetter() {
        val hint =
            Season2GuessingHintCopy.hintForStation(
                Season2Chapter1StationOrder.PICK_LETTER,
                PlayerAddress.Girl,
            )
        assertTrue(hint.contains("איזו אות שמענו"))
    }

    @Test
    fun hintForStation_memoryMatch_encouragesRemembering() {
        val hint =
            Season2GuessingHintCopy.hintForStation(
                Season2Chapter1StationOrder.MEMORY_MATCH,
                PlayerAddress.Boy,
            )
        assertTrue(hint.contains("ננסה לזכור"))
    }

    @Test
    fun processPraise_returnsNonEmptyAddressAwareText() {
        val praise = Season2GuessingHintCopy.processPraise(PlayerAddress.Girl)
        assertTrue(praise.contains("הקשבת") || praise.contains("ריכוז") || praise.contains("חשבת"))
    }
}
