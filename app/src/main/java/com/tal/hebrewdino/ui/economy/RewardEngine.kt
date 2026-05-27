package com.tal.hebrewdino.ui.economy

import android.content.Context
import com.tal.hebrewdino.ui.data.CharacterRepository
import com.tal.hebrewdino.ui.domain.economy.PendingRewardEvent
import com.tal.hebrewdino.ui.domain.economy.RewardGrantRequest
import com.tal.hebrewdino.ui.domain.economy.StationRoundCompleted
import com.tal.hebrewdino.ui.domain.economy.FoodRewards
import com.tal.hebrewdino.ui.domain.economy.GrowthStage
import com.tal.hebrewdino.ui.domain.economy.fanfareTextForFood
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
            var accessoryUnlockId: String? = null
            val wallet = characterRepository.playerWalletFlow.first()
            val accessoriesAllowed = wallet.growthStage == GrowthStage.ADULT
            request.markChapterProgress?.let { progress ->
                if (progress.stationId > 0) {
                    characterRepository.markChapterStationCompleted(
                        chapterIndex = progress.chapterIndex,
                        stationId = progress.stationId,
                    )
                    if (accessoriesAllowed) {
                        accessoryUnlockId =
                            characterRepository.grantAccessoryForStationCompleted(
                                chapterIndex = progress.chapterIndex,
                                stationId = progress.stationId,
                            )
                    }
                }
            }
            if (request.resetHunger) {
                characterRepository.setFullUntilAtMs(0L)
            }
            val eventId = UUID.randomUUID().toString()
            val food =
                if (request.applesReward > 0) {
                    FoodRewards.pickForSeed(eventId)
                } else {
                    null
                }
            if (request.applesReward > 0 && food != null) {
                characterRepository.addFood(request.applesReward, food.emoji)
            }
            if (request.emitPresentation && request.applesReward > 0 && food != null) {
                val event =
                    PendingRewardEvent(
                        eventId = eventId,
                        applesCount = request.applesReward,
                        fanfareText = fanfareTextForFood(request.applesReward, food),
                        visualCue = request.visualCue,
                        accessoryUnlockId = accessoryUnlockId,
                        foodEmoji = food.emoji,
                        foodNameSingularHe = food.nameSingularHe,
                        foodNamePluralHe = food.namePluralHe,
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
