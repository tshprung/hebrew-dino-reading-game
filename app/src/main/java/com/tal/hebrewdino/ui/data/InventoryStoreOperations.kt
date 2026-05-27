package com.tal.hebrewdino.ui.data

import kotlinx.coroutines.flow.Flow

internal interface InventoryStoreOperations {
    val ownedAccessoriesFlow: Flow<Set<String>>
    val equippedAccessoryFlow: Flow<String?>
    suspend fun addOwned(itemId: String)
    suspend fun setEquipped(itemId: String?)
    suspend fun clearAll()
}
