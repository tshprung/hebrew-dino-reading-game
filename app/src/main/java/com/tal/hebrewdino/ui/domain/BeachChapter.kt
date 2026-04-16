package com.tal.hebrewdino.ui.domain

import kotlin.random.Random

data class LevelConfig(
    val levelId: Int,
    val lettersPool: List<String>,
    val questionCount: Int,
)

data class BeachQuestion(
    val prompt: String,
    val targetLetter: String,
    val options: List<String>,
)

object BeachChapter {
    fun configForLevel(levelId: Int): LevelConfig {
        val base = listOf("א", "ב", "מ")
        val withLD = base + listOf("ל", "ד")

        val pool = when (levelId) {
            in 1..8 -> base
            else -> withLD
        }

        val questionCount = when (levelId) {
            1, 2 -> 6
            3, 4 -> 8
            5, 6, 7 -> 10
            else -> 12
        }

        return LevelConfig(levelId = levelId, lettersPool = pool, questionCount = questionCount)
    }

    fun generateQuestions(levelId: Int): List<BeachQuestion> {
        val config = configForLevel(levelId)
        val rnd = Random(levelId * 9973)

        return (1..config.questionCount).map {
            val target = config.lettersPool[rnd.nextInt(config.lettersPool.size)]
            val optionCount = when (config.lettersPool.size) {
                3 -> 3
                4 -> 4
                else -> 4
            }

            val options = buildList {
                add(target)
                val others = config.lettersPool.filter { it != target }.shuffled(rnd)
                addAll(others.take(optionCount - 1))
            }.shuffled(rnd)

            BeachQuestion(
                prompt = "בחר את האות: $target",
                targetLetter = target,
                options = options,
            )
        }
    }
}

