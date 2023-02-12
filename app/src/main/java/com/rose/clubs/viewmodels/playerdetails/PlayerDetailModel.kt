package com.rose.clubs.viewmodels.playerdetails

import com.rose.clubs.data.Player
import com.rose.clubs.data.Role

interface PlayerDetailModel {
    suspend fun getPlayerInfo(playerId: String): Player?
    suspend fun getViewerRole(clubId: String): Role
}