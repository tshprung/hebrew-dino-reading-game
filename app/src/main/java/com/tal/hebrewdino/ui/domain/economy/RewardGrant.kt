package com.tal.hebrewdino.ui.domain.economy

/** Configurable side-effects applied when a round or level completes. */
sealed interface RewardGrantRequest {
    val applesReward: Int
    val resetHunger: Boolean
    val emitPresentation: Boolean
    val visualCue: ParticleCue?
    val markChapterProgress: ChapterStationProgress?
}

data class ChapterStationProgress(
    val chapterIndex: Int,
    val stationId: Int,
)

/** Syllabus loop stations (Word Challenge / Falling Letters). */
data class StationRoundCompleted(
    val chapterIndex: Int,
    val stationId: Int,
    override val applesReward: Int = DEFAULT_APPLES,
    override val resetHunger: Boolean = true,
    override val emitPresentation: Boolean = true,
    override val visualCue: ParticleCue? = ParticleCue.CONFETTI_BURST,
    override val markChapterProgress: ChapterStationProgress? =
        if (stationId > 0) {
            ChapterStationProgress(chapterIndex, stationId)
        } else {
            null
        },
) : RewardGrantRequest

/** Legacy saga levels (GameScreen) — apples only, no hunger reset by default. */
data class LegacyLevelFood(
    override val applesReward: Int,
    override val resetHunger: Boolean = false,
    override val emitPresentation: Boolean = true,
    override val visualCue: ParticleCue? = ParticleCue.LEVEL_COMPLETE_SPARKLE,
    override val markChapterProgress: ChapterStationProgress? = null,
) : RewardGrantRequest

private const val DEFAULT_APPLES: Int = 3

fun fanfareTextForApples(count: Int): String =
    when (count) {
        3 -> "הצלחתם! קיבלתם שלושה תפוחים"
        1 -> "הצלחתם! קיבלתם תפוח אחד"
        2 -> "הצלחתם! קיבלתם שני תפוחים"
        else -> "הצלחתם! קיבלתם $count תפוחים"
    }

fun fanfareDisplayTextForApples(count: Int): String =
    when (count) {
        3 -> "הצלחתם! קיבלתם שלושה תפוחים! 🍎"
        1 -> "הצלחתם! קיבלתם תפוח אחד! 🍎"
        2 -> "הצלחתם! קיבלתם שני תפוחים! 🍎"
        else -> "הצלחתם! קיבלתם $count תפוחים! 🍎"
    }
