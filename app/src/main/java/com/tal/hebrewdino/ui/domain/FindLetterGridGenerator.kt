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
        maxTargetCount: Int? = null,
    ): Question.FindLetterGridQuestion {
        require(group.isNotEmpty())
        require(targetLetter in group)
        val columns = if (rnd.nextBoolean()) 3 else 4
        val total = columns * columns
        val desiredTargetCount = if (columns == 3) 3 else 4
        val maxTargets = (total - 2).coerceAtLeast(desiredTargetCount)
        val cappedMax = maxTargetCount?.coerceIn(desiredTargetCount, maxTargets) ?: maxTargets
        val targetCount = desiredTargetCount.coerceAtMost(cappedMax)
        val others = group.filter { it != targetLetter }.ifEmpty { listOf(targetLetter) }
        val indices = (0 until total).shuffled(rnd).take(targetCount).toSet()
        val cells =
            List(total) { idx ->
                if (idx in indices) targetLetter else others.random(rnd)
            }
        return Question.FindLetterGridQuestion(
            targetLetter = targetLetter,
            columns = columns,
            rows = columns,
            cells = cells,
        )
    }
}
