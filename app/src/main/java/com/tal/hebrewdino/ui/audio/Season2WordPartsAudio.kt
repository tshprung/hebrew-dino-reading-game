package com.tal.hebrewdino.ui.audio

import android.util.Log
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

object Season2WordPartsAudio {
    private const val MISSING_TAG = "MissingContent"
    private const val PART_GAP_MS = 120L

    /** Split card tap: part1 → part2 → that split's full word. */
    suspend fun playSplitTapSequence(
        catalogId: String,
        rawVoice: RawVoicePlayer,
        chapterId: Int? = null,
        stationId: Int? = null,
    ) {
        playPartsSequence(
            catalogId = catalogId,
            rawVoice = rawVoice,
            chapterId = chapterId,
            stationId = stationId,
        )
        playCorrectFullWord(
            catalogId = catalogId,
            rawVoice = rawVoice,
            chapterId = chapterId,
            stationId = stationId,
        )
    }

    /** Round prompt / replay tail: part1 → part2 (logs MissingContent when clips are absent). */
    suspend fun playPartsSequence(
        catalogId: String,
        rawVoice: RawVoicePlayer,
        chapterId: Int? = null,
        stationId: Int? = null,
    ) {
        val p1 = Season2RawAudio.wordPartRawResId(catalogId, partIndex = 1)
        val p2 = Season2RawAudio.wordPartRawResId(catalogId, partIndex = 2)
        if (p1 == null) {
            Log.e(
                MISSING_TAG,
                "Missing word-part chunk audio. chapterId=$chapterId stationId=$stationId " +
                    "context=Season2WordPartsAudio.playPartsSequence catalogId='$catalogId' partIndex=1",
            )
        } else {
            rawVoice.playRawBlocking(p1)
            delay(PART_GAP_MS.milliseconds)
        }
        if (p2 == null) {
            Log.e(
                MISSING_TAG,
                "Missing word-part chunk audio. chapterId=$chapterId stationId=$stationId " +
                    "context=Season2WordPartsAudio.playPartsSequence catalogId='$catalogId' partIndex=2",
            )
        } else {
            rawVoice.playRawBlocking(p2)
        }
    }

    /** Hint reveal: part1 → part2 → full word (skips missing part clips). */
    suspend fun playHintRevealSequence(
        catalogId: String,
        rawVoice: RawVoicePlayer,
        chapterId: Int? = null,
        stationId: Int? = null,
    ) {
        val p1 = Season2RawAudio.wordPartRawResId(catalogId, partIndex = 1)
        val p2 = Season2RawAudio.wordPartRawResId(catalogId, partIndex = 2)
        if (p1 != null) {
            rawVoice.playRawBlocking(p1)
            delay(120.milliseconds)
        }
        if (p2 != null) {
            rawVoice.playRawBlocking(p2)
            delay(120.milliseconds)
        }
        val wordResId = AudioClips.wordRawResIdByCatalogId(catalogId)
        if (wordResId == null) {
            Log.e(
                MISSING_TAG,
                "Missing word-part hint full-word audio. chapterId=$chapterId stationId=$stationId " +
                    "context=Season2WordPartsAudio.playHintRevealSequence catalogId='$catalogId'",
            )
            rawVoice.playRawBlocking(0)
            return
        }
        rawVoice.playRawBlocking(wordResId)
    }

    /** Correct-answer tail after tapped split parts were heard: full word only. */
    suspend fun playCorrectFullWord(
        catalogId: String,
        rawVoice: RawVoicePlayer,
        chapterId: Int? = null,
        stationId: Int? = null,
    ) {
        val wordResId = AudioClips.wordRawResIdByCatalogId(catalogId)
        if (wordResId == null) {
            Log.e(
                MISSING_TAG,
                "Missing word-part correct full-word audio. chapterId=$chapterId stationId=$stationId " +
                    "context=Season2WordPartsAudio.playCorrectFullWord catalogId='$catalogId'",
            )
            return
        }
        rawVoice.playRawBlocking(wordResId)
    }
}
