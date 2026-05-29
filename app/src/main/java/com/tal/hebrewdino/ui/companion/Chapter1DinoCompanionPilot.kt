package com.tal.hebrewdino.ui.companion

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.domain.Chapter1Config

/**
 * Season 1 Chapter 1 only — companion Dino art + raw voice (not station instructions).
 */
object Chapter1DinoCompanionPilot {
    /** Brief beat after success audio ends before returning to the journey map. */
    const val REWARD_POST_AUDIO_MS: Long = 350L

    /** Safety ceiling if a raw clip fails to complete (normal path uses full playback). */
    const val REWARD_AUDIO_MAX_WAIT_MS: Long = 15_000L

    /** Fixed slot size for all companion poses (prevents layout jump between frames). */
    val portraitWidthDp = 200
    val portraitHeightDp = 200

    @DrawableRes val poseHelp: Int = R.drawable.companion_dino_help

    @DrawableRes val poseIdle: Int = R.drawable.companion_dino_idle

    @DrawableRes val poseHappy: Int = R.drawable.companion_dino_happy

    @DrawableRes val poseEncourage: Int = R.drawable.companion_dino_encourage

    val talkFrameResIds: List<Int> =
        listOf(
            R.drawable.companion_dino_talk_1,
            R.drawable.companion_dino_talk_2,
        )

    @RawRes val introHelpFindEggs: Int = R.raw.dino_intro_help_find_eggs_v2

    @RawRes val chapterComplete: Int = R.raw.dino_chapter1_complete

    @RawRes
    fun successClipForStation(stationId: Int): Int =
        when (stationId.coerceIn(1, Chapter1Config.STATION_COUNT)) {
            1 -> R.raw.dino_success_station_1
            2 -> R.raw.dino_success_station_2
            3 -> R.raw.dino_success_station_3
            4 -> R.raw.dino_success_station_4
            5 -> R.raw.dino_success_station_5
            6 -> R.raw.dino_success_station_6
            else -> R.raw.dino_success_station_1
        }
}
