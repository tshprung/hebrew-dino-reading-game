package com.tal.hebrewdino.ui.layout

import com.tal.hebrewdino.test.ProjectSource
import org.junit.Assert.assertTrue
import org.junit.Test

class EdgeToEdgeSetupTest {
    @Test
    fun immersiveFullscreen_callsEnableEdgeToEdge() {
        val src = ProjectSource.read("app/src/main/java/com/tal/hebrewdino/ui/layout/ImmersiveFullscreen.kt")
        assertTrue(src.contains("enableEdgeToEdge()"))
    }

    @Test
    fun mainActivity_enablesImmersiveFullscreenOnLaunch() {
        val src = ProjectSource.read("app/src/main/java/com/tal/hebrewdino/MainActivity.kt")
        assertTrue(src.contains("enableImmersiveFullscreen()"))
    }

    @Test
    fun manifest_usesAppThemeWithCutoutMode() {
        val themes = ProjectSource.read("app/src/main/res/values/themes.xml")
        assertTrue(themes.contains("windowLayoutInDisplayCutoutMode"))
        assertTrue(themes.contains("shortEdges"))

        val manifest = ProjectSource.read("app/src/main/AndroidManifest.xml")
        assertTrue(manifest.contains("@style/Theme.HebrewDino"))
    }

    @Test
    fun topChromeScreens_useTopChromeInsetsPadding() {
        val screensWithTopChrome =
            listOf(
                "app/src/main/java/com/tal/hebrewdino/ui/screens/OpeningScreen.kt",
                "app/src/main/java/com/tal/hebrewdino/ui/screens/SeasonsScreen.kt",
                "app/src/main/java/com/tal/hebrewdino/ui/screens/ChaptersScreen.kt",
                "app/src/main/java/com/tal/hebrewdino/ui/screens/GameScreenLayout.kt",
            )
        for (path in screensWithTopChrome) {
            val src = ProjectSource.read(path)
            assertTrue("$path should pad top chrome for edge-to-edge", src.contains("topChromeInsetsPadding"))
        }
    }
}
