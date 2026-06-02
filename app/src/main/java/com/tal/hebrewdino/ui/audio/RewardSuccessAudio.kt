package com.tal.hebrewdino.ui.audio

import androidx.annotation.RawRes
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.data.DinoCharacter
import kotlin.random.Random

/** Session-scoped last reward clip (survives across reward screens in one app session). */
object RewardSuccessAudioSession {
    @RawRes @Volatile var lastPlayedRawResId: Int? = null

    internal fun resetForTests() {
        lastPlayedRawResId = null
    }
}

/**
 * Reward-screen companion speech (`res/raw` MP3 only).
 * Each companion has its own emotional pool — no neutral narrator clips in active selection.
 */
object RewardSuccessAudio {
    private val DinoClips: IntArray =
        intArrayOf(
            R.raw.reward_dino_01,
            R.raw.reward_dino_02,
            R.raw.reward_dino_03,
            R.raw.reward_dino_04,
            R.raw.reward_dino_05,
            R.raw.reward_dino_06,
            R.raw.reward_dino_07,
            R.raw.reward_dino_08,
        )

    private val DinaClips: IntArray =
        intArrayOf(
            R.raw.reward_dina_01,
            R.raw.reward_dina_02,
            R.raw.reward_dina_03,
            R.raw.reward_dina_04,
            R.raw.reward_dina_05,
            R.raw.reward_dina_06,
            R.raw.reward_dina_07,
            R.raw.reward_dina_08,
        )

    fun poolFor(companion: DinoCharacter): IntArray =
        when (companion) {
            DinoCharacter.Dino -> DinoClips
            DinoCharacter.Dina -> DinaClips
        }

    /**
     * Picks a reward speech clip, avoiding [avoidRawResId] when it is in this companion's pool
     * and another option exists.
     */
    @RawRes
    fun pick(
        companion: DinoCharacter,
        avoidRawResId: Int? = RewardSuccessAudioSession.lastPlayedRawResId,
        random: Random = Random,
    ): Int {
        val pool = poolFor(companion)
        val candidates =
            if (avoidRawResId != null && pool.contains(avoidRawResId) && pool.size > 1) {
                pool.filter { it != avoidRawResId }.toIntArray()
            } else {
                pool
            }
        val effective = if (candidates.isNotEmpty()) candidates else pool
        return effective[random.nextInt(effective.size)]
    }

    @RawRes
    fun pickAndRemember(
        companion: DinoCharacter,
        random: Random = Random,
    ): Int {
        val picked = pick(companion, RewardSuccessAudioSession.lastPlayedRawResId, random)
        RewardSuccessAudioSession.lastPlayedRawResId = picked
        return picked
    }
}
