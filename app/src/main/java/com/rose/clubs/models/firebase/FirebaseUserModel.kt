package com.rose.clubs.models.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rose.clubs.data.User
import com.rose.clubs.viewmodels.main.UserModel

class FirebaseUserModel(private val auth: FirebaseAuth = Firebase.auth) : UserModel {
    override suspend fun getUser(): User? {
        return auth.currentUser?.let {
            User(it.uid, it.email ?: "", it.displayName ?: "", it.photoUrl.toString())
        }
    }

    override suspend fun logout() {
        auth.signOut()
    }
}