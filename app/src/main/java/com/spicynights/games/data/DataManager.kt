package com.spicynights.games.data

import android.content.Context
import kotlinx.serialization.json.Json

class DataManager(
    private val context: Context,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    },
) {
    fun loadLevel(level: Level): Result<PromptPack> = runCatching {
        context.assets.open("data/${level.assetFileName}").use { stream ->
            val text = stream.bufferedReader().readText()
            json.decodeFromString<PromptPack>(text)
        }
    }

    fun buildSessionPack(
        base: PromptPack,
        customTruthLines: List<String>,
        customDareLines: List<String>,
    ): PromptPack {
        val extraTruths = customTruthLines.map { it.trim() }.filter { it.isNotEmpty() }
        val extraDares = customDareLines.map { it.trim() }.filter { it.isNotEmpty() }
        return PromptPack(
            truths = base.truths + extraTruths,
            dares = base.dares + extraDares,
        )
    }
}
