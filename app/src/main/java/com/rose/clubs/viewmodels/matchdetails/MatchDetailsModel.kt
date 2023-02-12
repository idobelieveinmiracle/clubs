package com.rose.clubs.viewmodels.matchdetails

import com.rose.clubs.data.Match
import com.rose.clubs.data.Player

interface MatchDetailsModel {
    suspend fun loadMatchInfo(matchId: String): Match?
    suspend fun loadPlayers(matchId: String): List<Player>
    suspend fun joinMatch(matchId: String): JoinMatchResult
    suspend fun getJoinEnabled(matchId: String, players: List<Player>): Boolean
}