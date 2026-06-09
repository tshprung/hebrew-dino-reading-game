package com.tal.hebrewdino.ui.companion

import android.util.Log
import com.tal.hebrewdino.ui.data.DinoCharacter
import com.tal.hebrewdino.ui.domain.Season2StationAudio
import com.tal.hebrewdino.ui.domain.TrainingV1Config

/** Selected Dino/Dina companion visuals — no legacy dino_* sprite fallbacks. */
object CompanionVisualPolicy {
    fun expectsSelectedCompanion(chapterId: Int): Boolean =
        chapterId in listOf(1, 2, 3, 4, 5, 6, TrainingV1Config.CHAPTER_ID) ||
            Season2StationAudio.isSeason2GameplayChapter(chapterId)

    fun reportMissingSelectedCompanion(
        context: String,
        detail: String,
        devToolsEnabled: Boolean,
        chapterId: Int? = null,
        stationId: Int? = null,
    ) {
        try {
            Log.e(
                "MissingContent",
                "Missing selected companion visual. context=$context chapterId=$chapterId stationId=$stationId detail=$detail",
            )
        } catch (_: RuntimeException) {
            // JVM unit tests: android.util.Log is not mocked.
        }
        if (devToolsEnabled) {
            throw IllegalStateException(
                "Missing selected companion visual. context=$context chapterId=$chapterId stationId=$stationId detail=$detail",
            )
        }
    }

    fun requireSelectedCompanion(
        character: DinoCharacter?,
        context: String,
        devToolsEnabled: Boolean,
        chapterId: Int? = null,
        stationId: Int? = null,
    ): DinoCharacter {
        if (character == null) {
            reportMissingSelectedCompanion(
                context = context,
                detail = "companionCharacter=null",
                devToolsEnabled = devToolsEnabled,
                chapterId = chapterId,
                stationId = stationId,
            )
            error("Missing selected companion for $context")
        }
        return character
    }

    fun assetsFor(character: DinoCharacter): CompanionAssets = CompanionAssets.forCharacter(character)
}
