package com.tal.hebrewdino.ui.companion

import androidx.annotation.RawRes
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.data.PlayerAddress

/** Season 1 Ch.1 companion speech + story copy keyed by companion and player address. */
object Chapter1CompanionCopy {
    fun introSpeechText(
        character: DinoCharacter,
        address: PlayerAddress,
    ): String {
        val name = character.displayNameHebrew()
        val taskLine =
            when (address) {
                PlayerAddress.Boy -> "בכל משימה שתפתור, נתקדם עוד קצת ונמצא רמז."
                PlayerAddress.Girl -> "בכל משימה שתפתרי, נתקדם עוד קצת ונמצא רמז."
            }
        val helpQuestion =
            when (address) {
                PlayerAddress.Boy -> "תעזור לי?"
                PlayerAddress.Girl -> "תעזרי לי?"
            }
        return "\u200Fהיי! אני $name.\n" +
            "אמא שלי, דינוזאורית, איבדה שלוש ביצים…\n" +
            "$taskLine\n" +
            helpQuestion
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
        val companionLine =
            when (character) {
                DinoCharacter.Dino -> "דינו יצא לדרך לעזור לאמא."
                DinoCharacter.Dina -> "דינה יצאה לדרך לעזור לאמא."
            }
        return "\u200Fאמא דינוזאורית שמרה על הביצים שלה…\n\n" +
            "אבל פתאום — הן נעלמו!\n\n" +
            "\"אוי לא! איפה הביצים שלי?\", אמרה אמא דינוזאורית\n\n" +
            "בואו נעזור לה!\n\n" +
            "$companionLine\n\n" +
            "נפתור משימות ונמצא רמזים."
    }

    fun chapter1MidBoostBody(
        character: DinoCharacter,
        address: PlayerAddress,
    ): String {
        val detectiveLine =
            when (address) {
                PlayerAddress.Boy -> "יופי! פתרת את החידות כמו בלש אמיתי."
                PlayerAddress.Girl -> "יופי! פתרת את החידות כמו בלשית אמיתית."
            }
        val progressLine =
            when (character) {
                DinoCharacter.Dino -> "דינו מרגיש שאנחנו מתקרבים לביצה."
                DinoCharacter.Dina -> "דינה מרגישה שאנחנו מתקרבים לביצה."
            }
        return "$detectiveLine\n\n" +
            "$progressLine\n\n" +
            "יש כאן סימנים שמובילים קדימה…\n" +
            "בואו נמשיך!"
    }

    fun finaleBody(
        character: DinoCharacter,
        address: PlayerAddress,
    ): String {
        val foundLine =
            when (character) {
                DinoCharacter.Dino -> "דינו מצא ביצה אחת!"
                DinoCharacter.Dina -> "דינה מצאה ביצה אחת!"
            }
        val continueLine =
            when (address) {
                PlayerAddress.Boy ->
                    "מצאנו ביצה אחת, אבל עוד ביצים מחכות לנו.\n" +
                        "נמשיך במסע!"
                PlayerAddress.Girl ->
                    "מצאנו ביצה אחת, אבל עוד ביצים מחכות לנו.\n" +
                        "נמשיך במסע!"
            }
        return "\u200F$foundLine\n\n" +
            "אמא דינוזאורית תשמח!\n\n" +
            continueLine
    }
}
