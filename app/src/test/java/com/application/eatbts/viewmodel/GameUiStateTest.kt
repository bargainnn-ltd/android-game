package com.application.eatbts.viewmodel

import kotlin.test.Test
import kotlin.test.assertEquals

class GameUiStateTest {

    @Test
    fun `default state has three skips per player`() {
        val s = GameUiState()
        assertEquals(listOf(3, 3), s.skipsRemaining)
    }

    @Test
    fun `default state has no preselected truth or dare`() {
        val s = GameUiState()
        assertEquals(null, s.selectedChoice)
    }
}
