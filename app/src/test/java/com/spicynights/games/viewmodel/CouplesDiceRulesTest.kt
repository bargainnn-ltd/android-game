package com.spicynights.games.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CouplesDiceRulesTest {

    @Test
    fun `double roll when both dice match`() {
        assertTrue(CouplesDiceRules.isDoubleRoll(6, 6))
        assertTrue(CouplesDiceRules.isDoubleRoll(1, 1))
        assertFalse(CouplesDiceRules.isDoubleRoll(3, 4))
    }

    @Test
    fun `body and action indices map 1 to 8`() {
        assertEquals(0, CouplesDiceRules.bodyPartIndex(1))
        assertEquals(7, CouplesDiceRules.bodyPartIndex(8))
        assertEquals(2, CouplesDiceRules.actionIndex(3))
        assertEquals(7, CouplesDiceRules.actionIndex(8))
    }

    @Test
    fun `max turns is five per player`() {
        assertEquals(10, CouplesDiceRules.maxTurns(2))
        assertEquals(15, CouplesDiceRules.maxTurns(3))
    }
}
