package com.tal.hebrewdino.ui.domain

/** Full letter name for UI / pedagogy (may include niqqud). */
fun hebrewLetterNameForSpeech(letter: String): String? =
    when (letter.trim().firstOrNull()?.toString().orEmpty()) {
        "א" -> "אָלֶף"
        "ב" -> "בֵּית"
        "ג" -> "גִּימֶל"
        "ד" -> "דָּלֶת"
        "ה" -> "הֵא"
        "ו" -> "וָו"
        "ז" -> "זַיִן"
        "ח" -> "חֵית"
        "ט" -> "טֵית"
        "י" -> "יוֹד"
        "כ", "ך" -> "כָּף"
        "ל" -> "לָמֶד"
        "מ", "ם" -> "מֵם"
        "נ", "ן" -> "נוּן"
        "ס" -> "סָמֶךְ"
        "ע" -> "עַיִן"
        "פ", "ף" -> "פֵּא"
        "צ", "ץ" -> "צַדִּי"
        "ק" -> "קוּף"
        "ר" -> "רֵישׁ"
        "ש" -> "שִׁין"
        "ת" -> "תָּו"
        else -> null
    }

/**
 * Letter name phrasing tuned for Android TTS.
 * ב drops medial י (בֵּית). ח uses חֵ only — חֵית/חֵת read as "cheit" on most engines.
 */
fun letterNameSpokenForTts(letterSymbol: String): String {
    val single = letterSymbol.trim().firstOrNull()?.toString().orEmpty()
    return when (single) {
        "ב" -> "בֵּת"
        "ח" -> "חֵ"
        else -> hebrewLetterNameForSpeech(single) ?: single
    }
}

/** Bare Hebrew letter shown in UI (e.g. falling-letters target chip). */
fun letterSymbolForDisplay(letterSymbol: String): String =
    hebrewLetterBase(letterSymbol)

/** Base consonant without niqqud (for matching and display). */
fun hebrewLetterBase(text: String): String {
    for (ch in text.trim()) {
        if (ch in '\u05D0'..'\u05EA') return ch.toString()
    }
    return text.trim().firstOrNull()?.toString().orEmpty()
}

private fun isHebrewNiqqudChar(ch: Char): Boolean =
    ch.code in 0x05B0..0x05BD || ch.code in 0x05BF..0x05C7

/** Random kamatz / patach / hiriq on falling letters; identification uses [hebrewLetterBase]. */
fun letterWithRandomNiqqudForFalling(
    baseLetter: String,
    rng: kotlin.random.Random = kotlin.random.Random.Default,
): String {
    val base = hebrewLetterBase(baseLetter).ifBlank { baseLetter.trim().first().toString() }
    val mark =
        when (rng.nextInt(3)) {
            0 -> '\u05B8'
            1 -> '\u05B7'
            else -> '\u05B4'
        }
    return base + mark
}

/** Dino name for TTS — hyphenated syllables read as "di-no". */
fun dinoNameSpokenForTts(): String = DinoStoryScripts.dinoNameSpoken()

fun introInstructionSpokenForTts(): String = DinoStoryScripts.part1IntroSpokenForTts()

/** Child-friendly alternative when engines misread מִשְׂרָדִים for מִשְׂימוֹת. */
fun activitiesWordSpokenForTts(): String = "פְעִילוּיוֹת"

/** Applies dino-name and common misread word fixes before [TextToSpeechManager] speaks. */
fun applyChildFriendlyTtsWorkarounds(text: String): String {
    val spokenName = dinoNameSpokenForTts()
    var result = text
    result = result.replace("הדינו", "את $spokenName")
    result = result.replace("לדינו", "ל$spokenName")
    result = result.replace("את דינו", "את $spokenName")
    result = result.replace("דינו", spokenName)
    result = result.replace("משימות", activitiesWordSpokenForTts())
    result = result.replace("מִשְׂרָדִים", activitiesWordSpokenForTts())
    result = result.replace("מִשְׂימוֹת", activitiesWordSpokenForTts())
    return result
}

/** TTS for "דינו רעב" — niqqud steers engines away from "raav". */
fun hungryDinoPromptSpokenForTts(): String =
    dinoNameSpokenForTts() + " רָעֵב! תאכילו אותו בתפוחים."

/** Niqqud shown on badges / bubbles (pedagogically correct). */
fun phonemeForDisplay(letterSymbol: String): String = phonemeWithNiqqud(letterSymbol)

