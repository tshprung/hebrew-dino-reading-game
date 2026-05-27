package com.tal.hebrewdino.ui.domain.cosmetics

import org.junit.Assert.assertTrue
import org.junit.Test

class AccessoryCelebrationTest {
    @Test
    fun sunglasses_celebration_mentions_gift_and_dino() {
        val spoken = accessoryCelebrationSpokenForTts(AccessoryCatalog.sunglasses.id)
        assertTrue(spoken.contains("משקפי שמש"))
        assertTrue(spoken.contains("דִּי-נוֹ"))
        assertTrue(spoken.contains("מגניב"))
    }
}
