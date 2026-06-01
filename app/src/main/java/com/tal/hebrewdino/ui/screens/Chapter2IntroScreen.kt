package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.R
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
                "מָצָאנוּ כְּבָר בֵּיצָה אַחַת — מְעוּלֶה!\n" +
                    "דִּינוֹ מַמְשִׁיךְ בַּדֶּרֶךְ, וְהַשְּׁבִיל מַתְחִיל לַעֲלוֹת לְמַעְלָה, לְתוֹךְ הֶהָרִים.\n" +
                    "עַל הָאֲדָמָה יֵשׁ עֲקֵבוֹת… מִישֶׁהוּ עָבַר כָּאן קֹדֶם.\n" +
                    "בּוֹאוּ נַעֲקֹב אַחֲרֵי הָעֲקֵבוֹת וְנִרְאֶה לְאָן הֵן מוֹבִילוֹת."
            DinoCharacter.Dina ->
                "מָצָאנוּ כְּבָר בֵּיצָה אַחַת — מְעוּלֶה!\n" +
                    "דִּינָה מַמְשִׁיכָה בַּדֶּרֶךְ, וְהַשְּׁבִיל מַתְחִיל לַעֲלוֹת לְמַעְלָה, לְתוֹךְ הֶהָרִים.\n" +
                    "עַל הָאֲדָמָה יֵשׁ עֲקֵבוֹת… מִישֶׁהוּ עָבַר כָּאן קֹדֶם.\n" +
                    "בּוֹאוּ נַעֲקֹב אַחֲרֵי הָעֲקֵבוֹת וְנִרְאֶה לְאָן הֵן מוֹבִילוֹת."
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
        voiceRawResId = voiceRawResId,
        bodyLineHeightOverride = 20.sp,
        dinoContentDescription = companionCharacter.displayNameHebrew(),
        onContinue = onContinue,
        modifier = modifier,
    )
}
