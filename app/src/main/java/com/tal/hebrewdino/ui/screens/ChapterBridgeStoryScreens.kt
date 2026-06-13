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
import com.tal.hebrewdino.ui.audio.StoryIntroBumperAudio
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
                "העקבות ממשיכות קדימה…\n" +
                    "נראה שמישהו עבר כאן ולקח משהו.\n" +
                    "דינו ממשיך לעקוב אחרי הדרך, ועכשיו יש כיוון ברור.\n" +
                    "בואו נתקדם אחרי העקבות בפרק הבא."
            DinoCharacter.Dina ->
                "העקבות ממשיכות קדימה…\n" +
                    "נראה שמישהו עבר כאן ולקח משהו.\n" +
                    "דינה ממשיכה לעקוב אחרי הדרך, ועכשיו יש כיוון ברור.\n" +
                    "בואו נתקדם אחרי העקבות בפרק הבא."
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
                "מאחורי הסלעים מופיעה ביצה נוספת!\n" +
                    "דינו מתרגש מאוד — מצאנו את הביצה השנייה!\n" +
                    "אבל עדיין חסרה עוד ביצה אחת.\n" +
                    "בואו נחזור אל השביל ונמשיך במסע."
            DinoCharacter.Dina ->
                "מאחורי הסלעים מופיעה ביצה נוספת!\n" +
                    "דינה מתרגשת מאוד — מצאנו את הביצה השנייה!\n" +
                    "אבל עדיין חסרה עוד ביצה אחת.\n" +
                    "בואו נחזור אל השביל ונמשיך במסע."
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
        foundEggDrawableRes = R.drawable.egg_pink_up,
        narrationPlaying = false,
        voiceRawResId = voiceRawResId,
        dinoContentDescription = "ביצה ורודה",
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
    val portraitCharacter = companionCharacter ?: DinoCharacter.Dino
    val voiceRawResId =
        when (companionCharacter) {
            DinoCharacter.Dino -> R.raw.ch4_story_intro_dino
            DinoCharacter.Dina -> R.raw.ch4_story_intro_dina
            null -> 0
        }
    val bumperVoiceRawResId = StoryIntroBumperAudio.introBumperRawRes(4, portraitCharacter)
    val bumperBodyText = StoryIntroBumperAudio.introBumperBodyText(4, portraitCharacter)
    val body =
        when (companionCharacter) {
            DinoCharacter.Dino, null ->
                "מצאנו כבר שתי ביצים — איזה כיף!\n" +
                    "אבל עדיין חסרה עוד ביצה אחת.\n" +
                    "דינו יורד מההר וממשיך בשביל.\n" +
                    "על האדמה יש סימנים קטנים… אולי הם יובילו אותנו אל הביצה השלישית.\n" +
                    "בואו נעזור לדינו לחפש רמז חדש."
            DinoCharacter.Dina ->
                "מצאנו כבר שתי ביצים — איזה כיף!\n" +
                    "אבל עדיין חסרה עוד ביצה אחת.\n" +
                    "דינה יורדת מההר וממשיכה בשביל.\n" +
                    "על האדמה יש סימנים קטנים… אולי הם יובילו אותנו אל הביצה השלישית.\n" +
                    "בואו נעזור לדינה לחפש רמז חדש."
        }
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.forest_bg_journey_road,
        title = "פרק 4 - סיבוך בדרך",
        body = body,
        companion = ChapterLobbyCompanion.DinoOnly,
        chapterId = 4,
        storyContext = "Chapter4IntroScreen",
        narrationPlaying = false,
        useCompanionDinoArt = true,
        companionCharacter = portraitCharacter,
        bumperVoiceRawResId = bumperVoiceRawResId,
        bumperBodyText = bumperBodyText,
        voiceRawResId = voiceRawResId,
        dinoContentDescription = portraitCharacter.displayNameHebrew(),
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
                "הנה! בין העלים מצאנו קליפה קטנה של ביצה.\n" +
                    "זאת לא הביצה האחרונה, אבל זה רמז חשוב!\n" +
                    "דינו מתרגש — אנחנו קרובים.\n" +
                    "בפרק הבא נמשיך לחפש, ואולי נמצא את הביצה האחרונה."
            DinoCharacter.Dina ->
                "הנה! בין העלים מצאנו קליפה קטנה של ביצה.\n" +
                    "זאת לא הביצה האחרונה, אבל זה רמז חשוב!\n" +
                    "דינה מתרגשת — אנחנו קרובים.\n" +
                    "בפרק הבא נמשיך לחפש, ואולי נמצא את הביצה האחרונה."
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
    val portraitCharacter = companionCharacter ?: DinoCharacter.Dino
    val voiceRawResId =
        when (companionCharacter) {
            DinoCharacter.Dino -> R.raw.ch5_story_intro_dino
            DinoCharacter.Dina -> R.raw.ch5_story_intro_dina
            null -> 0
        }
    val bumperVoiceRawResId = StoryIntroBumperAudio.introBumperRawRes(5, portraitCharacter)
    val bumperBodyText = StoryIntroBumperAudio.introBumperBodyText(5, portraitCharacter)
    val body =
        when (companionCharacter) {
            DinoCharacter.Dino, null ->
                "מצאנו קליפה קטנה של ביצה — זה רמז חשוב!\n" +
                    "דינו ממשיך ללכת בין העצים ומחפש סימנים נוספים.\n" +
                    "השביל נעשה צר יותר, ובין העלים רואים עוד סימן קטן.\n" +
                    "אולי הביצה האחרונה ממש קרובה.\n" +
                    "בואו נעזור לדינו להמשיך לחפש."
            DinoCharacter.Dina ->
                "מצאנו קליפה קטנה של ביצה — זה רמז חשוב!\n" +
                    "דינה ממשיכה ללכת בין העצים ומחפשת סימנים נוספים.\n" +
                    "השביל נעשה צר יותר, ובין העלים רואים עוד סימן קטן.\n" +
                    "אולי הביצה האחרונה ממש קרובה.\n" +
                    "בואו נעזור לדינה להמשיך לחפש."
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
        companionCharacter = portraitCharacter,
        bumperVoiceRawResId = bumperVoiceRawResId,
        bumperBodyText = bumperBodyText,
        voiceRawResId = voiceRawResId,
        dinoContentDescription = portraitCharacter.displayNameHebrew(),
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
                "הנה היא!\n" +
                    "מאחורי השיחים מופיעה הביצה האחרונה.\n" +
                    "דינו קופץ מרוב שמחה — מצאנו את כל שלוש הביצים!\n" +
                    "עכשיו המסע כמעט הושלם.\n" +
                    "בפרק הבא נחזור יחד ונראה מה קורה כשכל הביצים ביחד."
            DinoCharacter.Dina ->
                "הנה היא!\n" +
                    "מאחורי השיחים מופיעה הביצה האחרונה.\n" +
                    "דינה קופצת מרוב שמחה — מצאנו את כל שלוש הביצים!\n" +
                    "עכשיו המסע כמעט הושלם.\n" +
                    "בפרק הבא נחזור יחד ונראה מה קורה כשכל הביצים ביחד."
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
    val portraitCharacter = companionCharacter ?: DinoCharacter.Dino
    val voiceRawResId =
        when (companionCharacter) {
            DinoCharacter.Dino -> R.raw.ch6_story_intro_dino
            DinoCharacter.Dina -> R.raw.ch6_story_intro_dina
            null -> 0
        }
    val bumperVoiceRawResId = StoryIntroBumperAudio.introBumperRawRes(6, portraitCharacter)
    val bumperBodyText = StoryIntroBumperAudio.introBumperBodyText(6, portraitCharacter)
    val body =
        when (companionCharacter) {
            DinoCharacter.Dino, null ->
                "מצאנו את כל שלוש הביצים!\n" +
                    "דינו מחזיק את הביצים בזהירות ומתחיל לחזור בשביל.\n" +
                    "הדרך חזרה מלאה באור, וכולם מחכים לראות מה מצאנו.\n" +
                    "בואו נעזור לדינו להשלים את המסע."
            DinoCharacter.Dina ->
                "מצאנו את כל שלוש הביצים!\n" +
                    "דינה מחזיקה את הביצים בזהירות ומתחילה לחזור בשביל.\n" +
                    "הדרך חזרה מלאה באור, וכולם מחכים לראות מה מצאנו.\n" +
                    "בואו נעזור לדינה להשלים את המסע."
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
        companionCharacter = portraitCharacter,
        bumperVoiceRawResId = bumperVoiceRawResId,
        bumperBodyText = bumperBodyText,
        voiceRawResId = voiceRawResId,
        dinoContentDescription = portraitCharacter.displayNameHebrew(),
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
                "הגענו!\n" +
                    "דינו מניח את שלוש הביצים במקום בטוח.\n" +
                    "כולם שמחים — המסע הצליח!\n" +
                    "עזרתם לדינו לקרוא, לחפש, ולמצוא את כל הביצים.\n" +
                    "כל הכבוד!"
            DinoCharacter.Dina ->
                "הגענו!\n" +
                    "דינה מניחה את שלוש הביצים במקום בטוח.\n" +
                    "כולם שמחים — המסע הצליח!\n" +
                    "עזרתם לדינה לקרוא, לחפש, ולמצוא את כל הביצים.\n" +
                    "כל הכבוד!"
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
