package com.tal.hebrewdino.ui.audio

import androidx.annotation.RawRes
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.domain.ChallengeType

object StationMediaClips {
    @RawRes val station1InstructionsResId: Int = R.raw.station1_instructions

    @RawRes val station2InstructionsResId: Int = R.raw.station2_instructions

    @RawRes val station3InstructionsResId: Int = R.raw.station3_instructions

    @RawRes val successResId: Int = R.raw.station_success

    @RawRes val errorResId: Int = R.raw.station_error

    @RawRes
    fun instructionsResId(challengeType: ChallengeType): Int =
        when (challengeType) {
            ChallengeType.LETTER_RECOGNITION -> station1InstructionsResId
            ChallengeType.PHONEMIC_ISOLATION -> station2InstructionsResId
            else -> 0
        }

    fun isNativeAudioStation(challengeType: ChallengeType): Boolean =
        challengeType == ChallengeType.LETTER_RECOGNITION ||
            challengeType == ChallengeType.PHONEMIC_ISOLATION
}
