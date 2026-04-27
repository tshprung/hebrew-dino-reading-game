package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.data.DinoCharacter

@Composable
fun ForestIntroScreen(
    character: DinoCharacter,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.forest_bg_story_intro,
        dinoContentDescription = if (character == DinoCharacter.Dina) "דינה" else "דינו",
        title = "פרק 1 - מצא את הביצה",
        body =
            "אמא דינוזאור שמרה על הביצים שלה…\n" +
                "\n" +
                "אבל פתאום — הן נעלמו!\n" +
                "\n" +
                "\"אוי לא! איפה הביצים שלי?\", אמרה אמא דינוזאור\n" +
                "\n" +
                "בואו נעזור לה!\n" +
                "\n" +
                "נצא לדרך ונפתור משימות.",
        voiceAssetPath = AudioClips.StoryForestIntro,
        eggStripCount = 0,
        companion = ChapterLobbyCompanion.DinoAndMom,
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun ForestOutroScreen(
    character: DinoCharacter,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.forest_bg_story_outro_egg,
        dinoContentDescription = if (character == DinoCharacter.Dina) "דינה" else "דינו",
        title = "יש!",
        body =
            "מצאנו את הביצה הראשונה!\n" +
                "\n" +
                "אמא תשמח!\n" +
                "\n" +
                "אבל נשארו עוד 2 ביצים למצוא!\n" +
                "\n" +
                "בואו נמשיך!",
        voiceAssetPath = AudioClips.StoryEggOutro,
        eggStripCount = 1,
        companion = ChapterLobbyCompanion.DinoOnly,
        onContinue = onContinue,
        modifier = modifier,
    )
}
