package com.tal.hebrewdino.ui.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class LevelSession(
    private val plan: StationQuizPlan,
    private val letterPoolSpec: LetterPoolSpec = LetterPoolSpec.Default,
) {
    private val rnd = Random.Default
    private val popGenerator = PopBalloonsGenerator(letterPoolSpec)
    private val maxGroupIndex = max(0, letterPoolSpec.groups.lastIndex)

    private var lastCorrectAnswer: String? = null
    private var bag: MutableList<String> = mutableListOf()
    private val seenLetters: MutableSet<String> = mutableSetOf()
    /** Correct catalog entry ids already used per letter (picture stations 4–5). */
    private val lessonWordUsedCorrectIdsByLetter: MutableMap<String, MutableSet<String>> = mutableMapOf()

    private val questionCount: Int = plan.questionCount

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

    var groupIndex by mutableIntStateOf(plan.initialGroupIndex.coerceIn(0, maxGroupIndex))
        private set

    private var _currentQuestion: Question? by mutableStateOf(null)

    private fun letterGroup(): List<String> {
        val g = letterPoolSpec.groups.getOrNull(groupIndex).orEmpty()
        require(g.isNotEmpty()) { "Letter group empty for index $groupIndex" }
        return g
    }

    private fun episodeOptionLetters(): List<String> =
        when (letterPoolSpec) {
            Chapter1LetterPoolSpec -> Chapter1Config.letters
            Chapter2LetterPoolSpec -> Chapter2Config.letters
            Chapter3LetterPoolSpec -> Chapter3Config.letters
            Chapter4LetterPoolSpec -> Chapter4Config.letters
            else -> letterPoolSpec.groups.flatten().distinct()
        }

    val totalQuestions: Int get() = questionCount

    val questionNumber: Int get() = currentIndex + 1

    val currentQuestion: Question?
        get() {
            if (currentIndex >= questionCount) return null
            if (_currentQuestion == null) {
                val group = letterGroup()
                _currentQuestion =
                    when (plan.mode) {
                        StationQuizMode.FindLetterGrid -> {
                            val correct = nextBalancedCorrect(group)
                            FindLetterGridGenerator.generate(
                                rnd = rnd,
                                group = group,
                                targetLetter = correct,
                                maxTargetCount = if (letterPoolSpec === Chapter1LetterPoolSpec) 4 else null,
                            )
                        }
                        StationQuizMode.PickLetter -> {
                            val correct = nextBalancedCorrect(group)
                            popGenerator.generate(
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
                                optionCount = 7,
                            )
                        }
                        StationQuizMode.PictureStartsWith -> {
                            val correct = nextBalancedCorrect(group)
                            val used = lessonWordUsedCorrectIdsByLetter.getOrPut(correct) { mutableSetOf() }
                            Chapter1LessonGenerators.pictureStartsWith(
                                rnd = rnd,
                                group = group,
                                targetLetter = correct,
                                excludeCorrectWordIds = used,
                                optionLetters = episodeOptionLetters(),
                            ).also { q ->
                                used.add(q.catalogEntryId)
                            }
                        }
                        StationQuizMode.ImageMatch -> {
                            val correct = nextBalancedCorrect(group)
                            val used = lessonWordUsedCorrectIdsByLetter.getOrPut(correct) { mutableSetOf() }
                            Chapter1LessonGenerators.imageMatch(
                                rnd = rnd,
                                group = group,
                                targetLetter = correct,
                                excludeCorrectWordIds = used,
                                alwaysThreeChoices = plan.imageMatchAlwaysThreeChoices,
                            ).also { q ->
                                used.add(q.correctChoiceId)
                            }
                        }
                        StationQuizMode.FinaleSlot -> {
                            val (a, b) = nextTwoDistinctCorrect(group)
                            FinaleSlotGenerator.generate(rnd, group, a, b)
                        }
                    }
            }
            return _currentQuestion
        }

    fun submitAnswer(answer: String): AnswerResult {
        val q = currentQuestion ?: return AnswerResult.Finished
        val correct =
            when (q) {
                is Question.PopBalloonsQuestion -> answer == q.correctAnswer
                is Question.FindLetterGridQuestion,
                is Question.ImageMatchQuestion,
                is Question.PictureStartsWithQuestion,
                is Question.FinaleSlotQuestion,
                ->
                    error("Use grid / imageMatch / pictureStartsWith / finale APIs")
            }
        return applyOutcome(correct)
    }

    fun wrongTap(): AnswerResult = applyOutcome(false)

    fun completeCurrentRound(): AnswerResult = applyOutcome(true)

    fun submitImageMatch(choiceId: String): AnswerResult {
        val q = currentQuestion as? Question.ImageMatchQuestion ?: return AnswerResult.Finished
        val ok = choiceId == q.correctChoiceId
        return applyOutcome(ok)
    }

    fun submitPictureStartsWith(letter: String): AnswerResult {
        val q = currentQuestion as? Question.PictureStartsWithQuestion ?: return AnswerResult.Finished
        val ok = letter == q.correctLetter
        return applyOutcome(ok)
    }

    fun submitFinaleWords(filled: List<String>): AnswerResult {
        val q = currentQuestion as? Question.FinaleSlotQuestion ?: return AnswerResult.Finished
        val ok = filled == q.words
        return applyOutcome(ok)
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
            bag = group.shuffled(rnd).toMutableList()
        }

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
        require(others.isNotEmpty()) { "Letter group needs at least two letters for finale" }
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
