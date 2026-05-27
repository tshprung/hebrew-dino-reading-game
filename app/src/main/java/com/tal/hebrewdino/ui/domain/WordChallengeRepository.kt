package com.tal.hebrewdino.ui.domain

object WordChallengeRepository {
    const val LETTER_STATION_ROUNDS: Int = 8

    fun letterRecognitionForChapter(letters: List<String>): List<WordChallenge> =
        buildLetterChallenges(
            letters = letters,
            idPrefix = "ltr",
            challengeType = ChallengeType.LETTER_RECOGNITION,
            category = "זיהוי אות",
        )

    fun phonemicIsolationForChapter(letters: List<String>): List<WordChallenge> =
        buildLetterChallenges(
            letters = letters,
            idPrefix = "ph",
            challengeType = ChallengeType.PHONEMIC_ISOLATION,
            category = "בידוד צליל",
        )

    private fun buildLetterChallenges(
        letters: List<String>,
        idPrefix: String,
        challengeType: ChallengeType,
        category: String,
    ): List<WordChallenge> {
        val pool = letters.distinct().filter { it.isNotBlank() }
        if (pool.isEmpty()) return emptyList()
        val roundLetters =
            buildList {
                var i = 0
                while (size < LETTER_STATION_ROUNDS) {
                    add(pool[i % pool.size])
                    i += 1
                }
            }.shuffled()
        return roundLetters.mapIndexed { idx, letter ->
            WordChallenge(
                id = "${idPrefix}_ch_${pool.hashCode()}_$idx",
                questionText = letter,
                options = pool,
                correctOption = letter,
                challengeType = challengeType,
                category = category,
            )
        }
    }

    val letterRecognitionHebrewChapter1: List<WordChallenge> =
        letterRecognitionForChapter(HebrewSyllabus.chapters[0].letters)

    val phonemicIsolationHebrewChapter1Station2: List<WordChallenge> =
        phonemicIsolationForChapter(HebrewSyllabus.chapters[0].letters)

