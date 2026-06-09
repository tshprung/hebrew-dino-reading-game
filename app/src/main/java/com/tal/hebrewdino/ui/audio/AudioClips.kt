package com.tal.hebrewdino.ui.audio

import android.util.Log
import com.tal.hebrewdino.R

object AudioClips {
    private const val TAG: String = "MissingContent"

    const val VoChooseLetter = "audio/vo_choose_letter.wav"
    /** Episode 4 station 1: spoken "בחור את האות" (then letter name clip). Add `assets/audio/vo_bachor_et_haot.wav`; falls back to [VoChooseLetter] if missing. */
    const val VoBachorEtHaot = "audio/vo_bachor_et_haot.wav"
    /** Optional "מצא את האות"; station 1 intro may alternate with [VoChooseLetter]. */
    const val VoFindLetter = "audio/vo_find_letter.wav"
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
    const val WhichWordStartsWithLetter = "audio/which_word_starts_with_letter.wav"
    const val ChoosePictureStartsWithLetter = "audio/choose_picture_starts_with_letter.wav"

    /** Episode 1 station 4: "באיזו אות מתחילה המילה" (then play the word clip, e.g. "ברווז"). */
    const val WhichLetterDoesWordStart = "audio/which_letter_does_word_start.wav"

    /** Episode 1 station 6: "ליחצו על אות והמילה שמתחילה באותה האות". */
    const val MatchLetterToWordInstructions = "audio/match_letter_to_word_instructions.wav"
    /** Station 6 (image → word): "איזו מילה מתאימה לתמונה?" (generic/shared). */
    const val ImageToWordInstructions = "audio/image_to_word_instructions.wav"
    /** Episode 3 station 6: dedicated recording (optional). */
    const val Ch3ImageToWordInstructions = "audio/ch3_image_to_word_instructions.wav"

    const val Ch3St3PopAllLettersInWordInstruction = "audio/ch3_st3_pop_all_letters_in_word.wav"
    const val Ch3St4FindHighlightedLetterInWordInstruction = "audio/ch3_st4_find_highlighted_letter_in_word.wav"
    // (Old ch3 instruction constants removed/renamed; keep files if you already recorded them — they just won't be used.)

    /** Episode 1 station 2 prefix: "פוצץ את הבלונים עם האות". */
    const val PopBalloonsWithLetter = "audio/pop_balloons_with_letter.wav"
    const val PopAllBalloonsWithLetter = "audio/pop_all_balloons_with_letter.wav"

    /** Season 2 Chapter 1 station 4 (memory match): "מצא זוגות של אותיות זהות". */
    const val Season2Ch1St4MemoryMatchInstructions = "audio/season2_ch1_st4_memory_match_instructions.wav"

    /** Season 2 puzzle map: "עוד חלק מהמפה התגלה!" */
    const val Season2MapPartRevealed = "audio/season2_map_part_revealed.wav"

    /** Season 2 puzzle map after station 5: "אנחנו מתקרבים!" */
    const val Season2MapAlmostDone = "audio/season2_map_almost_done.wav"

    /** Season 2 completed chapter replay hint: "לחצו על ריבוע כדי לשחק שוב". */
    const val Season2ReplayTileInstruction = "audio/season2_replay_tile_instruction.wav"

    /** Season 2 Picture→Word: "בחרו את המילה שמתאימה לתמונה". */
    const val Season2PictureToWordInstructions = "audio/season2_picture_to_word_instructions.wav"

    /** Season 2 MissingFirstLetter: "איזו אות חסרה?". */
    const val Season2MissingFirstLetterInstructions = "audio/season2_missing_first_letter_instructions.wav"

    /** Season 2 WordParts: "איזה חלק משלים את המילה?". */
    const val Season2WordPartsInstructions = "audio/season2_word_parts_instructions.wav"

    /** Season 2 Rhyming: "איזו מילה מתחרזת?". */
    const val Season2RhymingInstructions = "audio/season2_rhyming_instructions.wav"

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
    private val Station2CorrectPopFinalePlaylist =
        arrayOf(SfxStation2PopFinale, SfxStation2PopPlop, SfxStation2PopSoft1, SfxBalloonPopSoft)
    private val Station2CorrectPopVariant0Playlist =
        arrayOf(SfxStation2PopSoft1, SfxBalloonPopSoft, SfxBalloonPop)
    private val Station2CorrectPopVariant1Playlist =
        arrayOf(SfxStation2PopSoft2, SfxBalloonPopSoft, SfxBalloonPop)

    fun station2CorrectPopPlaylist(variant: Int, finale: Boolean): Array<String> {
        if (finale) {
            return Station2CorrectPopFinalePlaylist
        }
        return when (variant % 2) {
            0 -> Station2CorrectPopVariant0Playlist
            else -> Station2CorrectPopVariant1Playlist
        }
    }

    /** Wrong balloon: plop first, then funny (no fallback). */
    private val Station2WrongPopPlaylist = arrayOf(SfxStation2PopPlop, SfxBalloonPopWrongFunny)

