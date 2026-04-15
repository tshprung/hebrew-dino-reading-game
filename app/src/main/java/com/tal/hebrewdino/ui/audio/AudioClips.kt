package com.tal.hebrewdino.ui.audio

object AudioClips {
    // System voice lines
    const val VoStart = "audio/vo_start.wav"
    const val VoChooseLetter = "audio/vo_choose_letter.wav"
    const val VoClickLetter = "audio/vo_click_letter.wav"
    const val VoWhichLetter = "audio/vo_which_letter.wav"
    const val VoListenChoose = "audio/vo_listen_choose.wav"
    const val VoGoodJob1 = "audio/vo_good_job_1.wav"
    const val VoGoodJob2 = "audio/vo_good_job_2.wav"
    const val VoTryAgain1 = "audio/vo_try_again_1.wav"
    const val VoTryAgain2 = "audio/vo_try_again_2.wav"
    const val VoLevelDone = "audio/vo_level_done.wav"

    // Letter-specific
    fun chooseLetterClip(letter: String): String? =
        when (letter) {
            "א" -> "audio/choose_alef.wav"
            "ב" -> "audio/choose_bet.wav"
            "מ" -> "audio/choose_mem.wav"
            "ל" -> "audio/choose_lamed.wav"
            "ד" -> "audio/choose_dalet.wav"
            else -> null
        }

    fun letterNameClip(letter: String): String? =
        when (letter) {
            "א" -> "audio/letter_alef.wav"
            "ב" -> "audio/letter_bet.wav"
            "מ" -> "audio/letter_mem.wav"
            "ל" -> "audio/letter_lamed.wav"
            "ד" -> "audio/letter_dalet.wav"
            else -> null
        }
}

