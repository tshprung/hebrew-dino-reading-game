package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.ui.screens.ChaptersProgress

/**
 * Parent-controlled waivers: a waived chapter counts as satisfied for unlocking the next chapter,
 * without requiring the child to replay or fully complete it.
 */
object ChapterUnlockWaiverPolicy {
    val season1ChapterRange: IntRange = 1..6

    fun parseWaivers(raw: String, validRange: IntRange = season1ChapterRange): Set<Int> {
        if (raw.isBlank()) return emptySet()
        return raw.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .filter { it in validRange }
            .toSet()
    }

    fun serializeWaivers(ids: Set<Int>, validRange: IntRange = season1ChapterRange): String =
        ids.filter { it in validRange }.sorted().joinToString(",")

    data class Season1Snapshot(
        val beachOutroSeen: Boolean,
        val chapter1AllStationsComplete: Boolean,
        val chapter2Completed: Boolean,
        val chapter3Completed: Boolean,
        val chapter4Completed: Boolean,
        val chapter5Completed: Boolean,
        val chapter6Completed: Boolean,
    )

    fun isChapterSatisfied(
        chapterId: Int,
        snapshot: Season1Snapshot,
        waivers: Set<Int>,
    ): Boolean {
        if (chapterId in waivers) return true
        return when (chapterId) {
            1 -> snapshot.beachOutroSeen || snapshot.chapter1AllStationsComplete
            2 -> snapshot.chapter2Completed
            3 -> snapshot.chapter3Completed
            4 -> snapshot.chapter4Completed
            5 -> snapshot.chapter5Completed
            6 -> snapshot.chapter6Completed
            else -> false
        }
    }

    fun unlockedChapter(snapshot: Season1Snapshot, waivers: Set<Int>): Int =
        when {
            isChapterSatisfied(6, snapshot, waivers) -> 6
            isChapterSatisfied(5, snapshot, waivers) -> 6
            isChapterSatisfied(4, snapshot, waivers) -> 5
            isChapterSatisfied(3, snapshot, waivers) -> 4
            isChapterSatisfied(2, snapshot, waivers) -> 3
            isChapterSatisfied(1, snapshot, waivers) -> 2
            else -> 1
        }

    fun canOpenChapter(
        chapterId: Int,
        snapshot: Season1Snapshot,
        waivers: Set<Int>,
    ): Boolean =
        when (chapterId) {
            1 -> true
            2 -> isChapterSatisfied(1, snapshot, waivers)
            3 -> isChapterSatisfied(2, snapshot, waivers)
            4 -> isChapterSatisfied(3, snapshot, waivers)
            5 -> isChapterSatisfied(4, snapshot, waivers)
            6 -> isChapterSatisfied(5, snapshot, waivers)
            7 -> isChapterSatisfied(6, snapshot, waivers)
            else -> false
        }

    fun effectiveChaptersProgress(
        snapshot: Season1Snapshot,
        waivers: Set<Int>,
    ): ChaptersProgress =
        ChaptersProgress(
            chapter1Completed = isChapterSatisfied(1, snapshot, waivers),
            chapter2Completed = isChapterSatisfied(2, snapshot, waivers),
            chapter3Completed = isChapterSatisfied(3, snapshot, waivers),
            chapter4Completed = isChapterSatisfied(4, snapshot, waivers),
            chapter5Completed = isChapterSatisfied(5, snapshot, waivers),
            chapter6Completed = isChapterSatisfied(6, snapshot, waivers),
        )

    fun maxSelectableChapterId(snapshot: Season1Snapshot, waivers: Set<Int>): Int =
        when {
            isChapterSatisfied(5, snapshot, waivers) -> 6
            isChapterSatisfied(4, snapshot, waivers) -> 5
            isChapterSatisfied(3, snapshot, waivers) -> 4
            else -> 3
        }

    fun isSeason2ChapterUnlocked(
        chapterIndex: Int,
        completedChapters: Set<Int>,
        waivers: Set<Int>,
    ): Boolean {
        if (!Season2ChapterRegistry.isPlayable(chapterIndex)) return false
        if (chapterIndex == 1) return true
        return (chapterIndex - 1) in completedChapters || (chapterIndex - 1) in waivers
    }
}
