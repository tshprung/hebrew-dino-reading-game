package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.StoryIntroBumperAudio
import com.tal.hebrewdino.ui.companion.displayNameHebrew
import com.tal.hebrewdino.ui.data.DinoCharacter

@Composable
fun Chapter3IntroScreen(
    companionCharacter: DinoCharacter?,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val portraitCharacter = companionCharacter ?: DinoCharacter.Dino
    val voiceRawResId =
        when (companionCharacter) {
            DinoCharacter.Dino -> R.raw.ch3_story_intro_dino
            DinoCharacter.Dina -> R.raw.ch3_story_intro_dina
            null -> 0
        }
    val bumperVoiceRawResId = StoryIntroBumperAudio.introBumperRawRes(3, portraitCharacter)
    val bumperBodyText = StoryIntroBumperAudio.introBumperBodyText(3, portraitCharacter)
    val body =
        when (companionCharacter) {
            DinoCharacter.Dino, null ->
                "העקבות מובילות אותנו לתוך ההרים.\n" +
                    "הדרך מטפסת למעלה, ודינו מסתכל סביב בזהירות.\n" +
                    "פתאום רואים סימן קטן על השביל.\n" +
                    "אולי הביצה הבאה קרובה!\n" +
                    "בואו נעזור לדינו להמשיך לעקוב אחרי העקבות."
            DinoCharacter.Dina ->
                "העקבות מובילות אותנו לתוך ההרים.\n" +
                    "הדרך מטפסת למעלה, ודינה מסתכלת סביב בזהירות.\n" +
                    "פתאום רואים סימן קטן על השביל.\n" +
                    "אולי הביצה הבאה קרובה!\n" +
                    "בואו נעזור לדינה להמשיך לעקוב אחרי העקבות."
        }
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.ch3_journey_bg,
        title = "פרק 3 — בעקבות העקבות",
        body = body,
        companion = ChapterLobbyCompanion.DinoOnly,
        chapterId = 3,
        storyContext = "Chapter3IntroScreen",
        useCompanionDinoArt = true,
        companionCharacter = portraitCharacter,
        narrationPlaying = false,
        bumperVoiceRawResId = bumperVoiceRawResId,
        bumperBodyText = bumperBodyText,
        voiceRawResId = voiceRawResId,
        dinoContentDescription = portraitCharacter.displayNameHebrew(),
        onContinue = onContinue,
        modifier = modifier,
    )
}
