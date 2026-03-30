package com.spicynights.games.viewmodel

import com.spicynights.games.data.PoolMode
import com.spicynights.games.data.PromptPack
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SessionQueuesTest {

    @Test
    fun `buildSessionQueues ALL keeps all when both included`() {
        val pack = PromptPack(
            truths = (1..10).map { "t$it" },
            dares = (1..10).map { "d$it" },
        )
        val (t, d) = buildSessionQueues(
            pack = pack,
            includeTruths = true,
            includeDares = true,
            poolMode = PoolMode.ALL,
            random = Random(0),
        )
        assertEquals(10, t.size)
        assertEquals(10, d.size)
    }

    @Test
    fun `buildSessionQueues RANDOM_20 caps total`() {
        val pack = PromptPack(
            truths = (1..20).map { "t$it" },
            dares = (1..20).map { "d$it" },
        )
        val (t, d) = buildSessionQueues(
            pack = pack,
            includeTruths = true,
            includeDares = true,
            poolMode = PoolMode.RANDOM_20,
            random = Random(42),
        )
        assertEquals(20, t.size + d.size)
    }

    @Test
    fun `buildSessionQueues excludes truths when disabled`() {
        val pack = PromptPack(truths = listOf("a"), dares = listOf("b"))
        val (t, d) = buildSessionQueues(
            pack = pack,
            includeTruths = false,
            includeDares = true,
            poolMode = PoolMode.ALL,
            random = Random(1),
        )
        assertTrue(t.isEmpty())
        assertEquals(1, d.size)
    }
}
