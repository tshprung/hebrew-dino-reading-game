package com.tal.hebrewdino.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import com.tal.hebrewdino.ui.components.ChapterNavChipStyles
import com.tal.hebrewdino.ui.audio.AppForegroundAudio
import com.tal.hebrewdino.ui.audio.InteractionAudio
import com.tal.hebrewdino.ui.audio.TextToSpeechManager
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.domain.economy.FoodInventoryEntry
import com.tal.hebrewdino.ui.domain.economy.GrowthStage
import com.tal.hebrewdino.ui.domain.economy.PlayerWallet
import com.tal.hebrewdino.ui.domain.economy.TamagotchiRules
import com.tal.hebrewdino.ui.audio.DinoMediaClips
import com.tal.hebrewdino.ui.audio.RawVoicePlayer
import com.tal.hebrewdino.ui.audio.SfxManager
import com.tal.hebrewdino.ui.components.lottie.DinoLottieVisual
import com.tal.hebrewdino.ui.components.lottie.EggLottiePhase
import com.tal.hebrewdino.ui.components.lottie.EggLottieVisual
import com.tal.hebrewdino.ui.components.particles.ConfettiBurstOverlay
import com.tal.hebrewdino.ui.layout.topChromeInsetsPadding
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import com.tal.hebrewdino.R

@Composable
fun DinoHomeScreen(
    viewModel: DinoHomeViewModel,
    onGoOnMission: () -> Unit,
    onBackToIntro: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val character by viewModel.character.collectAsState()
    val wallet by viewModel.wallet.collectAsState()
    val equippedAccessory by viewModel.equippedAccessory.collectAsState()
    val pendingAccessoryEquip by viewModel.pendingAccessoryEquip.collectAsState()
    val ownedAccessories by viewModel.ownedAccessories.collectAsState()
    val highlightMissionButton by viewModel.highlightMissionButton.collectAsState()
    val eggTapCount by viewModel.eggTapCount.collectAsState()
    val foodInventory by viewModel.foodInventory.collectAsState()
    val homeSpeechEpoch = viewModel.homeSpeechEpoch
    var eatPulseEpoch by remember { mutableIntStateOf(0) }

    DinoHomeContent(
        character = character,
        wallet = wallet,
        isDataReady = viewModel.isDataReady,
        homePromptText = wallet?.let(viewModel::homePromptText).orEmpty(),
        homePromptSpeech = wallet?.let(viewModel::homePromptSpokenForTts).orEmpty(),
        entryHomeSpeech = wallet?.let(viewModel::entryHomeSpokenForTts).orEmpty(),
        periodicHomeReminderSpeech = wallet?.let(viewModel::periodicHomeReminderSpokenForTts).orEmpty(),
        homeReminderEpoch = viewModel.homeReminderEpoch,
        suppressPeriodicReminders = viewModel.suppressPeriodicReminders,
        homeSpeechEpoch = homeSpeechEpoch,
        onHomeSpeechPlayed = viewModel::onHomeSpeechPlayed,
        playBabyChirpBeforeEntry = viewModel::shouldPlayBabyChirpBeforeEntrySpeech,
        eggTapCount = eggTapCount,
        onGoOnMission = onGoOnMission,
        onBackToIntro = onBackToIntro,
        hatchEpoch = viewModel.hatchEpoch,
        growEpoch = viewModel.growEpoch,
        eggTapEpoch = viewModel.eggTapEpoch,
        onEggTapped = viewModel::onEggTapped,
        eatPulseEpoch = eatPulseEpoch,
        equippedAccessoryId = equippedAccessory,
        pendingAccessoryEquipId = pendingAccessoryEquip,
        ownedAccessoryIds = ownedAccessories,
        highlightMissionButton = highlightMissionButton,
        onPendingAccessoryEquipped = viewModel::completePendingAccessoryEquip,
        onOwnedAccessoryTap = viewModel::equipOwnedAccessory,
        confettiTrigger = viewModel.confettiTrigger,
        foodInventory = foodInventory,
        onFeed = { foodEmoji ->
            viewModel.feedOnce(foodEmoji)
            eatPulseEpoch += 1
        },
        modifier = modifier,
    )
}

