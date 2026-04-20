package com.tal.hebrewdino.ui.domain

/**
 * Simple picture symbols for pre-readers when a word has no custom [LessonWordEntry.tileRes] art.
 * Uses emoji so cards stay meaningful offline without shipping dozens of bitmaps.
 */
object LessonWordIllustrations {
    fun emojiForWord(word: String): String =
        when (word) {
            "אבא" -> "👨"
            "אריה" -> "🦁"
            "אבטיח" -> "🍉"
            "בית" -> "🏠"
            "בלון" -> "🎈"
            "ברווז" -> "🦆"
            "מכונית" -> "🚗"
            "מחבת" -> "🍳"
            "מדוזה" -> "🪼"
            "לחם" -> "🍞"
            "לב" -> "❤️"
            "למידה" -> "📚"
            "דג" -> "🐟"
            "דלת" -> "🚪"
            "דחליל" -> "🧑‍🌾"
            "נמל" -> "🐜"
            "נר" -> "🕯️"
            "נעליים" -> "👟"
            "ראש" -> "🙂"
            "רכב" -> "🚃"
            "רעש" -> "📢"
            "שמש" -> "☀️"
            "שולחן" -> "🪑"
            "שוקולד" -> "🍫"
            "תפוח" -> "🍎"
            "תיק" -> "🎒"
            "תינוק" -> "👶"
            "יום" -> "📅"
            "ילד" -> "🧒"
            "כיסא" -> "🪑"
            "כלב" -> "🐕"
            "כדור" -> "⚽"
            "קוף" -> "🐵"
            "קוביה" -> "🧊"
            "קנדי" -> "🍬"
            "טוסט" -> "🍞"
            "טבע" -> "🌳"
            "טיגר" -> "🐯"
            "הר" -> "⛰️"
            "הפתעה" -> "🎁"
            "הליכון" -> "🏃"
            else -> "✨"
        }
}
