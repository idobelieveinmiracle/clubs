package com.rose.clubs.viewmodels.clublist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rose.clubs.data.Club
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ClubsListViewModel(private val clubsListModel: ClubsListModel) : ViewModel() {
    private val _clubs: MutableStateFlow<List<Club>> = MutableStateFlow(listOf())

    val clubs: StateFlow<List<Club>> get() = _clubs

    init {
        reloadMyClubs()
    }

    fun reloadMyClubs() {
        viewModelScope.launch {
            _clubs.value = clubsListModel.getMyClubs()
        }
    }

    class Factory(private val clubsListModel: ClubsListModel) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ClubsListViewModel(clubsListModel) as T
        }
    }
}