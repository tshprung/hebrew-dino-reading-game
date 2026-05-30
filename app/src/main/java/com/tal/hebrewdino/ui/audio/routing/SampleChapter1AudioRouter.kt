package com.tal.hebrewdino.ui.audio.routing

import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.audio.AudioClips
import com.tal.hebrewdino.ui.data.PlayerAddress
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder

object SampleChapter1AudioRouter : GameAudioRouter {
    private const val InstructionToTargetGapMs: Long = 170L

    override fun plan(event: AudioEvent): AudioPlan {
        return when (event) {
            is AudioEvent.StationInstruction -> planStationInstruction(event)
            is AudioEvent.WrongFeedback -> planWrongFeedback(event)
            is AudioEvent.CorrectPraise -> planCorrectPraise(event)
            is AudioEvent.StationReward -> planStationReward(event)
            else -> AudioPlan.Empty
        }
    }

    private fun planStationInstruction(event: AudioEvent.StationInstruction): AudioPlan {
        if (event.chapterId != 1) return AudioPlan.Empty
        val address = event.playerAddress ?: return AudioPlan.Empty

        return when (event.stationId) {
            Chapter1StationOrder.TAP_LETTER -> {
                val target = event.targetLetter ?: return AudioPlan.Empty
                AudioPlan(
                    steps =
                        listOf(
                            AudioStep(
                                lane = AudioLane.RawVoice,
                                source = AudioSource.RawRes(instructionPickLetterRaw(address)),
                                blocking = true,
                            ),
                            AudioStep(
                                lane = AudioLane.Voice,
                                source = AudioSource.Asset(AudioClips.letterNameClip(target) ?: return AudioPlan.Empty),
                                blocking = true,
                                delayBeforeMs = InstructionToTargetGapMs,
                            ),
                        ),
                )
            }
            Chapter1StationOrder.PICTURE_PICK_ONE -> {
                val catalogId = event.targetWordCatalogId ?: return AudioPlan.Empty
                AudioPlan(
                    steps =
                        listOf(
                            AudioStep(
                                lane = AudioLane.RawVoice,
                                source = AudioSource.RawRes(instructionPictureStartsWithRaw(address)),
                                blocking = true,
                            ),
                            AudioStep(
                                lane = AudioLane.Voice,
                                source = AudioSource.Asset(AudioClips.wordClipByCatalogId(catalogId)),
                                blocking = true,
                                delayBeforeMs = InstructionToTargetGapMs,
                            ),
                        ),
                )
            }
            else -> AudioPlan.Empty
        }
    }

    private fun planWrongFeedback(event: AudioEvent.WrongFeedback): AudioPlan {
        if (event.chapterId != 1) return AudioPlan.Empty
        val rawResId =
            when (event.playerAddress) {
                PlayerAddress.Boy -> R.raw.feedback_try_again_boy
                PlayerAddress.Girl -> R.raw.feedback_try_again_girl
                null -> R.raw.vo_try_again_neutral
            }
        return AudioPlan(
            steps =
                listOf(
                    AudioStep(
                        lane = AudioLane.RawVoice,
                        source = AudioSource.RawRes(rawResId),
                        blocking = true,
                    ),
                ),
        )
    }

    private fun planCorrectPraise(event: AudioEvent.CorrectPraise): AudioPlan {
        if (event.chapterId != 1) return AudioPlan.Empty
        return AudioPlan(
            steps =
                listOf(
                    AudioStep(
                        lane = AudioLane.RawVoice,
                        source = AudioSource.RawRes(R.raw.vo_praise_meule),
                        blocking = true,
                    ),
                ),
            noImmediateRepeatKey = "ch1_praise_meule",
        )
    }

    private fun planStationReward(event: AudioEvent.StationReward): AudioPlan {
        if (event.chapterId != 1) return AudioPlan.Empty
        val sid = event.stationId.coerceIn(1, 6)
        val rawResId =
            when (sid) {
                1 -> R.raw.dino_success_station_1
                2 -> R.raw.dino_success_station_2
                3 -> R.raw.dino_success_station_3
                4 -> R.raw.dino_success_station_4
                5 -> R.raw.dino_success_station_5
                6 -> R.raw.dino_success_station_6
                else -> R.raw.dino_success_station_1
            }
        return AudioPlan(
            steps =
                listOf(
                    AudioStep(
                        lane = AudioLane.RawVoice,
                        source = AudioSource.RawRes(rawResId),
                        blocking = true,
                    ),
                ),
        )
    }

    private fun instructionPickLetterRaw(address: PlayerAddress): Int {
        return when (address) {
            PlayerAddress.Boy -> R.raw.instruction_pick_letter_short_boy
            PlayerAddress.Girl -> R.raw.instruction_pick_letter_short_girl
        }
    }

    private fun instructionPictureStartsWithRaw(address: PlayerAddress): Int {
        return when (address) {
            PlayerAddress.Boy -> R.raw.instruction_picture_starts_with_short_boy
            PlayerAddress.Girl -> R.raw.instruction_picture_starts_with_short_girl
        }
    }
}