    val oddOneOutHebrew: List<WordChallenge> =
        listOf(
            WordChallenge(
                id = "odd_001",
                questionText = "איזו מילה יוצאת דופן? שלוש מילים מתחילות ב\"ח\".",
                options = listOf("חמור", "חזיר", "חלב", "מספריים"),
                correctOption = "מספריים",
                challengeType = ChallengeType.ODD_ONE_OUT,
                category = "צליל פתיחה",
            ),
            WordChallenge(
                id = "odd_002",
                questionText = "איזו מילה יוצאת דופן? שלוש מילים מתחילות ב\"כ\".",
                options = listOf("כדור", "כיסא", "כפית", "תינוק"),
                correctOption = "תינוק",
                challengeType = ChallengeType.ODD_ONE_OUT,
                category = "צליל פתיחה",
            ),
            WordChallenge(
                id = "odd_003",
                questionText = "איזו מילה יוצאת דופן? שלוש מילים מתחילות ב\"ב\".",
                options = listOf("בלון", "בובה", "בית", "דג"),
                correctOption = "דג",
                challengeType = ChallengeType.ODD_ONE_OUT,
                category = "צליל פתיחה",
            ),
            WordChallenge(
                id = "odd_004",
                questionText = "איזו מילה יוצאת דופן? שלוש מילים מתחילות ב\"ד\".",
                options = listOf("דג", "דלת", "דבש", "חתול"),
                correctOption = "חתול",
                challengeType = ChallengeType.ODD_ONE_OUT,
                category = "צליל פתיחה",
            ),
            WordChallenge(
                id = "odd_005",
                questionText = "איזו מילה יוצאת דופן? שלוש מילים מתחילות ב\"מ\".",
                options = listOf("מכונית", "מיטה", "מלפפון", "סוס"),
                correctOption = "סוס",
                challengeType = ChallengeType.ODD_ONE_OUT,
                category = "צליל פתיחה",
            ),
            WordChallenge(
                id = "odd_006",
                questionText = "איזו מילה יוצאת דופן? שלוש מילים מתחילות ב\"ס\".",
                options = listOf("סוכר", "סנדל", "סוס", "כדור"),
                correctOption = "כדור",
                challengeType = ChallengeType.ODD_ONE_OUT,
                category = "צליל פתיחה",
            ),
            WordChallenge(
                id = "odd_007",
                questionText = "איזו מילה יוצאת דופן? שלוש מילים מתחילות ב\"ש\".",
                options = listOf("שמש", "שוקו", "שולחן", "אבטיח"),
                correctOption = "אבטיח",
                challengeType = ChallengeType.ODD_ONE_OUT,
                category = "צליל פתיחה",
            ),
            WordChallenge(
                id = "odd_008",
                questionText = "איזו מילה יוצאת דופן? שלוש מילים מתחילות ב\"ר\".",
                options = listOf("רגל", "רכבת", "רימון", "בקבוק"),
                correctOption = "בקבוק",
                challengeType = ChallengeType.ODD_ONE_OUT,
                category = "צליל פתיחה",
            ),
            WordChallenge(
                id = "odd_009",
                questionText = "איזו מילה יוצאת דופן? שלוש מילים מתחילות ב\"א\".",
                options = listOf("אוטו", "אבא", "אוכל", "מדורה"),
                correctOption = "מדורה",
                challengeType = ChallengeType.ODD_ONE_OUT,
                category = "צליל פתיחה",
            ),
            WordChallenge(
                id = "odd_010",
                questionText = "איזו מילה יוצאת דופן? שלוש מילים מתחילות ב\"ל\".",
                options = listOf("לב", "לימון", "לחם", "מטרייה"),
                correctOption = "מטרייה",
                challengeType = ChallengeType.ODD_ONE_OUT,
                category = "צליל פתיחה",
            ),
            WordChallenge(
                id = "odd_011",
                questionText = "איזו מילה יוצאת דופן? שלוש מילים מתחילות ב\"פ\".",
                options = listOf("פרפר", "פיל", "פיצה", "גדר"),
                correctOption = "גדר",
                challengeType = ChallengeType.ODD_ONE_OUT,
                category = "צליל פתיחה",
            ),
            WordChallenge(
                id = "odd_012",
                questionText = "איזו מילה יוצאת דופן? שלוש מילים מתחילות ב\"נ\".",
                options = listOf("נמלה", "נעל", "נר", "גלידה"),
                correctOption = "גלידה",
                challengeType = ChallengeType.ODD_ONE_OUT,
                category = "צליל פתיחה",
            ),
            WordChallenge(
                id = "odd_013",
                questionText = "איזו מילה יוצאת דופן? שלוש מילים מתחילות ב\"ת\".",
                options = listOf("תוף", "תפוח", "תינוק", "כורסה"),
                correctOption = "כורסה",
                challengeType = ChallengeType.ODD_ONE_OUT,
                category = "צליל פתיחה",
            ),
            WordChallenge(
                id = "odd_014",
                questionText = "איזו מילה יוצאת דופן? שלוש מילים מתחילות ב\"ק\".",
                options = listOf("קוף", "קיץ", "קופסה", "ממתק"),
                correctOption = "ממתק",
                challengeType = ChallengeType.ODD_ONE_OUT,
                category = "צליל פתיחה",
            ),
            WordChallenge(
                id = "odd_015",
                questionText = "איזו מילה יוצאת דופן? שלוש מילים מתחילות ב\"ג\".",
                options = listOf("גזר", "גדר", "גרב", "חלון"),
                correctOption = "חלון",
                challengeType = ChallengeType.ODD_ONE_OUT,
                category = "צליל פתיחה",
            ),
        )

