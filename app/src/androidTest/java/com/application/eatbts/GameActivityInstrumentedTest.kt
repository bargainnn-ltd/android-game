package com.application.eatbts

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameActivityInstrumentedTest {

    @Test
    fun finishesWhenLaunchedWithoutConfigOrMatchId() {
        ActivityScenario.launch(GameActivity::class.java).use { scenario ->
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity { activity ->
                assertTrue(
                    "GameActivity should not stay open without EXTRA_CONFIG or EXTRA_MATCH_ID",
                    activity.isFinishing || activity.isDestroyed,
                )
            }
        }
    }
}
