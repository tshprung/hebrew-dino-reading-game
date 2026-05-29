package com.tal.hebrewdino.ui.companion

import androidx.annotation.DrawableRes
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.data.DinoCharacter

data class CompanionAssets(
    @DrawableRes val poseIdle: Int,
    @DrawableRes val poseHappy: Int,
    @DrawableRes val poseHelp: Int,
    @DrawableRes val poseEncourage: Int,
    val talkFrameResIds: List<Int>,
) {
    companion object {
        fun forCharacter(character: DinoCharacter): CompanionAssets =
            when (character) {
                DinoCharacter.Dino ->
                    CompanionAssets(
                        poseIdle = R.drawable.companion_dino_idle,
                        poseHappy = R.drawable.companion_dino_happy,
                        poseHelp = R.drawable.companion_dino_help,
                        poseEncourage = R.drawable.companion_dino_encourage,
                        talkFrameResIds =
                            listOf(
                                R.drawable.companion_dino_talk_1,
                                R.drawable.companion_dino_talk_2,
                            ),
                    )
                DinoCharacter.Dina ->
                    CompanionAssets(
                        poseIdle = R.drawable.companion_dina_idle,
                        poseHappy = R.drawable.companion_dina_happy,
                        poseHelp = R.drawable.companion_dina_help,
                        poseEncourage = R.drawable.companion_dina_encourage,
                        talkFrameResIds =
                            listOf(
                                R.drawable.companion_dina_talk_1,
                                R.drawable.companion_dina_talk_2,
                            ),
                    )
            }
    }
}

fun DinoCharacter.displayNameHebrew(): String =
    when (this) {
        DinoCharacter.Dino -> "דינו"
        DinoCharacter.Dina -> "דינה"
    }