    val rhymesHebrew: List<WordChallenge> =
        listOf(
            WordChallenge(
                id = "rhyme_001",
                questionText = "בלון",
                options = listOf("חלון", "חתול", "כיסא", "סוס"),
                correctOption = "חלון",
                challengeType = ChallengeType.RHYME,
                category = "חריזה",
            ),
            WordChallenge(
                id = "rhyme_002",
                questionText = "דבש",
                options = listOf("יבש", "כובע", "רכבת", "לב"),
                correctOption = "יבש",
                challengeType = ChallengeType.RHYME,
                category = "חריזה",
            ),
            WordChallenge(
                id = "rhyme_003",
                questionText = "בית",
                options = listOf("זית", "אוטו", "דלת", "ספר"),
                correctOption = "זית",
                challengeType = ChallengeType.RHYME,
                category = "חריזה",
            ),
            WordChallenge(
                id = "rhyme_004",
                questionText = "חתול",
                options = listOf("כחול", "בלון", "כיסא", "דג"),
                correctOption = "כחול",
                challengeType = ChallengeType.RHYME,
                category = "חריזה",
            ),
            WordChallenge(
                id = "rhyme_005",
                questionText = "ציפור",
                options = listOf("סיפור", "חתול", "דבש", "חלון"),
                correctOption = "סיפור",
                challengeType = ChallengeType.RHYME,
                category = "חריזה",
            ),
            WordChallenge(
                id = "rhyme_006",
                questionText = "דג",
                options = listOf("חג", "בובה", "גזר", "כובע"),
                correctOption = "חג",
                challengeType = ChallengeType.RHYME,
                category = "חריזה",
            ),
            WordChallenge(
                id = "rhyme_007",
                questionText = "דוב",
                options = listOf("טוב", "חלון", "כדור", "תפוח"),
                correctOption = "טוב",
                challengeType = ChallengeType.RHYME,
                category = "חריזה",
            ),
            WordChallenge(
                id = "rhyme_008",
                questionText = "תוף",
                options = listOf("סוף", "תפוח", "נעל", "רכבת"),
                correctOption = "סוף",
                challengeType = ChallengeType.RHYME,
                category = "חריזה",
            ),
            WordChallenge(
                id = "rhyme_009",
                questionText = "גדר",
                options = listOf("חדר", "כוס", "בובה", "תפוח"),
                correctOption = "חדר",
                challengeType = ChallengeType.RHYME,
                category = "חריזה",
            ),
            WordChallenge(
                id = "rhyme_010",
                questionText = "שמש",
                options = listOf("חמש", "כדור", "בלון", "כיסא"),
                correctOption = "חמש",
                challengeType = ChallengeType.RHYME,
                category = "חריזה",
            ),
            WordChallenge(
                id = "rhyme_011",
                questionText = "רימון",
                options = listOf("לימון", "תפוח", "כוס", "מיטה"),
                correctOption = "לימון",
                challengeType = ChallengeType.RHYME,
                category = "חריזה",
            ),
            WordChallenge(
                id = "rhyme_012",
                questionText = "מיטה",
                options = listOf("חיטה", "חלון", "כדור", "כיסא"),
                correctOption = "חיטה",
                challengeType = ChallengeType.RHYME,
                category = "חריזה",
            ),
            WordChallenge(
                id = "rhyme_013",
                questionText = "כדור",
                options = listOf("סידור", "נר", "דג", "חלון"),
                correctOption = "סידור",
                challengeType = ChallengeType.RHYME,
                category = "חריזה",
            ),
            WordChallenge(
                id = "rhyme_014",
                questionText = "עכבר",
                options = listOf("מדבר", "בלון", "חלב", "ספר"),
                correctOption = "מדבר",
                challengeType = ChallengeType.RHYME,
                category = "חריזה",
            ),
            WordChallenge(
                id = "rhyme_015",
                questionText = "חלון",
                options = listOf("בלון", "כובע", "תפוח", "מיטה"),
                correctOption = "בלון",
                challengeType = ChallengeType.RHYME,
                category = "חריזה",
            ),
        )
}
