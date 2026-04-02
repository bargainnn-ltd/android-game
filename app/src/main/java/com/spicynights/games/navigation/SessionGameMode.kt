package com.spicynights.games.navigation

/** Which game tile opened Session Setup (Screen 3). */
enum class SessionGameMode(val routeArg: String) {
    TRUTH_DARE("truth_dare"),
    NEVER("never"),
    SPICY_SPINNER("spicy_spinner"),
    WYR("wyr"),
    ;

    companion object {
        fun fromRouteArg(arg: String?): SessionGameMode =
            entries.find { it.routeArg == arg } ?: TRUTH_DARE
    }
}

fun sessionSetupRoute(mode: SessionGameMode): String =
    "session_setup/${mode.routeArg}"
