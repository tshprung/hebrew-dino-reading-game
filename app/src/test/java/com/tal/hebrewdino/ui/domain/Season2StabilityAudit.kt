package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.Season2RawAudio
import com.tal.hebrewdino.ui.data.DinoCharacter

/** Pure Season 2 stability audit helpers (unit-test reporting only). */
object Season2StabilityAudit {
    const val MAP_PRAISE_CAPTION_SOURCE: String = "INDEX_MAPPED_NOT_TRANSCRIPT_VERIFIED"

    data class StationExpectation(
        val chapterIndex: Int,
        val stationId: Int,
        val kind: Season2ChapterStationPlans.StationKind,
        val wordPartsMode: Season2WordPartsPresentationMode? = null,
        val advancedMode: Season2AdvancedStationMode? = null,
    )

    val intendedFinaleRouting: List<StationExpectation> =
        listOf(
            StationExpectation(1, 6, Season2ChapterStationPlans.StationKind.DragMissingLetter),
            StationExpectation(
                2,
                6,
                Season2ChapterStationPlans.StationKind.WordParts,
                wordPartsMode = Season2WordPartsPresentationMode.VisibleWordParts,
                advancedMode = Season2AdvancedStationMode.WordParts,
            ),
            StationExpectation(
                3,
                5,
                Season2ChapterStationPlans.StationKind.WordParts,
                wordPartsMode = Season2WordPartsPresentationMode.GuidedWordParts,
                advancedMode = Season2AdvancedStationMode.WordParts,
            ),
            StationExpectation(
                3,
                6,
                Season2ChapterStationPlans.StationKind.WordParts,
                wordPartsMode = Season2WordPartsPresentationMode.HiddenWordPartsChallenge,
                advancedMode = Season2AdvancedStationMode.WordParts,
            ),
            StationExpectation(4, 6, Season2ChapterStationPlans.StationKind.MatchLetterToWord),
            StationExpectation(
                5,
                6,
                Season2ChapterStationPlans.StationKind.WordParts,
                wordPartsMode = Season2WordPartsPresentationMode.GuidedWordParts,
                advancedMode = Season2AdvancedStationMode.WordParts,
            ),
            StationExpectation(6, 4, Season2ChapterStationPlans.StationKind.Rhyming, advancedMode = Season2AdvancedStationMode.Rhyming),
            StationExpectation(6, 6, Season2ChapterStationPlans.StationKind.MatchLetterToWord),
        )

    fun legacyUnusedMapRawResIds(): List<Int> =
        listOf(Season2RawAudio.MapPartRevealed, Season2RawAudio.MapAlmostDone)

    fun wordPartCatalogIdsWithRawChunks(): Set<String> =
        Season2WordPartsCatalog.curatedEntries
            .map { it.catalogId }
            .filter { Season2WordPartsCatalog.hasCompleteWordPartsAudio(it) }
            .toSet()

    fun curatedEntriesMissingWordPartChunks(): List<String> =
        Season2WordPartsCatalog.curatedEntries
            .map { it.catalogId }
            .filter { !Season2WordPartsCatalog.hasCompleteWordPartsAudio(it) }

    fun questionCountFor(chapterIndex: Int, stationId: Int): Int? {
        if (chapterIndex in 1..2) {
            return Season2Chapter1StationOrder.quizPlan(chapterIndex, stationId).questionCount
        }
        val ctx = Season2ChapterStationPlans.contextFor(chapterIndex) ?: return null
        if (
            chapterIndex >= 3 &&
                Season2ChapterStationPlans.stationKind(chapterIndex, stationId) ==
                Season2ChapterStationPlans.StationKind.MemoryMatch
        ) {
            return null
        }
        if (chapterIndex == 2 && stationId == Season2Chapter1StationOrder.MEMORY_MATCH) return null
        return Season2ChapterStationPlans.quizPlan(ctx, stationId).questionCount
    }

