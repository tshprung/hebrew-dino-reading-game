package com.tal.hebrewdino.ui.audio

import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.data.DinoCharacter
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RewardSuccessAudioTest {
    @Before
    fun resetSession() {
        RewardSuccessAudioSession.resetForTests()
    }

    @Test
    fun neutralPool_hasTwelveClips_allDefined() {
        val pool = RewardSuccessAudio.neutralPool()
        assertEquals(12, pool.size)
        pool.forEach { assertTrue(it != 0) }
    }

    @Test
    fun dinoPool_includesNeutralAndDinoClip() {
        val pool = RewardSuccessAudio.poolFor(DinoCharacter.Dino)
        assertEquals(13, pool.size)
        assertTrue(pool.contains(R.raw.reward_success_dino_01))
        assertTrue(RewardSuccessAudio.neutralPool().all { pool.contains(it) })
    }

    @Test
    fun dinaPool_includesNeutralAndDinaClip() {
        val pool = RewardSuccessAudio.poolFor(DinoCharacter.Dina)
        assertEquals(13, pool.size)
        assertTrue(pool.contains(R.raw.reward_success_dina_01))
        assertTrue(RewardSuccessAudio.neutralPool().all { pool.contains(it) })
    }

    @Test
    fun pick_returnsValidRawId() {
        val picked = RewardSuccessAudio.pick(DinoCharacter.Dino, avoidRawResId = null, random = Random(0))
        assertTrue(picked != 0)
        assertTrue(RewardSuccessAudio.poolFor(DinoCharacter.Dino).contains(picked))
    }

    @Test
    fun pick_noImmediateRepeat_avoidsPreviousWhenPossible() {
        val first = RewardSuccessAudio.pick(DinoCharacter.Dina, avoidRawResId = null, random = Random(1))
        val second = RewardSuccessAudio.pick(DinoCharacter.Dina, avoidRawResId = first, random = Random(1))
        assertNotEquals(first, second)
    }

    @Test
    fun pickAndRemember_updatesSession() {
        val picked = RewardSuccessAudio.pickAndRemember(DinoCharacter.Dino, random = Random(2))
        assertEquals(picked, RewardSuccessAudioSession.lastPlayedRawResId)
    }
}
