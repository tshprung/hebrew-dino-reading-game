package com.tal.hebrewdino.ui.domain

data class StationQuizPlan(
    val mode: StationQuizMode,
    val questionCount: Int,
    val initialGroupIndex: Int,
    /** Chapter 1 station 5: always three picture cards. */
    val imageMatchAlwaysThreeChoices: Boolean = false,
    /** Multiplier on word caption size under image-match cards (e.g. 1.5f for station 5). */
    val imageMatchCaptionSizeMultiplier: Float = 1f,
    /** Multiplier on image card width/height (e.g. 2f for station 5). */
    val imageMatchPictureSizeMultiplier: Float = 1f,
    /** When set (e.g. Episode 3 station 6), image match uses exactly this many picture choices (1 correct + rest distractors). */
    val imageMatchChoiceCount: Int? = null,
    /**
     * When [mode] is [StationQuizMode.PickLetter] or [StationQuizMode.PopBalloons],
     * optional balloon/option count (default 3 in [LevelSession] for PickLetter, 7 for PopBalloons).
     */
    val optionCount: Int? = null,
    /** Episode 3 station 2: word-analysis rounds (custom prompt/UI tweaks, but still single-pick). */
    val chapter3WordAnalysisPickLetter: Boolean = false,
    /** Highlighted-letter-in-word rounds (single-pick per highlighted letter). */
    val highlightedLetterInWordPickLetter: Boolean = false,
    /** Episode 3 station 3: any-letter-in-word (multiple answers accepted in UI, but still single pick). */
    val chapter3FindAnyLetterInWordPickLetter: Boolean = false,
    /** Pop ALL letters that appear in the word (flattened into sequential PopBalloons rounds). */
    val popAllLettersInWord: Boolean = false,
    /** Episode 3 station 5: audio-only letter recognition (play a letter, user taps it). */
    val chapter3AudioLetterRecognition: Boolean = false,
    /** Chapter 1 station 6: forbid showing both "אוטו" and "מכונית" in the same round. */
    val chapter1Station6ForbidAutoAndCarTogether: Boolean = false,
    /** For Training: forbid showing any pair among "אוטו" / "מכונית" / "רכב" in the same round. */
    val forbidVehicleSynonymsTogether: Boolean = false,
    /**
     * Six-station arc (chapters 4–5): hide the written target letter/word where episode 1–2 would show it;
     * prompts rely on pre-recorded letter/word audio instead.
     */
    val listenOnlyTargetPrompt: Boolean = false,
    /**
     * When set (Episode 4 station 3 only), caps how many target-letter cells appear in the find-grid question.
     */
    val findLetterGridMaxTargetCount: Int? = null,
    /**
     * UI-only: sort letter option buttons (e.g. Chapter 3 station 1 picture-first-letter).
     * Does not affect [LevelSession] or question generation.
     */
    val sortOptionLetters: Boolean = false,
    /**
     * When set, [LevelSession] generates Season 2 advanced questions instead of the legacy path.
     * Season 1 and Season 2 Chapters 1–2 never set this flag.
     */
    val season2AdvancedMode: Season2AdvancedStationMode? = null,
    /** Validated catalog ids for [season2AdvancedMode] question generation. */
    val season2WordCatalogIds: List<String>? = null,
    /** Letter pool for missing-first-letter distractors. */
    val season2AdvancedDistractorLetters: List<String> = emptyList(),
    /** Lightweight theme hook for advanced-station copy. */
    val season2StationTheme: Season2StationTheme = Season2StationTheme.Standard,
    /** Word-parts presentation ramp (visible / guided / hidden). */
    val season2WordPartsPresentationMode: Season2WordPartsPresentationMode? = null,
    /** When set with [season2WordPartsStationId], selects a station-specific word-parts pool. */
    val season2WordPartsStationChapterIndex: Int? = null,
    val season2WordPartsStationId: Int? = null,
    /** When set with [season2RhymeStationId], selects a station-specific rhyme pair list. */
    val season2RhymeStationChapterIndex: Int? = null,
    val season2RhymeStationId: Int? = null,
    /** When [mode] is [StationQuizMode.DragWordToPicture], number of picture/word pairs (2 or 3). */
    val dragWordToPicturePairCount: Int? = null,
    /** When [mode] is [StationQuizMode.DragMissingLetter], 0-based grapheme index of the missing letter. */
    val dragMissingLetterIndex: Int? = null,
)

object StationQuizPlans {
    fun chapter1(stationId: Int): StationQuizPlan = Chapter1StationOrder.quizPlan(stationId)

    fun chapter2(stationId: Int): StationQuizPlan =
        Chapter2StationOrder.quizPlan(stationId).let { plan ->
            when (stationId) {
                1 ->
                    plan.copy(optionCount = 5)
                3 ->
                    plan.copy(
                        optionCount = 5,
                        sortOptionLetters = true,
                    )
                else -> plan
            }
        }

    fun chapter3(stationId: Int): StationQuizPlan =
        Chapter3StationOrder.quizPlan(stationId).let { plan ->
            when (stationId) {
                1 -> plan.copy( // Ch3 st1 picture starts-with
                    optionCount = 5,
                    sortOptionLetters = true
                )
                5 -> plan.copy( // Ch3 st5 audio recognition
                    optionCount = 6,
                    sortOptionLetters = true,
                    listenOnlyTargetPrompt = true
                )
                else -> plan
            }
        }

    fun chapter4(stationId: Int): StationQuizPlan {
        val base = Chapter1StationOrder.quizPlan(stationId).copy(listenOnlyTargetPrompt = false)
        val plan = when (stationId) {
            Chapter1StationOrder.REVEAL_THEN_CHOOSE ->
                base.copy(
                    findLetterGridMaxTargetCount = 4
                )
            Chapter1StationOrder.PICTURE_PICK_ONE ->
                base.copy(
                    optionCount = 5,
                    sortOptionLetters = true
                )
            else -> base
        }
        return if (stationId == Chapter1StationOrder.TAP_LETTER) {
            plan.copy(
                optionCount = 5
            )
        } else plan
    }

    fun chapter5(stationId: Int): StationQuizPlan =
        Chapter5StationOrder.quizPlan(stationId)
            .copy(listenOnlyTargetPrompt = false)
            .let { base ->
                when (stationId) {
                    1 ->
                        base.copy(
                            optionCount = 5,
                        )
                    3 ->
                        base.copy(
                            findLetterGridMaxTargetCount = 4,
                        )
                    4 ->
                        base.copy(
                            optionCount = 5,
                            sortOptionLetters = true,
                        )
                    else -> base
                }
            }

    fun chapter6(stationId: Int): StationQuizPlan =
        Chapter6StationOrder.quizPlan(stationId).let { plan ->
            when (stationId) {
                1 ->
                    plan.copy(
                        optionCount = 5,
                        sortOptionLetters = true,
                    )
                5 ->
                    plan.copy(
                        optionCount = 6,
                        sortOptionLetters = true,
                        listenOnlyTargetPrompt = true,
                    )
                else -> plan
            }
        }

    fun trainingV1(stationId: Int): StationQuizPlan = TrainingV1SourceStation.sourceQuizPlan(stationId)

    // Season 2 stations are routed explicitly from Season2 screens; no shared plan entry needed here.
}
