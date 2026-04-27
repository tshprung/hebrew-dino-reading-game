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

