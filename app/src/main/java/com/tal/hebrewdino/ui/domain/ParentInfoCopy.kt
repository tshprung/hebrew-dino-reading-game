package com.tal.hebrewdino.ui.domain

/** Parent-facing copy for the home-screen information dialog. */
object ParentInfoCopy {
    const val DialogTitle: String = "כדאי לדעת"

    /**
     * Parents area supports reset and companion/address choices only — not stage skip/unlock.
     */
    fun dialogBodyLines(): List<String> =
        listOf(
            rtl("יש אזור הורים."),
            rtl("באזור ההורים אפשר לנהל את המשחק:"),
            rtl("לאפס התקדמות (כולל עונה 2), לבחור בין דינו ודינה, ולבחור איך לפנות לילד או לילדה."),
            rtl("אפשר להיכנס לאזור ההורים מהמסך הראשי."),
        )

    const val ContinueLabel: String = "הבנתי"

    const val InfoButtonLabel: String = "מידע"

    private fun rtl(text: String): String = "\u200F$text"
}

object ParentInfoPolicy {
    fun shouldAutoShow(lastSeenVersionCode: Int?, currentVersionCode: Int): Boolean =
        lastSeenVersionCode == null || lastSeenVersionCode < currentVersionCode
}
