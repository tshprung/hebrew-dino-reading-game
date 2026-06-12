package com.tal.hebrewdino.ui.companion

import androidx.annotation.DrawableRes
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.data.DinoCharacter

data class CompanionAssets(
    @get:DrawableRes val poseIdle: Int,
    @get:DrawableRes val poseHappy: Int,
    @get:DrawableRes val poseHelp: Int,
    @get:DrawableRes val poseEncourage: Int,
    val talkFrameResIds: List<Int>,
) {
    companion object {
        private val DINO_ASSETS = CompanionAssets(
            poseIdle = R.drawable.companion_dino_idle,
            poseHappy = R.drawable.companion_dino_happy,
            poseHelp = R.drawable.companion_dino_help,
            poseEncourage = R.drawable.companion_dino_encourage,
            talkFrameResIds = listOf(
                R.drawable.companion_dino_talk_1,
                R.drawable.companion_dino_talk_2,
            ),
        )

        private val DINA_ASSETS = CompanionAssets(
            poseIdle = R.drawable.companion_dina_idle,
            poseHappy = R.drawable.companion_dina_happy,
            poseHelp = R.drawable.companion_dina_help,
            poseEncourage = R.drawable.companion_dina_encourage,
            talkFrameResIds = listOf(
                R.drawable.companion_dina_talk_1,
                R.drawable.companion_dina_talk_2,
            ),
        )

        fun forCharacter(character: DinoCharacter): CompanionAssets =
            when (character) {
                DinoCharacter.Dino -> DINO_ASSETS
                DinoCharacter.Dina -> DINA_ASSETS
            }
    }
}

val DinoCharacter.assets: CompanionAssets
    get() = CompanionAssets.forCharacter(this)

fun DinoCharacter.displayNameHebrew(): String =
    when (this) {
        DinoCharacter.Dino -> "דינו"
        DinoCharacter.Dina -> "דינה"
    }
