package com.tal.hebrewdino.ui.audio

import androidx.annotation.RawRes
import com.tal.hebrewdino.ui.data.DinoCharacter
import kotlin.random.Random

/** Companion-specific Season 2 map praise and focus feedback (res/raw MP3). */
object Season2CompanionFeedbackAudio {
    @RawRes
    fun pickMapReturnPraise(
        companion: DinoCharacter,
        avoidRawResId: Int = 0,
        random: Random = Random,
    ): Int = pickFromPool(Season2RawAudio.mapPraisePool(companion), avoidRawResId, random)

    @RawRes
    fun pickFocusLine(
        companion: DinoCharacter,
        avoidRawResId: Int = 0,
        random: Random = Random,
    ): Int = pickFromPool(Season2RawAudio.focusPool(companion), avoidRawResId, random)

    fun mapPraiseCaption(@RawRes rawResId: Int): String =
        Season2RawAudio.mapPraiseCaption(rawResId)
            ?: error("Missing Season 2 map praise caption for rawResId=$rawResId")

    @RawRes
    private fun pickFromPool(
        pool: IntArray,
        avoidRawResId: Int,
        random: Random,
    ): Int {
        require(pool.isNotEmpty()) { "Season2 companion feedback pool must not be empty" }
        val candidates =
            if (avoidRawResId != 0 && pool.size > 1) {
                pool.filter { it != avoidRawResId }.toIntArray()
            } else {
                pool
            }
        val effective = if (candidates.isNotEmpty()) candidates else pool
        return effective[random.nextInt(effective.size)]
    }
}
