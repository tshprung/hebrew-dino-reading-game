package com.tal.hebrewdino.ui.audio

object AudioClips {
    // System voice lines
    const val VoStart = "audio/vo_start.wav"
    const val VoChooseLetter = "audio/vo_choose_letter.wav"
    /** Episode 4 station 1: spoken "בחור את האות" (then letter name clip). Add `assets/audio/vo_bachor_et_haot.wav`; falls back to [VoChooseLetter] if missing. */
    const val VoBachorEtHaot = "audio/vo_bachor_et_haot.wav"
    /** Optional "מצא את האות"; station 1 intro may alternate with [VoChooseLetter]. */
    const val VoFindLetter = "audio/vo_find_letter.wav"
    const val VoClickLetter = "audio/vo_click_letter.wav"
    const val VoWhichLetter = "audio/vo_which_letter.wav"
    const val VoListenChoose = "audio/vo_listen_choose.wav"
    const val VoGoodJob1 = "audio/vo_good_job_1.wav"
    const val VoGoodJob2 = "audio/vo_good_job_2.wav"
    const val VoTryAgain1 = "audio/vo_try_again_1.wav"
    const val VoTryAgain2 = "audio/vo_try_again_2.wav"
    /** Short praise (optional): e.g. "יפה!". */
    const val VoNice1 = "audio/vo_nice_1.wav"
    /** Optional spoken "כל הכבוד!" — place at `assets/audio/vo_kol_hakavod.wav` (ASCII filename). */
    const val VoKolHakavod = "audio/vo_kol_hakavod.wav"
    /** Optional e.g. "יפה מאוד!"; station 1 plays after letter name on correct when file exists. */
    const val VoYafeMeod = "audio/vo_yafe_meod.wav"
    // Optional extra short praise clips (record to add variety)
    const val VoPraiseMetzuyan = "audio/vo_praise_metzuyan.wav" // "מצוין!"
    const val VoPraiseYofi = "audio/vo_praise_yofi.wav" // "יופי!"
    const val VoPraiseHitzlacht = "audio/vo_praise_hitzlacht.wav" // "הצלחת!"
    const val VoLevelDone = "audio/vo_level_done.wav"

    /** Episode 1 station 5 prefix: "איזו מילה מתחילה באות". */
    const val WhichWordStartsWithLetter = "audio/find_word_starts_with_letter.wav"

    /** Episode 1 station 4: "באיזו אות מתחילה המילה" (then play the word clip, e.g. "ברווז"). */
    const val WhichLetterDoesWordStart = "audio/which_letter_does_word_start.wav"

    /** Episode 1 station 6: "ליחצו על אות והמילה שמתחילה באותה האות". */
    const val MatchLetterToWordInstructions = "audio/match_letter_to_word_instructions.wav"
    /** Station 6 (image → word): "איזו מילה מתאימה לתמונה?" (generic/shared). */
    const val ImageToWordInstructions = "audio/image_to_word_instructions.wav"
    /** Episode 3 station 6: dedicated recording (optional). */
    const val Ch3ImageToWordInstructions = "audio/ch3_image_to_word_instructions.wav"

    // Episode 3 — optional sentence-based instructions (recordings; fall back to legacy prompts if missing).
    const val Ch3St1PictureStartsWithInstruction = "audio/ch3_st1_picture_starts_with.wav"
    const val Ch3St2MatchLetterToWordInstruction = "audio/ch3_st2_match_letter_to_word.wav"
    const val Ch3St3PopAllLettersInWordInstruction = "audio/ch3_st3_pop_all_letters_in_word.wav"
    const val Ch3St4FindHighlightedLetterInWordInstruction = "audio/ch3_st4_find_highlighted_letter_in_word.wav"
    const val Ch3St5AudioLetterRecognitionInstruction = "audio/ch3_st5_audio_letter_recognition.wav"
    const val Ch3St6ImageMatchWordInstruction = "audio/ch3_st6_image_match_word.wav"
    // (Old ch3 instruction constants removed/renamed; keep files if you already recorded them — they just won't be used.)

    /** Episode 1 station 2 prefix: "פוצץ את הבלונים עם האות". */
    const val PopBalloonsWithLetter = "audio/pop_balloons_with_letter.wav"

    /** Wrong-tap prefix: "זה". Played before the tapped letter name. */
    const val ThisIsPrefix = "audio/this_is.wav"

    // SFX (optional). If missing from assets, playback will be skipped.
    const val SfxBalloonPop = "audio/sfx_pop.wav"
    /** Softer balloon pop/plop for kids (optional). */
    const val SfxBalloonPopSoft = "audio/sfx_pop_soft.wav"
    /** Funny wrong-pop sound for balloons (optional). */
    const val SfxBalloonPopWrongFunny = "audio/sfx_pop_wrong_funny.wav"

    /**
     * Episode 1 station 2 (preferred natural pops — provide these assets).
     * Keep to 2–3 variants total so the feel stays consistent.
     */
    const val SfxStation2PopSoft1 = "audio/sfx_st2_pop_soft_1.wav"
    const val SfxStation2PopSoft2 = "audio/sfx_st2_pop_soft_2.wav"
    const val SfxStation2PopPlop = "audio/sfx_st2_pop_plop.wav"
    /** Optional: slightly “special” last-balloon pop (still soft). */
    const val SfxStation2PopFinale = "audio/sfx_st2_pop_finale.wav"

