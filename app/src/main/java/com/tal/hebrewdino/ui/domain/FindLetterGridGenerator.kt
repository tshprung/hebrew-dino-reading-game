package com.tal.hebrewdino.ui.domain

import kotlin.random.Random

object FindLetterGridGenerator {
    /**
     * Builds a square [columns]×[rows] grid with [targetLetter] appearing at least [minTargetCount] times.
     * Other cells use random letters from [group] (excluding the target where possible).
     */
    fun generate(
        rnd: Random,
        group: List<String>,
        targetLetter: String,
        minTargetCount: Int = 3,
    ): Question.FindLetterGridQuestion {
        require(group.isNotEmpty())
        require(targetLetter in group)
        val columns = if (rnd.nextBoolean()) 3 else 4
        val rows = columns
        val total = columns * rows
        val maxTargets = (total - 2).coerceAtLeast(minTargetCount)
        val targetCount = (minTargetCount..maxTargets).random(rnd)
        val others = group.filter { it != targetLetter }.ifEmpty { listOf(targetLetter) }
        val indices = (0 until total).shuffled(rnd).take(targetCount).toSet()
        val cells =
            List(total) { idx ->
                if (idx in indices) targetLetter else others.random(rnd)
            }
        return Question.FindLetterGridQuestion(
            targetLetter = targetLetter,
            columns = columns,
            rows = rows,
            cells = cells,
        )
    }
}
