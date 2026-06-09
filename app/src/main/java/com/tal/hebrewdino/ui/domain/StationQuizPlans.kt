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
)

object StationQuizPlans {
    fun chapter1(stationId: Int): StationQuizPlan = Chapter1StationOrder.quizPlan(stationId)

    /** Chapters 2–4 reuse the same six-station plan as chapter 1 ([Chapter1StationOrder]); letters/art/intros differ. */
    fun chapter2(stationId: Int): StationQuizPlan = Chapter1StationOrder.quizPlan(stationId)

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
        Chapter1StationOrder.quizPlan(stationId)
            .copy(listenOnlyTargetPrompt = false)
            .let { base ->
                when (stationId) {
                    Chapter1StationOrder.TAP_LETTER ->
                        base.copy(
                            optionCount = 5
                        )
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
            }

    fun chapter6(stationId: Int): StationQuizPlan =
        chapter3(stationId)

    fun trainingV1(stationId: Int): StationQuizPlan =
        when (stationId) {
            TrainingV1Config.STATION_HEAR_LETTER_CHOOSE ->
                StationQuizPlan(
                    mode = StationQuizMode.PickLetter,
                    questionCount = 1,
                    initialGroupIndex = 0,
                    optionCount = 6,
                    listenOnlyTargetPrompt = true,
                    sortOptionLetters = true,
                )
            TrainingV1Config.STATION_WHICH_WORD_STARTS_WITH_LETTER ->
                StationQuizPlan(
                    mode = StationQuizMode.ImageMatch,
                    questionCount = 1,
                    initialGroupIndex = 0,
                    imageMatchChoiceCount = 3,
                    imageMatchAlwaysThreeChoices = true,
                    imageMatchCaptionSizeMultiplier = 2.0f,
                    imageMatchPictureSizeMultiplier = 1.15f,
                )
            TrainingV1Config.STATION_PICTURE_CHOOSE_WORD ->
                StationQuizPlan(
                    mode = StationQuizMode.ImageMatch,
                    questionCount = 1,
                    initialGroupIndex = 0,
                    imageMatchChoiceCount = 3,
                    imageMatchAlwaysThreeChoices = true,
                    imageMatchCaptionSizeMultiplier = 1.05f,
                    imageMatchPictureSizeMultiplier = 1.05f,
                )
            TrainingV1Config.STATION_FIND_HEARD_LETTER_IN_GRID ->
                StationQuizPlan(
                    mode = StationQuizMode.FindLetterGrid,
                    questionCount = 1,
                    initialGroupIndex = 0,
                    listenOnlyTargetPrompt = true,
                    findLetterGridMaxTargetCount = 4,
                )
            TrainingV1Config.STATION_WORD_BALLOONS ->
                StationQuizPlan(
                    mode = StationQuizMode.PopBalloons,
                    questionCount = 1,
                    initialGroupIndex = 0,
                    popAllLettersInWord = false,
                    optionCount = 10,
                )
            TrainingV1Config.STATION_MATCH_LETTER_TO_WORD ->
                StationQuizPlan(
                    mode = StationQuizMode.ImageMatch,
                    questionCount = 1,
                    initialGroupIndex = 0,
                    imageMatchChoiceCount = 3,
                    imageMatchAlwaysThreeChoices = true,
                    imageMatchCaptionSizeMultiplier = 1.5f,
                    imageMatchPictureSizeMultiplier = 1f,
                    forbidVehicleSynonymsTogether = true,
                )
            else -> error("Unknown Training v1 stationId=$stationId")
        }

    // Season 2 stations are routed explicitly from Season2 screens; no shared plan entry needed here.
}
