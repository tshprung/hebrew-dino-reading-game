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
 * Diversified reward-screen success voice lines (`res/raw` MP3 only).
 * Does not include legacy `dino_success_station_*` clips.
 */
object RewardSuccessAudio {
    private val NeutralClips: IntArray =
        intArrayOf(
            R.raw.reward_success_neutral_01,
            R.raw.reward_success_neutral_02,
            R.raw.reward_success_neutral_03,
            R.raw.reward_success_neutral_04,
            R.raw.reward_success_neutral_05,
            R.raw.reward_success_neutral_06,
            R.raw.reward_success_neutral_07,
            R.raw.reward_success_neutral_08,
            R.raw.reward_success_neutral_09,
            R.raw.reward_success_neutral_10,
            R.raw.reward_success_neutral_11,
            R.raw.reward_success_neutral_12,
        )

    @RawRes val dinoClip: Int = R.raw.reward_success_dino_01

    @RawRes val dinaClip: Int = R.raw.reward_success_dina_01

    fun neutralPool(): IntArray = NeutralClips

    fun poolFor(companion: DinoCharacter): IntArray =
        when (companion) {
            DinoCharacter.Dino -> NeutralClips + dinoClip
            DinoCharacter.Dina -> NeutralClips + dinaClip
        }

    /**
     * Picks a reward success clip, avoiding [avoidRawResId] when the pool has another option.
     */
    @RawRes
    fun pick(
        companion: DinoCharacter,
        avoidRawResId: Int? = RewardSuccessAudioSession.lastPlayedRawResId,
        random: Random = Random,
    ): Int {
        val pool = poolFor(companion)
        val candidates =
            if (avoidRawResId != null && pool.size > 1) {
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
