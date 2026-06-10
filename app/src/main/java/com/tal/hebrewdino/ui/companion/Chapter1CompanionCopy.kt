package com.tal.hebrewdino.ui.companion

import androidx.annotation.RawRes
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.data.PlayerAddress

/** Season 1 Ch.1 companion speech + story copy keyed by companion and player address. */
object Chapter1CompanionCopy {
    const val chapter1CompletionSpeechBubbleText: String = "\u200Fמצאנו ביצה! תודה על העזרה!"

    fun introSpeechText(
        character: DinoCharacter,
        address: PlayerAddress,
    ): String {
        val name = character.displayNameHebrew()
        val taskLine =
            when (address) {
                PlayerAddress.Boy -> "בכל משימה שתפתור נתקדם עוד קצת ונמצא רמז."
                PlayerAddress.Girl -> "בכל משימה שתפתרי נתקדם עוד קצת ונמצא רמז."
            }
        val helpQuestion =
            when (address) {
                PlayerAddress.Boy -> "תעזור לי?"
                PlayerAddress.Girl -> "תעזרי לי?"
            }
        return "\u200Fהיי אני $name. אמא שלי איבדה שלוש ביצים.\n" +
            "$taskLine $helpQuestion"
    }

    @RawRes
    fun introRawRes(
        character: DinoCharacter,
        address: PlayerAddress,
    ): Int =
        when (character) {
            DinoCharacter.Dino ->
                when (address) {
                    PlayerAddress.Boy -> R.raw.dino_intro_boy
                    PlayerAddress.Girl -> R.raw.dino_intro_girl
                }
            DinoCharacter.Dina ->
                when (address) {
                    PlayerAddress.Boy -> R.raw.dina_intro_boy
                    PlayerAddress.Girl -> R.raw.dina_intro_girl
                }
        }

    fun forestIntroBody(character: DinoCharacter): String {
        return when (character) {
            DinoCharacter.Dino ->
                "\u200Fביער הירוק גרו אמא דינוזאורית ודינו הקטן.\n" +
                    "יום אחד, אמא גילתה ששלוש הביצים שלה נעלמו.\n" +
                    "דינו יצא למסע כדי למצוא אותן."
            DinoCharacter.Dina ->
                "\u200Fביער הירוק גרו אמא דינוזאורית ודינה הקטנה.\n" +
                    "יום אחד, אמא גילתה ששלוש הביצים שלה נעלמו.\n" +
                    "דינה יצאה למסע כדי למצוא אותן."
        }
    }

    fun chapter1MidBoostBody(
        character: DinoCharacter,
    ): String {
        val foundCluesLine =
            when (character) {
                DinoCharacter.Dino -> "יש! דינו מצא רמזים טובים."
                DinoCharacter.Dina -> "יש! דינה מצאה רמזים טובים."
            }
        return "\u200F$foundCluesLine\n" +
            "המסע מתקדם.\n" +
            "עוד קצת, ונגיע לביצה!"
    }

    fun finaleBody(
        character: DinoCharacter,
    ): String {
        val foundLine =
            when (character) {
                DinoCharacter.Dino -> "יש! דינו מצא ביצה אחת!"
                DinoCharacter.Dina -> "יש! דינה מצאה ביצה אחת!"
            }
        return "\u200F$foundLine\n" +
            "אמא שמחה מאוד.\n" +
            "אבל עוד ביצים מחכות לנו...\n" +
            "נמשיך במסע!"
    }
}
