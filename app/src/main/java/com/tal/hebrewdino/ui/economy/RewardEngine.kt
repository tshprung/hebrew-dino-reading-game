package com.tal.hebrewdino.ui.economy

import android.content.Context
import com.tal.hebrewdino.ui.data.CharacterRepository
import com.tal.hebrewdino.ui.domain.economy.PendingRewardEvent
import com.tal.hebrewdino.ui.domain.economy.RewardGrantRequest
import com.tal.hebrewdino.ui.domain.economy.StationRoundCompleted
import com.tal.hebrewdino.ui.domain.economy.fanfareTextForApples
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

/**
 * Central coordinator for grants (persistence) and pending presentation events.
 */
class RewardEngine(
    private val characterRepository: CharacterRepository,
) {
    companion object {
        @Volatile
        private var instance: RewardEngine? = null

        fun get(context: Context): RewardEngine {
            val existing = instance
            if (existing != null) return existing
            return synchronized(this) {
                val again = instance
                if (again != null) return@synchronized again
                val created =
                    RewardEngine(
                        characterRepository = CharacterRepository(context.applicationContext),
                    )
                instance = created
                created
            }
        }
    }

    private val mutex = Mutex()
    private val _pendingRewardEvent = MutableStateFlow<PendingRewardEvent?>(null)
    val pendingRewardEvent: StateFlow<PendingRewardEvent?> = _pendingRewardEvent.asStateFlow()

    suspend fun grant(request: RewardGrantRequest) {
        mutex.withLock {
            request.markChapterProgress?.let { progress ->
                if (progress.stationId > 0) {
                    characterRepository.markChapterStationCompleted(
                        chapterIndex = progress.chapterIndex,
                        stationId = progress.stationId,
                    )
                    characterRepository.grantAccessoryForStationCompleted(
                        chapterIndex = progress.chapterIndex,
                        stationId = progress.stationId,
                    )
                }
            }
            if (request.resetHunger) {
                characterRepository.setFullUntilAtMs(0L)
            }
            if (request.applesReward > 0) {
                characterRepository.addFood(request.applesReward)
            }
            if (request.emitPresentation && request.applesReward > 0) {
                val event =
                    PendingRewardEvent(
                        eventId = UUID.randomUUID().toString(),
                        applesCount = request.applesReward,
                        fanfareText = fanfareTextForApples(request.applesReward),
                        visualCue = request.visualCue,
                    )
                characterRepository.savePendingRewardEvent(event)
                _pendingRewardEvent.value = event
            }
        }
    }

    suspend fun grantStationRoundCompleted(completed: StationRoundCompleted) {
        grant(completed)
    }

    /** Loads persisted event into memory if the in-memory cache is empty. */
    suspend fun ensurePendingEventLoaded(): PendingRewardEvent? =
        mutex.withLock {
            val cached = _pendingRewardEvent.value
            if (cached != null) return@withLock cached
            val stored = characterRepository.readPendingRewardEvent()
            if (stored != null) {
                _pendingRewardEvent.value = stored
            }
            stored
        }

    /** Idempotent read for presentation screens (does not clear). */
    suspend fun peekPendingEvent(): PendingRewardEvent? = ensurePendingEventLoaded()

    suspend fun markPresented(eventId: String) {
        mutex.withLock {
            val current = _pendingRewardEvent.value ?: characterRepository.readPendingRewardEvent()
            if (current?.eventId != eventId) return@withLock
            characterRepository.clearPendingRewardEvent()
            _pendingRewardEvent.value = null
        }
    }

    suspend fun clearPendingPresentation() {
        mutex.withLock {
            characterRepository.clearPendingRewardEvent()
            _pendingRewardEvent.value = null
        }
    }

    suspend fun hydrateFromStore() {
        mutex.withLock {
            _pendingRewardEvent.value = characterRepository.readPendingRewardEvent()
        }
    }

    suspend fun resetAll() {
        mutex.withLock {
            characterRepository.clearPendingRewardEvent()
            _pendingRewardEvent.value = null
        }
    }
}
