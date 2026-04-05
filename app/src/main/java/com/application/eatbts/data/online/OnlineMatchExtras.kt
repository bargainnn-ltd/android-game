package com.application.eatbts.data.online

import com.application.eatbts.data.Level

/** Serialized into matchmaking / match docs. */
data class OnlineTruthDareSession(
    val displayName: String,
    val level: Level,
    val includeTruths: Boolean,
    val includeDares: Boolean,
    val turnTimerSeconds: Int,
)
