package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tal.hebrewdino.R

@Composable
fun Chapter2IntroScreen(
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.mountain_bg_chapter2,
        title = "פרק 2 - מצא את הביצה הורודה",
        body =
            "מצאנו כבר ביצה אחת — מעולה!\n" +
                "\n" +
                "אבל אמא עדיין מחפשת עוד ביצים…\n" +
                "\n" +
                "אולי הביצה הבאה מסתתרת בהרים.\n" +
                "\n" +
                "בואו נמשיך לחפש יחד!",
        eggStripCount = 1,
        companion = ChapterLobbyCompanion.DinoOnly,
        narrationPlaying = false,
        dinoContentDescription = "דינו",
        onContinue = onContinue,
        onBack = onBack,
        modifier = modifier,
    )
}
