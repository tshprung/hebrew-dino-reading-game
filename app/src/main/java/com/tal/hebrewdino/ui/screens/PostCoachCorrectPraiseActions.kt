package com.tal.hebrewdino.ui.screens

import com.tal.hebrewdino.ui.audio.BackgroundMusicPlayer
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.Season2PostFocusCorrectAudio
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.data.DinoCharacter

/** Plays Dino/Dina praise after companion coach help — instead of narrator praise clips. */
internal object PostCoachCorrectPraiseActions {
    suspend fun playInStationIfCoachHelped(
        hadCoachIntervention: Boolean,
        companion: DinoCharacter?,
        rawVoice: RawVoicePlayer?,
        backgroundMusic: BackgroundMusicPlayer?,
        avoidRawResId: Int = 0,
    ): Int? {
        if (!hadCoachIntervention || companion == null || rawVoice == null) return null
        return Season2PostFocusCorrectAudio.playBlocking(
            companion = companion,
            rawVoice = rawVoice,
            backgroundMusic = backgroundMusic,
            avoidRawResId = avoidRawResId,
        )
    }

    suspend fun playInStationOrNarratorPraise(
        hadCoachIntervention: Boolean,
        companion: DinoCharacter?,
        rawVoice: RawVoicePlayer?,
        backgroundMusic: BackgroundMusicPlayer?,
        voice: VoicePlayer,
        audioRuntime: GameAudioRuntimeState,
        chapterId: Int,
        stationId: Int?,
        narratorCandidates: Array<String>,
        avoidCompanionRawResId: Int = 0,
        context: String,
        onCompanionPraisePlayed: (Int) -> Unit = {},
    ) {
        val companionRes =
            playInStationIfCoachHelped(
                hadCoachIntervention = hadCoachIntervention,
                companion = companion,
                rawVoice = rawVoice,
                backgroundMusic = backgroundMusic,
                avoidRawResId = avoidCompanionRawResId,
            )
        if (companionRes != null) {
            onCompanionPraisePlayed(companionRes)
            return
        }
        GameAudioActions.playPraiseNoImmediateRepeat(
            voice = voice,
            audioRuntime = audioRuntime,
            candidates = narratorCandidates,
            chapterId = chapterId,
            stationId = stationId,
            context = context,
            rawVoice = rawVoice,
        )
    }
}
