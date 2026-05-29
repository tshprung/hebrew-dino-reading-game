package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.tal.hebrewdino.ui.components.station.Chapter3SagaPopBalloonsWordBanner
import com.tal.hebrewdino.ui.components.station.PopBalloonsInstructionHeaderBlock
import com.tal.hebrewdino.ui.components.station.PopBalloonsStationContent
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.Season2ChapterIds
import com.tal.hebrewdino.ui.domain.StationQuizMode
import com.tal.hebrewdino.ui.domain.StationUiSpec

@Composable
internal fun PopBalloonsQuestionRenderer(
    current: Question.PopBalloonsQuestion,
    planMode: StationQuizMode,
    planPopAllLettersInWord: Boolean,
    popAllLettersWordForBanner: String,
    popAllLettersBannerInstruction: String,
    stationUiSpec: StationUiSpec,
    isCompactLandscapePhone: Boolean,
    listenOnly: Boolean,
    sagaUsesPopBalloonsAudioStaging: Boolean,
    showPopBalloonsTargetLetterChip: Boolean,
    episode4HelpSt15: Boolean,
    episode4HelpActiveHintLetter: String?,
    hintHeaderScale: Float,
    station2PinnedBalloonLetter: String?,
    station2PinnedBalloonColor: Color?,
    correctLetterSet: Set<String>?,
    enabled: Boolean,
    shakePx: Float,
    entryPulseScale: Float,
    visualRoundSeed: Int,
    maxVisibleBalloonCount: Int?,
    episode4CorrectBalloonHintEpoch: Int,
    helpSideInsetDp: Dp,
    contentTopPaddingDp: Dp = 0.dp,
    onBalloonPressed: (String) -> Unit,
    onPopSfx: suspend (letter: String, isCorrect: Boolean, finalCorrectBalloon: Boolean, balloonIndex: Int) -> Unit,
    onWrongPick: () -> Unit,
    onAllCorrectPopped: (correctLetter: String, poppedBalloonColor: Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    val forceSeason2Tuning =
        stationUiSpec.chapterId == Season2ChapterIds.Chapter1Tyrannosaurus &&
            stationUiSpec.stationId == Chapter1StationOrder.BALLOON_POP
    val compactLandscapePhoneTuning =
        forceSeason2Tuning || (isCompactLandscapePhone && stationUiSpec.popBalloonsCompactLandscapePhoneTuning)

    Column(
        modifier = modifier.fillMaxSize().padding(top = contentTopPaddingDp).scale(entryPulseScale),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (planPopAllLettersInWord) {
            Chapter3SagaPopBalloonsWordBanner(
                popAllLettersWord = popAllLettersWordForBanner,
                instructionText = popAllLettersBannerInstruction,
            )
        }

        if (planMode != StationQuizMode.PickLetter && !compactLandscapePhoneTuning) {
            PopBalloonsInstructionHeaderBlock(
                listenOnly = listenOnly,
                sagaUsesPopBalloonsAudioStaging = sagaUsesPopBalloonsAudioStaging,
                skipInstructionHeaderBlock = stationUiSpec.popBalloonsSkipInstructionHeaderBlock,
                balloonInstructionOverride = stationUiSpec.balloonInstructionOverride,
                useEpisode4BalloonInstructionPanel = stationUiSpec.useEpisode4BalloonInstructionPanel,
                showSagaStation2InstructionLine = stationUiSpec.popBalloonsShowSagaStation2InstructionLine,
                showTargetLetterChip = showPopBalloonsTargetLetterChip,
                episode4HelpSt15 = episode4HelpSt15,
                episode4HelpActiveHintLetter = episode4HelpActiveHintLetter,
                hintHeaderScale = hintHeaderScale,
                correctAnswer = current.correctAnswer,
                station2PinnedBalloonLetter = station2PinnedBalloonLetter,
                station2PinnedBalloonColor = station2PinnedBalloonColor,
                compactLandscapePhoneTuning = false,
            )
        }

        if (planMode == StationQuizMode.PickLetter) return

        if (compactLandscapePhoneTuning) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f, fill = true),
            ) {
                PopBalloonsOptions(
                    options = current.options,
                    correctAnswer = current.correctAnswer,
                    correctLetterSet = correctLetterSet,
                    enabled = enabled,
                    shakePx = shakePx,
                    visualRoundSeed = visualRoundSeed,
                    balloonSizeDp = 72.dp,
                    balloonLetterFontSize = 32.sp,
                    maxVisibleBalloonCount = 8,
                    compactLandscapeFreeFlight = true,
                    onBalloonPressed = onBalloonPressed,
                    onPopSfx = onPopSfx,
                    onWrongPick = onWrongPick,
                    onAllCorrectPopped = onAllCorrectPopped,
                    episode4CorrectBalloonHintEpoch = episode4CorrectBalloonHintEpoch,
                    helpSideInsetDp = helpSideInsetDp,
                )
                PopBalloonsInstructionHeaderBlock(
                    listenOnly = listenOnly,
                    sagaUsesPopBalloonsAudioStaging = sagaUsesPopBalloonsAudioStaging,
                    skipInstructionHeaderBlock = stationUiSpec.popBalloonsSkipInstructionHeaderBlock,
                    balloonInstructionOverride = stationUiSpec.balloonInstructionOverride,
                    useEpisode4BalloonInstructionPanel = stationUiSpec.useEpisode4BalloonInstructionPanel,
                    showSagaStation2InstructionLine = stationUiSpec.popBalloonsShowSagaStation2InstructionLine,
                    showTargetLetterChip = showPopBalloonsTargetLetterChip,
                    episode4HelpSt15 = episode4HelpSt15,
                    episode4HelpActiveHintLetter = episode4HelpActiveHintLetter,
                    hintHeaderScale = hintHeaderScale,
                    correctAnswer = current.correctAnswer,
                    station2PinnedBalloonLetter = station2PinnedBalloonLetter,
                    station2PinnedBalloonColor = station2PinnedBalloonColor,
                    compactLandscapePhoneTuning = true,
                    modifier = Modifier.align(Alignment.TopCenter).zIndex(2f),
                )
            }
        } else {
            PopBalloonsStationContent(
                question = current,
                correctLetterSet = correctLetterSet,
                enabled = enabled,
                shakePx = shakePx,
                visualRoundSeed = visualRoundSeed,
                maxVisibleBalloonCount = maxVisibleBalloonCount,
                compactLandscapePhoneTuning = false,
                episode4CorrectBalloonHintEpoch = episode4CorrectBalloonHintEpoch,
                helpSideInsetDp = helpSideInsetDp,
                onBalloonPressed = onBalloonPressed,
                onPopSfx = onPopSfx,
                onWrongPick = onWrongPick,
                onAllCorrectPopped = onAllCorrectPopped,
            )
        }
    }
}