@Composable
private fun DinoHomeContent(
    character: DinoCharacter,
    wallet: PlayerWallet?,
    isDataReady: Boolean,
    homePromptText: String,
    homePromptSpeech: String,
    entryHomeSpeech: String,
    periodicHomeReminderSpeech: String,
    homeReminderEpoch: Int,
    suppressPeriodicReminders: Boolean,
    homeSpeechEpoch: Int,
    onHomeSpeechPlayed: () -> Unit,
    playBabyChirpBeforeEntry: () -> Boolean,
    eggTapCount: Int,
    onGoOnMission: () -> Unit,
    onBackToIntro: () -> Unit,
    hatchEpoch: Int,
    growEpoch: Int,
    eggTapEpoch: Int,
    onEggTapped: () -> Unit,
    eatPulseEpoch: Int,
    equippedAccessoryId: String?,
    pendingAccessoryEquipId: String?,
    ownedAccessoryIds: Set<String>,
    highlightMissionButton: Boolean,
    onPendingAccessoryEquipped: () -> Unit,
    onOwnedAccessoryTap: (String) -> Unit,
    confettiTrigger: Int,
    foodInventory: List<FoodInventoryEntry>,
    onFeed: (foodEmoji: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val appContext = remember(context) { context.applicationContext }
    val stopInteractionAudio = remember(appContext) { { InteractionAudio.stopAllNow(appContext) } }
    val sfx = remember(appContext) { SfxManager(appContext) }
    val rawVoice = remember(appContext) { RawVoicePlayer(appContext) }
    val tts = remember(appContext) { TextToSpeechManager.get(appContext) }
    val scope = rememberCoroutineScope()
    val isInForeground by AppForegroundAudio.isInForeground.collectAsState()
    DisposableEffect(sfx, rawVoice) {
        onDispose {
            sfx.release()
            rawVoice.release()
        }
    }
    DisposableEffect(Unit) {
        onDispose { tts.stop() }
    }

    val growthStage = wallet?.growthStage ?: GrowthStage.EGG
    val isHungry = wallet?.isHungry ?: false
    val growthProgress01 = wallet?.growthProgress01 ?: 0f
    val fedFoodTotal = wallet?.fedTotal ?: 0
    val canFeed = foodInventory.isNotEmpty()

    LaunchedEffect(isDataReady, entryHomeSpeech, homeSpeechEpoch, isInForeground) {
        if (!isDataReady || !isInForeground || entryHomeSpeech.isBlank()) return@LaunchedEffect
        if (playBabyChirpBeforeEntry()) {
            sfx.playBabyChirp()
        }
        tts.speakFully(entryHomeSpeech, navigationSettleMs = 0L)
        onHomeSpeechPlayed()
    }

    LaunchedEffect(eggTapCount) {
        if (eggTapCount == TamagotchiRules.EGG_TAPS_TO_HATCH) {
            sfx.playEggCrack()
        }
    }

    var hungryCuePlayed by remember { mutableStateOf(false) }
    LaunchedEffect(isHungry, growthStage) {
        if (growthStage == GrowthStage.EGG) return@LaunchedEffect
        if (isHungry && !hungryCuePlayed) {
            hungryCuePlayed = true
            if (rawVoice.isPlayable(DinoMediaClips.whimperResId)) {
                rawVoice.playBlocking(DinoMediaClips.whimperResId)
            } else {
                sfx.playHungryWhimper()
            }
        }
        if (!isHungry) {
            hungryCuePlayed = false
        }
    }

    LaunchedEffect(
        isDataReady,
        foodInventory,
        periodicHomeReminderSpeech,
        isInForeground,
        homeReminderEpoch,
        homeSpeechEpoch,
        suppressPeriodicReminders,
    ) {
        if (!isDataReady || periodicHomeReminderSpeech.isBlank()) return@LaunchedEffect
        delay(DinoHomeViewModel.HOME_REMINDER_INTERVAL_MS)
        while (isActive) {
            if (!isInForeground || suppressPeriodicReminders) {
                delay(500L)
                continue
            }
            tts.interruptAndSpeak(periodicHomeReminderSpeech)
            delay(DinoHomeViewModel.HOME_REMINDER_INTERVAL_MS)
        }
    }
    val onEggTap =
        remember(onEggTapped, scope, sfx) {
            {
                scope.launch { sfx.playEggTap() }
                onEggTapped()
            }
        }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.forest_bg_journey_road),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
        )

        ConfettiBurstOverlay(
            trigger = confettiTrigger,
            modifier = Modifier.fillMaxSize(),
        )

        OwnedAccessoriesRail(
            ownedIds = ownedAccessoryIds,
            equippedId = equippedAccessoryId,
            pendingEquipId = pendingAccessoryEquipId,
            onAccessoryTap = { id ->
                stopInteractionAudio()
                onOwnedAccessoryTap(id)
            },
            modifier = Modifier.align(Alignment.CenterStart),
        )

        pendingAccessoryEquipId?.let { giftId ->
            PendingAccessorySideGift(
                accessoryId = giftId,
                onEquipAnimationFinished = onPendingAccessoryEquipped,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Box(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .topChromeInsetsPadding()
                    .padding(top = 4.dp, start = 8.dp),
        ) {
            OutlinedButton(
                onClick = {
                    stopInteractionAudio()
                    onBackToIntro()
                },
                colors = ChapterNavChipStyles.outlinedButtonColors(),
            ) {
                Text("חזור", style = ChapterNavChipStyles.labelTextStyle())
            }
        }

        Box(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .topChromeInsetsPadding()
                    .padding(top = 10.dp, end = 12.dp),
        ) {
            FoodInventoryRail(
                items = foodInventory,
                pulseWhenFeedable = canFeed,
                onFeed = { emoji ->
                    scope.launch { sfx.playChew() }
                    onFeed(emoji)
                },
            )
        }

        HungerChip(
            growthStage = growthStage,
            isHungry = isHungry,
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .topChromeInsetsPadding()
                    .padding(top = 58.dp, start = 12.dp),
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(1.dp))

            DinoCenterVisual(
                character = character,
                growthStage = growthStage,
                isHungry = isHungry,
                hatchEpoch = hatchEpoch,
                growEpoch = growEpoch,
                eggTapEpoch = eggTapEpoch,
                eatPulseEpoch = eatPulseEpoch,
                equippedAccessoryId = equippedAccessoryId,
                onEggTapped = onEggTap,
                onDinoTapped = {
                    stopInteractionAudio()
                    if (homePromptText.isNotBlank()) {
                        tts.interruptAndSpeak(homePromptSpeech)
                    }
                },
                modifier = Modifier.weight(1f, fill = true),
            )

            GrowthBar(
                growthProgress01 = growthProgress01,
                growthStage = growthStage,
                modifier =
                    Modifier
                        .clickable {
                            stopInteractionAudio()
                            if (homePromptText.isNotBlank()) {
                                tts.interruptAndSpeak(homePromptSpeech)
                            }
                        }
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
            )

            GrowthHint(
                growthStage = growthStage,
                fedFoodTotal = fedFoodTotal,
                modifier =
                    Modifier
                        .clickable {
                            stopInteractionAudio()
                            if (homePromptText.isNotBlank()) {
                                tts.interruptAndSpeak(homePromptSpeech)
                            }
                        }
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
            )

            ThemedActionButton(
                text = "צא למשימה",
                onClick = {
                    stopInteractionAudio()
                    onGoOnMission()
                },
                highlighted = highlightMissionButton,
                modifier =
                    Modifier
                        .fillMaxWidth(0.96f)
                        .height(64.dp)
                        .padding(bottom = 6.dp),
            )
        }
    }
}

