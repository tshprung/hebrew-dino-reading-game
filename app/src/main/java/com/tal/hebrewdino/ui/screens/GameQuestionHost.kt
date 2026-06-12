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
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.SoundPoolPlayer
import com.tal.hebrewdino.ui.audio.VoicePlayer
import com.tal.hebrewdino.ui.components.TargetLetterHeaderChip
import com.tal.hebrewdino.ui.data.PlayerAddress
import com.tal.hebrewdino.ui.domain.AnswerResult
import com.tal.hebrewdino.ui.domain.Chapter1Station5And6ImageMatchInnerScale
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import com.tal.hebrewdino.ui.domain.InstructionPanelStyle
import com.tal.hebrewdino.ui.domain.LevelSession
import com.tal.hebrewdino.ui.domain.Question
import com.tal.hebrewdino.ui.domain.StationBehaviorRegistry
import com.tal.hebrewdino.ui.domain.StationInstructionCopy
import com.tal.hebrewdino.ui.domain.Season2AdvancedStationMode
import com.tal.hebrewdino.ui.domain.Season2ChapterStationPlans
import com.tal.hebrewdino.ui.domain.Season2StationThemeCopy
import com.tal.hebrewdino.ui.domain.Season2Ch1QaPolicy
import com.tal.hebrewdino.ui.domain.Season2StationQaPolicy
import com.tal.hebrewdino.ui.domain.Season2StationUx
import com.tal.hebrewdino.ui.domain.StationQuizMode
import com.tal.hebrewdino.ui.domain.StationQuizPlan
import com.tal.hebrewdino.ui.domain.StationTemplateId
import com.tal.hebrewdino.ui.domain.TrainingV1Config
import com.tal.hebrewdino.ui.game.ChildGameAudioHooks
import com.tal.hebrewdino.ui.game.Season2MissingFirstLetterGame
import com.tal.hebrewdino.ui.game.Season2RhymingGame
import com.tal.hebrewdino.ui.game.Season2WordPartsGame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.random.Random

/** Chapter 2 station 6, Season 2 match-letter finale, and Training — same match-letter behavior path. */
private fun isChapter2StyleMatchLetterStation(chapterId: Int, stationId: Int): Boolean =
    ((chapterId == 1 || chapterId == 2 || chapterId == 4 || chapterId == 5) &&
        stationId == Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH) ||
        Season2StationUx.isMatchLetterFinale(chapterId, stationId) ||
        (chapterId == TrainingV1Config.CHAPTER_ID &&
            stationId == TrainingV1Config.STATION_MATCH_LETTER_TO_WORD)

private val MatchPraiseClips =
    arrayOf(
        AudioClips.VoPraiseMetzuyan,
        AudioClips.VoPraiseYofi,
        AudioClips.VoPraiseHitzlacht,
        AudioClips.VoNice1,
        AudioClips.VoGoodJob2,
        AudioClips.VoGoodJob1,
    )

internal fun pickRandomAvoiding(
    values: Array<String>,
    avoid: String?,
    random: Random = Random.Default,
): String? {
    val n = values.size
    if (n == 0) return null
    if (n == 1) return values[0]
    if (avoid == null) return values[random.nextInt(n)]

    var avoidIndex = -1
    for (i in 0 until n) {
        if (values[i] == avoid) {
            avoidIndex = i
            break
        }
    }
    if (avoidIndex == -1) return values[random.nextInt(n)]

    val r = random.nextInt(n - 1)
    val idx = if (r >= avoidIndex) r + 1 else r
    return values[idx]
}

internal data class GameQuestionHostUi(
    val phase: GamePhase,
    val stationUiSpec: com.tal.hebrewdino.ui.domain.StationUiSpec,
    val stationId: Int,
    val chapterId: Int,
    val trainingRoundIndex: Int?,
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
    val chapter1PlayerAddress: PlayerAddress?,
    val season2Chapter1UxStationId: Int? = null,
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
    val wordPartsCompletedEquation: String?,
    val wordPartsHintRevealWord: String?,
    val entryPulseEpoch: Int,
    val entryPulseScale: Float,
    val optionsShakePx: Float,
)

