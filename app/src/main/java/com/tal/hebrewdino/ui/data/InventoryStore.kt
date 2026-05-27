package com.tal.hebrewdino.ui.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Persists cosmetic inventory (owned accessories + equipped slot).
 */
class InventoryStore(
    context: Context,
) : InventoryStoreOperations {
    companion object {
        @Volatile
        internal var factory: (Context) -> InventoryStoreOperations = { InventoryStore(it) }
    }

    private val appContext = context.applicationContext

    private val ownedKey = stringSetPreferencesKey("inventory_owned_accessories")
    private val equippedKey = stringPreferencesKey("inventory_equipped_accessory")
    private val pendingEquipKey = stringPreferencesKey("inventory_pending_accessory_equip")

    override val ownedAccessoriesFlow: Flow<Set<String>> =
        appContext.dataStore.data.map { prefs ->
            prefs[ownedKey].orEmpty()
        }

    override val equippedAccessoryFlow: Flow<String?> =
        appContext.dataStore.data.map { prefs ->
            prefs[equippedKey]?.takeIf { it.isNotBlank() }
        }

    override val pendingAccessoryEquipFlow: Flow<String?> =
        appContext.dataStore.data.map { prefs ->
            prefs[pendingEquipKey]?.takeIf { it.isNotBlank() }
        }

    override suspend fun addOwned(itemId: String) {
        appContext.dataStore.edit { prefs ->
            val next = prefs[ownedKey].orEmpty().toMutableSet()
            next += itemId
            prefs[ownedKey] = next
        }
    }

    override suspend fun setEquipped(itemId: String?) {
        appContext.dataStore.edit { prefs ->
            if (itemId.isNullOrBlank()) {
                prefs.remove(equippedKey)
            } else {
                prefs[equippedKey] = itemId
            }
        }
    }

    override suspend fun setPendingAccessoryEquip(itemId: String?) {
        appContext.dataStore.edit { prefs ->
            if (itemId.isNullOrBlank()) {
                prefs.remove(pendingEquipKey)
            } else {
                prefs[pendingEquipKey] = itemId
            }
        }
    }

    override suspend fun clearAll() {
        appContext.dataStore.edit { prefs ->
            prefs.remove(ownedKey)
            prefs.remove(equippedKey)
            prefs.remove(pendingEquipKey)
        }
    }
}
