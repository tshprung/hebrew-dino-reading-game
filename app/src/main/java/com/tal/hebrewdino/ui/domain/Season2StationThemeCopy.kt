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
            Season2StationTheme.Standard -> null
        }

    fun pictureToWordInstruction(theme: Season2StationTheme): String =
        when (theme) {
            Season2StationTheme.HighLeaves -> "\u200Fבחרו את המילה שמתאימה לתמונה"
            else -> "\u200Fבחרו את המילה שמתאימה לתמונה"
        }

    fun missingFirstLetterInstruction(theme: Season2StationTheme): String =
        when (theme) {
            Season2StationTheme.LetterArmor -> "\u200Fאיזו אות חסרה?"
            else -> "\u200Fאיזו אות חסרה?"
        }

    fun wordPartsInstruction(
        theme: Season2StationTheme,
        presentationMode: Season2WordPartsPresentationMode = Season2WordPartsPresentationMode.GuidedWordParts,
    ): String =
        when (presentationMode) {
            Season2WordPartsPresentationMode.VisibleWordParts -> "\u200Fמצאו את חלקי המילה"
            Season2WordPartsPresentationMode.GuidedWordParts -> "\u200Fאיזה חלק משלים את המילה?"
            Season2WordPartsPresentationMode.HiddenWordPartsChallenge -> "\u200Fאיזה חלק משלים את המילה?"
        }

    fun rhymingInstruction(theme: Season2StationTheme): String =
        when (theme) {
            Season2StationTheme.UnderwaterBubbles -> "\u200Fאיזו מילה מתחרזת?"
            else -> "\u200Fאיזו מילה מתחרזת?"
        }
}
