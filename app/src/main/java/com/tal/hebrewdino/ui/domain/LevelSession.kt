package com.tal.hebrewdino.ui.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class LevelSession(
    private val questionCount: Int,
    private val tapGenerator: TapChoiceGenerator = TapChoiceGenerator(),
    private val popGenerator: PopBalloonsGenerator = PopBalloonsGenerator(),
) {
    private val rnd = Random.Default
    private val maxGroupIndex = max(0, LetterPool.groups.size - 1)

    private var lastCorrectAnswer: String? = null
    private var bag: MutableList<String> = mutableListOf()

    var currentIndex by mutableIntStateOf(0)
        private set

    var correctCount by mutableIntStateOf(0)
        private set

    var mistakeCount by mutableIntStateOf(0)
        private set

    var correctStreak by mutableIntStateOf(0)
        private set

    var incorrectStreak by mutableIntStateOf(0)
        private set

    var difficultyLevel by mutableIntStateOf(0)
        private set

    var groupIndex by mutableIntStateOf(0)
        private set

    private var _currentQuestion: Question? by mutableStateOf(null)

    val totalQuestions: Int get() = questionCount

    val questionNumber: Int get() = currentIndex + 1

    val currentQuestion: Question?
        get() {
            if (currentIndex >= questionCount) return null
            if (_currentQuestion == null) {
                val group = tapGenerator.group(groupIndex)
                val correct = nextBalancedCorrect(group)
                val optionCount = 3 // MVP: difficulty = group only

                _currentQuestion =
                    if (shouldUsePopQuestion()) {
                        popGenerator.generate(
                            rnd = rnd,
                            group = group,
                            correctAnswer = correct,
                            optionCount = optionCount,
                        )
                    } else {
                        tapGenerator.generateTapChoiceQuestion(
                            rnd = rnd,
                            group = group,
                            correctAnswer = correct,
                            optionCount = optionCount,
                        )
                    }
            }
            return _currentQuestion
        }

    fun submitAnswer(answer: String): AnswerResult {
        val q = currentQuestion ?: return AnswerResult.Finished
        val correct =
            when (q) {
                is Question.TapChoiceQuestion -> answer == q.correctAnswer
                is Question.PopBalloonsQuestion -> answer == q.correctAnswer
            }

        if (correct) {
            correctCount += 1
            correctStreak += 1
            incorrectStreak = 0
            if (correctStreak >= 3) {
                moveGroup(+1)
                correctStreak = 0
            }
        } else {
            mistakeCount += 1
            incorrectStreak += 1
            correctStreak = 0
            if (incorrectStreak >= 2) {
                moveGroup(-1)
                incorrectStreak = 0
            }
        }

        difficultyLevel = groupIndex
        return if (correct) AnswerResult.Correct else AnswerResult.Wrong
    }

    fun nextQuestion() {
        if (currentIndex >= questionCount) return
        currentIndex += 1
        _currentQuestion = null
    }

    private fun moveGroup(delta: Int) {
        val newIndex = min(maxGroupIndex, max(0, groupIndex + delta))
        if (newIndex != groupIndex) {
            groupIndex = newIndex
            bag.clear()
            _currentQuestion = null
        }
    }

    private fun nextBalancedCorrect(group: List<String>): String {
        if (bag.isEmpty()) {
            // Ensure each letter appears once before repeating.
            bag = group.shuffled(rnd).toMutableList()
        }

        // Try to avoid the same correct answer twice in a row.
        val candidate = bag.removeAt(0)
        val last = lastCorrectAnswer
        if (last != null && candidate == last && bag.isNotEmpty()) {
            val swapped = bag.removeAt(0)
            bag.add(candidate)
            lastCorrectAnswer = swapped
            return swapped
        }

        lastCorrectAnswer = candidate
        return candidate
    }

    private fun shouldUsePopQuestion(): Boolean {
        // Mix types lightly: about 30% pop-balloons questions.
        // (Keeps MVP stable while adding variety.)
        return rnd.nextInt(100) < 30
    }
}

sealed class AnswerResult {
    data object Correct : AnswerResult()
    data object Wrong : AnswerResult()
    data object Finished : AnswerResult()
}

