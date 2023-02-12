package com.rose.clubs.viewmodels.addclub

interface AddClubModel {
    suspend fun addClub(name: String, imageUri: String): Boolean
}