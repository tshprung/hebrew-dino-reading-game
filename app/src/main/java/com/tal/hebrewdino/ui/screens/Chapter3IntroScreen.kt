package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.companion.displayNameHebrew
import com.tal.hebrewdino.ui.data.DinoCharacter

@Composable
fun Chapter3IntroScreen(
    companionCharacter: DinoCharacter?,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val voiceRawResId =
        when (companionCharacter) {
            DinoCharacter.Dino -> R.raw.ch3_story_intro_dino
            DinoCharacter.Dina -> R.raw.ch3_story_intro_dina
            null -> 0
        }
    val body =
        when (companionCharacter) {
            DinoCharacter.Dino, null ->
                "הָעֲקֵבוֹת מוֹבִילוֹת אוֹתָנוּ לְתוֹךְ הֶהָרִים.\n" +
                    "הַדֶּרֶךְ מְטַפֶּסֶת לְמַעְלָה, וְדִינוֹ מִסְתַּכֵּל סָבִיב בְּזְהִירוּת.\n" +
                    "פִּתְאוֹם רוֹאִים סִימָן קָטָן עַל הַשְּׁבִיל.\n" +
                    "אוּלַי הַבֵּיצָה הַבָּאָה קְרוֹבָה!\n" +
                    "בּוֹאוּ נַעֲזֹר לְדִינוֹ לְהַמְשִׁיךְ לַעֲקֹב אַחֲרֵי הָעֲקֵבוֹת."
            DinoCharacter.Dina ->
                "הָעֲקֵבוֹת מוֹבִילוֹת אוֹתָנוּ לְתוֹךְ הֶהָרִים.\n" +
                    "הַדֶּרֶךְ מְטַפֶּסֶת לְמַעְלָה, וְדִינָה מִסְתַּכֶּלֶת סָבִיב בְּזְהִירוּת.\n" +
                    "פִּתְאוֹם רוֹאִים סִימָן קָטָן עַל הַשְּׁבִיל.\n" +
                    "אוּלַי הַבֵּיצָה הַבָּאָה קְרוֹבָה!\n" +
                    "בּוֹאוּ נַעֲזֹר לְדִינָה לְהַמְשִׁיךְ לַעֲקֹב אַחֲרֵי הָעֲקֵבוֹת."
        }
    ChapterLobbyStoryLayout(
        // Use the same background as in-station gameplay (user preference).
        backgroundRes = R.drawable.ch3_journey_bg,
        title = "פרק 3 — בעקבות העקבות",
        body = body,
        companion = ChapterLobbyCompanion.DinoOnly,
        chapterId = 3,
        storyContext = "Chapter3IntroScreen",
        useCompanionDinoArt = true,
        companionCharacter = companionCharacter ?: DinoCharacter.Dino,
        narrationPlaying = false,
        voiceRawResId = voiceRawResId,
        dinoContentDescription = (companionCharacter ?: DinoCharacter.Dino).displayNameHebrew(),
        onContinue = onContinue,
        modifier = modifier,
    )
}
