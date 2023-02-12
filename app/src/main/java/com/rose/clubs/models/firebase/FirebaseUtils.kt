package com.rose.clubs.models.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.rose.clubs.data.*
import com.rose.clubs.viewmodels.addmatch.SaveMatchResult
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "FirebaseUtils"

fun FirebaseFirestore.getClubsCollection(): CollectionReference {
    return collection("Clubs")
}

fun FirebaseFirestore.getUsersCollection(): CollectionReference {
    return collection("Users")
}

fun FirebaseFirestore.getPlayersCollection(): CollectionReference {
    return collection("Players")
}

fun FirebaseFirestore.getMatchesCollection(): CollectionReference {
    return collection("Matches")
}

fun FirebaseUser.getUser(): User {
    return User(uid, email ?: "", displayName ?: "", photoUrl.toString())
}

fun QueryDocumentSnapshot.toClub(): Club {
    return Club(
        id,
        data["name"]?.toString() ?: "",
        data["avatarUrl"]?.toString() ?: ""
    )
}

fun QueryDocumentSnapshot.toPlayer(
    club: Club = Club(
        clubId = data["clubId"]?.toString() ?: "",
        name = "",
        avatarUrl = ""
    ),
    user: User = User(
        userId = data["userId"]?.toString() ?: "",
        email = "",
        displayName = "",
        avatarUrl = ""
    )
): Player {
    return Player(
        playerId = id,
        user = user,
        club = club,
        number = Integer.parseInt(data["number"]?.toString() ?: "0"),
        balance = Integer.parseInt(data["balance"]?.toString() ?: "0"),
        role = Role.fromValue(
            Integer.parseInt(
                data["role"]?.toString() ?: "3"
            )
        ),
    )
}

fun QueryDocumentSnapshot.toMatch(): Match {
    return Match(
        id,
        data["location"]?.toString() ?: "",
        (data["time"]?.toString() ?: "-1").toLong(),
        (data["cost"]?.toString() ?: "0").toInt(),
        emptyList()
    )
}

fun DocumentSnapshot.toClub(): Club {
    return Club(
        id,
        data?.get("name")?.toString() ?: "",
        data?.get("avatarUrl")?.toString() ?: ""
    )
}

fun DocumentSnapshot.toPlayer(
    club: Club = Club(
        clubId = data?.get("clubId")?.toString() ?: "",
        name = "",
        avatarUrl = ""
    ),
    user: User = User(
        userId = data?.get("userId")?.toString() ?: "",
        email = "",
        displayName = "",
        avatarUrl = ""
    )
): Player {
    return Player(
        playerId = id,
        user = user,
        club = club,
        number = Integer.parseInt(data?.get("number")?.toString() ?: "0"),
        balance = Integer.parseInt(data?.get("balance")?.toString() ?: "0"),
        role = Role.fromValue(
            Integer.parseInt(
                data?.get("role")?.toString() ?: "3"
            )
        ),
    )
}

suspend fun FirebaseFirestore.loadClub(clubId: String): Club? = suspendCoroutine { continuation ->
    getClubsCollection()
        .document(clubId)
        .get()
        .addOnSuccessListener { doc ->
            if (doc.id == clubId) {
                continuation.resume(doc.toClub())
            } else {
                continuation.resume(null)
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "loadClubData: ", e)
            continuation.resume(null)
        }
}

suspend fun FirebaseFirestore.loadPlayers(clubId: String): List<Player> {
    return loadPlayers(Club(clubId, "", ""))
}

suspend fun FirebaseFirestore.loadPlayers(
    club: Club
): List<Player> = suspendCoroutine { continuation ->
    getPlayersCollection()
        .whereEqualTo("clubId", club.clubId)
        .whereGreaterThanOrEqualTo("number", 0)
        .get()
        .addOnSuccessListener { docs ->
            val players = docs.map { doc ->
                Log.i(TAG, "loadPlayers: ${doc.data}")
                doc.toPlayer(club)
            }

            continuation.resume(players)
        }.addOnFailureListener { e ->
            Log.e(TAG, "loadPlayers: ", e)
            continuation.resume(emptyList())
        }
}

suspend fun FirebaseFirestore.loadAskingPlayers(
    clubId: String
): List<Player> = suspendCoroutine { continuation ->
    getPlayersCollection()
        .whereEqualTo("clubId", clubId)
        .whereLessThan("number", 0)
        .get()
        .addOnSuccessListener { docs ->
            val players = docs.map { doc ->
                Log.i(TAG, "loadPlayers: ${doc.data}")
                doc.toPlayer()
            }

            continuation.resume(players)
        }.addOnFailureListener { e ->
            Log.e(TAG, "loadPlayers: ", e)
            continuation.resume(emptyList())
        }
}

