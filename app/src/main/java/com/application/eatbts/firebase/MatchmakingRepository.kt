package com.application.eatbts.firebase

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class MatchmakingTicketParams(
    val shuffleSeed: Long,
    val truthsRemaining: Int,
    val daresRemaining: Int,
    val levelKey: String,
    val includeTruths: Boolean,
    val includeDares: Boolean,
    val turnTimerSeconds: Int,
)

class MatchmakingRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {

    fun seekingTicketsFlow(gameMode: String): Flow<List<DocumentSnapshot>> = callbackFlow {
        val reg = db.collection(COLLECTION)
            .whereEqualTo(FIELD_GAME_MODE, gameMode)
            .whereEqualTo(FIELD_SEEKING, true)
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                trySend(snap?.documents ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    suspend fun enqueueTicket(
        uid: String,
        displayName: String,
        gameMode: String,
        params: MatchmakingTicketParams,
    ): String {
        val ticketId = UUID.randomUUID().toString()
        db.collection(COLLECTION).document(ticketId).set(
            mapOf(
                "uid" to uid,
                "display_name" to displayName,
                FIELD_GAME_MODE to gameMode,
                FIELD_SEEKING to true,
                "shuffle_seed" to params.shuffleSeed,
                "truths_remaining_init" to params.truthsRemaining,
                "dares_remaining_init" to params.daresRemaining,
                "level" to params.levelKey,
                "include_truths" to params.includeTruths,
                "include_dares" to params.includeDares,
                "turn_timer_seconds" to params.turnTimerSeconds,
                "created_at" to FieldValue.serverTimestamp(),
            ),
        ).await()
        return ticketId
    }

    /**
     * @return match id if this client created the match, null otherwise.
     */
    suspend fun tryPairOldest(
        myUid: String,
        myTicketId: String,
        tickets: List<DocumentSnapshot>,
        gameMode: String,
    ): String? {
        val ready = tickets
            .filter { it.getBoolean(FIELD_SEEKING) == true }
            .filter { it.getString(FIELD_GAME_MODE) == gameMode }
            .sortedWith(
                compareBy<DocumentSnapshot> { it.getTimestamp("created_at") }
                    .thenBy { it.id },
            )
        if (ready.size < 2) return null
        val t0 = ready[0]
        val t1 = ready[1]
        val u0 = t0.getString("uid") ?: return null
        val u1 = t1.getString("uid") ?: return null
        if (u0 == u1) return null
        if (myUid != u0 && myUid != u1) return null
        val runnerId = minOf(t0.id, t1.id)
        if (myTicketId != runnerId) return null

        val lead = t0
        val partner = t1
        val leadUid = u0
        val partnerUid = u1

        if (!matchParamsCompatible(lead, partner)) return null

        val players = listOf(leadUid, partnerUid)
        val nameLead = lead.getString("display_name")?.trim()?.ifEmpty { "Player" } ?: "Player"
        val namePartner = partner.getString("display_name")?.trim()?.ifEmpty { "Player" } ?: "Player"
        val playerNames = listOf(nameLead, namePartner)

        val shuffleSeed = lead.getLong("shuffle_seed") ?: return null
        val truthsRem = lead.getLong("truths_remaining_init")?.toInt() ?: return null
        val daresRem = lead.getLong("dares_remaining_init")?.toInt() ?: return null
        val level = lead.getString("level") ?: return null
        val includeTruths = lead.getBoolean("include_truths") ?: true
        val includeDares = lead.getBoolean("include_dares") ?: true
        val turnTimer = lead.getLong("turn_timer_seconds")?.toInt() ?: 30

        val matchRef = db.collection("matches").document()
        val gameState = initialGameState(truthsRem, daresRem)

        val createdId = db.runTransaction<String?> { tx ->
            val l = tx.get(lead.reference)
            val p = tx.get(partner.reference)
            if (!l.exists() || !p.exists()) return@runTransaction null
            if (l.getBoolean(FIELD_SEEKING) != true || p.getBoolean(FIELD_SEEKING) != true) {
                return@runTransaction null
            }
            tx.set(
                matchRef,
                mapOf(
                    "players" to players,
                    "player_names" to playerNames,
                    "current_turn" to leadUid,
                    "shuffle_seed" to shuffleSeed,
                    "level" to level,
                    "include_truths" to includeTruths,
                    "include_dares" to includeDares,
                    "turn_timer_seconds" to turnTimer,
                    "status" to "active",
                    "game_state" to gameState,
                    "created_at" to FieldValue.serverTimestamp(),
                    "updated_at" to FieldValue.serverTimestamp(),
                ),
            )
            tx.delete(lead.reference)
            tx.delete(partner.reference)
            matchRef.id
        }.await()

        return createdId
    }

    suspend fun deleteTicket(ticketId: String) {
        db.collection(COLLECTION).document(ticketId).delete().await()
    }

    suspend fun fetchLatestActiveMatchForPlayer(uid: String): String? {
        val snap = db.collection("matches")
            .whereArrayContains("players", uid)
            .whereEqualTo("status", "active")
            .orderBy("created_at", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .await()
        return snap.documents.firstOrNull()?.id
    }

    /** Same session settings; deck is taken from the older (lead) ticket only. */
    private fun matchParamsCompatible(a: DocumentSnapshot, b: DocumentSnapshot): Boolean {
        return a.getString("level") == b.getString("level") &&
            a.getBoolean("include_truths") == b.getBoolean("include_truths") &&
            a.getBoolean("include_dares") == b.getBoolean("include_dares") &&
            a.getLong("turn_timer_seconds") == b.getLong("turn_timer_seconds")
    }

    private fun initialGameState(truthsRem: Int, daresRem: Int): Map<String, Any?> = mapOf(
        "phase" to "face_down",
        "current_player_index" to 0,
        "current_prompt" to "",
        "selected_choice" to null,
        "pending_reveal_is_truth" to true,
        "truths_remaining" to truthsRem,
        "dares_remaining" to daresRem,
        "cards_revealed" to 0,
        "skips_remaining" to listOf(3, 3),
    )

    companion object {
        const val GAME_MODE_TRUTH_DARE = "truth_dare"
        private const val COLLECTION = "matchmaking"
        private const val FIELD_GAME_MODE = "game_mode"
        private const val FIELD_SEEKING = "seeking"
    }
}
