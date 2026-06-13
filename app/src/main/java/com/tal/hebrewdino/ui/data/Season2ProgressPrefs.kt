package com.tal.hebrewdino.ui.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tal.hebrewdino.ui.domain.ChapterUnlockWaiverPolicy
import com.tal.hebrewdino.ui.domain.Season2ChapterRegistry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Season 2 UX-only progress.
 *
 * We only need to know which dinosaur chapters are fully completed so the chapter-select
 * screen can highlight the next available dinosaur.
 */
class Season2ProgressPrefs(private val context: Context) {
    companion object {
        /**
         * Bump when Season 2 station order/plans change per chapter.
         * Saved per-station completion is keyed by station number only, so a version bump clears
         * affected chapter progress to avoid stale completions pointing at different station types.
         */
        const val STATION_PLAN_VERSION: Int = 2

        private val chapterRange: IntRange
            get() = 1..Season2ChapterRegistry.CHAPTER_COUNT
    }

    private val stationPlanVersionKey: Preferences.Key<Int> =
        intPreferencesKey("season2_station_plan_version")

    private val completedChaptersKey: Preferences.Key<String> =
        stringPreferencesKey("season2_completed_chapters")

    private val chapterUnlockWaiversKey: Preferences.Key<String> =
        stringPreferencesKey("season2_chapter_unlock_waivers")

    private fun completedStationsKeyForChapter(chapterId: Int): Preferences.Key<String> =
        stringPreferencesKey("season2_ch${chapterId}_completed_stations")

    private fun introDismissedKeyForChapter(chapterId: Int): Preferences.Key<String> =
        stringPreferencesKey("season2_ch${chapterId}_intro_dismissed")

    private fun puzzleMapExplainHeardKeyForChapter(chapterId: Int): Preferences.Key<String> =
        stringPreferencesKey("season2_ch${chapterId}_puzzle_map_explain_heard")

    private val seasonIntroDismissedKey: Preferences.Key<String> =
        stringPreferencesKey("season2_season_intro_dismissed")

    val chapterUnlockWaiversFlow: Flow<Set<Int>> =
        context.dataStore.data.map { prefs ->
            ChapterUnlockWaiverPolicy.parseWaivers(
                prefs[chapterUnlockWaiversKey].orEmpty(),
                validRange = chapterRange,
            )
        }

    suspend fun setChapterUnlockWaivers(chapterIds: Set<Int>) {
        context.dataStore.edit { prefs ->
            prefs[chapterUnlockWaiversKey] =
                ChapterUnlockWaiverPolicy.serializeWaivers(chapterIds, validRange = chapterRange)
        }
    }

    val completedChaptersFlow: Flow<Set<Int>> =
        context.dataStore.data.map { prefs ->
            val raw = prefs[completedChaptersKey].orEmpty()
            if (raw.isBlank()) return@map emptySet()
            raw.split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .filter { it in chapterRange }
                .toSet()
        }

    fun completedStationsFlow(chapterId: Int): Flow<Set<Int>> =
        context.dataStore.data.map { prefs ->
            if (chapterId !in chapterRange) return@map emptySet()
            val raw = prefs[completedStationsKeyForChapter(chapterId)].orEmpty()
            if (raw.isBlank()) return@map emptySet()
            raw.split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .filter { it in 1..6 }
                .toSet()
        }

    suspend fun markStationCompleted(chapterId: Int, stationId: Int) {
        if (chapterId !in chapterRange) return
        if (stationId !in 1..6) return
        val key = completedStationsKeyForChapter(chapterId)
        context.dataStore.edit { prefs ->
            val raw = prefs[key].orEmpty()
            val set =
                raw.split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .filter { it in 1..6 }
                    .toMutableSet()
            set.add(stationId)
            prefs[key] = set.toList().sorted().joinToString(",")
        }
    }

    val seasonIntroDismissedFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[seasonIntroDismissedKey] == "1"
        }

    suspend fun markSeasonIntroDismissed() {
        context.dataStore.edit { prefs ->
            prefs[seasonIntroDismissedKey] = "1"
        }
    }

    fun introDismissedFlow(chapterId: Int): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            if (chapterId !in chapterRange) return@map false
            prefs[introDismissedKeyForChapter(chapterId)] == "1"
        }

    suspend fun markIntroDismissed(chapterId: Int) {
        if (chapterId !in chapterRange) return
        context.dataStore.edit { prefs ->
            prefs[introDismissedKeyForChapter(chapterId)] = "1"
        }
    }

    fun puzzleMapExplainHeardFlow(chapterId: Int): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            if (chapterId !in chapterRange) return@map false
            prefs[puzzleMapExplainHeardKeyForChapter(chapterId)] == "1"
        }

    suspend fun markPuzzleMapExplainHeard(chapterId: Int) {
        if (chapterId !in chapterRange) return
        context.dataStore.edit { prefs ->
            prefs[puzzleMapExplainHeardKeyForChapter(chapterId)] = "1"
        }
    }

    suspend fun markChapterCompleted(chapterId: Int) {
        if (chapterId !in chapterRange) return
        context.dataStore.edit { prefs ->
            val raw = prefs[completedChaptersKey].orEmpty()
            val set =
                raw.split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .filter { it in chapterRange }
                    .toMutableSet()
            set.add(chapterId)
            prefs[completedChaptersKey] = set.toList().sorted().joinToString(",")
        }
    }

    /**
     * Clears Season 2 station + chapter completion when [STATION_PLAN_VERSION] advances.
     * Safe to call on every Season 2 entry; no-op when already migrated.
     */
    suspend fun migrateStationPlanProgressIfNeeded() {
        context.dataStore.edit { prefs ->
            val stored = prefs[stationPlanVersionKey] ?: 0
            if (stored >= STATION_PLAN_VERSION) return@edit
            val raw = prefs[completedChaptersKey].orEmpty()
            if (
                raw.isNotBlank() ||
                    chapterRange.any { ch ->
                        prefs[completedStationsKeyForChapter(ch)].orEmpty().isNotBlank()
                    }
            ) {
                prefs[completedChaptersKey] = ""
                for (ch in chapterRange) {
                    prefs[completedStationsKeyForChapter(ch)] = ""
                }
            }
            prefs[stationPlanVersionKey] = STATION_PLAN_VERSION
        }
    }

    suspend fun resetSeason2() {
        resetChapters(chapterRange.toSet())
        context.dataStore.edit { prefs ->
            prefs.remove(seasonIntroDismissedKey)
            prefs.remove(chapterUnlockWaiversKey)
            prefs[stationPlanVersionKey] = STATION_PLAN_VERSION
        }
    }

    /** Clears saved Season 2 progress for the given chapter numbers only. */
    suspend fun resetChapters(chapterIds: Set<Int>) {
        val ids = chapterIds.filter { it in chapterRange }.toSet()
        if (ids.isEmpty()) return
        context.dataStore.edit { prefs ->
            val raw = prefs[completedChaptersKey].orEmpty()
            val completed =
                raw.split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .filter { it in chapterRange }
                    .toMutableSet()
            completed.removeAll(ids)
            prefs[completedChaptersKey] = completed.sorted().joinToString(",")
            for (ch in ids) {
                prefs[completedStationsKeyForChapter(ch)] = ""
                prefs.remove(introDismissedKeyForChapter(ch))
                prefs.remove(puzzleMapExplainHeardKeyForChapter(ch))
            }
        }
    }
}

