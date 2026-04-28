package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips

@Composable
fun Chapter3IntroScreen(
    eggStripCount: Int,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.ch3_story_intro_bg,
        title = "פרק 3 — בעקבות העקבות",
        body =
            "דינו ממשיך אחרי העקבות.\n" +
                "\n" +
                "הדרך כבר לא ארוכה,\n" +
                "ונראה שהוא מתקרב למשהו חשוב.\n" +
                "\n" +
                "בואו נתקדם בזהירות ונגלה מה מחכה בהמשך.",
        eggStripCount = eggStripCount,
        companion = ChapterLobbyCompanion.DinoOnly,
        narrationPlaying = false,
        voiceAssetPath = AudioClips.StoryMountainPathIntro,
        dinoContentDescription = "דינו",
        onContinue = onContinue,
        modifier = modifier,
    )
}
