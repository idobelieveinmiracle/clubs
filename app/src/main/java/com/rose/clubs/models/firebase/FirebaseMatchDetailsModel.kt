package com.rose.clubs.models.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.rose.clubs.data.*
import com.rose.clubs.viewmodels.matchdetails.JoinMatchResult
import com.rose.clubs.viewmodels.matchdetails.MatchDetailsModel
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "FirebaseMatchDetailsMod"

class FirebaseMatchDetailsModel(
    private val auth: FirebaseAuth = Firebase.auth,
    private val firestore: FirebaseFirestore = Firebase.firestore
) : MatchDetailsModel {
    override suspend fun loadMatchInfo(
        matchId: String
    ): Match? = suspendCoroutine { continuation ->
        firestore.getMatchesCollection()
            .document(matchId)
            .get()
            .addOnSuccessListener { doc ->
                doc.data?.let { data ->
                    continuation.resume(
                        Match(
                            doc.id,
                            data["location"]?.toString() ?: "",
                            (data["time"]?.toString() ?: "-1").toLong(),
                            (data["cost"]?.toString() ?: "0").toInt(),
                            emptyList()
                        )
                    )
                } ?: run {
                    continuation.resume(null)
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "loadMatchInfo: ", e)
                continuation.resume(null)
            }
    }

    override suspend fun loadPlayers(
        matchId: String
    ): List<Player> {
        val playerIds = firestore.loadPlayerIdsByMatch(matchId)
        if (playerIds.isEmpty()) {
            return emptyList()
        }

        val players = ArrayList(firestore.getPlayersByIds(playerIds))

        val userMap = firestore.loadUsersMap(players)

        if (playerIds.size > players.size) {
            repeat(playerIds.size - players.size) {
                players.add(
                    Player(
                        "",
                        User("", "", "Anonymous", ""),
                        Club("", "", ""),
                        0,
                        Role.MEMBER,
                        0
                    )
                )
            }
        }

        return players.map { it.copy(user = userMap.getOrDefault(it.user.userId, it.user)) }
    }

    override suspend fun joinMatch(matchId: String): JoinMatchResult {
        val user = auth.currentUser?.getUser() ?: return JoinMatchResult.Failed("User data error!")

        val clubId =
            firestore.getClubIdOfMatch(matchId) ?: return JoinMatchResult.Failed("Club data error!")

        val playerId = firestore.getPlayerIdOfUserInClub(clubId = clubId, userId = user.userId)
            ?: return JoinMatchResult.Failed("Player data error!")

        val playerIds = firestore.loadPlayerIdsByMatch(matchId)
        val newPlayerIds = ArrayList(playerIds).apply {
            add(playerId)
        }.distinct()

        return suspendCoroutine { continuation ->
            firestore.getMatchesCollection()
                .document(matchId)
                .update("players", newPlayerIds)
                .addOnCompleteListener { res ->
                    if (res.isSuccessful) {
                        continuation.resume(JoinMatchResult.Success)
                    } else {
                        Log.e(TAG, "joinMatch: ", res.exception)
                        continuation.resume(JoinMatchResult.Failed("Join match failed!"))
                    }
                }
        }
    }

    override suspend fun getJoinEnabled(
        matchId: String,
        players: List<Player>
    ): Boolean {
        val user = auth.currentUser?.getUser() ?: return false

        if (players.find { it.user.userId == user.userId } != null) {
            return false
        }

        val clubId = firestore.getClubIdOfMatch(matchId) ?: return false

        return firestore.getPlayerIdOfUserInClub(clubId = clubId, userId = user.userId) != null
    }
}