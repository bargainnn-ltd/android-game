package com.spicynights.games

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityComposeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun ageGateAndHubVisible() {
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
        composeRule.onNodeWithTag("game_hub_screen").assertIsDisplayed()

        composeRule.onNodeWithTag("hub_game_never").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("session_setup_screen").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("session_setup_screen").assertIsDisplayed()
    }
}