    /**
     * Correct balloon pop playlist for Episode 1 station 2.
     * We prefer the “natural” Station 2 assets above; if they are missing, we fall back to the existing generic pops.
     */
    fun station2CorrectPopPlaylist(variant: Int, finale: Boolean): Array<String> {
        if (finale) {
            return arrayOf(SfxStation2PopFinale, SfxStation2PopPlop, SfxStation2PopSoft1, SfxBalloonPopSoft)
        }
        return when (variant % 2) {
            0 -> arrayOf(SfxStation2PopSoft1, SfxBalloonPopSoft, SfxBalloonPop)
            else -> arrayOf(SfxStation2PopSoft2, SfxBalloonPopSoft, SfxBalloonPop)
        }
    }

    /** Wrong balloon: plop first, then funny (no fallback). */
    fun station2WrongPopPlaylist(@Suppress("UNUSED_PARAMETER") balloonIndex: Int): Array<String> {
        return arrayOf(SfxStation2PopPlop, SfxBalloonPopWrongFunny)
    }
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
    /** Episode 4 finale: clue narration (record `audio/story_ch4_clue_outro.wav`; optional until present). */
    const val StoryCh4ClueOutro = "audio/story_ch4_clue_outro.wav"
    /** Episode 5 (optional narration). */
    const val StoryCh5Intro = "audio/story_ch5_intro.wav"
    const val StoryCh5ThirdEggOutro = "audio/story_ch5_third_egg_outro.wav"
    // Mid-chapter boost (after station 3)
    const val StoryCh1MidBoost = "audio/story_ch1_mid_boost.wav"
    const val StoryCh2MidBoost = "audio/story_ch2_mid_boost.wav"
    /** Episode 3: mid-chapter encouragement after station 3. */
    const val StoryCh3MidBoost = "audio/story_ch3_mid_boost.wav"
    const val StoryCh4MidBoost = "audio/story_ch4_mid_boost.wav"
    const val StoryCh5MidBoost = "audio/story_ch5_mid_boost.wav"

    /**
     * Optional per-word voice lines, keyed by catalog entry id (e.g. `w_ב_1`).
     *
     * Filename convention we use on disk (ASCII-only to avoid Windows/encoding issues):
     * `audio/word_w_<letterName>_<number>.wav`, e.g. `audio/word_w_alef_1.wav`.
     */
    fun wordClipByCatalogId(catalogEntryId: String): String {
        // Expected ids: w_<HEBREW_LETTER>_<N>
        val parts = catalogEntryId.split("_")
        if (parts.size == 3 && parts[0] == "w") {
            val heb = parts[1]
            val n = parts[2]
            val name =
                when (heb) {
                    "א" -> "alef"
                    "ב" -> "bet"
                    "ג" -> "gimel"
                    "ד" -> "dalet"
                    "ה" -> "heh"
                    "ו" -> "vav"
                    "ח" -> "chet"
                    "ט" -> "tet"
                    "י" -> "yod"
                    "כ" -> "kaf"
                    "ל" -> "lamed"
                    "מ" -> "mem"
                    "נ" -> "nun"
                    "פ" -> "peh"
                    "צ" -> "tsadi"
                    "ק" -> "kuf"
                    "ר" -> "reish"
                    "ש" -> "shin"
                    "ת" -> "taf"
                    else -> null
                }
            if (name != null && n.isNotBlank()) {
                return "audio/word_w_${name}_${n}.wav"
            }
        }
        // Fallback: legacy / direct-id naming.
        return "audio/word_${catalogEntryId}.wav"
    }

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
            "ו" -> "audio/choose_vav.wav"
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
            "ו" -> "audio/letter_vav.wav"
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

    /**
     * Optional single clip: wrong balloon / wrong tap sentence (e.g. letter name + "נסה שוב").
     * Used for Episode 1 station 2 wrong balloons when present.
     */
    fun wrongSentenceClip(letter: String): String? =
        when (letter) {
            "א" -> "audio/wrong_sentence_alef.wav"
            "ב" -> "audio/wrong_sentence_bet.wav"
            "ג" -> "audio/wrong_sentence_gimel.wav"
            "ד" -> "audio/wrong_sentence_dalet.wav"
            "ה" -> "audio/wrong_sentence_heh.wav"
            "ל" -> "audio/wrong_sentence_lamed.wav"
            "מ" -> "audio/wrong_sentence_mem.wav"
            else -> null
        }

    /**
     * Station 1 (episode 1) wrong: optional single clip "<letter>, נסה שוב" for gapless playback.
     */
    fun station1WrongCombined(letter: String): String? =
        when (letter) {
            "א" -> "audio/st1_wrong_alef.wav"
            "ב" -> "audio/st1_wrong_bet.wav"
            "ג" -> "audio/st1_wrong_gimel.wav"
            "ד" -> "audio/st1_wrong_dalet.wav"
            "ה" -> "audio/st1_wrong_heh.wav"
            "ל" -> "audio/st1_wrong_lamed.wav"
            "מ" -> "audio/st1_wrong_mem.wav"
            else -> null
        }

    /**
     * Station 1 correct: after the letter clip, play the first loadable tail (caller shuffles for variety).
     */
    fun station1CorrectPraiseTailCandidates(): Array<String> =
        arrayOf(
            VoYafeMeod,
            VoKolHakavod,
            VoNice1,
            VoGoodJob2,
            VoGoodJob1,
            VoPraiseYofi,
            VoPraiseMetzuyan,
            VoPraiseHitzlacht,
        )

    /**
     * After [VoLevelDone] on the "שלב הסתיים" reward screen — caller shuffles, then [VoicePlayer.playFirstAvailableBlocking].
     * Wider than "יפה" alone so the tail feels varied.
     */
    fun rewardStagePraiseTailCandidates(): Array<String> =
        arrayOf(
            VoKolHakavod,
            VoNice1,
            VoYafeMeod,
            VoGoodJob2,
            VoGoodJob1,
            VoPraiseMetzuyan,
            VoPraiseYofi,
            VoPraiseHitzlacht,
        )
}

