package com.tal.hebrewdino.ui.companion

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.data.PlayerAddress
import com.tal.hebrewdino.ui.domain.Chapter1Config

/**
 * Season 1 Chapter 1 only — companion layout sizes, reward styles, and neutral success audio.
 * Drawable poses come from [CompanionAssets].
 */
enum class CompanionRewardCelebrationStyle {
    Happy,
    Bounce,
    Talk,
    Wiggle,
    Sparkle,
    GrandFinale,
}

object Chapter1DinoCompanionPilot {
    const val REWARD_POST_AUDIO_MS: Long = 350L
    const val REWARD_AUDIO_MAX_WAIT_MS: Long = 15_000L

    val portraitWidthDp = 200
    val portraitHeightDp = 200

    val introPortraitWidthDp = 245
    val introPortraitHeightDp = 245
    val introPortraitCompactDp = 195

    /** Chapter finale — companion should feel emotionally present beside the found egg. */
    val finalePortraitWidthDp = 280
    val finalePortraitHeightDp = 280
    val finalePortraitCompactDp = 220

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

    @DrawableRes val poseMomIdle: Int = R.drawable.companion_mom_idle

    fun assets(character: DinoCharacter): CompanionAssets = CompanionAssets.forCharacter(character)

    fun introSpeechText(
        character: DinoCharacter,
        address: PlayerAddress,
    ): String = Chapter1CompanionCopy.introSpeechText(character, address)

    @RawRes
    fun introRawRes(
        character: DinoCharacter,
        address: PlayerAddress,
    ): Int = Chapter1CompanionCopy.introRawRes(character, address)

    @RawRes val chapterComplete: Int = R.raw.dino_chapter1_complete_neutral

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
