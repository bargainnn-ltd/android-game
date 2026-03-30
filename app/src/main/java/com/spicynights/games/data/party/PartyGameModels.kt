package com.spicynights.games.data.party

import kotlinx.serialization.Serializable

@Serializable
data class NeverPromptsFile(
    val prompts: List<String> = emptyList(),
)

@Serializable
data class WyrPairsFile(
    val pairs: List<WyrPairDto> = emptyList(),
)

@Serializable
data class WyrPairDto(
    val a: String,
    val b: String,
)

