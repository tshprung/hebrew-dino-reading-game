package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.Season2CompanionFeedbackAudio
import com.tal.hebrewdino.ui.audio.Season2RawAudio
import com.tal.hebrewdino.ui.data.DinoCharacter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class Season2PostFocusCorrectBatchTest {
  @Test
  fun postFocus_pool_usesSharedSuccessClips_notRewardMapOrNarrator() {
    val dino = Season2RawAudio.postFocusCorrectPool(DinoCharacter.Dino).toSet()
    val dina = Season2RawAudio.postFocusCorrectPool(DinoCharacter.Dina).toSet()
    assertEquals(dino, dina)
    assertEquals(
        setOf(R.raw.season2_success_01, R.raw.season2_success_02, R.raw.season2_success_03),
        dino,
    )
    assertFalse(R.raw.reward_dino_01 in dino)
    assertFalse(R.raw.reward_dina_01 in dina)
    assertFalse(R.raw.season2_map_praise_dino_01 in dino)
    assertFalse(R.raw.praise_short_01 in dino)
  }

  @Test
  fun postFocus_pick_avoidsImmediateRepeat() {
    val first =
        Season2CompanionFeedbackAudio.pickPostFocusCorrectPraise(
            DinoCharacter.Dino,
            avoidRawResId = 0,
            random = Random(2),
        )
    val second =
        Season2CompanionFeedbackAudio.pickPostFocusCorrectPraise(
            DinoCharacter.Dino,
            avoidRawResId = first,
            random = Random(2),
        )
    assertNotEquals(first, second)
  }

  @Test
  fun policy_playsOnlyAfterCoachIntervention() {
    assertTrue(
        Season2PostFocusCorrectPolicy.shouldPlayCompanionPraiseOnCorrect(
            season2HadCoachIntervention = true,
        ),
    )
    assertFalse(
        Season2PostFocusCorrectPolicy.shouldPlayCompanionPraiseOnCorrect(
            season2HadCoachIntervention = false,
        ),
    )
  }

  @Test
  fun gameScreen_usesCompanionPostFocusAudio_notTextBubbleOrNarrator() {
    val gameScreen = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/GameScreen.kt")
    assertTrue(gameScreen.contains("Season2PostFocusCorrectAudio.playBlocking"))
    assertFalse(gameScreen.contains("processPraise(chapter1PlayerAddress)"))
    val pop = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/PopBalloonsActions.kt")
    assertTrue(pop.contains("Season2PostFocusCorrectAudio.playBlocking"))
    assertTrue(pop.contains("afterCoachIntervention"))
  }

  @Test
  fun representativeStations_skipGenericPraiseAfterCoach() {
    assertTrue(
        Season2EarlyStationQaPolicy.shouldSkipInStationCorrectPraiseAfterCoach(
            season2HadCoachIntervention = true,
        ),
    )
    assertTrue(
        Season2PostFocusCorrectPolicy.shouldSuppressAdvanceRoundNarratorPraise(
            playedPostFocusCompanionPraise = true,
        ),
    )
    val pickLetter = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/PickLetterActions.kt")
    val picture = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/PictureStartsWithActions.kt")
    val image = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/ImageMatchActions.kt")
    val wordParts = readProjectSource("app/src/main/java/com/tal/hebrewdino/ui/screens/Season2AdvancedStationActions.kt")
    assertTrue(pickLetter.contains("shouldSkipInStationCorrectPraiseAfterCoach"))
    assertTrue(picture.contains("shouldSkipInStationCorrectPraiseAfterCoach"))
    assertTrue(image.contains("shouldSkipInStationCorrectPraiseAfterCoach"))
    assertTrue(wordParts.contains("shouldSkipInStationCorrectPraiseAfterCoach"))
  }

  @Test
  fun season2SuccessClips_existOnDiskAndResolve() {
    val rawDir = locateRawDir()
    listOf(
        "season2_success_01.mp3",
        "season2_success_02.mp3",
        "season2_success_03.mp3",
    ).forEach { assertTrue(java.io.File(rawDir, it).exists()) }
    assertEquals(R.raw.season2_success_01, Season2RawAudio.Success01)
    assertEquals(R.raw.season2_success_02, Season2RawAudio.Success02)
    assertEquals(R.raw.season2_success_03, Season2RawAudio.Success03)
  }

  @Test
  fun season1_pickLetterPlan_unchanged() {
    assertEquals(
      com.tal.hebrewdino.ui.domain.StationQuizMode.PickLetter,
      com.tal.hebrewdino.ui.domain.StationQuizPlans.chapter1(
        com.tal.hebrewdino.ui.domain.Chapter1StationOrder.TAP_LETTER,
      ).mode,
    )
  }

  private fun locateRawDir(): java.io.File {
    val candidates =
        listOf(
            java.io.File("app/src/main/res/raw"),
            java.io.File("../app/src/main/res/raw"),
            java.io.File("../../app/src/main/res/raw"),
        )
    return candidates.first { it.isDirectory }
  }

  private fun readProjectSource(relativePath: String): String {
    val candidates =
        listOf(
            java.io.File(relativePath),
            java.io.File("../$relativePath"),
            java.io.File("../../$relativePath"),
        )
    val file =
        candidates.firstOrNull { it.exists() }
            ?: error("Could not locate source file: $relativePath")
    return file.readText()
  }
}
