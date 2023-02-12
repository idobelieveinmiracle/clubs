package com.rose.clubs.viewmodels.login

import com.rose.clubs.data.User

interface LoginModel {
    suspend fun login(email: String, password: String): Boolean
    suspend fun register(user: User, password: String, imageUri: String): Boolean
}