package com.tal.hebrewdino.ui.domain

/** Story eggs collected after finishing each chapter’s egg finale (white → pink → cream). */
object CollectedEggs {
    fun stripCount(
        beachOutroSeen: Boolean,
        chapter3Completed: Boolean,
        chapter5Completed: Boolean,
    ): Int = listOf(beachOutroSeen, chapter3Completed, chapter5Completed).count { it }
}
