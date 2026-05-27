package com.tal.hebrewdino.ui.economy

import com.tal.hebrewdino.ui.domain.economy.StationRoundCompleted
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
        withFakeCharacterStore { repo, _ ->
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

            engine.markPresented(peeked.eventId)
            assertNull(engine.peekPendingEvent())
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
