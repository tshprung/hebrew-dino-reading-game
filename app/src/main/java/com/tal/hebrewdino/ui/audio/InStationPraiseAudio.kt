package com.tal.hebrewdino.ui.audio

import androidx.annotation.RawRes
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.domain.TrainingV1Config
import kotlin.random.Random

/**
 * Short varied praise clips for correct answers **inside** stations (Chapters 1–6 + training).
 * Not used on reward screens — see [RewardSuccessAudio].
 */
object InStationPraiseAudio {
    private val PraiseShortPool: IntArray =
        intArrayOf(
            R.raw.praise_short_01,
            R.raw.praise_short_02,
            R.raw.praise_short_03,
            R.raw.praise_short_04,
            R.raw.praise_short_05,
            R.raw.praise_short_06,
            R.raw.praise_short_07,
            R.raw.praise_short_08,
            R.raw.praise_short_09,
            R.raw.praise_short_10,
        )

    fun pool(): IntArray = PraiseShortPool

    fun usesRawPraisePool(chapterId: Int?): Boolean =
        chapterId == 1 ||
            chapterId == 2 ||
            chapterId == 3 ||
            chapterId == 4 ||
            chapterId == 5 ||
            chapterId == 6 ||
            chapterId == TrainingV1Config.CHAPTER_ID

    /** Tracking key stored in [com.tal.hebrewdino.ui.screens.GameAudioRuntimeState.lastPraiseAssetPath]. */
    fun trackingKey(@RawRes rawResId: Int): String = "raw:$rawResId"

    @RawRes
    fun rawResIdFromTrackingKey(trackingKey: String?): Int? {
        if (trackingKey == null || !trackingKey.startsWith("raw:")) return null
        return trackingKey.removePrefix("raw:").toIntOrNull()
    }

    /**
     * Picks an in-station praise clip, avoiding [avoidRawResId] when the pool has another option.
     */
    @RawRes
    fun pick(
        avoidRawResId: Int? = null,
        random: Random = Random,
    ): Int {
        val pool = PraiseShortPool
        val candidates =
            if (avoidRawResId != null && pool.size > 1) {
                pool.filter { it != avoidRawResId }.toIntArray()
            } else {
                pool
            }
        val effective = if (candidates.isNotEmpty()) candidates else pool
        return effective[random.nextInt(effective.size)]
    }
}
