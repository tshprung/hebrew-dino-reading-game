package com.tal.hebrewdino.ui.data

class InMemoryStoryNarrationGate : StoryNarrationGate {
    var eggKnockPromptSpoken: Boolean = false
    var part2Spoken: Boolean = false
    var part3Spoken: Boolean = false

    override suspend fun isEggKnockPromptSpoken(): Boolean = eggKnockPromptSpoken

    override suspend fun setEggKnockPromptSpoken() {
        eggKnockPromptSpoken = true
    }

    override suspend fun isPart2Spoken(): Boolean = part2Spoken

    override suspend fun isPart3Spoken(): Boolean = part3Spoken

    override suspend fun setPart2Spoken() {
        part2Spoken = true
    }

    override suspend fun setPart3Spoken() {
        part3Spoken = true
    }

    override suspend fun reset() {
        eggKnockPromptSpoken = false
        part2Spoken = false
        part3Spoken = false
    }
}
