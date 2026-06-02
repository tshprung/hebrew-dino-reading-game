package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.ui.data.PlayerAddress

/** Address-aware on-screen instructions for Training chapter (Ch.7) only. */
object TrainingInstructionCopy {
    fun pickLetter(address: PlayerAddress): String =
        when (address) {
            PlayerAddress.Boy -> "בחר את האות"
            PlayerAddress.Girl -> "בחרי את האות"
        }

    /** ImageMatch: child picks the picture that starts with the target letter. */
    fun pictureStartsWith(address: PlayerAddress): String =
        when (address) {
            PlayerAddress.Boy -> "בחר את התמונה שמתחילה באות"
            PlayerAddress.Girl -> "בחרי את התמונה שמתחילה באות"
        }
}
