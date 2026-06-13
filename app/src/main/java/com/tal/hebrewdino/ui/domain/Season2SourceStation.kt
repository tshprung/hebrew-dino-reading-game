package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.BALLOON_POP
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.PICTURE_PICK_ALL
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.PICTURE_PICK_ONE
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder.TAP_LETTER
import com.tal.hebrewdino.ui.domain.Season2Chapter1StationOrder.FINALE_STATION
import com.tal.hebrewdino.ui.domain.Season2Chapter1StationOrder.MEMORY_MATCH
import com.tal.hebrewdino.ui.domain.Season2Chapter1StationOrder.PICTURE_STARTS_WITH
import com.tal.hebrewdino.ui.domain.Season2Chapter1StationOrder.PICK_LETTER
import com.tal.hebrewdino.ui.domain.Season2Chapter1StationOrder.POP_BALLOONS
import com.tal.hebrewdino.ui.domain.Season2Chapter1StationOrder.WHICH_WORD_STARTS_WITH
import com.tal.hebrewdino.ui.domain.Season2ChapterStationPlans.StationKind

/**
 * Maps Season 2 gameplay stations (chapterId 101–107) to the first equivalent Season 1 station.
 * Used for UI spec, quiz-plan shape, audio staging, and coach replay parity.
 *
 * Stations with no S1 equivalent (MemoryMatch, WordParts, Rhyming) return null from [canonicalSource].
 */
object Season2SourceStation {
    fun isGameplayChapter(chapterId: Int): Boolean =
        Season2StationAudio.isSeason2GameplayChapter(chapterId)

    fun chapterIndex(gameplayChapterId: Int): Int = gameplayChapterId - 100

    fun stationKind(gameplayChapterId: Int, stationId: Int): StationKind? {
        if (!isGameplayChapter(gameplayChapterId)) return null
        val chapterIndex = chapterIndex(gameplayChapterId)
        return when (chapterIndex) {
            1 ->
                when (stationId) {
                    POP_BALLOONS -> StationKind.PopBalloons
                    PICK_LETTER -> StationKind.PickLetter
                    PICTURE_STARTS_WITH -> StationKind.PictureStartsWith
                    MEMORY_MATCH -> StationKind.DragWordToPicture
                    WHICH_WORD_STARTS_WITH -> StationKind.WhichWordStartsWith
                    FINALE_STATION -> StationKind.DragMissingLetter
                    else -> null
                }
            2 ->
                when (stationId) {
                    1 -> StationKind.PickLetter
                    2 -> StationKind.DragWordToPicture
                    PICTURE_STARTS_WITH -> StationKind.PictureStartsWith
                    MEMORY_MATCH -> StationKind.MemoryMatch
                    5 -> StationKind.DragMissingLetter
                    FINALE_STATION -> StationKind.WordParts
                    else -> null
                }
            in 3..7 -> Season2ChapterStationPlans.stationKind(chapterIndex, stationId)
            else -> null
        }
    }

    /** First S1 occurrence for this station kind, or null when there is no saga equivalent. */
    fun canonicalSource(kind: StationKind): Pair<Int, Int>? =
        when (kind) {
            StationKind.PopBalloons -> 1 to BALLOON_POP
            StationKind.PickLetter -> 1 to TAP_LETTER
            StationKind.PictureStartsWith -> 1 to PICTURE_PICK_ONE
            StationKind.WhichWordStartsWith -> 1 to PICTURE_PICK_ALL
            StationKind.MatchLetterToWord -> 1 to FINALE_PICTURE_LETTER_MATCH
            StationKind.DragWordToPicture -> 3 to 3
            StationKind.DragMissingLetter -> 5 to 2
            StationKind.PictureToWord -> 3 to 6
            StationKind.MemoryMatch,
            StationKind.WordParts,
            StationKind.Rhyming,
            StationKind.MissingFirstLetter,
            -> null
        }

    fun canonicalSource(gameplayChapterId: Int, stationId: Int): Pair<Int, Int>? {
        val kind = stationKind(gameplayChapterId, stationId) ?: return null
        return canonicalSource(kind)
    }

