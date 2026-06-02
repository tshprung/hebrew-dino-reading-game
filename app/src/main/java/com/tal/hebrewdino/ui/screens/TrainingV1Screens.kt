package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.data.PlayerAddress
import com.tal.hebrewdino.ui.domain.StationQuizPlans
import com.tal.hebrewdino.ui.domain.TrainingV1Config
import com.tal.hebrewdino.ui.domain.TrainingV1LetterPoolSpec

@Composable
fun TrainingV1IntroScreen(
    companionCharacter: DinoCharacter,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val voiceRawResId =
        when (companionCharacter) {
            DinoCharacter.Dino -> R.raw.training_intro_dino
            DinoCharacter.Dina -> R.raw.training_intro_dina
        }
    val body =
        when (companionCharacter) {
            DinoCharacter.Dino ->
                "ברוכים הבאים לאימון! כאן אפשר לתרגל אותיות ומילים בלי לחץ.\n" +
                    "דינו יעזור לכם להקשיב, לבחור, ולנסות שוב כשצריך.\n" +
                    "בואו נתחיל."
            DinoCharacter.Dina ->
                "ברוכים הבאים לאימון! כאן אפשר לתרגל אותיות ומילים בלי לחץ.\n" +
                    "דינה תעזור לכם להקשיב, לבחור, ולנסות שוב כשצריך.\n" +
                    "בואו נתחיל."
        }
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.forest_bg_journey_road,
        title = "אימון",
        body = body,
        companion = ChapterLobbyCompanion.DinoOnly,
        chapterId = TrainingV1Config.CHAPTER_ID,
        storyContext = "TrainingV1IntroScreen",
        showStoryCharacterArt = false,
        voiceRawResId = voiceRawResId,
        dinoContentDescription = "",
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun TrainingV1RoundScreen(
    roundIndex: Int,
    onBack: () -> Unit,
    onRoundComplete: () -> Unit,
    playerAddress: PlayerAddress?,
    modifier: Modifier = Modifier,
) {
    val round = roundIndex.coerceIn(1, TrainingV1Config.TOTAL_ROUNDS)
    val stationId =
        trainingStationOrderV1.getOrNull(round - 1)
            ?: TrainingV1Config.STATION_HEAR_LETTER_CHOOSE
    val basePlan = StationQuizPlans.trainingV1(stationId)
    val plan =
        when {
            stationId == TrainingV1Config.STATION_WHICH_WORD_STARTS_WITH_LETTER && (round == 2 || round == 9) ->
                basePlan.copy(
                    imageMatchCaptionSizeMultiplier = basePlan.imageMatchCaptionSizeMultiplier * 0.50f,
                    imageMatchPictureSizeMultiplier = basePlan.imageMatchPictureSizeMultiplier * 0.50f,
                )
            else -> basePlan
        }
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
        chapter1PlayerAddress = playerAddress,
        modifier = modifier,
    )
}

@Composable
fun TrainingV1CompleteScreen(
    companionCharacter: DinoCharacter,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val voiceRawResId =
        when (companionCharacter) {
            DinoCharacter.Dino -> R.raw.training_outro_dino
            DinoCharacter.Dina -> R.raw.training_outro_dina
        }
    val body =
        when (companionCharacter) {
            DinoCharacter.Dino ->
                "כל הכבוד! סיימתם את האימון.\n" +
                    "עכשיו אתם מוכנים יותר להמשיך במסע עם דינו."
            DinoCharacter.Dina ->
                "כל הכבוד! סיימתם את האימון.\n" +
                    "עכשיו אתם מוכנים יותר להמשיך במסע עם דינה."
        }
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.forest_bg_journey_road,
        title = "כל הכבוד!",
        body = body,
        companion = ChapterLobbyCompanion.DinoOnly,
        chapterId = TrainingV1Config.CHAPTER_ID,
        storyContext = "TrainingV1CompleteScreen",
        showStoryCharacterArt = false,
        voiceRawResId = voiceRawResId,
        dinoContentDescription = "",
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
