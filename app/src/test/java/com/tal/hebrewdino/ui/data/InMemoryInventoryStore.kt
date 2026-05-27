package com.tal.hebrewdino.ui.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory inventory for unit tests (avoids DataStore on bare ContextWrapper). */
class InMemoryInventoryStore : InventoryStoreOperations {
    private val owned = MutableStateFlow<Set<String>>(emptySet())
    private val equipped = MutableStateFlow<String?>(null)

    override val ownedAccessoriesFlow: Flow<Set<String>> = owned
    override val equippedAccessoryFlow: Flow<String?> = equipped

    override suspend fun addOwned(itemId: String) {
        owned.value = owned.value + itemId
    }

    override suspend fun setEquipped(itemId: String?) {
        equipped.value = itemId?.takeIf { it.isNotBlank() }
    }

    override suspend fun clearAll() {
        owned.value = emptySet()
        equipped.value = null
    }
}
