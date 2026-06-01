package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.companion.Chapter1AddressAwareAudio
import com.tal.hebrewdino.ui.companion.Chapter1CompanionCopy
import com.tal.hebrewdino.ui.companion.displayNameHebrew
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.data.PlayerAddress

@Composable
fun Chapter1MidBoostScreen(
    companionCharacter: DinoCharacter,
    playerAddress: PlayerAddress,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.forest_bg_story_intro,
        title = "ממשיכים!",
        body = Chapter1CompanionCopy.chapter1MidBoostBody(companionCharacter, playerAddress),
        companion = ChapterLobbyCompanion.DinoOnly,
        chapterId = 1,
        storyContext = "Chapter1MidBoostScreen",
        useCompanionDinoArt = true,
        companionCharacter = companionCharacter,
        useWarmReadableStoryPanel = true,
        voiceRawResId = Chapter1AddressAwareAudio.storyMidRawRes(companionCharacter),
        dinoContentDescription = companionCharacter.displayNameHebrew(),
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter2MidBoostScreen(
    companionCharacter: DinoCharacter,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val voiceRawResId =
        when (companionCharacter) {
            DinoCharacter.Dino -> R.raw.ch2_story_mid_dino
            DinoCharacter.Dina -> R.raw.ch2_story_mid_dina
        }
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.chapter2_story_intro,
        title = "ממש טוב!",
        body =
            "הוֹלֵךְ לָכֶם נֶהֱדָר!\n" +
                "עַכְשָׁיו הַדֶּרֶךְ עוֹלָה לְמַעְלָה, וְהָעֲקֵבוֹת נִהְיוֹת בְּרוּרוֹת יוֹתֵר.\n" +
                "עוֹד קְצָת — וְאוּלַי נְגַלֶּה מִי עָבַר כָּאן וּמָה נִלְקַח.",
        companion = ChapterLobbyCompanion.DinoOnly,
        chapterId = 2,
        storyContext = "Chapter2MidBoostScreen",
        useCompanionDinoArt = true,
        companionCharacter = companionCharacter,
        voiceRawResId = voiceRawResId,
        dinoContentDescription = companionCharacter.displayNameHebrew(),
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter3MidBoostScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.ch3_journey_bg,
        title = "ממשיכים!",
        body =
            "איזה יופי! אתם מתקדמים מצוין.\n" +
                "עוד קצת והביצה הורודה כבר מרגישה קרובה.\n" +
                "בואו נמשיך לגלות מה מחכה לנו בהמשך.",
        companion = ChapterLobbyCompanion.DinoOnly,
        voiceAssetPath = AudioClips.StoryCh3MidBoost,
        dinoContentDescription = "דינו",
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter4MidBoostScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.forest_bg_journey_road,
        title = "כיף לראות!",
        body =
            "אתם זוכרים ומחברים מילים ממש טוב.\n" +
                "עוד קצת חיזוק ואתם מוכנים לעוד אתגרים.\n" +
                "בואו נמשיך ביחד!",
        companion = ChapterLobbyCompanion.DinoOnly,
        chapterId = 4,
        storyContext = "Chapter4MidBoostScreen",
        voiceAssetPath = AudioClips.StoryCh4MidBoost,
        dinoContentDescription = "דינו",
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter5MidBoostScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.forest_bg_journey_road,
        title = "ממשיכים!",
        body =
            "אתם כבר מקשיבים וקוראים מצוין.\n" +
                "עוד קצת — ואולי נגיע לביצה השלישית.\n" +
                "בואו נסיים את המסע!",
        companion = ChapterLobbyCompanion.DinoOnly,
        chapterId = 5,
        storyContext = "Chapter5MidBoostScreen",
        voiceAssetPath = AudioClips.StoryCh5MidBoost,
        dinoContentDescription = "דינו",
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter6MidBoostScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.forest_bg_journey_road,
        title = "ממשיכים!",
        body =
            "כמעט בבית!\n" +
                "דינו כבר רואה את הדרך לאמא.\n" +
                "נמשיך לתרגל ונחזיר את הביצים הביתה.",
        companion = ChapterLobbyCompanion.DinoOnly,
        voiceAssetPath = AudioClips.StoryCh6MidBoost,
        dinoContentDescription = "דינו",
        onContinue = onContinue,
        modifier = modifier,
    )
}
