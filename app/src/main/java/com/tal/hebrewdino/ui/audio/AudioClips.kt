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

    /** Prefix prompt recorded once: "מצאו את האות". */
    const val FindTheLetter = "audio/find_the_letter.wav"

    /** Episode 1 station 5 prefix: "איזו מילה מתחילה באות". */
    const val WhichWordStartsWithLetter = "audio/which_word_starts_with_letter.wav"

    /** Episode 1 station 6: "חברו בין אות למילה המתאימה". */
    const val MatchLetterToWordInstructions = "audio/match_letter_to_word_instructions.wav"

    // SFX (optional). If missing from assets, playback will be skipped.
    const val SfxBalloonPop = "audio/sfx_pop.wav"
    const val SfxCorrect = "audio/sfx_correct.wav"
    const val SfxWrong = "audio/sfx_wrong.wav"

    // Story narration (optional).
    // Episode 1 (forest)
    const val StoryForestIntro = "audio/episode1_intro.wav"
    const val StoryEggOutro = "audio/episode1_outro.wav"
    // Episode 2–4 (optional, if you record them later)
    const val StoryMountainApproachIntro = "audio/story_mountain_approach_intro.wav"
    const val StoryMountainApproachOutro = "audio/story_mountain_approach_outro.wav"
    const val StoryMountainPathIntro = "audio/story_mountain_path_intro.wav"
    const val StoryMountainPathOutro = "audio/story_mountain_path_outro.wav"
    const val StoryCh4Intro = "audio/story_ch4_intro.wav"
    const val StoryCh4HomeOutro = "audio/story_ch4_home_outro.wav"

    /** Optional per-word voice lines, keyed by catalog entry id (e.g. `w_ב_1`). */
    fun wordClipByCatalogId(catalogEntryId: String): String = "audio/word_${catalogEntryId}.wav"

    // Letter-specific
    fun chooseLetterClip(letter: String): String? =
        when (letter) {
            "א" -> "audio/choose_alef.wav"
            "ב" -> "audio/choose_bet.wav"
            "ג" -> "audio/choose_gimel.wav"
            "מ" -> "audio/choose_mem.wav"
            "ל" -> "audio/choose_lamed.wav"
            "ד" -> "audio/choose_dalet.wav"
            "ה" -> "audio/choose_heh.wav"
            "ח" -> "audio/choose_chet.wav"
            "ט" -> "audio/choose_tet.wav"
            "י" -> "audio/choose_yod.wav"
            "כ" -> "audio/choose_kaf.wav"
            "נ" -> "audio/choose_nun.wav"
            "פ" -> "audio/choose_peh.wav"
            "צ" -> "audio/choose_tsadi.wav"
            "ק" -> "audio/choose_kuf.wav"
            "ר" -> "audio/choose_reish.wav"
            "ש" -> "audio/choose_shin.wav"
            "ת" -> "audio/choose_taf.wav"
            else -> null
        }

    fun letterNameClip(letter: String): String? =
        when (letter) {
            "א" -> "audio/letter_alef.wav"
            "ב" -> "audio/letter_bet.wav"
            "ג" -> "audio/letter_gimel.wav"
            "ד" -> "audio/letter_dalet.wav"
            "ה" -> "audio/letter_heh.wav"
            "ח" -> "audio/letter_chet.wav"
            "ט" -> "audio/letter_tet.wav"
            "י" -> "audio/letter_yod.wav"
            "כ" -> "audio/letter_kaf.wav"
            "מ" -> "audio/letter_mem.wav"
            "ל" -> "audio/letter_lamed.wav"
            "נ" -> "audio/letter_nun.wav"
            "פ" -> "audio/letter_peh.wav"
            "צ" -> "audio/letter_tsadi.wav"
            "ק" -> "audio/letter_kuf.wav"
            "ר" -> "audio/letter_reish.wav"
            "ש" -> "audio/letter_shin.wav"
            "ת" -> "audio/letter_taf.wav"
            else -> null
        }
}

