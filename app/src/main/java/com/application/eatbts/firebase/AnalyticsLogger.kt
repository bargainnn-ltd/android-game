package com.application.eatbts.firebase

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class AnalyticsLogger(
    private val analytics: FirebaseAnalytics,
) {
    fun logLogin(method: String) {
        analytics.logEvent(FirebaseAnalytics.Event.LOGIN, Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, method)
        })
    }

    fun logSignUp(method: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, method)
        })
    }

    fun logMatchStart(matchId: String) {
        analytics.logEvent("match_start", Bundle().apply {
            putString("match_id", matchId)
        })
    }

    fun logMatchEnd(matchId: String) {
        analytics.logEvent("match_end", Bundle().apply {
            putString("match_id", matchId)
        })
    }

    fun logMatchTurn(matchId: String) {
        analytics.logEvent("match_turn", Bundle().apply {
            putString("match_id", matchId)
        })
    }

    fun logAdReward(type: String) {
        analytics.logEvent("ad_reward", Bundle().apply {
            putString("ad_type", type)
        })
    }

    fun logAdImpression(type: String) {
        analytics.logEvent("ad_impression", Bundle().apply {
            putString("ad_type", type)
        })
    }
}
