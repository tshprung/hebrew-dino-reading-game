package com.tal.hebrewdino.ui.audio

import androidx.annotation.RawRes
import com.tal.hebrewdino.ui.domain.ChallengeType
import com.tal.hebrewdino.ui.domain.applyChildFriendlyTtsWorkarounds
import com.tal.hebrewdino.ui.domain.hebrewLetterBase
import com.tal.hebrewdino.ui.domain.letterNameSpokenForTts
import com.tal.hebrewdino.ui.domain.phonemeSpokenForTts
import com.tal.hebrewdino.ui.domain.wrongLetterFeedbackSpeech

/**
 * Station speech when `res/raw` clips are still placeholders (copies of menu BGM).
 * Prefers short `assets/audio` clips, then TTS.
 */
object StationVoiceGuide {
    /** Size of placeholder MP3s copied from menu BGM during asset staging. */
    const val PLACEHOLDER_RAW_BYTES: Long = 3_651_725L

    private const val MIN_PLAYABLE_BYTES: Long = 256L
    private const val MAX_REAL_RAW_BYTES: Long = 600_000L

    fun isRealRawClip(
        rawVoice: RawVoicePlayer,
        @RawRes resId: Int,
    ): Boolean {
        if (!rawVoice.isPlayable(resId)) return false
        return isRealRawLength(rawVoice.rawResourceLength(resId))
    }

    internal fun isRealRawLength(length: Long?): Boolean {
        if (length == null || length < MIN_PLAYABLE_BYTES) return false
        if (length == PLACEHOLDER_RAW_BYTES) return false
        if (length > MAX_REAL_RAW_BYTES) return false
        return true
    }

    suspend fun playWordChallengeInstructions(
        rawVoice: RawVoicePlayer,
        voice: VoicePlayer,
        tts: TextToSpeechManager,
        challengeType: ChallengeType,
        targetLetter: String,
        ttsInstruction: String,
    ) {
        val resId = StationMediaClips.instructionsResId(challengeType)
        if (resId != 0 && isRealRawClip(rawVoice, resId)) {
            rawVoice.playBlocking(resId)
            return
        }
        when (challengeType) {
            ChallengeType.LETTER_RECOGNITION ->
                playLetterRecognitionIntro(voice, tts, targetLetter, ttsInstruction)
            ChallengeType.PHONEMIC_ISOLATION ->
                playPhonemicIntro(voice, tts, targetLetter, ttsInstruction)
            else -> speakTtsIfNeeded(tts, ttsInstruction)
        }
    }

    suspend fun playStation3Instructions(
        rawVoice: RawVoicePlayer,
        voice: VoicePlayer,
        tts: TextToSpeechManager,
        targetLetter: String,
        ttsInstruction: String,
    ) {
        if (isRealRawClip(rawVoice, StationMediaClips.station3InstructionsResId)) {
            rawVoice.playBlocking(StationMediaClips.station3InstructionsResId)
            return
        }
        val base = hebrewLetterBase(targetLetter)
        val intro =
            listOf(AudioClips.PopBalloonsWithLetter, AudioClips.PopAllBalloonsWithLetter)
                .firstOrNull { voice.hasAsset(it) }
        val letterClip = AudioClips.letterNameClip(base)
        val parts =
            buildList {
                intro?.let { add(it) }
                letterClip?.let { add(it) }
            }.filter { voice.hasAsset(it) }
        if (parts.isNotEmpty()) {
            voice.playSequenceBlocking(*parts.toTypedArray())
        } else {
            speakTtsIfNeeded(tts, ttsInstruction)
        }
    }

    suspend fun playOptionTapFeedback(
        voice: VoicePlayer,
        tts: TextToSpeechManager,
        challengeType: ChallengeType,
        optionLetter: String,
        sfx: SfxManager,
    ) {
        val base = hebrewLetterBase(optionLetter)
        when (challengeType) {
            ChallengeType.LETTER_RECOGNITION -> {
                val clip = AudioClips.letterNameClip(base)
                if (clip != null && voice.hasAsset(clip)) {
                    voice.playBlocking(clip)
                } else {
                    val spoken = letterNameSpokenForTts(base)
                    if (spoken.isNotBlank()) {
                        tts.interruptAndSpeak(applyChildFriendlyTtsWorkarounds(spoken))
                    }
                }
            }
            ChallengeType.PHONEMIC_ISOLATION -> {
                val phoneme = phonemeSpokenForTts(base)
                if (phoneme.isNotBlank()) {
                    tts.interruptAndSpeak(applyChildFriendlyTtsWorkarounds(phoneme))
                }
            }
            else -> return
        }
        sfx.playWrong()
    }

    suspend fun playFallingLetterTap(
        voice: VoicePlayer,
        tts: TextToSpeechManager,
        letterText: String,
        sfx: SfxManager,
    ) {
        val base = hebrewLetterBase(letterText)
        val clip = AudioClips.letterNameClip(base)
        if (clip != null && voice.hasAsset(clip)) {
            voice.playBlocking(clip)
        } else {
            val spoken = wrongLetterFeedbackSpeech(letterText)
            if (spoken.isNotBlank()) {
                tts.interruptAndSpeak(applyChildFriendlyTtsWorkarounds(spoken))
            }
        }
        sfx.playWrong()
    }

    private suspend fun playLetterRecognitionIntro(
        voice: VoicePlayer,
        tts: TextToSpeechManager,
        targetLetter: String,
        ttsInstruction: String,
    ) {
        val base = hebrewLetterBase(targetLetter)
        val combined = AudioClips.chooseLetterClip(base)
        if (combined != null && voice.hasAsset(combined)) {
            voice.playBlocking(combined)
            return
        }
        val letterClip = AudioClips.letterNameClip(base)
        val intro =
            listOf(AudioClips.VoChooseLetter, AudioClips.VoFindLetter, AudioClips.VoBachorEtHaot)
                .firstOrNull { voice.hasAsset(it) }
        val parts =
            buildList {
                intro?.let { add(it) }
                letterClip?.let { add(it) }
            }.filter { voice.hasAsset(it) }
        if (parts.isNotEmpty()) {
            voice.playSequenceBlocking(*parts.toTypedArray())
        } else {
            speakTtsIfNeeded(tts, ttsInstruction)
        }
    }

    private suspend fun playPhonemicIntro(
        voice: VoicePlayer,
        tts: TextToSpeechManager,
        targetLetter: String,
        ttsInstruction: String,
    ) {
        val base = hebrewLetterBase(targetLetter)
        val intro =
            listOf(AudioClips.VoFindLetter, AudioClips.VoChooseLetter)
                .firstOrNull { voice.hasAsset(it) }
        if (intro != null && voice.hasAsset(intro)) {
            voice.playBlocking(intro)
        }
        val phoneme = phonemeSpokenForTts(base)
        if (phoneme.isNotBlank()) {
            tts.interruptAndSpeak(applyChildFriendlyTtsWorkarounds(phoneme))
            return
        }
        speakTtsIfNeeded(tts, ttsInstruction)
    }

    private suspend fun speakTtsIfNeeded(
        tts: TextToSpeechManager,
        text: String,
    ) {
        if (text.isBlank()) return
        tts.speakFully(applyChildFriendlyTtsWorkarounds(text), navigationSettleMs = 0L)
    }
}
