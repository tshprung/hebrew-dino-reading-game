package com.tal.hebrewdino.ui.components.station

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.ui.components.TargetLetterHeaderChip
import com.tal.hebrewdino.ui.domain.LessonWordIllustrations
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.screens.PopBalloonsOptions
import com.tal.hebrewdino.ui.screens.Station2PinnedBalloonMini

/**
 * Chapter 3 saga pop-balloons word + emoji banner (same layout as [com.tal.hebrewdino.ui.screens.GameScreen]).
 */
@Composable
fun Chapter3SagaPopBalloonsWordBanner(
    popAllLettersWord: String,
    modifier: Modifier = Modifier,
) {
    val emoji = LessonWordIllustrations.emojiForWord(popAllLettersWord)
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "פוצץ את כל הבלונים עם אותיות המופיעות במילה:",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF0B2B3D),
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .background(Color.White.copy(alpha = 0.72f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .background(
                            color = Color(0xFFFFF59D).copy(alpha = 0.95f),
                            shape = RoundedCornerShape(16.dp),
                        )
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = popAllLettersWord,
                    fontSize = 41.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0B2B3D),
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = emoji, fontSize = 36.sp)
        }
    }
}

/**
 * Instruction / header UI above the balloon playfield when not in pick-letter mode.
 */
@Composable
fun PopBalloonsInstructionHeaderBlock(
    chapterId: Int,
    stationId: Int,
    listenOnly: Boolean,
    sagaUsesPopBalloonsAudioStaging: Boolean,
    isSagaEpisode: Boolean,
    balloonInstructionOverride: String?,
    useEpisode4BalloonInstructionPanel: Boolean,
    episode4HelpSt15: Boolean,
    episode4HelpActiveHintLetter: String?,
    hintHeaderScale: Float,
    correctAnswer: String,
    station2PinnedBalloonLetter: String?,
    station2PinnedBalloonColor: Color?,
    modifier: Modifier = Modifier,
) {
    // Same as GameScreen’s outer balloon Column: center children in full width (RTL-safe).
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (!(chapterId == 3 && sagaUsesPopBalloonsAudioStaging)) {
            if (useEpisode4BalloonInstructionPanel && sagaUsesPopBalloonsAudioStaging) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = balloonInstructionOverride ?: "פוצץ את הבלונים עם האות:",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0B2B3D),
                        textAlign = TextAlign.Center,
                        modifier =
                            Modifier
                                .background(Color.White.copy(alpha = 0.72f), RoundedCornerShape(18.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                    )
                    if (episode4HelpSt15 && episode4HelpActiveHintLetter != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        TargetLetterHeaderChip(
                            letter = episode4HelpActiveHintLetter!!,
                            modifier = Modifier.scale(hintHeaderScale),
                        )
                    }
                }
            } else if (sagaUsesPopBalloonsAudioStaging && station2PinnedBalloonLetter != null) {
                Text(
                    text =
                        balloonInstructionOverride
                            ?: if (listenOnly) {
                                "פוצץ את הבלונים של האות שנשמעה:"
                            } else {
                                "פוצץ את הבלונים עם האות:"
                            },
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0B2B3D),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 10.dp),
                )
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (!listenOnly) {
                        TargetLetterHeaderChip(
                            letter = correctAnswer,
                            modifier = Modifier.scale(hintHeaderScale),
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Station2PinnedBalloonMini(
                        letter = station2PinnedBalloonLetter!!,
                        balloonColor = station2PinnedBalloonColor ?: Color(0xFF6BCB77),
                        showLetter = !listenOnly,
                        modifier = Modifier.scale(hintHeaderScale),
                    )
                }
            } else {
                if (isSagaEpisode && stationId == 2 && chapterId != 4) {
                    Text(
                        text =
                            balloonInstructionOverride
                                ?: if (listenOnly) {
                                    "פוצץ את הבלונים של האות שנשמעה:"
                                } else {
                                    "פוצץ את הבלונים עם האות:"
                                },
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0B2B3D),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp, bottom = 10.dp),
                    )
                }
                if (!listenOnly) {
                    TargetLetterHeaderChip(
                        letter = correctAnswer,
                        modifier =
                            Modifier
                                .padding(top = 4.dp)
                                .scale(hintHeaderScale),
                    )
                }
            }
        }
    }
}

/**
 * Balloon playfield UI ([PopBalloonsOptions]) for [com.tal.hebrewdino.ui.screens.GameScreen].
 * Caller owns session, SoundPool/VoicePlayer jobs, cooldowns, and advancement.
 */
@Composable
fun ColumnScope.PopBalloonsStationContent(
    question: Question.PopBalloonsQuestion,
    correctLetterSet: Set<String>?,
    enabled: Boolean,
    shakePx: Float,
    visualRoundSeed: Int,
    episode4CorrectBalloonHintEpoch: Int,
    helpSideInsetDp: Dp,
    onBalloonPressed: (String) -> Unit,
    onPopSfx: suspend (letter: String, isCorrect: Boolean, finalCorrectBalloon: Boolean, balloonIndex: Int) -> Unit,
    onWrongPick: () -> Unit,
    onAllCorrectPopped: (correctLetter: String, poppedBalloonColor: Color) -> Unit,
) {
    Spacer(modifier = Modifier.height(8.dp))
    Box(
        modifier = Modifier.fillMaxWidth().weight(1f),
        contentAlignment = Alignment.Center,
    ) {
        PopBalloonsOptions(
            options = question.options,
            correctAnswer = question.correctAnswer,
            correctLetterSet = correctLetterSet,
            enabled = enabled,
            shakePx = shakePx,
            visualRoundSeed = visualRoundSeed,
            onBalloonPressed = onBalloonPressed,
            onPopSfx = onPopSfx,
            onWrongPick = onWrongPick,
            onAllCorrectPopped = onAllCorrectPopped,
            episode4CorrectBalloonHintEpoch = episode4CorrectBalloonHintEpoch,
            helpSideInsetDp = helpSideInsetDp,
        )
    }
}
