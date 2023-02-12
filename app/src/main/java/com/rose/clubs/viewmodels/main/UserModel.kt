package com.rose.clubs.viewmodels.main

import com.rose.clubs.data.User

interface UserModel {
    suspend fun getUser(): User?
    suspend fun logout()
}