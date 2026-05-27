package com.tal.hebrewdino.ui.domain.economy

data class FoodInventoryEntry(
    val emoji: String,
    val count: Int,
)

/** Serializes emoji→count map for DataStore (e.g. "🍎:3;🍌:2"). */
object FoodInventoryCodec {
    const val DEFAULT_FOOD_EMOJI: String = "🍎"

    fun encode(map: Map<String, Int>): String =
        map
            .filter { it.value > 0 }
            .entries
            .joinToString(";") { "${it.key}:${it.value}" }

    fun decode(raw: String?): Map<String, Int> {
        if (raw.isNullOrBlank()) return emptyMap()
        return raw
            .split(";")
            .mapNotNull { part ->
                val idx = part.lastIndexOf(':')
                if (idx <= 0) return@mapNotNull null
                val emoji = part.substring(0, idx)
                val count = part.substring(idx + 1).toIntOrNull() ?: 0
                if (count > 0) emoji to count else null
            }.toMap()
    }

    fun entriesForDisplay(map: Map<String, Int>): List<FoodInventoryEntry> =
        FoodRewards.kinds.mapNotNull { kind ->
            val count = map[kind.emoji] ?: 0
            if (count > 0) FoodInventoryEntry(kind.emoji, count) else null
        }

    fun totalCount(map: Map<String, Int>): Int = map.values.sum().coerceAtLeast(0)
}
