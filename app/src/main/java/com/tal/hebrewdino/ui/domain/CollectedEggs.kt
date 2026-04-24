package com.tal.hebrewdino.ui.domain

/** Story eggs collected after finishing each chapter’s egg finale (white → pink → cream). */
object CollectedEggs {
    fun stripCount(beachOutroSeen: Boolean, chapter2Completed: Boolean, chapter3Completed: Boolean): Int =
        listOf(beachOutroSeen, chapter2Completed, chapter3Completed).count { it }
}
