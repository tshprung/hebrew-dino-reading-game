package com.tal.hebrewdino.ui.components.station

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.tal.hebrewdino.ui.components.ChapterNavChipStyles
import com.tal.hebrewdino.ui.components.TargetLetterHeaderChip
import com.tal.hebrewdino.ui.domain.Chapter3EpisodeContent
import com.tal.hebrewdino.ui.domain.LessonWordIllustrations
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.screens.LetterOptions

/**
 * Pick-letter / [LetterOptions] station UI used from [com.tal.hebrewdino.ui.screens.GameScreen] when
 * [com.tal.hebrewdino.ui.domain.StationQuizMode.PickLetter] is active on a pop-balloons question round.
 * Caller owns session submission, audio, cooldowns, and advancement.
 */
@Composable
fun ColumnScope.PickLetterStationContent(
    question: Question.PopBalloonsQuestion,
    sessionRoundIndex: Int,
    useHighlightedLetterInWordRow: Boolean,
    highlightedInWordInstruction: String?,
    showListenOnlyHebrewPanel: Boolean,
    listenOnlyPanelInstruction: String?,
    repeatLetterButtonLabel: String?,
    sagaUsesPickLetterAudioStaging: Boolean,
    station1PinnedCorrectLetter: String?,
    pickLetterInstructionOverride: String?,
    pickLetterSagaStation1CompactPreamble: String?,
    showSagaStation1CompactPreamble: Boolean,
    pickLetterAllowPinnedCorrectShortcut: Boolean,
    boxTopPaddingDp: Dp,
    letterOptionsExtraTopPaddingDp: Dp,
    enabled: Boolean,
    shakePx: Float,
    correctPulseLetter: String?,
    correctPulseEpoch: Int,
    letterOptions: List<String>,
    strongLetterButtonFeedback: Boolean = false,
    onRepeatLetterClick: () -> Unit,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .then(
                    if (boxTopPaddingDp > 0.dp) {
                        Modifier.padding(top = boxTopPaddingDp)
                    } else {
                        Modifier
                    },
                ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (useHighlightedLetterInWordRow && highlightedInWordInstruction != null) {
                val spell = Chapter3EpisodeContent.pickSpellRound(sessionRoundIndex)
                val emoji = LessonWordIllustrations.emojiForWord(spell.word)
                Text(
                    text = highlightedInWordInstruction,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0B2B3D),
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .padding(start = 8.dp, end = 8.dp, bottom = 6.dp)
                            .background(Color.White.copy(alpha = 0.72f), RoundedCornerShape(18.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                )
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .background(
                                    color = Color(0xFFFFF59D).copy(alpha = 0.95f),
                                    shape = RoundedCornerShape(18.dp),
                                )
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                    ) {
                        Chapter3SpellWordRow(
                            word = spell.word,
                            highlightIndex = spell.slotIndex,
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = emoji, fontSize = 42.sp)
                }
            }
            if (showListenOnlyHebrewPanel && listenOnlyPanelInstruction != null) {
                Text(
                    text = listenOnlyPanelInstruction,
                    fontSize = 39.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0B2B3D),
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .background(Color.White.copy(alpha = 0.72f), RoundedCornerShape(18.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onRepeatLetterClick,
                    colors = ChapterNavChipStyles.outlinedButtonColors(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    Text(
                        repeatLetterButtonLabel ?: "",
                        style = ChapterNavChipStyles.labelTextStyle().copy(fontSize = 32.sp),
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else if (pickLetterInstructionOverride != null) {
                Text(
                    text = pickLetterInstructionOverride,
                    fontSize = 39.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0B2B3D),
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .background(Color.White.copy(alpha = 0.72f), RoundedCornerShape(18.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                )
                Spacer(modifier = Modifier.height(18.dp))
            } else if (showSagaStation1CompactPreamble && pickLetterSagaStation1CompactPreamble != null) {
                Text(
                    text = pickLetterSagaStation1CompactPreamble,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0B2B3D),
                    textAlign = TextAlign.Center,
                )
                TargetLetterHeaderChip(
                    letter = question.correctAnswer,
                    modifier = Modifier.padding(top = 10.dp),
                )
                Spacer(modifier = Modifier.height(18.dp))
            } else if (!useHighlightedLetterInWordRow) {
                TargetLetterHeaderChip(
                    letter = question.correctAnswer,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
            LetterOptions(
                options = letterOptions,
                enabled = enabled,
                shakePx = shakePx,
                correctPulseLetter = correctPulseLetter,
                correctPulseEpoch = correctPulseEpoch,
                strongPressFeedback = strongLetterButtonFeedback,
                onPick = onPick,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true)
                        .then(
                            if (letterOptionsExtraTopPaddingDp > 0.dp) {
                                Modifier.padding(top = letterOptionsExtraTopPaddingDp)
                            } else {
                                Modifier
                            },
                        ),
            )
        }
    }
}

@Composable
private fun Chapter3SpellWordRow(
    word: String,
    highlightIndex: Int,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Row(
            modifier = modifier.padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            word.forEachIndexed { i, ch ->
                Text(
                    text = ch.toString(),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black,
                    color = if (i == highlightIndex) Color(0xFFE65100) else Color(0xFF0B2B3D),
                    modifier = Modifier.padding(horizontal = 3.dp),
                )
            }
        }
    }
}
