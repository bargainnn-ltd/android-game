package com.spicynights.games.data

import android.content.Context
import com.spicynights.games.data.party.NeverPromptsFile
import com.spicynights.games.data.party.WyrPairDto
import com.spicynights.games.data.party.WyrPairsFile
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

    fun loadNeverPrompts(level: Level): Result<List<String>> = runCatching {
        val name = "never_${level.partyAssetSuffix()}.json"
        context.assets.open("data/$name").use { stream ->
            json.decodeFromString<NeverPromptsFile>(stream.bufferedReader().readText()).prompts
        }
    }

    fun loadWyrPairs(level: Level): Result<List<WyrPairDto>> = runCatching {
        val name = "wyr_${level.partyAssetSuffix()}.json"
        context.assets.open("data/$name").use { stream ->
            json.decodeFromString<WyrPairsFile>(stream.bufferedReader().readText()).pairs
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

private fun Level.partyAssetSuffix(): String = when (this) {
    Level.MILD -> "mild"
    Level.SPICY -> "spicy"
    Level.EXTREME -> "extreme"
}
