package com.tal.hebrewdino.ui.companion

import androidx.annotation.RawRes
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.data.PlayerAddress
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.StationTemplateId

/** Season 1 Ch.1 raw narration + address-aware instruction/feedback clips. */
object Chapter1AddressAwareAudio {
    enum class InstructionKind {
        PickLetter,
        FindLetter,
        PictureStartsWith,
        WhichWordStartsWith,
        FindWordStartsWith,
        MatchLetterToWord,
        MemoryMatch,
        PopBalloons,
    }

    @RawRes
    fun storyIntroRawRes(character: DinoCharacter): Int =
        when (character) {
            DinoCharacter.Dino -> R.raw.ch1_story_intro_dino
            DinoCharacter.Dina -> R.raw.ch1_story_intro_dina
        }

    @RawRes
    fun storyMidRawRes(character: DinoCharacter): Int =
        when (character) {
            DinoCharacter.Dino -> R.raw.ch1_story_mid_dino
            DinoCharacter.Dina -> R.raw.ch1_story_mid_dina
        }

    @RawRes
    fun storyOutroRawRes(character: DinoCharacter): Int =
        when (character) {
            DinoCharacter.Dino -> R.raw.ch1_story_outro_dino
            DinoCharacter.Dina -> R.raw.ch1_story_outro_dina
        }

    @RawRes
    fun instructionRawRes(
        kind: InstructionKind,
        address: PlayerAddress,
    ): Int =
        when (kind) {
            InstructionKind.PickLetter ->
                when (address) {
                    PlayerAddress.Boy -> R.raw.instruction_pick_letter_short_boy
                    PlayerAddress.Girl -> R.raw.instruction_pick_letter_short_girl
                }
            InstructionKind.FindLetter ->
                when (address) {
                    PlayerAddress.Boy -> R.raw.instruction_find_letter_short_boy
                    PlayerAddress.Girl -> R.raw.instruction_find_letter_short_girl
                }
            InstructionKind.PictureStartsWith ->
                when (address) {
                    PlayerAddress.Boy -> R.raw.instruction_picture_starts_with_short_boy
                    PlayerAddress.Girl -> R.raw.instruction_picture_starts_with_short_girl
                }
            InstructionKind.WhichWordStartsWith ->
                when (address) {
                    PlayerAddress.Boy -> R.raw.instruction_which_word_starts_with_short_boy
                    PlayerAddress.Girl -> R.raw.instruction_which_word_starts_with_short_girl
                }
            InstructionKind.FindWordStartsWith ->
                when (address) {
                    PlayerAddress.Boy -> R.raw.instruction_find_word_starts_with_boy
                    PlayerAddress.Girl -> R.raw.instruction_find_word_starts_with_girl
                }
            InstructionKind.MatchLetterToWord ->
                when (address) {
                    PlayerAddress.Boy -> R.raw.instruction_match_letter_to_word_short_boy
                    PlayerAddress.Girl -> R.raw.instruction_match_letter_to_word_short_girl
                }
            InstructionKind.MemoryMatch ->
                when (address) {
                    PlayerAddress.Boy -> R.raw.instruction_memory_match_boy
                    PlayerAddress.Girl -> R.raw.instruction_memory_match_girl
                }
            InstructionKind.PopBalloons ->
                when (address) {
                    PlayerAddress.Boy -> R.raw.instruction_pop_balloons_short_boy
                    PlayerAddress.Girl -> R.raw.instruction_pop_balloons_short_girl
                }
        }

    @RawRes
    fun tryAgainRawRes(address: PlayerAddress): Int =
        when (address) {
            PlayerAddress.Boy -> R.raw.feedback_try_again_boy
            PlayerAddress.Girl -> R.raw.feedback_try_again_girl
        }

    @RawRes
    fun greatRawRes(address: PlayerAddress): Int =
        when (address) {
            PlayerAddress.Boy -> R.raw.feedback_great_boy
            PlayerAddress.Girl -> R.raw.feedback_great_girl
        }

    fun instructionKindFor(
        stationId: Int,
        stationTemplateId: StationTemplateId,
        q: Question,
    ): InstructionKind? {
        // Template-first: Season 2 arc uses station ids 1=balloons, 2=pick-letter (not Ch.1 numbering).
        when (stationTemplateId) {
            StationTemplateId.PopBalloons -> return InstructionKind.PopBalloons
            StationTemplateId.PickLetter -> return InstructionKind.PickLetter
            StationTemplateId.FindLetterGrid -> return InstructionKind.FindLetter
            StationTemplateId.PictureStartsWith ->
                if (q is Question.PictureStartsWithQuestion) {
                    return InstructionKind.PictureStartsWith
                }
            StationTemplateId.ImageMatch ->
                if (q is Question.ImageMatchQuestion) {
                    return InstructionKind.WhichWordStartsWith
                }
            StationTemplateId.MatchLetterToWord -> return InstructionKind.MatchLetterToWord
            StationTemplateId.ImageToWord -> return InstructionKind.FindWordStartsWith
            else -> Unit
        }
        return when (stationId.coerceIn(1, Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH)) {
            Chapter1StationOrder.TAP_LETTER -> InstructionKind.PickLetter
            Chapter1StationOrder.BALLOON_POP -> InstructionKind.PopBalloons
            Chapter1StationOrder.REVEAL_THEN_CHOOSE -> InstructionKind.FindLetter
            Chapter1StationOrder.PICTURE_PICK_ONE ->
                if (q is Question.PictureStartsWithQuestion) {
                    InstructionKind.PictureStartsWith
                } else {
                    null
                }
            Chapter1StationOrder.PICTURE_PICK_ALL ->
                if (q is Question.ImageMatchQuestion) {
                    InstructionKind.WhichWordStartsWith
                } else {
                    null
                }
            Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH ->
                when (stationTemplateId) {
                    StationTemplateId.MatchLetterToWord -> InstructionKind.MatchLetterToWord
                    StationTemplateId.ImageToWord -> InstructionKind.FindWordStartsWith
                    else -> InstructionKind.MatchLetterToWord
                }
            else -> null
        }
    }
}