private fun phonemeWithNiqqud(letterSymbol: String): String {
    val single = letterSymbol.trim().firstOrNull()?.toString().orEmpty()
    return when (single) {
        "א" -> "אָ"
        "ב" -> "בְּ"
        "ו" -> "וּ"
        "ק" -> "קֻ"
        "ר" -> "רְ"
        "ד" -> "דְ"
        "ה" -> "הַ"
        "נ" -> "נְ"
        "י" -> "יְ"
        "ח" -> "חַ"
        "ת" -> "תַ"
        "צ" -> "צְ"
        "ש" -> "שְׁ"
        "מ" -> "מְ"
        "ל" -> "לְ"
        "ס" -> "סְ"
        "ג" -> "גְּ"
        "ז" -> "זְ"
        "ט" -> "טְ"
        "כ", "ך" -> "כְּ"
        "ע" -> "עֲ"
        "פ", "ף" -> "פְּ"
        else -> ""
    }
}

/**
 * Spoken phoneme for station 2 (בידוד צליל): a short voweled syllable, never the letter name.
 * Derived from [phonemeForDisplay] niqqud; shva displays use tsere/segol-style syllables for TTS.
 */
fun phonemeSpokenForTts(letterSymbol: String): String {
    val trimmed = letterSymbol.trim()
    if (trimmed.isEmpty()) return ""
    val display =
        if (trimmed.any(::isHebrewNiqqudMark)) {
            trimmed
        } else {
            phonemeForDisplay(trimmed)
        }
    return ttsSyllableForDisplayedPhoneme(display).ifBlank {
        trimmed.firstOrNull()?.toString().orEmpty()
    }
}

private fun isHebrewNiqqudMark(ch: Char): Boolean = isHebrewNiqqudChar(ch)

private fun ttsSyllableForDisplayedPhoneme(display: String): String {
    if (display.isBlank()) return ""
    val base = display.first().toString()
    val marks = display.drop(1)
    return when {
        marks.contains('\u05B0') -> ttsSyllableForShva(base)
        marks.contains('\u05B7') -> base + "\u05B7"
        marks.contains('\u05B8') -> base + "\u05B8"
        marks.contains('\u05B4') -> if (base == "ו") "ו" else base + "\u05B4"
        marks.contains('\u05B9') -> base + "\u05B9"
        marks.contains('\u05BB') -> if (base == "ק") "קו" else base + "\u05BB"
        marks.contains('\u05B6') -> base + "\u05B6"
        marks.contains('\u05B2') -> if (base == "ע") "עֵ" else base + "\u05B6"
        else -> ttsSyllableForShva(base)
    }
}

/** Maps shva-display phonemes to a vowelized syllable Android TTS reads as a sound (not a letter name). */
private fun ttsSyllableForShva(base: String): String =
    when (base) {
        "א" -> "אֲ"
        "ב" -> "בֵ"
        "ג" -> "גֵ"
        "ד" -> "דֵ"
        "ה" -> "הֵ"
        "ו" -> "ו"
        "ז" -> "זֵ"
        "ח" -> "חֵ"
        "ט" -> "טֵ"
        "י" -> "יֵ"
        "כ", "ך" -> "כֵ"
        "ל" -> "לֵ"
        "מ" -> "מֵ"
        "נ" -> "נֵ"
        "ס" -> "סֵ"
        "ע" -> "עֵ"
        "פ", "ף" -> "פֵ"
        "צ", "ץ" -> "צֵ"
        "ק" -> "קֵ"
        "ר" -> "רֵ"
        "ש" -> "שֵ"
        "ת" -> "תֵ"
        else -> base + "\u05B6"
    }

@Deprecated("Use phonemeSpokenForTts for speech and phonemeForDisplay for UI", ReplaceWith("phonemeSpokenForTts(letterSymbol)"))
fun phonemeForTts(letterSymbol: String): String = phonemeSpokenForTts(letterSymbol)

fun requireHebrewLetterNameForTts(letterSymbol: String): String {
    val single = letterSymbol.trim().firstOrNull()?.toString().orEmpty()
    return letterNameSpokenForTts(single).ifBlank { "אות" }
}

/** Station 1: letter name. Station 2: spoken phoneme (consonant / short vowel). */
fun targetSuccessSpeech(
    challengeType: ChallengeType,
    targetLetter: String,
): String {
    val letter = targetLetter.trim().firstOrNull()?.toString().orEmpty()
    return when (challengeType) {
        ChallengeType.PHONEMIC_ISOLATION -> phonemeSpokenForTts(letter).ifBlank { letter }
        ChallengeType.LETTER_RECOGNITION -> requireHebrewLetterNameForTts(letter)
        else -> requireHebrewLetterNameForTts(letter)
    }
}

/** Wrong tap on falling letters: letter name only (no "זה"). */
fun wrongLetterFeedbackSpeech(letter: String): String {
    val single = letter.trim().firstOrNull()?.toString().orEmpty()
    return letterNameSpokenForTts(single)
}
