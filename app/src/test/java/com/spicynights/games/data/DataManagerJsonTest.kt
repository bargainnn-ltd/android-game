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
        assertEquals(listOf("a", "b"), pack.truths)
        assertEquals(listOf("c"), pack.dares)
    }

    @Test
    fun `empty arrays allowed`() {
        val raw = """{"truths":[],"dares":[]}"""
        val pack = json.decodeFromString<PromptPack>(raw)
        assertTrue(pack.truths.isEmpty())
        assertTrue(pack.dares.isEmpty())
    }
}
