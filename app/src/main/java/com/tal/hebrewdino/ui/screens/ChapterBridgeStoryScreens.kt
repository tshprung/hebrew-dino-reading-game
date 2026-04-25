package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tal.hebrewdino.R

@Composable
fun Chapter2OutroScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.mountain_bg_chapter2,
        title = "יש!",
        body =
            "מצאנו עוד ביצה!\n" +
                "\n" +
                "אמא כבר מרגישה יותר רגועה…\n" +
                "\n" +
                "נשארה עוד ביצה אחת —\n" +
                "נמשיך בהרפתקה הבאה.",
        eggStripCount = 2,
        companion = ChapterLobbyCompanion.DinoOnly,
        narrationPlaying = false,
        dinoContentDescription = "דינו",
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter3OutroScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.mountain_bg_chapter3,
        title = "יש!!!",
        body =
            "מצאנו את הביצה האחרונה!\n" +
                "\n" +
                "כל הביצים חזרו!\n" +
                "\n" +
                "בוא נחזיר אותן לאמא.",
        eggStripCount = 3,
        companion = ChapterLobbyCompanion.DinoOnly,
        narrationPlaying = false,
        dinoContentDescription = "דינו",
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter4IntroScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.mountain_bg_chapter4,
        title = "פרק 4",
        body =
            "בוא נחזיר את הביצים לאמא. היא מחכה בקן.\n" +
                "\n" +
                "אבל בדרך יש לנו לעבור את הביצה הגדולה.\n" +
                "\n" +
                "תעזור/י לי?",
        eggStripCount = 3,
        companion = ChapterLobbyCompanion.DinoOnly,
        narrationPlaying = false,
        dinoContentDescription = "דינו",
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter4OutroScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.forest_bg_story_outro_egg,
        title = "איזה כיף!",
        body =
            "כל הביצים חזרו הביתה!\n" +
                "\n" +
                "אמא מחבקת ואומרת:\n" +
                "\"תודה שעזרת לי!\"\n" +
                "\n" +
                "כל הכבוד! 🎉",
        eggStripCount = 3,
        companion = ChapterLobbyCompanion.DinoAndMom,
        narrationPlaying = false,
        dinoContentDescription = "דינו",
        onContinue = onContinue,
        modifier = modifier,
    )
}
