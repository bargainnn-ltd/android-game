package com.application.eatbts.firebase

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class PlayerProfileRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {

    suspend fun upsertPlayer(
        userId: String,
        name: String,
        avatarUrl: String?,
        fcmToken: String? = null,
    ) {
        val ref = db.collection(COLLECTION).document(userId)
        val snap = ref.get().await()
        val data = mutableMapOf<String, Any>(
            "user_id" to userId,
            "name" to name,
            "avatar" to (avatarUrl ?: ""),
            "last_login" to FieldValue.serverTimestamp(),
        )
        if (!snap.exists()) {
            data["created_at"] = FieldValue.serverTimestamp()
        }
        if (fcmToken != null) {
            data["fcm_token"] = fcmToken
        }
        ref.set(data, SetOptions.merge()).await()
    }
}

private const val COLLECTION = "players"
