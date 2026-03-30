package com.spicynights.games.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DataManagerInstrumentedTest {

    @Test
    fun loadsAllLevelsFromAssets() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val dm = DataManager(ctx)
        Level.entries.forEach { level ->
            val result = dm.loadLevel(level)
            assertTrue(result.isSuccess)
            val pack = result.getOrThrow()
            assertTrue(pack.truths.isNotEmpty())
            assertTrue(pack.dares.isNotEmpty())
        }
    }
}
