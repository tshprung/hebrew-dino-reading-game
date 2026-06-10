package com.tal.hebrewdino.ui.audio

import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.data.DinoCharacter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class Season2CompanionFeedbackAudioTest {
    @Test
    fun mapPraise_usesCompanionSpecificPools() {
        val dinoPool = Season2RawAudio.mapPraisePool(DinoCharacter.Dino).toSet()
        val dinaPool = Season2RawAudio.mapPraisePool(DinoCharacter.Dina).toSet()
        assertEquals(5, dinoPool.size)
        assertEquals(5, dinaPool.size)
        assertTrue(R.raw.season2_map_praise_dino_01 in dinoPool)
        assertTrue(R.raw.season2_map_praise_dina_01 in dinaPool)
        assertTrue(dinoPool.intersect(dinaPool).isEmpty())
    }

    @Test
    fun focusLines_useCompanionSpecificPools() {
        val dinoPool = Season2RawAudio.focusPool(DinoCharacter.Dino).toSet()
        val dinaPool = Season2RawAudio.focusPool(DinoCharacter.Dina).toSet()
        assertEquals(3, dinoPool.size)
        assertEquals(3, dinaPool.size)
        assertTrue(R.raw.season2_focus_dino_01 in dinoPool)
        assertTrue(R.raw.season2_focus_dina_01 in dinaPool)
    }

    @Test
    fun mapPraise_avoidsImmediateRepeatWhenPoolHasAlternatives() {
        val first = Season2CompanionFeedbackAudio.pickMapReturnPraise(DinoCharacter.Dino, avoidRawResId = 0, random = Random(1))
        val second =
            Season2CompanionFeedbackAudio.pickMapReturnPraise(
                DinoCharacter.Dino,
                avoidRawResId = first,
                random = Random(1),
            )
        assertNotEquals(first, second)
    }
}