    fun station2WrongPopPlaylist(@Suppress("UNUSED_PARAMETER") balloonIndex: Int): Array<String> {
        return Station2WrongPopPlaylist
    }
    const val SfxCorrect = "audio/sfx_correct.wav"
    const val SfxWrong = "audio/sfx_wrong.wav"

    // Story narration
    // (Season 1 Ch3–Ch6 story narration migrated to res/raw MP3 variants; WAV story narration assets removed.)

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
                    "ז" -> "zayin"
                    "כ" -> "kaf"
                    "ל" -> "lamed"
                    "מ" -> "mem"
                    "נ" -> "nun"
                    "פ" -> "peh"
                    "ס" -> "samech"
                    "צ" -> "tsadi"
                    "ק" -> "kuf"
                    "ע" -> "ayin"
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

    private val WordRawResByCatalogId: Map<String, Int> =
        mapOf(
            "w_א_1" to R.raw.word_w_alef_1,
            "w_א_2" to R.raw.word_w_alef_2,
            "w_א_3" to R.raw.word_w_alef_3,
            "w_א_4" to R.raw.word_w_alef_4,
            "w_א_5" to R.raw.word_w_alef_5,
            "w_ב_1" to R.raw.word_w_bet_1,
            "w_ב_2" to R.raw.word_w_bet_2,
            "w_ב_3" to R.raw.word_w_bet_3,
            "w_ג_1" to R.raw.word_w_gimel_1,
            "w_ג_2" to R.raw.word_w_gimel_2,
            "w_ג_3" to R.raw.word_w_gimel_3,
            "w_ג_4" to R.raw.word_w_gimel_4,
            "w_ד_1" to R.raw.word_w_dalet_1,
            "w_ד_2" to R.raw.word_w_dalet_2,
            "w_ד_3" to R.raw.word_w_dalet_3,
            "w_ד_4" to R.raw.word_w_dalet_4,
            "w_ה_1" to R.raw.word_w_heh_1,
            "w_ה_2" to R.raw.word_w_heh_2,
            "w_ה_3" to R.raw.word_w_heh_3,
            "w_ו_1" to R.raw.word_w_vav_1,
            "w_ו_2" to R.raw.word_w_vav_2,
            "w_ו_3" to R.raw.word_w_vav_3,
            "w_ח_1" to R.raw.word_w_chet_1,
            "w_ח_2" to R.raw.word_w_chet_2,
            "w_ח_3" to R.raw.word_w_chet_3,
            "w_ח_4" to R.raw.word_w_chet_4,
            "w_ט_1" to R.raw.word_w_tet_1,
            "w_ט_2" to R.raw.word_w_tet_2,
            "w_ט_3" to R.raw.word_w_tet_3,
            "w_י_2" to R.raw.word_w_yod_2,
            "w_י_3" to R.raw.word_w_yod_3,
            "w_י_4" to R.raw.word_w_yod_4,
            "w_י_5" to R.raw.word_w_yod_5,
            "w_י_6" to R.raw.word_w_yod_6,
            "w_ז_1" to R.raw.word_w_zayin_1,
            "w_ז_2" to R.raw.word_w_zayin_2,
            "w_ז_3" to R.raw.word_w_zayin_3,
            "w_ז_4" to R.raw.word_w_zayin_4,
            "w_ס_1" to R.raw.word_w_samech_1,
            "w_ס_2" to R.raw.word_w_samech_2,
            "w_ס_3" to R.raw.word_w_samech_3,
            "w_ס_4" to R.raw.word_w_samech_4,
            "w_ס_5" to R.raw.word_w_samech_5,
            "w_ע_1" to R.raw.word_w_ayin_1,
            "w_ע_2" to R.raw.word_w_ayin_2,
            "w_ע_3" to R.raw.word_w_ayin_3,
            "w_ע_4" to R.raw.word_w_ayin_4,
            "w_ע_5" to R.raw.word_w_ayin_5,
            "w_ע_6" to R.raw.word_w_ayin_6,
            "w_ע_7" to R.raw.word_w_ayin_7,
            "w_כ_1" to R.raw.word_w_kaf_1,
            "w_כ_2" to R.raw.word_w_kaf_2,
            "w_כ_3" to R.raw.word_w_kaf_3,
            "w_ל_1" to R.raw.word_w_lamed_1,
            "w_ל_2" to R.raw.word_w_lamed_2,
            "w_ל_3" to R.raw.word_w_lamed_3,
            "w_מ_1" to R.raw.word_w_mem_1,
            "w_מ_2" to R.raw.word_w_mem_2,
            "w_מ_3" to R.raw.word_w_mem_3,
            "w_מ_4" to R.raw.word_w_mem_4,
            "w_מ_5" to R.raw.word_w_mem_5,
            "w_נ_1" to R.raw.word_w_nun_1,
            "w_נ_2" to R.raw.word_w_nun_2,
            "w_נ_3" to R.raw.word_w_nun_3,
            "w_נ_4" to R.raw.word_w_nun_4,
            "w_פ_1" to R.raw.word_w_peh_1,
            "w_פ_2" to R.raw.word_w_peh_2,
            "w_פ_3" to R.raw.word_w_peh_3,
            "w_פ_4" to R.raw.word_w_peh_4,
            "w_צ_1" to R.raw.word_w_tsadi_1,
            "w_צ_2" to R.raw.word_w_tsadi_2,
            "w_צ_3" to R.raw.word_w_tsadi_3,
            "w_צ_4" to R.raw.word_w_tsadi_4,
            "w_ק_1" to R.raw.word_w_kuf_1,
            "w_ק_2" to R.raw.word_w_kuf_2,
            "w_ק_3" to R.raw.word_w_kuf_3,
            "w_ר_1" to R.raw.word_w_reish_1,
            "w_ר_2" to R.raw.word_w_reish_2,
            "w_ר_3" to R.raw.word_w_reish_3,
            "w_ר_4" to R.raw.word_w_reish_4,
            "w_ר_5" to R.raw.word_w_reish_5,
            "w_ש_1" to R.raw.word_w_shin_1,
            "w_ש_2" to R.raw.word_w_shin_2,
            "w_ש_3" to R.raw.word_w_shin_3,
            "w_ש_4" to R.raw.word_w_shin_4,
            "w_ת_1" to R.raw.word_w_taf_1,
            "w_ת_2" to R.raw.word_w_taf_2,
            "w_ת_3" to R.raw.word_w_taf_3,
            "w_ת_4" to R.raw.word_w_taf_4,
        )

