package com.application.eatbts.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.application.eatbts.CoupleGamesApp
import kotlinx.coroutines.launch

class CoupleGamesFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val app = applicationContext as? CoupleGamesApp ?: return
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val user = FirebaseAuth.getInstance().currentUser ?: return
        app.applicationScope.launch {
            runCatching {
                app.playerProfileRepository.upsertPlayer(
                    userId = uid,
                    name = user.displayName?.trim()?.ifEmpty { null } ?: "Guest",
                    avatarUrl = user.photoUrl?.toString(),
                    fcmToken = token,
                )
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
    }
}
