package com.tal.hebrewdino.ui.components.learning

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.tal.hebrewdino.ui.domain.Chapter1StationOrder

/**
 * Station 4/5/6 word cards: caption size is derived from card width; long words need an extra tighten
 * so Hebrew captions stay on one line inside narrow tiles.
 */
private data class CaptionOverrideKey(
    val chapterId: Int?,
    val stationId: Int?,
    val word: String,
)

private val CaptionOverridesByChapterStationWord: Map<CaptionOverrideKey, Float> =
    mapOf(
        CaptionOverrideKey(chapterId = 3, stationId = 1, word = "הפתעה") to (0.616f * 0.80f),
        CaptionOverrideKey(chapterId = 3, stationId = 1, word = "דבש") to 0.80f,
        CaptionOverrideKey(chapterId = 3, stationId = 1, word = "שמש") to (0.80f * 0.90f),
        CaptionOverrideKey(chapterId = 3, stationId = 1, word = "מיטה") to (0.90f * 0.80f),
        CaptionOverrideKey(chapterId = 3, stationId = 1, word = "אבטיח") to (0.72f * 0.80f),
        CaptionOverrideKey(chapterId = 3, stationId = 1, word = "מכונית") to (0.81f * 0.70f),
        CaptionOverrideKey(chapterId = 6, stationId = 1, word = "חתול") to 0.70f,
        CaptionOverrideKey(chapterId = 6, stationId = 1, word = "נמלה") to (0.90f * 0.70f),
        CaptionOverrideKey(chapterId = 6, stationId = 1, word = "צפרדע") to (0.665f * 0.85f),
        CaptionOverrideKey(chapterId = 6, stationId = 1, word = "פרח") to 0.85f,
        CaptionOverrideKey(chapterId = 6, stationId = 1, word = "כדור") to 0.85f,
        CaptionOverrideKey(chapterId = 6, stationId = 1, word = "טלפון") to (0.86f * 0.80f),
        CaptionOverrideKey(chapterId = 6, stationId = 1, word = "דחליל") to (0.76f * 0.80f),
        CaptionOverrideKey(chapterId = 6, stationId = 1, word = "קוביה") to 0.80f,
        CaptionOverrideKey(chapterId = 6, stationId = 1, word = "חולצה") to (0.72f * 0.80f),
        CaptionOverrideKey(chapterId = 6, stationId = 1, word = "ברווז") to (0.90f * 0.80f),
        CaptionOverrideKey(chapterId = 6, stationId = 1, word = "פרפר") to (0.90f * 0.70f),
        CaptionOverrideKey(chapterId = 6, stationId = 1, word = "תפוח") to (0.90f * 0.70f),
        CaptionOverrideKey(chapterId = 6, stationId = 1, word = "דבש") to 0.70f,
        CaptionOverrideKey(chapterId = 6, stationId = 1, word = "טיגריס") to (0.72f * 0.70f),
        CaptionOverrideKey(chapterId = 2, stationId = Chapter1StationOrder.PICTURE_PICK_ALL, word = "שעון") to 0.85f,
        CaptionOverrideKey(chapterId = 4, stationId = null, word = "פילפל") to (0.855f * 0.90f),
        CaptionOverrideKey(chapterId = 1, stationId = null, word = "מכונית") to (0.81f * 0.90f),
    )

private val CaptionOverridesByWord: Map<String, Float> =
    mapOf(
        "חלון" to 0.80f,
        "תיק" to 0.80f,
        "קוף" to 0.80f,
        "תוף" to 0.85f,
        "קטר" to 0.85f,
        "היפופוטם" to 0.528f,
        "הפתעה" to 0.616f,
        "גלידה" to 0.86f,
        "שולחן" to 0.82f,
        "חולצה" to 0.72f,
        "פרפר" to 0.90f,
        "טיגריס" to 0.72f,
        "צלחת" to 0.81f,
        "נמלה" to 0.90f,
        "טוסט" to 0.95f,
        "דחליל" to 0.76f,
        "צפרדע" to 0.665f,
        "פילפל" to 0.855f,
        "תפוח" to 0.90f,
        "בלון" to 0.90f,
        "מיטה" to 0.90f,
        "מחבת" to 0.81f,
        "למידה" to 0.81f,
        "מכונית" to 0.81f,
        "אבטיח" to 0.72f,
        "ארנב" to 0.80f,
        "מוצץ" to 0.80f,
        "ברווז" to 0.90f,
    )

private fun captionTightOverride(
    chapterId: Int?,
    stationId: Int?,
    word: String,
): Float? {
    val exact = CaptionOverridesByChapterStationWord[CaptionOverrideKey(chapterId, stationId, word)]
    if (exact != null) return exact
    val chapterWildcardStation =
        CaptionOverridesByChapterStationWord[CaptionOverrideKey(chapterId, stationId = null, word = word)]
    if (chapterWildcardStation != null) return chapterWildcardStation
    return CaptionOverridesByWord[word]
}

fun captionFontSizeForWordCard(
    density: Density,
    cardWidth: Dp,
    word: String,
    sizeMultiplier: Float,
    /** When set, allows chapter/station-specific caption tweaks (e.g. Ch2 station 5). */
    chapterId: Int? = null,
    stationId: Int? = null,
): TextUnit =
    with(density) {
        val codePoints = word.codePointCount(0, word.length)
        val wordTight = captionTightOverride(chapterId, stationId, word)
            ?: when {
                codePoints >= 9 -> 0.82f
                codePoints >= 7 -> 0.88f
                codePoints >= 6 -> 0.80f
                codePoints >= 5 -> 0.86f
                else -> 1f
            }
        val m = sizeMultiplier * wordTight
        (cardWidth.toPx() * 0.22f * m).coerceIn(
            22f * fontScale * m,
            40f * fontScale * m,
        ).sp
    }
