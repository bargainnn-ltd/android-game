package com.spicynights.games.viewmodel

import com.spicynights.games.data.PoolMode
import com.spicynights.games.data.PromptLine
import com.spicynights.games.data.PromptPack
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SessionQueuesTest {

    @Test
    fun `buildSessionQueues ALL keeps all when both included`() {
        val pack = PromptPack(
            truths = (1..10).map { PromptLine("t$it", 0) },
            dares = (1..10).map { PromptLine("d$it", 0) },
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
            truths = (1..20).map { PromptLine("t$it", 0) },
            dares = (1..20).map { PromptLine("d$it", 0) },
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
        val pack = PromptPack(truths = listOf(PromptLine("a", 0)), dares = listOf(PromptLine("b", 0)))
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

    @Test
    fun `orderPromptLines sorts by tier then shuffles within tier`() {
        val lines = listOf(
            PromptLine("c", 3),
            PromptLine("a", 1),
            PromptLine("b", 2),
            PromptLine("a2", 1),
        )
        val ordered = orderPromptLines(lines, Random(12345))
        assertEquals(listOf(1, 1, 2, 3), ordered.map { it.tier })
        assertEquals(setOf("a", "a2"), ordered.take(2).map { it.text }.toSet())
        assertEquals("b", ordered[2].text)
        assertEquals("c", ordered[3].text)
    }

    @Test
    fun `orderPromptLines all tier zero is full shuffle`() {
        val lines = listOf(
            PromptLine("x", 0),
            PromptLine("y", 0),
            PromptLine("z", 0),
        )
        val ordered = orderPromptLines(lines, Random(999))
        assertEquals(setOf("x", "y", "z"), ordered.map { it.text }.toSet())
        assertEquals(3, ordered.size)
    }

    @Test
    fun `buildSessionQueues orders dares by tier`() {
        val pack = PromptPack(
            truths = emptyList(),
            dares = listOf(
                PromptLine("late", 5),
                PromptLine("early", 1),
                PromptLine("mid", 3),
            ),
        )
        val (_, d) = buildSessionQueues(
            pack = pack,
            includeTruths = false,
            includeDares = true,
            poolMode = PoolMode.ALL,
            random = Random(1),
        )
        assertEquals(listOf("early", "mid", "late"), d)
    }
}
