package com.tal.hebrewdino.ui.economy

import com.tal.hebrewdino.ui.domain.cosmetics.AccessoryCatalog
import com.tal.hebrewdino.ui.domain.economy.StationRoundCompleted
import kotlinx.coroutines.flow.first
import com.tal.hebrewdino.ui.FakeCharacterStore
import com.tal.hebrewdino.ui.withFakeCharacterStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RewardEngineTest {
    @Test
    fun grant_station_round_persists_pending_event_until_marked_presented() = runTest {
        withFakeCharacterStore(
            FakeCharacterStore(growthStage = "EGG", foodCount = 3),
        ) { repo, _ ->
            val engine = RewardEngine(repo)

            engine.grantStationRoundCompleted(
                StationRoundCompleted(
                    chapterIndex = 0,
                    stationId = 1,
                    resetHunger = true,
                ),
            )

            val peeked = engine.peekPendingEvent()
            assertNotNull(peeked)
            assertEquals(3, peeked!!.applesCount)
            assertEquals(null, peeked.accessoryUnlockId)

            engine.markPresented(peeked.eventId)
            assertNull(engine.peekPendingEvent())
        }
    }

    @Test
    fun grant_station_round_unlocks_accessory_only_when_dino_is_adult() = runTest {
        withFakeCharacterStore(
            FakeCharacterStore(growthStage = "ADULT", foodCount = 3),
        ) { repo, _ ->
            val engine = RewardEngine(repo)

            engine.grantStationRoundCompleted(
                StationRoundCompleted(
                    chapterIndex = 0,
                    stationId = 1,
                    resetHunger = true,
                ),
            )

            val peeked = engine.peekPendingEvent()
            assertNotNull(peeked)
            assertEquals(AccessoryCatalog.hat.id, peeked!!.accessoryUnlockId)
            assertEquals(AccessoryCatalog.hat.id, repo.pendingAccessoryEquipFlow.first())
        }
    }

    @Test
    fun grant_without_hunger_reset_leaves_full_until_unchanged() = runTest {
        withFakeCharacterStore { repo, store ->
            store.fullUntilAtMsFlow.value = 9_999L
            val engine = RewardEngine(repo)

            engine.grantStationRoundCompleted(
                StationRoundCompleted(
                    chapterIndex = 0,
                    stationId = 2,
                    resetHunger = false,
                ),
            )

            assertEquals(9_999L, repo.fullUntilAtMsFlow.first())
        }
    }
}
