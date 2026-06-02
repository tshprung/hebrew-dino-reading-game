package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.StoryIntroBumperAudio
import com.tal.hebrewdino.ui.companion.displayNameHebrew
import com.tal.hebrewdino.ui.data.DinoCharacter

@Composable
fun Chapter2IntroScreen(
    companionCharacter: DinoCharacter,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val voiceRawResId =
        when (companionCharacter) {
            DinoCharacter.Dino -> R.raw.ch2_story_intro_dino
            DinoCharacter.Dina -> R.raw.ch2_story_intro_dina
        }
    val body =
        when (companionCharacter) {
            DinoCharacter.Dino ->
                "מצאנו כבר ביצה אחת — מעולה!\n" +
                    "דינו ממשיך בדרך, והשביל מתחיל לעלות למעלה, לתוך ההרים.\n" +
                    "על האדמה יש עקבות… מישהו עבר כאן קודם.\n" +
                    "בואו נעקב אחרי העקבות ונראה לאן הן מובילות."
            DinoCharacter.Dina ->
                "מצאנו כבר ביצה אחת — מעולה!\n" +
                    "דינה ממשיכה בדרך, והשביל מתחיל לעלות למעלה, לתוך ההרים.\n" +
                    "על האדמה יש עקבות… מישהו עבר כאן קודם.\n" +
                    "בואו נעקב אחרי העקבות ונראה לאן הן מובילות."
        }
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.chapter2_story_intro,
        title = "פרק 2 - מוצאים עקבות לביצה הורודה",
        body = body,
        companion = ChapterLobbyCompanion.DinoOnly,
        chapterId = 2,
        storyContext = "Chapter2IntroScreen",
        useCompanionDinoArt = true,
        companionCharacter = companionCharacter,
        narrationPlaying = false,
        bumperVoiceRawResId = StoryIntroBumperAudio.introBumperRawRes(2, companionCharacter),
        bumperBodyText = StoryIntroBumperAudio.introBumperBodyText(2, companionCharacter),
        voiceRawResId = voiceRawResId,
        bodyLineHeightOverride = 20.sp,
        dinoContentDescription = companionCharacter.displayNameHebrew(),
        onContinue = onContinue,
        modifier = modifier,
    )
}
