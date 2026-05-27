package com.tal.hebrewdino.ui.data

import kotlinx.coroutines.flow.Flow

internal interface InventoryStoreOperations {
    val ownedAccessoriesFlow: Flow<Set<String>>
    val equippedAccessoryFlow: Flow<String?>
    val pendingAccessoryEquipFlow: Flow<String?>

    suspend fun addOwned(itemId: String)
    suspend fun setEquipped(itemId: String?)
    suspend fun setPendingAccessoryEquip(itemId: String?)
    suspend fun clearAll()
}
