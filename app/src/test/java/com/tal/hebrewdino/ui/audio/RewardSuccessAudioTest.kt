package com.tal.hebrewdino.ui.audio

import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.data.DinoCharacter
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun dinoPool_hasExactlyEightCharacterClips() {
        val pool = RewardSuccessAudio.poolFor(DinoCharacter.Dino)
        assertEquals(8, pool.size)
        pool.forEach { assertTrue(it != 0) }
        assertTrue(pool.contains(R.raw.reward_dino_01))
        assertTrue(pool.contains(R.raw.reward_dino_08))
    }

    @Test
    fun dinaPool_hasExactlyEightCharacterClips() {
        val pool = RewardSuccessAudio.poolFor(DinoCharacter.Dina)
        assertEquals(8, pool.size)
        pool.forEach { assertTrue(it != 0) }
        assertTrue(pool.contains(R.raw.reward_dina_01))
        assertTrue(pool.contains(R.raw.reward_dina_08))
    }

    @Test
    fun activePools_useOnlyCompanionRewardClips() {
        val dinoPool = RewardSuccessAudio.poolFor(DinoCharacter.Dino).toSet()
        val dinaPool = RewardSuccessAudio.poolFor(DinoCharacter.Dina).toSet()
        val expectedDino =
            setOf(
                R.raw.reward_dino_01,
                R.raw.reward_dino_02,
                R.raw.reward_dino_03,
                R.raw.reward_dino_04,
                R.raw.reward_dino_05,
                R.raw.reward_dino_06,
                R.raw.reward_dino_07,
                R.raw.reward_dino_08,
            )
        val expectedDina =
            setOf(
                R.raw.reward_dina_01,
                R.raw.reward_dina_02,
                R.raw.reward_dina_03,
                R.raw.reward_dina_04,
                R.raw.reward_dina_05,
                R.raw.reward_dina_06,
                R.raw.reward_dina_07,
                R.raw.reward_dina_08,
            )
        assertEquals(expectedDino, dinoPool)
        assertEquals(expectedDina, dinaPool)
    }

    @Test
    fun pick_returnsValidRawId_fromCompanionPool() {
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
    fun pick_noImmediateRepeat_ignoresPreviousFromOtherCompanionPool() {
        val dinoClip = RewardSuccessAudio.pick(DinoCharacter.Dino, avoidRawResId = null, random = Random(3))
        val dinaClip = RewardSuccessAudio.pick(DinoCharacter.Dina, avoidRawResId = dinoClip, random = Random(3))
        assertTrue(RewardSuccessAudio.poolFor(DinoCharacter.Dina).contains(dinaClip))
    }

    @Test
    fun pickAndRemember_updatesSession() {
        val picked = RewardSuccessAudio.pickAndRemember(DinoCharacter.Dino, random = Random(2))
        assertEquals(picked, RewardSuccessAudioSession.lastPlayedRawResId)
    }
}