suspend fun FirebaseFirestore.loadUsersMap(
    players: List<Player>
): Map<String, User> = suspendCoroutine { continuation ->
    getUsersCollection()
        .whereIn(FieldPath.documentId(), players.map { it.user.userId })
        .get()
        .addOnSuccessListener { docs ->
            val usersMap = docs.associateBy(
                { it.id },
                { doc ->
                    User(
                        doc.id,
                        doc.data["email"]?.toString() ?: "",
                        doc.data["displayName"]?.toString() ?: "",
                        doc.data["avatarUrl"]?.toString() ?: ""
                    )
                }
            )
            continuation.resume(usersMap)
        }
}

suspend fun FirebaseFirestore.deletePlayer(
    playerId: String
): Boolean = suspendCoroutine { continuation ->
    getPlayersCollection()
        .document(playerId)
        .delete()
        .addOnCompleteListener { result ->
            Log.i(TAG, "deletePlayer: ${result.exception}")
            continuation.resume(result.isSuccessful)
        }
}

suspend fun FirebaseFirestore.saveMatch(
    clubId: String,
    match: Match
): SaveMatchResult = suspendCoroutine { continuation ->
    getMatchesCollection()
        .add(hashMapOf(
            "clubId" to clubId,
            "location" to match.location,
            "time" to match.time,
            "cost" to match.cost
        )).addOnCompleteListener { res ->
            if (res.isSuccessful) {
                continuation.resume(SaveMatchResult.Success)
            } else {
                Log.e(TAG, "saveMatch: ", res.exception)
                continuation.resume(SaveMatchResult.Failed("Save error!"))
            }
        }
}

suspend fun FirebaseFirestore.loadMatches(
    clubId: String
): List<Match> = suspendCoroutine { continuation ->
    getMatchesCollection()
        .whereEqualTo("clubId", clubId)
        .get()
        .addOnSuccessListener { docs ->
            continuation.resume(docs.map { it.toMatch() })
        }.addOnFailureListener { e ->
            Log.e(TAG, "loadMatches: ", e)
            continuation.resume(emptyList())
        }
}

suspend fun FirebaseFirestore.loadPlayerIdsByMatch(
    matchId: String
): List<String> = suspendCoroutine { continuation ->
    getMatchesCollection()
        .document(matchId)
        .get()
        .addOnSuccessListener { doc ->
            doc.data?.let { data ->
                Log.i(TAG, "loadPlayerIds: ${data["players"]}")
                val playersField = data["players"] as? List<*>
                if (playersField != null) {
                    continuation.resume(playersField.filterIsInstance<String>())
                } else {
                    continuation.resume(emptyList())
                }
            } ?: run {
                continuation.resume(emptyList())
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "loadPlayerIds: ", e)
            continuation.resume(emptyList())
        }
}

suspend fun FirebaseFirestore.getClubIdOfMatch(
    matchId: String
): String? = suspendCoroutine { continuation ->
    getMatchesCollection()
        .document(matchId)
        .get()
        .addOnSuccessListener { doc ->
            continuation.resume(doc.data?.get("clubId")?.toString())
        }.addOnFailureListener { e ->
            Log.e(TAG, "getJoinEnabled: ", e)
            continuation.resume(null)
        }
}

suspend fun FirebaseFirestore.getPlayerIdOfUserInClub(
    clubId: String,
    userId: String
): String? = suspendCoroutine { conti ->
    getPlayersCollection()
        .whereEqualTo("clubId", clubId)
        .whereEqualTo("userId", userId)
        .get()
        .addOnSuccessListener { docs ->
            if (docs.isEmpty) {
                conti.resume(null)
            } else {
                conti.resume(docs.first().id)
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "getJoinEnabled: ", e)
            conti.resume(null)
        }
}

suspend fun FirebaseFirestore.getPlayerInfoOfUserInClub(
    clubId: String,
    userId: String
): Player? = suspendCoroutine { conti ->
    getPlayersCollection()
        .whereEqualTo("clubId", clubId)
        .whereEqualTo("userId", userId)
        .get()
        .addOnSuccessListener { docs ->
            if (docs.isEmpty) {
                conti.resume(null)
            } else {
                conti.resume(docs.first().toPlayer())
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "getJoinEnabled: ", e)
            conti.resume(null)
        }
}

suspend fun FirebaseFirestore.getPlayersByIds(
    playerIds: List<String>
): List<Player> = suspendCoroutine { continuation ->
    getPlayersCollection()
        .whereIn(FieldPath.documentId(), playerIds)
        .get()
        .addOnSuccessListener { docs ->
            continuation.resume(docs.map { it.toPlayer() })
        }.addOnFailureListener { e ->
            Log.e(TAG, "getPlayersByIds: ", e)
            continuation.resume(emptyList())
        }
}