package com.tal.hebrewdino.ui.domain

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Approved warmup-arc (St1–3) QA rules for S2 chapters 3–6, applied by station kind. */
object Season2WarmupStationQaPolicy {
  const val PopBalloonsQuestionCount: Int = 5
  const val PickLetterQuestionCount: Int = 6
  const val PictureStartsWithQuestionCount: Int = 6

  /** Gap between balloon letter name and in-station praise (halved per Ch1-St1 QA). */
  const val BalloonLetterToPraiseGapMs: Long = 45L

  /** Physical left nudge ≈0.5cm (matches [com.tal.hebrewdino.ui.screens.SixStationArcHalfCmNudge]). */
  val HalfCmPhysicalLeftDp: Dp = 19.dp

  fun usesRawLetterNameStationFeedback(gameplayChapterId: Int): Boolean =
    Season2StationAudio.isSeason2GameplayChapter(gameplayChapterId) ||
      gameplayChapterId == 1 ||
      gameplayChapterId == 2 ||
      gameplayChapterId == 4 ||
      gameplayChapterId == 5 ||
      gameplayChapterId == TrainingV1Config.CHAPTER_ID

  fun shouldPlayBalloonProgressPraise(
    season2QuizBalloons: Boolean,
    finalCorrectBalloon: Boolean,
    afterCoachIntervention: Boolean,
  ): Boolean =
    season2QuizBalloons &&
      !finalCorrectBalloon &&
      !afterCoachIntervention

  fun shouldPlayBalloonCompanionPraiseAfterCoach(
    season2QuizBalloons: Boolean,
    isCorrect: Boolean,
    afterCoachIntervention: Boolean,
  ): Boolean = season2QuizBalloons && isCorrect && afterCoachIntervention

  fun shouldPlayBalloonFinalRoundPraise(
    season2QuizBalloons: Boolean,
    finalCorrectBalloon: Boolean,
    afterCoachIntervention: Boolean,
  ): Boolean = season2QuizBalloons && finalCorrectBalloon && !afterCoachIntervention
}
