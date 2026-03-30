package com.spicynights.games.data

import kotlinx.serialization.Serializable

@Serializable
data class PromptPack(
    val truths: List<String> = emptyList(),
    val dares: List<String> = emptyList(),
)