    fun stationsBelowSixRounds(): Map<String, Int> {
        val out = linkedMapOf<String, Int>()
        for (chapter in 1..Season2ChapterRegistry.CHAPTER_COUNT) {
            for (station in 1..6) {
                if (
                    chapter == 2 && station == Season2Chapter1StationOrder.MEMORY_MATCH ||
                        (
                            chapter >= 3 &&
                                Season2ChapterStationPlans.stationKind(chapter, station) ==
                                Season2ChapterStationPlans.StationKind.MemoryMatch
                        )
                ) {
                    continue
                }
                val count = questionCountFor(chapter, station) ?: continue
                if (count < 6) {
                    out["Ch$chapter-St$station"] = count
                }
            }
        }
        return out
    }

    fun lettersPerChapter(): Map<Int, List<String>> =
        (1..Season2ChapterRegistry.CHAPTER_COUNT).mapNotNull { index ->
            Season2ChapterRegistry.chapter(index)?.let { index to it.letters }
        }.toMap()

    fun ch3WordPartsPools(): Pair<Set<String>, Set<String>> {
        val scope =
            Season2WordPartsCatalog.wordCatalogIdsForChapter3WordParts(Season2ChapterContent.ch3Words)
        val guided =
            Season2WordPartsCatalog.entriesForPresentationMode(
                scope,
                Season2WordPartsPresentationMode.GuidedWordParts,
                stationChapterIndex = 3,
                stationId = 5,
            ).map { it.catalogId }.toSet()
        val hidden =
            Season2WordPartsCatalog.entriesForPresentationMode(
                scope,
                Season2WordPartsPresentationMode.HiddenWordPartsChallenge,
                stationChapterIndex = 3,
                stationId = 6,
            ).map { it.catalogId }.toSet()
        return guided to hidden
    }

    fun memWordsForLetter(chapterIndex: Int, letter: String): Int {
        val words = Season2ChapterRegistry.chapter(chapterIndex)?.wordCatalogIds.orEmpty()
        return words.count { id ->
            LessonWordCatalog.entries.find { it.id == id }?.letter == letter
        }
    }

    fun audioClipsContainsSeason2WavPath(): Boolean {
        val fields = AudioClips::class.java.declaredFields
        return fields.any { field ->
            if (field.type != String::class.java) return@any false
            field.isAccessible = true
            val value = field.get(null) as? String ?: return@any false
            value.contains("season2", ignoreCase = true) && value.endsWith(".wav")
        }
    }

    fun focusPoolsAreCompanionSpecific(): Boolean {
        val dino = Season2RawAudio.focusPool(DinoCharacter.Dino).toSet()
        val dina = Season2RawAudio.focusPool(DinoCharacter.Dina).toSet()
        return dino.isNotEmpty() && dina.isNotEmpty() && dino.none { it in dina }
    }

    fun mapPraisePoolsAreCompanionSpecific(): Boolean {
        val dino = Season2RawAudio.mapPraisePool(DinoCharacter.Dino).toSet()
        val dina = Season2RawAudio.mapPraisePool(DinoCharacter.Dina).toSet()
        return dino.size == 5 && dina.size == 5 && dino.none { it in dina }
    }

    fun compactContentGapsReport(): String {
        val belowSix = stationsBelowSixRounds().entries.joinToString(";") { "${it.key}=${it.value}" }
        val missingParts = curatedEntriesMissingWordPartChunks().joinToString(",")
        val (guided, hidden) = ch3WordPartsPools()
        return "below6=$belowSix|missingWordpart=$missingParts|ch3St5=${guided.size}|ch3St6=${hidden.size}"
    }

    fun compactUnusedAudioReport(): String =
        "legacy_map=${legacyUnusedMapRawResIds().joinToString(",") { it.toString() }}|deprecatedVoice=Season2Copy.returnCaptionVoiceRawRes"
}
