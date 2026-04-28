package com.tal.hebrewdino.ui.domain

import kotlin.random.Random
import org.junit.Assert.assertTrue
import org.junit.Test

class Chapter3Station2VehicleSynonymsTest {
    @Test
    fun chapter3Station2_doesNotShowAutoCarVehicleSynonymsTogether() {
        val group = Chapter3Config.letters
        val banned = setOf("אוטו", "רכב", "מכונית")

        // Sweep a bunch of seeds to reduce flakiness.
        repeat(250) { seed ->
            val rnd = Random(seed)
            val q =
                Chapter3LessonGenerators.imageMatch(
                    rnd = rnd,
                    group = group,
                    targetLetter = group.first(),
                    excludeCorrectWordIds = emptySet(),
                    alwaysThreeChoices = true, // station 2 uses 3 cards
                    totalChoiceCount = 3,
                )
            val hits = q.choices.count { it.word in banned }
            assertTrue(
                "Vehicle synonyms appeared together (seed=$seed): ${q.choices.map { it.word }}",
                hits <= 1,
            )
        }
    }
}

