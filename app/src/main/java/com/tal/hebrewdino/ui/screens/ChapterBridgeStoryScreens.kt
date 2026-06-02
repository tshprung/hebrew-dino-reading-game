package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.companion.displayNameHebrew
import com.tal.hebrewdino.ui.data.DinoCharacter

@Composable
fun Chapter2OutroScreen(
    companionCharacter: DinoCharacter,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val voiceRawResId =
        when (companionCharacter) {
            DinoCharacter.Dino -> R.raw.ch2_story_outro_dino
            DinoCharacter.Dina -> R.raw.ch2_story_outro_dina
        }
    val body =
        when (companionCharacter) {
            DinoCharacter.Dino ->
                "הָעֲקֵבוֹת מַמְשִׁיכוֹת קָדִימָה…\n" +
                    "נִרְאֶה שֶׁמִּישֶׁהוּ עָבַר כָּאן וְלָקַח מַשֶּׁהוּ.\n" +
                    "דִּינוֹ מַמְשִׁיךְ לַעֲקֹב אַחֲרֵי הַדֶּרֶךְ, וְעַכְשָׁיו יֵשׁ כִּוּוּן בָּרוּר.\n" +
                    "בּוֹאוּ נִתְקַדֵּם אַחֲרֵי הָעֲקֵבוֹת בַּפֶּרֶק הַבָּא."
            DinoCharacter.Dina ->
                "הָעֲקֵבוֹת מַמְשִׁיכוֹת קָדִימָה…\n" +
                    "נִרְאֶה שֶׁמִּישֶׁהוּ עָבַר כָּאן וְלָקַח מַשֶּׁהוּ.\n" +
                    "דִּינָה מַמְשִׁיכָה לַעֲקֹב אַחֲרֵי הַדֶּרֶךְ, וְעַכְשָׁיו יֵשׁ כִּוּוּן בָּרוּר.\n" +
                    "בּוֹאוּ נִתְקַדֵּם אַחֲרֵי הָעֲקֵבוֹת בַּפֶּרֶק הַבָּא."
        }
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.chapter2_story_outro,
        title = "יש!",
        body = body,
        companion = ChapterLobbyCompanion.DinoOnly,
        chapterId = 2,
        storyContext = "Chapter2OutroScreen",
        useCompanionDinoArt = true,
        companionCharacter = companionCharacter,
        narrationPlaying = false,
        voiceRawResId = voiceRawResId,
        dinoContentDescription = companionCharacter.displayNameHebrew(),
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter3OutroScreen(
    companionCharacter: DinoCharacter?,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val voiceRawResId =
        when (companionCharacter) {
            DinoCharacter.Dino -> R.raw.ch3_story_outro_dino
            DinoCharacter.Dina -> R.raw.ch3_story_outro_dina
            null -> 0
        }
    val body =
        when (companionCharacter) {
            DinoCharacter.Dino, null ->
                "מֵאֲחוֹרֵי הַסְּלָעִים מוֹפִיעָה בֵּיצָה נוֹסֶפֶת!\n" +
                    "דִינוֹ מִתְרַגֵּשׁ מְאוֹד — מָצָאנוּ אֶת הַבֵּיצָה הַשְּׁנִיָּה!\n" +
                    "אֲבָל עֲדַיִן חֲסֵרָה עוֹד בֵּיצָה אַחַת.\n" +
                    "בּוֹאוּ נַחְזֹר אֶל הַשְּׁבִיל וְנַמְשִׁיךְ בַּמַּסָּע."
            DinoCharacter.Dina ->
                "מֵאֲחוֹרֵי הַסְּלָעִים מוֹפִיעָה בֵּיצָה נוֹסֶפֶת!\n" +
                    "דִינָה מִתְרַגֶּשֶׁת מְאוֹד — מָצָאנוּ אֶת הַבֵּיצָה הַשְּׁנִיָּה!\n" +
                    "אֲבָל עֲדַיִן חֲסֵרָה עוֹד בֵּיצָה אַחַת.\n" +
                    "בּוֹאוּ נַחְזֹר אֶל הַשְּׁבִיל וְנַמְשִׁיךְ בַּמַּסָּע."
        }
    ChapterLobbyStoryLayout(
        // Use the same background as in-station gameplay (user preference).
        backgroundRes = R.drawable.ch3_journey_bg,
        title = "הנה היא!",
        body = body,
        companion = ChapterLobbyCompanion.DinoOnly,
        chapterId = 3,
        storyContext = "Chapter3OutroScreen",
        useCompanionDinoArt = true,
        companionCharacter = companionCharacter ?: DinoCharacter.Dino,
        narrationPlaying = false,
        voiceRawResId = voiceRawResId,
        dinoContentDescription = (companionCharacter ?: DinoCharacter.Dino).displayNameHebrew(),
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter4IntroScreen(
    companionCharacter: DinoCharacter?,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val voiceRawResId =
        when (companionCharacter) {
            DinoCharacter.Dino -> R.raw.ch4_story_intro_dino
            DinoCharacter.Dina -> R.raw.ch4_story_intro_dina
            null -> 0
        }
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.forest_bg_journey_road,
        title = "פרק 4 - סיבוך בדרך",
        body =
            "המסע אחרי הביצה השלישית ממשיך…\n" +
                "\n" +
                "הדרך מתפתלת: יש רמזים חדשים, וגם סיבוך קטן בדרך.\n" +
                "\n" +
                "עדיין לא מצאנו את הביצה השלישית — אבל כל תחנה מקרבת אותנו.\n" +
                "\n" +
                "בואו נאזין היטב ונמשיך יחד.",
        companion = ChapterLobbyCompanion.DinoOnly,
        chapterId = 4,
        storyContext = "Chapter4IntroScreen",
        narrationPlaying = false,
        useCompanionDinoArt = true,
        companionCharacter = companionCharacter ?: DinoCharacter.Dino,
        voiceRawResId = voiceRawResId,
        dinoContentDescription = (companionCharacter ?: DinoCharacter.Dino).displayNameHebrew(),
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter4OutroScreen(
    companionCharacter: DinoCharacter?,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val voiceRawResId =
        when (companionCharacter) {
            DinoCharacter.Dino -> R.raw.ch4_story_outro_dino
            DinoCharacter.Dina -> R.raw.ch4_story_outro_dina
            null -> 0
        }
    val body =
        when (companionCharacter) {
            DinoCharacter.Dino, null ->
                "מצאנו רמז! האות פ…\n" +
                    "זֹאת לֹא הַבֵּיצָה הָאַחֲרוֹנָה, אֲבָל זֶה רֶמֶז חָשׁוּב!\n" +
                    "\n" +
                    "דינו ממשיך קדימה — העקבות מובילות הלאה, והמסע עוד לא נגמר."
            DinoCharacter.Dina ->
                "מצאנו רמז! האות פ…\n" +
                    "זֹאת לֹא הַבֵּיצָה הָאַחֲרוֹנָה, אֲבָל זֶה רֶמֶז חָשׁוּב!\n" +
                    "\n" +
                    "דינה ממשיכה קדימה — העקבות מובילות הלאה, והמסע עוד לא נגמר."
        }
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.forest_bg_journey_road,
        title = "מצאנו רמז!",
        body = body,
        companion = ChapterLobbyCompanion.DinoOnly,
        chapterId = 4,
        storyContext = "Chapter4OutroScreen",
        narrationPlaying = false,
        useCompanionDinoArt = true,
        companionCharacter = companionCharacter ?: DinoCharacter.Dino,
        voiceRawResId = voiceRawResId,
        dinoContentDescription = (companionCharacter ?: DinoCharacter.Dino).displayNameHebrew(),
        onContinue = onContinue,
        betweenTitleAndBody = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "👣  👣  👣",
                    fontSize = 28.sp,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "פ",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0B2B3D),
                )
            }
        },
        modifier = modifier,
    )
}

