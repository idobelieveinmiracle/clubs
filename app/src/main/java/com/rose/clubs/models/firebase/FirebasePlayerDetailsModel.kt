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
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "FirebasePlayerDetailsMo"

class FirebasePlayerDetailsModel(
    private val auth: FirebaseAuth = Firebase.auth,
    private val firestore: FirebaseFirestore = Firebase.firestore
) : PlayerDetailModel {
    override suspend fun getPlayerInfo(playerId: String): Player? {
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
        } ?: return null

        val userMap = firestore.loadUsersMap(listOf(player))

        return player.copy(user = userMap.getOrDefault(player.user.userId, player.user))
    }

    override suspend fun getViewerRole(clubId: String): Role {
        if (clubId.isEmpty()) {
            return Role.MEMBER
        }
        val user = auth.currentUser?.getUser() ?: return Role.MEMBER

        val player = firestore.getPlayerInfoOfUserInClub(clubId, user.userId) ?: return Role.MEMBER
        return player.role
    }
}