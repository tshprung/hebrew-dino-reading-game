package com.tal.hebrewdino.ui.domain

/**
 * Canonical Hebrew copy for station headers/instructions. Wired from [StationBehaviorRegistry] into
 * [StationUiSpec]; leaf composables should not hardcode these strings.
 */
object StationInstructionCopy {
    const val PickLetterHighlightedInWord = "מצא את האות המודגשת במילה:"
    const val PickLetterListenOnly = "מצא את האות שנאמרת"
    const val PickLetterRepeatLetterButton = "חזור על האות"
    const val PickLetterSagaStation1Preamble = "בחר את האות:"
    const val PickLetterEpisode4AndCh5 = "בחר את האות:"

    const val PopBalloonsPopAllLettersInWord = "פוצץ את כל הבלונים עם אותיות המופיעות במילה:"
    const val PopBalloonsWithLetter = "פוצץ את הבלונים עם האות:"
    const val PopBalloonsListenFirst = "פוצץ את הבלונים של האות שנשמעה:"

    const val FindGridVisibleTarget = "מצא את האות:"
    const val FindGridListenFirst = "מצאו את האות שנשמעת:"

    const val PictureStartsWithListenFirstSaga = "באיזו אות המילה מתחילה?"
    const val PictureStartsWithDefault = "באיזו אות מתחילה המילה?"
    const val PictureStartsWithEpisode4 = "באיזו אות מתחילה המילה:"

    const val ImageMatchFindWordStartingWithLetter = "מצא את המילה המתחילה באות:"
    const val ImageMatchListenFirst = "מצאו את המילה לפי האות שנשמעה:"

    const val MatchLetterFinale = "ליחצו על אות והמילה שמתחילה באותה האות"

    const val Chapter3ImageToWord = "איזו מילה מתאימה לתמונה של:"
}
