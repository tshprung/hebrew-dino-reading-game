package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips

@Composable
fun Chapter1MidBoostScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.forest_bg_story_intro,
        title = "ממשיכים!",
        body =
            "יופי! פתרת את החידות כמו בלש/ית אמיתי/ת.\n" +
                "יש כאן סימנים שמובילים קדימה…\n" +
                "בואו נמשיך ונראה אם נגיע לביצה.",
        eggStripCount = 0,
        companion = ChapterLobbyCompanion.DinoOnly,
        voiceAssetPath = AudioClips.StoryCh1MidBoost,
        dinoContentDescription = "דינו",
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter2MidBoostScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.chapter2_story_intro,
        title = "ממש טוב!",
        body =
            "הולך לכם נהדר!\n" +
                "עכשיו הדרך עולה למעלה, והעקבות נהיות ברורות יותר.\n" +
                "עוד קצת—ואולי נגלה מי עבר כאן ומה הוא לקח.",
        eggStripCount = 0,
        companion = ChapterLobbyCompanion.DinoOnly,
        voiceAssetPath = AudioClips.StoryCh2MidBoost,
        dinoContentDescription = "דינו",
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter3MidBoostScreen(
    eggStripCount: Int,
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
        eggStripCount = eggStripCount,
        companion = ChapterLobbyCompanion.DinoOnly,
        voiceAssetPath = AudioClips.StoryCh3MidBoost,
        dinoContentDescription = "דינו",
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter4MidBoostScreen(
    eggStripCount: Int,
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
        eggStripCount = eggStripCount,
        companion = ChapterLobbyCompanion.DinoOnly,
        voiceAssetPath = AudioClips.StoryCh4MidBoost,
        dinoContentDescription = "דינו",
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun Chapter5MidBoostScreen(
    eggStripCount: Int,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.forest_bg_journey_road,
        title = "ממשיכים!",
        body =
            "אתם כבר מקשיבים וקוראים כמו בלשים אמיתיים.\n" +
                "עוד קצת — ואולי נגיע לביצה השלישית.\n" +
                "בואו נסיים את המסע!",
        eggStripCount = eggStripCount,
        companion = ChapterLobbyCompanion.DinoOnly,
        voiceAssetPath = AudioClips.StoryCh5MidBoost,
        dinoContentDescription = "דינו",
        onContinue = onContinue,
        modifier = modifier,
    )
}

