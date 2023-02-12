package com.rose.clubs.viewmodels.searchclubs

import com.rose.clubs.data.Club

interface SearchClubModel {
    suspend fun search(searchText: String): List<Club>
}