@Composable
private fun HungerChip(
    growthStage: GrowthStage,
    isHungry: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = Color.Black.copy(alpha = 0.28f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val label =
                when {
                    growthStage == GrowthStage.EGG -> "ביצה"
                    growthStage == GrowthStage.ADULT && !isHungry -> "בוגר"
                    isHungry -> "רעב"
                    else -> "מלא ושמח"
                }
            val labelColor =
                when {
                    growthStage == GrowthStage.EGG -> Color(0xFFFFE27A)
                    growthStage == GrowthStage.ADULT && !isHungry -> Color(0xFFFFE27A)
                    isHungry -> Color(0xFFFF6B6B)
                    else -> Color(0xFFB6F2C1)
                }
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = labelColor,
            )
        }
    }
}

@Composable
private fun MapChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = Color.Black.copy(alpha = 0.34f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = rtl("למפות"),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun FoodInventoryRail(
    items: List<FoodInventoryEntry>,
    pulseWhenFeedable: Boolean,
    onFeed: (foodEmoji: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val feedPulse =
        if (pulseWhenFeedable) {
            rememberInfiniteTransition(label = "food_feed_pulse")
                .animateFloat(
                    initialValue = 1f,
                    targetValue = 1.06f,
                    animationSpec =
                        infiniteRepeatable(
                            animation = tween(durationMillis = 900, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse,
                        ),
                    label = "food_scale",
                )
                .value
        } else {
            1f
        }
    Row(
        modifier = modifier.scale(feedPulse),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { entry ->
            FoodIconChip(
                emoji = entry.emoji,
                count = entry.count,
                onClick = { onFeed(entry.emoji) },
            )
        }
    }
}

@Composable
private fun FoodIconChip(
    emoji: String,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = Color(0xFF2DB86E).copy(alpha = 0.42f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = Color.White,
            )
        }
    }
}

@Composable
private fun DinoCenterVisual(
    character: DinoCharacter,
    growthStage: GrowthStage,
    isHungry: Boolean,
    hatchEpoch: Int,
    growEpoch: Int,
    eggTapEpoch: Int,
    eatPulseEpoch: Int,
    equippedAccessoryId: String?,
    onEggTapped: () -> Unit,
    onDinoTapped: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        val eggRes =
            if (character == DinoCharacter.DINA_PINK) {
                R.drawable.egg_pink
            } else {
                R.drawable.egg_white
            }

        var hatchVisible by remember { mutableStateOf(false) }
        val hatchAlpha = remember { Animatable(0f) }
        LaunchedEffect(hatchEpoch) {
            if (hatchEpoch == 0) return@LaunchedEffect
            hatchVisible = true
            hatchAlpha.snapTo(1f)
            hatchAlpha.animateTo(0f, tween(650))
            hatchVisible = false
        }

        when (growthStage) {
            GrowthStage.EGG -> {
                val eggSize = maxWidth.coerceAtMost(280.dp)
                EggLottieVisual(
                    character = character,
                    phase = EggLottiePhase.IDLE_WIGGLE,
                    tapImpulseEpoch = eggTapEpoch,
                    onEggTapped = onEggTapped,
                    onCrackAnimationEnd = {},
                    size = eggSize,
                )
            }

            GrowthStage.BABY,
            GrowthStage.ADULT,
            -> {
                val base = maxWidth.coerceAtMost(280.dp)
                val size = if (growthStage == GrowthStage.BABY) base * 0.72f else base
                DinoLottieVisual(
                    character = character,
                    isHungry = isHungry,
                    eatPulseEpoch = eatPulseEpoch,
                    growEpoch = growEpoch,
                    equippedAccessoryId = equippedAccessoryId,
                    onDinoTapped = onDinoTapped,
                    size = size,
                )
                if (hatchVisible && hatchAlpha.value > 0f) {
                    Image(
                        painter = androidx.compose.ui.res.painterResource(id = eggRes),
                        contentDescription = null,
                        modifier =
                            Modifier
                                .size(base)
                                .alpha(hatchAlpha.value),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                    )
                }
            }
        }
    }
}

