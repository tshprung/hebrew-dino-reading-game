package com.tal.hebrewdino.ui.domain.cosmetics

data class AccessoryItem(
    val id: String,
    val displayNameHe: String,
    val emoji: String,
)

object AccessoryCatalog {
    val hat =
        AccessoryItem(
            id = "hat",
            displayNameHe = "כובע",
            emoji = "🎩",
        )
    val sunglasses =
        AccessoryItem(
            id = "sunglasses",
            displayNameHe = "משקפיים",
            emoji = "🕶️",
        )
    val bowtie =
        AccessoryItem(
            id = "bowtie",
            displayNameHe = "פפיון",
            emoji = "🎀",
        )

    val all: List<AccessoryItem> = listOf(hat, sunglasses, bowtie)

    fun find(itemId: String): AccessoryItem? = all.firstOrNull { it.id == itemId }
}
