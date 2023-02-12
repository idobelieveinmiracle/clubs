package com.rose.clubs.viewmodels.clubdetails

import com.rose.clubs.data.Club
import com.rose.clubs.data.Player

interface ClubDetailsModel {
    suspend fun loadClubData(clubId: String): Club?
    suspend fun loadPlayers(clubId: String): List<Player>
    suspend fun askToJoin(clubId: String): Boolean
    suspend fun cancelAsk(clubId: String): Boolean
    suspend fun getActionType(clubId: String, players: List<Player>? = null): ActionType
}