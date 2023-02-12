package com.rose.clubs.models.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.rose.clubs.data.Club
import com.rose.clubs.data.Player
import com.rose.clubs.data.Role
import com.rose.clubs.data.User
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

fun DocumentSnapshot.toClub(): Club {
    return Club(
        id,
        data?.get("name")?.toString() ?: "",
        data?.get("avatarUrl")?.toString() ?: ""
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
