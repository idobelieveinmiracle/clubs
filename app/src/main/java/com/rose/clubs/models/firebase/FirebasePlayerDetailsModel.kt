package com.rose.clubs.models.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.rose.clubs.data.Player
import com.rose.clubs.data.Role
import com.rose.clubs.viewmodels.playerdetails.PlayerDetailModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "FirebasePlayerDetailsMo"

class FirebasePlayerDetailsModel(
    private val auth: FirebaseAuth = Firebase.auth,
    private val firestore: FirebaseFirestore = Firebase.firestore
) : PlayerDetailModel {
    override suspend fun getPlayerInfo(
        playerId: String
    ): Player? = withContext(Dispatchers.IO) {
        val player: Player = suspendCoroutine { continuation ->
            firestore.getPlayersCollection()
                .document(playerId)
                .get()
                .addOnSuccessListener { doc ->
                    continuation.resume(doc.toPlayer())
                }.addOnFailureListener { e ->
                    Log.e(TAG, "getPlayerInfo: ", e)
                    continuation.resume(null)
                }
        } ?: return@withContext null

        val userMap = async { firestore.loadUsersMap(listOf(player)) }

        val playerCost = async { firestore.getPlayersCostInClub(playerId) }

        player.copy(
            user = userMap.await().getOrDefault(player.user.userId, player.user),
            balance = player.balance - playerCost.await()
        )
    }

    override suspend fun getViewerRole(clubId: String): Role {
        if (clubId.isEmpty()) {
            return Role.MEMBER
        }
        val user = auth.currentUser?.getUser() ?: return Role.MEMBER

        val player = firestore.getPlayerInfoOfUserInClub(clubId, user.userId) ?: return Role.MEMBER
        return player.role
    }

    override suspend fun addBalance(playerId: String, delta: Int): Boolean {
        val player: Player = suspendCoroutine { continuation ->
            firestore.getPlayersCollection()
                .document(playerId)
                .get()
                .addOnSuccessListener { doc ->
                    continuation.resume(doc.toPlayer())
                }.addOnFailureListener { e ->
                    Log.e(TAG, "getPlayerInfo: ", e)
                    continuation.resume(null)
                }
        } ?: return false

        return suspendCoroutine { continuation ->
            firestore.getPlayersCollection()
                .document(playerId)
                .update("balance", player.balance + delta)
                .addOnCompleteListener { result ->
                    if (!result.isSuccessful) {
                        Log.e(TAG, "addBalance: ", result.exception)
                    }
                    continuation.resume(result.isSuccessful)
                }
        }
    }

    override suspend fun kickPlayer(playerId: String): Boolean {
        return firestore.deletePlayer(playerId)
    }
}