package com.tal.hebrewdino.ui.companion

import com.tal.hebrewdino.R
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.domain.Season2ChapterIds
import com.tal.hebrewdino.ui.domain.TrainingV1Config
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CompanionVisualPolicyTest {
    @Test
    fun season2GameplayChapters_requireSelectedCompanion() {
        for (chapterId in Season2ChapterIds.Chapter1Tyrannosaurus..Season2ChapterIds.Chapter6Mosasaurus) {
            assertTrue(
                "chapterId=$chapterId",
                CompanionVisualPolicy.expectsSelectedCompanion(chapterId),
            )
        }
    }

    @Test
    fun season1AndTraining_requireSelectedCompanion() {
        for (chapterId in 1..6) {
            assertTrue(CompanionVisualPolicy.expectsSelectedCompanion(chapterId))
        }
        assertTrue(CompanionVisualPolicy.expectsSelectedCompanion(TrainingV1Config.CHAPTER_ID))
    }

    @Test
    fun unrelatedChapterId_doesNotExpectSelectedCompanion() {
        assertFalse(CompanionVisualPolicy.expectsSelectedCompanion(99))
    }

    @Test
    fun companionAssets_useCurrentCompanionDrawables_notLegacyDino() {
        val dino = CompanionVisualPolicy.assetsFor(DinoCharacter.Dino)
        val dina = CompanionVisualPolicy.assetsFor(DinoCharacter.Dina)
        assertEquals(R.drawable.companion_dino_idle, dino.poseIdle)
        assertEquals(R.drawable.companion_dina_idle, dina.poseIdle)
        assertTrue(dino.talkFrameResIds.all { it != 0 })
        assertTrue(dina.talkFrameResIds.all { it != 0 })
    }

    @Test
    fun requireSelectedCompanion_throwsWhenMissingInDebug() {
        var thrown: IllegalStateException? = null
        try {
            CompanionVisualPolicy.requireSelectedCompanion(
                character = null,
                context = "test",
                devToolsEnabled = true,
            )
        } catch (e: IllegalStateException) {
            thrown = e
        }
        assertNotNull(thrown)
        assertTrue(thrown!!.message!!.contains("Missing selected companion visual"))
    }
}
