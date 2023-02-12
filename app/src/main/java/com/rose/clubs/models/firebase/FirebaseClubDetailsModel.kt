package com.rose.clubs.models.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.rose.clubs.data.Club
import com.rose.clubs.data.Player
import com.rose.clubs.data.Role
import com.rose.clubs.data.User
import com.rose.clubs.viewmodels.clubdetails.ActionType
import com.rose.clubs.viewmodels.clubdetails.ClubDetailsModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "FirebaseClubDetailsMode"

class FirebaseClubDetailsModel(
    private val firestore: FirebaseFirestore = Firebase.firestore,
    private val auth: FirebaseAuth = Firebase.auth
) : ClubDetailsModel {
    override suspend fun loadClubData(clubId: String): Club? = firestore.loadClub(clubId)

    override suspend fun loadPlayers(
        clubId: String
    ): List<Player> = withContext(Dispatchers.IO) {
        val club = Club(clubId, "", "")

        val players = firestore.loadPlayers(club)

        Log.i(TAG, "loadPlayers: $players")

        if (players.isEmpty()) {
            return@withContext players
        }

        val usersMap = firestore.loadUsersMap(players)

        players.map { player ->
            player.copy(
                user = usersMap.getOrDefault(player.user.userId, player.user)
            )
        }
    }

    override suspend fun askToJoin(clubId: String): Boolean {
        val user = auth.currentUser?.getUser() ?: return false
        return askNewPlayerForClub(clubId, user)
    }

    override suspend fun cancelAsk(clubId: String): Boolean {
        val user = auth.currentUser?.getUser() ?: return false

        val player = getPlayerInfo(clubId, user.userId) ?: return false

        return firestore.deletePlayer(player.playerId)
    }

    override suspend fun getActionType(clubId: String, players: List<Player>?): ActionType {
        val user = auth.currentUser?.getUser() ?: return ActionType.NONE

        val loadedPlayers = players ?: loadPlayers(clubId)

        val player = loadedPlayers.find { it.user.userId == user.userId }
        Log.i(TAG, "getActionType: player $player")

        if (player != null) {
            return if (player.role == Role.MEMBER) {
                ActionType.NONE
            } else {
                ActionType.ADD_MATCH
            }
        }

        val askingPlayers = loadAskingPlayers(clubId)

        return if (askingPlayers.find { it.user.userId == user.userId } != null) {
            ActionType.CANCEL_ASK
        } else {
            ActionType.ASK_TO_JOIN
        }
    }

    private suspend fun loadAskingPlayers(
        clubId: String
    ) : List<Player> = firestore.loadAskingPlayers(clubId)

    private suspend fun askNewPlayerForClub(
        clubId: String,
        user: User
    ): Boolean = suspendCoroutine { continuation ->
        firestore.getPlayersCollection()
            .add(hashMapOf(
                "clubId" to clubId,
                "userId" to user.userId,
                "number" to -1,
                "role" to Role.MEMBER.value,
                "balance" to 0,
            )).addOnCompleteListener { result ->
                Log.i(TAG, "askNewPlayerForClub: ${result.exception}")
                continuation.resume(result.isSuccessful)
            }
    }

    private suspend fun getPlayerInfo(
        clubId: String,
        userId: String
    ): Player? = suspendCoroutine { continuation ->
        firestore.getPlayersCollection()
            .whereEqualTo("clubId", clubId)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { docs ->
                if (docs.isEmpty) {
                    continuation.resume(null)
                } else {
                    continuation.resume(docs.first().toPlayer())
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "getPlayerInfo: ", e)
                continuation.resume(null)
            }
    }
}