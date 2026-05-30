package com.tal.hebrewdino.ui.audio.routing

import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.data.PlayerAddress
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder
import org.junit.Assert.assertEquals
import org.junit.Test

class SampleChapter1AudioRouterTest {
    private val router: GameAudioRouter = SampleChapter1AudioRouter

    @Test
    fun ch1_station1_instruction_girl_then_targetLetter() {
        val plan =
            router.plan(
                AudioEvent.StationInstruction(
                    chapterId = 1,
                    stationId = Chapter1StationOrder.TAP_LETTER,
                    stationTemplateId = null,
                    playerAddress = PlayerAddress.Girl,
                    targetLetter = "א",
                    targetWordCatalogId = null,
                ),
            )
        assertEquals(
            AudioPlan(
                steps =
                    listOf(
                        AudioStep(
                            lane = AudioLane.RawVoice,
                            source = AudioSource.RawRes(R.raw.instruction_pick_letter_short_girl),
                            blocking = true,
                        ),
                        AudioStep(
                            lane = AudioLane.Voice,
                            source = AudioSource.Asset("audio/letter_alef.wav"),
                            blocking = true,
                            delayBeforeMs = 170L,
                        ),
                    ),
            ),
            plan,
        )
    }

    @Test
    fun ch1_station4_instruction_boy_then_targetWord() {
        val catalogId = "w_ב_1"
        val plan =
            router.plan(
                AudioEvent.StationInstruction(
                    chapterId = 1,
                    stationId = Chapter1StationOrder.PICTURE_PICK_ONE,
                    stationTemplateId = null,
                    playerAddress = PlayerAddress.Boy,
                    targetLetter = null,
                    targetWordCatalogId = catalogId,
                ),
            )
        assertEquals(
            AudioPlan(
                steps =
                    listOf(
                        AudioStep(
                            lane = AudioLane.RawVoice,
                            source = AudioSource.RawRes(R.raw.instruction_picture_starts_with_short_boy),
                            blocking = true,
                        ),
                        AudioStep(
                            lane = AudioLane.Voice,
                            source = AudioSource.Asset(AudioClips.wordClipByCatalogId(catalogId)),
                            blocking = true,
                            delayBeforeMs = 170L,
                        ),
                    ),
            ),
            plan,
        )
    }

    @Test
    fun ch1_wrongFeedback_routes_tryAgain_boy() {
        val plan =
            router.plan(
                AudioEvent.WrongFeedback(
                    chapterId = 1,
                    stationId = 1,
                    playerAddress = PlayerAddress.Boy,
                    wrongLetter = "ב",
                    wrongWordCatalogId = null,
                    wrongAlreadySpoken = false,
                ),
            )
        assertEquals(
            AudioPlan(
                steps =
                    listOf(
                        AudioStep(
                            lane = AudioLane.RawVoice,
                            source = AudioSource.RawRes(R.raw.feedback_try_again_boy),
                            blocking = true,
                        ),
                    ),
            ),
            plan,
        )
    }

    @Test
    fun ch1_wrongFeedback_routes_tryAgain_fallbackNeutral_whenNoAddress() {
        val plan =
            router.plan(
                AudioEvent.WrongFeedback(
                    chapterId = 1,
                    stationId = 1,
                    playerAddress = null,
                    wrongLetter = "ב",
                    wrongWordCatalogId = null,
                    wrongAlreadySpoken = false,
                ),
            )
        assertEquals(
            AudioPlan(
                steps =
                    listOf(
                        AudioStep(
                            lane = AudioLane.RawVoice,
                            source = AudioSource.RawRes(R.raw.vo_try_again_neutral),
                            blocking = true,
                        ),
                    ),
            ),
            plan,
        )
    }

    @Test
    fun ch1_correctPraise_routes_to_meule_raw() {
        val plan =
            router.plan(
                AudioEvent.CorrectPraise(
                    chapterId = 1,
                    stationId = 1,
                    playerAddress = PlayerAddress.Girl,
                    avoidNoRepeatKey = null,
                ),
            )
        assertEquals(
            AudioPlan(
                steps =
                    listOf(
                        AudioStep(
                            lane = AudioLane.RawVoice,
                            source = AudioSource.RawRes(R.raw.vo_praise_meule),
                            blocking = true,
                        ),
                    ),
                noImmediateRepeatKey = "ch1_praise_meule",
            ),
            plan,
        )
    }

    @Test
    fun ch1_stationReward_station5_maps_to_success5() {
        val plan =
            router.plan(
                AudioEvent.StationReward(
                    chapterId = 1,
                    stationId = 5,
                ),
            )
        assertEquals(
            AudioPlan(
                steps =
                    listOf(
                        AudioStep(
                            lane = AudioLane.RawVoice,
                            source = AudioSource.RawRes(R.raw.dino_success_station_5),
                            blocking = true,
                        ),
                    ),
            ),
            plan,
        )
    }
}
