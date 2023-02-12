package com.rose.clubs.models.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.rose.clubs.data.Club
import com.rose.clubs.data.User
import com.rose.clubs.viewmodels.clublist.ClubsListModel
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "FirebaseClubsListModel"

class FirebaseClubsListModel(
    private val auth: FirebaseAuth = Firebase.auth,
    private val firestore: FirebaseFirestore = Firebase.firestore
) : ClubsListModel {

    override suspend fun getMyClubs(): List<Club> {
        val firebaseUser = auth.currentUser ?: return emptyList()

        val clubIds = getListClubIds(firebaseUser.uid)

        Log.i(TAG, "getMyClubs: $clubIds")

        if (clubIds.isEmpty()) {
            return emptyList()
        }

        return getClubListByIds(clubIds)
    }

    private suspend fun getListClubIds(
        userId: String
    ): List<String> = suspendCoroutine { continuation ->
        firestore.getPlayersCollection()
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { docs ->
                val clubIds = docs.map { doc ->
                    doc.data["clubId"]?.toString() ?: ""
                }.filter { it.isNotEmpty() }
                continuation.resume(clubIds)
            }.addOnFailureListener { e ->
                Log.e(TAG, "getListClubIds: ", e)
                continuation.resume(emptyList())
            }
    }

    private suspend fun getClubListByIds(
        clubIds: List<String>
    ): List<Club> = suspendCoroutine { continuation ->
        firestore.getClubsCollection()
            .whereIn(FieldPath.documentId(), clubIds)
            .get()
            .addOnSuccessListener { docs ->
                val clubs = docs.map { doc -> doc.toClub() }
                Log.i(TAG, "getClubListByIds: $clubs ${docs.size()}")
                continuation.resume(clubs)
            }.addOnFailureListener { e ->
                Log.e(TAG, "getClubListByIds: ", e)
                continuation.resume(emptyList())
            }
    }

    override suspend fun getUser(): User? {
        return auth.currentUser?.getUser()
    }
}