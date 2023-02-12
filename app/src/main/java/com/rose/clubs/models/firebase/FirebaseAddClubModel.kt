package com.rose.clubs.models.firebase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.rose.clubs.data.Role
import com.rose.clubs.viewmodels.addclub.AddClubModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "FirebaseAddClubModel"

class FirebaseAddClubModel(
    private val auth: FirebaseAuth = Firebase.auth,
    private val firestore: FirebaseFirestore = Firebase.firestore,
    private val storage: FirebaseStorage = Firebase.storage,
    private val context: Context
) : AddClubModel {
    override suspend fun addClub(name: String, imageUri: String): Boolean {
        val user = auth.currentUser ?: return false
        val clubId = insertClub(name, user)

        if (clubId.isEmpty()) {
            return false
        }

        if (!initPlayer(clubId, user)) {
            return false
        }

        val imageUrl = uploadImage(clubId, imageUri)

        return saveImageUrl(clubId, imageUrl)
    }

    private suspend fun insertClub(
        name: String,
        user: FirebaseUser
    ): String = suspendCoroutine { continuation ->
        firestore.getClubsCollection()
            .add(hashMapOf(
                "name" to name,
                "owner" to user.uid,
            )).addOnSuccessListener {
                continuation.resume(it.id)
            }.addOnFailureListener { e ->
                Log.e(TAG, "saveClub: ", e)
                continuation.resume("")
            }
    }

    private suspend fun initPlayer(
        clubId: String,
        user: FirebaseUser
    ): Boolean = suspendCoroutine { continuation ->
        firestore.getPlayersCollection()
            .add(hashMapOf(
                "clubId" to clubId,
                "userId" to user.uid,
                "number" to 10,
                "role" to Role.CAPTAIN.value,
                "balance" to 0
            )).addOnCompleteListener {
                continuation.resume(true)
            }.addOnFailureListener { e ->
                Log.e(TAG, "initPlayer: ", e)
                continuation.resume(false)
            }
    }

    private suspend fun uploadImage(
        clubId: String,
        imageUri: String
    ): String = withContext(Dispatchers.IO) {
        Log.i(TAG, "uploadImage: starting...")
        val stream = context.contentResolver.openInputStream(Uri.parse(imageUri))
            ?: return@withContext ""
        val reference = storage.reference
        val imagePath = "clubs/avatars/${clubId}_${System.currentTimeMillis()}"

        coroutineContext.job.invokeOnCompletion {
            stream.close()
        }

        val uploadTask = reference.child(imagePath).putStream(stream)

        Log.i(TAG, "uploadImage: uploading")
        suspendCoroutine { continuation ->
            uploadTask.addOnSuccessListener { snapshot ->
                snapshot.metadata?.reference?.downloadUrl?.let { urlTask ->
                    urlTask.addOnCompleteListener {
                        if (it.isSuccessful) {
                            continuation.resume(it.result.toString())
                        } else {
                            continuation.resume("")
                        }
                    }
                } ?: run {
                    continuation.resume("")
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "uploadImage: ", e)
                continuation.resume("")
            }
        }
    }

    private suspend fun saveImageUrl(
        clubId: String,
        imageUrl: String
    ): Boolean = suspendCoroutine { continuation ->
        firestore.getClubsCollection()
            .document(clubId)
            .update("avatarUrl", imageUrl)
            .addOnSuccessListener { continuation.resume(true) }
            .addOnFailureListener { e ->
                Log.e(TAG, "saveImageUrl: ", e)
                continuation.resume(false)
            }
    }
}