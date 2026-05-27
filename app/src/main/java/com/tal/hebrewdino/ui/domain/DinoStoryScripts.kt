package com.tal.hebrewdino.ui.domain

/**
 * Short-lined narrative scripts for TTS (natural pauses via sentence breaks and ellipses).
 * Display copy may use plain "דינו"; spoken copy uses [dinoNameSpoken] for engine-friendly syllables.
 */
object DinoStoryScripts {
    /** Hyphenated syllable break — reads as "di-no" on Android Hebrew TTS. */
    fun dinoNameSpoken(): String = "דִּי-נוֹ"

    /** Vowelized so TTS reads "bo ne-sa-chek" instead of "bo nish-chak". */
    fun playTogetherSpokenForTts(): String = "בּוֹאוּ נְשַׂחֵק"

    fun part1IntroSpokenForTts(): String =
        listOf(
            "תראו… ביצה גדולה!",
            "הביצה זזה קצת…",
            "אולי יש בפנים ${dinoNameSpoken()} קטן?",
            "${playTogetherSpokenForTts()} וְנִרְאֶה מָה יִקְרֶה.",
        ).joinToString(" ")

    fun part1IntroDisplayLines(): List<String> =
        listOf(
            "תראו… ביצה גדולה!",
            "הביצה זזה קצת…",
            "אולי יש בפנים דינו קטן?",
            "בואו נשחק ונראה מה יקרה.",
        )

    fun eggKnockPromptSpokenForTts(): String =
        "היי, משהו זוז בפנים! בואו נקיש על הביצה! טוק, טוק, טוק!"

    fun postHatchIntroSpokenForTts(): String =
        "הנה הוא! הוא קצת רעב. בואו נעשה משימות ונאסוף לו אוכל כדי שיגדל!"

    fun part2BabyHatchSpokenForTts(): String = postHatchIntroSpokenForTts()

    fun part3FirstAccessorySpokenForTts(): String =
        listOf(
            "וואו! תראו את ${dinoNameSpoken()}!",
            "פתאום הופיע לו כובע מצחיק.",
            "ככל שתמשיכו לשחק, ${dinoNameSpoken()} יקבל עוד הפתעות.",
        ).joinToString(" ")
}
