package com.application.eatbts

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.application.eatbts.data.local.AppThemePreference
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsThemeInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun app(): CoupleGamesApp =
        InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as CoupleGamesApp

    @After
    fun resetTheme() = runBlocking {
        app().preferencesRepository.setAppThemePreference(AppThemePreference.MIDNIGHT)
    }

    private fun passAgeGateIfNeeded() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("age_verification_screen").fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithTag("game_hub_screen").fetchSemanticsNodes().isNotEmpty()
        }
        if (composeRule.onAllNodesWithTag("age_checkbox").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithTag("age_checkbox").performClick()
            composeRule.onNodeWithTag("age_enter").performClick()
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("game_hub_screen").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun lightThemeSelectionPersistsInDataStore() = runBlocking {
        passAgeGateIfNeeded()
        composeRule.onNodeWithTag("hub_menu_button").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("settings_screen").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("settings_theme_light").performClick()
        composeRule.waitForIdle()

        val theme = app().preferencesRepository.appThemePreference.first()
        assertEquals(AppThemePreference.LIGHT, theme)
    }
}
