package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tal.hebrewdino.R
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
        body = Chapter1CompanionCopy.chapter1MidBoostBody(companionCharacter),
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
            "הולך לכם נהדר!\n" +
                "עכשיו הדרך עולה למעלה, והעקבות נהיו ברורות יותר.\n" +
                "עוד קצת — ואולי נגלה מי עבר כאן ומה נלקח.",
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
    companionCharacter: DinoCharacter?,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val voiceRawResId =
        when (companionCharacter) {
            DinoCharacter.Dino -> R.raw.ch3_story_mid_dino
            DinoCharacter.Dina -> R.raw.ch3_story_mid_dina
            null -> 0
        }
    val body =
        when (companionCharacter) {
            DinoCharacter.Dino, null ->
                "אתם מתקדמים נפלא!\n" +
                    "העקבות כבר ברורות יותר, ודינו שומע רעש קטן מאחורי הסלעים.\n" +
                    "עוד קצת מאמץ — ואולי נגלה מה מסתתר שם."
            DinoCharacter.Dina ->
                "אתם מתקדמים נפלא!\n" +
                    "העקבות כבר ברורות יותר, ודינה שומעת רעש קטן מאחורי הסלעים.\n" +
                    "עוד קצת מאמץ — ואולי נגלה מה מסתתר שם."
        }
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.ch3_journey_bg,
        title = "ממשיכים!",
        body = body,
        companion = ChapterLobbyCompanion.DinoOnly,
        chapterId = 3,
        storyContext = "Chapter3MidBoostScreen",
        useCompanionDinoArt = true,
        companionCharacter = companionCharacter ?: DinoCharacter.Dino,
        voiceRawResId = voiceRawResId,
        dinoContentDescription = (companionCharacter ?: DinoCharacter.Dino).displayNameHebrew(),
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter4MidBoostScreen(
    companionCharacter: DinoCharacter?,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val voiceRawResId =
        when (companionCharacter) {
            DinoCharacter.Dino -> R.raw.ch4_story_mid_dino
            DinoCharacter.Dina -> R.raw.ch4_story_mid_dina
            null -> 0
        }
    val body =
        when (companionCharacter) {
            DinoCharacter.Dino, null ->
                "אתם עוזרים נפלא!\n" +
                    "דינו מביט בשביל ורואה עוד סימנים קטנים.\n" +
                    "הם מובילים לכיוון העצים.\n" +
                    "עוד קצת — ואולי נמצא רמז שיראה לנו לאן להמשיך."
            DinoCharacter.Dina ->
                "אתם עוזרים נפלא!\n" +
                    "דינה מביטה בשביל ורואה עוד סימנים קטנים.\n" +
                    "הם מובילים לכיוון העצים.\n" +
                    "עוד קצת — ואולי נמצא רמז שיראה לנו לאן להמשיך."
        }
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.forest_bg_journey_road,
        title = "כיף לראות!",
        body = body,
        companion = ChapterLobbyCompanion.DinoOnly,
        chapterId = 4,
        storyContext = "Chapter4MidBoostScreen",
        useCompanionDinoArt = true,
        companionCharacter = companionCharacter ?: DinoCharacter.Dino,
        voiceRawResId = voiceRawResId,
        dinoContentDescription = (companionCharacter ?: DinoCharacter.Dino).displayNameHebrew(),
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter5MidBoostScreen(
    companionCharacter: DinoCharacter?,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val voiceRawResId =
        when (companionCharacter) {
            DinoCharacter.Dino -> R.raw.ch5_story_mid_dino
            DinoCharacter.Dina -> R.raw.ch5_story_mid_dina
            null -> 0
        }
    val body =
        when (companionCharacter) {
            DinoCharacter.Dino, null ->
                "אתם עושים עבודה נפלאה!\n" +
                    "דינו מוצא עוד קליפה קטנה, והפעם היא ממש טרייה.\n" +
                    "הסימנים מובילים אל מקום שקט מאחורי השיחים.\n" +
                    "עוד קצת — ואולי נמצא את הביצה האחרונה."
            DinoCharacter.Dina ->
                "אתם עושים עבודה נפלאה!\n" +
                    "דינה מוצאת עוד קליפה קטנה, והפעם היא ממש טרייה.\n" +
                    "הסימנים מובילים אל מקום שקט מאחורי השיחים.\n" +
                    "עוד קצת — ואולי נמצא את הביצה האחרונה."
        }
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.forest_bg_journey_road,
        title = "ממשיכים!",
        body = body,
        companion = ChapterLobbyCompanion.DinoOnly,
        chapterId = 5,
        storyContext = "Chapter5MidBoostScreen",
        useCompanionDinoArt = true,
        companionCharacter = companionCharacter ?: DinoCharacter.Dino,
        voiceRawResId = voiceRawResId,
        dinoContentDescription = (companionCharacter ?: DinoCharacter.Dino).displayNameHebrew(),
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter6MidBoostScreen(
    companionCharacter: DinoCharacter?,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val voiceRawResId =
        when (companionCharacter) {
            DinoCharacter.Dino -> R.raw.ch6_story_mid_dino
            DinoCharacter.Dina -> R.raw.ch6_story_mid_dina
            null -> 0
        }
    val body =
        when (companionCharacter) {
            DinoCharacter.Dino, null ->
                "אתם כבר ממש קרובים לסוף!\n" +
                    "דינו מתקדם בשמחה, והביצים נשמרות ביחד.\n" +
                    "עוד קצת קריאה, עוד קצת מאמץ — והמסע יושלם."
            DinoCharacter.Dina ->
                "אתם כבר ממש קרובים לסוף!\n" +
                    "דינה מתקדמת בשמחה, והביצים נשמרות ביחד.\n" +
                    "עוד קצת קריאה, עוד קצת מאמץ — והמסע יושלם."
        }
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.forest_bg_journey_road,
        title = "ממשיכים!",
        body = body,
        companion = ChapterLobbyCompanion.DinoOnly,
        chapterId = 6,
        storyContext = "Chapter6MidBoostScreen",
        useCompanionDinoArt = true,
        companionCharacter = companionCharacter ?: DinoCharacter.Dino,
        voiceRawResId = voiceRawResId,
        dinoContentDescription = (companionCharacter ?: DinoCharacter.Dino).displayNameHebrew(),
        onContinue = onContinue,
        modifier = modifier,
    )
}
