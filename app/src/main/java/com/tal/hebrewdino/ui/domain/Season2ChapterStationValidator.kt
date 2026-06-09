package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.R

/**
 * Full chapter readiness: poster, words, letters, all six stations, no T-Rex poster reuse.
 */
object Season2ChapterStationValidator {
    data class ChapterReadiness(
        val chapterIndex: Int,
        val qaReady: Boolean,
        val lockReason: Season2ChapterLockReason,
        val issues: List<String>,
    )

    fun evaluate(
        chapterIndex: Int,
        posterResId: Int?,
        letters: List<String>,
        wordCatalogIds: List<String>,
        memoryMatchLetters: List<String>,
        letterPoolSpec: LetterPoolSpec,
        stationContext: Season2ChapterStationPlans.ChapterStationContext?,
    ): ChapterReadiness {
        val issues = mutableListOf<String>()

        if (posterResId == null) {
            issues.add("poster_puzzle_full.png (chapter-specific)")
        } else if (chapterIndex != 1 && posterResId == trexPosterResId()) {
            issues.add("unique poster (must not reuse season2_trex_puzzle_full)")
        }

        val assetValidation =
            Season2ChapterAssetValidator.validate(
                posterResId = posterResId,
                letters = letters,
                wordCatalogIds = wordCatalogIds,
                forbidTrexPoster = chapterIndex != 1,
            )
        issues.addAll(assetValidation.missingAssets)

        if (chapterIndex >= 3) {
            if (letterPoolSpec !is Season2ChapterLetterPool) {
                issues.add("chapter $chapterIndex needs a Season2ChapterLetterPool")
            } else if (letterPoolSpec.chapterIndex != chapterIndex) {
                issues.add("letter pool chapter index mismatch")
            }
            if (stationContext != null) {
                issues.addAll(Season2ChapterStationPlans.validateAllStations(stationContext))
            } else {
                issues.add("missing station context for chapter $chapterIndex")
            }
        }

        if (memoryMatchLetters.isEmpty()) {
            issues.add("memory match letters empty")
        }

        val distinct = issues.distinct()
        val qaReady = distinct.isEmpty() && posterResId != null
        val lockReason =
            when {
                qaReady -> Season2ChapterLockReason.QaReady
                assetValidation.missingAssets.isNotEmpty() -> Season2ChapterLockReason.MissingAssets
                else -> Season2ChapterLockReason.NotImplemented
            }
        return ChapterReadiness(
            chapterIndex = chapterIndex,
            qaReady = qaReady,
            lockReason = lockReason,
            issues = distinct,
        )
    }

    private fun trexPosterResId(): Int = R.drawable.season2_trex_puzzle_full
}
