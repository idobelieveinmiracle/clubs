package com.rose.clubs.viewmodels.addmatch

import com.rose.clubs.data.Match

interface AddMatchModel {
    suspend fun saveMatch(clubId: String, match: Match): SaveMatchResult
}