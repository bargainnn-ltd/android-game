package com.application.eatbts.data

import kotlinx.serialization.Serializable

@Serializable
data class PromptPack(
    val truths: List<PromptLine> = emptyList(),
    val dares: List<PromptLine> = emptyList(),
)
