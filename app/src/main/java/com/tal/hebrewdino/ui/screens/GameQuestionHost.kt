package com.tal.hebrewdino.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.components.TargetLetterHeaderChip
import com.tal.hebrewdino.ui.domain.AnswerResult
import com.tal.hebrewdino.ui.domain.Chapter1Station5And6ImageMatchInnerScale
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.InstructionPanelStyle
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.StationBehaviorRegistry
import com.tal.hebrewdino.ui.domain.StationInstructionCopy
import com.tal.hebrewdino.ui.domain.StationQuizMode
import com.tal.hebrewdino.ui.domain.StationQuizPlan
import com.tal.hebrewdino.ui.domain.StationTemplateId
import com.tal.hebrewdino.ui.domain.TrainingV1Config
import com.tal.hebrewdino.ui.game.ChildGameAudioHooks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

internal data class GameQuestionHostUi(
    val phase: GamePhase,
    val stationUiSpec: com.tal.hebrewdino.ui.domain.StationUiSpec,
    val stationId: Int,
    val chapterId: Int,
    val plan: StationQuizPlan,
    val listenOnly: Boolean,
    val sagaUsesPickLetterAudioStaging: Boolean,
    val sagaUsesPopBalloonsAudioStaging: Boolean,
    val sagaUsesFindGridAudioStaging: Boolean,
    val isChapter3HighlightedLetterInWordStation: Boolean,
    val isChapter3AudioLetterRecognitionStation: Boolean,
    val isChapter3PopAllLettersStation: Boolean,
    val highlightedInWordWord: String?,
    val highlightedInWordSlotIndex: Int?,
    val audioEnabled: Boolean,
    val isCompactLandscapePhone: Boolean,
    val episode4HelpSt15: Boolean,
    val episode4HelpActiveHintLetter: String?,
    val episode4HelpStation2BalloonHintEpoch: Int,
    val episode4HelpStation3GridHintEpoch: Int,
    val popBalloonsHelpControlsEnabled: Boolean,
    val balloonHelpHintLetter: String?,
    val showPopBalloonsTargetLetterChip: Boolean,
)

internal data class GameQuestionHostState(
    val station1PinnedCorrectLetter: String?,
    val station2PinnedBalloonLetter: String?,
    val station2PinnedBalloonColor: Color?,
    val hintHeaderScale: Float,
    val enabled: Boolean,
    val shakeEpoch: Int,
    val wrongTapsThisQuestion: Int,
    val hintPulseEpoch: Int,
    val correctTapPulseLetter: String?,
    val correctTapPulseEpoch: Int,
    val station4WrongFlashLetter: String?,
    val station4WrongFlashEpoch: Int,
    val station4PinnedCorrectLetter: String?,
    val entryPulseEpoch: Int,
    val entryPulseScale: Float,
    val optionsShakePx: Float,
)

internal data class GameQuestionHostDeps(
    val session: LevelSession,
    val scope: CoroutineScope,
    val voice: VoicePlayer,
    val sfx: SoundPoolPlayer,
    val cancelFeedbackVoice: () -> Unit,
    val getFeedbackVoiceJob: () -> Job?,
    val setFeedbackVoiceJob: (Job?) -> Unit,
    val consumeTapCooldown: () -> Boolean,
)

internal data class GameQuestionHostHandlers(
    val performSideHelpReplay: () -> Unit,
    val handleFindGridSagaGridLetterTapped: (tapped: String, question: Question.FindLetterGridQuestion) -> Unit,
    val handleFindGridCellTapped: (index: Int, question: Question.FindLetterGridQuestion) -> Unit,
    val handleFindGridCompleted: () -> Unit,
    val handlePickLetterPick: (picked: String) -> Unit,
    val handlePopBalloonsPopSfx: suspend (letter: String, isCorrect: Boolean, finalCorrectBalloon: Boolean, balloonIndex: Int) -> Unit,
    val handlePopBalloonsWrongPick: () -> Unit,
    val handlePopBalloonsAllCorrectPopped: (lastLetter: String, poppedBalloonColor: Color, isChapter3PopAllLettersStation: Boolean) -> Unit,
    val handlePictureStartsWithPick: (picked: String) -> Unit,
    val handleImageToWordReplayCorrectChoice: () -> Unit,
    val handleImageToWordWordPressed: (choiceId: String) -> Unit,
    val handleImageToWordAttempt: (choiceId: String) -> Boolean,
    val handleImageMatchAttempt: (choiceId: String) -> Boolean,
    val handleFinaleWrongPlacement: () -> Unit,
    val onWrongFeedback: () -> Unit,
    val advanceAfterRound: suspend (isLast: Boolean) -> Unit,
)

