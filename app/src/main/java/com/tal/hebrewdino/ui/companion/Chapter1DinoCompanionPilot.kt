package com.tal.hebrewdino.ui.companion

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import com.tal.hebrewdino.R
import kotlin.random.Random

/**
 * Season 1 Chapter 1 only — companion Dino art + raw voice (not station instructions).
 */
object Chapter1DinoCompanionPilot {
    const val REWARD_VISIBLE_MS: Long = 1500L

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

    @RawRes val introHelpFindEggs: Int = R.raw.dino_intro_help_find_eggs

    @RawRes val chapterComplete: Int = R.raw.dino_chapter1_complete

    private val successClips: IntArray =
        intArrayOf(
            R.raw.dino_success_1,
            R.raw.dino_success_2,
            R.raw.dino_success_3,
        )

    fun randomSuccessClip(): Int = successClips[Random.nextInt(successClips.size)]
}
