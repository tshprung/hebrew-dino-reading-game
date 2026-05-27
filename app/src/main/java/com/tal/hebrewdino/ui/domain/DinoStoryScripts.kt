package com.tal.hebrewdino.ui.domain

/**
 * Short-lined narrative scripts for TTS (natural pauses via sentence breaks and ellipses).
 * Display copy may use plain "דינו"; spoken copy uses [dinoNameSpoken] for engine-friendly syllables.
 */
object DinoStoryScripts {
    /** Hyphenated syllable break — reads as "di-no" on Android Hebrew TTS. */
    fun dinoNameSpoken(): String = "דִּי-נוֹ"

    fun part1IntroSpokenForTts(): String =
        listOf(
            "תראו… ביצה גדולה!",
            "הביצה זזה קצת…",
            "אולי יש בפנים ${dinoNameSpoken()} קטן?",
            "בואו נשחק ונראה מה יקרה.",
        ).joinToString(" ")

    fun part1IntroDisplayLines(): List<String> =
        listOf(
            "תראו… ביצה גדולה!",
            "הביצה זזה קצת…",
            "אולי יש בפנים דינו קטן?",
            "בואו נשחק ונראה מה יקרה.",
        )

    fun part2BabyHatchSpokenForTts(): String =
        listOf(
            "היי! אני ${dinoNameSpoken()}!",
            "אני קצת רעב…",
            "בואו נשחק ונאסוף תפוחים.",
            "לחצו על התפוחים כדי להאכיל אותי.",
        ).joinToString(" ")

    fun part3FirstAccessorySpokenForTts(): String =
        listOf(
            "וואו! תראו את ${dinoNameSpoken()}!",
            "פתאום הופיע לו כובע מצחיק.",
            "ככל שתמשיכו לשחק, ${dinoNameSpoken()} יקבל עוד הפתעות.",
        ).joinToString(" ")
}
