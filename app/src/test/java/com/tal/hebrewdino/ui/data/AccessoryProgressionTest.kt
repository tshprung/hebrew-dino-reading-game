package com.tal.hebrewdino.ui.data

import com.tal.hebrewdino.ui.FakeCharacterStore
import com.tal.hebrewdino.ui.TestAppContext
import com.tal.hebrewdino.ui.domain.cosmetics.AccessoryCatalog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AccessoryProgressionTest {
    private var previousStoreFactory: (android.content.Context) -> CharacterRepository.CharacterStore = { error("") }
    private var previousInventoryFactory: (android.content.Context) -> InventoryStoreOperations = { error("") }

    @Before
    fun setUp() {
        previousStoreFactory = CharacterRepository.storeFactory
        previousInventoryFactory = InventoryStore.factory
        CharacterRepository.storeFactory = { FakeCharacterStore(foodCount = 5, growthStage = "ADULT") }
        InventoryStore.factory = { _ -> InMemoryInventoryStore() }
    }

    @After
    fun tearDown() {
        CharacterRepository.storeFactory = previousStoreFactory
        InventoryStore.factory = previousInventoryFactory
    }

    @Test
    fun grantProgressionAccessory_unlocks_and_equips_once() = runTest {
        val repo = CharacterRepository(TestAppContext())

        val first = repo.grantProgressionAccessory(AccessoryCatalog.hat.id)
        assertTrue(first)
        assertTrue(AccessoryCatalog.hat.id in repo.ownedAccessoriesFlow.first())
        assertEquals(AccessoryCatalog.hat.id, repo.equippedAccessoryFlow.first())

        val second = repo.grantProgressionAccessory(AccessoryCatalog.hat.id)
        assertFalse(second)
    }

    @Test
    fun grantAccessoryForStationCompleted_maps_stations() = runTest {
        val repo = CharacterRepository(TestAppContext())

        assertTrue(repo.grantAccessoryForStationCompleted(chapterIndex = 0, stationId = 1))
        assertEquals(AccessoryCatalog.hat.id, repo.equippedAccessoryFlow.first())

        assertTrue(repo.grantAccessoryForStationCompleted(chapterIndex = 0, stationId = 2))
        assertEquals(AccessoryCatalog.sunglasses.id, repo.equippedAccessoryFlow.first())

        assertTrue(repo.grantAccessoryForStationCompleted(chapterIndex = 0, stationId = 3))
        assertEquals(AccessoryCatalog.bowtie.id, repo.equippedAccessoryFlow.first())
    }
}
