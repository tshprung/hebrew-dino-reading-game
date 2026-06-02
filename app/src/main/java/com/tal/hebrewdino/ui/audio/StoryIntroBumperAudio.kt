package com.tal.hebrewdino.ui.audio

import androidx.annotation.RawRes
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.data.DinoCharacter

/** Short direct companion speech before chapter intro narrator clips (Chapters 2–6 only). */
object StoryIntroBumperAudio {
    fun supportsIntroBumper(chapterId: Int): Boolean = chapterId in 2..6

    @RawRes
    fun introBumperRawRes(
        chapterId: Int,
        companion: DinoCharacter,
    ): Int =
        when (chapterId) {
            2 ->
                when (companion) {
                    DinoCharacter.Dino -> R.raw.story_bumper_ch2_intro_dino
                    DinoCharacter.Dina -> R.raw.story_bumper_ch2_intro_dina
                }
            3 ->
                when (companion) {
                    DinoCharacter.Dino -> R.raw.story_bumper_ch3_intro_dino
                    DinoCharacter.Dina -> R.raw.story_bumper_ch3_intro_dina
                }
            4 ->
                when (companion) {
                    DinoCharacter.Dino -> R.raw.story_bumper_ch4_intro_dino
                    DinoCharacter.Dina -> R.raw.story_bumper_ch4_intro_dina
                }
            5 ->
                when (companion) {
                    DinoCharacter.Dino -> R.raw.story_bumper_ch5_intro_dino
                    DinoCharacter.Dina -> R.raw.story_bumper_ch5_intro_dina
                }
            6 ->
                when (companion) {
                    DinoCharacter.Dino -> R.raw.story_bumper_ch6_intro_dino
                    DinoCharacter.Dina -> R.raw.story_bumper_ch6_intro_dina
                }
            else -> 0
        }

    /** Visible companion direct-speech line for Ch.2–6 intro bumpers (matches bumper MP3). */
    fun introBumperBodyText(
        chapterId: Int,
        companion: DinoCharacter,
    ): String =
        when (chapterId) {
            2 ->
                "איזה כיף שאתם איתי! תודה שאתם עוזרים לי לחפש את הביצה הבאה."
            3 ->
                when (companion) {
                    DinoCharacter.Dino ->
                        "אני שמח שאתם ממשיכים איתי! בואו נעקוב יחד אחרי העקבות."
                    DinoCharacter.Dina ->
                        "אני שמחה שאתם ממשיכים איתי! בואו נעקוב יחד אחרי העקבות."
                }
            4 ->
                "תודה שאתם עוזרים לי! יש סימנים חדשים, וביחד נוכל למצוא רמז."
            5 ->
                "איזה כיף שאתם איתי! הביצה האחרונה כבר קרובה."
            6 ->
                "מצאנו את כל הביצים! תודה שעזרתם לי להגיע עד לכאן."
            else -> ""
        }
}