@Composable
internal fun GameQuestionHost(
    ui: GameQuestionHostUi,
    state: GameQuestionHostState,
    deps: GameQuestionHostDeps,
    handlers: GameQuestionHostHandlers,
) {
    val current = deps.session.currentQuestion ?: return
    if (ui.phase == GamePhase.Intro) {
        if (ui.stationUiSpec.showBetweenRoundIntroPulse) {
            IntroPulse(stationId = ui.stationId, question = current, modifier = Modifier.fillMaxWidth())
        }
        return
    }

    when (current) {
        is Question.FindLetterGridQuestion -> {
            FindLetterGridQuestionRenderer(
                current = current,
                listenOnly = ui.listenOnly,
                isSagaRevealStation = ui.stationUiSpec.findGridSagaRevealStation,
                sagaUsesFindGridAudioStaging = ui.sagaUsesFindGridAudioStaging,
                stationUiSpec = ui.stationUiSpec,
                chapter3ContextWordHint =
                    StationBehaviorRegistry.findGridContextWordHint(
                        stationUiSpec = ui.stationUiSpec,
                        questionIndex = deps.session.currentIndex,
                        sagaUsesFindGridAudioStaging = ui.sagaUsesFindGridAudioStaging,
                    ),
                floatingTargetLetterHint =
                    if (ui.episode4HelpSt15 && ui.stationUiSpec.findGridUseEpisode4HelpHints) {
                        ui.episode4HelpActiveHintLetter
                    } else {
                        null
                    },
                episode4TargetCellsHintEpoch =
                    if (ui.episode4HelpSt15 && ui.stationUiSpec.findGridUseEpisode4HelpHints) {
                        ui.episode4HelpStation3GridHintEpoch
                    } else {
                        0
                    },
                hintPulseEpoch = state.hintPulseEpoch,
                enabled = state.enabled,
                contentKey = deps.session.currentIndex,
                entryPulseScale = state.entryPulseScale,
                optionsShakePx = state.optionsShakePx,
                onSagaGridLetterTapped =
                    if (ui.sagaUsesFindGridAudioStaging) {
                        { tapped -> handlers.handleFindGridSagaGridLetterTapped(tapped, current) }
                    } else {
                        null
                    },
                onCorrectTap =
                    if (ui.audioEnabled && !ui.sagaUsesFindGridAudioStaging) {
                        {
                            deps.scope.launch {
                                deps.sfx.playFirstAvailable(AudioClips.SfxCorrect, volume = 0.58f)
                            }
                        }
                    } else {
                        null
                    },
                onCellTapped = { index -> handlers.handleFindGridCellTapped(index, current) },
                onCompleted = { handlers.handleFindGridCompleted() },
                modifier = Modifier.fillMaxSize(),
            )
        }
        is Question.PopBalloonsQuestion -> {
            if (ui.plan.mode == StationQuizMode.PickLetter) {
                PickLetterQuestionRenderer(
                    current = current,
                    stationUiSpec = ui.stationUiSpec,
                    listenOnly = ui.listenOnly,
                    isSagaEpisode = isSagaEpisode(ui.chapterId),
                    sagaUsesPickLetterAudioStaging = ui.sagaUsesPickLetterAudioStaging,
                    chapterId = ui.chapterId,
                    stationId = ui.stationId,
                    highlightedInWordWord = ui.highlightedInWordWord,
                    highlightedInWordSlotIndex = ui.highlightedInWordSlotIndex,
                    isChapter3HighlightedLetterInWordStation = ui.isChapter3HighlightedLetterInWordStation,
                    isChapter3AudioLetterRecognitionStation = ui.isChapter3AudioLetterRecognitionStation,
                    station1PinnedCorrectLetter = state.station1PinnedCorrectLetter,
                    entryPulseScale = state.entryPulseScale,
                    enabled = state.enabled,
                    shakePx = state.optionsShakePx,
                    wrongTapsThisQuestion = state.wrongTapsThisQuestion,
                    hintPulseEpoch = state.hintPulseEpoch,
                    correctTapPulseLetter = state.correctTapPulseLetter,
                    correctTapPulseEpoch = state.correctTapPulseEpoch,
                    temporaryHintLetter = ui.episode4HelpActiveHintLetter,
                    onRepeatLetterClick = repeatLetter@{
                        if (!ui.audioEnabled) return@repeatLetter
                        deps.cancelFeedbackVoice()
                        val letter = current.correctAnswer
                        val clip = AudioClips.letterNameClip(letter) ?: return@repeatLetter
                        val job = deps.scope.launch { deps.voice.playBlocking(clip) }
                        deps.setFeedbackVoiceJob(job)
                    },
                    onPick = { picked -> handlers.handlePickLetterPick(picked) },
                )
            } else {
                val correctLetterSet =
                    if (ui.isChapter3PopAllLettersStation) {
                        val w = deps.session.chapter3PopAllLettersCurrentWord()?.first.orEmpty()
                        w.toCharArray().map { it.toString() }.toSet()
                    } else {
                        null
                    }
                val visualRoundSeed =
                    if (ui.sagaUsesPopBalloonsAudioStaging) {
                        deps.session.currentIndex
                    } else {
                        0
                    }
                val maxVisibleBalloonCount =
                    if (ui.isCompactLandscapePhone && ui.stationUiSpec.popBalloonsCompactLandscapePhoneTuning) 8 else null
                val episode4CorrectBalloonHintEpoch =
                    if (ui.episode4HelpSt15 && ui.stationId == Chapter1StationOrder.BALLOON_POP) {
                        ui.episode4HelpStation2BalloonHintEpoch
                    } else {
                        0
                    }
                val helpSideInsetDp = ui.stationUiSpec.balloonPlayAreaStartInsetDp.dp
                val popAllWordForBanner = deps.session.chapter3PopAllLettersCurrentWord()?.first.orEmpty()
                PopBalloonsQuestionRenderer(
                    current = current,
                    planMode = ui.plan.mode,
                    planPopAllLettersInWord = ui.plan.popAllLettersInWord,
                    popAllLettersWordForBanner = popAllWordForBanner,
                    popAllLettersBannerInstruction =
                        ui.stationUiSpec.popBalloonsPopAllLettersBannerInstruction
                            ?: StationInstructionCopy.PopBalloonsPopAllLettersInWord,
                    stationUiSpec = ui.stationUiSpec,
                    isCompactLandscapePhone = ui.isCompactLandscapePhone,
                    listenOnly = ui.listenOnly,
                    sagaUsesPopBalloonsAudioStaging = ui.sagaUsesPopBalloonsAudioStaging,
                    showPopBalloonsTargetLetterChip = ui.showPopBalloonsTargetLetterChip,
                    episode4HelpSt15 = ui.episode4HelpSt15,
                    episode4HelpActiveHintLetter = ui.episode4HelpActiveHintLetter,
                    hintHeaderScale = state.hintHeaderScale,
                    station2PinnedBalloonLetter = state.station2PinnedBalloonLetter,
                    station2PinnedBalloonColor = state.station2PinnedBalloonColor,
                    correctLetterSet = correctLetterSet,
                    enabled = state.enabled,
                    shakePx = state.optionsShakePx,
                    entryPulseScale = state.entryPulseScale,
                    visualRoundSeed = visualRoundSeed,
                    maxVisibleBalloonCount = maxVisibleBalloonCount,
                    episode4CorrectBalloonHintEpoch = episode4CorrectBalloonHintEpoch,
                    helpSideInsetDp = helpSideInsetDp,
                    contentTopPaddingDp =
                        if (ui.sagaUsesPopBalloonsAudioStaging) SixStationArcHalfCmNudge else 0.dp,
                    onBalloonPressed = { _ -> },
                    onPopSfx = handlers.handlePopBalloonsPopSfx,
                    onWrongPick = { handlers.handlePopBalloonsWrongPick() },
                    onAllCorrectPopped = { lastLetter, poppedBalloonColor ->
                        handlers.handlePopBalloonsAllCorrectPopped(
                            lastLetter,
                            poppedBalloonColor,
                            ui.isChapter3PopAllLettersStation,
                        )
                    },
                )
            }
        }
        is Question.PictureStartsWithQuestion -> {
            val pictureInstructionText =
                when {
                    ui.stationUiSpec.pictureStartsWithInstructionOverride != null ->
                        ui.stationUiSpec.pictureStartsWithInstructionOverride
                    ui.listenOnly && isSagaEpisode(ui.chapterId) && ui.stationId == Chapter1StationOrder.PICTURE_PICK_ONE ->
                        ui.stationUiSpec.pictureStartsWithListenOnlySagaInstruction
                            ?: StationInstructionCopy.PictureStartsWithListenFirstSaga
                    else ->
                        StationInstructionCopy.PictureStartsWithDefault
                }
            val pictureInstructionReadablePanel =
                ui.stationUiSpec.pictureStartsWithReadablePanel ||
                    ui.stationUiSpec.pictureStartsWithInstructionPanelStyle == InstructionPanelStyle.WhiteRounded
            val pictureShowWordCaption =
                !(ui.listenOnly && ui.stationUiSpec.hidePictureWordCaptionWhenListenOnlySaga)
            PictureStartsWithQuestionRenderer(
                current = current,
                stationUiSpec = ui.stationUiSpec,
                isCompactLandscapePhone = ui.isCompactLandscapePhone,
                instructionText = pictureInstructionText,
                instructionReadablePanel = pictureInstructionReadablePanel,
                showWordCaption = pictureShowWordCaption,
                onPictureTapReplayWord =
                    if (ui.episode4HelpSt15 && ui.stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                        { handlers.performSideHelpReplay() }
                    } else if (ui.audioEnabled) {
                        {
                            deps.cancelFeedbackVoice()
                            val wordPath = AudioClips.wordClipByCatalogId(current.catalogEntryId)
                            if (deps.voice.hasAsset(wordPath)) {
                                val job = deps.scope.launch { deps.voice.playBlocking(wordPath) }
                                deps.setFeedbackVoiceJob(job)
                            }
                        }
                    } else {
                        null
                    },
                temporaryStartingLetterHint =
                    if (ui.episode4HelpSt15 && ui.stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                        ui.episode4HelpActiveHintLetter
                    } else {
                        null
                    },
                pinnedCorrectLetter =
                    if (ui.chapterId == 4 && ui.stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                        state.station4PinnedCorrectLetter
                    } else {
                        null
                    },
                enabled = state.enabled,
                shakePx = state.optionsShakePx,
                entryPulseEpoch =
                    if (ui.chapterId == 6 && ui.stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                        0
                    } else {
                        state.entryPulseEpoch
                    },
                promptWordSizeMultiplier = ui.plan.imageMatchCaptionSizeMultiplier * 1.2f,
                innerPictureScale =
                    Chapter1Station5And6ImageMatchInnerScale.innerScalePictureStartsWith(
                        catalogEntryId = current.catalogEntryId,
                        letter = current.correctLetter,
                        word = current.word,
                        tintArgb = current.tintArgb,
                        tileDrawable = current.tileDrawable,
                    ),
                pictureSizeMultiplier = ui.plan.imageMatchPictureSizeMultiplier,
                sortOptionLetters = ui.plan.sortOptionLetters,
                chapterId = ui.chapterId,
                stationId = ui.stationId,
                hintCorrectLetter = current.correctLetter.takeIf { state.wrongTapsThisQuestion >= 2 },
                hintPulseEpoch = state.hintPulseEpoch,
                correctPulseLetter = state.correctTapPulseLetter,
                correctPulseEpoch = state.correctTapPulseEpoch,
                wrongFlashLetter = state.station4WrongFlashLetter,
                wrongFlashEpoch = state.station4WrongFlashEpoch,
                entryPulseScale = 1f,
                onPickLetter = { picked -> handlers.handlePictureStartsWithPick(picked) },
            )
        }
        is Question.ImageMatchQuestion ->
            if (ui.stationUiSpec.templateId == StationTemplateId.ImageToWord) {
                ImageToWordQuestionRenderer(
                    current = current,
                    contentKey = deps.session.currentIndex,
                    enabled = state.enabled,
                    entryPulseScale = state.entryPulseScale,
                    optionsShakePx = state.optionsShakePx,
                    instructionText =
                        ui.stationUiSpec.imageToWordInstructionText
                            ?: StationInstructionCopy.Chapter3ImageToWord,
                    onPictureTapReplayWord =
                        if (ui.audioEnabled) {
                            { handlers.handleImageToWordReplayCorrectChoice() }
                        } else {
                            null
                        },
                    onWordPressed = { choiceId -> handlers.handleImageToWordWordPressed(choiceId) },
                    onAttempt = { choiceId -> handlers.handleImageToWordAttempt(choiceId) },
                )
            } else if (
                ui.stationUiSpec.templateId == StationTemplateId.MatchLetterToWord
            ) {
                val matchChoices = current.choices
                fun speakNow(play: suspend () -> Unit) {
                    if (!ui.audioEnabled) return
                    deps.cancelFeedbackVoice()
                    val job = deps.scope.launch { play() }
                    deps.setFeedbackVoiceJob(job)
                }
                var lastPraiseClip by remember(ui.chapterId, ui.stationId) { mutableStateOf<String?>(null) }
                val matchPraiseClips =
                    listOf(
                        AudioClips.VoPraiseMetzuyan,
                        AudioClips.VoPraiseYofi,
                        AudioClips.VoPraiseHitzlacht,
                        AudioClips.VoNice1,
                        AudioClips.VoGoodJob2,
                        AudioClips.VoGoodJob1,
                    )

                fun handleMatchWordPressed(choiceId: String) {
                    speakNow {
                        val clip =
                            AudioClips.imageToWordClipByCatalogId(
                                catalogEntryId = choiceId,
                                chapterId = ui.chapterId,
                                voiceHasAsset = { path -> deps.voice.hasAsset(path) },
                            )
                        deps.voice.playBlocking(clip)
                    }
                }

                fun handleMatchLetterPressed(letter: String) {
                    val clip = AudioClips.letterNameClip(letter) ?: return
                    speakNow { deps.voice.playBlocking(clip) }
                }

                fun handleMatchSolved() {
                    if (!deps.consumeTapCooldown()) return
                    deps.scope.launch {
                        withTimeoutOrNull(4500L) { deps.getFeedbackVoiceJob()?.join() }
                        if (ui.audioEnabled) {
                            deps.cancelFeedbackVoice()
                            val job =
                                deps.scope.launch {
                                    val candidates = matchPraiseClips.filter { it != lastPraiseClip }
                                    val pickFrom =
                                        if (candidates.isNotEmpty()) candidates else listOfNotNull(lastPraiseClip)
                                    val picked = pickFrom.shuffled().firstOrNull()
                                    if (picked != null) {
                                        lastPraiseClip = picked
                                        deps.voice.playBlocking(picked)
                                    }
                                }
                            deps.setFeedbackVoiceJob(job)
                            withTimeoutOrNull(3000L) { job.join() }
                        }
                        when (deps.session.completeCurrentRound()) {
                            AnswerResult.Correct -> {
                                if (ui.audioEnabled) ChildGameAudioHooks.onCorrect()
                                val isLast = deps.session.currentIndex >= deps.session.totalQuestions - 1
                                handlers.advanceAfterRound(isLast)
                            }
                            else -> {}
                        }
                    }
                }
                MatchLetterToWordQuestionRenderer(
                    choices = matchChoices,
                    stationUiSpec = ui.stationUiSpec,
                    isCompactLandscapePhone = ui.isCompactLandscapePhone,
                    choicePairLimit = 3,
                    contentKey = deps.session.currentIndex,
                    enabled = state.enabled,
                    entryPulseScale = state.entryPulseScale,
                    letterTileSizeMultiplier = if (ui.chapterId == TrainingV1Config.CHAPTER_ID) 1.10f else 1f,
                    onWordPressed = { choiceId -> handleMatchWordPressed(choiceId) },
                    onLetterPressed = { letter -> handleMatchLetterPressed(letter) },
                    onCorrectMatch = { _ ->
                        if (ui.audioEnabled) {
                            deps.scope.launch { deps.sfx.playFirstAvailable(AudioClips.SfxCorrect, volume = 0.58f) }
                        }
                    },
                    onWrongMatch = { _, _ -> },
                    onMatchAttempt = { _ -> },
                    innerPictureScaleForChoice = { choice ->
                        Chapter1Station5And6ImageMatchInnerScale.innerScale(choice)
                    },
                    captionSizeMultiplier = ui.plan.imageMatchCaptionSizeMultiplier,
                    chapterId = ui.chapterId,
                    stationId = ui.stationId,
                    instructionReadablePanel = ui.stationUiSpec.matchLetterInstructionReadablePanel,
                    instructions =
                        ui.stationUiSpec.matchLetterInstructionText
                            ?: StationInstructionCopy.MatchLetterFinale,
                    onSolved = { handleMatchSolved() },
                )
            } else {
                val listenOnlyTemporaryHintLetter =
                    if (ui.episode4HelpSt15 &&
                        ui.stationUiSpec.templateId == StationTemplateId.ImageMatch &&
                        ui.stationUiSpec.hintMode == com.tal.hebrewdino.ui.domain.StationHintMode.TemporaryTargetLetter
                    ) {
                        ui.episode4HelpActiveHintLetter
                    } else {
                        null
                    }
                ImageMatchQuestionRenderer(
                    current = current,
                    stationUiSpec = ui.stationUiSpec,
                    isCompactLandscapePhone = ui.isCompactLandscapePhone,
                    headerInstructionFontScale =
                        (if (ui.chapterId == TrainingV1Config.CHAPTER_ID) 1.35f else 1.35f * 2f),
                    listenOnlyTemporaryHintLetter = listenOnlyTemporaryHintLetter,
                    contentKey = deps.session.currentIndex,
                    enabled = state.enabled,
                    shakePx = state.optionsShakePx,
                    entryPulseEpoch = state.entryPulseEpoch,
                    hintCorrectChoiceId = current.correctChoiceId.takeIf { state.wrongTapsThisQuestion >= 2 },
                    hintPulseEpoch = state.hintPulseEpoch,
                    captionSizeMultiplier = ui.plan.imageMatchCaptionSizeMultiplier,
                    pictureSizeMultiplier = ui.plan.imageMatchPictureSizeMultiplier,
                    innerPictureScaleForChoice = { choice ->
                        Chapter1Station5And6ImageMatchInnerScale.innerScale(choice)
                    },
                    chapterId = ui.chapterId,
                    stationId = ui.stationId,
                    onAttempt = { choiceId -> handlers.handleImageMatchAttempt(choiceId) },
                    entryPulseScale = state.entryPulseScale,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        is Question.FinaleSlotQuestion ->
            FinaleSlotQuestionRenderer(
                current = current,
                contentKey = deps.session.currentIndex,
                enabled = state.enabled,
                shakeEpoch = state.shakeEpoch,
                onWrongPlacement = {
                    handlers.handleFinaleWrongPlacement()
                    handlers.onWrongFeedback()
                },
                onSolved = { words ->
                    deps.scope.launch {
                        when (deps.session.submitFinaleWords(words)) {
                            AnswerResult.Correct -> {
                                if (ui.audioEnabled) ChildGameAudioHooks.onCorrect()
                                val isLast = deps.session.currentIndex >= deps.session.totalQuestions - 1
                                handlers.advanceAfterRound(isLast)
                            }
                            else -> {}
                        }
                    }
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(state.optionsShakePx.toInt(), 0) },
            )
    }

    val fullScreenBalloonHintLetter =
        when {
            ui.episode4HelpSt15 -> ui.episode4HelpActiveHintLetter
            ui.popBalloonsHelpControlsEnabled -> ui.balloonHelpHintLetter
            else -> null
        }
    if (ui.phase == GamePhase.Play &&
        fullScreenBalloonHintLetter != null &&
        current is Question.PopBalloonsQuestion &&
        ui.stationUiSpec.templateId == StationTemplateId.PopBalloons &&
        !ui.stationUiSpec.excludeFullScreenBalloonHintOverlay
    ) {
        Box(
            modifier = Modifier.fillMaxSize().zIndex(3f),
            contentAlignment = Alignment.Center,
        ) {
            TargetLetterHeaderChip(letter = fullScreenBalloonHintLetter)
        }
    }
}
