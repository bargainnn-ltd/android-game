package com.application.eatbts.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * A prompt with optional [tier] for escalation ordering (lower = earlier in session).
 * JSON may be a plain string (tier 0) or `{ "text": "...", "tier": n }`.
 */
@Serializable(with = PromptLineSerializer::class)
data class PromptLine(
    val text: String,
    val tier: Int = 0,
)

object PromptLineSerializer : KSerializer<PromptLine> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("PromptLine")

    override fun deserialize(decoder: Decoder): PromptLine {
        val jsonDecoder = decoder as? JsonDecoder
            ?: error("PromptLine expects JSON input")
        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonPrimitive -> PromptLine(element.content, 0)
            is JsonObject -> {
                val textEl = element["text"] ?: error("PromptLine object missing \"text\"")
                val text = textEl.jsonPrimitive.content
                val tier = element["tier"]?.jsonPrimitive?.intOrNull ?: 0
                PromptLine(text, tier)
            }
            else -> error("Unexpected JSON for PromptLine")
        }
    }

    override fun serialize(encoder: Encoder, value: PromptLine) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: error("PromptLine expects JSON output")
        if (value.tier == 0) {
            jsonEncoder.encodeJsonElement(JsonPrimitive(value.text))
        } else {
            jsonEncoder.encodeJsonElement(
                JsonObject(
                    mapOf(
                        "text" to JsonPrimitive(value.text),
                        "tier" to JsonPrimitive(value.tier),
                    ),
                ),
            )
        }
    }
}
