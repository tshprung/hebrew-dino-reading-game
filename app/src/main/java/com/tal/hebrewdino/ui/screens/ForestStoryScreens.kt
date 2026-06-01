package com.tal.hebrewdino.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.companion.Chapter1AddressAwareAudio
import com.tal.hebrewdino.ui.companion.Chapter1CompanionCopy
import com.tal.hebrewdino.ui.companion.displayNameHebrew
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.data.PlayerAddress

@Composable
fun ForestIntroScreen(
    character: DinoCharacter,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.forest_bg_story_intro,
        dinoContentDescription = character.displayNameHebrew(),
        title = "פרק 1 - מצא את הביצה",
        body = Chapter1CompanionCopy.forestIntroBody(character),
        chapterId = 1,
        storyContext = "ForestIntroScreen",
        voiceRawResId = Chapter1AddressAwareAudio.storyIntroRawRes(character),
        companion = ChapterLobbyCompanion.DinoAndMom,
        useCompanionDinoArt = true,
        companionCharacter = character,
        useCompanionMomArt = true,
        useWarmReadableStoryPanel = true,
        showMotherLostEggsCue = true,
        onContinue = onContinue,
        modifier = modifier,
    )
}

@Composable
fun ForestOutroScreen(
    character: DinoCharacter,
    playerAddress: PlayerAddress,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChapterLobbyStoryLayout(
        backgroundRes = R.drawable.forest_bg_story_outro_egg,
        dinoContentDescription = character.displayNameHebrew(),
        title = "יש!",
        body = Chapter1CompanionCopy.finaleBody(character, playerAddress),
        chapterId = 1,
        storyContext = "ForestOutroScreen",
        voiceRawResId = Chapter1AddressAwareAudio.storyOutroRawRes(character),
        companion = ChapterLobbyCompanion.DinoOnly,
        useCompanionDinoArt = true,
        companionCharacter = character,
        onContinue = onContinue,
        modifier = modifier,
    )
}
