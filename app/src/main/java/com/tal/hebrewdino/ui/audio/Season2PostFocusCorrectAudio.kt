package com.tal.hebrewdino.ui.audio

import androidx.annotation.RawRes
import com.tal.hebrewdino.ui.data.DinoCharacter

/** Audible Dino/Dina praise after Season 2 two-mistake focus — not narrator praise_short. */
object Season2PostFocusCorrectAudio {
    suspend fun playBlocking(
        companion: DinoCharacter,
        rawVoice: RawVoicePlayer,
        backgroundMusic: BackgroundMusicPlayer?,
        avoidRawResId: Int,
    ): Int {
        @RawRes val praiseRes =
            Season2CompanionFeedbackAudio.pickPostFocusCorrectPraise(
                companion = companion,
                avoidRawResId = avoidRawResId,
            )
        if (backgroundMusic != null) {
            backgroundMusic.withVoiceDuck {
                rawVoice.playRawBlocking(praiseRes)
            }
        } else {
            rawVoice.playRawBlocking(praiseRes)
        }
        return praiseRes
    }
}
