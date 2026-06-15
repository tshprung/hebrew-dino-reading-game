package com.tal.hebrewdino.ui.domain

/** Lightweight copy/theme hooks for Season 2 station skins (no heavy visuals in this batch). */
object Season2StationThemeCopy {
    fun stageLabelSuffix(theme: Season2StationTheme, stationId: Int): String? =
        when (theme) {
            Season2StationTheme.Footprints ->
                when (stationId) {
                    Season2Chapter1StationOrder.PICK_LETTER -> " · עקבות בחול"
                    else -> null
                }
            Season2StationTheme.StegosaurusPlates -> " · לוחות סטגוזאורוס"
            Season2StationTheme.HighLeaves -> " · עלים גבוהים"
            Season2StationTheme.LetterArmor -> " · שריון אותיות"
            Season2StationTheme.UnderwaterBubbles -> " · בועות מתחת למים"
            Season2StationTheme.FlyingSky -> " · שמי פרהיסטוריים"
            Season2StationTheme.Standard -> null
        }

    fun pictureToWordInstruction(theme: Season2StationTheme): String =
        when (theme) {
            Season2StationTheme.HighLeaves -> "\u200Fאיזו מילה מתאימה לתמונה?"
            else -> "\u200Fאיזו מילה מתאימה לתמונה?"
        }

    fun missingFirstLetterInstruction(theme: Season2StationTheme): String =
        when (theme) {
            Season2StationTheme.LetterArmor -> "\u200Fאיזו אות חסרה במילה?"
            else -> "\u200Fאיזו אות חסרה במילה?"
        }

    fun wordPartsInstruction(
        presentationMode: Season2WordPartsPresentationMode = Season2WordPartsPresentationMode.GuidedWordParts,
    ): String =
        when (presentationMode) {
            Season2WordPartsPresentationMode.VisibleWordParts -> "\u200Fמצאו את חלקי המילה"
            Season2WordPartsPresentationMode.GuidedWordParts -> "\u200Fמצאו את חלקי המילה"
            Season2WordPartsPresentationMode.HiddenWordPartsChallenge ->
                "\u200Fמצאו את חלקי המילה ששמעתם"
        }

    fun rhymingInstruction(
        theme: Season2StationTheme,
        targetWord: String? = null,
    ): String {
        if (!targetWord.isNullOrBlank()) {
            return "\u200Fאיזו מילה מתחרזת עם $targetWord?"
        }
        return when (theme) {
            Season2StationTheme.UnderwaterBubbles,
            Season2StationTheme.FlyingSky,
            -> "\u200Fאיזו מילה מתחרזת עם?"
            else -> "\u200Fאיזו מילה מתחרזת עם?"
        }
    }
}
