package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips

@Composable
fun Chapter2IntroScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.chapter2_story_intro,
        title = "פרק 2 - מוצאים עקבות לביצה הורודה",
        body =
            "מצאנו כבר ביצה אחת — מעולה!\n" +
                "\n" +
                "דינו ממשיך בדרך,\n" +
                "והשביל מתחיל לעלות למעלה,\n" +
                "לתוך ההרים.\n" +
                "\n" +
                "על האדמה יש עקבות…\n" +
                "מישהו עבר כאן קודם.\n" +
                "\n" +
                "בואו נעקוב אחרי העקבות ונראה לאן הן מובילות.",
        companion = ChapterLobbyCompanion.DinoOnly,
        narrationPlaying = false,
        voiceAssetPath = AudioClips.StoryMountainApproachIntro,
        bodyLineHeightOverride = 20.sp,
        dinoContentDescription = "דינו",
        onContinue = onContinue,
        modifier = modifier,
    )
}
