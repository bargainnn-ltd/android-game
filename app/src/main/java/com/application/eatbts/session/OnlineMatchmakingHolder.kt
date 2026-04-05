package com.application.eatbts.session

import com.application.eatbts.data.online.OnlineTruthDareSession

object OnlineMatchmakingHolder {
    @Volatile
    var pendingSession: OnlineTruthDareSession? = null
}
