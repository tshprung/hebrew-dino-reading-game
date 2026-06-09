package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.tal.hebrewdino.ui.companion.CompanionAssets
import com.tal.hebrewdino.ui.companion.CompanionDinoPortrait
import com.tal.hebrewdino.ui.companion.displayNameHebrew
import com.tal.hebrewdino.ui.data.DinoCharacter

private val BubbleFill = Color(0xFFFFF8E8)
private val BubbleText = Color(0xFF4A3A2E)
private val BubbleOutline = Color(0xFFD8C8B0)

/**
 * Compact companion speech hint — companion anchored on the physical right (use inside LTR box).
 * Bubble sits to the left of the companion with a tail pointing toward them.
 */
@Composable
fun Season2CompanionSpeechHint(
    text: String,
    companionCharacter: DinoCharacter,
    isTalking: Boolean,
    modifier: Modifier = Modifier,
    companionSizeDp: Dp = 76.dp,
    showCompanionPortrait: Boolean = true,
) {
    val assets = remember(companionCharacter) { CompanionAssets.forCharacter(companionCharacter) }
    val pose = if (isTalking) assets.poseEncourage else assets.poseIdle

    val bubbleLift =
        if (showCompanionPortrait) {
            companionSizeDp * 0.42f
        } else {
            companionSizeDp * 0.78f
        }

    Row(
        modifier = modifier.zIndex(22f),
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(
            modifier =
                Modifier
                    .padding(
                        end = if (showCompanionPortrait) 4.dp else 6.dp,
                        bottom = bubbleLift,
                    ),
            horizontalAlignment = Alignment.End,
        ) {
            Surface(
                modifier = Modifier.widthIn(max = 158.dp),
                shape = RoundedCornerShape(12.dp),
                color = BubbleFill,
                shadowElevation = 3.dp,
                border = BorderStroke(1.dp, BubbleOutline.copy(alpha = 0.6f)),
            ) {
                Text(
                    text = text,
                    modifier =
                        Modifier
                            .background(BubbleFill)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                    style =
                        MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            lineHeight = 15.sp,
                        ),
                    color = BubbleText,
                    textAlign = TextAlign.End,
                    maxLines = 2,
                )
            }
            SpeechBubbleTail(
                modifier =
                    Modifier
                        .align(Alignment.End)
                        .padding(end = if (showCompanionPortrait) 10.dp else 14.dp),
            )
        }
        if (showCompanionPortrait) {
            CompanionDinoPortrait(
                poseRes = pose,
                talkFrameResIds = assets.talkFrameResIds,
                isTalking = isTalking,
                contentDescription = companionCharacter.displayNameHebrew(),
                modifier = Modifier.size(companionSizeDp),
            )
        }
    }
}

@Composable
private fun SpeechBubbleTail(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(width = 14.dp, height = 9.dp)) {
        val path =
            Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width, size.height)
                close()
            }
        drawPath(path, BubbleFill)
        drawPath(path, BubbleOutline.copy(alpha = 0.45f), style = Stroke(1f))
    }
}