    fun wordRawResIdByCatalogId(catalogId: String): Int? = WordRawResByCatalogId[catalogId]

    fun imageToWordClipByCatalogId(
        catalogEntryId: String,
        chapterId: Int,
        voiceHasAsset: (String) -> Boolean,
    ): String {
        if (chapterId == 3 || chapterId == 6) {
            val ch3Clip = "audio/ch3_word_${catalogEntryId}.wav"
            if (voiceHasAsset(ch3Clip)) return ch3Clip
        }
        return wordClipByCatalogId(catalogEntryId)
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
            "ז" -> "audio/choose_zayin.wav"
            "ס" -> "audio/choose_samech.wav"
            "ע" -> "audio/choose_ayin.wav"
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
            "ז" -> "audio/letter_zayin.wav"
            "ס" -> "audio/letter_samech.wav"
            "ע" -> "audio/letter_ayin.wav"
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

    fun letterNameRawResId(letter: String): Int? =
        when (letter) {
            "א" -> R.raw.letter_name_alef
            "ב" -> R.raw.letter_name_bet
            "ג" -> R.raw.letter_name_gimel
            "ד" -> R.raw.letter_name_dalet
            "ה" -> R.raw.letter_name_heh
            "ו" -> R.raw.letter_name_vav
            "ז" -> R.raw.letter_name_zayin
            "ח" -> R.raw.letter_name_chet
            "ט" -> R.raw.letter_name_tet
            "י" -> R.raw.letter_name_yod
            "כ" -> R.raw.letter_name_kaf
            "ל" -> R.raw.letter_name_lamed
            "מ" -> R.raw.letter_name_mem
            "נ" -> R.raw.letter_name_nun
            "ס" -> R.raw.letter_name_samech
            "ע" -> R.raw.letter_name_ayin
            "פ" -> R.raw.letter_name_peh
            "צ" -> R.raw.letter_name_tsadi
            "ק" -> R.raw.letter_name_kuf
            "ר" -> R.raw.letter_name_reish
            "ש" -> R.raw.letter_name_shin
            "ת" -> R.raw.letter_name_tav
            else -> null
        }

    fun reportMissingLetterNameMapping(
        tappedLetter: String,
        chapterId: Int? = null,
        stationId: Int? = null,
        context: String,
    ) {
        Log.e(
            TAG,
            "Missing required letter-name mapping. tappedLetter='$tappedLetter' chapterId=$chapterId stationId=$stationId context=$context",
        )
    }

    fun reportMissingLetterNameAsset(
        tappedLetter: String,
        mappedAssetPath: String?,
        chapterId: Int? = null,
        stationId: Int? = null,
        context: String,
        detail: String? = null,
    ) {
        Log.e(
            TAG,
            "Missing/unloadable required letter-name asset. tappedLetter='$tappedLetter' mappedAssetPath=$mappedAssetPath chapterId=$chapterId stationId=$stationId context=$context detail=$detail",
        )
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
    private val Station1CorrectPraiseTailCandidates =
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

    fun station1CorrectPraiseTailCandidates(): Array<String> = Station1CorrectPraiseTailCandidates

    /**
     * After [VoLevelDone] on the "שלב הסתיים" reward screen — caller shuffles, then [VoicePlayer.playFirstAvailableBlocking].
     * Wider than "יפה" alone so the tail feels varied.
     */
    private val RewardStagePraiseTailCandidates =
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

    fun rewardStagePraiseTailCandidates(): Array<String> = RewardStagePraiseTailCandidates
}

