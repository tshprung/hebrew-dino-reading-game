package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tal.hebrewdino.R

@Composable
fun Chapter3IntroScreen(
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.mountain_bg_chapter3,
        title = "פרק 3 - מצא את הביצה הסגולה",
        body =
            "נשארה רק ביצה אחת!\n" +
                "\n" +
                "השביל נהיה צר…\n" +
                "\n" +
                "\"צריך להיזהר\"\n" +
                "\n" +
                "קדימה, עוד קצת!",
        eggStripCount = 2,
        companion = ChapterLobbyCompanion.DinoOnly,
        narrationPlaying = false,
        dinoContentDescription = "דינו",
        onContinue = onContinue,
        onBack = onBack,
        modifier = modifier,
    )
}
