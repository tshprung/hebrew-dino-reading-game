package com.tal.hebrewdino.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class OpeningAndSeasonsUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun disableComposeClockAutoAdvance() {
        composeRule.mainClock.autoAdvance = false
    }

    private fun setTestContent(content: @Composable () -> Unit) {
        composeRule.setContent {
            MaterialTheme {
                content()
            }
        }
    }

    @Test
    fun opening_showsSettingsAndPlay() {
        setTestContent {
            OpeningScreen(
                onPlay = { },
                onOpenSettings = { },
                onExit = { },
                enableMotion = false,
            )
        }

        composeRule.onNodeWithText("הגדרות").assertIsDisplayed()
        composeRule.onNodeWithText("יציאה").assertIsDisplayed()
        composeRule.onNodeWithText("בואו נשחק").assertIsDisplayed()
    }

    @Test
    fun opening_showsCoreControlsByTag() {
        setTestContent {
            OpeningScreen(
                onPlay = { },
                onOpenSettings = { },
                onExit = { },
                enableMotion = false,
            )
        }

        composeRule.onNodeWithTag("opening_settings").assertIsDisplayed()
        composeRule.onNodeWithTag("opening_exit").assertIsDisplayed()
        composeRule.onNodeWithTag("opening_play").assertIsDisplayed()
    }

    @Test
    fun opening_buttonsHaveClickAction() {
        setTestContent {
            OpeningScreen(
                onPlay = { },
                onOpenSettings = { },
                onExit = { },
                enableMotion = false,
            )
        }

        composeRule.onNodeWithTag("opening_play").assertHasClickAction()
        composeRule.onNodeWithTag("opening_settings").assertHasClickAction()
        composeRule.onNodeWithTag("opening_exit").assertHasClickAction()
    }

    @Test
    fun opening_playInvokesCallback() {
        var played = false
        setTestContent {
            OpeningScreen(
                onPlay = { played = true },
                onOpenSettings = { },
                onExit = { },
                enableMotion = false,
            )
        }

        composeRule.onNodeWithText("בואו נשחק").performClick()
        composeRule.runOnIdle {
            assertTrue(played)
        }
    }

    @Test
    fun opening_playText_isExact() {
        setTestContent {
            OpeningScreen(
                onPlay = { },
                onOpenSettings = { },
                onExit = { },
                enableMotion = false,
            )
        }

        composeRule.onNodeWithText("בואו נשחק").assertIsDisplayed()
    }

    @Test
    fun opening_settingsInvokesCallback() {
        var opened = false
        setTestContent {
            OpeningScreen(
                onPlay = { },
                onOpenSettings = { opened = true },
                onExit = { },
                enableMotion = false,
            )
        }

        composeRule.onNodeWithTag("opening_settings").performClick()
        composeRule.runOnIdle {
            assertTrue(opened)
        }
    }

    @Test
    fun opening_exitInvokesCallback() {
        var exited = false
        setTestContent {
            OpeningScreen(
                onPlay = { },
                onOpenSettings = { },
                onExit = { exited = true },
                enableMotion = false,
            )
        }

        composeRule.onNodeWithTag("opening_exit").performClick()
        composeRule.runOnIdle {
            assertTrue(exited)
        }
    }

    @Test
    fun seasons_showsBackAndInvokesCallback() {
        var wentBack = false
        setTestContent {
            SeasonsScreen(
                onOpenSeason1 = { },
                onBackToOpening = { wentBack = true },
            )
        }

        composeRule.onNodeWithText("חזור").assertIsDisplayed()
        composeRule.onNodeWithText("בחר עונה").assertIsDisplayed()
        composeRule.onNodeWithText("חזור").performClick()
        composeRule.runOnIdle {
            assertTrue(wentBack)
        }
    }

    @Test
    fun seasons_showsBackAndTitleByTag() {
        setTestContent {
            SeasonsScreen(
                onOpenSeason1 = { },
                onBackToOpening = { },
            )
        }

        composeRule.onNodeWithTag("seasons_back").assertIsDisplayed()
        composeRule.onNodeWithTag("seasons_title").assertIsDisplayed()
    }

    @Test
    fun seasons_showsSeasonCardsAndContinue() {
        setTestContent {
            SeasonsScreen(
                onOpenSeason1 = { },
                onBackToOpening = { },
            )
        }

        composeRule.onNodeWithText("עונה 1: המסע הראשון").assertIsDisplayed()
        composeRule.onNodeWithText("עונה 2: מילים חדשות").assertIsDisplayed()
        composeRule.onNodeWithText("עונה 3: קוראים יותר").assertIsDisplayed()
        composeRule.onNodeWithText("עונה 4: הרפתקה חדשה").assertIsDisplayed()
        composeRule.onNodeWithText("המשך").assertIsDisplayed()
    }

    @Test
    fun seasons_continueInvokesCallback() {
        var openedSeason1 = false
        setTestContent {
            SeasonsScreen(
                onOpenSeason1 = { openedSeason1 = true },
                onBackToOpening = { },
            )
        }

        composeRule.onNodeWithText("המשך").performClick()
        composeRule.runOnIdle { assertTrue(openedSeason1) }
    }

    @Test
    fun navigation_openingToSeasonsAndBack() {
        setTestContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "opening") {
                composable("opening") {
                    OpeningScreen(
                        onPlay = { navController.navigate("seasons") },
                        onOpenSettings = { },
                        onExit = { },
                        enableMotion = false,
                    )
                }
                composable("seasons") {
                    SeasonsScreen(
                        onOpenSeason1 = { },
                        onBackToOpening = { navController.popBackStack() },
                    )
                }
            }
        }

        composeRule.onNodeWithTag("opening_play").assertIsDisplayed()
        composeRule.onNodeWithTag("opening_play").performClick()
        composeRule.onNodeWithTag("seasons_back").assertIsDisplayed()
        composeRule.onNodeWithTag("seasons_back").performClick()
        composeRule.onNodeWithTag("opening_settings").assertIsDisplayed()
    }
}
