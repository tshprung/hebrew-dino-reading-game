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

    /** Correct catalog entry ids already used per letter (picture stations 4–5). */
    private val lessonWordUsedCorrectIdsByLetter: MutableMap<String, MutableSet<String>> = mutableMapOf()

    /** Pop-all-letters balloons: pick 5 unique words once per station run. */
    private val chapter3PopAllLettersWords: List<Pair<String, String>>? =
        if (plan.mode == StationQuizMode.PopBalloons && plan.popAllLettersInWord) {
            val all =
                when (letterPoolSpec) {
                    Chapter3LetterPoolSpec -> Chapter3EpisodeContent.balloonWordCatalogPairs()
                    Chapter6LetterPoolSpec -> Chapter6Config.balloonWordCatalogPairs()
                    TrainingV1LetterPoolSpec -> TrainingV1Config.balloonWordCatalogPairs()
                    else -> Chapter3EpisodeContent.balloonWordCatalogPairs()
                }
            require(all.size >= 5) { "Pop-all-letters balloons needs at least 5 words; got ${all.size}" }
            all.shuffled(rnd).distinctBy { it.first }.take(5)
        } else {
            null
        }

    fun chapter3PopAllLettersCurrentWord(): Pair<String, String>? {
        val list = chapter3PopAllLettersWords ?: return null
        return list[currentIndex.coerceIn(0, list.lastIndex)]
    }

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
            Chapter5LetterPoolSpec -> Chapter5Config.letters
            else -> letterPoolSpec.groups.flatten().distinct()
        }

    data class HighlightedLetterInWordRound(
        val word: String,
        val catalogId: String,
        val slotIndex: Int,
        val correctLetter: String,
    )

    fun highlightedLetterInWordRound(questionIndex: Int = currentIndex): HighlightedLetterInWordRound? {
        if (!plan.highlightedLetterInWordPickLetter) return null
        return if (letterPoolSpec === Chapter6LetterPoolSpec) {
            val r = Chapter6Config.pickSpellRound(questionIndex)
            HighlightedLetterInWordRound(
                word = r.word,
                catalogId = r.catalogId,
                slotIndex = r.slotIndex,
                correctLetter = r.correctLetter,
            )
        } else {
            val r = Chapter3EpisodeContent.pickSpellRound(questionIndex)
            HighlightedLetterInWordRound(
                word = r.word,
                catalogId = r.catalogId,
                slotIndex = r.slotIndex,
                correctLetter = r.correctLetter,
            )
        }
    }

    fun highlightedLetterInWordCompletesWordAfterCorrectRound(questionIndex: Int = currentIndex): Boolean {
        val r = highlightedLetterInWordRound(questionIndex) ?: return false
        return r.slotIndex == r.word.lastIndex
    }

    private fun pictureStartsWithOptionLetters(correctLetter: String): List<String> {
        val pool = episodeOptionLetters().distinct()
        val desired =
            (plan.optionCount ?: pool.size)
                .coerceAtMost(6)
                .coerceAtLeast(2)
                .coerceAtMost(pool.size)
        if (pool.size <= desired) return pool
        val distractors = pool.filter { it != correctLetter }.shuffled(rnd)
        return (listOf(correctLetter) + distractors.take((desired - 1).coerceAtLeast(1))).distinct()
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
                            val targetLetter =
                                if (letterPoolSpec === Chapter3LetterPoolSpec) {
                                    Chapter3EpisodeContent.gridTargetLetter(currentIndex)
                                } else {
                                    nextBalancedCorrect(group)
                                }
                            FindLetterGridGenerator.generate(
                                rnd = rnd,
                                group = group,
                                targetLetter = targetLetter,
                                maxTargetCount =
                                    when {
                                        plan.findLetterGridMaxTargetCount != null -> plan.findLetterGridMaxTargetCount
                                        letterPoolSpec === Chapter3LetterPoolSpec -> 4
                                        letterPoolSpec === Chapter1LetterPoolSpec ||
                                            letterPoolSpec === Chapter2LetterPoolSpec -> 4
                                        else -> null
                                    },
                            )
                        }
                        StationQuizMode.PickLetter -> {
                            if (plan.highlightedLetterInWordPickLetter) {
                                val round =
                                    highlightedLetterInWordRound(currentIndex)
                                        ?: error("Expected highlighted-in-word round when plan.highlightedLetterInWordPickLetter=true")
                                val optionCount = plan.optionCount ?: 6
                                val pool = episodeOptionLetters().distinct()
                                val options =
                                    buildList {
                                        add(round.correctLetter)
                                        addAll(
                                            pool
                                                .filter { it != round.correctLetter }
                                                .shuffled(rnd)
                                                .take((optionCount - 1).coerceAtLeast(1)),
                                        )
                                    }.distinct()
                                val padded =
                                    if (options.size >= optionCount) {
                                        options.take(optionCount)
                                    } else {
                                        val extra =
                                            pool
                                                .filter { it !in options }
                                                .shuffled(rnd)
                                                .take(optionCount - options.size)
                                        options + extra
                                    }
                                Question.PopBalloonsQuestion(
                                    correctAnswer = round.correctLetter,
                                    options = padded,
                                )
                            } else {
                                val optionCount = plan.optionCount ?: 6
                                if (plan.chapter3AudioLetterRecognition) {
                                    val correct = nextBalancedCorrect(group)
                                    popGenerator.generatePickLetterOptions(
                                        rnd = rnd,
                                        group = group,
                                        correctAnswer = correct,
                                        optionCount = optionCount,
                                    )
                                } else if (letterPoolSpec === Chapter3LetterPoolSpec && plan.chapter3FindAnyLetterInWordPickLetter) {
                                    val (word, _) = Chapter3EpisodeContent.findAnyInWord(currentIndex)
                                    val inWord =
                                        word.toCharArray().map { it.toString() }.distinct()
                                    val distractors =
                                        Chapter3Config.letters
                                            .filter { it !in inWord }
                                            .shuffled(rnd)
                                            .take((optionCount - inWord.size).coerceAtLeast(1))
                                    val options = (inWord + distractors).take(optionCount)
                                    Question.PopBalloonsQuestion(
                                        // Placeholder; GameScreen will accept ANY in-word letter as correct for this station.
                                        correctAnswer = options.first(),
                                        options = options,
                                    )
                                } else if (letterPoolSpec === Chapter3LetterPoolSpec) {
                                    val round = Chapter3EpisodeContent.pickSpellRound(currentIndex)
                                    popGenerator.generatePickLetterOptions(
                                        rnd = rnd,
                                        group = group,
                                        correctAnswer = round.correctLetter,
                                        optionCount = optionCount,
                                    )
                                } else {
                                    val correct = nextBalancedCorrect(group)
                                    popGenerator.generatePickLetterOptions(
                                        rnd = rnd,
                                        group = group,
                                        correctAnswer = correct,
                                        optionCount = plan.optionCount ?: 3,
                                    )
                                }
                            }
                        }
                        StationQuizMode.PopBalloons -> {
                            if (plan.popAllLettersInWord) {
                                val (word, _) =
                                    chapter3PopAllLettersCurrentWord()
                                        ?: error("Expected Chapter 3 pop-all-letters words to be initialized")
                                // Chapter 3 station 3: balloons must include letter occurrences (e.g. "שמש" has two "ש").
                                val correctBalloons = word.toCharArray().map { it.toString() }
                                val optionCount = plan.optionCount ?: 10
                                val inWord = correctBalloons.toSet()
                                val pool = episodeOptionLetters().distinct()
                                val distractors =
                                    pool
                                        .filter { it !in inWord }
                                        .shuffled(rnd)
                                        .take((optionCount - correctBalloons.size).coerceAtLeast(1))
                                val options = (correctBalloons + distractors).take(optionCount).shuffled(rnd)
                                Question.PopBalloonsQuestion(
                                    // Used for help-hint (one of the in-word letters); multi-correct lives in UI.
                                    correctAnswer = correctBalloons.firstOrNull().orEmpty(),
                                    options = options,
                                )
                            } else {
                                val correct =
                                    if (letterPoolSpec === Chapter3LetterPoolSpec) {
                                        Chapter3EpisodeContent.balloonFirstLetter(currentIndex)
                                    } else {
                                        nextBalancedCorrect(group)
                                    }
                                if (letterPoolSpec === Chapter3LetterPoolSpec) {
                                    popGenerator.generateWithRepeatedCorrect(
                                        rnd = rnd,
                                        group = group,
                                        correctAnswer = correct,
                                        optionCount = plan.optionCount ?: 10,
                                        correctBalloonCountRange = 1..3,
                                    )
                                } else {
                                    popGenerator.generate(
                                        rnd = rnd,
                                        group = group,
                                        correctAnswer = correct,
                                        optionCount = plan.optionCount ?: 7,
                                        correctBalloonCountRange =
                                            if (letterPoolSpec === TrainingV1LetterPoolSpec) {
                                                2..3
                                            } else {
                                                null
                                            },
                                    )
                                }
                            }
                        }
                        StationQuizMode.PictureStartsWith -> {
                            val correct = nextBalancedCorrect(group)
                            val used = lessonWordUsedCorrectIdsByLetter.getOrPut(correct) { mutableSetOf() }
                            val optionLetters = pictureStartsWithOptionLetters(correctLetter = correct)
                            val q =
                                if (letterPoolSpec === Chapter3LetterPoolSpec) {
                                    Chapter3LessonGenerators.pictureStartsWith(
                                        rnd = rnd,
                                        group = group,
                                        targetLetter = correct,
                                        excludeCorrectWordIds = used,
                                        optionLetters = optionLetters,
                                    )
                                } else if (letterPoolSpec === Chapter6LetterPoolSpec) {
                                    Chapter6LessonGenerators.pictureStartsWith(
                                        rnd = rnd,
                                        group = group,
                                        targetLetter = correct,
                                        excludeCorrectWordIds = used,
                                        optionLetters = optionLetters,
                                    )
                                } else {
                                    Chapter1LessonGenerators.pictureStartsWith(
                                        rnd = rnd,
                                        group = group,
                                        targetLetter = correct,
                                        excludeCorrectWordIds = used,
                                        optionLetters = optionLetters,
                                    )
                                }
                            used.add(q.catalogEntryId)
                            q
                        }
                        StationQuizMode.ImageMatch -> {
                            val correct = nextBalancedCorrect(group)
                            val used = lessonWordUsedCorrectIdsByLetter.getOrPut(correct) { mutableSetOf() }
                            val q =
                                if (letterPoolSpec === Chapter3LetterPoolSpec) {
                                    Chapter3LessonGenerators.imageMatch(
                                        rnd = rnd,
                                        group = group,
                                        targetLetter = correct,
                                        excludeCorrectWordIds = used,
                                        alwaysThreeChoices = plan.imageMatchAlwaysThreeChoices,
                                        totalChoiceCount = plan.imageMatchChoiceCount,
                                    )
                                } else if (letterPoolSpec === Chapter6LetterPoolSpec) {
                                    Chapter6LessonGenerators.imageMatch(
                                        rnd = rnd,
                                        group = group,
                                        targetLetter = correct,
                                        excludeCorrectWordIds = used,
                                        alwaysThreeChoices = plan.imageMatchAlwaysThreeChoices,
                                        totalChoiceCount = plan.imageMatchChoiceCount,
                                    )
                                } else {
                                    Chapter1LessonGenerators.imageMatch(
                                        rnd = rnd,
                                        group = group,
                                        targetLetter = correct,
                                        excludeCorrectWordIds = used,
                                        alwaysThreeChoices = plan.imageMatchAlwaysThreeChoices,
                                        forbidAutoAndCarTogether = plan.chapter1Station6ForbidAutoAndCarTogether,
                                        forbidVehicleSynonymsTogether = plan.forbidVehicleSynonymsTogether,
                                    )
                                }
                            used.add(q.correctChoiceId)
                            q
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
        val second = nextBalancedCorrect(group)
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
