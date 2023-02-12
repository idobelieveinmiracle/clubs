package com.rose.clubs.data

data class Player(
    val playerId: String,
    val user: User,
    val club: Club,
    val number: Int,
    val role: Role,
    val balance: Int
)