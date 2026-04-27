package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips

@Composable
fun Chapter3IntroScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.chapter3_story_intro,
        title = "פרק 3 - מצא את הביצה הורודה",
        body =
            "דינו ממשיך לעקוב אחרי העקבות.\n" +
                "\n" +
                "הדרך כבר לא ארוכה,\n" +
                "ונראה שהוא מתקרב למשהו חשוב.\n" +
                "\n" +
                "בואו נתקדם בזהירות ונגלה מה מחכה בהמשך.",
        eggStripCount = 2,
        companion = ChapterLobbyCompanion.DinoOnly,
        narrationPlaying = false,
        voiceAssetPath = AudioClips.StoryMountainPathIntro,
        dinoContentDescription = "דינו",
        onContinue = onContinue,
        modifier = modifier,
    )
}
