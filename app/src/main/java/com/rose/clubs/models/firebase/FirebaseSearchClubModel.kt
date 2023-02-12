package com.rose.clubs.models.firebase

import android.util.Log
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.rose.clubs.data.Club
import com.rose.clubs.viewmodels.searchclubs.SearchClubModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "FirebaseSearchClubModel"

class FirebaseSearchClubModel(
    private val firestore: FirebaseFirestore = Firebase.firestore
) : SearchClubModel {
    override suspend fun search(searchText: String): List<Club> = withContext(Dispatchers.IO) {
        listOf(
            async {
                searchByName(searchText)
            },
            async {
                searchById(searchText)
            }
        ).awaitAll().flatten()
    }

    private suspend fun searchByName(
        searchText: String
    ): List<Club> = suspendCoroutine { continuation ->
        firestore.getClubsCollection()
            .orderBy("name")
            .startAt(searchText)
            .endAt("${searchText}~")
            .get()
            .addOnSuccessListener { docs ->
                continuation.resume(docs.map { it.toClub() })
            }.addOnFailureListener { e ->
                Log.e(TAG, "searchByName: ", e)
                continuation.resume(emptyList())
            }
    }

    private suspend fun searchById(
        searchText: String
    ): List<Club> = suspendCoroutine { continuation ->
        firestore.getClubsCollection()
            .whereEqualTo(FieldPath.documentId(), searchText)
            .get()
            .addOnSuccessListener { docs ->
                continuation.resume(docs.map { it.toClub() })
            }.addOnFailureListener { e ->
                Log.e(TAG, "searchById: ", e)
                continuation.resume(emptyList())
            }
    }
}