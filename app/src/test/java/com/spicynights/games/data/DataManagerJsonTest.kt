package com.spicynights.games.data

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DataManagerJsonTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parses prompt pack`() {
        val raw = """
            {"truths":["a","b"],"dares":["c"]}
        """.trimIndent()
        val pack = json.decodeFromString<PromptPack>(raw)
        assertEquals(
            listOf(PromptLine("a", 0), PromptLine("b", 0)),
            pack.truths,
        )
        assertEquals(listOf(PromptLine("c", 0)), pack.dares)
    }

    @Test
    fun `empty arrays allowed`() {
        val raw = """{"truths":[],"dares":[]}"""
        val pack = json.decodeFromString<PromptPack>(raw)
        assertTrue(pack.truths.isEmpty())
        assertTrue(pack.dares.isEmpty())
    }

    @Test
    fun `parses dare objects with tier`() {
        val raw = """
            {"truths":["plain"],"dares":[{"text":"x","tier":3}]}
        """.trimIndent()
        val pack = json.decodeFromString<PromptPack>(raw)
        assertEquals(listOf(PromptLine("plain", 0)), pack.truths)
        assertEquals(listOf(PromptLine("x", 3)), pack.dares)
    }
}