internal data class GameQuestionHostDeps(
    val session: LevelSession,
    val gameViewModel: GameViewModel,
    val scope: CoroutineScope,
    val voice: VoicePlayer,
    val sfx: SoundPoolPlayer,
    val rawVoice: RawVoicePlayer,
    val cancelFeedbackVoice: () -> Unit,
    val audioRuntime: GameAudioRuntimeState,
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
    val handleMissingFirstLetterPick: (picked: String) -> Unit,
    val handleWordPartsPick: (picked: Question.WordPartsSplitOption) -> Unit,
    val handleRhymingPick: (choiceId: String) -> Unit,
    val handleAdvancedReplayWord: () -> Unit,
    val handleWordPartsPictureTap: () -> Unit,
    val handleWordPartsHintRevealAudio: () -> Unit,
    val handleFinaleWrongPlacement: () -> Unit,
    val onWrongFeedback: (wrongPickedLetter: String?, wrongWordCatalogId: String?, wrongPickedLetterAlreadySpoken: Boolean, wrongWordAlreadySpoken: Boolean) -> Unit,
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
                chapter1PlayerAddress = ui.chapter1PlayerAddress,
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
                            FindGridActions.handleNonStagedCorrectTap(
                                audioEnabled = ui.audioEnabled,
                                scope = deps.scope,
                                sfx = deps.sfx,
                            )
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
                    chapter1PlayerAddress = ui.chapter1PlayerAddress,
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
                        val letter = current.correctAnswer
                        GameAudioActions.launchPromptVoice(
                            audioEnabled = ui.audioEnabled,
                            scope = deps.scope,
                            audioRuntime = deps.audioRuntime,
                            cancelFeedbackVoice = deps.cancelFeedbackVoice,
                        ) {
                            if (ui.chapterId == 1 || ui.chapterId == 2 || ui.chapterId == 4 || ui.chapterId == 5) {
                                val resId = AudioClips.letterNameRawResId(letter)
                                if (resId == null) {
                                    android.util.Log.e(
                                        "MissingContent",
                                        "Missing required repeat-letter audio. chapterId=${ui.chapterId} stationId=${ui.stationId} context=GameQuestionHost.onRepeatLetterClick stage=missing raw letter-name mapping letter='$letter'",
                                    )
                                    deps.rawVoice.playRawBlocking(0)
                                    return@launchPromptVoice
                                }
                                deps.rawVoice.playRawBlocking(resId)
                            } else {
                                val clip = AudioClips.letterNameClip(letter) ?: return@launchPromptVoice
                                deps.voice.playBlocking(clip)
                            }
                        }
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
                    chapter1PlayerAddress = ui.chapter1PlayerAddress,
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
                    season2Chapter1UxStationId = ui.season2Chapter1UxStationId,
                    contentTopPaddingDp =
                        if (ui.sagaUsesPopBalloonsAudioStaging ||
                            (ui.chapterId == 3 && ui.stationId == 3) ||
                            (ui.chapterId == TrainingV1Config.CHAPTER_ID && ui.stationId == TrainingV1Config.STATION_WORD_BALLOONS)
                        ) {
                            SixStationArcHalfCmNudge
                        } else if (ui.chapterId == 6 && ui.stationId == 3) {
                            SixStationArcHalfCmNudge
                        } else {
                            0.dp
                        },
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
                    if (
                        ui.episode4HelpSt15 &&
                            (
                                ui.stationId == Chapter1StationOrder.PICTURE_PICK_ONE ||
                                    Season2StationUx.isWarmupPictureStartsWith(ui.chapterId, ui.stationId)
                            )
                    ) {
                        { handlers.performSideHelpReplay() }
                    } else if (ui.audioEnabled) {
                        {
                            val requiresRawWordReplay =
                                ((ui.chapterId == 3 || ui.chapterId == 6) && ui.stationId == 1) ||
                                    ((ui.chapterId == 1 || ui.chapterId == 2 || ui.chapterId == 4 || ui.chapterId == 5) &&
                                        ui.stationId == Chapter1StationOrder.PICTURE_PICK_ONE) ||
                                    Season2StationUx.isWarmupPictureStartsWith(ui.chapterId, ui.stationId) ||
                                    (ui.chapterId == TrainingV1Config.CHAPTER_ID &&
                                        ui.stationId == TrainingV1Config.STATION_PICTURE_CHOOSE_WORD)
                            GameAudioActions.launchPromptVoice(
                                audioEnabled = ui.audioEnabled,
                                scope = deps.scope,
                                audioRuntime = deps.audioRuntime,
                                cancelFeedbackVoice = deps.cancelFeedbackVoice,
                            ) {
                                if (requiresRawWordReplay) {
                                    val resId = AudioClips.wordRawResIdByCatalogId(current.catalogEntryId)
                                    if (resId == null) {
                                        android.util.Log.e(
                                            "MissingContent",
                                            "Missing required word audio. chapterId=${ui.chapterId} stationId=${ui.stationId} context=GameQuestionHost.onPictureTapReplayWord stage=missing raw word mapping catalogId='${current.catalogEntryId}'",
                                        )
                                        deps.rawVoice.playRawBlocking(0)
                                        return@launchPromptVoice
                                    }
                                    deps.rawVoice.playRawBlocking(resId)
                                } else {
                                    val wordPath = AudioClips.wordClipByCatalogId(current.catalogEntryId)
                                    if (deps.voice.hasAsset(wordPath)) {
                                        deps.voice.playBlocking(wordPath)
                                    }
                                }
                            }
                        }
                    } else {
                        null
                    },
                temporaryStartingLetterHint =
                    if (
                        ui.episode4HelpSt15 &&
                            (
                                ui.stationId == Chapter1StationOrder.PICTURE_PICK_ONE ||
                                    Season2StationUx.isWarmupPictureStartsWith(ui.chapterId, ui.stationId)
                            )
                    ) {
                        ui.episode4HelpActiveHintLetter
                    } else {
                        null
                    },
                pinnedCorrectLetter =
                    if ((ui.chapterId in 1..5 && ui.stationId == Chapter1StationOrder.PICTURE_PICK_ONE) ||
                        (ui.chapterId == 3 && ui.stationId == 1) ||
                        (ui.chapterId == 6 && ui.stationId == 1)
                    ) {
                        state.station4PinnedCorrectLetter
                    } else {
                        null
                    },
                enabled = state.enabled,
                shakePx = state.optionsShakePx,
                entryPulseEpoch =
                    if (
                        (ui.chapterId == 6 && ui.stationId == Chapter1StationOrder.PICTURE_PICK_ONE) ||
                            Season2StationUx.isWarmupPictureStartsWith(ui.chapterId, ui.stationId)
                    ) {
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
            when {
                ui.plan.season2AdvancedMode == Season2AdvancedStationMode.PictureToWord -> {
                    ImageToWordQuestionRenderer(
                        current = current,
                        contentKey = deps.session.currentIndex,
                        enabled = state.enabled,
                        entryPulseScale = state.entryPulseScale,
                        optionsShakePx = state.optionsShakePx,
                        instructionText =
                            Season2StationThemeCopy.pictureToWordInstruction(ui.plan.season2StationTheme),
                        chapterId = ui.chapterId,
                        stationId = ui.stationId,
                        trainingRoundIndex = ui.trainingRoundIndex,
                        onPictureTapReplayWord =
                            if (ui.audioEnabled) {
                                { handlers.handleAdvancedReplayWord() }
                            } else {
                                null
                            },
                        onWordPressed = { choiceId -> handlers.handleImageToWordWordPressed(choiceId) },
                        onAttempt = { choiceId -> handlers.handleImageToWordAttempt(choiceId) },
                    )
                }
                ui.stationUiSpec.templateId == StationTemplateId.ImageToWord -> {
                    ImageToWordQuestionRenderer(
                        current = current,
                        contentKey = deps.session.currentIndex,
                        enabled = state.enabled,
                        entryPulseScale = state.entryPulseScale,
                        optionsShakePx = state.optionsShakePx,
                        instructionText =
                            ui.stationUiSpec.imageToWordInstructionText
                                ?: StationInstructionCopy.Chapter3ImageToWord,
                        chapterId = ui.chapterId,
                        stationId = ui.stationId,
                        trainingRoundIndex = ui.trainingRoundIndex,
                        onPictureTapReplayWord =
                            if (ui.audioEnabled) {
                                { handlers.handleImageToWordReplayCorrectChoice() }
                            } else {
                                null
                            },
                        onWordPressed = { choiceId -> handlers.handleImageToWordWordPressed(choiceId) },
                        onAttempt = { choiceId -> handlers.handleImageToWordAttempt(choiceId) },
                    )
                }

                ui.stationUiSpec.templateId == StationTemplateId.MatchLetterToWord -> {
                    val matchChoices = current.choices

                    var lastSpokenMatchWordChoiceId by remember(ui.chapterId, ui.stationId, deps.session.currentIndex) {
                        mutableStateOf<String?>(null)
                    }

                    fun handleMatchWordPressed(choiceId: String) {
                        val requiresRawWordTap =
                            ((ui.chapterId == 3 || ui.chapterId == 6) && ui.stationId == 2) ||
                                ((ui.chapterId == 1 || ui.chapterId == 2 || ui.chapterId == 4 || ui.chapterId == 5) &&
                                    ui.stationId == Chapter1StationOrder.FINALE_PICTURE_LETTER_MATCH) ||
                                Season2StationUx.isMatchLetterFinale(ui.chapterId, ui.stationId) ||
                                (ui.chapterId == TrainingV1Config.CHAPTER_ID &&
                                    ui.stationId == TrainingV1Config.STATION_MATCH_LETTER_TO_WORD)
                        val rawResId =
                            if (requiresRawWordTap) {
                                AudioClips.wordRawResIdByCatalogId(choiceId)
                            } else {
                                null
                            }
                        val clip =
                            if (requiresRawWordTap) {
                                ""
                            } else {
                                AudioClips.imageToWordClipByCatalogId(
                                    catalogEntryId = choiceId,
                                    chapterId = ui.chapterId,
                                    voiceHasAsset = { path -> deps.voice.hasAsset(path) },
                                )
                            }
                        val spokenNow =
                            ui.audioEnabled &&
                                if (requiresRawWordTap) {
                                    rawResId != null
                                } else {
                                    deps.voice.hasAsset(clip)
                                }
                        lastSpokenMatchWordChoiceId = if (spokenNow) choiceId else null
                        GameAudioActions.launchPromptVoice(
                            audioEnabled = ui.audioEnabled,
                            scope = deps.scope,
                            audioRuntime = deps.audioRuntime,
                            cancelFeedbackVoice = deps.cancelFeedbackVoice,
                        ) {
                            if (requiresRawWordTap) {
                                val resId = rawResId
                                if (resId == null) {
                                    android.util.Log.e(
                                        "MissingContent",
                                        "Missing required word audio. chapterId=${ui.chapterId} stationId=${ui.stationId} context=GameQuestionHost.handleMatchWordPressed stage=missing raw word mapping catalogId='$choiceId'",
                                    )
                                    deps.rawVoice.playRawBlocking(0)
                                    return@launchPromptVoice
                                }
                                deps.rawVoice.playRawBlocking(resId)
                            } else {
                                deps.voice.playBlocking(clip)
                            }
                        }
                    }

                    fun handleMatchLetterPressed(letter: String) {
                        GameAudioActions.launchPromptVoice(
                            audioEnabled = ui.audioEnabled,
                            scope = deps.scope,
                            audioRuntime = deps.audioRuntime,
                            cancelFeedbackVoice = deps.cancelFeedbackVoice,
                        ) {
                            if (ui.chapterId == 1 || ui.chapterId == 2 || ui.chapterId == 3 || ui.chapterId == 4 || ui.chapterId == 5 || ui.chapterId == 6) {
                                val resId = AudioClips.letterNameRawResId(letter)
                                if (resId == null) {
                                    android.util.Log.e(
                                        "MissingContent",
                                        "Missing required letter-name audio. chapterId=${ui.chapterId} stationId=${ui.stationId} context=GameQuestionHost.handleMatchLetterPressed stage=missing raw letter-name mapping letter='$letter'",
                                    )
                                    deps.rawVoice.playRawBlocking(0)
                                    return@launchPromptVoice
                                }
                                deps.rawVoice.playRawBlocking(resId)
                            } else if (ui.chapterId == TrainingV1Config.CHAPTER_ID) {
                                val resId = AudioClips.letterNameRawResId(letter)
                                if (resId == null) {
                                    android.util.Log.e(
                                        "MissingContent",
                                        "Missing required letter-name audio. chapterId=${ui.chapterId} stationId=${ui.stationId} context=GameQuestionHost.handleMatchLetterPressed stage=missing raw letter-name mapping letter='$letter'",
                                    )
                                    deps.rawVoice.playRawBlocking(0)
                                    return@launchPromptVoice
                                }
                                deps.rawVoice.playRawBlocking(resId)
                            } else {
                                val clip = AudioClips.letterNameClip(letter) ?: return@launchPromptVoice
                                deps.voice.playBlocking(clip)
                            }
                        }
                    }

                    fun handleMatchSolved() {
                        if (!deps.gameViewModel.consumeTapCooldown()) return
                        deps.gameViewModel.inputLocked = true
                        deps.scope.launch {
                            GameAudioActions.awaitTrackedVoices(deps.audioRuntime, 4500L)
                            if (ui.audioEnabled) {
                                val job =
                                    GameAudioActions.launchFeedbackVoice(
                                        audioEnabled = ui.audioEnabled,
                                        scope = deps.scope,
                                        audioRuntime = deps.audioRuntime,
                                        cancelFeedbackVoice = deps.cancelFeedbackVoice,
                                    ) {
                                        GameAudioActions.playPraiseNoImmediateRepeat(
                                            voice = deps.voice,
                                            audioRuntime = deps.audioRuntime,
                                            candidates = MatchPraiseClips,
                                            chapterId = ui.chapterId,
                                            stationId = ui.stationId,
                                            context = "GameQuestionHost.handleMatchSolved(praise)",
                                            rawVoice = deps.rawVoice,
                                        )
                                    }
                                GameAudioActions.joinSilently(job)
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
                        letterTileSizeMultiplier = 1f,
                        onWordPressed = { choiceId -> handleMatchWordPressed(choiceId) },
                        onLetterPressed = { letter -> handleMatchLetterPressed(letter) },
                        onCorrectMatch = { _ ->
                            if (ui.audioEnabled) {
                                deps.scope.launch {
                                    if (isChapter2StyleMatchLetterStation(ui.chapterId, ui.stationId)) {
                                        GameAudioActions.awaitTrackedVoices(deps.audioRuntime, 4500L)
                                    }
                                    deps.sfx.playFirstAvailable(
                                        AudioClips.SfxCorrect,
                                        volume = 0.58f
                                    )
                                }
                            }
                        },
                        onWrongMatch = { pickedLetter, pickedChoiceId ->
                            if (isChapter2StyleMatchLetterStation(ui.chapterId, ui.stationId)) {
                                val alreadySpoken =
                                    if (ui.chapterId == 1 || ui.chapterId == 2 || ui.chapterId == 4 || ui.chapterId == 5) {
                                        AudioClips.letterNameRawResId(pickedLetter) != null
                                    } else {
                                        val clip = AudioClips.letterNameClip(pickedLetter)
                                        clip != null && deps.voice.hasAsset(clip)
                                    }
                                val wrongWordAlreadySpoken = lastSpokenMatchWordChoiceId == pickedChoiceId
                                deps.scope.launch {
                                    GameAudioActions.awaitTrackedVoices(deps.audioRuntime, 4500L)
                                    handlers.onWrongFeedback(
                                        pickedLetter,
                                        pickedChoiceId,
                                        alreadySpoken,
                                        wrongWordAlreadySpoken,
                                    )
                                }
                            } else if ((ui.chapterId == 3 || ui.chapterId == 6) && ui.stationId == 2) {
                                val wrongWordAlreadySpoken = lastSpokenMatchWordChoiceId == pickedChoiceId
                                deps.scope.launch {
                                    GameAudioActions.awaitTrackedVoices(deps.audioRuntime, 4500L)
                                    handlers.onWrongFeedback(
                                        null,
                                        pickedChoiceId,
                                        false,
                                        wrongWordAlreadySpoken,
                                    )
                                }
                            }
                        },
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
                }

                else -> {
                    val listenOnlyTemporaryHintLetter =
                        if (ui.episode4HelpSt15 &&
                            ui.stationUiSpec.templateId == StationTemplateId.ImageMatch &&
                            ui.stationUiSpec.hintMode == com.tal.hebrewdino.ui.domain.StationHintMode.TemporaryTargetLetter
                        ) {
                            ui.episode4HelpActiveHintLetter
                        } else {
                            null
                        }
                    val imageMatchExtraNudgeDp =
                        if (ui.chapterId == TrainingV1Config.CHAPTER_ID &&
                            ui.stationId == TrainingV1Config.STATION_WHICH_WORD_STARTS_WITH_LETTER &&
                            ui.trainingRoundIndex == 9
                        ) {
                            SixStationArcHalfCmNudge
                        } else {
                            0.dp
                        }
                    val effectiveStationUiSpec =
                        if (imageMatchExtraNudgeDp != 0.dp) {
                            ui.stationUiSpec.copy(
                                imageMatchVerticalNudgeDp = ui.stationUiSpec.imageMatchVerticalNudgeDp + imageMatchExtraNudgeDp.value,
                            )
                        } else {
                            ui.stationUiSpec
                        }
                    ImageMatchQuestionRenderer(
                        current = current,
                        stationUiSpec = effectiveStationUiSpec,
                        chapter1PlayerAddress = ui.chapter1PlayerAddress,
                        isCompactLandscapePhone = ui.isCompactLandscapePhone,
                        headerInstructionFontScale =
                            when {
                                Season2StationUx.stationKindForGameplayChapter(ui.chapterId, ui.stationId) ==
                                    Season2ChapterStationPlans.StationKind.WhichWordStartsWith ->
                                    1.15f
                                ui.chapterId == TrainingV1Config.CHAPTER_ID -> 1.35f
                                else -> 1.35f * 2f
                            },
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
                        onChoiceWordPreview =
                            if (
                                ui.audioEnabled &&
                                    Season2StationQaPolicy.isWhichWordStartsWithStation(
                                        gameplayChapterId = ui.chapterId,
                                        season2UxStationId = ui.season2Chapter1UxStationId,
                                    )
                            ) {
                                { choiceId ->
                                    ImageMatchActions.handleImageToWordWordPressed(
                                        choiceId = choiceId,
                                        audioEnabled = ui.audioEnabled,
                                        cancelFeedbackVoice = deps.cancelFeedbackVoice,
                                        chapterId = ui.chapterId,
                                        scope = deps.scope,
                                        voice = deps.voice,
                                        rawVoice = deps.rawVoice,
                                        audioRuntime = deps.audioRuntime,
                                    )
                                }
                            } else {
                                null
                            },
                        season2Chapter1UxStationId = ui.season2Chapter1UxStationId,
                        entryPulseScale = state.entryPulseScale,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        is Question.MissingFirstLetterQuestion ->
            Season2MissingFirstLetterGame(
                question = current,
                instructionText =
                    Season2StationThemeCopy.missingFirstLetterInstruction(ui.plan.season2StationTheme),
                enabled = state.enabled,
                shakePx = state.optionsShakePx,
                onPictureTapReplayWord =
                    if (ui.audioEnabled) {
                        { handlers.handleAdvancedReplayWord() }
                    } else {
                        null
                    },
                hintCorrectLetter = current.correctLetter.takeIf { state.wrongTapsThisQuestion >= 2 },
                hintPulseEpoch = state.hintPulseEpoch,
                correctPulseLetter = state.correctTapPulseLetter,
                correctPulseEpoch = state.correctTapPulseEpoch,
                wrongFlashLetter = state.station4WrongFlashLetter,
                wrongFlashEpoch = state.station4WrongFlashEpoch,
                onPickLetter = { picked -> handlers.handleMissingFirstLetterPick(picked) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(state.optionsShakePx.toInt(), 0) },
            )
        is Question.WordPartsQuestion ->
            Season2WordPartsGame(
                question = current,
                instructionText =
                    Season2StationThemeCopy.wordPartsInstruction(
                        presentationMode = current.presentationMode,
                    ),
                enabled = state.enabled,
                completedEquation = state.wordPartsCompletedEquation,
                hintRevealWord = state.wordPartsHintRevealWord,
                onPictureTapReplayWord =
                    if (ui.audioEnabled) {
                        { handlers.handleWordPartsPictureTap() }
                    } else {
                        null
                    },
                onHintRevealAudio =
                    if (ui.audioEnabled) {
                        { handlers.handleWordPartsHintRevealAudio() }
                    } else {
                        null
                    },
                onPickSplit = { split -> handlers.handleWordPartsPick(split) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(state.optionsShakePx.toInt(), 0) },
            )
        is Question.RhymingQuestion ->
            Season2RhymingGame(
                question = current,
                instructionText = Season2StationThemeCopy.rhymingInstruction(ui.plan.season2StationTheme),
                enabled = state.enabled,
                onTargetTapReplayWord =
                    if (ui.audioEnabled) {
                        { handlers.handleAdvancedReplayWord() }
                    } else {
                        null
                    },
                onPickChoice = { choiceId -> handlers.handleRhymingPick(choiceId) },
                modifier = Modifier.fillMaxWidth(),
            )
        is Question.FinaleSlotQuestion ->
            FinaleSlotQuestionRenderer(
                current = current,
                contentKey = deps.session.currentIndex,
                enabled = state.enabled,
                shakeEpoch = state.shakeEpoch,
                onWrongPlacement = {
                    handlers.handleFinaleWrongPlacement()
                    handlers.onWrongFeedback(null, null, false, false)
                },
                onSolved = { words ->
                    deps.gameViewModel.inputLocked = true
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
