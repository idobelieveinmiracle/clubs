package com.rose.clubs.models.firebase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.rose.clubs.data.User
import com.rose.clubs.viewmodels.login.LoginModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "FirebaseLoginModel"

class FirebaseLoginModel(
    private val auth: FirebaseAuth = Firebase.auth,
    private val firestore: FirebaseFirestore = Firebase.firestore,
    private val storage: FirebaseStorage = Firebase.storage,
    private val context: Context
) : LoginModel {
    override suspend fun login(
        email: String,
        password: String
    ): Boolean = suspendCoroutine { continuation ->
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                Log.i(TAG, "login: $email $password ${task.isSuccessful} ${task.exception}")
                continuation.resume(task.isSuccessful)
            }
    }

    override suspend fun register(user: User, password: String, imageUri: String): Boolean {
        val firebaseUser = createUser(user.email, password) ?: return false
        val photoUrl = uploadProfilePicture(imageUri, firebaseUser)
        Log.i(TAG, "register: $photoUrl")
        return updateProfileInfo(firebaseUser, user.copy(avatarUrl = photoUrl)) &&
                insertPublicProfileInfo(user.copy(userId = firebaseUser.uid, avatarUrl = photoUrl))
    }

    private suspend fun createUser(
        email: String,
        password: String
    ): FirebaseUser? = suspendCoroutine { continuation ->
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                continuation.resume(authResult.user)
            }.addOnFailureListener { e ->
                Log.e(TAG, "register: ", e)
                continuation.resume(null)
            }
    }

    private suspend fun uploadProfilePicture(
        imageUri: String,
        firebaseUser: FirebaseUser
    ): String = withContext(Dispatchers.IO) {
        Log.i(TAG, "uploadProfilePicture: ")
        val stream = context.contentResolver.openInputStream(Uri.parse(imageUri))
            ?: return@withContext ""
        val reference = storage.reference
        val avatarPath = "users/avatars/${firebaseUser.uid}_${System.currentTimeMillis()}"

        coroutineContext.job.invokeOnCompletion {
            stream.close()
        }

        val uploadTask = reference.child(avatarPath).putStream(stream)

        Log.i(TAG, "uploadProfilePicture: start run firebase stuff")
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
                Log.e(TAG, "uploadProfilePicture: ", e)
                continuation.resume("")
            }
        }
    }

    private suspend fun updateProfileInfo(
        firebaseUser: FirebaseUser,
        user: User
    ): Boolean = suspendCoroutine { continuation ->
        firebaseUser.updateProfile(
            UserProfileChangeRequest.Builder()
                .setDisplayName(user.displayName)
                .setPhotoUri(Uri.parse(user.avatarUrl))
                .build()
        ).addOnCompleteListener {
            Log.i(TAG, "updateProfileInfo: err ${it.exception}")
            continuation.resume(it.isSuccessful)
        }
    }

    private suspend fun insertPublicProfileInfo(
        user: User
    ): Boolean = suspendCoroutine { continuation ->
        firestore.getUsersCollection()
            .document(user.userId)
            .set(hashMapOf(
                "displayName" to user.displayName,
                "avatarUrl" to user.avatarUrl
            ))
            .addOnCompleteListener {
                continuation.resume(it.isSuccessful)
            }
    }

}