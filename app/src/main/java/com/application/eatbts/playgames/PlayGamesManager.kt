package com.application.eatbts.playgames

import android.app.Activity
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.Player
import kotlinx.coroutines.tasks.await

/**
 * Play Games Services v2 — create leaderboards/achievements in Play Console and replace IDs below.
 */
class PlayGamesManager {

    suspend fun signInSilently(activity: Activity): Boolean {
        return try {
            PlayGames.getGamesSignInClient(activity).isAuthenticated.await().isAuthenticated
        } catch (_: Exception) {
            false
        }
    }

    suspend fun requestInteractiveSignIn(activity: Activity): Boolean {
        return try {
            PlayGames.getGamesSignInClient(activity).signIn().await().isAuthenticated
        } catch (_: Exception) {
            false
        }
    }

    suspend fun currentPlayer(activity: Activity): Player? {
        return try {
            PlayGames.getPlayersClient(activity).currentPlayer.await()
        } catch (_: Exception) {
            null
        }
    }

    fun submitScore(activity: Activity, leaderboardId: String, score: Long) {
        if (leaderboardId.isBlank() || leaderboardId == "YOUR_LEADERBOARD_ID") return
        PlayGames.getLeaderboardsClient(activity).submitScore(leaderboardId, score)
    }

    fun unlockAchievement(activity: Activity, achievementId: String) {
        if (achievementId.isBlank() || achievementId == "YOUR_ACHIEVEMENT_ID") return
        PlayGames.getAchievementsClient(activity).unlock(achievementId)
    }
}