@Composable
fun Chapter5IntroScreen(
    companionCharacter: DinoCharacter?,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val voiceRawResId =
        when (companionCharacter) {
            DinoCharacter.Dino -> R.raw.ch5_story_intro_dino
            DinoCharacter.Dina -> R.raw.ch5_story_intro_dina
            null -> 0
        }
    val body =
        when (companionCharacter) {
            DinoCharacter.Dino, null ->
                "מָצָאנוּ קְלִפָּה קְטַנָּה שֶׁל בֵּיצָה — זֶה רֶמֶז חָשׁוּב!\n" +
                    "דִּינוֹ מַמְשִׁיךְ לָלֶכֶת בֵּין הָעֵצִים וּמְחַפֵּשׂ סִימָנִים נוֹסָפִים.\n" +
                    "הַשְּׁבִיל נַעֲשֶׂה צָר יוֹתֵר, וּבֵין הֶעָלִים רוֹאִים עוֹד סִימָן קָטָן.\n" +
                    "אוּלַי הַבֵּיצָה הָאַחֲרוֹנָה מַמָּשׁ קְרוֹבָה.\n" +
                    "בּוֹאוּ נַעֲזֹר לְדִינוֹ לְהַמְשִׁיךְ לְחַפֵּשׂ."
            DinoCharacter.Dina ->
                "מָצָאנוּ קְלִפָּה קְטַנָּה שֶׁל בֵּיצָה — זֶה רֶמֶז חָשׁוּב!\n" +
                    "דִּינָה מַמְשִׁיכָה לָלֶכֶת בֵּין הָעֵצִים וּמְחַפֶּשֶׂת סִימָנִים נוֹסָפִים.\n" +
                    "הַשְּׁבִיל נַעֲשֶׂה צָר יוֹתֵר, וּבֵין הֶעָלִים רוֹאִים עוֹד סִימָן קָטָן.\n" +
                    "אוּלַי הַבֵּיצָה הָאַחֲרוֹנָה מַמָּשׁ קְרוֹבָה.\n" +
                    "בּוֹאוּ נַעֲזֹר לְדִינָה לְהַמְשִׁיךְ לְחַפֵּשׂ."
        }
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.forest_bg_journey_road,
        title = "פרק 5 - הביצה השלישית",
        body = body,
        companion = ChapterLobbyCompanion.DinoOnly,
        chapterId = 5,
        storyContext = "Chapter5IntroScreen",
        narrationPlaying = false,
        useCompanionDinoArt = true,
        companionCharacter = companionCharacter ?: DinoCharacter.Dino,
        voiceRawResId = voiceRawResId,
        dinoContentDescription = (companionCharacter ?: DinoCharacter.Dino).displayNameHebrew(),
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter5OutroScreen(
    companionCharacter: DinoCharacter?,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val voiceRawResId =
        when (companionCharacter) {
            DinoCharacter.Dino -> R.raw.ch5_story_outro_dino
            DinoCharacter.Dina -> R.raw.ch5_story_outro_dina
            null -> 0
        }
    val body =
        when (companionCharacter) {
            DinoCharacter.Dino, null ->
                "הִנֵּה הִיא!\n" +
                    "מֵאֲחוֹרֵי הַשִּׂיחִים מוֹפִיעָה הַבֵּיצָה הָאַחֲרוֹנָה.\n" +
                    "דִּינוֹ קוֹפֵץ מֵרֹב שִׂמְחָה — מָצָאנוּ אֶת כָּל שָׁלוֹשׁ הַבֵּיצִים!\n" +
                    "עַכְשָׁיו הַמַּסָּע כִּמְעַט הֻשְׁלַם.\n" +
                    "בַּפֶּרֶק הַבָּא נַחְזֹר יַחַד וְנִרְאֶה מָה קוֹרֶה כְּשֶׁכָּל הַבֵּיצִים בְּיַחַד."
            DinoCharacter.Dina ->
                "הִנֵּה הִיא!\n" +
                    "מֵאֲחוֹרֵי הַשִּׂיחִים מוֹפִיעָה הַבֵּיצָה הָאַחֲרוֹנָה.\n" +
                    "דִּינָה קוֹפֶצֶת מֵרֹב שִׂמְחָה — מָצָאנוּ אֶת כָּל שָׁלוֹשׁ הַבֵּיצִים!\n" +
                    "עַכְשָׁיו הַמַּסָּע כִּמְעַט הֻשְׁלַם.\n" +
                    "בַּפֶּרֶק הַבָּא נַחְזֹר יַחַד וְנִרְאֶה מָה קוֹרֶה כְּשֶׁכָּל הַבֵּיצִים בְּיַחַד."
        }
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.forest_bg_story_outro_egg,
        title = "יש!",
        body = body,
        companion = ChapterLobbyCompanion.DinoOnly,
        chapterId = 5,
        storyContext = "Chapter5OutroScreen",
        narrationPlaying = false,
        useCompanionDinoArt = true,
        companionCharacter = companionCharacter ?: DinoCharacter.Dino,
        voiceRawResId = voiceRawResId,
        dinoContentDescription = (companionCharacter ?: DinoCharacter.Dino).displayNameHebrew(),
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter6IntroScreen(
    companionCharacter: DinoCharacter?,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val voiceRawResId =
        when (companionCharacter) {
            DinoCharacter.Dino -> R.raw.ch6_story_intro_dino
            DinoCharacter.Dina -> R.raw.ch6_story_intro_dina
            null -> 0
        }
    val body =
        when (companionCharacter) {
            DinoCharacter.Dino, null ->
                "דינו מצא את שלוש הביצים.\n" +
                    "עכשיו צריך לחזור הביתה לאמא.\n" +
                    "\n" +
                    "בדרך נתרגל את כל האותיות והמילים שלמדנו."
            DinoCharacter.Dina ->
                "דינה מצאה את שלוש הביצים.\n" +
                    "עכשיו צריך לחזור הביתה לאמא.\n" +
                    "\n" +
                    "בדרך נתרגל את כל האותיות והמילים שלמדנו."
        }
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.forest_bg_journey_road,
        title = "פרק 6 - חוזרים הביתה",
        body = body,
        companion = ChapterLobbyCompanion.DinoOnly,
        chapterId = 6,
        storyContext = "Chapter6IntroScreen",
        narrationPlaying = false,
        useCompanionDinoArt = true,
        companionCharacter = companionCharacter ?: DinoCharacter.Dino,
        voiceRawResId = voiceRawResId,
        dinoContentDescription = (companionCharacter ?: DinoCharacter.Dino).displayNameHebrew(),
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter6OutroScreen(
    companionCharacter: DinoCharacter?,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val voiceRawResId =
        when (companionCharacter) {
            DinoCharacter.Dino -> R.raw.ch6_story_outro_dino
            DinoCharacter.Dina -> R.raw.ch6_story_outro_dina
            null -> 0
        }
    val body =
        when (companionCharacter) {
            DinoCharacter.Dino, null ->
                "דינו חזר לאמא עם שלוש הביצים.\n" +
                    "אמא שמחה מאוד.\n" +
                    "\n" +
                    "כל הכבוד — עזרתם לדינו לקרוא,\n" +
                    "להקשיב ולמצוא את הדרך הביתה!"
            DinoCharacter.Dina ->
                "דינה חזרה לאמא עם שלוש הביצים.\n" +
                    "אמא שמחה מאוד.\n" +
                    "\n" +
                    "כל הכבוד — עזרתם לדינה לקרוא,\n" +
                    "להקשיב ולמצוא את הדרך הביתה!"
        }
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.forest_bg_story_outro_egg,
        title = "חזרנו הביתה!",
        body = body,
        companion = ChapterLobbyCompanion.DinoOnly,
        chapterId = 6,
        storyContext = "Chapter6OutroScreen",
        narrationPlaying = false,
        useCompanionDinoArt = true,
        companionCharacter = companionCharacter ?: DinoCharacter.Dino,
        voiceRawResId = voiceRawResId,
        dinoContentDescription = (companionCharacter ?: DinoCharacter.Dino).displayNameHebrew(),
        onContinue = onContinue,
        modifier = modifier,
    )
}
