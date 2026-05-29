package com.tal.hebrewdino.ui.companion

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.domain.Chapter1Config

/**
 * Season 1 Chapter 1 only — companion Dino art + raw voice (not station instructions).
 */
enum class CompanionRewardCelebrationStyle {
    /** Station 1 — happy pose. */
    Happy,
    /** Station 2 — gentle bounce. */
    Bounce,
    /** Station 3 — talk frames while voice plays. */
    Talk,
    /** Station 4 — playful wiggle. */
    Wiggle,
    /** Station 5 — happy + light sparkles. */
    Sparkle,
    /** Station 6 — happier celebration + stronger sparkles. */
    GrandFinale,
}

object Chapter1DinoCompanionPilot {
    /** Matches [dino_intro_help_find_eggs_v2] narration (RTL). */
    const val INTRO_SPEECH_TEXT: String =
        "\u200Fהיי! אני דינו.\n" +
            "אמא שלי איבדה שלוש ביצים…\n" +
            "בכל משימה שתפתור, נתקדם עוד קצת ונמצא רמז.\n" +
            "תעזור לי?"

    /** Brief beat after success audio ends before returning to the journey map. */
    const val REWARD_POST_AUDIO_MS: Long = 350L

    /** Safety ceiling if a raw clip fails to complete (normal path uses full playback). */
    const val REWARD_AUDIO_MAX_WAIT_MS: Long = 15_000L

    /** Fixed slot size for reward / standard companion poses (prevents layout jump between frames). */
    val portraitWidthDp = 200
    val portraitHeightDp = 200

    /** Intro screen — ~22% larger so Dino balances the speech bubble. */
    val introPortraitWidthDp = 245
    val introPortraitHeightDp = 245
    val introPortraitCompactDp = 195

    /** Chapter finale — prominent next to the found egg. */
    val finalePortraitWidthDp = 248
    val finalePortraitHeightDp = 248
    val finalePortraitCompactDp = 196

    fun introPortraitSize(isCompact: Boolean): Pair<Int, Int> =
        if (isCompact) {
            introPortraitCompactDp to introPortraitCompactDp
        } else {
            introPortraitWidthDp to introPortraitHeightDp
        }

    fun finalePortraitSize(isCompact: Boolean): Pair<Int, Int> =
        if (isCompact) {
            finalePortraitCompactDp to finalePortraitCompactDp
        } else {
            finalePortraitWidthDp to finalePortraitHeightDp
        }

    @DrawableRes val poseHelp: Int = R.drawable.companion_dino_help

    @DrawableRes val poseIdle: Int = R.drawable.companion_dino_idle

    @DrawableRes val poseHappy: Int = R.drawable.companion_dino_happy

    @DrawableRes val poseEncourage: Int = R.drawable.companion_dino_encourage

    @DrawableRes val poseMomIdle: Int = R.drawable.companion_mom_idle

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

    /** Short journey hint shown on station reward (visual only; no extra audio). */
    fun journeyProgressCueForStation(stationId: Int): String =
        when (stationId.coerceIn(1, Chapter1Config.STATION_COUNT)) {
            1, 4 -> "\u200Fמצאנו רמז!"
            2, 5 -> "\u200Fעוד צעד בדרך!"
            3 -> "\u200Fמתקדמים לביצה!"
            6 -> "\u200Fכמעט שם!"
            else -> "\u200Fמתקדמים!"
        }

    fun rewardCelebrationForStation(stationId: Int): CompanionRewardCelebrationStyle =
        when (stationId.coerceIn(1, Chapter1Config.STATION_COUNT)) {
            1 -> CompanionRewardCelebrationStyle.Happy
            2 -> CompanionRewardCelebrationStyle.Bounce
            3 -> CompanionRewardCelebrationStyle.Talk
            4 -> CompanionRewardCelebrationStyle.Wiggle
            5 -> CompanionRewardCelebrationStyle.Sparkle
            6 -> CompanionRewardCelebrationStyle.GrandFinale
            else -> CompanionRewardCelebrationStyle.Happy
        }
}
