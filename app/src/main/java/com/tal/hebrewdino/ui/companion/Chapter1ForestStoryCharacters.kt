package com.tal.hebrewdino.ui.companion

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tal.hebrewdino.ui.components.AnimatedTalkingCharacter
import com.tal.hebrewdino.ui.layout.ScreenFit

/**
 * Ch.1 forest intro: mother (large parent) + smaller Dino + three eggs at mother's feet.
 */
@Composable
fun Chapter1ForestStoryCharacters(
    dinoIdleRes: Int,
    dinoTalkResIds: List<Int>,
    dinoTalking: Boolean,
    dinoContentDescription: String,
    momIdleRes: Int,
    momTalkResIds: List<Int>,
    momTalking: Boolean,
    useCompanionMomArt: Boolean,
    modifier: Modifier = Modifier,
) {
    val isCompact = ScreenFit.isCompactLandscapePhone()
    val momSize = if (isCompact) 148.dp else 142.dp
    val dinoSize = if (isCompact) 72.dp else 76.dp
    Chapter1ForestStoryCharactersLayout(
        momSize = momSize,
        dinoSize = dinoSize,
        dinoIdleRes = dinoIdleRes,
        dinoTalkResIds = dinoTalkResIds,
        dinoTalking = dinoTalking,
        dinoContentDescription = dinoContentDescription,
        momIdleRes = momIdleRes,
        momTalkResIds = momTalkResIds,
        momTalking = momTalking,
        useCompanionMomArt = useCompanionMomArt,
        modifier = modifier,
    )
}

@Composable
private fun Chapter1ForestStoryCharactersLayout(
    momSize: Dp,
    dinoSize: Dp,
    dinoIdleRes: Int,
    dinoTalkResIds: List<Int>,
    dinoTalking: Boolean,
    dinoContentDescription: String,
    momIdleRes: Int,
    momTalkResIds: List<Int>,
    momTalking: Boolean,
    useCompanionMomArt: Boolean,
    modifier: Modifier = Modifier,
) {
    val dinoMomGap = if (ScreenFit.isCompactLandscapePhone()) 10.dp else 14.dp
    Row(
        modifier = modifier.wrapContentWidth(),
        verticalAlignment = Alignment.Bottom,
    ) {
        CompanionDinoPortrait(
            poseRes = dinoIdleRes,
            talkFrameResIds = dinoTalkResIds,
            isTalking = dinoTalking,
            modifier =
                Modifier
                    .size(dinoSize)
                    .offset(y = 6.dp),
            contentDescription = dinoContentDescription,
        )
        Spacer(modifier = Modifier.width(dinoMomGap))
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier.padding(bottom = if (ScreenFit.isCompactLandscapePhone()) 22.dp else 26.dp),
        ) {
            MomStoryCharacter(
                momIdleRes = momIdleRes,
                momTalkResIds = momTalkResIds,
                momTalking = momTalking,
                useCompanionMomArt = useCompanionMomArt,
                size = momSize,
            )
            MotherLostEggsCue(
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 14.dp),
            )
        }
    }
}

@Composable
private fun MomStoryCharacter(
    momIdleRes: Int,
    momTalkResIds: List<Int>,
    momTalking: Boolean,
    useCompanionMomArt: Boolean,
    size: Dp,
) {
    if (useCompanionMomArt) {
        CompanionGentleIdleMotion(active = momTalking) {
            AnimatedTalkingCharacter(
                idleRes = momIdleRes,
                talkFrameResIds = momTalkResIds,
                isTalking = false,
                modifier = Modifier.size(size),
                contentDescription = "אמא דינוזאור",
            )
        }
    } else {
        AnimatedTalkingCharacter(
            idleRes = momIdleRes,
            talkFrameResIds = momTalkResIds,
            isTalking = momTalking,
            modifier = Modifier.size(size),
            contentDescription = "אמא דינוזאור",
        )
    }
}
