package com.spicynights.games.navigation

/** Which game tile opened Session Setup (Screen 3). */
enum class SessionGameMode(val routeArg: String) {
    QUICK_SESSION("quick"),
    TRUTH_DARE("truth_dare"),
    NEVER("never"),
    DIRTY_DICE("dirty_dice"),
    WYR("wyr"),
    ;

    companion object {
        fun fromRouteArg(arg: String?): SessionGameMode =
            entries.find { it.routeArg == arg } ?: TRUTH_DARE
    }
}

fun sessionSetupRoute(mode: SessionGameMode): String =
    "session_setup/${mode.routeArg}"
