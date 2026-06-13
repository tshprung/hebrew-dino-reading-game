package com.tal.hebrewdino.ui.domain

import android.util.Log
import androidx.annotation.RawRes
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/** Season 1 gameplay audio routing (chapter IDs 1–6). */
object Season1StationAudio {
    private const val MISSING_TAG = "MissingContent"

    /**
     * Post-instruction handoff for Ch5 st2 / Ch6 st4 round intro.
     * Halves the baseline 60ms coach gap — start the target word earlier into the instruction tail.
     */
    const val DRAG_MISSING_LETTER_INSTRUCTION_TO_WORD_GAP_MS: Long = 30L

    fun isSeason1DragWordToPictureStation(
        chapterId: Int,
        stationId: Int,
    ): Boolean = (chapterId == 3 || chapterId == 6) && stationId == 3

    fun isSeason1DragMissingLetterStation(
        chapterId: Int,
        stationId: Int,
    ): Boolean = (chapterId == 5 && stationId == 2) || (chapterId == 6 && stationId == 4)

    /** Canonical Season 1 drag-missing-letter source (Ch5 st2). */
    const val SOURCE_CHAPTER_ID: Int = 5
    const val SOURCE_STATION_ID: Int = 2

    /** Maps Ch6 st4 (and Ch5 st2) to the canonical behavior chapter/station ids. */
    fun resolveDragMissingLetterBehavior(chapterId: Int, stationId: Int): Pair<Int, Int> =
        if (isSeason1DragMissingLetterStation(chapterId, stationId)) {
            SOURCE_CHAPTER_ID to SOURCE_STATION_ID
        } else {
            chapterId to stationId
        }

    @RawRes
    fun dragWordToPictureInstructionRawResId(): Int = R.raw.s1_drag_word_to_picture_instruction

    @RawRes
    fun dragMissingLetterInstructionRawResId(): Int = R.raw.s1_drag_missing_letter_instruction

    suspend fun playDragWordToPictureInstruction(
        rawVoice: RawVoicePlayer?,
        chapterId: Int,
        stationId: Int,
        context: String,
    ) {
        if (rawVoice == null) {
            Log.e(
                MISSING_TAG,
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=$context stage=rawVoice=null expectedInstructionRawRes=${dragWordToPictureInstructionRawResId()}",
            )
            return
        }
        rawVoice.playRawBlocking(dragWordToPictureInstructionRawResId())
    }

    suspend fun playDragMissingLetterInstruction(
        rawVoice: RawVoicePlayer?,
        chapterId: Int,
        stationId: Int,
        context: String,
    ) {
        if (rawVoice == null) {
            Log.e(
                MISSING_TAG,
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=$context stage=rawVoice=null expectedInstructionRawRes=${dragMissingLetterInstructionRawResId()}",
            )
            return
        }
        rawVoice.playRawBlocking(dragMissingLetterInstructionRawResId())
    }

    suspend fun playDragWordToPictureWord(
        rawVoice: RawVoicePlayer?,
        catalogEntryId: String,
        chapterId: Int,
        stationId: Int,
        context: String,
    ) {
        playDragMissingLetterWord(
            rawVoice = rawVoice,
            catalogEntryId = catalogEntryId,
            chapterId = chapterId,
            stationId = stationId,
            context = context,
        )
    }

    suspend fun playDragMissingLetterWord(
        rawVoice: RawVoicePlayer?,
        catalogEntryId: String,
        chapterId: Int,
        stationId: Int,
        context: String,
    ) {
        if (rawVoice == null) {
            Log.e(
                MISSING_TAG,
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=$context stage=rawVoice=null expectedWordRawRes catalogId='$catalogEntryId'",
            )
            return
        }
        val wordResId = AudioClips.wordRawResIdByCatalogId(catalogEntryId)
        if (wordResId == null) {
            Log.e(
                MISSING_TAG,
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=$context stage=missing raw word mapping catalogId='$catalogEntryId'",
            )
            rawVoice.playRawBlocking(0)
            return
        }
        rawVoice.playRawBlocking(wordResId)
    }

    suspend fun playDragMissingLetterLetterName(
        rawVoice: RawVoicePlayer?,
        letter: String,
        chapterId: Int,
        stationId: Int,
        context: String,
    ) {
        if (rawVoice == null) {
            Log.e(
                MISSING_TAG,
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=$context stage=rawVoice=null expectedLetterRawRes letter='$letter'",
            )
            return
        }
        val letterResId = AudioClips.letterNameRawResId(letter)
        if (letterResId == null) {
            Log.e(
                MISSING_TAG,
                "Missing required station prompt audio. chapterId=$chapterId stationId=$stationId context=$context stage=missing raw letter-name mapping letter='$letter'",
            )
            rawVoice.playRawBlocking(0)
            return
        }
        rawVoice.playRawBlocking(letterResId)
    }

    suspend fun playDragMissingLetterRoundIntro(
        rawVoice: RawVoicePlayer,
        catalogEntryId: String,
        chapterId: Int,
        stationId: Int,
        includeInstruction: Boolean,
        context: String,
    ) {
        if (includeInstruction) {
            val instructionResId = dragMissingLetterInstructionRawResId()
            val instructionDurationMs = rawVoice.rawDurationMs(instructionResId)
            if (instructionDurationMs > DRAG_MISSING_LETTER_INSTRUCTION_TO_WORD_GAP_MS) {
                coroutineScope {
                    val instructionJob =
                        launch {
                            rawVoice.playRawBlocking(instructionResId)
                        }
                    val handoffDelayMs =
                        instructionDurationMs - DRAG_MISSING_LETTER_INSTRUCTION_TO_WORD_GAP_MS
                    delay(handoffDelayMs)
                    rawVoice.stopNow()
                    instructionJob.cancel()
                }
            } else {
                rawVoice.playRawBlocking(instructionResId)
            }
        }
        playDragMissingLetterWord(
            rawVoice = rawVoice,
            catalogEntryId = catalogEntryId,
            chapterId = chapterId,
            stationId = stationId,
            context = context,
        )
    }

    suspend fun playDragMissingLetterCorrectFeedback(
        rawVoice: RawVoicePlayer?,
        letter: String,
        catalogEntryId: String?,
        chapterId: Int,
        stationId: Int,
        sfx: SoundPoolPlayer,
    ) {
        if (rawVoice == null) {
            sfx.playFirstAvailable(AudioClips.SfxCorrect, volume = 0.58f)
            return
        }
        playDragMissingLetterLetterName(
            rawVoice = rawVoice,
            letter = letter,
            chapterId = chapterId,
            stationId = stationId,
            context = "Season1StationAudio.playDragMissingLetterCorrectFeedback(letter)",
        )
        if (catalogEntryId != null) {
            playDragMissingLetterWord(
                rawVoice = rawVoice,
                catalogEntryId = catalogEntryId,
                chapterId = chapterId,
                stationId = stationId,
                context = "Season1StationAudio.playDragMissingLetterCorrectFeedback(word)",
            )
        }
        sfx.playFirstAvailable(AudioClips.SfxCorrect, volume = 0.58f)
    }
}
