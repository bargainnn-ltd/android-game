package com.application.eatbts

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConfigurationMatrixInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

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
    fun hubRemainsVisibleAfterOrientationChanges() {
        passAgeGateIfNeeded()
        composeRule.onNodeWithTag("game_hub_screen").assertIsDisplayed()

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.setOrientationLeft()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("game_hub_screen").assertIsDisplayed()

        device.setOrientationNatural()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("game_hub_screen").assertIsDisplayed()
    }
}
