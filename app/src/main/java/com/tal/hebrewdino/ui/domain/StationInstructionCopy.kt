package com.tal.hebrewdino.ui.domain

/**
 * Canonical Hebrew copy for station headers/instructions. Wired from [StationBehaviorRegistry] into
 * [StationUiSpec]; leaf composables should not hardcode these strings.
 */
object StationInstructionCopy {
    const val TrainingHearLetterChoose = "בחר את האות"
    const val TrainingWhichWordStartsWithLetter = "איזו מילה מתחילה באות?"
    const val TrainingChooseWordForPicture = "בחר את המילה שמתאימה לתמונה"
    const val TrainingFindLetterInGrid = "מצאו את האות שנשמעת:"

    const val PickLetterHighlightedInWord = "מיצאו את האות המודגשת במילה:"
    const val PickLetterSagaStation1Preamble = "בחר את האות:"

    const val PopBalloonsPopAllLettersInWord = "פוצץ את כל הבלונים עם אותיות שמופיעות במילה:"
    const val PopBalloonsWithLetter = "פוצץ את הבלונים עם האות:"
    const val PopBalloonsListenFirst = "פוצץ את הבלונים של האות שנשמעה:"

    const val FindGridVisibleTarget = "מצא את האות:"
    const val FindGridListenFirst = "מצאו את האות שנשמעת:"

    const val PictureStartsWithListenFirstSaga = "באיזו אות המילה מתחילה?"
    const val PictureStartsWithDefault = "באיזו אות מתחילה המילה?"

    const val ImageMatchFindWordStartingWithLetter = "בחר את התמונה שמתחילה באות:"
    const val ImageMatchListenFirst = "בחר את התמונה שמתחילה באות:"

    const val MatchLetterFinale = "ליחצו על אות והמילה שמתחילה באותה האות"

    const val Chapter3ImageToWord = "איזו מילה מתאימה לתמונה של:"
}
