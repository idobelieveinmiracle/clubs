package com.rose.clubs.data

data class Match(
    val matchId: String,
    val location: String,
    val time: Long,
    val cost: Int,
    val players: List<Player>
)
