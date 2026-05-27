package com.tal.hebrewdino.ui.data

interface StoryNarrationGate {
    suspend fun isPart2Spoken(): Boolean

    suspend fun isPart3Spoken(): Boolean

    suspend fun setPart2Spoken()

    suspend fun setPart3Spoken()

    suspend fun reset()
}
