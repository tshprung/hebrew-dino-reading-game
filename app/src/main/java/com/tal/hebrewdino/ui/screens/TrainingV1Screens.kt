package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.domain.StationQuizPlans
import com.tal.hebrewdino.ui.domain.TrainingV1Config
import com.tal.hebrewdino.ui.domain.TrainingV1LetterPoolSpec

@Composable
fun TrainingV1IntroScreen(
    eggStripCount: Int,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.forest_bg_journey_road,
        title = "אימון",
        body = "בוא נתאמן על האותיות והמילים שכבר למדנו!",
        eggStripCount = eggStripCount,
        companion = ChapterLobbyCompanion.DinoOnly,
        voiceAssetPath = null,
        dinoContentDescription = "דינו",
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun TrainingV1RoundScreen(
    roundIndex: Int,
    onBack: () -> Unit,
    onRoundComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val round = roundIndex.coerceIn(1, TrainingV1Config.TOTAL_ROUNDS)
    val stationId =
        trainingStationOrderV1.getOrNull(round - 1)
            ?: TrainingV1Config.STATION_HEAR_LETTER_CHOOSE
    val plan = StationQuizPlans.trainingV1(stationId)
    LetterQuizStationScreen(
        stationId = stationId,
        chapterId = TrainingV1Config.CHAPTER_ID,
        chapterTitle = "אימון",
        stageLabel = "סיבוב $round מתוך ${TrainingV1Config.TOTAL_ROUNDS}",
        topChromeProgressOverride = round to TrainingV1Config.TOTAL_ROUNDS,
        plan = plan,
        letterPoolSpec = TrainingV1LetterPoolSpec,
        backgroundRes = R.drawable.forest_bg_journey_road,
        onBack = onBack,
        onComplete = { _, _, _ -> onRoundComplete() },
        modifier = modifier,
    )
}

@Composable
fun TrainingV1CompleteScreen(
    eggStripCount: Int,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.forest_bg_journey_road,
        title = "כל הכבוד!",
        body = "כל הכבוד! סיימת אימון אותיות!",
        eggStripCount = eggStripCount,
        companion = ChapterLobbyCompanion.DinoOnly,
        voiceAssetPath = null,
        dinoContentDescription = "דינו",
        onContinue = onContinue,
        modifier = modifier,
    )
}

// TODO(Training v2): Controlled randomization:
// - every type appears at least once
// - no same type twice in a row
// - easier rounds early, harder rounds later
private val trainingStationOrderV1: List<Int> =
    listOf(
        TrainingV1Config.STATION_HEAR_LETTER_CHOOSE,
        TrainingV1Config.STATION_WHICH_WORD_STARTS_WITH_LETTER,
        TrainingV1Config.STATION_PICTURE_CHOOSE_WORD,
        TrainingV1Config.STATION_FIND_HEARD_LETTER_IN_GRID,
        TrainingV1Config.STATION_WORD_BALLOONS,
        TrainingV1Config.STATION_MATCH_LETTER_TO_WORD,
        TrainingV1Config.STATION_HEAR_LETTER_CHOOSE,
        TrainingV1Config.STATION_PICTURE_CHOOSE_WORD,
        TrainingV1Config.STATION_WHICH_WORD_STARTS_WITH_LETTER,
        TrainingV1Config.STATION_MATCH_LETTER_TO_WORD,
    )
