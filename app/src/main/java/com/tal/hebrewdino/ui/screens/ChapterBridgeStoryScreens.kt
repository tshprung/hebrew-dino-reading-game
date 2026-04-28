package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips

@Composable
fun Chapter2OutroScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.chapter2_story_outro,
        title = "יש!",
        body =
            "העקבות ממשיכות קדימה…\n" +
                "\n" +
                "נראה שמישהו עבר כאן ולקח משהו.\n" +
                "\n" +
                "דינו ממשיך לעקוב אחרי הדרך,\n" +
                "ועכשיו יש כיוון ברור.\n" +
                "\n" +
                "בואו נתקדם אחרי העקבות בפרק הבא.",
        eggStripCount = 1,
        companion = ChapterLobbyCompanion.DinoOnly,
        narrationPlaying = false,
        voiceAssetPath = AudioClips.StoryMountainApproachOutro,
        dinoContentDescription = "דינו",
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter3OutroScreen(
    eggStripCount: Int,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.ch3_reward_bg,
        title = "הנה היא!",
        body =
            "הנה היא!\n" +
                "\n" +
                "דינו מצא עוד ביצה — והיא שלמה!\n" +
                "\n" +
                "הוא אוסף אותה בזהירות,\n" +
                "וממשיך במסע למצוא את הביצה האחרונה.",
        eggStripCount = eggStripCount,
        companion = ChapterLobbyCompanion.DinoOnly,
        narrationPlaying = false,
        voiceAssetPath = AudioClips.StoryMountainPathOutro,
        dinoContentDescription = "דינו",
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter4IntroScreen(
    eggStripCount: Int,
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
        eggStripCount = eggStripCount,
        companion = ChapterLobbyCompanion.DinoOnly,
        narrationPlaying = false,
        voiceAssetPath = AudioClips.StoryCh4Intro,
        dinoContentDescription = "דינו",
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter4OutroScreen(
    eggStripCount: Int,
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
        eggStripCount = eggStripCount,
        companion = ChapterLobbyCompanion.DinoAndMom,
        narrationPlaying = false,
        voiceAssetPath = AudioClips.StoryCh4HomeOutro,
        dinoContentDescription = "דינו",
        onContinue = onContinue,
        modifier = modifier,
    )
}