    fun sourceQuizPlan(sourceChapterId: Int, sourceStationId: Int): StationQuizPlan =
        when (sourceChapterId) {
            1 -> StationQuizPlans.chapter1(sourceStationId)
            2 -> StationQuizPlans.chapter2(sourceStationId)
            3 -> StationQuizPlans.chapter3(sourceStationId)
            4 -> StationQuizPlans.chapter4(sourceStationId)
            5 -> StationQuizPlans.chapter5(sourceStationId)
            6 -> StationQuizPlans.chapter6(sourceStationId)
            else -> error("Unsupported sourceChapterId=$sourceChapterId")
        }

    /**
     * Aligns S2 plan shape (round counts, option counts, image-match flags) to the canonical S1 source.
     * Preserves S2 content fields (word catalogs, letters, themes, advanced modes).
     */
    fun alignQuizPlanToSource(
        gameplayChapterId: Int,
        stationId: Int,
        plan: StationQuizPlan,
    ): StationQuizPlan {
        val (sourceChapterId, sourceStationId) = canonicalSource(gameplayChapterId, stationId) ?: return plan
        val source = sourceQuizPlan(sourceChapterId, sourceStationId)
        return plan.copy(
            questionCount = source.questionCount,
            initialGroupIndex = source.initialGroupIndex,
            optionCount = source.optionCount,
            imageMatchAlwaysThreeChoices = source.imageMatchAlwaysThreeChoices,
            imageMatchChoiceCount = source.imageMatchChoiceCount,
            imageMatchCaptionSizeMultiplier = source.imageMatchCaptionSizeMultiplier,
            imageMatchPictureSizeMultiplier = source.imageMatchPictureSizeMultiplier,
            sortOptionLetters = source.sortOptionLetters,
            listenOnlyTargetPrompt = source.listenOnlyTargetPrompt,
            dragWordToPicturePairCount =
                source.dragWordToPicturePairCount ?: plan.dragWordToPicturePairCount,
            dragMissingLetterIndex = source.dragMissingLetterIndex ?: plan.dragMissingLetterIndex,
            findLetterGridMaxTargetCount = source.findLetterGridMaxTargetCount,
            highlightedLetterInWordPickLetter = source.highlightedLetterInWordPickLetter,
            chapter3AudioLetterRecognition = source.chapter3AudioLetterRecognition,
            chapter3WordAnalysisPickLetter = source.chapter3WordAnalysisPickLetter,
            popAllLettersInWord = source.popAllLettersInWord,
            chapter1Station6ForbidAutoAndCarTogether = source.chapter1Station6ForbidAutoAndCarTogether,
            forbidVehicleSynonymsTogether = source.forbidVehicleSynonymsTogether,
        )
    }

    /** Resolves S2/training ids to canonical S1 ids for audio/QA; identity when no mapping. */
    fun resolveForBehavior(chapterId: Int, stationId: Int): Pair<Int, Int> {
        if (chapterId == TrainingV1Config.CHAPTER_ID) {
            return TrainingV1SourceStation.resolve(chapterId, stationId)
        }
        return canonicalSource(chapterId, stationId) ?: (chapterId to stationId)
    }

    fun hasParitySource(gameplayChapterId: Int, stationId: Int): Boolean =
        canonicalSource(gameplayChapterId, stationId) != null

    /** Maps S2 UI spec ids to canonical S1 ids so coach drag replay uses saga audio paths. */
    fun coachReplayUiSpec(stationUiSpec: StationUiSpec): StationUiSpec {
        val (sourceChapterId, sourceStationId) =
            canonicalSource(stationUiSpec.chapterId, stationUiSpec.stationId)
                ?: return stationUiSpec
        return stationUiSpec.copy(
            chapterId = sourceChapterId,
            stationId = sourceStationId,
        )
    }

    private val uniqueKinds =
        setOf(
            StationKind.MemoryMatch,
            StationKind.WordParts,
            StationKind.Rhyming,
            StationKind.MissingFirstLetter,
        )

    fun shouldAlignQuizPlan(kind: StationKind): Boolean = kind !in uniqueKinds
}
