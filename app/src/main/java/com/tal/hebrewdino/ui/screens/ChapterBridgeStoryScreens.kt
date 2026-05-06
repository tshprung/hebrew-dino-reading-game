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
import com.tal.hebrewdino.ui.audio.AudioClips

@Composable
fun Chapter2OutroScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.chapter2_story_outro,
        title = "יש!",
        body =
            "העקבות ממשיכות קדימה…\n" +
                "\n" +
                "נראה שמישהו עבר כאן ולקח משהו.\n" +
                "\n" +
                "דינו ממשיך לעקוב אחרי הדרך,\n" +
                "ועכשיו יש כיוון ברור.\n" +
                "\n" +
                "בואו נתקדם אחרי העקבות בפרק הבא.",
        eggStripCount = 1,
        companion = ChapterLobbyCompanion.DinoOnly,
        narrationPlaying = false,
        voiceAssetPath = AudioClips.StoryMountainApproachOutro,
        dinoContentDescription = "דינו",
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter3OutroScreen(
    eggStripCount: Int,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLobbyStoryLayout(
        // Use the same background as in-station gameplay (user preference).
        backgroundRes = R.drawable.ch3_journey_bg,
        title = "הנה היא!",
        body =
            "הנה היא!\n" +
                "\n" +
                "דינו מצא עוד ביצה — והיא שלמה!\n" +
                "\n" +
                "הוא אוסף אותה בזהירות,\n" +
                "וממשיך במסע למצוא את הביצה האחרונה.",
        eggStripCount = eggStripCount,
        companion = ChapterLobbyCompanion.DinoOnly,
        narrationPlaying = false,
        voiceAssetPath = AudioClips.StoryMountainPathOutro,
        dinoContentDescription = "דינו",
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter4IntroScreen(
    eggStripCount: Int,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
        eggStripCount = eggStripCount,
        companion = ChapterLobbyCompanion.DinoOnly,
        narrationPlaying = false,
        voiceAssetPath = AudioClips.StoryCh4Intro,
        dinoContentDescription = "דינו",
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter4OutroScreen(
    eggStripCount: Int,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.forest_bg_journey_road,
        title = "מצאנו רמז!",
        body =
            "מצאנו רמז! האות פ… הביצה בטח קרובה!\n" +
                "\n" +
                "דינו ממשיך קדימה — העקבות מובילות הלאה, והמסע עוד לא נגמר.",
        eggStripCount = eggStripCount,
        companion = ChapterLobbyCompanion.DinoOnly,
        narrationPlaying = false,
        voiceAssetPath = AudioClips.StoryCh4ClueOutro,
        dinoContentDescription = "דינו",
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
    eggStripCount: Int,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.forest_bg_journey_road,
        title = "פרק 5 - הביצה השלישית",
        body =
            "אחרי כל הרמזים והדרך הארוכה —\n" +
                "הביצה השלישית כבר מרגישה קרובה.\n" +
                "\n" +
                "עוד קצת קשב, עוד קצת חיזוק…\n" +
                "ואולי הפעם נמצא אותה.",
        eggStripCount = eggStripCount,
        companion = ChapterLobbyCompanion.DinoOnly,
        narrationPlaying = false,
        voiceAssetPath = AudioClips.StoryCh5Intro,
        dinoContentDescription = "דינו",
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter5OutroScreen(
    eggStripCount: Int,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.forest_bg_story_outro_egg,
        title = "יש!",
        body =
            "מצאנו את הביצה השלישית!\n" +
                "\n" +
                "דינו אוסף אותה בזהירות.\n" +
                "עכשיו אפשר לחשוב על הדרך חזרה הביתה…\n" +
                "\n" +
                "כל הכבוד! 🎉",
        eggStripCount = eggStripCount,
        companion = ChapterLobbyCompanion.DinoOnly,
        narrationPlaying = false,
        voiceAssetPath = AudioClips.StoryCh5ThirdEggOutro,
        dinoContentDescription = "דינו",
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter6IntroScreen(
    eggStripCount: Int,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.forest_bg_journey_road,
        title = "פרק 6 - חוזרים הביתה",
        body =
            "דינו מצא את שלוש הביצים.\n" +
                "עכשיו צריך לחזור הביתה לאמא.\n" +
                "\n" +
                "בדרך נתרגל את כל האותיות והמילים שלמדנו.",
        eggStripCount = eggStripCount,
        companion = ChapterLobbyCompanion.DinoOnly,
        narrationPlaying = false,
        voiceAssetPath = AudioClips.StoryCh5ThirdEggOutro,
        dinoContentDescription = "דינו",
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter6OutroScreen(
    eggStripCount: Int,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.forest_bg_story_outro_egg,
        title = "חזרנו הביתה!",
        body =
            "דינו חזר לאמא עם שלוש הביצים.\n" +
                "אמא שמחה מאוד.\n" +
                "\n" +
                "כל הכבוד — עזרתם לדינו לקרוא,\n" +
                "להקשיב ולמצוא את הדרך הביתה!",
        eggStripCount = eggStripCount,
        companion = ChapterLobbyCompanion.DinoOnly,
        narrationPlaying = false,
        voiceAssetPath = AudioClips.StoryCh5ThirdEggOutro,
        dinoContentDescription = "דינו",
        onContinue = onContinue,
        modifier = modifier,
    )
}
