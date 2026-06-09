package com.tal.hebrewdino.ui.domain

/**
 * Season 2 advanced learning-station interaction patterns (Chapters 3–6).
 * Wired via [StationQuizPlan.season2AdvancedMode]; Season 1 and S2 Ch1–2 never set this flag.
 */
enum class Season2AdvancedStationMode {
    /** Picture prompt + tap the matching written word (reuses [Question.ImageMatchQuestion] + ImageToWord UI). */
    PictureToWord,
    /** Picture + partial word with missing first letter; pick the first letter. */
    MissingFirstLetter,
    /** Connect beginning letter/sound + rest of word. */
    WordParts,
    /** Phonological awareness — pick the picture/word that rhymes with the target. */
    Rhyming,
}