@Composable
private fun GrowthBar(
    growthProgress01: Float,
    growthStage: GrowthStage,
    modifier: Modifier = Modifier,
) {
    val pulse =
        rememberInfiniteTransition(label = "growth_pulse")
            .animateFloat(
                initialValue = 0.88f,
                targetValue = 1f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween<Float>(durationMillis = 650),
                        repeatMode = RepeatMode.Reverse,
                    ),
                label = "pulse",
            )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.Black.copy(alpha = 0.26f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = if (growthStage == GrowthStage.ADULT) "מקסימום!" else "גדילה",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, fontSize = 20.sp),
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            LinearProgressIndicator(
                progress = { growthProgress01.coerceIn(0f, 1f) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .alpha(pulse.value),
                color = if (growthStage == GrowthStage.ADULT) Color(0xFFFFE27A) else Color(0xFF3CB371),
                trackColor = Color.White.copy(alpha = 0.25f),
            )
        }
    }
}

@Composable
private fun GrowthHint(
    growthStage: GrowthStage,
    fedFoodTotal: Int,
    modifier: Modifier = Modifier,
) {
    val text =
        when (growthStage) {
            GrowthStage.EGG -> "הקישו על הביצה - טוק, טוק, טוק!"
            GrowthStage.BABY -> {
                val remaining = (TamagotchiRules.APPLES_TO_ADULT - fedFoodTotal).coerceAtLeast(0)
                if (remaining <= 0) {
                    "דינו גדל!"
                } else if (remaining == 1) {
                    "עוד תפוח אחד ודינו יגדל!"
                } else {
                    "עוד ${appleCountMasculinePhrase(remaining)} ודינו יגדל!"
                }
            }
            GrowthStage.ADULT -> "דינו בוגר! המשיכו להאכיל ולצאת למשימות!"
        }

    val hintBg =
        if (growthStage == GrowthStage.ADULT) {
            Color(0xFFFFE27A).copy(alpha = 0.28f)
        } else {
            Color.Black.copy(alpha = 0.22f)
        }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = hintBg,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = rtl(text),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun ThemedActionButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    highlighted: Boolean = false,
    colors: List<Color> =
        listOf(
            Color(0xFFFFE27A).copy(alpha = 0.62f),
            Color(0xFFFFB82E).copy(alpha = 0.70f),
            Color(0xFFFF9A1A).copy(alpha = 0.76f),
        ),
    modifier: Modifier = Modifier,
) {
    val pillShape = RoundedCornerShape(999.dp)
    val pulseScale =
        if (highlighted && enabled) {
            rememberInfiniteTransition(label = "mission_cta_pulse")
                .animateFloat(
                    initialValue = 1f,
                    targetValue = 1.04f,
                    animationSpec =
                        infiniteRepeatable(
                            animation = tween(durationMillis = 900, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse,
                        ),
                    label = "scale",
                )
                .value
        } else {
            1f
        }
    val gradientColors =
        if (highlighted) {
            listOf(
                Color(0xFF2DB86E).copy(alpha = 0.88f),
                Color(0xFF2DB86E).copy(alpha = 0.92f),
                Color(0xFF1F9A5A).copy(alpha = 0.94f),
            )
        } else {
            colors
        }
    val labelColor = if (highlighted) Color.White else Color(0xFF102A43)

    Surface(
        modifier =
            modifier
                .scale(pulseScale)
                .alpha(if (enabled) 1f else 0.55f)
                .then(
                    if (highlighted) {
                        Modifier.border(3.dp, Color(0xFFFFE27A), pillShape)
                    } else {
                        Modifier
                    },
                ),
        shape = pillShape,
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
                    .background(
                        Brush.verticalGradient(colors = gradientColors),
                        shape = pillShape,
                    )
                    .background(Color.White.copy(alpha = if (highlighted) 0.06f else 0.10f), shape = pillShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = rtl(text),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = labelColor,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun rtl(text: String): String = "\u200F$text"
