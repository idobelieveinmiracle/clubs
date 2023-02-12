package com.rose.clubs.models.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.rose.clubs.data.Club
import com.rose.clubs.viewmodels.notifications.NotificationData
import com.rose.clubs.viewmodels.notifications.NotificationsModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "FirebaseClubNotificatio"

class FirebaseClubNotificationsModel(
    private val clubId: String,
    private val firestore: FirebaseFirestore = Firebase.firestore,
) : NotificationsModel {
    override suspend fun fetchNotifications(): List<NotificationData> {
        val players = firestore.loadAskingPlayers(clubId)

        if (players.isEmpty()) {
            return emptyList()
        }

        val usersMap = firestore.loadUsersMap(players)

        return players.map { player ->
            val user = usersMap[player.user.userId]
            val displayName = user?.displayName ?: "A player"

            NotificationData(
                player.playerId,
                displayName,
                "$displayName want to join your club",
                user?.avatarUrl ?: ""
            )
        }
    }

    override suspend fun getClubInfo(): Club? = firestore.loadClub(clubId)

    override suspend fun agreeNotification(id: String) {
        val players = firestore.loadPlayers(clubId)

        val number = players.maxBy { it.number }.number + 1

        suspendCoroutine { continuation ->
            firestore.getPlayersCollection()
                .document(id)
                .update("number", number)
                .addOnCompleteListener {
                    Log.i(TAG, "agreeNotification: ${it.exception}")
                    continuation.resume(Unit)
                }
        }
    }

    override suspend fun disagreeNotification(id: String) {
        firestore.deletePlayer(id)
    }
}