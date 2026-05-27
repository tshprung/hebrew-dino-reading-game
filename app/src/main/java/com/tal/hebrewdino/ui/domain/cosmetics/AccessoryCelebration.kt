package com.tal.hebrewdino.ui.domain.cosmetics

import com.tal.hebrewdino.ui.domain.dinoNameSpokenForTts

/** One-time TTS on challenge summary when a milestone accessory unlocks. */
fun accessoryCelebrationSpokenForTts(accessoryId: String): String {
    val giftName =
        when (accessoryId) {
            AccessoryCatalog.hat.id -> "כובע מצחיק"
            AccessoryCatalog.sunglasses.id -> "משקפי שמש"
            AccessoryCatalog.bowtie.id -> "פפיון חמוד"
            else -> AccessoryCatalog.find(accessoryId)?.displayNameHe ?: "מתנה"
        }
    val dino = dinoNameSpokenForTts()
    return "איזה יופי! קיבלת $giftName במתנה! $dino כל כך ישמח עכשיו וייראה ממש מגניב!"
}
