package com.tal.hebrewdino.ui.domain

import com.tal.hebrewdino.ui.data.PlayerAddress
import org.junit.Assert.assertEquals
import org.junit.Test

class TrainingInstructionCopyTest {
    @Test
    fun pickLetter_boyUsesMasculineImperative() {
        assertEquals("בחר את האות", TrainingInstructionCopy.pickLetter(PlayerAddress.Boy))
    }

    @Test
    fun pickLetter_girlUsesFeminineImperative() {
        assertEquals("בחרי את האות", TrainingInstructionCopy.pickLetter(PlayerAddress.Girl))
    }

    @Test
    fun pictureStartsWith_boyUsesMasculineImperative() {
        assertEquals(
            "בחר את התמונה שמתחילה באות",
            TrainingInstructionCopy.pictureStartsWith(PlayerAddress.Boy),
        )
    }

    @Test
    fun pictureStartsWith_girlUsesFeminineImperative() {
        assertEquals(
            "בחרי את התמונה שמתחילה באות",
            TrainingInstructionCopy.pictureStartsWith(PlayerAddress.Girl),
        )
    }
}
