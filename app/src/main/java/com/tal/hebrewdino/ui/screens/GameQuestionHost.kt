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

@Composable
internal fun GameQuestionHost(
    phase: GamePhase,
    stationUiSpec: com.tal.hebrewdino.ui.domain.StationUiSpec,
    stationId: Int,
    chapterId: Int,
    plan: StationQuizPlan,
    listenOnly: Boolean,
    sagaUsesPickLetterAudioStaging: Boolean,
    sagaUsesPopBalloonsAudioStaging: Boolean,
    sagaUsesFindGridAudioStaging: Boolean,
    isChapter3HighlightedLetterInWordStation: Boolean,
    isChapter3AudioLetterRecognitionStation: Boolean,
    isChapter3PopAllLettersStation: Boolean,
    highlightedInWordWord: String?,
    highlightedInWordSlotIndex: Int?,
    audioEnabled: Boolean,
    isCompactLandscapePhone: Boolean,
    episode4HelpSt15: Boolean,
    episode4HelpActiveHintLetter: String?,
    episode4HelpStation2BalloonHintEpoch: Int,
    episode4HelpStation3GridHintEpoch: Int,
    popBalloonsHelpControlsEnabled: Boolean,
    balloonHelpHintLetter: String?,
    showPopBalloonsTargetLetterChip: Boolean,
    station1PinnedCorrectLetter: String?,
    station2PinnedBalloonLetter: String?,
    station2PinnedBalloonColor: Color?,
    hintHeaderScale: Float,
    enabled: Boolean,
    shakeEpoch: Int,
    wrongTapsThisQuestion: Int,
    hintPulseEpoch: Int,
    correctTapPulseLetter: String?,
    correctTapPulseEpoch: Int,
    station4WrongFlashLetter: String?,
    station4WrongFlashEpoch: Int,
    station4PinnedCorrectLetter: String?,
    entryPulseEpoch: Int,
    entryPulseScale: Float,
    optionsShakePx: Float,
    session: LevelSession,
    scope: CoroutineScope,
    voice: VoicePlayer,
    sfx: SoundPoolPlayer,
    cancelFeedbackVoice: () -> Unit,
    getFeedbackVoiceJob: () -> Job?,
    setFeedbackVoiceJob: (Job?) -> Unit,
    consumeTapCooldown: () -> Boolean,
    performSideHelpReplay: () -> Unit,
    handleFindGridSagaGridLetterTapped: (tapped: String, question: Question.FindLetterGridQuestion) -> Unit,
    handleFindGridCellTapped: (index: Int, question: Question.FindLetterGridQuestion) -> Unit,
    handleFindGridCompleted: () -> Unit,
    handlePickLetterPick: (picked: String) -> Unit,
    handlePopBalloonsPopSfx: suspend (letter: String, isCorrect: Boolean, finalCorrectBalloon: Boolean, balloonIndex: Int) -> Unit,
    handlePopBalloonsWrongPick: () -> Unit,
    handlePopBalloonsAllCorrectPopped: (lastLetter: String, poppedBalloonColor: Color, isChapter3PopAllLettersStation: Boolean) -> Unit,
    handlePictureStartsWithPick: (picked: String) -> Unit,
    handleImageToWordReplayCorrectChoice: () -> Unit,
    handleImageToWordWordPressed: (choiceId: String) -> Unit,
    handleImageToWordAttempt: (choiceId: String) -> Boolean,
    handleImageMatchAttempt: (choiceId: String) -> Boolean,
    handleFinaleWrongPlacement: () -> Unit,
    onWrongFeedback: () -> Unit,
    advanceAfterRound: suspend (isLast: Boolean) -> Unit,
) {
    val current = session.currentQuestion ?: return
    if (phase == GamePhase.Intro) {
        if (stationUiSpec.showBetweenRoundIntroPulse) {
            IntroPulse(stationId = stationId, question = current, modifier = Modifier.fillMaxWidth())
        }
        return
    }

    when (current) {
        is Question.FindLetterGridQuestion -> {
            FindLetterGridQuestionRenderer(
                current = current,
                listenOnly = listenOnly,
                isSagaRevealStation = stationUiSpec.findGridSagaRevealStation,
                sagaUsesFindGridAudioStaging = sagaUsesFindGridAudioStaging,
                stationUiSpec = stationUiSpec,
                chapter3ContextWordHint =
                    StationBehaviorRegistry.findGridContextWordHint(
                        stationUiSpec = stationUiSpec,
                        questionIndex = session.currentIndex,
                        sagaUsesFindGridAudioStaging = sagaUsesFindGridAudioStaging,
                    ),
                floatingTargetLetterHint =
                    if (episode4HelpSt15 && stationUiSpec.findGridUseEpisode4HelpHints) {
                        episode4HelpActiveHintLetter
                    } else {
                        null
                    },
                episode4TargetCellsHintEpoch =
                    if (episode4HelpSt15 && stationUiSpec.findGridUseEpisode4HelpHints) {
                        episode4HelpStation3GridHintEpoch
                    } else {
                        0
                    },
                hintPulseEpoch = hintPulseEpoch,
                enabled = enabled,
                contentKey = session.currentIndex,
                entryPulseScale = entryPulseScale,
                optionsShakePx = optionsShakePx,
                onSagaGridLetterTapped =
                    if (sagaUsesFindGridAudioStaging) {
                        { tapped -> handleFindGridSagaGridLetterTapped(tapped, current) }
                    } else {
                        null
                    },
                onCorrectTap =
                    if (audioEnabled && !sagaUsesFindGridAudioStaging) {
                        {
                            scope.launch {
                                sfx.playFirstAvailable(AudioClips.SfxCorrect, volume = 0.58f)
                            }
                        }
                    } else {
                        null
                    },
                onCellTapped = { index -> handleFindGridCellTapped(index, current) },
                onCompleted = { handleFindGridCompleted() },
                modifier = Modifier.fillMaxSize(),
            )
        }
        is Question.PopBalloonsQuestion -> {
            if (plan.mode == StationQuizMode.PickLetter) {
                PickLetterQuestionRenderer(
                    current = current,
                    stationUiSpec = stationUiSpec,
                    listenOnly = listenOnly,
                    isSagaEpisode = isSagaEpisode(chapterId),
                    sagaUsesPickLetterAudioStaging = sagaUsesPickLetterAudioStaging,
                    chapterId = chapterId,
                    stationId = stationId,
                    highlightedInWordWord = highlightedInWordWord,
                    highlightedInWordSlotIndex = highlightedInWordSlotIndex,
                    isChapter3HighlightedLetterInWordStation = isChapter3HighlightedLetterInWordStation,
                    isChapter3AudioLetterRecognitionStation = isChapter3AudioLetterRecognitionStation,
                    station1PinnedCorrectLetter = station1PinnedCorrectLetter,
                    entryPulseScale = entryPulseScale,
                    enabled = enabled,
                    shakePx = optionsShakePx,
                    wrongTapsThisQuestion = wrongTapsThisQuestion,
                    hintPulseEpoch = hintPulseEpoch,
                    correctTapPulseLetter = correctTapPulseLetter,
                    correctTapPulseEpoch = correctTapPulseEpoch,
                    temporaryHintLetter = episode4HelpActiveHintLetter,
                    onRepeatLetterClick = repeatLetter@{
                        if (!audioEnabled) return@repeatLetter
                        cancelFeedbackVoice()
                        val letter = current.correctAnswer
                        val clip = AudioClips.letterNameClip(letter) ?: return@repeatLetter
                        val job = scope.launch { voice.playBlocking(clip) }
                        setFeedbackVoiceJob(job)
                    },
                    onPick = { picked -> handlePickLetterPick(picked) },
                )
            } else {
                val correctLetterSet =
                    if (isChapter3PopAllLettersStation) {
                        val w = session.chapter3PopAllLettersCurrentWord()?.first.orEmpty()
                        w.toCharArray().map { it.toString() }.toSet()
                    } else {
                        null
                    }
                val visualRoundSeed =
                    if (sagaUsesPopBalloonsAudioStaging) {
                        session.currentIndex
                    } else {
                        0
                    }
                val maxVisibleBalloonCount =
                    if (isCompactLandscapePhone && stationUiSpec.popBalloonsCompactLandscapePhoneTuning) 8 else null
                val episode4CorrectBalloonHintEpoch =
                    if (episode4HelpSt15 && stationId == Chapter1StationOrder.BALLOON_POP) {
                        episode4HelpStation2BalloonHintEpoch
                    } else {
                        0
                    }
                val helpSideInsetDp = stationUiSpec.balloonPlayAreaStartInsetDp.dp
                val popAllWordForBanner = session.chapter3PopAllLettersCurrentWord()?.first.orEmpty()
                PopBalloonsQuestionRenderer(
                    current = current,
                    planMode = plan.mode,
                    planPopAllLettersInWord = plan.popAllLettersInWord,
                    popAllLettersWordForBanner = popAllWordForBanner,
                    popAllLettersBannerInstruction =
                        stationUiSpec.popBalloonsPopAllLettersBannerInstruction
                            ?: StationInstructionCopy.PopBalloonsPopAllLettersInWord,
                    stationUiSpec = stationUiSpec,
                    isCompactLandscapePhone = isCompactLandscapePhone,
                    listenOnly = listenOnly,
                    sagaUsesPopBalloonsAudioStaging = sagaUsesPopBalloonsAudioStaging,
                    showPopBalloonsTargetLetterChip = showPopBalloonsTargetLetterChip,
                    episode4HelpSt15 = episode4HelpSt15,
                    episode4HelpActiveHintLetter = episode4HelpActiveHintLetter,
                    hintHeaderScale = hintHeaderScale,
                    station2PinnedBalloonLetter = station2PinnedBalloonLetter,
                    station2PinnedBalloonColor = station2PinnedBalloonColor,
                    correctLetterSet = correctLetterSet,
                    enabled = enabled,
                    shakePx = optionsShakePx,
                    entryPulseScale = entryPulseScale,
                    visualRoundSeed = visualRoundSeed,
                    maxVisibleBalloonCount = maxVisibleBalloonCount,
                    episode4CorrectBalloonHintEpoch = episode4CorrectBalloonHintEpoch,
                    helpSideInsetDp = helpSideInsetDp,
                    contentTopPaddingDp =
                        if (sagaUsesPopBalloonsAudioStaging) SixStationArcHalfCmNudge else 0.dp,
                    onBalloonPressed = { _ -> },
                    onPopSfx = handlePopBalloonsPopSfx,
                    onWrongPick = { handlePopBalloonsWrongPick() },
                    onAllCorrectPopped = { lastLetter, poppedBalloonColor ->
                        handlePopBalloonsAllCorrectPopped(
                            lastLetter,
                            poppedBalloonColor,
                            isChapter3PopAllLettersStation,
                        )
                    },
                )
            }
        }
        is Question.PictureStartsWithQuestion -> {
            val pictureInstructionText =
                when {
                    stationUiSpec.pictureStartsWithInstructionOverride != null ->
                        stationUiSpec.pictureStartsWithInstructionOverride
                    listenOnly && isSagaEpisode(chapterId) && stationId == Chapter1StationOrder.PICTURE_PICK_ONE ->
                        stationUiSpec.pictureStartsWithListenOnlySagaInstruction
                            ?: StationInstructionCopy.PictureStartsWithListenFirstSaga
                    else ->
                        StationInstructionCopy.PictureStartsWithDefault
                }
            val pictureInstructionReadablePanel =
                stationUiSpec.pictureStartsWithReadablePanel ||
                    stationUiSpec.pictureStartsWithInstructionPanelStyle == InstructionPanelStyle.WhiteRounded
            val pictureShowWordCaption =
                !(listenOnly && stationUiSpec.hidePictureWordCaptionWhenListenOnlySaga)
            PictureStartsWithQuestionRenderer(
                current = current,
                stationUiSpec = stationUiSpec,
                isCompactLandscapePhone = isCompactLandscapePhone,
                instructionText = pictureInstructionText,
                instructionReadablePanel = pictureInstructionReadablePanel,
                showWordCaption = pictureShowWordCaption,
                onPictureTapReplayWord =
                    if (episode4HelpSt15 && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                        { performSideHelpReplay() }
                    } else if (audioEnabled) {
                        {
                            cancelFeedbackVoice()
                            val wordPath = AudioClips.wordClipByCatalogId(current.catalogEntryId)
                            if (voice.hasAsset(wordPath)) {
                                val job = scope.launch { voice.playBlocking(wordPath) }
                                setFeedbackVoiceJob(job)
                            }
                        }
                    } else {
                        null
                    },
                temporaryStartingLetterHint =
                    if (episode4HelpSt15 && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                        episode4HelpActiveHintLetter
                    } else {
                        null
                    },
                pinnedCorrectLetter =
                    if (chapterId == 4 && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                        station4PinnedCorrectLetter
                    } else {
                        null
                    },
                enabled = enabled,
                shakePx = optionsShakePx,
                entryPulseEpoch =
                    if (chapterId == 6 && stationId == Chapter1StationOrder.PICTURE_PICK_ONE) {
                        0
                    } else {
                        entryPulseEpoch
                    },
                promptWordSizeMultiplier = plan.imageMatchCaptionSizeMultiplier * 1.2f,
                innerPictureScale =
                    Chapter1Station5And6ImageMatchInnerScale.innerScalePictureStartsWith(
                        catalogEntryId = current.catalogEntryId,
                        letter = current.correctLetter,
                        word = current.word,
                        tintArgb = current.tintArgb,
                        tileDrawable = current.tileDrawable,
                    ),
                pictureSizeMultiplier = plan.imageMatchPictureSizeMultiplier,
                sortOptionLetters = plan.sortOptionLetters,
                chapterId = chapterId,
                stationId = stationId,
                hintCorrectLetter = current.correctLetter.takeIf { wrongTapsThisQuestion >= 2 },
                hintPulseEpoch = hintPulseEpoch,
                correctPulseLetter = correctTapPulseLetter,
                correctPulseEpoch = correctTapPulseEpoch,
                wrongFlashLetter = station4WrongFlashLetter,
                wrongFlashEpoch = station4WrongFlashEpoch,
                entryPulseScale = 1f,
                onPickLetter = { picked -> handlePictureStartsWithPick(picked) },
            )
        }
        is Question.ImageMatchQuestion ->
            if (stationUiSpec.templateId == StationTemplateId.ImageToWord) {
                ImageToWordQuestionRenderer(
                    current = current,
                    contentKey = session.currentIndex,
                    enabled = enabled,
                    entryPulseScale = entryPulseScale,
                    optionsShakePx = optionsShakePx,
                    instructionText =
                        stationUiSpec.imageToWordInstructionText
                            ?: StationInstructionCopy.Chapter3ImageToWord,
                    onPictureTapReplayWord =
                        if (audioEnabled) {
                            { handleImageToWordReplayCorrectChoice() }
                        } else {
                            null
                        },
                    onWordPressed = { choiceId -> handleImageToWordWordPressed(choiceId) },
                    onAttempt = { choiceId -> handleImageToWordAttempt(choiceId) },
                )
            } else if (
                stationUiSpec.templateId == StationTemplateId.MatchLetterToWord
            ) {
                val matchChoices = current.choices
                fun speakNow(play: suspend () -> Unit) {
                    if (!audioEnabled) return
                    cancelFeedbackVoice()
                    val job = scope.launch { play() }
                    setFeedbackVoiceJob(job)
                }
                var lastPraiseClip by remember(chapterId, stationId) { mutableStateOf<String?>(null) }
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
                                chapterId = chapterId,
                                voiceHasAsset = { path -> voice.hasAsset(path) },
                            )
                        voice.playBlocking(clip)
                    }
                }

                fun handleMatchLetterPressed(letter: String) {
                    val clip = AudioClips.letterNameClip(letter) ?: return
                    speakNow { voice.playBlocking(clip) }
                }

                fun handleMatchSolved() {
                    if (!consumeTapCooldown()) return
                    scope.launch {
                        withTimeoutOrNull(4500L) { getFeedbackVoiceJob()?.join() }
                        if (audioEnabled) {
                            cancelFeedbackVoice()
                            val job =
                                scope.launch {
                                    val candidates = matchPraiseClips.filter { it != lastPraiseClip }
                                    val pickFrom =
                                        if (candidates.isNotEmpty()) candidates else listOfNotNull(lastPraiseClip)
                                    val picked = pickFrom.shuffled().firstOrNull()
                                    if (picked != null) {
                                        lastPraiseClip = picked
                                        voice.playBlocking(picked)
                                    }
                                }
                            setFeedbackVoiceJob(job)
                            withTimeoutOrNull(3000L) { job.join() }
                        }
                        when (session.completeCurrentRound()) {
                            AnswerResult.Correct -> {
                                if (audioEnabled) ChildGameAudioHooks.onCorrect()
                                val isLast = session.currentIndex >= session.totalQuestions - 1
                                advanceAfterRound(isLast)
                            }
                            else -> {}
                        }
                    }
                }
                MatchLetterToWordQuestionRenderer(
                    choices = matchChoices,
                    stationUiSpec = stationUiSpec,
                    isCompactLandscapePhone = isCompactLandscapePhone,
                    choicePairLimit = 3,
                    contentKey = session.currentIndex,
                    enabled = enabled,
                    entryPulseScale = entryPulseScale,
                    letterTileSizeMultiplier = if (chapterId == TrainingV1Config.CHAPTER_ID) 1.10f else 1f,
                    onWordPressed = { choiceId -> handleMatchWordPressed(choiceId) },
                    onLetterPressed = { letter -> handleMatchLetterPressed(letter) },
                    onCorrectMatch = { _ ->
                        if (audioEnabled) {
                            scope.launch { sfx.playFirstAvailable(AudioClips.SfxCorrect, volume = 0.58f) }
                        }
                    },
                    onWrongMatch = { _, _ -> },
                    onMatchAttempt = { _ -> },
                    innerPictureScaleForChoice = { choice ->
                        Chapter1Station5And6ImageMatchInnerScale.innerScale(choice)
                    },
                    captionSizeMultiplier = plan.imageMatchCaptionSizeMultiplier,
                    chapterId = chapterId,
                    stationId = stationId,
                    instructionReadablePanel = stationUiSpec.matchLetterInstructionReadablePanel,
                    instructions =
                        stationUiSpec.matchLetterInstructionText
                            ?: StationInstructionCopy.MatchLetterFinale,
                    onSolved = { handleMatchSolved() },
                )
            } else {
                val listenOnlyTemporaryHintLetter =
                    if (episode4HelpSt15 &&
                        stationUiSpec.templateId == StationTemplateId.ImageMatch &&
                        stationUiSpec.hintMode == com.tal.hebrewdino.ui.domain.StationHintMode.TemporaryTargetLetter
                    ) {
                        episode4HelpActiveHintLetter
                    } else {
                        null
                    }
                ImageMatchQuestionRenderer(
                    current = current,
                    stationUiSpec = stationUiSpec,
                    isCompactLandscapePhone = isCompactLandscapePhone,
                    headerInstructionFontScale =
                        (if (chapterId == TrainingV1Config.CHAPTER_ID) 1.35f else 1.35f * 2f),
                    listenOnlyTemporaryHintLetter = listenOnlyTemporaryHintLetter,
                    contentKey = session.currentIndex,
                    enabled = enabled,
                    shakePx = optionsShakePx,
                    entryPulseEpoch = entryPulseEpoch,
                    hintCorrectChoiceId = current.correctChoiceId.takeIf { wrongTapsThisQuestion >= 2 },
                    hintPulseEpoch = hintPulseEpoch,
                    captionSizeMultiplier = plan.imageMatchCaptionSizeMultiplier,
                    pictureSizeMultiplier = plan.imageMatchPictureSizeMultiplier,
                    innerPictureScaleForChoice = { choice ->
                        Chapter1Station5And6ImageMatchInnerScale.innerScale(choice)
                    },
                    chapterId = chapterId,
                    stationId = stationId,
                    onAttempt = { choiceId -> handleImageMatchAttempt(choiceId) },
                    entryPulseScale = entryPulseScale,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        is Question.FinaleSlotQuestion ->
            FinaleSlotQuestionRenderer(
                current = current,
                contentKey = session.currentIndex,
                enabled = enabled,
                shakeEpoch = shakeEpoch,
                onWrongPlacement = {
                    handleFinaleWrongPlacement()
                    onWrongFeedback()
                },
                onSolved = { words ->
                    scope.launch {
                        when (session.submitFinaleWords(words)) {
                            AnswerResult.Correct -> {
                                if (audioEnabled) ChildGameAudioHooks.onCorrect()
                                val isLast = session.currentIndex >= session.totalQuestions - 1
                                advanceAfterRound(isLast)
                            }
                            else -> {}
                        }
                    }
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(optionsShakePx.toInt(), 0) },
            )
    }

    val fullScreenBalloonHintLetter =
        when {
            episode4HelpSt15 -> episode4HelpActiveHintLetter
            popBalloonsHelpControlsEnabled -> balloonHelpHintLetter
            else -> null
        }
    if (phase == GamePhase.Play &&
        fullScreenBalloonHintLetter != null &&
        current is Question.PopBalloonsQuestion &&
        stationUiSpec.templateId == StationTemplateId.PopBalloons &&
        !stationUiSpec.excludeFullScreenBalloonHintOverlay
    ) {
        Box(
            modifier = Modifier.fillMaxSize().zIndex(3f),
            contentAlignment = Alignment.Center,
        ) {
            TargetLetterHeaderChip(letter = fullScreenBalloonHintLetter)
        }
    }
}
