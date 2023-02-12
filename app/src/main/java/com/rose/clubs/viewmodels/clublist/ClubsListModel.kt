package com.rose.clubs.viewmodels.clublist

import com.rose.clubs.data.Club
import com.rose.clubs.data.User

interface ClubsListModel {
    suspend fun getMyClubs(): List<Club>
    suspend fun getUser(): User?
}