package com.tal.hebrewdino.ui.data

import com.tal.hebrewdino.ui.domain.economy.FoodInventoryCodec
import com.tal.hebrewdino.ui.domain.economy.FoodRewards
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FoodInventoryCodecTest {
    @Test
    fun encode_decode_round_trip() {
        val map = mapOf("🍎" to 3, "🍑" to 2)
        val encoded = FoodInventoryCodec.encode(map)
        assertEquals(map, FoodInventoryCodec.decode(encoded))
    }

    @Test
    fun entries_for_display_follows_catalog_order() {
        val map = mapOf("🍉" to 1, "🍎" to 3)
        val entries = FoodInventoryCodec.entriesForDisplay(map)
        assertEquals(listOf("🍎", "🍉"), entries.map { it.emoji })
        assertEquals(3, entries.first { it.emoji == "🍎" }.count)
    }

    @Test
    fun catalog_includes_user_fruit_emojis() {
        val emojis = FoodRewards.kinds.map { it.emoji }.toSet()
        assertTrue("🍎" in emojis)
        assertTrue("🍑" in emojis)
        assertTrue("🍌" in emojis)
        assertTrue("🍉" in emojis)
    }
}
