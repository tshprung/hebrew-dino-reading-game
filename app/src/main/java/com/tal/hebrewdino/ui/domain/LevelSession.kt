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
    initialGroupIndex: Int = 0,
    private val quizMode: StationQuizMode,
    letterPoolSpec: LetterPoolSpec = LetterPoolSpec.Default,
) {
    private val rnd = Random.Default
    private val tapGenerator = TapChoiceGenerator(letterPoolSpec)
    private val popGenerator = PopBalloonsGenerator(letterPoolSpec)
    private val matchGenerator = PictureLetterMatchGenerator(letterPoolSpec)
    private val maxGroupIndex = max(0, letterPoolSpec.groups.lastIndex)

    private var lastCorrectAnswer: String? = null
    private var bag: MutableList<String> = mutableListOf()
    private val seenLetters: MutableSet<String> = mutableSetOf()

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

    var groupIndex by mutableIntStateOf(initialGroupIndex.coerceIn(0, maxGroupIndex))
        private set

    private var _currentQuestion: Question? by mutableStateOf(null)

    val totalQuestions: Int get() = questionCount

    val questionNumber: Int get() = currentIndex + 1

    val currentQuestion: Question?
        get() {
            if (currentIndex >= questionCount) return null
            if (_currentQuestion == null) {
                val group = tapGenerator.group(groupIndex)
                _currentQuestion =
                    when (quizMode) {
                        StationQuizMode.TapChoice -> {
                            val correct = nextBalancedCorrect(group)
                            tapGenerator.generateTapChoiceQuestion(
                                rnd = rnd,
                                group = group,
                                correctAnswer = correct,
                                optionCount = 3,
                            )
                        }
                        StationQuizMode.PopBalloons -> {
                            val correct = nextBalancedCorrect(group)
                            popGenerator.generate(
                                rnd = rnd,
                                group = group,
                                correctAnswer = correct,
                                optionCount = 3,
                            )
                        }
                        StationQuizMode.RevealTiles -> {
                            val correct = nextBalancedCorrect(group)
                            val tap =
                                tapGenerator.generateTapChoiceQuestion(
                                    rnd = rnd,
                                    group = group,
                                    correctAnswer = correct,
                                    optionCount = 3,
                                )
                            Question.RevealTilesQuestion(
                                correctAnswer = tap.correctAnswer,
                                options = tap.options,
                            )
                        }
                        StationQuizMode.PictureLetterMatch -> {
                            val (a, b) = nextTwoDistinctCorrect(group)
                            matchGenerator.generate(
                                rnd = rnd,
                                group = group,
                                letter1 = a,
                                letter2 = b,
                            )
                        }
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
                is Question.RevealTilesQuestion -> answer == q.correctAnswer
                is Question.PictureLetterMatchQuestion ->
                    error("PictureLetterMatchQuestion uses submitMatchOutcome(success)")
            }
        return applyOutcome(correct)
    }

    /** Called when the child finished a tap–tap matching board (both pairs correct) or made a wrong pair attempt. */
    fun submitMatchOutcome(success: Boolean): AnswerResult {
        val q = currentQuestion ?: return AnswerResult.Finished
        require(q is Question.PictureLetterMatchQuestion)
        return applyOutcome(success)
    }

    private fun applyOutcome(correct: Boolean): AnswerResult {
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

    /**
     * Returns true only the first time this letter is seen in this session.
     * (We intentionally do NOT persist this between sessions.)
     */
    fun consumeFirstTimeSeen(letter: String): Boolean = seenLetters.add(letter)

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

    private fun nextTwoDistinctCorrect(group: List<String>): Pair<String, String> {
        val first = nextBalancedCorrect(group)
        var second = nextBalancedCorrect(group)
        if (second != first) return first to second
        val others = group.filter { it != first }
        require(others.isNotEmpty()) { "Letter group needs at least two letters for matching" }
        val alt = others.random(rnd)
        lastCorrectAnswer = alt
        return first to alt
    }
}

sealed class AnswerResult {
    data object Correct : AnswerResult()
    data object Wrong : AnswerResult()
    data object Finished : AnswerResult()
}
