package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.companion.CompanionAssets
import com.tal.hebrewdino.ui.data.DinoCharacter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Season2RewardLayoutTest {
    @Test
    fun posterIsLargerThanCompanion() {
        assertTrue(Season2RewardLayout.POSTER_MAX_HEIGHT_COMPACT_DP > Season2RewardLayout.COMPANION_SIZE_COMPACT_DP)
        assertTrue(Season2RewardLayout.POSTER_MAX_WIDTH_COMPACT_DP > Season2RewardLayout.COMPANION_SIZE_COMPACT_DP)
        assertTrue(
            Season2RewardLayout.COMPANION_SIZE_COMPACT_DP <=
                (
                    Season2RewardLayout.POSTER_MAX_HEIGHT_COMPACT_DP *
                        Season2RewardLayout.COMPANION_MAX_POSTER_HEIGHT_RATIO
                ).toInt(),
        )
    }

    @Test
    fun headlineCelebratoryButNotOversized() {
        assertTrue(Season2RewardLayout.HEADLINE_SP_COMPACT in 26..34)
        assertTrue(Season2RewardLayout.SUBLINE_SP_COMPACT < Season2RewardLayout.HEADLINE_SP_COMPACT)
    }

    @Test
    fun continueButtonSizedForChildTap() {
        assertTrue(Season2RewardLayout.CONTINUE_MIN_WIDTH_DP >= 160)
        assertTrue(Season2RewardLayout.CONTINUE_MAX_WIDTH_DP <= 220)
        assertTrue(Season2RewardLayout.CONTINUE_HEIGHT_DP >= 44)
    }

    @Test
    fun rewardUsesSelectedCompanionAssets_notLegacyDrawables() {
        val dino = CompanionAssets.forCharacter(DinoCharacter.Dino)
        val dina = CompanionAssets.forCharacter(DinoCharacter.Dina)
        assertEquals(R.drawable.companion_dino_idle, dino.poseIdle)
        assertEquals(R.drawable.companion_dina_idle, dina.poseIdle)
        assertEquals(R.drawable.companion_dino_talk_1, dino.talkFrameResIds.first())
    }

    @Test
    fun chapterCompletionTrigger_unchangedByLayoutPolicy() {
        assertFalse(
            Season2Copy.isChapterComplete(
                chapterIndex = 1,
                completedChapters = emptySet(),
                completedStations = (1..5).toSet(),
            ),
        )
        assertTrue(
            Season2Copy.isChapterComplete(
                chapterIndex = 1,
                completedChapters = emptySet(),
                completedStations = (1..6).toSet(),
            ),
        )
    }
}
